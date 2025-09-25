package importApp.service;

import importApp.entity.AuthProvider;
import importApp.entity.UserEntity;
import importApp.exception.AccountLockedException;
import importApp.exception.BadCredentialsException;
import importApp.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;
    private static final String DUMMY_PASSWORD_HASH = "$2y$12$L6XNHqlesFuFzyGG3t4CmeCaEGrR2.vU2JZyThnAXF92iKyn2kLJq";

    @Autowired
    private UserMapper userMapper;

    @Lazy
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // ユーザーを登録するメソッド
    public void registerUser(UserEntity user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userMapper.insertUser(user);
    }

    // ログインユーザーを認証するメソッド（アカウントロック機能付き）
    public UserEntity loginUser(String username, String password) {
        UserEntity user = userMapper.findByUsername(username);

        // ユーザーが存在しない場合
        if (user == null) {
            logger.warn("Login attempt for non-existent user: {}", username);
            bCryptPasswordEncoder.matches(password, DUMMY_PASSWORD_HASH);
            return null;
        }

        // アカウントがロックされているかチェック
        boolean wasLocked = isAccountLocked(user);
        if (wasLocked) {
            bCryptPasswordEncoder.matches(password, DUMMY_PASSWORD_HASH);
            logger.warn("Login attempt for locked account: {}", username);
            throw new AccountLockedException("アカウントがロックされています。" +
                getRemainingLockTime(user) + "分後に再度お試しください。");
        }
        
        
        // パスワード検証
        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
            // ログイン成功：失敗回数をリセット
            if (user.getFailedLoginAttempts() > 0) {
                userMapper.resetFailedLoginAttempts(username);
                logger.info("Reset failed login attempts for user: {}", username);
            }
            return user;
        } else {
            // ログイン失敗：失敗回数を増加
            handleFailedLogin(username, user);
            return null;
        }
    }
    
    private boolean isAccountLocked(UserEntity user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }
        
        // ロック期間が過ぎている場合はリセット
        if (LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            userMapper.resetFailedLoginAttempts(user.getUsername());
            // userオブジェクトも更新（重要！）
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            logger.info("Account lock expired, reset failed attempts for user: {}", user.getUsername());
            return false;
        }
        
        return true;
    }
    
    private long getRemainingLockTime(UserEntity user) {
        if (user.getAccountLockedUntil() == null) {
            return 0;
        }
        long remainingMinutes = java.time.Duration.between(
            LocalDateTime.now(), 
            user.getAccountLockedUntil()
        ).toMinutes();
        return Math.max(1, remainingMinutes); // 最小1分を表示
    }
    
    private void handleFailedLogin(String username, UserEntity user) {
        int newAttemptCount = user.getFailedLoginAttempts() + 1;
        userMapper.incrementFailedLoginAttempts(username);
        
        if (newAttemptCount >= MAX_LOGIN_ATTEMPTS) {
            // アカウントをロック
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            userMapper.lockAccount(username, lockUntil);
            logger.warn("Account locked due to {} failed login attempts: {}", MAX_LOGIN_ATTEMPTS, username);
            throw new AccountLockedException("ログインに" + MAX_LOGIN_ATTEMPTS + "回失敗したため、アカウントがロックされました。" +
                LOCK_DURATION_MINUTES + "分後に再度お試しください。");
        } else {
            // 残り試行回数を通知
            int remainingAttempts = MAX_LOGIN_ATTEMPTS - newAttemptCount;
            logger.warn("Failed login attempt {} of {} for user: {}", newAttemptCount, MAX_LOGIN_ATTEMPTS, username);
            throw new BadCredentialsException("ユーザー名またはパスワードが正しくありません。" +
                "あと" + remainingAttempts + "回失敗するとアカウントがロックされます。");
        }
    }

    public boolean deleteUser(Long id) {
        int result = userMapper.markUserAsDeleted(id);
        return result > 0;
    }

    // パスワードを変更するメソッド
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        // ユーザーの存在確認
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }

        // OAuth ユーザーはパスワード変更不可
        if (user.getProvider() == AuthProvider.GOOGLE) {
            logger.warn("Password change attempt for OAuth user: userId={}, provider={}", userId, user.getProvider());
            throw new IllegalStateException("Password change is not allowed for OAuth users");
        }

        // ローカルユーザーのパスワード検証
        if (user.getPassword() == null || !bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // 現在のパスワードが間違っている
        }
        
        // 新しいパスワードを暗号化
        String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);
        
        // パスワード更新
        int result = userMapper.updatePassword(userId, encodedNewPassword);
        return result > 0;
    }

    @Transactional
    public UserEntity processOAuthPostLogin(String email, String googleId, String name, 
                                          String picture, Boolean emailVerified) {
        
        logger.info("Processing OAuth2 login: email={}, googleId={}", email, googleId);
        
        UserEntity existingUser = userMapper.findByGoogleId(googleId);
        
        if (existingUser != null) {
            logger.info("Existing Google user found: userId={}", existingUser.getUser_id());
            
            // 更新対象の項目のみ設定（既存の値は保持）
            if (picture != null && !picture.equals(existingUser.getProfileImageUrl())) {
                existingUser.setProfileImageUrl(picture);
            }
            if (emailVerified != null && !emailVerified.equals(existingUser.getIsEmailVerified())) {
                existingUser.setIsEmailVerified(emailVerified);
            }
            
            // usernameは必ず既存の値を保持（nullで上書きしない）
            // DBから取得した値がそのまま保持される
            
            logger.info("Updating user profile for userId: {}", existingUser.getUser_id());
            userMapper.updateUser(existingUser);
            return existingUser;
        }

        UserEntity localUser = userMapper.findByEmail(email);
        if (localUser != null && localUser.getProvider() == AuthProvider.LOCAL) {
            logger.info("Linking existing local user with Google account: userId={}", localUser.getUser_id());
            localUser.setGoogleId(googleId);
            localUser.setProvider(AuthProvider.GOOGLE);
            localUser.setProfileImageUrl(picture);
            localUser.setIsEmailVerified(emailVerified);
            userMapper.updateUser(localUser);
            return localUser;
        }

        logger.info("Creating new Google user: email={}", email);
        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setUsername(generateUsernameFromEmail(email));
        newUser.setProvider(AuthProvider.GOOGLE);
        newUser.setGoogleId(googleId);
        newUser.setProfileImageUrl(picture);
        newUser.setIsEmailVerified(emailVerified);

        userMapper.insertUser(newUser);
        logger.info("New Google user created: userId={}", newUser.getUser_id());
        return newUser;
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int suffix = 1;
        
        while (userMapper.findByUsername(username) != null) {
            username = baseUsername + suffix++;
        }
        
        logger.info("Generated username: {} from email: {}", username, email);
        return username;
    }
}

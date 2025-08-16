package importApp.service;

import importApp.entity.AuthProvider;
import importApp.entity.UserEntity;
import importApp.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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

    // ログインユーザーを認証するメソッド
    public UserEntity loginUser(String username, String password) {
        UserEntity user = userMapper.findByUsername(username);
        if (user != null && bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return user; // 認証成功
        }
        return null; // 認証失敗
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

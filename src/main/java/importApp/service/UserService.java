package importApp.service;

import importApp.entity.UserEntity;
import importApp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

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
        // ユーザーの存在確認とパスワード検証
        UserEntity user = userMapper.findById(userId);
        if (user == null || !bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // 現在のパスワードが間違っている
        }
        
        // 新しいパスワードを暗号化
        String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);
        
        // パスワード更新
        int result = userMapper.updatePassword(userId, encodedNewPassword);
        return result > 0;
    }
}

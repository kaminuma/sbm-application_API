package importApp.service;

import importApp.entity.UserEntity;
import importApp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
    public UserEntity loginUser(String email, String password) {
        UserEntity user = userMapper.findByUsername(email);
        if (user != null && bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return user; // 認証成功
        }
        return null; // 認証失敗
    }
}
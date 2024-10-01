package importApp.service;

import importApp.entity.UserEntity;
import importApp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public void registerUser(UserEntity user) {
        userMapper.insertUser(user);
    }
}

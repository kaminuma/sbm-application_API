package importApp.mapper;

import importApp.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {
    void insertUser(UserEntity user);
    UserEntity findByEmail(String email);
    UserEntity findByUsername(String username);
}
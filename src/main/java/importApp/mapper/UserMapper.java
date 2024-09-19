package importApp.mapper;

import importApp.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    UserEntity findByUsername (@Param("username") String username, @Param("password") String password);
}

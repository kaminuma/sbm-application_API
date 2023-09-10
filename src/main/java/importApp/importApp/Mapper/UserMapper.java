package importApp.importApp.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import importApp.importApp.Entity.UserEntity;

@Mapper
public interface UserMapper {
    UserEntity findByUsername(@Param("username") String username, @Param("password") String password);
}

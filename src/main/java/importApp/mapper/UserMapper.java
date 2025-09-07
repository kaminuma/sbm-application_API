package importApp.mapper;

import importApp.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    void insertUser(UserEntity user);
    UserEntity findByEmail(String email);
    UserEntity findByUsername(String username);
    UserEntity findById(@Param("id") Long id);
    int markUserAsDeleted(@Param("id") Long id);
    int updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);
    
    UserEntity findByGoogleId(@Param("googleId") String googleId);
    
    void updateUser(UserEntity user);
    
    int incrementFailedLoginAttempts(@Param("username") String username);
    int resetFailedLoginAttempts(@Param("username") String username);
    int lockAccount(@Param("username") String username, @Param("lockedUntil") LocalDateTime lockedUntil);
    
    void updateProfileImageUrl(@Param("userId") Long userId, @Param("profileImageUrl") String profileImageUrl);

}
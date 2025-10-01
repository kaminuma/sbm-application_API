package importApp.mapper;

import importApp.entity.RefreshTokenEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface RefreshTokenMapper {
    void insertRefreshToken(RefreshTokenEntity refreshToken);
    RefreshTokenEntity findByToken(@Param("token") String token);
    RefreshTokenEntity findByUserId(@Param("userId") Long userId);
    int deleteByToken(@Param("token") String token);
    int deleteByUserId(@Param("userId") Long userId);
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
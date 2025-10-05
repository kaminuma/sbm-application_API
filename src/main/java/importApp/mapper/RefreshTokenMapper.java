package importApp.mapper;

import importApp.entity.RefreshTokenEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RefreshTokenMapper {
    void insertRefreshToken(RefreshTokenEntity refreshToken);
    RefreshTokenEntity findByToken(@Param("token") String token);
    RefreshTokenEntity findByUserId(@Param("userId") Long userId);
    List<RefreshTokenEntity> findByUserIdOrderByLastUsedDesc(@Param("userId") Long userId);
    int deleteByToken(@Param("token") String token);
    int deleteByUserId(@Param("userId") Long userId);
    int deleteByUserIdExceptToken(@Param("userId") Long userId, @Param("exceptToken") String exceptToken);
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
    int updateLastUsedAt(@Param("token") String token, @Param("lastUsedAt") LocalDateTime lastUsedAt);
}
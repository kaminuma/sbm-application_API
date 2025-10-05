package importApp.service;

import importApp.entity.RefreshTokenEntity;
import importApp.mapper.RefreshTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    private static final int REFRESH_TOKEN_LENGTH = 32;
    private static final int REFRESH_TOKEN_VALIDITY_MINUTES = 5;

    public RefreshTokenEntity createRefreshToken(Long userId) {
        // 既存のリフレッシュトークンを削除（ユーザーごとに1つのみ）
        refreshTokenMapper.deleteByUserId(userId);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUser_id(userId);
        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpires_at(LocalDateTime.now().plusMinutes(REFRESH_TOKEN_VALIDITY_MINUTES));

        refreshTokenMapper.insertRefreshToken(refreshToken);
        return refreshToken;
    }

    public RefreshTokenEntity verifyRefreshToken(String token) {
        RefreshTokenEntity refreshToken = refreshTokenMapper.findByToken(token);

        if (refreshToken == null) {
            return null;
        }

        if (refreshToken.getExpires_at().isBefore(LocalDateTime.now())) {
            // 期限切れのトークンを削除
            refreshTokenMapper.deleteByToken(token);
            return null;
        }

        return refreshToken;
    }

    public RefreshTokenEntity rotateRefreshToken(String oldToken) {
        // 期限内のトークンをそのまま再利用する
        return verifyRefreshToken(oldToken);
    }

    public void deleteRefreshToken(String token) {
        refreshTokenMapper.deleteByToken(token);
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        refreshTokenMapper.deleteByUserId(userId);
    }

    public void cleanupExpiredTokens() {
        refreshTokenMapper.deleteExpiredTokens(LocalDateTime.now());
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[REFRESH_TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}

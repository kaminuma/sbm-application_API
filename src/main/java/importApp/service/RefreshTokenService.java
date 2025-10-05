package importApp.service;

import importApp.entity.RefreshTokenEntity;
import importApp.entity.DeviceType;
import importApp.mapper.RefreshTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    private static final int REFRESH_TOKEN_LENGTH = 32;
    private static final int REFRESH_TOKEN_VALIDITY_MINUTES = 5;
    private static final int MAX_TOKENS_PER_USER = 5; // 1ユーザーあたりの最大デバイス数

    public RefreshTokenEntity createRefreshToken(Long userId) {
        return createRefreshToken(userId, null, null, null);
    }

    public RefreshTokenEntity createRefreshToken(Long userId, String userAgent, String ipAddress, String deviceInfo) {
        // 古いトークンのクリーンアップ（制限数を超えた場合）
        cleanupOldTokens(userId);

        DeviceType deviceType = DeviceType.fromUserAgent(userAgent);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUser_id(userId);
        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpires_at(LocalDateTime.now().plusMinutes(REFRESH_TOKEN_VALIDITY_MINUTES));
        refreshToken.setDeviceType(deviceType.getValue());
        refreshToken.setDeviceInfo(deviceInfo != null ? deviceInfo : userAgent);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setLastUsedAt(LocalDateTime.now());

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

        // 最終使用時刻を更新
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenMapper.updateLastUsedAt(token, LocalDateTime.now());

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

    private void cleanupOldTokens(Long userId) {
        List<RefreshTokenEntity> userTokens = refreshTokenMapper.findByUserIdOrderByLastUsedDesc(userId);

        if (userTokens.size() >= MAX_TOKENS_PER_USER) {
            // 最大数に達している場合、古いトークンを削除
            for (int i = MAX_TOKENS_PER_USER - 1; i < userTokens.size(); i++) {
                refreshTokenMapper.deleteByToken(userTokens.get(i).getToken());
            }
        }
    }

    public List<RefreshTokenEntity> getUserActiveTokens(Long userId) {
        return refreshTokenMapper.findByUserIdOrderByLastUsedDesc(userId);
    }

    public void revokeAllUserTokensExcept(Long userId, String exceptToken) {
        refreshTokenMapper.deleteByUserIdExceptToken(userId, exceptToken);
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[REFRESH_TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}

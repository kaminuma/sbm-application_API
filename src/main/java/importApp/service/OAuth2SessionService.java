package importApp.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
public class OAuth2SessionService {

    private final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public OAuth2SessionService() {
        // 5分毎に期限切れセッションをクリーンアップ
        scheduler.scheduleWithFixedDelay(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    public String createSession(String jwt, String userId) {
        String sessionId = UUID.randomUUID().toString();
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10); // 10分有効
        
        sessions.put(sessionId, new SessionData(jwt, userId, expiryTime));
        return sessionId;
    }

    public SessionData getAndRemoveSession(String sessionId) {
        SessionData session = sessions.remove(sessionId);
        if (session == null || session.isExpired()) {
            return null;
        }
        return session;
    }

    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
    }

    public static class SessionData {
        private final String jwt;
        private final String userId;
        private final long expiryTime;

        public SessionData(String jwt, String userId, long expiryTime) {
            this.jwt = jwt;
            this.userId = userId;
            this.expiryTime = expiryTime;
        }

        public String getJwt() { return jwt; }
        public String getUserId() { return userId; }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long currentTime) {
            return currentTime > expiryTime;
        }
    }
}
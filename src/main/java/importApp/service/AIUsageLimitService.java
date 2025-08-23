package importApp.service;

import importApp.entity.AIUsageLimit;
import importApp.entity.UserEntity;
import importApp.mapper.AIUsageLimitMapper;
import importApp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIUsageLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIUsageLimitService.class);
    private static final String PROVIDER_GEMINI = "gemini";
    
    @Autowired
    private AIUsageLimitMapper aiUsageLimitMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${ai.limits.enabled:true}")
    private boolean limitsEnabled;
    
    @Value("${ai.limits.debug-mode:false}")
    private boolean debugMode;
    
    @Value("${ai.limits.debug-user-ids:}")
    private String debugUserIdsStr;
    
    @Value("${ai.limits.default-daily:5}")
    private int defaultDailyLimit;
    
    @Value("${ai.limits.default-monthly:50}")
    private int defaultMonthlyLimit;
    
    private List<Integer> debugUserIds;
    
    @PostConstruct
    public void init() {
        // デバッグユーザーIDのパース
        debugUserIds = parseDebugUserIds(debugUserIdsStr);
    }
    
    private List<Integer> parseDebugUserIds(String userIdsStr) {
        List<Integer> result = new ArrayList<>();
        if (userIdsStr != null && !userIdsStr.trim().isEmpty()) {
            try {
                String[] ids = userIdsStr.split(",");
                for (String id : ids) {
                    String trimmedId = id.trim();
                    if (!trimmedId.isEmpty()) {
                        result.add(Integer.parseInt(trimmedId));
                    }
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid debug user IDs format: {}. Using empty list.", userIdsStr);
            }
        }
        return result;
    }
    
    /**
     * AI利用可能かチェック（現在は日次制限のみチェック）
     */
    public boolean canUseAI(Integer userId) {
        if (!limitsEnabled) {
            logger.debug("AI usage limits are disabled");
            return true;
        }
        
        // デバッグモードチェック
        if (debugMode) {
            logger.debug("Debug mode enabled - unlimited usage for all users");
            return true;
        }
        
        // 特定ユーザーの無制限チェック
        if (debugUserIds != null && debugUserIds.contains(userId)) {
            logger.debug("User {} is in debug user list - unlimited usage", userId);
            return true;
        }
        
        try {
            AIUsageLimit usage = aiUsageLimitMapper.findByUserAndDate(
                userId, PROVIDER_GEMINI, LocalDate.now());
            
            if (usage == null) {
                logger.debug("No usage record found for user {} - first use today", userId);
                return true;  // 本日初回利用
            }
            
            UserEntity user = userMapper.findById(Long.valueOf(userId));
            if (user == null) {
                logger.warn("User not found: {}", userId);
                return false;
            }
            
            // 日次制限チェック（現在はこれのみ使用）
            int dailyLimit = user.getAiDailyLimit() != null ? user.getAiDailyLimit() : defaultDailyLimit;
            boolean canUse = usage.getDailyCount() < dailyLimit;
            
            // TODO: 将来的に月次制限を有効化する場合は以下のコメントを解除
            // int monthlyLimit = user.getAiMonthlyLimit() != null ? user.getAiMonthlyLimit() : defaultMonthlyLimit;
            // canUse = canUse && usage.getMonthlyCount() < monthlyLimit;
            
            if (!canUse) {
                logger.warn("AI usage limit reached for user {}: daily count={}/{}", 
                    userId, usage.getDailyCount(), dailyLimit);
            }
            
            return canUse;
            
        } catch (Exception e) {
            logger.error("Error checking AI usage limit for user {}", userId, e);
            // エラー時は安全側に倒して利用不可とする
            return false;
        }
    }
    
    /**
     * AI利用回数をインクリメント
     */
    @Transactional
    public void incrementUsage(Integer userId) {
        try {
            aiUsageLimitMapper.incrementCount(userId, PROVIDER_GEMINI, LocalDate.now());
            
            // ログ出力
            AIUsageLimit usage = aiUsageLimitMapper.findByUserAndDate(
                userId, PROVIDER_GEMINI, LocalDate.now());
            if (usage != null) {
                logger.info("AI usage incremented for user {}: daily={}, monthly={}", 
                    userId, usage.getDailyCount(), usage.getMonthlyCount());
            }
            
        } catch (Exception e) {
            logger.error("Error incrementing AI usage for user {}", userId, e);
            // インクリメント失敗は続行（利用は許可されたので）
        }
    }
    
    /**
     * AI利用状況を取得
     */
    public Map<String, Object> getUsageInfo(Integer userId) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            AIUsageLimit usage = aiUsageLimitMapper.findByUserAndDate(
                userId, PROVIDER_GEMINI, LocalDate.now());
            UserEntity user = userMapper.findById(Long.valueOf(userId));
            
            if (user == null) {
                logger.warn("User not found: {}", userId);
                info.put("error", "User not found");
                return info;
            }
            
            int dailyLimit = user.getAiDailyLimit() != null ? user.getAiDailyLimit() : defaultDailyLimit;
            int monthlyLimit = user.getAiMonthlyLimit() != null ? user.getAiMonthlyLimit() : defaultMonthlyLimit;
            
            info.put("dailyUsed", usage != null ? usage.getDailyCount() : 0);
            info.put("dailyLimit", dailyLimit);
            info.put("dailyRemaining", dailyLimit - (usage != null ? usage.getDailyCount() : 0));
            
            // 月次情報も含めるが、現在は参照のみ
            info.put("monthlyUsed", usage != null ? usage.getMonthlyCount() : 0);
            info.put("monthlyLimit", monthlyLimit);
            info.put("monthlyRemaining", monthlyLimit - (usage != null ? usage.getMonthlyCount() : 0));
            
            info.put("provider", PROVIDER_GEMINI);
            info.put("canUseToday", canUseAI(userId));
            info.put("nextResetDate", LocalDate.now().plusDays(1).toString());
            
            // デバッグ情報追加
            info.put("debugMode", debugMode);
            info.put("isDebugUser", debugUserIds != null && debugUserIds.contains(userId));
            info.put("limitsEnabled", limitsEnabled);
            
        } catch (Exception e) {
            logger.error("Error getting usage info for user {}", userId, e);
            info.put("error", "Failed to get usage info");
        }
        
        return info;
    }
}
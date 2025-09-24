package importApp.controller;

import importApp.dto.AIAnalysisRequestDto;
import importApp.dto.AIAnalysisResponseDto;
import importApp.service.AIService;
import importApp.service.AIUsageLimitService;
import importApp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIService aiService;
    
    @Autowired
    private AIUsageLimitService aiUsageLimitService;
    
    @Autowired
    private JwtService jwtService;

    @PostMapping("/analysis")
    public ResponseEntity<AIAnalysisResponseDto> generateAnalysis(
            @Valid @RequestBody AIAnalysisRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("AI分析リクエスト受信: focus={}, detail={}, style={}", 
                   request.getAnalysisFocus(), request.getDetailLevel(), request.getResponseStyle());
        
        try {
            // JWTトークンからユーザーIDを抽出
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format");
                AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
                errorResponse.setSuccess(false);
                errorResponse.setError("認証ヘッダーの形式が無効です");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userId;
            
            try {
                userId = jwtService.extractUserId(token);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token has expired");
                AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
                errorResponse.setSuccess(false);
                errorResponse.setError("認証トークンの有効期限が切れています。再ログインしてください。");
                return ResponseEntity.status(401).body(errorResponse);
            } catch (SignatureException e) {
                logger.warn("Invalid JWT token signature");
                AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
                errorResponse.setSuccess(false);
                errorResponse.setError("認証トークンが無効です。");
                return ResponseEntity.status(401).body(errorResponse);
            } catch (Exception e) {
                logger.warn("JWT token validation error: {}", e.getMessage());
                AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
                errorResponse.setSuccess(false);
                errorResponse.setError("認証エラーが発生しました。");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            // AI利用制限チェック
            Integer userIdInt = Integer.valueOf(userId);
            if (!aiUsageLimitService.canUseAI(userIdInt)) {
                logger.warn("AI usage limit reached for user {}", userId);
                AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
                errorResponse.setSuccess(false);
                errorResponse.setError("本日のAI分析利用回数が上限に達しました。明日再度お試しください。");
                
                // 使用状況情報を含める
                Map<String, Object> usageInfo = aiUsageLimitService.getUsageInfo(userIdInt);
                errorResponse.setUsageInfo(usageInfo);
                
                return ResponseEntity.status(429).body(errorResponse);
            }
            
            // AI分析実行
            AIAnalysisResponseDto response = aiService.generateAnalysis(request, userId);
            
            // 成功時のみ利用回数をインクリメント
            if (response.isSuccess()) {
                aiUsageLimitService.incrementUsage(userIdInt);
                
                // 使用状況情報を含める
                Map<String, Object> usageInfo = aiUsageLimitService.getUsageInfo(userIdInt);
                response.setUsageInfo(usageInfo);
            }
            
            logger.info("AI分析完了: userId={}, success={}", userId, response.isSuccess());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("不正なリクエスト: {}", e.getMessage());
            AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
            errorResponse.setSuccess(false);
            errorResponse.setError("リクエスト内容に不正があります。入力内容を確認してください。");
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("AI分析エラー", e);
            AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
            errorResponse.setSuccess(false);
            errorResponse.setError("AI分析中にエラーが発生しました。しばらく待ってから再試行してください。");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * AI利用状況を取得
     */
    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getAIUsage(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // JWTトークンからユーザーIDを抽出
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format");
                return ResponseEntity.badRequest().body(Map.of("error", "認証ヘッダーの形式が無効です"));
            }
            
            String token = authHeader.replace("Bearer ", "");
            String userId;
            
            try {
                userId = jwtService.extractUserId(token);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token has expired");
                return ResponseEntity.status(401).body(Map.of("error", "認証トークンの有効期限が切れています"));
            } catch (SignatureException e) {
                logger.warn("Invalid JWT token signature");
                return ResponseEntity.status(401).body(Map.of("error", "認証トークンが無効です"));
            } catch (Exception e) {
                logger.warn("JWT token validation error: {}", e.getMessage());
                return ResponseEntity.status(401).body(Map.of("error", "認証エラーが発生しました"));
            }
            
            // 使用状況取得
            Map<String, Object> usageInfo = aiUsageLimitService.getUsageInfo(Integer.valueOf(userId));
            logger.info("AI usage info retrieved for user {}", userId);
            
            return ResponseEntity.ok(usageInfo);
            
        } catch (Exception e) {
            logger.error("Error getting AI usage info", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "使用状況の取得に失敗しました"));
        }
    }
    
}
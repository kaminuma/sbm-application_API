package importApp.controller;

import importApp.dto.AIAnalysisRequestDto;
import importApp.dto.AIAnalysisResponseDto;
import importApp.service.AIService;
import importApp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*")
public class AIController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIService aiService;
    
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
            String userId = jwtService.extractUserId(token);
            
            // AI分析実行
            AIAnalysisResponseDto response = aiService.generateAnalysis(request, userId);
            
            logger.info("AI分析完了: userId={}, success={}", userId, response.isSuccess());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("不正なリクエスト: {}", e.getMessage());
            AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
            errorResponse.setSuccess(false);
            errorResponse.setError("リクエスト内容に不正があります: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("AI分析エラー", e);
            AIAnalysisResponseDto errorResponse = new AIAnalysisResponseDto();
            errorResponse.setSuccess(false);
            errorResponse.setError("AI分析中にエラーが発生しました。しばらく待ってから再試行してください。");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
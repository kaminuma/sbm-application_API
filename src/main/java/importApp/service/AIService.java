package importApp.service;

import importApp.dto.AIAnalysisRequestDto;
import importApp.dto.AIAnalysisResponseDto;
import importApp.entity.ActivityGetEntity;
import importApp.entity.MoodRecordEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Flux;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:streamGenerateContent}")
    private String geminiApiUrl;


    private final WebClient webClient;
    private final ActivityService activityService;
    private final MoodRecordService moodRecordService;
    private final ObjectMapper objectMapper;

    public AIService(WebClient webClient,
                     ActivityService activityService,
                     MoodRecordService moodRecordService,
                     ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.activityService = activityService;
        this.moodRecordService = moodRecordService;
        this.objectMapper = objectMapper;
    }

    public AIAnalysisResponseDto generateAnalysis(AIAnalysisRequestDto request, String userId) {
        
        // Gemini APIキー検証
        if (!isValidGeminiApiKey()) {
            logger.error("Gemini APIキーが無効または未設定です");
            throw new IllegalStateException("AI service is not available due to configuration error");
        }
        
        try {
            // 1. データ取得
            List<ActivityGetEntity> activities = activityService.getActivitiesByUserAndDateRange(
                userId, request.getStartDate(), request.getEndDate());
            List<MoodRecordEntity> moodRecords = moodRecordService.getMoodRecordsByUserAndDateRange(
                userId, request.getStartDate(), request.getEndDate());

            logger.info("データ取得完了: activities={}, moods={}", activities.size(), moodRecords.size());

            // 2. データ存在チェック
            if (activities.isEmpty() && moodRecords.isEmpty()) {
                AIAnalysisResponseDto response = new AIAnalysisResponseDto();
                response.setSuccess(false);
                response.setError("指定期間にデータが見つかりません。活動や気分の記録を追加してから再試行してください。");
                return response;
            }

            // 3. プロンプト生成
            String prompt = generatePrompt(request, activities, moodRecords);
            logger.info("プロンプトサイズ: {} 文字, 活動記録: {} 件, 気分記録: {} 件",
                       prompt.length(), activities.size(), moodRecords.size());

            // 4. Gemini API呼び出し
            return callGeminiAPI(prompt);

        } catch (Exception e) {
            logger.error("AI分析処理エラー", e);
            AIAnalysisResponseDto response = new AIAnalysisResponseDto();
            response.setSuccess(false);
            response.setError("AI分析処理中にエラーが発生しました。しばらく待ってから再試行してください。");
            return response;
        }
    }

    private String generatePrompt(AIAnalysisRequestDto request, 
                                  List<ActivityGetEntity> activities, 
                                  List<MoodRecordEntity> moodRecords) {
        
        StringBuilder prompt = new StringBuilder();
        
        // 基本指示
        prompt.append("あなたは親しみやすい生活コーチです。以下のデータを分析してユーザーにインサイトを提供してください：\n\n");
        
        // 設定に応じた分析方針
        switch (request.getAnalysisFocus()) {
            case "MOOD_FOCUSED":
                prompt.append("【分析方針】感情とメンタル面に重点を置いて分析してください。\n");
                break;
            case "ACTIVITY_FOCUSED":
                prompt.append("【分析方針】活動パターンと時間配分に重点を置いて分析してください。\n");
                break;
            case "BALANCED":
                prompt.append("【分析方針】気分と活動の両方をバランス良く分析してください。\n");
                break;
            case "WELLNESS_FOCUSED":
                prompt.append("【分析方針】健康とウェルビーイングの観点で分析してください。\n");
                break;
        }
        
        // 応答スタイル
        switch (request.getResponseStyle()) {
            case "FRIENDLY":
                prompt.append("【口調】親しみやすくフレンドリーな口調で回答してください。\n");
                break;
            case "PROFESSIONAL":
                prompt.append("【口調】客観的で専門的な口調で回答してください。\n");
                break;
            case "ENCOURAGING":
                prompt.append("【口調】ポジティブで励ましを含む口調で回答してください。\n");
                break;
            case "CASUAL":
                prompt.append("【口調】親しい友人のような気軽な口調で回答してください。\n");
                break;
        }
        
        // 詳細レベル
        switch (request.getDetailLevel()) {
            case "CONCISE":
                prompt.append("【詳細度】要点を絞った簡潔な分析にしてください。\n");
                break;
            case "STANDARD":
                prompt.append("【詳細度】適度な詳細度で分析してください。\n");
                break;
            case "DETAILED":
                prompt.append("【詳細度】包括的で詳しい分析にしてください。\n");
                break;
        }
        
        prompt.append("\n");
        
        // データセクション
        prompt.append(formatActivityData(activities));
        prompt.append("\n");
        prompt.append(formatMoodData(moodRecords));
        prompt.append("\n");
        
        // 出力指示
        prompt.append("以下のJSON形式で回答してください：\n");
        prompt.append("{\n");
        prompt.append("  \"overall_summary\": \"全体的な分析サマリー（100-300文字）\",\n");
        prompt.append("  \"mood_insights\": \"感情・気分に関する洞察（100-200文字）\",\n");
        prompt.append("  \"activity_insights\": \"活動パターンに関する洞察（100-200文字）\",\n");
        prompt.append("  \"recommendations\": \"具体的な改善提案・アドバイス（100-250文字）\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    private String formatActivityData(List<ActivityGetEntity> activities) {
        if (activities.isEmpty()) {
            return "### 活動記録\n記録された活動はありません。\n";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("### 活動記録詳細\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (ActivityGetEntity activity : activities) {
            formatted.append(String.format("%s %s-%s: %s [%s] - %s\n",
                activity.getDate().toString(),
                activity.getStart() != null ? activity.getStart().toString() : "",
                activity.getEnd() != null ? activity.getEnd().toString() : "",
                activity.getTitle(),
                activity.getCategory(),
                activity.getContents() != null ? activity.getContents() : ""));
        }
        
        // カテゴリ別集計
        Map<String, Long> categoryStats = activities.stream()
            .collect(Collectors.groupingBy(
                ActivityGetEntity::getCategory,
                Collectors.counting()
            ));
        
        formatted.append("\n### カテゴリ別集計\n");
        categoryStats.forEach((category, count) -> 
            formatted.append(String.format("%s: %d件\n", category, count))
        );
        
        return formatted.toString();
    }

    private String formatMoodData(List<MoodRecordEntity> moodRecords) {
        if (moodRecords.isEmpty()) {
            return "### 感情記録\n記録された感情データはありません。\n";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("### 感情記録\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (MoodRecordEntity mood : moodRecords) {
            String emoji = getMoodEmoji(mood.getMood());
            formatted.append(String.format("%s: %s (%d/5) - %s\n",
                mood.getDate().toString(),
                emoji,
                mood.getMood(),
                mood.getNote() != null ? mood.getNote() : ""));
        }
        
        // 統計情報
        double avgMood = moodRecords.stream()
            .mapToInt(MoodRecordEntity::getMood)
            .average()
            .orElse(0.0);
        
        int maxMood = moodRecords.stream()
            .mapToInt(MoodRecordEntity::getMood)
            .max()
            .orElse(0);
            
        int minMood = moodRecords.stream()
            .mapToInt(MoodRecordEntity::getMood)
            .min()
            .orElse(0);
        
        formatted.append(String.format("\n### 感情統計\n"));
        formatted.append(String.format("平均ムード: %.1f/5\n", avgMood));
        formatted.append(String.format("最高: %d 最低: %d\n", maxMood, minMood));
        
        return formatted.toString();
    }

    private String getMoodEmoji(int moodLevel) {
        switch (moodLevel) {
            case 1: return "😞";
            case 2: return "😔";
            case 3: return "😐";
            case 4: return "😊";
            case 5: return "😄";
            default: return "😐";
        }
    }

    private AIAnalysisResponseDto callGeminiAPI(String prompt) {
        // Gemini APIリクエストボディ構築
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 2000
            )
        );

        try {
            logger.info("Gemini Streaming API呼び出し開始");

            // WebClientでリアルタイムStreaming処理
            Flux<String> streamingFlux = webClient.post()
                .uri(geminiApiUrl)
                .header("x-goog-api-key", geminiApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class);

            // ストリーミングチャンクをリアルタイム処理
            StringBuilder fullResponse = new StringBuilder();
            streamingFlux
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> logger.info("ストリーミング受信完了"))
                .doOnError(error -> logger.error("ストリーミングエラー: {}", error.getMessage()))
                .blockLast(); // 同期的に完了を待つ

            return parseStreamingResponse(fullResponse.toString());

        } catch (Exception e) {
            logger.error("Gemini API呼び出しエラー: {}", e.getMessage(), e);
            throw new RuntimeException("AI分析サービスとの通信でエラーが発生しました。");
        }
    }

    private AIAnalysisResponseDto parseStreamingResponse(String streamingResponse) {
        try {
            logger.info("ストリーミングレスポンス解析開始");

            // 型安全なストリーミングレスポンス解析
            TypeReference<List<GeminiStreamChunk>> typeRef = new TypeReference<List<GeminiStreamChunk>>() {};
            List<GeminiStreamChunk> chunks = objectMapper.readValue(streamingResponse, typeRef);

            // 各チャンクのテキストを結合
            StringBuilder fullText = new StringBuilder();
            for (GeminiStreamChunk chunk : chunks) {
                if (chunk != null && chunk.getCandidates() != null && !chunk.getCandidates().isEmpty()) {
                    GeminiCandidate candidate = chunk.getCandidates().get(0);
                    if (candidate != null && candidate.getContent() != null &&
                        candidate.getContent().getParts() != null &&
                        !candidate.getContent().getParts().isEmpty()) {
                        String text = candidate.getContent().getParts().get(0).getText();
                        if (text != null) {
                            fullText.append(text);
                        }
                    }
                }
            }

            logger.info("ストリーミングテキスト結合完了: {} 文字", fullText.length());

            // 結合されたテキストからJSON部分を抽出
            String jsonText = extractJsonFromText(fullText.toString());

            // JSONをパース
            TypeReference<Map<String, String>> mapTypeRef = new TypeReference<Map<String, String>>() {};
            Map<String, String> insights = objectMapper.readValue(jsonText, mapTypeRef);

            // レスポンス構築
            AIAnalysisResponseDto response = new AIAnalysisResponseDto();
            response.setSuccess(true);

            AIAnalysisResponseDto.AIInsightData data = new AIAnalysisResponseDto.AIInsightData();
            data.setOverallSummary(insights.get("overall_summary"));
            data.setMoodInsights(insights.get("mood_insights"));
            data.setActivityInsights(insights.get("activity_insights"));
            data.setRecommendations(insights.get("recommendations"));

            response.setData(data);

            logger.info("ストリーミングレスポンス解析完了");
            return response;

        } catch (Exception e) {
            logger.error("ストリーミングレスポンス解析エラー", e);
            AIAnalysisResponseDto response = new AIAnalysisResponseDto();
            response.setSuccess(false);
            response.setError("AI分析結果の処理中にエラーが発生しました。");
            return response;
        }
    }
    
    private String extractJsonFromText(String text) {
        // ```json マーカーがある場合の処理
        if (text.contains("```json")) {
            int jsonStart = text.indexOf("```json") + 7;
            int jsonEnd = text.indexOf("```", jsonStart);
            if (jsonEnd > jsonStart) {
                return text.substring(jsonStart, jsonEnd).trim();
            }
        }
        
        // { } に囲まれた部分を抽出（より安全な方法）
        int braceCount = 0;
        int jsonStart = -1;
        int jsonEnd = -1;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                if (braceCount == 0) {
                    jsonStart = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && jsonStart != -1) {
                    jsonEnd = i + 1;
                    break;
                }
            }
        }
        
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            String jsonText = text.substring(jsonStart, jsonEnd);
            // JSON構文の基本的な検証
            try {
                objectMapper.readTree(jsonText); // 構文チェックのみ
                return jsonText;
            } catch (Exception e) {
                logger.warn("抽出されたJSONの構文が無効です: {}", e.getMessage());
            }
        }
        
        throw new RuntimeException("有効なJSON形式の回答が見つかりませんでした");
    }

    /**
     * Gemini APIキーの基本的な検証
     */
    private boolean isValidGeminiApiKey() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return false;
        }
        
        // ダミーキーまたはプレースホルダーの除外
        if (geminiApiKey.contains("dummy") || 
            geminiApiKey.contains("placeholder") || 
            geminiApiKey.contains("your_") ||
            geminiApiKey.length() < 20) { // Gemini APIキーの最小長チェック
            return false;
        }
        
        return true;
    }

    // 型安全なGeminiストリーミングレスポンス用クラス
    private static class GeminiStreamChunk {
        private List<GeminiCandidate> candidates;

        public List<GeminiCandidate> getCandidates() {
            return candidates;
        }
    }

    private static class GeminiCandidate {
        private GeminiContent content;

        public GeminiContent getContent() {
            return content;
        }
    }

    private static class GeminiContent {
        private List<GeminiPart> parts;

        public List<GeminiPart> getParts() {
            return parts;
        }
    }

    private static class GeminiPart {
        private String text;

        public String getText() {
            return text;
        }
    }
}
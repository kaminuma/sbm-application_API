package importApp.controller;

import importApp.client.AnalyzerApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AnalysisController extends BaseController{

    private final AnalyzerApiClient analyzerApiClient;

    @Autowired
    public AnalysisController(AnalyzerApiClient analyzerApiClient) {
        this.analyzerApiClient = analyzerApiClient;
    }

    /**
     * カテゴリ分析を実行するエンドポイント
     *
     * @param startDate 開始日 (YYYY-MM-DD)
     * @param endDate 終了日 (YYYY-MM-DD)
     * @param userId ユーザーID
     * @return 分析結果を含むレスポンス
     */
    @GetMapping("/analyze")
    public ResponseEntity<?> analyzeCategory(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String userId
    ) {
        try {
            // Analyzer API にリクエストを送信
            var result = analyzerApiClient.analyzeCategory(startDate, endDate, userId);

            // 成功した場合、結果を返す
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

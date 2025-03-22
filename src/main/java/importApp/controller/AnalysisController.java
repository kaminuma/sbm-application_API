package importApp.controller;

import importApp.client.AnalyzerApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AnalysisController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

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
        log.info("GET /analyze requested with startDate={}, endDate={}, userId={}", startDate, endDate, userId);

        // Analyzer API にリクエストを送信
        var result = analyzerApiClient.analyzeCategory(startDate, endDate, userId);

        log.info("Analysis result successfully retrieved for userId={}", userId);
        return ResponseEntity.ok(result);
    }
}

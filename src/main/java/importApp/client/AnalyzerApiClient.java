package importApp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class AnalyzerApiClient {

    private final RestTemplate restTemplate;

    @Value("${analyzer.api.base-url}") // application.ymlからベースURLを取得
    private String baseUrl;

    public AnalyzerApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * PythonのAnalyzer APIにリクエストを送信
     *
     * @param startDate 開始日
     * @param endDate   終了日
     * @param userId    ユーザーID
     * @return 分析結果
     */
    public Map<String, Object> analyzeCategory(String startDate, String endDate, String userId) {

        String apiUrl = baseUrl + "/analysis/category";

        // クエリパラメータ名をPython API用に変換
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("start_date", startDate) // キャメルケースからスネークケースに変換
                .queryParam("end_date", endDate)
                .queryParam("user_id", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // GETリクエストの送信
            ResponseEntity<Map> response = restTemplate.exchange(
                    uriBuilder.toUriString(), // クエリパラメータ付きのURL
                    HttpMethod.GET,          // GETリクエスト
                    entity,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("データ分析サービスとの通信でエラーが発生しました。");
        }
    }
}
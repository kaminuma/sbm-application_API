package importApp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("/api/v1") // 共通プレフィックスを設定
public abstract class BaseController {

    /**
     * クライアントのIPアドレスを取得
     * プロキシ経由の場合も考慮した取得方法
     */
    protected String getClientIpAddress(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For ヘッダーには複数のIPが含まれる場合があるので、最初のIPを取得
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}


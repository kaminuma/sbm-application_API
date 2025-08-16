package importApp.security;

import importApp.entity.UserEntity;
import importApp.service.UserService;
import importApp.service.OAuth2SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OAuth2SessionService sessionService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
        logger.info("OAuth2 authentication success");
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        try {
            String email = (String) attributes.get("email");
            String googleId = (String) attributes.get("sub");
            String name = (String) attributes.get("name");
            String picture = (String) attributes.get("picture");
            Boolean emailVerified = (Boolean) attributes.get("email_verified");

            logger.info("Processing OAuth2 user: email={}, googleId={}", email, googleId);

            UserEntity user = userService.processOAuthPostLogin(email, googleId, name, picture, emailVerified);

            String jwt = jwtService.generateToken(String.valueOf(user.getUser_id()));

            // セキュアなセッション経由でJWT受け渡し
            String sessionId = sessionService.createSession(jwt, String.valueOf(user.getUser_id()));
            String redirectUrl = String.format("%s/login/callback?session=%s", frontendUrl, sessionId);
            
            logger.info("OAuth2 login successful, redirecting to: {}", frontendUrl + "/login/callback");
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            logger.error("OAuth2 login processing error", e);
            String errorUrl = String.format("%s/login?error=%s", 
                                          frontendUrl, 
                                          URLEncoder.encode("oauth2_processing_error", StandardCharsets.UTF_8));
            response.sendRedirect(errorUrl);
        }
    }
}
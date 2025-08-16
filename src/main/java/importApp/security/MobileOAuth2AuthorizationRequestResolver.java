package importApp.security;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class MobileOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    
    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    
    public MobileOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, "/oauth2/authorization");
    }
    
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }
    
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }
    
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        
        if (authorizationRequest == null) {
            return null;
        }
        
        // mobile=trueパラメータをstateに埋め込む
        String mobileParam = request.getParameter("mobile");
        if ("true".equals(mobileParam)) {
            String originalState = authorizationRequest.getState();
            String newState = originalState + "|mobile=true";
            
            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .state(newState)
                    .build();
        }
        
        return authorizationRequest;
    }
}
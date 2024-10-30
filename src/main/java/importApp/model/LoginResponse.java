package importApp.model;

import lombok.Data;

@Data
public class LoginResponse {
    private final String token;
    private final String userId;
}



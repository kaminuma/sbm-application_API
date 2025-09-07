package importApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageResponseDto {
    
    @JsonProperty("profile_image_url")
    private String profileImageUrl;
    
    @JsonProperty("is_google_image")
    private boolean isGoogleImage;
    
    @JsonProperty("can_delete")
    private boolean canDelete;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("success")
    private boolean success;
}
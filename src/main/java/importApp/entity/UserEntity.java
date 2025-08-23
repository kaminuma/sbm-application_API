package importApp.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @Size(min = 8)
    private String password; // null許可（Googleユーザー用）

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;
    
    @Column(name = "ai_daily_limit")
    private Integer aiDailyLimit = 5;
    
    @Column(name = "ai_monthly_limit")
    private Integer aiMonthlyLimit = 50;

    public UserEntity() {
        this.provider = AuthProvider.LOCAL;
        this.isEmailVerified = false;
        this.aiDailyLimit = 5;
        this.aiMonthlyLimit = 50;
    }
    
    // Getters for AI limits
    public Integer getAiDailyLimit() {
        return aiDailyLimit;
    }
    
    public void setAiDailyLimit(Integer aiDailyLimit) {
        this.aiDailyLimit = aiDailyLimit;
    }
    
    public Integer getAiMonthlyLimit() {
        return aiMonthlyLimit;
    }
    
    public void setAiMonthlyLimit(Integer aiMonthlyLimit) {
        this.aiMonthlyLimit = aiMonthlyLimit;
    }
}
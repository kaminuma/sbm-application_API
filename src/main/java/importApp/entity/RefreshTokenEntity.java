package importApp.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refresh_token_id;

    @Column(name = "user_id", nullable = false)
    private Long user_id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expires_at;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }
}
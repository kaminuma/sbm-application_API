package importApp.model;
import lombok.Data;

@Data // Lombokを使用してゲッターやセッターを自動生成
public class UserResponse {
    private Long user_id;         // ユーザーID
    private String username;      // ユーザー名
    private String email;         // メールアドレス
    private String profileImageUrl; // プロフィール画像URL

    // 既存のコンストラクタ（後方互換性のため）
    public UserResponse(Long id, String username) {
        this.user_id = id;
        this.username = username;
    }
    
    // 設定ページ用のコンストラクタ
    public UserResponse(Long user_id, String username, String email, String profileImageUrl) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }
}

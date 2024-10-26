package importApp.model;
import lombok.Data;

@Data // Lombokを使用してゲッターやセッターを自動生成
public class UserResponse {
    private Long user_id;         // ユーザーID
    private String username; // ユーザー名

    // コンストラクタ
    public UserResponse(Long id, String username) {
        this.user_id = id;
        this.username = username;
    }
}

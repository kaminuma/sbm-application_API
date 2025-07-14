package importApp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lombokでの省略記法（@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor）
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PostRequest {
    private Integer userId;
    private String title;
    private String contents;
    private String start; // "HH:mm" 形式
    private String end;   // "HH:mm" 形式
    private String date;      // "yyyy-MM-dd" 形式
    private String category;
    private String categorySub;
}


package importApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoodRequest {
    private Long userId;
    private String date; // "yyyy-MM-dd" 形式
    private Integer mood; // 1-5の整数
    private String note;
} 
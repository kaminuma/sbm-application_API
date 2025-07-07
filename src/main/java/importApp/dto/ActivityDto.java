package importApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class ActivityDto {
    private Long activityId;
    private Integer userId;
    private String title;
    private String contents;
    private String start;
    private String end;
    private Date date;
    private String category;
    private String categorySub;

    public ActivityDto(Long activityId, Integer userId, String title, String contents, String start, String end, String category, String categorySub) {
        this.activityId = activityId;
        this.userId = userId;
        this.title = title;
        this.contents = contents;
        this.start = start;
        this.end = end;
        this.category = category;
        this.categorySub = categorySub;
    }
}
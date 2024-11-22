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

    public ActivityDto(Long activityId, Integer userId, String title, String contents, String start, String end) {
        this.activityId = activityId;
        this.userId = userId;
        this.title = title;
        this.contents = contents;
        this.start = start;
        this.end = end;
        this.date = date;
    }
}
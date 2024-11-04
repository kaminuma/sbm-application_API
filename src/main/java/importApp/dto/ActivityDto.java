package importApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class ActivityDto {
    private Integer userId;
    private String title;
    private String contents;
    private String start;
    private String end;
    private Date date;

    public ActivityDto(Integer userId, String title, String contents, String start, String end) {
        this.userId = userId;
        this.title = title;
        this.contents = contents;
        this.start = start;
        this.end = end;
        this.date = date;
    }
}
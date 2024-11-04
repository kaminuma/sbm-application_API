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
    private String startTime;
    private String endTIme;
    private Date date;

    public ActivityDto(Integer userId, String title, String contents, String start, String end) {
        this.userId = userId;
        this.title = title;
        this.contents = contents;
        this.startTime = start;
        this.endTIme = end;
        this.date = date;
    }
}
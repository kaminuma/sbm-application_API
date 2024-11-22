package importApp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    private long userId;
    private Date date;
    private LocalTime start;
    private LocalTime end;
    private String title;
    private String contents;
    private Long createdBy;
    private Date createdAt;
    private Long updatedBy;
    private Date updatedAt;

    public ActivityEntity(
            Long activityId,
            long userId,
            Date date,
            LocalTime start,
            LocalTime end,
            String title,
            String contents,
            Long createdBy,
            Long updatedBy) {
        this.activityId = activityId;
        this.userId = userId;
        this.date = date;
        this.start = start;
        this.end = end;
        this.title = title;
        this.contents = contents;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
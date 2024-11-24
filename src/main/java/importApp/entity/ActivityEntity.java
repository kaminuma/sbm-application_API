package importApp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Date;

@Data
@NoArgsConstructor
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
            Date createdAt,
            Long updatedBy,
            Date updatedAt) {
        this.activityId = activityId;
        this.userId = userId;
        this.date = date;
        this.start = start;
        this.end = end;
        this.title = title;
        this.contents = contents;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}

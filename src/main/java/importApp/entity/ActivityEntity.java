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
    private LocalTime startTime;
    private LocalTime endTime;
    private String name;
    private String contents;
    private Long createdBy;
    private Date createdAt;
    private Long updatedBy;
    private Date updatedAt;

    public ActivityEntity(long userId,
                          Date date,
                          LocalTime startTime,
                          LocalTime endTime,
                          String name,
                          String contents,
                          Long createdBy,
                          Long updatedBy) {
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.contents = contents;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}

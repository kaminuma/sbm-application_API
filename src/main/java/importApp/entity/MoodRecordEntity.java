package importApp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Date date;
    private Integer mood;
    private String note;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isDeleted;

    public MoodRecordEntity(Long userId, Date date, Integer mood, String note) {
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.note = note;
    }
} 
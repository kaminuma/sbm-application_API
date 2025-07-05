package importApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodRecordDto {
    private Long id;
    private Long userId;
    private String date;
    private Integer mood;
    private String note;
    private Date createdAt;
    private Date updatedAt;

    public MoodRecordDto(Long id, Long userId, String date, Integer mood, String note) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.note = note;
    }
} 
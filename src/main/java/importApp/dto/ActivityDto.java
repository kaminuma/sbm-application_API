package importApp.dto;

import lombok.Data;

@Data
public class ActivityDto {
    private String title;
    private String description;
    private String start;
    private String end;

    public ActivityDto(String title, String description, String start, String end) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
    }
}
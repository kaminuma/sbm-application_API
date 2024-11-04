package importApp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostActivityEntity {
    private Long activityId; // 自動生成されるID
    private long userId; // ユーザーID
    private Date date; // 日付
    private String start; // 開始時間（String型）
    private String end;   // 終了時間（String型）
    private String title; // アクティビティ名
    private String contents; // 内容
    private Long createdBy; // 作成者のID
    private Date createdAt; // 作成日時
    private Long updatedBy; // 更新者のID
    private Date updatedAt; // 更新日時

    public PostActivityEntity(long userId, Date date, String start, String end, String title, String contents, Long createdBy, Long updatedBy) {
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

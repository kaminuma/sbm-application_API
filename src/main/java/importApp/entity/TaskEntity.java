package importApp.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;
    private long projectId;
    private long userId;
    private String taskName;
    private String description;
    private Date dueDate;
    private Date createDate;
    private Date updateDate;
    private String priority;
    private String status;

    public TaskEntity(String taskName,
                      String description,
                      Date dueDate,
                      Date createDate,
                      Date updateDate,
                      String priority,
                      String status,
                      long projectId,
                      long userId)
    {
        this.projectId = projectId;
        this.userId = userId;
        this.taskName = taskName;
        this.description = description;
        this.dueDate = dueDate;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.priority = priority;
        this.status = status;
    }
}

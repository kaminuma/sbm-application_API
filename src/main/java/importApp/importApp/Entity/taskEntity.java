package importApp.importApp.Entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tasks")
@Data
public class taskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskName;
    private String description;
    private Date dueDate;
    private String priority;
    private String status;
    private long projectId;
    private long userId;

    public taskEntity(String taskName, String description, Date dueDate, String priority, String status, long projectId, long userId) {
        this.taskName = taskName;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.projectId = projectId;
        this.userId = userId;
    }
}

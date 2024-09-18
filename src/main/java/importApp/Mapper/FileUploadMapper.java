package importApp.Mapper;

import importApp.Entity.taskEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileUploadMapper {
    @Insert
            ("INSERT INTO tasks (task_name, description, due_date, priority, status, project_id, user_id) " +
            "VALUES (#{taskName}, #{description}, #{dueDate}, #{priority}, #{status}, #{projectId}, #{userId})")
    void insertTask(taskEntity task);
}

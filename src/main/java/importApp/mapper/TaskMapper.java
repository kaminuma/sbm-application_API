package importApp.mapper;

import importApp.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface TaskMapper {
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    void save(TaskEntity taskEntity);

    List<TaskEntity> findTaskByUserId(long userId);
}
package importApp.mapper;

import importApp.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface FileUploadMapper {
    void insertTask(TaskEntity task);
}

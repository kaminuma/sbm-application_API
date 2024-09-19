package importApp.mapper;

import importApp.entity.taskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface FileUploadMapper {
    void insertTask(taskEntity task);
}

package importApp.mapper;

import importApp.entity.ActivityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface FileUploadMapper {
    void insertActivity(ActivityEntity task);
}

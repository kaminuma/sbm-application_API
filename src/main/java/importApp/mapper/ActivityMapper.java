package importApp.mapper;

import importApp.entity.ActivityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface ActivityMapper {
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    void save(ActivityEntity taskEntity);

    List<ActivityEntity> findActivitiesByUserId(long userId);
}
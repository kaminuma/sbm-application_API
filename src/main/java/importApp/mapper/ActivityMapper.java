package importApp.mapper;

import importApp.entity.ActivityGetEntity;
import importApp.entity.PostActivityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface ActivityMapper {
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    void save(PostActivityEntity taskEntity);

    List<ActivityGetEntity> findActivitiesByUserId(long userId);

    List<ActivityGetEntity> findActivitiesByUserIdAndDateRange(@Param("userId") long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    int markActivityAsDeleted(@Param("id") Long id);

    int updateActivity(PostActivityEntity activity);

}
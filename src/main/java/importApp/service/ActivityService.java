package importApp.service;

import importApp.entity.ActivityEntity;
import importApp.mapper.ActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    public String createActivity(ActivityEntity activity) {
        activityMapper.save(activity);
        return "success";
    }

    public List<ActivityEntity> findActivitiesByUserId(long userId) {
        return activityMapper.findActivitiesByUserId(userId);
    }
}

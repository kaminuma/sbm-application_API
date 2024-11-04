package importApp.service;

import importApp.dto.ActivityDto;
import importApp.entity.ActivityEntity;
import importApp.entity.PostActivityEntity;
import importApp.mapper.ActivityMapper;
import importApp.model.PostRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    public ModelMapper modelMapper;
    private ActivityDto activityDto;

    public String createActivity(PostRequest request) {
        PostActivityEntity entity = modelMapper.map(request, PostActivityEntity.class);
        entity.setUpdatedAt(null); // 必要に応じて設定
        entity.setUpdatedBy(null);   // 必要に応じて設定
        entity.setCreatedAt(null); // 必要に応じて設定
        entity.setCreatedBy(null);

        activityMapper.save(entity);
        return "success";
    }

    public List<ActivityEntity> findActivitiesByUserId(long userId) {
        return activityMapper.findActivitiesByUserId(userId);
    }
}

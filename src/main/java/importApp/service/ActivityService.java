package importApp.service;

import importApp.dto.ActivityDto;
import importApp.entity.ActivityGetEntity;
import importApp.entity.PostActivityEntity;
import importApp.mapper.ActivityMapper;
import importApp.model.PostRequest;
import importApp.model.PutRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public boolean updateActivity(PutRequest request) {
        PostActivityEntity entity = modelMapper.map(request, PostActivityEntity.class);
        entity.setUpdatedAt(null); // 必要に応じて設定
        entity.setUpdatedBy(null);   // 必要に応じて設定
        entity.setCreatedAt(null); // 必要に応じて設定
        entity.setCreatedBy(null);
        int updatedRows = activityMapper.updateActivity(entity);
        return updatedRows > 0;
    }

    public List<ActivityGetEntity> findActivitiesByUserId(long userId) {
        return activityMapper.findActivitiesByUserId(userId);
    }
    public boolean deleteActivity(Long id) {
        int result = activityMapper.markActivityAsDeleted(id);
        return result > 0;
    }

    // アクティビティの所有者確認
    public boolean isOwner(Long activityId, String userId) {
        // ユーザーのすべてのアクティビティを取得
        List<ActivityGetEntity> userActivities = activityMapper.findActivitiesByUserId(Long.parseLong(userId));

        // ユーザーのアクティビティリストに該当するactivityIdが存在するかを確認
        return userActivities.stream()
                .anyMatch(activity -> activity.getActivityId().equals(activityId));
    }
}

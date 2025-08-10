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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    public ModelMapper modelMapper;
    private ActivityDto activityDto;

    public String createActivity(PostRequest request) {
        // バリデーション
        if (request.getCategory() == null || request.getCategory().isEmpty()) {
            throw new IllegalArgumentException("categoryは必須です");
        }
        if ("その他".equals(request.getCategory()) && (request.getCategorySub() == null || request.getCategorySub().isEmpty())) {
            throw new IllegalArgumentException("category_subは必須です");
        }
        if (request.getCategorySub() == null) {
            request.setCategorySub("");
        }
        PostActivityEntity entity = modelMapper.map(request, PostActivityEntity.class);
        // 作成者と作成日時を設定
        entity.setCreatedAt(convertToDate(LocalDateTime.now()));
        entity.setCreatedBy(Long.valueOf(request.getUserId()));
        // 更新日時と更新者は作成時は未設定
        entity.setUpdatedAt(null);
        entity.setUpdatedBy(null);

        activityMapper.save(entity);
        return "success";
    }

    public boolean updateActivity(PutRequest request) {
        // バリデーション
        if (request.getCategory() == null || request.getCategory().isEmpty()) {
            throw new IllegalArgumentException("categoryは必須です");
        }
        if ("その他".equals(request.getCategory()) && (request.getCategorySub() == null || request.getCategorySub().isEmpty())) {
            throw new IllegalArgumentException("category_subは必須です");
        }
        if (request.getCategorySub() == null) {
            request.setCategorySub("");
        }
        PostActivityEntity entity = modelMapper.map(request, PostActivityEntity.class);
        //　更新者と更新日時を設定
        entity.setUpdatedAt(convertToDate(LocalDateTime.now()));
        entity.setUpdatedBy(Long.valueOf(request.getUserId()));
        // 作成日時と作成者は変更しない
        entity.setCreatedAt(null);
        entity.setCreatedBy(null);
        int updatedRows = activityMapper.updateActivity(entity);
        return updatedRows > 0;
    }

    public List<ActivityGetEntity> findActivitiesByUserId(long userId) {
        return activityMapper.findActivitiesByUserId(userId);
    }

    public List<ActivityGetEntity> getActivitiesByUserAndDateRange(String userId, String startDate, String endDate) {
        return activityMapper.findActivitiesByUserIdAndDateRange(Long.parseLong(userId), startDate, endDate);
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

    // Date型にLocalDateTimeを変換するメソッドの作成
    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

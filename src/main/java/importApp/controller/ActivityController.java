package importApp.controller;

import importApp.dto.ActivityDto;
import importApp.entity.ActivityGetEntity;
import importApp.model.PostRequest;
import importApp.model.PutRequest;
import importApp.security.JwtService;
import importApp.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private JwtService jwtService;

    @PostMapping
    public ResponseEntity<String> postActivity(@RequestBody PostRequest postRequest,
                                               @RequestHeader("Authorization") String token) throws AccessDeniedException {

        String userIdFromToken = jwtService.extractUserId(token);

        // 修正: ユーザーIDの比較に失敗した場合に403を返す
        if (!postRequest.getUserId().toString().equals(userIdFromToken)) {
            throw new AccessDeniedException("You are not authorized to create this activity.");
        }

        String createdActivity = activityService.createActivity(postRequest);
        return new ResponseEntity<>(createdActivity, HttpStatus.CREATED);
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<Void> updateActivity(
            @PathVariable Long activityId,
            @RequestBody PutRequest putRequest,
            @RequestHeader("Authorization") String token) throws AccessDeniedException {

        String userIdFromToken = jwtService.extractUserId(token);
        // パスのactivityIdをエンティティにセット
        putRequest.setActivityId(activityId);

        // 修正: アクティビティ所有者の確認に失敗した場合に403を返す
        boolean isOwner = activityService.isOwner(activityId, userIdFromToken);
        if (!isOwner) {
            throw new AccessDeniedException("You are not authorized to update this activity.");
        }

        boolean isUpdated = activityService.updateActivity(putRequest);
        if (isUpdated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public List<ActivityDto> findTasksByUserId(@RequestParam("userId") Long userId,
                                               @RequestHeader("Authorization") String token) throws AccessDeniedException {

        String userIdFromToken = jwtService.extractUserId(token);

        // 修正: ユーザーIDの比較に失敗した場合に403を返す
        if (!userId.toString().equals(userIdFromToken)) {
            throw new AccessDeniedException("You are not authorized to access this resource.");
        }

        List<ActivityGetEntity> activities = activityService.findActivitiesByUserId(userId);

        return activities.stream()
                .map(activity -> new ActivityDto(
                        activity.getActivityId(),
                        (int) activity.getUserId(),
                        activity.getTitle(),
                        activity.getContents(),
                        formatDateTime(activity.getDate(), activity.getStart()), // 'YYYY-MM-DD HH:mm'形式
                        formatDateTime(activity.getDate(), activity.getEnd())  // 'YYYY-MM-DD HH:mm'形式
                ))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id,
                                               @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);

        // 修正: アクティビティ所有者の確認に失敗した場合に403を返す
        boolean isOwner = activityService.isOwner(id, userIdFromToken);
        if (!isOwner) {
            throw new AccessDeniedException("You are not authorized to delete this activity.");
        }

        boolean isDeleted = activityService.deleteActivity(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ヘルパーメソッド: 日付と時間を結合してフォーマット
    private String formatDateTime(Date date, LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.atTime(time).format(formatter);
    }
}

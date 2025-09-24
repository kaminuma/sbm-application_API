package importApp.controller;

import importApp.dto.ActivityDto;
import importApp.entity.ActivityGetEntity;
import importApp.model.PostRequest;
import importApp.model.PutRequest;
import importApp.security.JwtService;
import importApp.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ActivityController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityService activityService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/activities")
    public ResponseEntity<?> postActivity(@RequestBody PostRequest postRequest,
                                               @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("POST /activities requested by userId={}", userIdFromToken);

        if (!postRequest.getUserId().toString().equals(userIdFromToken)) {
            log.warn("Access denied: token userId={} does not match request userId={}", userIdFromToken, postRequest.getUserId());
            throw new AccessDeniedException("You are not authorized to create this activity.");
        }
        try {
            String createdActivity = activityService.createActivity(postRequest);
            log.info("Activity created successfully: {}", createdActivity);
            return ResponseEntity.status(HttpStatus.CREATED).body("success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("入力データが正しくありません。入力内容を確認してください。");
        }
    }

    @PutMapping("/activities/{activityId}")
    public ResponseEntity<?> updateActivity(@PathVariable Long activityId,
                                               @RequestBody PutRequest putRequest,
                                               @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("PUT /activities/{} requested by userId={}", activityId, userIdFromToken);

        putRequest.setActivityId(activityId);

        boolean isOwner = activityService.isOwner(activityId, userIdFromToken);
        if (!isOwner) {
            log.warn("Access denied: userId={} is not owner of activityId={}", userIdFromToken, activityId);
            throw new AccessDeniedException("You are not authorized to update this activity.");
        }
        try {
            boolean isUpdated = activityService.updateActivity(putRequest);
            log.info("Activity update status for id {}: {}", activityId, isUpdated);
            return isUpdated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("入力データが正しくありません。入力内容を確認してください。");
        }
    }

    @GetMapping("/activities")
    public List<ActivityDto> findTasksByUserId(@RequestParam("userId") Long userId,
                                               @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("GET /activities requested for userId={} by token userId={}", userId, userIdFromToken);

        if (!userId.toString().equals(userIdFromToken)) {
            log.warn("Access denied: token userId={} does not match param userId={}", userIdFromToken, userId);
            throw new AccessDeniedException("You are not authorized to access this resource.");
        }

        List<ActivityGetEntity> activities = activityService.findActivitiesByUserId(userId);
        log.info("Found {} activities for userId={}", activities.size(), userId);

        return activities.stream()
                .map(activity -> new ActivityDto(
                        activity.getActivityId(),
                        (int) activity.getUserId(),
                        activity.getTitle(),
                        activity.getContents(),
                        formatDateTime(activity.getDate(), activity.getStart()), // 'YYYY-MM-DD HH:mm'形式
                        formatDateTime(activity.getDate(), activity.getEnd()),  // 'YYYY-MM-DD HH:mm'形式
                        activity.getCategory(),
                        activity.getCategorySub()
                ))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id,
                                               @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("DELETE /activities/{} requested by userId={}", id, userIdFromToken);

        boolean isOwner = activityService.isOwner(id, userIdFromToken);
        if (!isOwner) {
            log.warn("Access denied: userId={} is not owner of activityId={}", userIdFromToken, id);
            throw new AccessDeniedException("You are not authorized to delete this activity.");
        }

        boolean isDeleted = activityService.deleteActivity(id);
        log.info("Activity delete status for id {}: {}", id, isDeleted);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private String formatDateTime(Date date, LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.atTime(time).format(formatter);
    }
}

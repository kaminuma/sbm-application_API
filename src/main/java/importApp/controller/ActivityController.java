package importApp.controller;

import importApp.dto.ActivityDto;
import importApp.entity.ActivityEntity;
import importApp.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<String> createTask(@RequestBody ActivityEntity task) {
        String createdTask = activityService.createActivity(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ActivityDto> findTasksByUserId(@RequestParam("userId") Long userId) {
        List<ActivityEntity> activities = activityService.findActivitiesByUserId(userId);

        List<ActivityDto> activityDtos = activities.stream()
                .map(activity -> new ActivityDto(
                        activity.getName(),
                        activity.getDescription(),
                        formatDateTime(activity.getDate(), activity.getStartTime()), // 'YYYY-MM-DD HH:mm'形式
                        formatDateTime(activity.getDate(), activity.getEndTime())  // 'YYYY-MM-DD HH:mm'形式
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(activityDtos).getBody();
    }

    // ヘルパーメソッド: 日付と時間を結合してフォーマット
    private String formatDateTime(Date date, LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.atTime(time).format(formatter);
    }
}

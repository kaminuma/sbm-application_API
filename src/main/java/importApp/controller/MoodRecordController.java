package importApp.controller;

import importApp.dto.MoodRecordDto;
import importApp.model.MoodRequest;
import importApp.security.JwtService;
import importApp.service.MoodRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MoodRecordController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MoodRecordController.class);

    @Autowired
    private MoodRecordService moodRecordService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/mood")
    public ResponseEntity<Map<String, Object>> getMoodRecords(@RequestParam("userId") Long userId,
                                                              @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("GET /mood requested for userId={} by token userId={}", userId, userIdFromToken);

        if (!userId.toString().equals(userIdFromToken)) {
            log.warn("Access denied: token userId={} does not match param userId={}", userIdFromToken, userId);
            throw new AccessDeniedException("You are not authorized to access this resource.");
        }

        List<MoodRecordDto> moodRecords = moodRecordService.findMoodRecordsByUserId(userId);
        log.info("Found {} mood records for userId={}", moodRecords.size(), userId);

        Map<String, Object> response = new HashMap<>();
        response.put("moodRecords", moodRecords);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mood")
    public ResponseEntity<Map<String, Object>> createMoodRecord(@RequestBody MoodRequest request,
                                                                @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("POST /mood requested by userId={}", userIdFromToken);

        if (!request.getUserId().toString().equals(userIdFromToken)) {
            log.warn("Access denied: token userId={} does not match request userId={}", userIdFromToken, request.getUserId());
            throw new AccessDeniedException("You are not authorized to create this mood record.");
        }

        try {
            String result = moodRecordService.createMoodRecord(request);
            log.info("Mood record creation result: {}", result);

            Map<String, Object> response = new HashMap<>();
            response.put("success", "success".equals(result));
            response.put("message", "success".equals(result) ? "気分記録を作成しました" : "気分記録の作成に失敗しました");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalStateException e) {
            log.error("Business logic error: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PutMapping("/mood/{date}")
    public ResponseEntity<Map<String, Object>> updateMoodRecord(@PathVariable String date,
                                                                @RequestBody MoodRequest request,
                                                                @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("PUT /mood/{} requested by userId={}", date, userIdFromToken);

        if (!request.getUserId().toString().equals(userIdFromToken)) {
            log.warn("Access denied: token userId={} does not match request userId={}", userIdFromToken, request.getUserId());
            throw new AccessDeniedException("You are not authorized to update this mood record.");
        }

        try {
            String result = moodRecordService.updateMoodRecord(date, request);
            log.info("Mood record update result: {}", result);

            Map<String, Object> response = new HashMap<>();
            response.put("success", "success".equals(result));
            response.put("message", "success".equals(result) ? "気分記録を更新しました" : "気分記録の更新に失敗しました");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalStateException e) {
            log.error("Business logic error: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/mood/{date}")
    public ResponseEntity<Map<String, Object>> deleteMoodRecord(@PathVariable String date,
                                                                @RequestParam("userId") Long userId,
                                                                @RequestHeader("Authorization") String token) throws AccessDeniedException {
        String userIdFromToken = jwtService.extractUserId(token);
        log.info("DELETE /mood/{} requested by userId={}", date, userIdFromToken);

        if (!userId.toString().equals(userIdFromToken)) {
            log.warn("Access denied: token userId={} does not match param userId={}", userIdFromToken, userId);
            throw new AccessDeniedException("You are not authorized to delete this mood record.");
        }

        String result = moodRecordService.deleteMoodRecord(userId, date);
        log.info("Mood record deletion result: {}", result);

        Map<String, Object> response = new HashMap<>();
        response.put("success", "success".equals(result));
        response.put("message", "success".equals(result) ? "気分記録を削除しました" : "気分記録の削除に失敗しました");

        return ResponseEntity.ok(response);
    }
} 
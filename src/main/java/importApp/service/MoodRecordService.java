package importApp.service;

import importApp.dto.MoodRecordDto;
import importApp.entity.MoodRecordEntity;
import importApp.mapper.MoodRecordMapper;
import importApp.model.MoodRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoodRecordService {

    @Autowired
    private MoodRecordMapper moodRecordMapper;

    public String createMoodRecord(MoodRequest request) {
        // バリデーション
        if (request.getMood() == null || request.getMood() < 1 || request.getMood() > 5) {
            throw new IllegalArgumentException("Mood must be between 1 and 5");
        }

        // 日付の変換
        Date date = parseDate(request.getDate());
        
        // 既存の記録があるかチェック
        MoodRecordEntity existingRecord = moodRecordMapper.findMoodRecordByUserIdAndDate(request.getUserId(), date);
        if (existingRecord != null) {
            throw new IllegalStateException("Mood record already exists for this date");
        }

        MoodRecordEntity entity = new MoodRecordEntity(
                request.getUserId(),
                date,
                request.getMood(),
                request.getNote()
        );
        
        entity.setCreatedAt(new Date());
        entity.setUpdatedAt(new Date());
        
        moodRecordMapper.save(entity);
        return "success";
    }

    public String updateMoodRecord(String date, MoodRequest request) {
        // バリデーション
        if (request.getMood() == null || request.getMood() < 1 || request.getMood() > 5) {
            throw new IllegalArgumentException("Mood must be between 1 and 5");
        }

        Date recordDate = parseDate(date);
        
        // 既存の記録を取得
        MoodRecordEntity existingRecord = moodRecordMapper.findMoodRecordByUserIdAndDate(request.getUserId(), recordDate);
        if (existingRecord == null) {
            throw new IllegalStateException("Mood record not found for this date");
        }

        // 更新
        existingRecord.setMood(request.getMood());
        existingRecord.setNote(request.getNote());
        existingRecord.setUpdatedAt(new Date());
        
        int updatedRows = moodRecordMapper.updateMoodRecord(existingRecord);
        return updatedRows > 0 ? "success" : "failed";
    }

    public List<MoodRecordDto> findMoodRecordsByUserId(Long userId) {
        List<MoodRecordEntity> entities = moodRecordMapper.findMoodRecordsByUserId(userId);
        
        return entities.stream()
                .map(entity -> new MoodRecordDto(
                        entity.getId(),
                        entity.getUserId(),
                        formatDate(entity.getDate()),
                        entity.getMood(),
                        entity.getNote()
                ))
                .collect(Collectors.toList());
    }

    public List<MoodRecordEntity> getMoodRecordsByUserAndDateRange(String userId, String startDate, String endDate) {
        return moodRecordMapper.findMoodRecordsByUserIdAndDateRange(Long.parseLong(userId), startDate, endDate);
    }

    public String deleteMoodRecord(Long userId, String date) {
        Date recordDate = parseDate(date);
        int deletedRows = moodRecordMapper.deleteMoodRecordByUserIdAndDate(userId, recordDate);
        return deletedRows > 0 ? "success" : "failed";
    }

    public boolean isOwner(Long moodRecordId, String userId) {
        List<MoodRecordEntity> userMoodRecords = moodRecordMapper.findMoodRecordsByUserId(Long.parseLong(userId));
        return userMoodRecords.stream()
                .anyMatch(record -> record.getId().equals(moodRecordId));
    }

    private Date parseDate(String dateStr) {
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
        }
    }

    private String formatDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
} 
package importApp.mapper;

import importApp.entity.MoodRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Mapper
@Component
public interface MoodRecordMapper {
    void save(MoodRecordEntity moodRecord);
    
    List<MoodRecordEntity> findMoodRecordsByUserId(Long userId);
    
    MoodRecordEntity findMoodRecordByUserIdAndDate(@Param("userId") Long userId, @Param("date") Date date);
    
    int updateMoodRecord(MoodRecordEntity moodRecord);
    
    int markMoodRecordAsDeleted(@Param("id") Long id);
    
    int deleteMoodRecordByUserIdAndDate(@Param("userId") Long userId, @Param("date") Date date);
} 
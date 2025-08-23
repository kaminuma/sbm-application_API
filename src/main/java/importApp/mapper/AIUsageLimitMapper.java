package importApp.mapper;

import importApp.entity.AIUsageLimit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface AIUsageLimitMapper {
    
    /**
     * ユーザーID、プロバイダー、日付で利用状況を取得
     */
    AIUsageLimit findByUserAndDate(
        @Param("userId") Integer userId, 
        @Param("provider") String provider,
        @Param("date") LocalDate date
    );
    
    /**
     * 利用回数をインクリメント（存在しない場合は新規作成）
     */
    void incrementCount(
        @Param("userId") Integer userId,
        @Param("provider") String provider,
        @Param("date") LocalDate date
    );
}
package importApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class AIAnalysisRequestDto {
    
    @NotBlank(message = "開始日は必須です")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日付形式はYYYY-MM-DDである必要があります")
    @JsonProperty("start_date")
    private String startDate;
    
    @NotBlank(message = "終了日は必須です")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日付形式はYYYY-MM-DDである必要があります")
    @JsonProperty("end_date") 
    private String endDate;
    
    @NotBlank(message = "分析焦点は必須です")
    @Pattern(regexp = "MOOD_FOCUSED|ACTIVITY_FOCUSED|BALANCED|WELLNESS_FOCUSED")
    @JsonProperty("analysis_focus")
    private String analysisFocus;
    
    @NotBlank(message = "詳細レベルは必須です")
    @Pattern(regexp = "CONCISE|STANDARD|DETAILED")
    @JsonProperty("detail_level")
    private String detailLevel;
    
    @NotBlank(message = "応答スタイルは必須です")
    @Pattern(regexp = "FRIENDLY|PROFESSIONAL|ENCOURAGING|CASUAL")
    @JsonProperty("response_style")
    private String responseStyle;
    
    // getters and setters
    public String getStartDate() { 
        return startDate; 
    }
    
    public void setStartDate(String startDate) { 
        this.startDate = startDate; 
    }
    
    public String getEndDate() { 
        return endDate; 
    }
    
    public void setEndDate(String endDate) { 
        this.endDate = endDate; 
    }
    
    public String getAnalysisFocus() { 
        return analysisFocus; 
    }
    
    public void setAnalysisFocus(String analysisFocus) { 
        this.analysisFocus = analysisFocus; 
    }
    
    public String getDetailLevel() { 
        return detailLevel; 
    }
    
    public void setDetailLevel(String detailLevel) { 
        this.detailLevel = detailLevel; 
    }
    
    public String getResponseStyle() { 
        return responseStyle; 
    }
    
    public void setResponseStyle(String responseStyle) { 
        this.responseStyle = responseStyle; 
    }
}
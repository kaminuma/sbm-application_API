package importApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AIAnalysisResponseDto {
    
    private boolean success;
    private String error;
    
    @JsonProperty("data")
    private AIInsightData data;
    
    @JsonProperty("usage_info")
    private Map<String, Object> usageInfo;
    
    public static class AIInsightData {
        
        @JsonProperty("overall_summary")
        private String overallSummary;
        
        @JsonProperty("mood_insights")
        private String moodInsights;
        
        @JsonProperty("activity_insights") 
        private String activityInsights;
        
        @JsonProperty("recommendations")
        private String recommendations;
        
        // getters and setters
        public String getOverallSummary() { 
            return overallSummary; 
        }
        
        public void setOverallSummary(String overallSummary) { 
            this.overallSummary = overallSummary; 
        }
        
        public String getMoodInsights() { 
            return moodInsights; 
        }
        
        public void setMoodInsights(String moodInsights) { 
            this.moodInsights = moodInsights; 
        }
        
        public String getActivityInsights() { 
            return activityInsights; 
        }
        
        public void setActivityInsights(String activityInsights) { 
            this.activityInsights = activityInsights; 
        }
        
        public String getRecommendations() { 
            return recommendations; 
        }
        
        public void setRecommendations(String recommendations) { 
            this.recommendations = recommendations; 
        }
    }
    
    // getters and setters
    public boolean isSuccess() { 
        return success; 
    }
    
    public void setSuccess(boolean success) { 
        this.success = success; 
    }
    
    public String getError() { 
        return error; 
    }
    
    public void setError(String error) { 
        this.error = error; 
    }
    
    public AIInsightData getData() { 
        return data; 
    }
    
    public void setData(AIInsightData data) { 
        this.data = data; 
    }
    
    public Map<String, Object> getUsageInfo() {
        return usageInfo;
    }
    
    public void setUsageInfo(Map<String, Object> usageInfo) {
        this.usageInfo = usageInfo;
    }
}
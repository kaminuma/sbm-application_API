package importApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${storage.upload-dir:uploads/profiles}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // アップロードされた画像ファイルへの静的アクセスを設定
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        
        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/uploads/profiles/**")
                .allowedOrigins("*")
                .allowedMethods("GET");
    }
}
package importApp.controller;

import importApp.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController extends BaseController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        // トークンから userId を取得
        String userId = authentication.getName(); // Spring Security を利用

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("ファイルが空です。");
        }

        try {
            // ファイル処理をサービスに委譲
            String result = fileService.parseExcelFile(file, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 例外処理
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ファイルの処理中にエラーが発生しました: " + e.getMessage());
        }
    }
}

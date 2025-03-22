package importApp.controller;

import importApp.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String userId = authentication.getName(); // Spring Security を利用
        logger.info("ファイルアップロードリクエスト受信: userId={}, ファイル名={}", userId, file != null ? file.getOriginalFilename() : "null");

        if (file == null || file.isEmpty()) {
            logger.warn("アップロードされたファイルが空です: userId={}", userId);
            return ResponseEntity.badRequest().body("ファイルが空です。");
        }

        try {
            String result = fileService.parseExcelFile(file, userId);
            logger.info("ファイル処理成功: userId={}, 結果={}", userId, result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ファイル処理中にエラーが発生しました: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ファイルの処理中にエラーが発生しました: " + e.getMessage());
        }
    }
}

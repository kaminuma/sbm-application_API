package importApp.importApp.Controller;


import importApp.importApp.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        // ファイルのフォーマットを確認
        if (fileService.isValidFormat(file)) {
            // ファイルデータの処理
            fileService.processFile(file);
            return "redirect:/success"; // 成功時のページにリダイレクト
        } else {
            return "redirect:/error"; // エラーページにリダイレクト
        }
    }
}

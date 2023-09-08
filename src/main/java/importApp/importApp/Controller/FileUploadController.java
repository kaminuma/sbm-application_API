package importApp.importApp.Controller;


import importApp.importApp.Entity.taskEntity;
import importApp.importApp.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class FileUploadController  {

    private final FileService excelService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FileUploadController(FileService excelService, JdbcTemplate jdbcTemplate) {
        this.excelService = excelService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
            List<taskEntity> tasks = excelService.parseExcelFile(file);

            for (taskEntity task : tasks) {
                // データベースにインサートするSQLクエリ
                String sql = "INSERT INTO tasks (task_name, description, due_date, priority, status, project_id, user_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                // パラメータを設定してインサート実行
                jdbcTemplate.update(
                        sql,
                        task.getTaskName(),
                        task.getDescription(),
                        task.getDueDate(),
                        task.getPriority(),
                        task.getStatus(),
                        task.getProjectId(),
                        task.getUserId()
                );
            }

            return "Excelファイルのデータがデータベースにインサートされました。";
    }
}

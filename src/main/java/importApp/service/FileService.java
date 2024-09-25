package importApp.service;

import importApp.entity.TaskEntity;
import importApp.mapper.FileUploadMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

@Service
public class FileService {

    @Autowired
    FileUploadMapper fileUploadMapper;

    // ファイルのフォーマットを確認
    public boolean isValidFormat(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // フォーマットが正しいかの確認ロジックを追加
            // 例: シート名やセルの内容の確認
            // ...

            return true; // フォーマットが正しい場合
        } catch (Exception e) {
            return false; // フォーマットが正しくない場合
        }
    }

    // ファイルデータの処理
    public String parseExcelFile(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // 1列目のシートを選択

            Iterator<Row> iterator = sheet.iterator();

            // ヘッダ行をスキップする
            if (iterator.hasNext()) {
                iterator.next();
            }

            while (iterator.hasNext()) {
                Row row = iterator.next();

                // A列のセルを取得
                Cell taskNameCell = row.getCell(0); // A列のセルを取得

                if (taskNameCell == null || taskNameCell.getCellType() != CellType.STRING || taskNameCell.getStringCellValue().isEmpty()) {
                    // A列が空白の場合、ループを終了
                    break;
                }

                Cell descriptionCell = row.getCell(1);
                Cell dueDateCell = row.getCell(2);
                Cell priorityCell = row.getCell(3);
                Cell statusCell = row.getCell(4);
                Cell projectIdCell = row.getCell(5);
                Cell userIdCell = row.getCell(6);

                // セルからデータを取得してTaskオブジェクトを作成
                String taskName = taskNameCell.getStringCellValue();
                String description = descriptionCell.getStringCellValue();
                Date dueDate = dueDateCell.getDateCellValue();
                String priority = priorityCell.getStringCellValue();
                String status = statusCell.getStringCellValue();
                long projectId = (long) projectIdCell.getNumericCellValue();
                long userId = (long) userIdCell.getNumericCellValue();

                TaskEntity task = new TaskEntity(taskName, description, dueDate, priority, status, projectId, userId);

                fileUploadMapper.insertTask(task);
            }

            return "success";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

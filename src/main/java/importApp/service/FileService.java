package importApp.service;

import importApp.entity.ActivityEntity;
import importApp.mapper.FileUploadMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;

@Service
public class FileService {

    @Autowired
    private FileUploadMapper fileUploadMapper;

    // ファイルのフォーマットを確認
    public boolean isValidFormat(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // フォーマットが正しいかの確認ロジックを追加
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

                // 必須列のセルを取得
                Cell dateCell = row.getCell(0); // A列: 日付
                if (dateCell == null) {
                    // A列が空白または日付でない場合、ループを終了
                    break;
                }

                Cell nameCell = row.getCell(1); // B列: アクティビティ名
                if (nameCell == null || nameCell.getCellType() != CellType.STRING || nameCell.getStringCellValue().isEmpty()) {
                    // B列が空白の場合、ループを終了
                    break;
                }

                Cell contentsCell = row.getCell(2); // C列: 説明
                Cell startTimeCell = row.getCell(3); // D列: 開始時間
                Cell endTimeCell = row.getCell(4); // E列: 終了時間

                // セルからデータを取得してActivityオブジェクトを作成
                String name = nameCell.getStringCellValue();
                String contents = contentsCell != null && contentsCell.getCellType() == CellType.STRING
                        ? contentsCell.getStringCellValue() : "";

                LocalTime start = null;
                LocalTime end = null;

                // D列の開始時間を処理
                if (startTimeCell != null && startTimeCell.getCellType() == CellType.NUMERIC) {
                    // Excelの日付/時間からLocalTimeを取得
                    Date startDate = startTimeCell.getDateCellValue();
                    start = startDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime();
                }

                // E列の終了時間を処理
                if (endTimeCell != null && endTimeCell.getCellType() == CellType.NUMERIC) {
                    Date endDate = endTimeCell.getDateCellValue();
                    end = endDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime();
                }

                // 日付を取得して、ActivityEntityを作成
                Date date = dateCell.getDateCellValue(); // A列から日付を取得

                ActivityEntity activity = new ActivityEntity(
                        9999, // userIdのサンプル値（必要に応じて適切な値に変更）
                        date, // dateは現在の日時
                        start,
                        end,
                        name,
                        contents,
                        null,        // createdByはひとまずnullでDBインサート
                        null         // updatedByもひとまずnullでDBインサート
                );

                // データを挿入
                fileUploadMapper.insertActivity(activity);
            }

            return "success";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

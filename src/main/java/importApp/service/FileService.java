package importApp.service;

import importApp.entity.ActivityEntity;
import importApp.mapper.FileUploadMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
            return true; // フォーマットが正しい場合
        } catch (Exception e) {
            return false; // フォーマットが正しくない場合
        }
    }

    // ファイルデータの処理
    public String parseExcelFile(MultipartFile file, String userId) {
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
                    break; // A列が空白または日付でない場合
                }

                Cell nameCell = row.getCell(1); // B列: アクティビティ名
                if (nameCell == null || nameCell.getCellType() != CellType.STRING || nameCell.getStringCellValue().isEmpty()) {
                    break; // B列が空白の場合
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
                Date date = dateCell.getDateCellValue();

                ActivityEntity activity = new ActivityEntity(
                        null,              // ID（DBで自動生成される場合はnull）
                        Integer.parseInt(userId), // トークンから取得したuserId
                        date,              // A列から取得した日付
                        start,             // 開始時間
                        end,               // 終了時間
                        name,              // B列のアクティビティ名
                        contents,          // C列の説明
                        (long) Integer.parseInt(userId),
                        convertToDate(LocalDateTime.now()), // 現在日時
                        null, // updatedByは未設定
                        null  // updatedAtは未設定
                );

                // データを挿入
                fileUploadMapper.insertActivity(activity);
            }

            return "success";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // LocalDateTime を Date に変換するメソッドを作成
    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

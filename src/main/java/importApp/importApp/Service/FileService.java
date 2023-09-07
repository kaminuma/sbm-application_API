package importApp.importApp.Service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

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
    public void processFile(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // ファイルデータを処理するロジックを追加
            // 例: Apache POIを使用してセルとROWを取得し、データベースを更新
            // ...
        } catch (Exception e) {
            // エラーハンドリング
        }
    }
}

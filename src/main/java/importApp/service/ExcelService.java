package importApp.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFDataBarFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class ExcelService {

    public XSSFWorkbook createExcelWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("分析");

        // ヘッダー作成
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("日付");
        header.createCell(1).setCellValue("PV");

        // サンプルデータ
        int[] pvData = {8000, 6500, 10000, 3000, 5800, 3200, 7100, 9000, 6700, 4000};
        for (int i = 0; i < pvData.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(String.format("2025/03/%02d", i + 1));
            row.createCell(1).setCellValue(pvData[i]);
        }

        // 条件付き書式（データバー）の設定
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        // 条件式 "1" は常に真なので、全セルに適用
        XSSFConditionalFormattingRule rule = (XSSFConditionalFormattingRule) sheetCF.createConditionalFormattingRule("1");

        // workbook から IndexedColorMap を取得してExcel風の緑色を作成
        IndexedColorMap colorMap = workbook.getStylesSource().getIndexedColors();
        XSSFColor greenColor = new XSSFColor(new Color(46, 125, 50), colorMap);
        XSSFDataBarFormatting dataBar = rule.createDataBarFormatting(greenColor);
        // POI 5.x ではデフォルトで数値が表示されます（showValue = true）

        // 条件付き書式の適用範囲：B2:B11（1行目はヘッダー）
        CellRangeAddress[] regions = { new CellRangeAddress(1, pvData.length, 1, 1) };
        sheetCF.addConditionalFormatting(regions, rule);

        return workbook;
    }
}

package importApp.controller;

import importApp.service.ExcelService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class ExcelDownloadController {

    private final ExcelService excelService;

    @Autowired
    public ExcelDownloadController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/excel/download")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        XSSFWorkbook workbook = excelService.createExcelWorkbook();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=insell-bar.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}

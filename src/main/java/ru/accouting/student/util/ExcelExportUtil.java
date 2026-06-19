package ru.accouting.student.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.accouting.student.dto.StudentCredentialsRow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ExcelExportUtil {

    public static void exportCredentials(List<StudentCredentialsRow> rows, OutputStream os) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Credentials");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] titles = {"Фамилия", "Имя", "Отчество", "Пароль"};

            for (int i = 0; i < titles.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(titles[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (StudentCredentialsRow r : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullSafe(r.lastName()));
                row.createCell(1).setCellValue(nullSafe(r.firstName()));
                row.createCell(2).setCellValue(nullSafe(r.patronymic()));
                row.createCell(3).setCellValue(nullSafe(r.password()));
            }

            for (int i = 0; i < titles.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(os);
            os.flush();
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}

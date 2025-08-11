package com.teamphacode.MerchantManagement.util.form;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class ExcelTemplateHelper {

    public static Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    public static void addTitle(Sheet sheet, String title, int colSpan) {
        Workbook wb = sheet.getWorkbook();
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);

        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleCell.setCellStyle(style);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colSpan));
    }

    public static void addInfoRow(Sheet sheet, String label, String value, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    public static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    public static void addHeaderRow(Sheet sheet, List<String> headers, int rowIndex) {
        Workbook wb = sheet.getWorkbook();
        Row headerRow = sheet.createRow(rowIndex);

        CellStyle style = createHeaderStyle(wb);

        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
        }
    }

    public static void addDataRows(Sheet sheet, List<List<Object>> data, int startRow) {
        Workbook wb = sheet.getWorkbook();
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        int rowIdx = startRow;
        for (List<Object> rowData : data) {
            Row row = sheet.createRow(rowIdx++);
            for (int colIdx = 0; colIdx < rowData.size(); colIdx++) {
                Cell cell = row.createCell(colIdx);
                Object value = rowData.get(colIdx);
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    cell.setCellValue(value != null ? value.toString() : "");
                }
                cell.setCellStyle(style);
            }
        }
    }

    public static void addTotalRow(Sheet sheet, String label, int mergeFromCol, int mergeToCol, int startCol, int endCol, int startDataRow, int rowIndex) {
        Workbook wb = sheet.getWorkbook();
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);

        Row totalRow = sheet.createRow(rowIndex);
        Cell labelCell = totalRow.createCell(mergeFromCol);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, mergeFromCol, mergeToCol));

        for (int col = mergeFromCol; col <= mergeToCol; col++) {
            Cell cell = totalRow.createCell(col);
            cell.setCellStyle(style);
            if (col == mergeFromCol) {
                cell.setCellValue(label);
            }
        }
        for (int col = startCol; col <= endCol; col++) {
            Cell totalCell = totalRow.createCell(col);
            String colLetter = CellReference.convertNumToColString(col);
            totalCell.setCellFormula(String.format("SUM(%s%d:%s%d)", colLetter, startDataRow, colLetter, rowIndex));
            totalCell.setCellStyle(style);
        }
    }

    /**
     * Hàm tạo header nhóm tổng quát, có thể tái sử dụng cho nhiều sheet.
     *
     * @param sheet    Sheet đang thao tác
     * @param workbook Workbook chứa sheet
     * @param startRow Dòng bắt đầu tạo header nhóm
     * @param headers  Danh sách các header nhóm, mỗi phần tử là mảng Object gồm:
     *                 - [0]: cột bắt đầu (int)
     *                 - [1]: số cột gộp (int)
     *                 - [2]: tiêu đề (String)
     */
    public static void createGroupHeader(Sheet sheet, Workbook workbook, int startRow,
                                         List<Object[]> headers) {
        Row groupHeader = sheet.createRow(startRow);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (Object[] header : headers) {
            int colStart = (int) header[0];
            int colSpan = (int) header[1];
            String title = (String) header[2];

            Cell cell = groupHeader.createCell(colStart);
            cell.setCellValue(title);
            cell.setCellStyle(headerStyle);

            if (colSpan > 1) {
                sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, colStart, colStart + colSpan - 1));
            }
        }
    }
}

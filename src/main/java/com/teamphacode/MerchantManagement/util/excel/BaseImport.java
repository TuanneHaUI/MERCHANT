package com.teamphacode.MerchantManagement.util.excel;

import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BaseImport {
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> clazz, Map<String, String> headerToFieldMap) throws Exception {
        List<T> resultList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            // 1. Đọc hàng tiêu đề để xác định vị trí các cột
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return resultList; // File rỗng
            }
            Row headerRow = rowIterator.next();
            Map<Integer, String> indexToFieldMap = mapHeaderIndicesToFields(headerRow, headerToFieldMap);

            // 2. Lặp qua các hàng dữ liệu
            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();

                // Tạo một instance mới của đối tượng DTO
                T instance = clazz.getDeclaredConstructor().newInstance();

                // 3. Đọc từng ô và set giá trị vào đối tượng
                for (Map.Entry<Integer, String> entry : indexToFieldMap.entrySet()) {
                    int columnIndex = entry.getKey();
                    String fieldName = entry.getValue();
                    Cell cell = currentRow.getCell(columnIndex);

                    if (cell != null) {
                        Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object cellValue = getCellValue(cell, field.getType());
                        field.set(instance, cellValue);
                    }
                }
                resultList.add(instance);
            }
        }
        return resultList;
    }

    /**
     * Tạo map ánh xạ từ chỉ số cột (column index) sang tên trường (field name).
     */
    private static Map<Integer, String> mapHeaderIndicesToFields(Row headerRow, Map<String, String> headerToFieldMap) {
        Map<Integer, String> indexToFieldMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerText = cell.getStringCellValue().trim();
            if (headerToFieldMap.containsKey(headerText)) {
                indexToFieldMap.put(cell.getColumnIndex(), headerToFieldMap.get(headerText));
            }
        }
        return indexToFieldMap;
    }

    /**
     * Lấy giá trị của ô và chuyển đổi sang kiểu dữ liệu phù hợp với trường Java.
     */
    private static Object getCellValue(Cell cell, Class<?> fieldType) {
        DataFormatter formatter = new DataFormatter();
        String cellValueStr = formatter.formatCellValue(cell).trim();

        if (cellValueStr.isEmpty()) {
            return null;
        }

        try {
            if (fieldType == String.class) {
                return cellValueStr;
            } else if (fieldType == Long.class || fieldType == long.class) {
                return Long.parseLong(cellValueStr);
            } else if (fieldType == Integer.class || fieldType == int.class) {
                return Integer.parseInt(cellValueStr);
            } else if (fieldType == Double.class || fieldType == double.class) {
                return Double.parseDouble(cellValueStr);
            } else if (fieldType == BigDecimal.class) {
                return new BigDecimal(cellValueStr);
            } else if (fieldType == LocalDateTime.class) {
                    return cell.getLocalDateTimeCellValue();
            } else if (fieldType.isEnum() && fieldType == StatusEnum.class) {
                if ("1".equals(cellValueStr)) {
                    return StatusEnum.Active;
                } else if ("2".equals(cellValueStr)) {
                    return StatusEnum.Close;
                }
                throw new IllegalArgumentException("Giá trị không hợp lệ cho Trạng thái. Chỉ chấp nhận 1 hoặc 2.");
            }
        } catch (Exception e) {
            // Nếu có lỗi chuyển đổi, có thể trả về null hoặc ném ra lỗi tùy chỉnh
            System.err.println("Không thể chuyển đổi giá trị '" + cellValueStr + "' cho kiểu " + fieldType.getName());
            return null;
        }

        return null;
    }
}

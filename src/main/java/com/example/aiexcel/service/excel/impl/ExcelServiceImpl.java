package com.example.aiexcel.service.excel.impl;

import com.example.aiexcel.service.excel.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelServiceImpl implements ExcelService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelServiceImpl.class);

    @Override
    public Workbook loadWorkbook(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
            return convertCsvToWorkbook(file.getInputStream());
        }
        // 使用内部方法避免递归调用
        try (InputStream inputStream = file.getInputStream()) {
            return WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            throw new IOException("Error loading workbook", e);
        }
    }

    @Override
    public Workbook loadWorkbook(InputStream inputStream) throws IOException {
        try {
            return WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            throw new IOException("Error loading workbook", e);
        }
    }

    /**
     * 将CSV输入流转为Excel Workbook
     */
    private Workbook convertCsvToWorkbook(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(); // 创建一个新的Excel工作簿
        Sheet sheet = workbook.createSheet("Sheet1");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int rowNum = 0;

            while ((line = reader.readLine()) != null) {
                Row row = sheet.createRow(rowNum++);
                String[] values = parseCsvLine(line);

                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    String value = values[i];

                    if (value != null && !value.isEmpty()) {
                        // 尝试将值解析为数字
                        try {
                            double numericValue = Double.parseDouble(value);
                            cell.setCellValue(numericValue);
                        } catch (NumberFormatException e) {
                            // 如果不是数字，则作为字符串处理
                            cell.setCellValue(value);
                        }
                    }
                }
            }
        }

        return workbook;
    }

    /**
     * 解析CSV行，处理包含逗号的值（被引号包围的值）
     */
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 处理两个连续的引号作为单个引号
                    currentValue.append('"');
                    i++; // 跳过下一个引号
                } else {
                    // 切换引号状态
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 如果不在引号内遇到逗号，则是一个字段分隔符
                values.add(currentValue.toString());
                currentValue.setLength(0); // 清空当前值
            } else {
                // 添加字符到当前值
                currentValue.append(c);
            }
        }

        // 添加最后一列
        values.add(currentValue.toString());

        return values.toArray(new String[0]);
    }

    @Override
    public void saveWorkbook(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
    }

    @Override
    public byte[] getWorkbookAsBytes(Workbook workbook) throws IOException {
        try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public String getExcelDataAsString(Workbook workbook) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            sb.append("Sheet: ").append(sheet.getSheetName()).append("\n");

            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            sb.append(cell.getStringCellValue()).append("\t");
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                sb.append(cell.getDateCellValue()).append("\t");
                            } else {
                                sb.append(cell.getNumericCellValue()).append("\t");
                            }
                            break;
                        case BOOLEAN:
                            sb.append(cell.getBooleanCellValue()).append("\t");
                            break;
                        case FORMULA:
                            // 对于公式单元格，添加计算结果而不是公式本身
                            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                            CellValue cellValue = evaluator.evaluate(cell);
                            switch (cellValue.getCellType()) {
                                case STRING:
                                    sb.append(cellValue.getStringValue()).append("\t");
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        sb.append(DateUtil.getJavaDate(cellValue.getNumberValue())).append("\t");
                                    } else {
                                        sb.append(cellValue.getNumberValue()).append("\t");
                                    }
                                    break;
                                case BOOLEAN:
                                    sb.append(cellValue.getBooleanValue()).append("\t");
                                    break;
                                case ERROR:
                                    sb.append("#ERROR!").append("\t");
                                    break;
                                default:
                                    sb.append(cellValue.formatAsString()).append("\t");
                            }
                            break;
                        default:
                            sb.append(cell.toString()).append("\t");
                    }
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public Object[][] getExcelDataAsArray(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
        int lastRowNum = sheet.getLastRowNum();
        Row firstRow = sheet.getRow(0);
        int lastCellNum = (firstRow != null) ? firstRow.getLastCellNum() : 0;

        Object[][] data = new Object[lastRowNum + 1][lastCellNum];

        for (int i = 0; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < lastCellNum; j++) {
                if (row != null) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                data[i][j] = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    data[i][j] = cell.getDateCellValue();
                                } else {
                                    data[i][j] = cell.getNumericCellValue();
                                }
                                break;
                            case BOOLEAN:
                                data[i][j] = cell.getBooleanCellValue();
                                break;
                            case FORMULA:
                                // 对于公式单元格，返回计算结果而不是公式本身
                                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                CellValue cellValue = evaluator.evaluate(cell);
                                switch (cellValue.getCellType()) {
                                    case STRING:
                                        data[i][j] = cellValue.getStringValue();
                                        break;
                                    case NUMERIC:
                                        data[i][j] = cellValue.getNumberValue();
                                        break;
                                    case BOOLEAN:
                                        data[i][j] = cellValue.getBooleanValue();
                                        break;
                                    case ERROR:
                                        data[i][j] = "#ERROR!";
                                        break;
                                    default:
                                        data[i][j] = cellValue.formatAsString();
                                        break;
                                }
                                break;
                            default:
                                data[i][j] = null;
                        }
                    } else {
                        data[i][j] = null;
                    }
                } else {
                    data[i][j] = null;
                }
            }
        }

        return data;
    }

    @Override
    public void updateCell(Workbook workbook, String sheetName, int rowIndex, int colIndex, Object value) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }

        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Double || value instanceof Float) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Integer || value instanceof Long) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
    }

    @Override
    public void updateRange(Workbook workbook, String sheetName, int startRowIndex, int startColIndex,
                           int endRowIndex, int endColIndex, Object[][] values) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        int valueRow = 0;
        for (int i = startRowIndex; i <= endRowIndex && valueRow < values.length; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                row = sheet.createRow(i);
            }

            int valueCol = 0;
            for (int j = startColIndex; j <= endColIndex && valueCol < values[valueRow].length; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    cell = row.createCell(j);
                }

                Object value = values[valueRow][valueCol];
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Double || value instanceof Float) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Integer || value instanceof Long) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else {
                    cell.setCellValue(value != null ? value.toString() : "");
                }

                valueCol++;
            }
            valueRow++;
        }
    }

    // Enhanced Excel processing capabilities

    @Override
    public void applyCellFormatting(Workbook workbook, String sheetName, int rowIndex, int colIndex,
                                   String format, String color) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }

        CellStyle cellStyle = workbook.createCellStyle();

        // Apply format pattern if specified
        if (format != null && !format.isEmpty()) {
            short formatIndex = workbook.getCreationHelper().createDataFormat().getFormat(format);
            cellStyle.setDataFormat(formatIndex);
        }

        // Apply color if specified
        if (color != null && !color.isEmpty()) {
            try {
                // This would require mapping color names to indexes
                // For simplicity, using a predefined set of colors
                short colorIndex = getIndexedColor(color);
                if (colorIndex != -1) {
                    cellStyle.setFillForegroundColor(colorIndex);
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
            } catch (Exception e) {
                // If color parsing fails, continue without color
            }
        }

        cell.setCellStyle(cellStyle);
    }

    @Override
    public void insertRow(Workbook workbook, String sheetName, int rowIndex, Object[] values) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        // Shift existing rows down starting from the insertion point
        sheet.shiftRows(rowIndex, sheet.getLastRowNum(), 1);

        // Create the new row
        Row newRow = sheet.createRow(rowIndex);

        // Set values for the new row
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Cell cell = newRow.createCell(i);
                Object value = values[i];

                if (value != null) {
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Double || value instanceof Float) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Integer || value instanceof Long) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    } else {
                        cell.setCellValue(value.toString());
                    }
                } else {
                    cell.setCellValue("");
                }
            }
        }
    }

    @Override
    public void insertColumn(Workbook workbook, String sheetName, int colIndex, Object[] values) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        // For each row, insert the new column cell at the specified index
        for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                row = sheet.createRow(i);
            }

            // Shift cells to the right starting from the end of the row
            for (int j = Math.max(row.getLastCellNum(), colIndex); j > colIndex; j--) {
                Cell srcCell = row.getCell(j - 1);
                if (srcCell != null) {
                    Cell targetCell = row.getCell(j);
                    if (targetCell == null) {
                        targetCell = row.createCell(j);
                    }
                    // Copy the cell content
                    copyCellContent(srcCell, targetCell);
                }
            }

            // Create the new cell at the specified index
            Cell newCell = row.getCell(colIndex);
            if (newCell == null) {
                newCell = row.createCell(colIndex);
            }

            // Set value for this position if available
            if (values != null && i < values.length) {
                Object value = values[i];
                if (value != null) {
                    if (value instanceof String) {
                        newCell.setCellValue((String) value);
                    } else if (value instanceof Double || value instanceof Float) {
                        newCell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Integer || value instanceof Long) {
                        newCell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        newCell.setCellValue((Boolean) value);
                    } else {
                        newCell.setCellValue(value.toString());
                    }
                } else {
                    newCell.setCellValue("");
                }
            } else {
                newCell.setCellValue("");
            }
        }
    }

    @Override
    public void deleteRow(Workbook workbook, String sheetName, int rowIndex) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            sheet.removeRow(row);
        }
        // Shift rows up starting from the row after the deleted row
        sheet.shiftRows(rowIndex + 1, sheet.getLastRowNum(), -1);
    }

    @Override
    public void deleteColumn(Workbook workbook, String sheetName, int colIndex) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        // For each row, delete the cell at the specified index
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                // Remove the cell at the specified index
                Cell cell = row.getCell(colIndex);
                if (cell != null) {
                    row.removeCell(cell);
                }

                // Shift cells to the left after the deleted column
                for (int j = colIndex; j < row.getLastCellNum(); j++) {
                    Cell srcCell = row.getCell(j + 1);
                    if (srcCell != null) {
                        Cell targetCell = row.getCell(j);
                        if (targetCell == null) {
                            targetCell = row.createCell(j);
                        }
                        copyCellContent(srcCell, targetCell);
                        row.removeCell(srcCell);
                    }
                }
            }
        }
    }

    @Override
    public void applyFormula(Workbook workbook, String sheetName, int rowIndex, int colIndex, String formula) {
        // 不执行任何操作，因为我们现在在AiExcelCommandParser中处理公式计算
        // 这里保持为空实现以确保接口兼容性
        logger.debug("applyFormula called but skipped as formula evaluation is handled in AiExcelCommandParser. Formula: {}", formula);
    }

    @Override
    public String getCellValue(Workbook workbook, String sheetName, int rowIndex, int colIndex) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet != null) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                Cell cell = row.getCell(colIndex);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case STRING:
                            return cell.getStringCellValue();
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                return cell.getDateCellValue().toString();
                            } else {
                                double value = cell.getNumericCellValue();
                                // Check if it's actually an integer to avoid .0 decimals
                                if (value == Math.floor(value)) {
                                    return String.valueOf((int) value);
                                } else {
                                    return String.valueOf(value);
                                }
                            }
                        case BOOLEAN:
                            return String.valueOf(cell.getBooleanCellValue());
                        case FORMULA:
                            // 对于公式单元格，返回计算结果而不是公式本身
                            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                            CellValue cellValue = evaluator.evaluate(cell);
                            switch (cellValue.getCellType()) {
                                case STRING:
                                    return cellValue.getStringValue();
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        // 这是日期格式的数字
                                        return DateUtil.getJavaDate(cellValue.getNumberValue()).toString();
                                    } else {
                                        double value = cellValue.getNumberValue();
                                        // 检查是否实际为整数以避免 .0 小数
                                        if (value == Math.floor(value)) {
                                            return String.valueOf((int) value);
                                        } else {
                                            return String.valueOf(value);
                                        }
                                    }
                                case BOOLEAN:
                                    return String.valueOf(cellValue.getBooleanValue());
                                case ERROR:
                                    return "#ERROR!";
                                default:
                                    return cellValue.formatAsString();
                            }
                        case BLANK:
                            return "";
                        default:
                            return cell.toString();
                    }
                }
            }
        }
        return "";
    }

    @Override
    public int getRowCount(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet != null) {
            return sheet.getLastRowNum() + 1; // +1 because last row num is 0-indexed
        }
        return 0;
    }

    @Override
    public int getColumnCount(Workbook workbook, String sheetName, int rowIndex) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet != null) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                return row.getLastCellNum();
            }
        }
        return 0;
    }

    @Override
    public String[] getExcelHeaders(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0); // 默认获取第一个工作表
        if (sheet == null || sheet.getFirstRowNum() < 0) {
            return new String[0];
        }

        Row firstRow = sheet.getRow(sheet.getFirstRowNum());
        if (firstRow == null) {
            return new String[0];
        }

        int lastCellNum = firstRow.getLastCellNum();
        String[] headers = new String[lastCellNum];

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = firstRow.getCell(i);
            headers[i] = getCellValueAsString(cell);
            if (headers[i] == null || headers[i].trim().isEmpty()) {
                // 如果表头单元格为空，使用列字母作为表头
                headers[i] = getColumnLetter(i + 1);
            }
        }

        return headers;
    }

    /**
     * 获取单元格值的字符串表示
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // 检查是否为整数
                        if (cell.getNumericCellValue() == Math.floor(cell.getNumericCellValue())) {
                            return String.valueOf((long) cell.getNumericCellValue());
                        } else {
                            return String.valueOf(cell.getNumericCellValue());
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    // 对于公式单元格，返回计算结果而不是公式本身
                    // 需要获取所在工作簿以创建公式计算器
                    Workbook workbook = cell.getSheet().getWorkbook();
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    switch (cellValue.getCellType()) {
                        case STRING:
                            return cellValue.getStringValue();
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                return DateUtil.getJavaDate(cellValue.getNumberValue()).toString();
                            } else {
                                double value = cellValue.getNumberValue();
                                // 检查是否实际为整数以避免 .0 小数
                                if (value == Math.floor(value)) {
                                    return String.valueOf((int) value);
                                } else {
                                    return String.valueOf(value);
                                }
                            }
                        case BOOLEAN:
                            return String.valueOf(cellValue.getBooleanValue());
                        case ERROR:
                            return "#ERROR!";
                        default:
                            return cellValue.formatAsString();
                    }
                case BLANK:
                    return "";
                case ERROR:
                    return "#ERROR!";
                default:
                    return cell.toString();
            }
        } catch (Exception e) {
            // 记录异常但继续处理
            return cell.toString();
        }
    }

    /**
     * 将列索引转换为字母 (0->A, 1->B, ..., 26->AA, etc.)
     */
    private String getColumnLetter(int columnIndex) {
        if (columnIndex <= 0) {
            return "";
        }

        StringBuilder columnName = new StringBuilder();
        while (columnIndex > 0) {
            columnIndex--; // 调整为0基索引
            columnName.insert(0, (char) ('A' + columnIndex % 26));
            columnIndex = columnIndex / 26;
        }
        return columnName.toString();
    }

    /**
     * Maps color names to Excel IndexedColors
     */
    private short getIndexedColor(String colorName) {
        if (colorName == null) return -1;

        String upperColor = colorName.toUpperCase().trim();

        switch (upperColor) {
            case "BLACK":
                return IndexedColors.BLACK.getIndex();
            case "WHITE":
                return IndexedColors.WHITE.getIndex();
            case "RED":
                return IndexedColors.RED.getIndex();
            case "BRIGHT_GREEN":
                return IndexedColors.BRIGHT_GREEN.getIndex();
            case "BLUE":
                return IndexedColors.BLUE.getIndex();
            case "YELLOW":
                return IndexedColors.YELLOW.getIndex();
            case "PINK":
                return IndexedColors.PINK.getIndex();
            case "TURQUOISE":
                return IndexedColors.TURQUOISE.getIndex();
            case "DARK_RED":
                return IndexedColors.DARK_RED.getIndex();
            case "GREEN":
                return IndexedColors.GREEN.getIndex();
            case "DARK_BLUE":
                return IndexedColors.DARK_BLUE.getIndex();
            case "DARK_YELLOW":
                return IndexedColors.DARK_YELLOW.getIndex();
            case "VIOLET":
                return IndexedColors.VIOLET.getIndex();
            case "TEAL":
                return IndexedColors.TEAL.getIndex();
            case "GREY_25_PERCENT":
                return IndexedColors.GREY_25_PERCENT.getIndex();
            case "GREY_50_PERCENT":
                return IndexedColors.GREY_50_PERCENT.getIndex();
            case "CORAL":
                return IndexedColors.CORAL.getIndex();
            case "ROYAL_BLUE":
                return IndexedColors.ROYAL_BLUE.getIndex();
            default:
                return -1; // Indicates color not found in predefined set
        }
    }

    /**
     * Copy cell content from source to target
     */
    private void copyCellContent(Cell src, Cell target) {
        if (src == null || target == null) return;

        switch (src.getCellType()) {
            case STRING:
                target.setCellValue(src.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(src)) {
                    target.setCellValue(src.getDateCellValue());
                } else {
                    target.setCellValue(src.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                target.setCellValue(src.getBooleanCellValue());
                break;
            case FORMULA:
                target.setCellFormula(src.getCellFormula());
                break;
            case BLANK:
                target.setBlank();
                break;
            case ERROR:
                target.setCellErrorValue(src.getErrorCellValue());
                break;
            default:
                target.setCellValue(src.toString());
        }
    }

    /**
     * 计算单元格公式并更新单元格值
     * @param workbook 工作簿
     * @param cell 要计算的单元格
     */
    @SuppressWarnings("unused")
    private void evaluateFormulaCell(Workbook workbook, Cell cell) {
        if (cell == null) {
            return;
        }

        // 创建公式计算器
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 计算公式并更新单元格值
        evaluator.evaluateInCell(cell);
    }

    /**
     * 计算单元格公式并将结果设置回单元格（替换公式）
     * @param workbook 工作簿
     * @param cell 要计算的单元格
     */
    @SuppressWarnings("unused")
    private void evaluateFormulaCellAndSetValue(Workbook workbook, Cell cell) {
        if (cell == null) {
            return;
        }

        // 创建公式计算器
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 计算公式获取结果
        CellValue cellValue = evaluator.evaluate(cell);

        // 根据结果类型设置单元格值（替换原来的公式）
        switch (cellValue.getCellType()) {
            case STRING:
                cell.setCellValue(cellValue.getStringValue());
                break;
            case NUMERIC:
                cell.setCellValue(cellValue.getNumberValue());
                break;
            case BOOLEAN:
                cell.setCellValue(cellValue.getBooleanValue());
                break;
            case ERROR:
                cell.setCellErrorValue(cellValue.getErrorValue());
                break;
            default:
                cell.setCellValue(cellValue.formatAsString());
                break;
        }
    }

    /**
     * 计算整个工作簿中的所有公式并将结果设置回单元格（替换公式）
     * @param workbook 工作簿
     */
    public void evaluateAllFormulasInWorkbook(Workbook workbook) {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 遍历所有工作表
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);

            // 遍历所有行
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                // 遍历所有单元格
                for (org.apache.poi.ss.usermodel.Cell cell : row) {
                    if (cell.getCellType() == CellType.FORMULA) {
                        // 计算公式并获取结果
                        CellValue cellValue = evaluator.evaluate(cell);

                        // 根据结果类型设置单元格值（替换原来的公式）
                        switch (cellValue.getCellType()) {
                            case STRING:
                                cell.setCellValue(cellValue.getStringValue());
                                break;
                            case NUMERIC:
                                cell.setCellValue(cellValue.getNumberValue());
                                break;
                            case BOOLEAN:
                                cell.setCellValue(cellValue.getBooleanValue());
                                break;
                            case ERROR:
                                cell.setCellErrorValue(cellValue.getErrorValue());
                                break;
                            default:
                                cell.setCellValue(cellValue.formatAsString());
                                break;
                        }
                    }
                }
            }
        }
    }
}
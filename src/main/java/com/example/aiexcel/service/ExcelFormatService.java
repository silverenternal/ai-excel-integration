package com.example.aiexcel.service;

import com.example.aiexcel.model.FormatOptions;
import com.example.aiexcel.service.excel.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Excel格式设置服务
 * 提供高级格式设置功能
 */
@Service
public class ExcelFormatService {

    @Autowired
    private ExcelService excelService;

    private static final Logger logger = LoggerFactory.getLogger(ExcelFormatService.class);

    /**
     * 格式化单个单元格
     */
    public boolean formatCell(MultipartFile file, String sheetName, int rowIndex, int colIndex, FormatOptions formatOptions) {
        try {
            Workbook workbook = excelService.loadWorkbook(file);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.error("Sheet {} not found", sheetName);
                return false;
            }

            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            Cell cell = row.getCell(colIndex);
            if (cell == null) {
                cell = row.createCell(colIndex);
            }

            // 创建或获取现有的单元格样式
            CellStyle cellStyle = createCellStyle(workbook, formatOptions, cell.getCellStyle());

            // 应用样式到单元格
            cell.setCellStyle(cellStyle);

            // 更新Excel服务中的工作簿（这取决于具体实现）
            // 注意：这里我们只是修改了workbook对象，实际保存或应用更改取决于具体需求

            logger.info("Successfully formatted cell at ({}, {}) in sheet {}", rowIndex, colIndex, sheetName);
            return true;
        } catch (Exception e) {
            logger.error("Error formatting cell at ({}, {}) in sheet {}", rowIndex, colIndex, sheetName, e);
            return false;
        }
    }

    /**
     * 格式化单元格范围
     */
    public boolean formatCellRange(MultipartFile file, String sheetName, int startRow, int startCol, 
                                  int endRow, int endCol, FormatOptions formatOptions) {
        try {
            Workbook workbook = excelService.loadWorkbook(file);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.error("Sheet {} not found", sheetName);
                return false;
            }

            // 创建样式
            CellStyle cellStyle = createCellStyle(workbook, formatOptions, null);

            // 应用样式到范围内的所有单元格
            for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }

                for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    if (cell == null) {
                        cell = row.createCell(colIndex);
                    }
                    cell.setCellStyle(cellStyle);
                }
            }

            logger.info("Successfully formatted cell range ({}, {}) to ({}, {}) in sheet {}", 
                       startRow, startCol, endRow, endCol, sheetName);
            return true;
        } catch (Exception e) {
            logger.error("Error formatting cell range in sheet {}", sheetName, e);
            return false;
        }
    }

    /**
     * 格式化整行
     */
    public boolean formatRow(MultipartFile file, String sheetName, int rowIndex, FormatOptions formatOptions) {
        try {
            Workbook workbook = excelService.loadWorkbook(file);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.error("Sheet {} not found", sheetName);
                return false;
            }

            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            // 创建样式
            CellStyle cellStyle = createCellStyle(workbook, formatOptions, null);

            // 应用样式到该行的所有单元格
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (cell == null) {
                    cell = row.createCell(i);
                }
                cell.setCellStyle(cellStyle);
            }

            // 如果需要设置整行高度
            if (formatOptions.getFontSize() != null) {
                row.setHeight((short) (formatOptions.getFontSize() * 25)); // 简单的字体大小到行高的转换
            }

            logger.info("Successfully formatted row {} in sheet {}", rowIndex, sheetName);
            return true;
        } catch (Exception e) {
            logger.error("Error formatting row {} in sheet {}", rowIndex, sheetName, e);
            return false;
        }
    }

    /**
     * 格式化整列
     */
    public boolean formatColumn(MultipartFile file, String sheetName, int colIndex, FormatOptions formatOptions) {
        try {
            Workbook workbook = excelService.loadWorkbook(file);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.error("Sheet {} not found", sheetName);
                return false;
            }

            // 创建样式
            CellStyle cellStyle = createCellStyle(workbook, formatOptions, null);

            // 设置整列宽度（如果指定了相关信息）
            if (formatOptions.getFontSize() != null) {
                sheet.setColumnWidth(colIndex, formatOptions.getFontSize() * 256); // 简单的字体大小到列宽的转换
            }

            // 应用样式到该列的所有单元格
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(colIndex);
                    if (cell != null) {
                        cell.setCellStyle(cellStyle);
                    } else {
                        // 如果单元格不存在，可以选择创建一个再设置样式，或者跳过
                        Cell newCell = row.createCell(colIndex);
                        newCell.setCellStyle(cellStyle);
                    }
                }
            }

            logger.info("Successfully formatted column {} in sheet {}", colIndex, sheetName);
            return true;
        } catch (Exception e) {
            logger.error("Error formatting column {} in sheet {}", colIndex, sheetName, e);
            return false;
        }
    }

    /**
     * 合并单元格并应用格式
     */
    public boolean mergeAndFormatCells(MultipartFile file, String sheetName, int startRow, int startCol, 
                                      int endRow, int endCol, FormatOptions formatOptions) {
        try {
            Workbook workbook = excelService.loadWorkbook(file);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.error("Sheet {} not found", sheetName);
                return false;
            }

            // 创建样式
            CellStyle cellStyle = createCellStyle(workbook, formatOptions, null);

            // 合并单元格
            CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
            sheet.addMergedRegion(region);

            // 在合并区域的左上角单元格设置样式
            Row firstRow = sheet.getRow(startRow);
            if (firstRow == null) {
                firstRow = sheet.createRow(startRow);
            }
            Cell firstCell = firstRow.getCell(startCol);
            if (firstCell == null) {
                firstCell = firstRow.createCell(startCol);
            }
            firstCell.setCellStyle(cellStyle);

            logger.info("Successfully merged and formatted cells from ({}, {}) to ({}, {}) in sheet {}", 
                       startRow, startCol, endRow, endCol, sheetName);
            return true;
        } catch (Exception e) {
            logger.error("Error merging and formatting cells in sheet {}", sheetName, e);
            return false;
        }
    }

    /**
     * 从Map创建FormatOptions对象
     */
    public FormatOptions createFormatOptionsFromMap(Map<String, Object> formatMap) {
        FormatOptions options = new FormatOptions();
        
        if (formatMap.containsKey("bold")) {
            options.setBold((Boolean) formatMap.get("bold"));
        }
        if (formatMap.containsKey("italic")) {
            options.setItalic((Boolean) formatMap.get("italic"));
        }
        if (formatMap.containsKey("fontSize")) {
            options.setFontSize((Integer) formatMap.get("fontSize"));
        }
        if (formatMap.containsKey("fontColor")) {
            options.setFontColor((String) formatMap.get("fontColor"));
        }
        if (formatMap.containsKey("backgroundColor")) {
            options.setBackgroundColor((String) formatMap.get("backgroundColor"));
        }
        if (formatMap.containsKey("borderColor")) {
            options.setBorderColor((String) formatMap.get("borderColor"));
        }
        if (formatMap.containsKey("borderStyle")) {
            options.setBorderStyle((String) formatMap.get("borderStyle"));
        }
        if (formatMap.containsKey("horizontalAlignment")) {
            options.setHorizontalAlignment((String) formatMap.get("horizontalAlignment"));
        }
        if (formatMap.containsKey("verticalAlignment")) {
            options.setVerticalAlignment((String) formatMap.get("verticalAlignment"));
        }
        if (formatMap.containsKey("numberFormat")) {
            options.setNumberFormat((String) formatMap.get("numberFormat"));
        }
        if (formatMap.containsKey("wrapText")) {
            options.setWrapText((Boolean) formatMap.get("wrapText"));
        }
        if (formatMap.containsKey("rotation")) {
            options.setRotation((Integer) formatMap.get("rotation"));
        }

        return options;
    }

    /**
     * 创建单元格样式
     */
    private CellStyle createCellStyle(Workbook workbook, FormatOptions formatOptions, CellStyle baseStyle) {
        CellStyle cellStyle = baseStyle != null ? baseStyle : workbook.createCellStyle();

        if (formatOptions == null) {
            return cellStyle; // 返回基础样式
        }

        // 设置字体
        Font font = workbook.createFont();
        if (formatOptions.getBold() != null) {
            font.setBold(formatOptions.getBold());
        }
        if (formatOptions.getItalic() != null) {
            font.setItalic(formatOptions.getItalic());
        }
        if (formatOptions.getFontSize() != null) {
            font.setFontHeightInPoints(formatOptions.getFontSize().shortValue());
        }
        if (formatOptions.getFontColor() != null) {
            // 简化处理，实际中可能需要将颜色名转换为索引
            // 这里只是示例，实际实现需要更完整的颜色处理
        }
        cellStyle.setFont(font);

        // 设置背景色
        if (formatOptions.getBackgroundColor() != null) {
            // 实际实现需要将颜色名称或值转换为Excel支持的格式
            // 这只是一个示例框架
        }

        // 设置对齐方式
        if (formatOptions.getHorizontalAlignment() != null) {
            switch (formatOptions.getHorizontalAlignment().toLowerCase()) {
                case "left":
                    cellStyle.setAlignment(HorizontalAlignment.LEFT);
                    break;
                case "center":
                    cellStyle.setAlignment(HorizontalAlignment.CENTER);
                    break;
                case "right":
                    cellStyle.setAlignment(HorizontalAlignment.RIGHT);
                    break;
                default:
                    cellStyle.setAlignment(HorizontalAlignment.GENERAL);
            }
        }

        if (formatOptions.getVerticalAlignment() != null) {
            switch (formatOptions.getVerticalAlignment().toLowerCase()) {
                case "top":
                    cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
                    break;
                case "middle":
                    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    break;
                case "bottom":
                    cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
                    break;
                default:
                    cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
            }
        }

        // 设置边框
        if (formatOptions.getBorderStyle() != null) {
            BorderStyle border = BorderStyle.valueOf(formatOptions.getBorderStyle().toUpperCase());
            cellStyle.setBorderBottom(border);
            cellStyle.setBorderTop(border);
            cellStyle.setBorderLeft(border);
            cellStyle.setBorderRight(border);
        }

        // 设置文本换行
        if (formatOptions.getWrapText() != null) {
            cellStyle.setWrapText(formatOptions.getWrapText());
        }

        // 设置数字格式
        if (formatOptions.getNumberFormat() != null) {
            short formatIndex = workbook.getCreationHelper().createDataFormat().getFormat(formatOptions.getNumberFormat());
            cellStyle.setDataFormat(formatIndex);
        }

        return cellStyle;
    }
}
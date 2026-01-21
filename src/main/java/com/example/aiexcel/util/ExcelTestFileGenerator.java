package com.example.aiexcel.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelTestFileGenerator {
    
    public static void main(String[] args) {
        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("员工数据");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"姓名", "年龄", "部门", "薪资", "入职日期"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 添加数据
            Object[][] data = {
                {"张三", 28, "销售部", 8000, "2022-03-15"},
                {"李四", 32, "技术部", 12000, "2021-07-20"},
                {"王五", 25, "市场部", 7000, "2023-01-10"},
                {"赵六", 30, "人事部", 9000, "2022-11-05"},
                {"钱七", 27, "技术部", 11000, "2023-05-12"},
                {"孙八", 29, "销售部", 8500, "2022-09-18"},
                {"周九", 31, "财务部", 10000, "2021-12-03"},
                {"吴十", 26, "市场部", 7500, "2023-03-22"}
            };
            
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    if (data[i][j] instanceof String) {
                        row.createCell(j).setCellValue((String) data[i][j]);
                    } else if (data[i][j] instanceof Integer) {
                        row.createCell(j).setCellValue((Integer) data[i][j]);
                    } else if (data[i][j] instanceof Double) {
                        row.createCell(j).setCellValue((Double) data[i][j]);
                    }
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存文件
            FileOutputStream fileOut = new FileOutputStream("test_data.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            
            System.out.println("Excel测试文件已成功创建: test_data.xlsx");
            System.out.println("文件包含员工数据，可用于测试AI Excel集成功能");
            
        } catch (IOException e) {
            System.err.println("创建Excel文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
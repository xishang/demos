package com.demos.java.poidemo.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/7
 * excel数据填充
 */
public abstract class ExcelData {

    public static final String OUTPUT_DIR = "/Users/xishang/temp/xlsx/";
    public static final int PAGE_DATA_SIZE = 20;

    // excel模版文件
    private String templateFile;
    // 静态数据
    private List<ExcelElement> staticData;
    // 动态数据
    private List<Object> dynamicData;
    // 输出文件索引
    private int fileIndex;
    // 临时文件名-前缀
    private String filePrefix;

    // 动态文件起始行
    private int dynamicStartIndex;

    public ExcelData(String templateFile, int dynamicStartIndex) {
        this.dynamicStartIndex = dynamicStartIndex;
        this.templateFile = templateFile;
        filePrefix = UUID.randomUUID().toString().replace("-", "");
    }

    // 准备静态数据
    public abstract List<ExcelElement> prepareStaticData();
    // 准备动态数据
    public abstract List<Object> prepareDynamicData();
    // 单元格样式
    public abstract Map<Integer, CellStyle> getCellStyleMap(XSSFSheet sheet);

    // 生成单个文件
    public String generateOneFile() throws Exception {
        String outputFile = OUTPUT_DIR + filePrefix + ".xlsx";
        staticData = prepareStaticData();
        InputStream is = new FileInputStream(templateFile);
        XSSFWorkbook wb = new XSSFWorkbook(is);
        XSSFSheet sheet = wb.getSheetAt(0);
        // 填充静态数据
        for (ExcelElement data : staticData) {
            Row row = sheet.getRow(data.getRowIndex());
            if (row == null) {
                continue;
            }
            Cell cell = row.getCell(data.getColIndex());
            if (cell != null) {
                cell.setCellValue(getFormatData(data.getData()));
            }
        }
        // 填充动态数据
        Map<Integer, CellStyle> cellStyleMap = getCellStyleMap(sheet);
        dynamicData = prepareDynamicData();
        int dynamicIndex = dynamicStartIndex;
        // 填充动态数据
        for (Object data : dynamicData) {
            // 创建新的行
            if (sheet.getRow(dynamicIndex) != null) {
                sheet.shiftRows(dynamicIndex, sheet.getLastRowNum(), 1, true, false);
            }
            XSSFRow row = sheet.createRow(dynamicIndex++);
            Class<?> clazz = data.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                // 解析@ExcelCell标注的字段
                if (field.isAnnotationPresent(ExcelCell.class)) {
                    ExcelCell excelCell = field.getAnnotation(ExcelCell.class);
                    int cellIndex = excelCell.cellIndex();
                    // 创建单元格
                    XSSFCell cell = row.createCell(cellIndex);
                    // 设置单元格样式
                    CellStyle cellStyle = cellStyleMap.get(cellIndex);
                    if (cellStyle != null) {
                        cell.setCellStyle(cellStyle);
                    }
                    // 设置值
                    field.setAccessible(true);
                    cell.setCellValue(getFormatData(field.get(data)));
                }
            }
        }
        // 输出文件
        FileOutputStream fos = new FileOutputStream(outputFile);
        wb.write(fos);
        fos.close();
        return outputFile;
    }

    // 生成文件列表
    public List<String> generateFiles() throws Exception {
        fileIndex = 0;
        List<String> files = new ArrayList<>();
        String staticFile = generateStaticFile();
        if (StringUtils.isNotEmpty(staticFile)) {
            files.add(staticFile);
        }
        List<String> dynamicFiles = generateDynamicFiles();
        if (dynamicFiles != null) {
            files.addAll(dynamicFiles);
        }
        return files;
    }

    private String generateStaticFile() throws Exception {
        staticData = prepareStaticData();
        InputStream is = new FileInputStream(templateFile);
        XSSFWorkbook wb = new XSSFWorkbook(is);
        XSSFSheet sheet = wb.getSheetAt(0);
        // 填充静态数据
        for (ExcelElement data : staticData) {
            Row row = sheet.getRow(data.getRowIndex());
            if (row == null) {
                continue;
            }
            Cell cell = row.getCell(data.getColIndex());
            if (cell != null) {
                cell.setCellValue(getFormatData(data.getData()));
            }
        }
        // 输出文件
        String outputFile = generateFileName();
        FileOutputStream fos = new FileOutputStream(outputFile);
        wb.write(fos);
        fos.close();
        return outputFile;
    }

    private List<String> generateDynamicFiles() throws Exception {
        List<String> fileList = new ArrayList<>();
        dynamicData = prepareDynamicData();
        List<Object> tempList = new ArrayList<>();
        for (Object data : dynamicData) {
            tempList.add(data);
            if (tempList.size() == PAGE_DATA_SIZE) {
                String outputFile = generateFileName();
                fileList.add(generateFile(tempList, outputFile));
                tempList = new ArrayList<>();
            }
        }
        if (!tempList.isEmpty()) {
            String outputFile = generateFileName();
            fileList.add(generateFile(tempList, outputFile));
        }
        return fileList;
    }

    // 动态数据生成文件
    public String generateFile(List<Object> list, String outputFile) throws Exception {
        InputStream is = new FileInputStream(templateFile);
        XSSFWorkbook wb = new XSSFWorkbook(is);
        XSSFSheet sheet = wb.getSheetAt(0);
        Map<Integer, CellStyle> cellStyleMap = getCellStyleMap(sheet);
        int dynamicIndex = dynamicStartIndex;
        // 填充动态数据
        for (Object data : list) {
            // 创建新的行
            if (sheet.getRow(dynamicIndex) != null) {
                sheet.shiftRows(dynamicIndex, sheet.getLastRowNum(), 1, true, false);
            }
            XSSFRow row = sheet.createRow(dynamicIndex++);
            Class<?> clazz = data.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                // 解析@ExcelCell标注的字段
                if (field.isAnnotationPresent(ExcelCell.class)) {
                    ExcelCell excelCell = field.getAnnotation(ExcelCell.class);
                    int cellIndex = excelCell.cellIndex();
                    // 创建单元格
                    XSSFCell cell = row.createCell(cellIndex);
                    // 设置单元格样式
                    CellStyle cellStyle = cellStyleMap.get(cellIndex);
                    if (cellStyle != null) {
                        cell.setCellStyle(cellStyle);
                    }
                    // 设置值
                    field.setAccessible(true);
                    cell.setCellValue(getFormatData(field.get(data)));
                }
            }
        }
        // 输出文件
        FileOutputStream fos = new FileOutputStream(outputFile);
        wb.write(fos);
        fos.close();
        return outputFile;
    }

    // 格式化数据
    public String getFormatData(Object data) {
        return String.valueOf(data);
    }

    public String generateFileName() {
        return OUTPUT_DIR + filePrefix + "_" + fileIndex++ + ".xlsx";
    }

}

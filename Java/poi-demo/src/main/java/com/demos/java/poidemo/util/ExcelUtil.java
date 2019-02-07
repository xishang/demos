package com.demos.java.poidemo.util;


import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ExcelUtil {

	/**
	 * xls格式读取
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static List[] readXls(File file) throws Exception {
		InputStream is = new FileInputStream(file);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
        List[] data = null;
		try {
	        // 获取第一个工作薄
	        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
	        if (hssfSheet == null) {
	            return null;
	        }
	        // 获取当前工作薄的每一行
	        data = new List[hssfSheet.getLastRowNum()];
	        for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
	            HSSFRow hssfRow = hssfSheet.getRow(rowNum);
	            if (hssfRow != null) {
	            	List<Object> list = new ArrayList<>();
	            	for(int i = 0; i < hssfRow.getPhysicalNumberOfCells(); i++){
	            		list.add(cell(hssfRow.getCell(i)));
	            	}
	            	data[rowNum-1] = list;
	            }
	        }
		} catch (Exception e) {
		} finally {
			if (hssfWorkbook != null){
				hssfWorkbook.close();
			}
			if(null != is){
				is.close();
			}
		}
		
		return data;
	}
	
	/**
	 * xlsx格式读取
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static List[] readXlsx(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		XSSFWorkbook wb=null;
		List[] data = null;
		try {
			wb = new XSSFWorkbook(fis);
			XSSFSheet sheet = wb.getSheetAt(0);  //只取第一张工作表
			if (null == sheet) {
				return null;
			}
			sheet.removeRow(sheet.getRow(0));//移除表头
			data = new List[sheet.getLastRowNum()];
			for(int i = 1; i <= sheet.getLastRowNum(); i++){
				Row row = sheet.getRow(i);
				if (row != null) {
	            	List<Object> list = new ArrayList<>();
	            	for(int j = 0; j < row.getPhysicalNumberOfCells(); j++){
	            		list.add(cell(row.getCell(j)));
	            	}
	            	data[i-1] = list;
	            }
			}
			
		} catch (Exception e) {
		} finally {
			if (wb != null){
				wb.close();
			}
		}
		return data;
	}
	
	private static Object cell(Cell cell) {
		Object o = null;
		if(cell == null){
			return "";
		}
		CellType cellType = cell.getCellTypeEnum();
		switch (cellType) {
		case STRING:
			o = cell.getStringCellValue();
			break;
		case NUMERIC:
			o = cell.getNumericCellValue();
			break;
		case BOOLEAN:
			o = cell.getBooleanCellValue();
			break;
		case FORMULA:
			o = cell.getArrayFormulaRange();
			break;
		case ERROR:
			o = cell.getErrorCellValue();
			break;
		default:
			break;
		}
		return o;
	}
}

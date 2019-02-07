package com.demos.java.poidemo.excel;

import com.demos.java.poidemo.util.AsposeUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/7
 */
public class PdfUtil {

    public static final String FILE_TEMP_PATH = "/Users/xishang/temp/pdf/";

    public static String excel2Pdf(List<String> excelFiles) throws Exception {
        String filePrefix = UUID.randomUUID().toString().replace("-", "");
        String[] files = new String[excelFiles.size()];
        int fileIndex = 0;
        for (String excelFile : excelFiles) {
            String pdfFile = FILE_TEMP_PATH + filePrefix + "_" + fileIndex + ".pdf";
            AsposeUtils.excel2pdf(excelFile, pdfFile);
            files[fileIndex++] = pdfFile;
        }
        String mergeFile = FILE_TEMP_PATH + filePrefix + ".pdf";
        mergePdfFiles(files, mergeFile);
        return mergeFile;
    }

    public static boolean mergePdfFiles(String[] files, String outputFile) {
        boolean retValue = false;
        Document document = null;
        try {
            document = new Document(new PdfReader(files[0]).getPageSize(1));
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputFile));
            document.open();
            for (int i = 0; i < files.length; i++) {
                PdfReader reader = new PdfReader(files[i]);
                int n = reader.getNumberOfPages();
                for (int j = 1; j <= n; j++) {
                    document.newPage();
                    PdfImportedPage page = copy.getImportedPage(reader, j);
                    copy.addPage(page);
                }
            }
            retValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
        return retValue;
    }

}

package com.bdaim.common.util.excel;

import com.alibaba.excel.event.WriteHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.util.Map;

public class ExcelAfterWriteHandlerImpl implements WriteHandler {

    CellStyle cellStyle;

    private Map<String, Integer> sheetMergeIndex;

    private Map<String, String> sheetMergeName;

    public ExcelAfterWriteHandlerImpl() {
    }

    public ExcelAfterWriteHandlerImpl(Map<String, Integer> sheetMergeIndex, Map<String, String> sheetMergeName) {
        this.sheetMergeIndex = sheetMergeIndex;
        this.sheetMergeName = sheetMergeName;
    }

    @Override
    public void sheet(int sheetNo, Sheet sheet) {
        Workbook workbook = sheet.getWorkbook();
        //创建样式
        cellStyle = workbook.createCellStyle();
        //设置是否锁
        cellStyle.setLocked(false);
    }

    @Override
    public void row(int rowNum, Row row) {
    }

    @Override
    public void cell(int cellNum, Cell cell) {
        Workbook workbook = cell.getSheet().getWorkbook();
        Sheet currentSheet = cell.getSheet();
        if(sheetMergeName.get(currentSheet.getSheetName()).equals(cell.getStringCellValue())){
            // 合并单元格：参数：起始行, 终止行, 起始列, 终止列
            CellRangeAddress cra = new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), 0, sheetMergeIndex.get(currentSheet.getSheetName()));
            currentSheet.addMergedRegion(cra);
            //注意：边框样式需要重新设置一下
            RegionUtil.setBorderTop(BorderStyle.THIN, cra, currentSheet);

            Font font = workbook.createFont();
            //设置字体大小
            font.setFontHeightInPoints((short)11);
            //字体加粗
            font.setBold(true);

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            style.setFont(font);
            //设置自动换行;
            style.setWrapText(false);
            //设置水平对齐的样式为居中对齐;
            style.setAlignment(HorizontalAlignment.CENTER);
            //设置垂直对齐的样式为居中对齐;
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setFillBackgroundColor(IndexedColors.LIME.index);
            cell.setCellStyle(style);
        }
    }
}

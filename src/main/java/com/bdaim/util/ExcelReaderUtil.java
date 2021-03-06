package com.bdaim.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 读取excel文件的工具类
 * @auther: Chacker
 * @date: 2019/7/31 17:37
 */
public class ExcelReaderUtil {
    private static Logger logger = LoggerFactory.getLogger(ExcelReaderUtil.class);
    /**
     * 根据suffix(文件后缀名不同)读取excel文件
     *
     * @param path excel的文件路径
     * @return
     * @auther Chacker
     * @date 2019/7/31 17:38
     */
    public static List<List<String>> readExcel(String path) {
        String suffix = path.substring(path.lastIndexOf("."));
        List<List<String>> lists = new ArrayList<List<String>>();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            //获取工作簿
            Workbook workbook;
            if (Constant.XLS.equals(suffix)) {
                workbook = new HSSFWorkbook(inputStream);
            } else if (Constant.XLSX.equals(suffix)) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                return lists;
            }

            //读取第一个工作页 sheet1
            Sheet sheet = workbook.getSheetAt(0);
            //第一行为标题
            for (Row row : sheet) {
                ArrayList<String> list = new ArrayList<String>();
                for (Cell cell : row) {
                    //根据不同类型转化成字符串 此步必须有，否则会抛出异常
                    cell.setCellType(CellType.STRING);
                    list.add(cell.getStringCellValue());
                }
                lists.add(list);
            }
            logger.info("读取excel成功，条数为"+lists.size());
        } catch (Exception e) {
            logger.info(e.getMessage());
            logger.info("读取excel失败");
            e.printStackTrace();
        } finally {
            //关闭IO流
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lists;
    }
}

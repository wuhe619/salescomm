package com.bdaim.common.util;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019/3/15
 * @description
 */
public class ExcelUtil {

    /**
     * 获取excel表头
     *
     * @param in
     * @param sheet
     * @param trim
     * @return
     */
    public static Object readHeaders(InputStream in, Sheet sheet, boolean trim) {
        // 如果输入流不支持mark/reset，需要对其进行包裹
        if (!in.markSupported()) {
            in = FileMagic.prepareToCheckMagic(in);
        }
        final List<Object> rows = new ArrayList<>();
        new ExcelReader(in, null, new AnalysisEventListener() {
            @Override
            public void invoke(Object object, AnalysisContext context) {
                if (context.getCurrentRowNum() == 0) {
                    rows.add(object);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }, trim).read(sheet);
        if (rows.size() > 0) {
            return rows.get(0);
        }
        return null;
    }

    /**
     * 获取excel行数
     *
     * @param in
     * @param sheet
     * @param trim
     * @return
     */
    public static int readRowCount(InputStream in, Sheet sheet, boolean trim) {
        // 如果输入流不支持mark/reset，需要对其进行包裹
        if (!in.markSupported()) {
            in = FileMagic.prepareToCheckMagic(in);
        }
        final Map<String, Integer> rows = new HashMap<>();
        new ExcelReader(in, null, new AnalysisEventListener() {
            @Override
            public void invoke(Object object, AnalysisContext context) {
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                rows.put("count", context.getTotalCount());
            }
        }, trim).read(sheet);
        return rows.get("count");
    }

    /**
     * 获取excel全部内容
     *
     * @param in
     * @param sheet
     * @param trim
     * @return
     */
    public static List<Object> readExcel(InputStream in, Sheet sheet, boolean trim) {
        // 如果输入流不支持mark/reset，需要对其进行包裹
        if (!in.markSupported()) {
            in = FileMagic.prepareToCheckMagic(in);
        }
        final List<Object> rows = new ArrayList<>();
        new ExcelReader(in, null, new AnalysisEventListener() {
            @Override
            public void invoke(Object object, AnalysisContext context) {
                rows.add(object);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }, trim).read(sheet);
        return rows;
    }

    /**
     * 读取excel
     *
     * @param in            文件输入流
     * @param customContent 自定义模型可以在
     *                      {@link AnalysisEventListener#invoke(Object, AnalysisContext)}
     *                      AnalysisContext中获取用于监听者回调使用
     * @param eventListener 用户监听
     * @throws IOException
     * @throws EmptyFileException
     * @throws InvalidFormatException
     */
    public static ExcelReader getExcelReader(InputStream in, Object customContent,
                                             AnalysisEventListener<?> eventListener) throws EmptyFileException, InvalidFormatException {
        // 如果输入流不支持mark/reset，需要对其进行包裹
        if (!in.markSupported()) {
            in = FileMagic.prepareToCheckMagic(in);
        }
        // 获取excel类型
        ExcelTypeEnum excelTypeEnum = ExcelTypeEnum.valueOf(in);
        if (excelTypeEnum != null) {
            return new ExcelReader(in, customContent, eventListener);
        }
        throw new InvalidFormatException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");

    }

    /**
     * 读取excel
     *
     * @param in            文件输入流
     * @param customContent 自定义模型可以在
     *                      {@link AnalysisEventListener#invoke(Object, AnalysisContext) }
     *                      AnalysisContext中获取用于监听者回调使用
     * @param eventListener 用户监听
     * @param trim          是否对解析的String做trim()默认true,用于防止 excel中空格引起的装换报错。
     * @throws IOException
     * @throws EmptyFileException
     * @throws InvalidFormatException
     */
    public static ExcelReader getExcelReader(InputStream in, Object customContent,
                                             AnalysisEventListener<?> eventListener, boolean trim)
            throws EmptyFileException, InvalidFormatException {
        // 如果输入流不支持mark/reset，需要对其进行包裹
        if (!in.markSupported()) {
            in = FileMagic.prepareToCheckMagic(in);
        }
        // 获取excel类型
        ExcelTypeEnum excelTypeEnum = ExcelTypeEnum.valueOf(in);
        if (excelTypeEnum != null) {
            return new ExcelReader(in, customContent, eventListener, trim);
        }
        throw new InvalidFormatException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
    }
}
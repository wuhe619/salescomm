package com.bdaim.common.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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


    /**
     * 根据模板导出
     * @param list
     * @param templateName
     * @param response
     * @throws IOException
     * @throws IllegalAccessException
     */
    public static void exportExcelByList(List list, String templateName, HttpServletResponse response) throws IOException, IllegalAccessException {
        // 加载模板
        TemplateExportParams params = new TemplateExportParams(templateName);
        // 生成workbook 并导出
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        /*File savefile = new File("C:/Users/Administrator/Desktop/");
        if (!savefile.exists()) {
            boolean result = savefile.mkdirs();
            System.out.println("目录不存在,进行创建,创建" + (result ? "成功!" : "失败！"));
        }*/
        //FileOutputStream fos = new FileOutputStream("C:/Users/Administrator/Desktop/理货.xlsx");
        workbook.write(response.getOutputStream());
        response.getOutputStream().close();
    }

    private static List<Map<String, Object>> objectToMap(List obj) throws IllegalAccessException {
        List<Map<String, Object>> list = new ArrayList<>(16);
        for (Object o : obj) {
            Map<String, Object> map = new HashMap<>();
            Class<?> clazz = o.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                /*
                 * Returns the value of the field represented by this {@code Field}, on the
                 * specified object. The value is automatically wrapped in an object if it
                 * has a primitive type.
                 * 注:返回对象该该属性的属性值，如果该属性的基本类型，那么自动转换为包装类
                 */
                Object value = field.get(o);
                map.put(fieldName, value);
            }
            list.add(map);
        }

        return list;
    }
}
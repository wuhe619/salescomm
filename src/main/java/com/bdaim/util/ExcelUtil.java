package com.bdaim.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.poi.EmptyFileException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2019/3/15
 * @description
 */
public class ExcelUtil {
    private final static Logger LOG = LoggerFactory.getLogger(ExcelUtil.class);

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
     *
     * @param list
     * @param templateName
     * @param response
     * @throws IOException
     * @throws IllegalAccessException
     */
    public static void exportExcelByList(List list, String templateName, HttpServletResponse response) throws IOException, IllegalAccessException {
        //String classPath = new FileUrlEntity().getFileUrl();
        String classPath = PropertiesUtil.getStringValue("file.file_path");
        String pathF = File.separator;
        classPath = classPath.replace("/", pathF);
        String templatePath = classPath + pathF + "tp" + pathF + templateName + ".xlsx";
        LOG.info("文件路径是：" + templatePath);
        TemplateExportParams params = new TemplateExportParams(templatePath);
        // 生成workbook 并导出
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        response.setHeader("Content-Disposition", "attachment; filename=" + System.currentTimeMillis() + ExcelTypeEnum.XLSX.getValue());
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        workbook.write(response.getOutputStream());
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

    /**
     * 读取excel按照sheet分组返回
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Map readExcel(MultipartFile file) throws IOException {
        Workbook workBook = getWorkBook(file);
        // 循环工作表Sheet
        Map data = new HashMap();
        List sheetData;
        Map row;
        for (int numSheet = 0; numSheet < workBook.getNumberOfSheets(); numSheet++) {
            sheetData = new ArrayList();
            Map head = new HashMap();
            List headers = (List) ExcelUtil.readHeaders(file.getInputStream(), new Sheet(numSheet + 1), true);
            for (int i = 0; i < headers.size(); i++) {
                head.put(i, headers.get(i));
            }
            List<Object> excelData = ExcelUtil.readExcel(file.getInputStream(), new Sheet(numSheet + 1), true);
            for (int i = 1; i < excelData.size(); i++) {
                List list = (List) excelData.get(i);
                row = new HashMap();
                for (Object value : head.values()) {
                    row.put(value, "");
                }
                boolean success = false;
                for (int j = 0; j < list.size(); j++) {
                    if (StringUtil.isNotEmpty(String.valueOf(list.get(j)))) {
                        success = true;
                    }
                    if (String.valueOf(head.get(j)).endsWith("日期") && StringUtil.isNotEmpty(String.valueOf(list.get(j)))) {
                        row.put(head.get(j), getPOIDate(false, NumberConvertUtil.parseDouble(String.valueOf(list.get(j)))));
                    } else {
                        row.put(head.get(j), list.get(j));
                    }
                }
                if (success) {
                    sheetData.add(row);
                }
            }
            data.put("sheet" + (numSheet + 1), sheetData);
        }
        return data;
    }

    public static String getPOIDate(boolean use1904windowing, double value) {
        int wholeDays = (int) Math.floor(value);
        int millisecondsInDay = (int) ((value - (double) wholeDays) * 8.64E7D + 0.5D);
        Calendar calendar = new GregorianCalendar();
        short startYear = 1900;
        byte dayAdjust = -1;
        if (use1904windowing) {
            startYear = 1904;
            dayAdjust = 1;
        } else if (wholeDays < 61) {
            dayAdjust = 0;
        }
        calendar.set(startYear, 0, wholeDays + dayAdjust, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, millisecondsInDay);
        if (calendar.get(Calendar.MILLISECOND) == 0) {
            calendar.clear(Calendar.MILLISECOND);
        }
        Date date = calendar.getTime();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        return s.format(date);
    }

    /**
     * 得到Workbook对象
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Workbook getWorkBook(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        Workbook hssfWorkbook = null;
        try {
            hssfWorkbook = new HSSFWorkbook(is);
        } catch (Exception ex) {
            is = new FileInputStream(file);
            hssfWorkbook = new XSSFWorkbook(is);
        }
        return hssfWorkbook;
    }

    /**
     * 得到Workbook对象
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Workbook getWorkBook(MultipartFile file) throws IOException {
        InputStream is = file.getInputStream();
        Workbook hssfWorkbook = null;
        try {
            hssfWorkbook = new HSSFWorkbook(is);
        } catch (Exception ex) {
            is = file.getInputStream();
            hssfWorkbook = new XSSFWorkbook(is);
        }
        return hssfWorkbook;
    }
}
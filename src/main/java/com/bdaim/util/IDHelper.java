package com.bdaim.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
public class IDHelper {
    private static List<Long> cache=new Vector<Long>();
    private static SimpleDateFormat dataFormat=new SimpleDateFormat("yyMMddhhmmss");
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final AtomicInteger atomicInteger = new AtomicInteger(1000000);
    private static int i=0;
    private static int maxI=9999;
    private static int maxJ=99999;
    private static int maxK=999999;
    private static int maxG=999999;
    private static Date curDate=new Date();

    public static synchronized Long getID(){
        curDate.setTime(System.currentTimeMillis());
        if (i>=maxI)i=0;
        Long id=Long.valueOf(dataFormat.format(curDate)+String.format("%1$04d",i++));
        return id;
    }

    public static synchronized Long getUserID(){
        curDate.setTime(System.currentTimeMillis());
        if (i>=maxJ)i=0;
        Long id=Long.valueOf(dataFormat.format(curDate)+String.format("%1$05d",i++));
        return id;
    }

    public static synchronized Long getTransactionId(){
        curDate.setTime(System.currentTimeMillis());
        if (i>=maxK)i=0;
        Long id=Long.valueOf(dataFormat.format(curDate)+String.format("%1$06d",i++));
        return id;
    }

    public static synchronized String getOrderNoByAtomic(String no) {
        atomicInteger.getAndIncrement();
        int i = atomicInteger.get();
        String date = simpleDateFormat.format(new Date());
        return no + date + i;
    }

    public static synchronized Long getTouchId(){
        curDate.setTime(System.currentTimeMillis());
        if (i>=maxG)i=0;
        Long id=Long.valueOf(dataFormat.format(curDate)+String.format("%1$06d",i++));
        return id;
    }



}

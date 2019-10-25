package com.bdaim.callcenter.common;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.util.ConstantsUtil;
import com.bdaim.util.StringUtil;
import com.bdaim.util.http.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author chengning@salescomm.net
 * @date 2018/10/18
 * @description
 */
public class PhoneAreaUtil {

    private static Logger log = LoggerFactory.getLogger(PhoneAreaUtil.class);

    public static String getPhoneAttributionArea(String phone) {
        String area = "";
        if (StringUtil.isNotEmpty(phone)) {
            final String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?query=" + phone + "&co=&resource_id=6004&t=1539178164929&ie=utf8&oe=gbk&cb=op_aladdin_callback&format=json&tn=baidu&cb=jQuery1102004486019281905329_1539178090338&_=1539178090341";
            String result = null;
            try {
                result = HttpUtil.httpGet(url, new HashMap<>(), new HashMap<>());
            } catch (Exception e) {
                log.error("获取手机号归属地请求失败,再次重试", e);
                result = HttpUtil.httpGet(url, new HashMap<>(), new HashMap<>());
            }
            //log.info("通过http获取归属地返回结果:" + result);
            if (StringUtil.isNotEmpty(result)) {
                try {
                    result = result.substring(result.indexOf("(") + 1, result.lastIndexOf(")"));
                    //log.info("解析后的返回结果:" + result);
                    JSONObject jsonObject = JSON.parseObject(result);
                    if (jsonObject != null) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        if (jsonArray.size() > 0) {
                            area = jsonArray.getJSONObject(0).getString("prov") + jsonArray.getJSONObject(0).getString("city");
                        }
                    }

                } catch (Exception e) {
                    area = "";
                    log.error("解析返回结果出错,", e);
                }
            }
            return area;
        } else {
            log.warn("获取手机号归属地手机号为空phone:" + phone);
            return "手机号为空";
        }
    }


    public static String replacePhone(Object phone) {
        if (phone == null) {
            return null;
        }
        String result = null;
        if (String.valueOf(phone).contains(ConstantsUtil.BLACK_PHONE_PREFIX)) {
            result = String.valueOf(phone).replace(ConstantsUtil.BLACK_PHONE_PREFIX, "");
        } else {
            result = String.valueOf(phone);
        }
        return result;
    }

    class PhoneAttributionThread implements Runnable {
        private Thread t;
        private String threadName;
        private List<String> phones;
        private CountDownLatch countDown;
        private Map<String, String> areaMap;

        public PhoneAttributionThread(String threadName, List<String> phones, CountDownLatch countDown, Map<String, String> areaMap) {
            this.threadName = threadName;
            this.phones = phones;
            this.countDown = countDown;
            this.areaMap = areaMap;
        }

        @Override
        public void run() {
            if (phones != null && phones.size() > 0) {
                String area;
                for (int i = 0; i < phones.size(); i++) {
                    area = getPhoneAttributionArea(phones.get(i));
                    while (StringUtil.isEmpty(area)) {
                        area = getPhoneAttributionArea(phones.get(i));
                    }
                    areaMap.put(phones.get(i), area);
                }
                countDown.countDown();
            }
        }

        public void start() {
            System.out.println("Starting " + threadName);
            if (t == null) {
                t = new Thread(this, threadName);
                t.start();
            }
        }
    }

    public static void main1(String[] args) throws Exception {
        File file = new File("C:\\Users\\Administrator\\Desktop\\家长手机号.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        String area;
        List<String> phones = new ArrayList<>();
        long start = System.currentTimeMillis();
        List<String> singleList = new CopyOnWriteArrayList<>();
        Map<String, String> areaMap = new ConcurrentHashMap<>();

        while ((line = bufferedReader.readLine()) != null) {
            phones.add(line);
           /* area = getPhoneAttributionArea(line);
            while (StringUtil.isEmpty(area)) {
                area = getPhoneAttributionArea(line);
            }
            singleList.add(area);*/
            //System.out.println(area);
        }
        System.out.println("普通模式耗时:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        int size = 100;
        final CountDownLatch countDown = new CountDownLatch((phones.size() / size) + 1);
        for (int i = 0; i < phones.size(); i += size) {
            if (i + 100 > phones.size()) {
                size = phones.size() - i;
            }
            PhoneAreaUtil.PhoneAttributionThread R1 = new PhoneAreaUtil().new PhoneAttributionThread("thread-" + i, phones.subList(i, i + size), countDown, areaMap);
            R1.start();
        }
        try {
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("其他线程完成任务,主线程开始执行...");
        System.out.println("主线程任务完成，整个任务进度完成...");
        System.out.println("多线程模式耗时:" + (System.currentTimeMillis() - start));
        System.out.println("普通模式归属地大小:" + singleList.size());
        System.out.println("多线程模式归属地数据:" + areaMap.toString());
    }

    public static void main2(String[] args) throws Exception {
        File file = new File("C:\\Users\\Administrator\\Desktop\\417.2.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        String area;
        List<String> phones = new ArrayList<>();
        long start = System.currentTimeMillis();
        List<String> singleList = new CopyOnWriteArrayList<>();
        Map<String, String> areaMap = new ConcurrentHashMap<>();
        String updateSql = "";
        JSONObject jsonObject;
        JSONArray jsonArray;
        Map<String, String> data;
        int k = 0;
        while ((line = bufferedReader.readLine()) != null) {
            k++;
            //System.out.println(line.indexOf("\"{"));
            // {"touchId":"1811201054560024181126055856007905","remark":"","groupId":"425","cust_group_id":"425","superId":"691b0adf9bf10a417490e63dc481e35d","labelIds":[{"labelId":"1811040553380220","optionValue":"没孩子"},{"labelId":"1810300556533081","optionValue":"失败"}],"super_name":"","super_age":"","super_sex":null,"super_telphone":"","super_phone":"","super_address_province_city":"","super_address_street":""}
            jsonObject = JSON.parseObject(line);
            if (jsonObject != null) {
                if ("417".equals(jsonObject.getString("groupId"))) {
                    data = new HashMap<>();
                    jsonArray = jsonObject.getJSONArray("labelIds");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        data.put(jsonArray.getJSONObject(i).getString("labelId"), jsonArray.getJSONObject(i).getString("optionValue"));
                    }
                    updateSql += "UPDATE t_customer_group_list_" + jsonObject.getString("groupId") + " SET `super_data`='" + JSON.toJSONString(data) + "' WHERE `id`='" + jsonObject.getString("superId") + "';\r\n";
                }

            }

        }
        System.out.println(k);
        System.out.println(updateSql);

    }

    public static final String PROVINCE_NAME = "河北,山西,台湾,辽宁,吉林,黑龙江,江苏,浙江,安徽,福建,江西,山东,河南,湖北,湖南,广东,甘肃,四川,贵州,海南,云南,青海,陕西,广西,西藏,宁夏,新疆,内蒙,澳门,香港";

    public static Set<String> PROVINCE_SET = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        if (PROVINCE_SET.size() == 0) {
            String[] provinceNames = PROVINCE_NAME.split(",");
            for (String p : provinceNames) {
                PROVINCE_SET.add(p);
            }
        }


        File file = new File("C:\\Users\\Administrator\\Desktop\\1.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line, area;
        while ((line = bufferedReader.readLine()) != null) {
            area = getPhoneAttributionArea(line);
            while (StringUtil.isEmpty(area)) {
                area = getPhoneAttributionArea(line);
            }
            if (StringUtil.isNotEmpty(area)) {
                for (String p : PROVINCE_SET) {
                    if (area.indexOf(p) >= 0) {
                        area = area.replaceAll(p, "");
                        break;
                    }
                }

            }
            System.out.println(area);
        }
    }
}

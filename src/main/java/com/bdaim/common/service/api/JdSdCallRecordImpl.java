package com.bdaim.common.service.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.PhoneService;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.util.DateUtil;
import com.bdaim.util.LogUtil;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询通话记录接口（京东-尚德）
 *
 * @author chengning@salescomm.net
 * @date 2019/2/14 16:27
 */
@Service("JdSdCallRecordService")
@Transactional
public class JdSdCallRecordImpl {

    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private PhoneService phoneService;

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final static String CUST_ID = "1903120348469162";

    //private final static String DB_NAME = "label_dev";
    private final static String DB_NAME = " ";

    public String execute(HttpServletRequest request) {
        String time = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        JSONObject json = new JSONObject();
        //用户群id
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) {
            LogUtil.warn("客户" + CUST_ID + "查询通话记录id参数为空");
            json.put("code", -2);
            json.put("message", "id必传");
            return json.toJSONString();
        }

        //坐席账号
        String account = request.getParameter("account");
        //外呼状态
        String callStatus = request.getParameter("callStatus");
        //查询开始时间
        String startTime = request.getParameter("startTime");
        //查询结束时间
        String endTime = request.getParameter("endTime");
        //页码
        String pageNum = request.getParameter("pageNum");
        //展示数
        String pageSize = request.getParameter("pageSize");

        // 检查客户群权限
        String querySql = "SELECT * FROM " + DB_NAME + " customer_group WHERE cust_id =? AND id = ?";
        List cGroup = marketResourceDao.sqlQuery(querySql, CUST_ID, id);
        if (cGroup == null || (cGroup != null && cGroup.size() == 0)) {
            json.put("code", -5);
            json.put("message", "操作异常");
            return json.toJSONString();
        }

        //校验页码参数
        Map<String, Integer> pageMap = pageCheck(pageNum, pageSize);
        int pNum = pageMap.get("pageNum");
        int pSize = pageMap.get("pageSize");

        //判断如果时间不是本月默认查询当月数据
        LocalDateTime lStarTime, lEndTime;
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            boolean flag = false;
            try {
                lStarTime = LocalDateTime.parse(startTime, DTF);
                lEndTime = LocalDateTime.parse(endTime, DTF);
                flag = DateUtil.isSameMonth(lStarTime, lEndTime);
                if (flag) {
                    //如果在相同月份查询当前月份  如果不在默认查询当前月
                    time = lStarTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
                }
            } catch (Exception e) {
                LogUtil.error("时间转换异常:", e);
                json.put("code", -3);
                json.put("message", "时间格式不正确");
                return json.toJSONString();
            }
        }
        //查询致电信息
        String userId = null;
        try {
            if (StringUtil.isNotEmpty(account)) {
                //查询userid
                List<Map<String, Object>> list = marketResourceDao.sqlQuery("SELECT id FROM t_customer_user WHERE account =? AND cust_id = ? ", account, CUST_ID);
                if (list.size() > 0) {
                    if (list.size() > 0) {
                        userId = String.valueOf(list.get(0).get("id"));
                    } else {
                        json.put("code", -2);
                        json.put("message", account + "账户不存在");
                        return json.toJSONString();
                    }
                }
            }
            List<Map<String, Object>> list = queryCallLog(id, userId, callStatus, startTime, endTime, pNum, pSize, time);
            json.put("total", list.size());
            json.put("data", list);
        } catch (Exception e) {
            LogUtil.error("查询触达记录信息异常,", e);
            json.put("code", -1);
            json.put("message", "查询失败");
        }
        return json.toJSONString();
    }

    public List<Map<String, Object>> queryCallLog(String id, String userId, String callStatus, String startTime, String endTime, int pageNum, int pageSize, String time) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT touch_id touchId,superid,u.account,v.create_time createTime,v.`status`,customer_group_id id,called_duration callTime");
        stringBuffer.append(" FROM t_touch_voice_log_" + time + " v  LEFT JOIN t_customer_user u ON v.user_id = u.id WHERE v.cust_id =? ");
        List<Object> p = new ArrayList<>();
        p.add(CUST_ID);
        if (StringUtil.isNotEmpty(id)) {
            p.add(id);
            stringBuffer.append(" AND v.customer_group_id =? ");
        }
        if (StringUtil.isNotEmpty(userId)) {
            p.add(userId);
            stringBuffer.append(" AND u.user_id =? ");
        }
        if (StringUtil.isNotEmpty(callStatus)) {
            p.add(callStatus);
            stringBuffer.append(" AND v.status = ? ");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            stringBuffer.append(" AND v.create_time BETWEEN ? and ?");
        }
        p.add(pageNum);
        p.add(pageSize);
        stringBuffer.append(" ORDER BY v.create_time DESC LIMIT ?, ? ");
        LogUtil.info("查询触达记录信息sql:" + stringBuffer.toString());
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(stringBuffer.toString(), p.toArray());
        List<Map<String, Object>> superMessage = null, labelList, queryName;
        Map<String, Object> labelMap, labelDataMap;
        String superData, customerGroupId, superId;
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                customerGroupId = String.valueOf(list.get(i).get("id"));
                superId = String.valueOf(list.get(i).get("superid"));
                if (StringUtil.isNotEmpty(customerGroupId) && StringUtil.isNotEmpty(superId)) {
                    //根据客户群id查询自建属性信息
                    superMessage = marketResourceDao.sqlQuery("SELECT super_data FROM t_customer_group_list_" + customerGroupId + " WHERE id =?", superId);
                    LogUtil.info("查询自建属性信息值:" + superMessage);
                    superData = String.valueOf(superMessage.get(0).get("super_data"));
                    if (StringUtil.isNotEmpty(superData)) {
                        labelList = new ArrayList<>();
                        //将自建属性转换为json对象
                        labelDataMap = JSON.parseObject(superData);
                        if (labelDataMap != null && labelDataMap.size() > 0) {
                            for (Map.Entry<String, Object> labelDataKey : labelDataMap.entrySet()) {
                                labelMap = new HashMap<>();
                                //根据labelId查询属性名
                                queryName = marketResourceDao.sqlQuery("SELECT label_name FROM t_customer_label WHERE label_id =?", labelDataKey.getKey());
                                if (queryName.size() > 0) {
                                    labelMap.put(String.valueOf(queryName.get(0).get("label_name")), labelDataKey.getValue());
                                    labelList.add(labelMap);
                                }
                            }
                        }
                        list.get(i).put("superData", labelList);
                    }
                }
                if (list.get(i) != null) {
                    list.get(i).put("phone", phoneService.getPhoneBySuperId(String.valueOf(list.get(i).get("superid"))));
                }
            }
        }

        return list;
    }

    public Map<String, Integer> pageCheck(String pageNum, String pageSize) {
        //默认pageSize最大为100
        HashMap<String, Integer> map = new HashMap<>();
        //核验页码和展示数
        int pNum = 0;
        int pSize = 20;
        if (StringUtil.isNotEmpty(pageNum)) {
            pNum = Integer.parseInt(pageNum);
            if (pNum < 0) {
                pNum = 0;
            }
        }
        if (StringUtil.isNotEmpty(pageSize)) {
            pSize = Integer.parseInt(pageSize);
            if (pSize > 100) {
                pSize = 100;
            }
            if (pSize < 1) {
                pSize = 1;
            }
        }
        map.put("pageNum", pNum);
        map.put("pageSize", pSize);
        return map;
    }

}

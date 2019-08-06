package com.bdaim.smscenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.batch.dto.ExpressLog;
import com.bdaim.batch.entity.SenderInfo;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.smscenter.dao.SendmessageImplDao;
import com.bdaim.smscenter.service.SendmessageService;
import com.github.crab2died.ExcelUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author wangxx@bdaim.com
 * @Description:
 * @date 2018/12/27 10:31
 */
@Service("SendmessageService")
@Transactional
public class SendmessageImpl implements SendmessageService {

    private final static Logger LOG = Logger.getLogger(SendmessageImpl.class);
    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    Logger logger = Logger.getLogger(SendmessageImpl.class);


    @Resource
    JdbcTemplate jdbcTemplate;

    @Resource
    SendmessageImplDao sendmessageImplDao;


    @Override
    public Map<Object, Object> sendadd(SenderInfo senderInfo, String compId) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        String id = getRandomString();
        try {
            if (senderInfo.getType() != null && senderInfo.getType() == 1) {
                StringBuilder cancelDefault = new StringBuilder("update t_sender_info set type=2 where cust_id=?");
                sendmessageImplDao.executeUpdateSQL(cancelDefault.toString(), compId);
                StringBuilder sqlBuilder = new StringBuilder("INSERT INTO t_sender_info(id,cust_id,sender_name,phone,province,city,district,address,postcodes,type,create_time) values(?,?,?,?,?,?,?,?,?,?,?)");
                sendmessageImplDao.executeUpdateSQL(sqlBuilder.toString(), id, compId, senderInfo.getSender_name(), senderInfo.getPhone(), senderInfo.getProvince(), senderInfo.getCity(), senderInfo.getDistrict(), senderInfo.getAddress(), senderInfo.getPostcodes(), senderInfo.getType(), new Timestamp(System.currentTimeMillis()));
            } else {
                StringBuilder sqlBuilder = new StringBuilder("INSERT INTO t_sender_info(id,cust_id,sender_name,phone,province,city,district,address,postcodes,type,create_time) values(?,?,?,?,?,?,?,?,?,?,?)");
                sendmessageImplDao.executeUpdateSQL(sqlBuilder.toString(), id, compId, senderInfo.getSender_name(), senderInfo.getPhone(), senderInfo.getProvince(), senderInfo.getCity(), senderInfo.getDistrict(), senderInfo.getAddress(), senderInfo.getPostcodes(), senderInfo.getType(), new Timestamp(System.currentTimeMillis()));
            }
            map.put("message", "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("message", "添加失败");
        }
        return map;
    }

    /**
     * 生成4位随机验证码
     */
    private static String getRandomString() {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    //查询
    @Override
    public Page sendlist(PageParam page, String compId) {

        StringBuilder sqlBuilder = new StringBuilder("SELECT t.id,t.sender_name,t.province,t.city,t.district,t.address,t.postcodes,t.phone,t.type FROM t_sender_info t WHERE 1=1");
        if (StringUtil.isNotEmpty(compId)) {
            sqlBuilder.append(" and t.cust_id =" + compId);
        }
        sqlBuilder.append(" order by t.create_time desc ");
        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
    }

    @Override
    public Map<Object, Object> senddelete(String id) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        try {
            StringBuilder sqlBuilder = new StringBuilder("DELETE FROM t_sender_info WHERE id=?");
            if (StringUtil.isNotEmpty(id)) {
                sendmessageImplDao.executeUpdateSQL(sqlBuilder.toString(), id);
            }
            map.put("message", "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("message", "删除失败");
        }
        return map;
    }

    @Override
    public Map<Object, Object> sendupdate(SenderInfo senderInfo) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        try {
            StringBuilder sqlBuilder = new StringBuilder("update t_sender_info set sender_name=?,phone=?,province=?,city=?,district=?,address=?,postcodes=?,type=?,create_time=? where id=?");
            sendmessageImplDao.executeUpdateSQL(sqlBuilder.toString(), senderInfo.getSender_name(), senderInfo.getPhone(), senderInfo.getProvince(), senderInfo.getCity(), senderInfo.getDistrict(), senderInfo.getAddress(), senderInfo.getPostcodes(), senderInfo.getType(), new Timestamp(System.currentTimeMillis()), senderInfo.getId());
            map.put("message", "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("message", "修改失败");
        }
        return map;


    }

    @Override
    public Map<Object, Object> defaultupdate(String id, String compId) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        try {
            StringBuilder sqlBuilder = new StringBuilder("update t_sender_info set type=2 where cust_id=?");
            sendmessageImplDao.executeUpdateSQL(sqlBuilder.toString(), compId);
            StringBuilder cancelDefault = new StringBuilder("update t_sender_info set type=1 where id=? and cust_id=?");
            sendmessageImplDao.executeUpdateSQL(cancelDefault.toString(), id, compId);
            map.put("message", "设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("message", "设置失败");
        }
        return map;
    }

    @Override
    public Page pageList(PageParam page, ExpressLog expressLog) {
        String batchid = expressLog.getBatchid();
        StringBuilder sqlBuilder = new StringBuilder("SELECT touch_id, address_id, receive_name,create_time,STATUS \n" +
                "FROM t_touch_express_log WHERE batch_id= " + batchid);
        if (StringUtil.isNotEmpty(expressLog.getTouch_id())) {
            sqlBuilder.append(" AND touch_id=" + expressLog.getTouch_id());
        }
        if (StringUtil.isNotEmpty(expressLog.getAddressid())) {
            sqlBuilder.append(" AND address_id = " + expressLog.getAddressid());
        }

        if (StringUtil.isNotEmpty(expressLog.getFirstdelivery())) {
            sqlBuilder.append(" AND create_time >= '" + expressLog.getFirstdelivery() + "'");
        }
        if (StringUtil.isNotEmpty(expressLog.getLastsendtime())) {
            sqlBuilder.append(" AND create_time <= '" + expressLog.getLastsendtime() + "'");
        }
        if (expressLog.getStatus() != null) {
            sqlBuilder.append(" AND STATUS = " + expressLog.getStatus());
        }
        sqlBuilder.append(" ORDER BY create_time DESC");

        Page result = new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);

        return result;

    }

    @Override
    public Object exportExportRecords(ExpressLog expressLog, HttpServletResponse response) {

        Map<String, Object> resultMap = new HashMap<>();
        String batchid = expressLog.getBatchid();
        try {
            StringBuffer ob = new StringBuffer();

            ob.append(
                    "  SELECT touch_id, address_id, receive_name,create_time,STATUS  ")
                    .append(" FROM t_touch_express_log WHERE batch_id= " + batchid);

            if (StringUtil.isNotEmpty(expressLog.getTouch_id())) {
                ob.append(" AND touch_id=" + expressLog.getTouch_id());
            }
            if (StringUtil.isNotEmpty(expressLog.getAddressid())) {
                ob.append(" AND address_id = " + expressLog.getAddressid());
            }

            if (StringUtil.isNotEmpty(expressLog.getFirstdelivery())) {
                ob.append(" AND create_time >= '" + expressLog.getFirstdelivery() + "'");
            }
            if (StringUtil.isNotEmpty(expressLog.getLastsendtime())) {
                ob.append(" AND create_time <= '" + expressLog.getLastsendtime() + "'");
            }
            if (expressLog.getStatus() != null) {
                ob.append(" AND STATUS = " + expressLog.getStatus());
            }
            ob.append(" ORDER BY create_time DESC");
            LOG.info("快递记录sql:\t" + ob.toString());

            List<Map<String, Object>> billlist = jdbcTemplate.queryForList(ob.toString());
            List<List<Object>> data = new ArrayList<>();
            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("快递ID");//1
            titles.add("地址ID");
            titles.add("姓名");
            titles.add("发送时间");//6
            titles.add("快递状态");
            String fileName = "快递记录" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            String fileType = ".xlsx";

            List<Object> rowList;
            for (Map<String, Object> column : billlist) {
                rowList = new ArrayList<>();
                if (column.get("type") != null) {
                    rowList.add(TransactionEnum.getName(Integer.parseInt(String.valueOf(column.get("type")))));
                }
                rowList.add(column.get("touch_id") != null ? column.get("touch_id") : "");
                rowList.add(column.get("address_id") != null ? column.get("address_id") : "");
                rowList.add(column.get("receive_name") != null ? column.get("receive_name") : "");
                rowList.add(column.get("create_time") != null ? column.get("create_time") : "");
                if ("1".equals(String.valueOf(column.get("status")))) {
                    rowList.add("等待发送");
                }
                if ("2".equals(String.valueOf(column.get("status")))) {
                    rowList.add("接单");
                }
                if ("3".equals(String.valueOf(column.get("status")))) {
                    rowList.add("运输中");
                }
                if ("4".equals(String.valueOf(column.get("status")))) {
                    rowList.add("签收");
                }
                if ("5".equals(String.valueOf(column.get("status")))) {
                    rowList.add("拒签");
                }
                if ("6".equals(String.valueOf(column.get("status")))) {
                    rowList.add("发送失败");
                }
                if ("7".equals(String.valueOf(column.get("status")))) {
                    rowList.add("撤销发送");
                }
                data.add(rowList);
            }
            if (data.size() > 0) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                OutputStream outputStream = null;
                outputStream = response.getOutputStream();
                ExcelUtils.getInstance().exportObjects2Excel(data, titles, outputStream);
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
                logger.info("快递记录导出成功");
                resultMap.put("code", "000");
                resultMap.put("_message", "快递记录导出成功！");
            } else {
                resultMap.put("code", "001");
                resultMap.put("_message", "快递记录无数据导出！");
                return JSON.toJSONString(resultMap);
            }
        } catch (Exception e) {
            logger.error("快递记录导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "快递记录导出失败！");
        }
        return JSON.toJSONString(resultMap);

    }


    @Override
    public Object repairDetailsderive(String batchid, String name, String phone, String touch_id, Integer status, Integer status1, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            StringBuffer ob = new StringBuffer();

            ob.append(
                    "  SELECT\n" +
                            "\tnl.name,nl.phoneId,nl.id_card,nl.status,n.success_num,nl.channel,t.touch_id,nl.site,COUNT(t.touch_id) AS su,t.status as status1\n" +
                            "FROM\n" +
                            "nl_batch_detail nl\n" +
                            "LEFT JOIN t_touch_express_log t ON nl.id = t.address_id\n" +
                            "LEFT JOIN nl_batch n ON t.batch_id = n.id \n" +
                            "WHERE\n" +
                            "\tnl.batch_id =" + batchid);
            if (StringUtil.isNotEmpty(name)) {
                ob.append(" AND nl.name=" + name);
            }
            if (StringUtil.isNotEmpty(phone)) {
                ob.append(" AND nl.phoneId = " + phone);
            }
            if (StringUtil.isNotEmpty(touch_id)) {
                ob.append(" AND t.touch_id = " + touch_id);
            }
            if (status != null) {
                ob.append(" AND nl.status = " + status);
            }
            if (status1 != null) {
                ob.append(" AND t.status = " + status1);
            }
            LOG.info("修复详情sql:\t" + ob.toString());

            List<Map<String, Object>> billlist = jdbcTemplate.queryForList(ob.toString());
            List<List<Object>> data = new ArrayList<>();
            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("姓名");//1
            titles.add("手机号");
            titles.add("身份证号");
            titles.add("修复状态");//6
            titles.add("修复数量");
            titles.add("渠道");
            titles.add("地址ID");
            titles.add("地址");
            titles.add("提交次数");
            titles.add("快递状态");

            String fileName = "修复详情" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            String fileType = ".xlsx";

            List<Object> rowList;
            for (Map<String, Object> column : billlist) {
                rowList = new ArrayList<>();
                if (column.get("type") != null) {
                    rowList.add(TransactionEnum.getName(Integer.parseInt(String.valueOf(column.get("type")))));
                }
                rowList.add(column.get("name") != null ? column.get("name") : "");
                rowList.add(column.get("phone") != null ? column.get("phone") : "");
                rowList.add(column.get("id_card") != null ? column.get("id_card") : "");
                if ("0".equals(String.valueOf(column.get("status")))) {
                    rowList.add("失败");
                }
                if ("1".equals(String.valueOf(column.get("status")))) {
                    rowList.add("成功");
                }
                rowList.add(column.get("success_num") != null ? column.get("success_num") : "");
                if ("5".equals(String.valueOf(column.get("channel")))) {
                    rowList.add("京东");
                }

                rowList.add(column.get("touch_id") != null ? column.get("touch_id") : "");
                rowList.add(column.get("site") != null ? column.get("site") : "");
                rowList.add(column.get("su") != null ? column.get("su") : "");

                if ("1".equals(String.valueOf(column.get("status1")))) {
                    rowList.add("等待发送");
                }
                if ("2".equals(String.valueOf(column.get("status1")))) {
                    rowList.add("接单");
                }
                if ("3".equals(String.valueOf(column.get("status1")))) {
                    rowList.add("运输中");
                }
                if ("4".equals(String.valueOf(column.get("status1")))) {
                    rowList.add("签收");
                }
                if ("5".equals(String.valueOf(column.get("status1")))) {
                    rowList.add("拒签");
                }
                if ("6".equals(String.valueOf(column.get("status1")))) {
                    rowList.add("发送失败");
                }
                if ("7".equals(String.valueOf(column.get("status1")))) {
                    rowList.add("撤销发送");
                }
                data.add(rowList);
            }
            if (data.size() > 0) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                OutputStream outputStream = null;
                outputStream = response.getOutputStream();
                ExcelUtils.getInstance().exportObjects2Excel(data, titles, outputStream);
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
                logger.info("快递记录导出成功");
                resultMap.put("code", "000");
                resultMap.put("_message", "快递记录导出成功！");
            } else {
                resultMap.put("code", "001");
                resultMap.put("_message", "快递记录无数据导出！");
                return JSON.toJSONString(resultMap);
            }
        } catch (Exception e) {
            logger.error("快递记录导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "快递记录导出失败！");
        }
        return JSON.toJSONString(resultMap);
    }

    @Override
    public List<Map<String, Object>> repairDetails(Integer pageNum, Integer pageSize, String batchid, String name, String phone, String addressId, Integer repairStatus, Integer expressStatus) {
        StringBuffer querySql = new StringBuffer("SELECT nl.name,u.phone,nl.id_card,nl.status\n" +
                " FROM nl_batch_detail nl\n" +
                " LEFT JOIN u ON nl.phoneId=u.id\n" +
                " LEFT JOIN t_touch_express_log tl ON nl.id=tl.touch_id\n" +
                " WHERE nl.batch_id=?");
        if (StringUtil.isNotEmpty(name) && !"null".equals(name)) {
            querySql.append(" AND nl.name= " + name);
        }
        if (StringUtil.isNotEmpty(phone) && !"null".equals(phone)) {
            querySql.append(" AND u.phone= " + phone);
        }
        if (StringUtil.isNotEmpty(addressId) && !"null".equals(addressId)) {
            querySql.append(" AND nl.id= " + addressId);
        }
        if (repairStatus != null) {
            querySql.append(" AND nl.status = " + repairStatus);
        }
        if (expressStatus != null) {
            querySql.append(" AND tl.status =" + expressStatus);
        }
        querySql.append(" GROUP BY nl.id_card  LIMIT " + (pageNum - 1) + "," + pageSize);
        List<Map<String, Object>> list = sendmessageImplDao.sqlQuery(querySql.toString(), batchid);
        for (int j = 0; j < list.size(); j++) {
            String idCard = String.valueOf(list.get(j).get("id_card"));
            String querySqlq = "SELECT COUNT(id)successNum FROM nl_batch_detail WHERE batch_id =? AND STATUS =1 AND id IS NOT NULL AND id_card=?";
            List<Map<String, Object>> list1 = sendmessageImplDao.sqlQuery(querySqlq, batchid, idCard);
            list.get(j).put("successNum", list1.get(0).get("successNum"));
            String querySq = "SELECT express_path FROM nl_batch_detail WHERE batch_id =? AND id_card=?";
            List<Map<String, Object>> list3 = sendmessageImplDao.sqlQuery(querySq, batchid, idCard);
            for (int i = 0; i < list3.size(); i++) {
                String expressPath = String.valueOf(list3.get(0).get("express_path"));
                if (expressPath != null && !expressPath.equals("null")) {
                    String tupian1 = "/pic/";
                    StringBuilder path = new StringBuilder(tupian1).append(expressPath);
                    list.get(j).put("express_path", path);
                }
            }

        }
        if (list.size() > 0) {
            StringBuffer sql;
            List<Map<String, Object>> channelList;
            for (int i = 0; i < list.size(); i++) {
                sql = new StringBuffer();
                sql.append("SELECT t.supplier_id,nl.id,nl.site,tl.status,tl.address_id" +
                        " FROM nl_batch_detail nl " +
                        " LEFT JOIN t_market_resource t ON nl.resource_id=t.resource_id\n" +
                        " LEFT JOIN t_touch_express_log tl ON nl.id=tl.address_id\n" +
                        " WHERE nl.id_card=? AND t.type_code=12 AND nl.batch_id=?");
                if (StringUtil.isNotEmpty(addressId) && !"null".equals(addressId)) {
                    sql.append(" AND nl.id =" + addressId);
                }
                if (repairStatus != null) {
                    sql.append(" AND nl.status =" + repairStatus);
                }
                if (expressStatus != null) {
                    sql.append(" AND tl.status = " + expressStatus);
                }
                sql.append(" GROUP BY tl.address_id");
                channelList = sendmessageImplDao.sqlQuery(sql.toString(), list.get(i).get("id_card"), batchid);
                for (int j = 0; j < channelList.size(); j++) {
                    String addressIdl = String.valueOf(channelList.get(j).get("address_id"));
                    //拿到地址id查询最后一次发送记录状态
                    String queryStatusSql = "SELECT address_id,status  from t_touch_express_log WHERE batch_id = ? AND address_id  = ? ORDER BY create_time Desc";
                    List<Map<String, Object>> queryStatusList = sendmessageImplDao.sqlQuery(queryStatusSql, batchid, addressIdl);
                    int expreStatus = 0;
                    if (queryStatusList.size() > 0) {
                        String status = String.valueOf(queryStatusList.get(0).get("status"));
                        if (StringUtil.isNotEmpty(status)) {
                            expreStatus = Integer.parseInt(status);
                        }
                    }
                    channelList.get(j).put("status", expreStatus);
                    String querySqlq = "SELECT COUNT(touch_id) submission FROM `t_touch_express_log` WHERE address_id =? and batch_id=?";
                    List<Map<String, Object>> list2 = sendmessageImplDao.sqlQuery(querySqlq, addressIdl, batchid);
                    channelList.get(j).put("submission", list2.get(0).get("submission"));
                }
                list.get(i).put("channelList", channelList);
            }
        }
        return list;
    }

    @Override
    public void add(String fileName, String batch_id, String id_card) {

        StringBuilder sqlBuilder = new StringBuilder("SELECT express_path FROM nl_batch_detail WHERE batch_id= " + batch_id + " AND id_card= '" + id_card + "'");
        List<Map<String, Object>> express_path = jdbcTemplate.queryForList(sqlBuilder.toString());
        String op = express_path.toString();

        if (op != null) {

            StringBuilder sql = new StringBuilder("update nl_batch_detail set express_path=? where batch_id=? and id_card=?");
            sendmessageImplDao.executeUpdateSQL(sql.toString(), fileName, batch_id, id_card);


        } else {
            logger.warn("批次" + batch_id + "idCard" + id_card + "未查询到批次数据");

        }


    }

    @Override
    public Map<String, Object> submitCourier(String siteid, String companyid, String bachid) {
        Map<String, Object> resultMap = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT express_path FROM nl_batch_detail WHERE id= '" + siteid + "'");
        List<Map<String, Object>> express_path = jdbcTemplate.queryForList(sqlBuilder.toString());
        String file = express_path.toString();
        if (file == null) {
            resultMap.put("markedWords", "请先上传文件在进行提交");
        } else {

            StringBuilder sqlBuilderl = new StringBuilder("SELECT id,sender_name,phone,province,city,district,address,type FROM t_sender_info WHERE cust_id='" + companyid + "'");
            List<Map<String, Object>> express = jdbcTemplate.queryForList(sqlBuilderl.toString());
            resultMap.put("markedWord", express);
            StringBuilder sqlBuilde = new StringBuilder("SELECT express_path FROM nl_batch_detail WHERE id= '" + siteid + "' AND batch_id='" + bachid + "'");
            List<Map<String, Object>> tupian = sendmessageImplDao.sqlQuery(sqlBuilde.toString());

            for (int i = 0; i < tupian.size(); i++) {
                String expressPath = String.valueOf(tupian.get(0).get("express_path"));
                String tupian1 = "/pic/";
                StringBuilder path = new StringBuilder(tupian1).append(expressPath);
                resultMap.put("path", path);
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> express(String touch_id) {
        Map<String, Object> resultMap = new HashMap<>();
        StringBuilder sqlBuilderl = new StringBuilder("SELECT sender_message,file_path FROM t_touch_express_log WHERE touch_id=?");
        List<Map<String, Object>> express = jdbcTemplate.queryForList(sqlBuilderl.toString(), touch_id);
        String file_path = String.valueOf(express.get(0).get("file_path"));
        com.alibaba.fastjson.JSONObject json = JSON.parseObject(String.valueOf(express.get(0).get("sender_message")));
        resultMap.put("address", json.getString("address"));
        resultMap.put("province", json.getString("province"));
        resultMap.put("phone", json.getString("phone"));
        resultMap.put("city", json.getString("city"));
        resultMap.put("district", json.getString("district"));
        resultMap.put("sender_name", json.getString("sender_name"));
        resultMap.put("postcodes", json.getString("postcodes"));
        resultMap.put("file_path", file_path);
        return resultMap;
    }

    @Override
    public Map<String, Object> time(String batchid) {
        Map<String, Object> resultMap = new HashMap<>();
        StringBuffer thisTime = new StringBuffer("SELECT MAX(create_time) as atlatest,MIN(create_time) as initialmortgage FROM t_touch_express_log WHERE batch_id=?");
        List<Map<String, Object>> ThisTime = sendmessageImplDao.sqlQuery(thisTime.toString(), batchid);
        for (int i = 0; i < ThisTime.size(); i++) {

            String atlatest = String.valueOf(ThisTime.get(i).get("atlatest"));
            String atlatest1 = atlatest.substring(0, atlatest.length() - 2);
            String initialmortgage = String.valueOf(ThisTime.get(i).get("initialmortgage"));
            String initialmortgage1 = initialmortgage.substring(0, initialmortgage.length() - 2);
            resultMap.put("atlatest", atlatest1);
            resultMap.put("initialmortgage", initialmortgage1);
        }


        return resultMap;
    }

    @Override
    public Map<String, Object> senderList(Map<String, Object> map) {
        //分页参数处理
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(NumberConvertUtil.parseInt(String.valueOf(map.get("page_num"))));
        pageParam.setPageSize(NumberConvertUtil.parseInt(String.valueOf(map.get("page_size"))));

        StringBuffer listSql = new StringBuffer("SELECT id,sender_name AS senderName,phone,CONCAT(province,city,district) AS province,address,postcodes," +
                "DATE_FORMAT(create_time,'%Y-%m-%d %H:%i:%s') AS createTime,type FROM t_sender_info WHERE cust_id='");
        String custId = String.valueOf(map.get("cust_id"));
        listSql.append(custId + "'");
        String senderName = String.valueOf(map.get("sender_name"));
        if (StringUtil.isNotEmpty(senderName)) {
            listSql.append(" AND sender_name LIKE '%" + senderName + "%'");
        }
        String phone = String.valueOf(map.get("phone"));
        if (StringUtil.isNotEmpty(phone)) {
            listSql.append(" AND phone LIKE '%" + phone + "%'");
        }

        Page page = new Pagination().getPageData(listSql.toString(), null, pageParam, jdbcTemplate);
        Map<String, Object> resultMap = new HashMap<>(10);
        resultMap.put("total", page.getTotal());
        resultMap.put("rows", page.getList());
        return resultMap;
    }

    @Override
    public void senderAdd(Map<String, Object> map) {
        String id = String.valueOf(System.currentTimeMillis());
        String custId = String.valueOf(map.get("cust_id"));
        String senderName = String.valueOf(map.get("sender_name"));
        String phone = String.valueOf(map.get("phone"));
        String[] provinceInfo = (String[])map.get("province");
        String province = provinceInfo[0];
        String city = provinceInfo[1];
        String district = provinceInfo[2];
        String address = String.valueOf(map.get("address"));
        String postCodes = String.valueOf(map.get("postcodes"));
        String type = String.valueOf(map.get("type"));
        StringBuffer stringBuffer = new StringBuffer("INSERT INTO t_sender_info (id,cust_id,sender_name,phone,province,city,district,address," +
                "postcodes,type,create_time) VALUES ('");
        stringBuffer.append(id + "','").append(custId + "','")
                .append(senderName + "','").append(phone + "','")
                .append(province + "','").append(city + "','")
                .append(district + "','").append(address + "','")
                .append(postCodes + "','" + type + "',NOW())");
        jdbcTemplate.update(stringBuffer.toString());

        //如果此次添加的为默认地址，则把该企业名下其他地址 设为 非默认
        updateOthers(type, custId, id);
    }

    @Override
    public void senderDelete(String id) {
        String sql = "DELETE FROM t_sender_info WHERE id='" + id + "'";
        jdbcTemplate.update(sql);
    }

    @Override
    public void defaultUpdate(String id, String cust_id) {
        //将所选ID设为默认发件地址
        String defaultSql = "UPDATE t_sender_info SET type='1' WHERE id='" + id + "' AND cust_id='" + cust_id + "'";
        jdbcTemplate.update(defaultSql);
        //将该企业下的其他发件地址 设置为 非默认
        String notDefaultSql = "UPDATE t_sender_info SET type='2' WHERE id!='" + id + "' AND cust_id='" + cust_id + "'";
        jdbcTemplate.update(notDefaultSql);
    }

    @Override
    public void senderUpdate(Map<String, Object> map) {
        String id = String.valueOf(map.get("id"));
        String custId = String.valueOf(map.get("cust_id"));
        String senderName = String.valueOf(map.get("sender_name"));
        String phone = String.valueOf(map.get("phone"));

        String[] provinceInfo = (String[])map.get("province");
        String province = provinceInfo[0];
        String city = provinceInfo[1];
        String district = provinceInfo[2];
        String address = String.valueOf(map.get("address"));
        String postCodes = String.valueOf(map.get("postcodes"));
        String type = String.valueOf(map.get("type"));

        StringBuffer stringBuffer = new StringBuffer("UPDATE t_sender_info SET sender_name='");
        stringBuffer.append(senderName).append("',phone='").append(phone).append("',province='")
                .append(province).append("',city='").append(city).append("',district='")
                .append(district).append("',address='").append(address).append("',postcodes='")
                .append(postCodes).append("',type='").append(type).append("',create_time=NOW() WHERE id='")
                .append(id).append("' AND cust_id='").append(custId).append("'");
        jdbcTemplate.update(stringBuffer.toString());

        //如果此次添加的为默认地址，则把该企业名下其他地址 设为 非默认
        updateOthers(type, custId, id);
    }

    /**
     * 如果此次添加或修改的为默认地址，则把该企业名下其他地址 设为 非默认
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/5 17:38
     */
    public void updateOthers(String type, String custId, String id) {
        if ("1".equals(type)) {
            //则把该企业下的其他发件人信息 修改为 非默认地址
            StringBuffer updateOthers = new StringBuffer("UPDATE t_sender_info SET type='2' WHERE cust_id='");
            updateOthers.append(custId).append("' AND id!='").append(id).append("'");
            jdbcTemplate.update(updateOthers.toString());
        }
    }


}



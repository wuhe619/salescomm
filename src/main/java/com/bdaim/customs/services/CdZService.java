package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.page.PageList;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.CangdanXmlEXP311;
import com.bdaim.util.DateUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/***
 * 舱单.主单
 */
@Service("busi_cd_z")
@Transactional
public class CdZService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(CdZService.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private CustomerUserDao customerUserDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private CustomerDao customerDao;

//    @Autowired
//    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private CangdanXmlEXP311 cangdanXmlEXP311;

    @Autowired
    private CustomerService customerService;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // TODO Auto-generated method stub
        if (StringUtil.isNotEmpty(info.getString("fromSbzId"))) {
            HBusiDataManager h = serviceUtils.getObjectByIdAndType(cust_id, info.getLong("fromSbzId"), BusiTypeEnum.SZ.getType());
            if (h == null) {
                throw new TouchException("数据不存在");
            }
            if (!cust_id.equals(h.getCust_id().toString())) {
                throw new TouchException("你无权处理");
            }

            List<HBusiDataManager> dataList = new ArrayList<>();
            if ("Y".equals(h.getExt_2())) {
                throw new TouchException("已经提交过了,不能重复提交");
            }

            buildDanList0(info, id, dataList, cust_id, cust_user_id, h);
            int index = -1;
            List<JSONObject> fdData = new ArrayList();
            List<JSONObject> sData = new ArrayList();
            JSONObject content, json;
            HBusiDataManager dm;
            for (int i = 0; i < dataList.size(); i++) {
                dm = dataList.get(i);
                json = JSON.parseObject(JSON.toJSONString(dm));
                content = JSON.parseObject(dm.getContent());
                content.remove("products");
                //serviceUtils.addDataToES(dm.getId().toString(), dm.getType(), JSON.parseObject(dm.getContent()));
                if (dm.getType().equals(BusiTypeEnum.CZ.getType())) {
                    index = i;
                    serviceUtils.addDataToES(dm.getId().toString(), dm.getType(), JSON.parseObject(dm.getContent()));
                } else if (dm.getType().equals(BusiTypeEnum.CF.getType())) {
                    json.putAll(content);
                    fdData.add(json);
                    //fdData.add(JSON.parseObject(dm.getContent()));
                } else if (dm.getType().equals(BusiTypeEnum.CS.getType())) {
                    json.putAll(content);
                    sData.add(json);
                    //sData.add(JSON.parseObject(dm.getContent()));
                }
            }
            if (fdData.size() > 0) {
                elasticSearchService.bulkInsertDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.CF.getType()), Constants.INDEX_TYPE, fdData);
            }
            if (sData.size() > 0) {
                elasticSearchService.bulkInsertDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.CS.getType()), Constants.INDEX_TYPE, sData);
            }
            if (index > -1) {
                dataList.remove(index);
            }
            if (dataList.size() > 0) {
                Map<String, List<HBusiDataManager>> datamap = new TreeMap<>();
                for (HBusiDataManager manager : dataList) {
                    if (datamap.containsKey(manager.getType())) {
                        List<HBusiDataManager> d = datamap.get(manager.getType());
                        d.add(manager);
                    } else {
                        List<HBusiDataManager> d = new ArrayList<>();
                        d.add(manager);
                        datamap.put(manager.getType(), d);
                    }
                }
                Set<String> types = datamap.keySet();
                Iterator<String> iterator = types.iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    List<HBusiDataManager> d = datamap.get(key);
                    serviceUtils.batchInsert(key, d);
                }
            }
        }
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // 提交至海关平台
        {
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id, id, busiType);
            if (dbManager == null) {
                throw new TouchException("无权操作");
            }
            String content = dbManager.getContent();
            JSONObject json = JSONObject.parseObject(content);
            Iterator keys = info.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                json.put(key, info.get(key));
            }
            serviceUtils.updateDataToES(busiType, id.toString(), json);
        }

    }


    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) throws Exception {
        //舱单导出
        if (StringUtil.isNotEmpty(param.getString("_rule_")) && param.getString("_rule_").startsWith("_export")) {
            String split = "||";
            StringBuffer content = new StringBuffer();
            //主单数据
            content.append(info.getString("bill_no")).append(split)
                    .append(info.getString("voyage_no")).append(split)
                    .append(info.getString("i_e_flag")).append(split)
                    .append(info.getString("traf_name")).append(split)
                    .append(info.getString("traf_name_en")).append(split)
                    .append(info.getString("gross_wt")).append(split)
                    .append(info.getString("pack_no")).append(split)
                    .append(info.getString("single_batch_num")).append(split)
                    .append(info.getString("traf_mode")).append(split)
                    .append(info.getString("depart_arrival_port")).append(split)
                    .append(info.getString("i_e_port")).append(split).append("\r\n");
            //分单数据
            List<JSONObject> singles = serviceUtils.queryChildData(BusiTypeEnum.CF.getType(), cust_id, cust_group_id, cust_user_id, id, param);
            if (singles != null && singles.size() > 0) {
                for (JSONObject jo : singles) {
                    content.append(jo.getString("bill_no")).append(split)
                            .append(jo.getString("main_gname")).append(split)
                            .append(jo.getString("pack_no")).append(split)
                            .append(jo.getString("total_value")).append(split)
                            .append(jo.getString("curr_code")).append(split)
                            .append(info.getString("trade_country")).append(split)
                            .append(jo.getString("wrap_type")).append(split)
                            .append("\r\n");
                }
            }
            info.put("_export_cd_z_main_data", content);
        } else if ("HAIGUAN".equals(param.getString("_rule_"))) {
            String sql = "select id,content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=? and id=? ";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, busiType, id);
            if (list.size() == 0) {
                log.warn("舱单主单数据不存在[" + busiType + "]" + id);
                throw new TouchException("1000", "舱单主单数据不存在");
            }
            Map m = list.get(0);
            //Map m  = jdbcTemplate.queryForMap(sql, busiType, id);
            String cdContent = String.valueOf(m.get("content"));
            if ("1".equals(String.valueOf(m.get("ext_1"))) && StringUtil.isNotEmpty(cdContent)
                    && "1".equals(JSON.parseObject(cdContent).getString("send_status"))) {
                log.warn("舱单主单:[" + id + "]已提交至海关");
                throw new TouchException("舱单主单:[" + id + "]已提交至海关");
            }

            // 更新舱单主单信息
            String content = (String) m.get("content");
            JSONObject jo = JSONObject.parseObject(content);
            jo.put("ext_1", "1");
            jo.put("send_status", "1");
            info.put("ext_1", "1");
            info.put("send_status", "1");
            jo.put("id", m.get("id"));
            jo.put("cust_id", m.get("cust_id"));
            jo.put("cust_group_id", m.get("cust_group_id"));
            jo.put("cust_user_id", m.get("cust_user_id"));
            jo.put("create_id", m.get("create_id"));
            jo.put("create_date", m.get("create_date"));
            jo.put("update_id", m.get("update_id"));
            jo.put("update_date", m.get("update_date"));
            if (m.get("ext_1") != null && !"".equals(m.get("ext_1")))
                jo.put("ext_1", m.get("ext_1"));
            if (m.get("ext_2") != null && !"".equals(m.get("ext_2")))
                jo.put("ext_2", m.get("ext_2"));
            if (m.get("ext_3") != null && !"".equals(m.get("ext_3")))
                jo.put("ext_3", m.get("ext_3"));
            if (m.get("ext_4") != null && !"".equals(m.get("ext_4")))
                jo.put("ext_4", m.get("ext_4"));
            if (m.get("ext_5") != null && !"".equals(m.get("ext_5")))
                jo.put("ext_5", m.get("ext_5"));
            String d = DateUtil.fmtDateToStr(new Date(), "yyyy-MM-dd HH:mm:ss");
            jo.put("decl_time", d);
            info.put("decl_time", d);
            //更新舱单分单信息
            String selectSql = "select id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(BusiTypeEnum.CF.getType(), "") + " WHERE ext_4=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.CZ.getType(), "") + " WHERE id = ?) AND type = ? AND IFNULL(ext_1,'') <>'1' ";
            List<Map<String, Object>> ds = jdbcTemplate.queryForList(selectSql, id, BusiTypeEnum.CF.getType());
            //start to create xml file
            log.info("starto to create xml file");
            Map<String, Object> customerInfo = getCustomerInfo(cust_id);
            log.info("getCustomerInfo 查询企业信息，" + customerInfo);
            CustomerProperty iObj = customerDao.getProperty(cust_id, "i");
            log.info("CustomerUserPropertyDO", iObj);
            String sendId = "";
            if (iObj != null) {
                String value = iObj.getPropertyValue();
                JSONObject iJson = JSONObject.parseObject(value);
                sendId = iJson.getString("sender_id");
            }
            log.info("sender_id", sendId);
            customerInfo.put("sender_id", sendId);
            CustomerUser customerUser = customerUserDao.get(cust_user_id);
            log.info("customerUser", customerUser);
            CustomerUserPropertyDO propertyDO = customerUserDao.getProperty(cust_user_id.toString(), "input_no");
            customerInfo.put("input_name", "");
            customerInfo.put("input_no", "");
            if (customerUser != null) {
                customerInfo.put("input_name", customerUser.getRealname());
            }
            if (propertyDO != null) {
                customerInfo.put("input_no", propertyDO.getPropertyValue());
            }
            log.info("舱单分单数：" + ds.size());

            cdCheck(m, ds, customerInfo);

            String xmlString = cangdanXmlEXP311.createXml(m, ds, customerInfo);
            if (StringUtil.isEmpty(xmlString)) {
                throw new TouchException("2000", "生成舱单xml报文出错");
            }
            info.put("xml", xmlString);

            sql = "UPDATE " + HMetaDataDef.getTable(busiType, "") + " SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE id = ?  AND type = ?  ";
            jdbcTemplate.update(sql, jo.toJSONString(), id, busiType);
            serviceUtils.updateDataToES(BusiTypeEnum.CZ.getType(), id.toString(), jo);

            String updateSql = " UPDATE " + HMetaDataDef.getTable(BusiTypeEnum.CF.getType(), "") + " SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE id =? AND type = ? AND IFNULL(ext_1,'') <>'1' ";
            for (int i = 0; i < ds.size(); i++) {
                m = ds.get(i);
                content = (String) m.get("content");
                jo = JSONObject.parseObject(content);
                jo.put("ext_1", "1");
                jo.put("send_status", "1");
                jo.put("id", m.get("id"));
                jo.put("cust_id", m.get("cust_id"));
                jo.put("cust_group_id", m.get("cust_group_id"));
                jo.put("cust_user_id", m.get("cust_user_id"));
                jo.put("create_id", m.get("create_id"));
                jo.put("create_date", m.get("create_date"));
                jo.put("update_id", m.get("update_id"));
                jo.put("update_date", m.get("update_date"));
                jo.put("decl_time", d);
                if (m.get("ext_1") != null && !"".equals(m.get("ext_1")))
                    jo.put("ext_1", m.get("ext_1"));
                if (m.get("ext_2") != null && !"".equals(m.get("ext_2")))
                    jo.put("ext_2", m.get("ext_2"));
                if (m.get("ext_3") != null && !"".equals(m.get("ext_3")))
                    jo.put("ext_3", m.get("ext_3"));
                if (m.get("ext_4") != null && !"".equals(m.get("ext_4")))
                    jo.put("ext_4", m.get("ext_4"));
                if (m.get("ext_5") != null && !"".equals(m.get("ext_5")))
                    jo.put("ext_5", m.get("ext_5"));
                jdbcTemplate.update(updateSql, jo.toJSONString(), m.get("id"), BusiTypeEnum.CF.getType());
                serviceUtils.updateDataToES(BusiTypeEnum.CF.getType(), String.valueOf(m.get("id")), jo);
            }


        }

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    @Deprecated
    public void buildDanList(JSONObject info, Long id, List<HBusiDataManager> dataList, String custId, Long userId, HBusiDataManager h) throws Exception {
        HBusiDataManager CZ = new HBusiDataManager();
        CZ.setType(BusiTypeEnum.CZ.getType());
        CZ.setId(id);
        CZ.setCreateDate(new Date());
        CZ.setCust_id(Long.valueOf(custId));
        CZ.setCreateId(Long.valueOf(userId));
        CZ.setExt_3(h.getExt_3());
        CZ.setExt_1("0");//未发送 1，已发送


        JSONObject json = JSON.parseObject(h.getContent());
        json.put("create_id", userId);
        json.put("cust_id", custId);
        json.put("type", CZ.getType());
        json.put("create_date", CZ.getCreateDate());
        json.put("send_status", CZ.getExt_1());
        json.put("commit_cangdan_status", "Y");

        JSONObject jon = JSON.parseObject(h.getContent());
        jon.put("commit_cangdan_status", "Y");
        h.setExt_2("Y");
        h.setContent(jon.toJSONString());
//        dataList.add(h);

        String sql = "update " + HMetaDataDef.getTable(h.getType(), "") + " set content='" + jon.toJSONString() + "'"
                + " ,ext_2='Y'"
                + " where id=" + h.getId() + " and type='" + h.getType() + "'";
        jdbcTemplate.update(sql);

        Iterator keys = json.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            info.put(key, json.get(key));
        }
        info.put("ext_3", h.getExt_3());
        info.put("ext_1", "0");
//		String content = json.toJSONString();
        CZ.setContent(info.toJSONString());
        dataList.add(CZ);
        List<HBusiDataManager> parties = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), info.getLong("fromSbzId"));
        for (HBusiDataManager hp : parties) {
            HBusiDataManager hm = new HBusiDataManager();
            hm.setType(BusiTypeEnum.CF.getType());
            hm.setCreateDate(new Date());
            Long fid = sequenceService.getSeq(BusiTypeEnum.CF.getType());
            hm.setId(fid);
            hm.setExt_3(hp.getExt_3());
            hm.setExt_4(hp.getExt_4());
            hm.setCreateId(hp.getCreateId());
            hm.setCust_id(hp.getCust_id());
            JSONObject _content = JSON.parseObject(hp.getContent());
            _content.put("pid", id);
            _content.put("main_bill_no", json.get("bill_no"));
            hm.setContent(_content.toJSONString());
            dataList.add(hm);
            List<HBusiDataManager> goods = serviceUtils.listSdByBillNo(custId, BusiTypeEnum.SS.getType(), hp.getExt_4(), hp.getExt_3());
            for (HBusiDataManager gp : goods) {
                HBusiDataManager good = new HBusiDataManager();
                gp.setType(BusiTypeEnum.CS.getType());
                Long gid = sequenceService.getSeq(BusiTypeEnum.CS.getType());
                good.setId(gid);
                good.setCreateId(userId);
                good.setCreateDate(new Date());
                JSONObject __content = JSON.parseObject(gp.getContent());
                __content.put("pid", fid);
                _content.put("main_bill_no", _content.get("bill_no"));
                good.setContent(__content.toJSONString());
                good.setType(BusiTypeEnum.CS.getType());
                good.setCreateId(gp.getCreateId());
                good.setCust_id(gp.getCust_id());
                good.setExt_3(gp.getExt_3());
                good.setExt_4(gp.getExt_4());
                dataList.add(good);
            }
        }
    }

    public void buildDanList0(JSONObject info, Long id, List<HBusiDataManager> dataList, String custId, Long userId, HBusiDataManager h) throws Exception {
        HBusiDataManager cz = new HBusiDataManager();
        cz.setType(BusiTypeEnum.CZ.getType());
        cz.setId(id);
        cz.setCreateDate(new Date());
        cz.setCust_id(Long.valueOf(custId));
        cz.setCreateId(Long.valueOf(userId));
        cz.setExt_3(h.getExt_3());
        cz.setExt_1("0");//0 未发送 1，已发送


        JSONObject json = JSON.parseObject(h.getContent());
        json.put("create_id", userId);
        json.put("cust_id", custId);
        json.put("type", cz.getType());
        json.put("create_date", cz.getCreateDate());
        json.put("send_status", cz.getExt_1());
        json.put("commit_cangdan_status", "Y");

        JSONObject jon = JSON.parseObject(h.getContent());
        jon.put("commit_cangdan_status", "Y");
        h.setExt_2("Y");
        h.setContent(jon.toJSONString());
//        dataList.add(h);

        String sql = "update " + HMetaDataDef.getTable(h.getType(), "") + " set content='" + jon.toJSONString() + "'"
                + " ,ext_2='Y'"
                + " where id=" + h.getId() + " and type='" + h.getType() + "'";
        jdbcTemplate.update(sql);

        Iterator keys = json.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            info.put(key, json.get(key));
        }
        info.put("ext_3", h.getExt_3());
        info.put("ext_1", "0");
//		String content = json.toJSONString();

        // 根据主单号查询申报单分单列表
        List<HBusiDataManager> parties = serviceUtils.listDataByParentBillNo(custId, BusiTypeEnum.SF.getType(), h.getExt_3());
        List<String> billNos = new ArrayList<>();
        for (HBusiDataManager hp : parties) {
            billNos.add(hp.getExt_3());
        }
        // 查询所有分单下的税单
        //List<HBusiDataManager> goods = serviceUtils.listDataByParentBillNos(custId, BusiTypeEnum.SS.getType(), billNos);
        List<JSONObject> jsonObjects = serviceUtils.listSdByBillNos(custId, BusiTypeEnum.SS.getType(), h.getExt_3(), billNos, new JSONObject());
        List<HBusiDataManager> goods = JSON.parseArray(JSON.toJSONString(jsonObjects), HBusiDataManager.class);
        Map<Long, List> cache = new HashMap<>();
        JSONObject fd = null;
        List<HBusiDataManager> tmp;
        for (HBusiDataManager p : goods) {
            fd = JSON.parseObject(p.getContent());
            if (fd != null) {
                tmp = cache.get(fd.getLong("pid"));
                if (tmp == null) {
                    tmp = new ArrayList<>();
                }
                tmp.add(p);
                cache.put(fd.getLong("pid"), tmp);
            }
        }
        List<HBusiDataManager> goodList = null;
        HBusiDataManager hm, good;
        int pack_no = 0;
        Double weightTotal = 0d;
        for (HBusiDataManager hp : parties) {
            hm = new HBusiDataManager();
            hm.setType(BusiTypeEnum.CF.getType());
            hm.setCreateDate(new Date());
            Long fid = sequenceService.getSeq(BusiTypeEnum.CF.getType());
            hm.setId(fid);
            hm.setExt_2(hp.getExt_2());
            hm.setExt_3(hp.getExt_3());
            hm.setExt_4(hp.getExt_4());
            hm.setCreateId(hp.getCreateId());
            hm.setCust_id(hp.getCust_id());
            JSONObject _content = JSON.parseObject(hp.getContent());
            _content.put("pid", id);
            _content.put("main_bill_no", json.get("bill_no"));
            // 舱单总分单件数=分运单件数和
            pack_no += _content.getIntValue("pack_no");

            Double fdWeightTotal = 0.0;
            Double total_value = 0.0;
            BigDecimal qty = null;
            BigDecimal multiply = null;
            goodList = cache.get(hp.getId());
            if (goodList != null) {
                for (HBusiDataManager gp : goodList) {
                    good = new HBusiDataManager();
                    //gp.setType(BusiTypeEnum.CS.getType());
                    Long gid = sequenceService.getSeq(BusiTypeEnum.CS.getType());
                    good.setId(gid);
                    good.setCreateId(userId);
                    good.setCreateDate(new Date());
                    JSONObject productContent = JSON.parseObject(gp.getContent());
                    productContent.put("pid", hm.getId());
                    productContent.put("main_bill_no", _content.get("bill_no"));
                    productContent.put("opt_type", "ADD");
                    // 分单重量（公斤）=分单所有商品毛重
                    String ggrosswt = productContent.getString("ggrosswt");
                    if (StringUtil.isEmpty(ggrosswt)) {
                        ggrosswt = "0";
                    }
                    fdWeightTotal += Double.valueOf(ggrosswt);
                    String G_QTY = productContent.getString("g_qty");
                    String decl_price = productContent.getString("decl_price");
                    if (StringUtil.isNotEmpty(G_QTY) && StringUtil.isNotEmpty(decl_price)) {
                        qty = new BigDecimal(G_QTY);
                        multiply = qty.multiply(new BigDecimal(decl_price));
                        float total_price = multiply.setScale(5, BigDecimal.ROUND_HALF_UP).floatValue();
                        //税单价格合计
                        productContent.put("total_price", total_price);
                        total_value += total_price;
                    }
                    good.setContent(productContent.toJSONString());
                    good.setType(BusiTypeEnum.CS.getType());
                    good.setCreateId(gp.getCreateId());
                    good.setCust_id(gp.getCust_id());
                    good.setExt_2(gp.getExt_2());
                    good.setExt_3(gp.getExt_3());
                    good.setExt_4(gp.getExt_4());
                    dataList.add(good);
                }
            }
            // 分单价值
            _content.put("total_value", total_value.floatValue());
            // 分单重量
            _content.put("weight", fdWeightTotal.floatValue());
            hm.setContent(_content.toJSONString());
            dataList.add(hm);
            /*String weight = _content.getString("weight");
            if (StringUtil.isEmpty(weight)) {
                weight = "0";
            }
            weightTotal += Double.valueOf(weight);*/
            //weightTotal += fdWeightTotal;
        }
        info.put("total_pack_no", pack_no);
        //info.put("weight_total", weightTotal.floatValue());
        cz.setContent(info.toJSONString());
        dataList.add(cz);
        h.setContent(jon.toJSONString());
    }

    /**
     * 查询分单和商品
     *
     * @param busiType
     * @param cust_id
     * @param cust_group_id
     * @param cust_user_id
     * @param pid
     * @param info
     * @return
     */
    private List queryChildData(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long pid, JSONObject info, JSONObject param) {
        List sqlParams = new ArrayList();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
        if (!"all".equals(cust_id))
            sqlstr.append(" and cust_id='").append(cust_id).append("'");

        sqlParams.add(busiType);
        Iterator keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(String.valueOf(param.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key) || "_rule_".equals(key)) {
                continue;
            } else if (key.startsWith("_g_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') > ?");
            } else if (key.startsWith("_ge_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') >= ?");
            } else if (key.startsWith("_l_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') < ?");
            } else if (key.startsWith("_le_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') <= ?");
            } else if (key.startsWith("_eq_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') = ?");
            } else {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
            }
            sqlParams.add(param.get(key));
        }
        //sqlstr.append(" and JSON_EXTRACT(content, '$.pid')=?");
        sqlstr.append(" and ext_4=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(busiType), "") + " WHERE id = ?)");
        sqlParams.add(pid);

        List<Map<String, Object>> ds = jdbcTemplate.queryForList(sqlstr.toString(), sqlParams.toArray());
        List data = new ArrayList();
        for (int i = 0; i < ds.size(); i++) {
            Map m = (Map) ds.get(i);
            JSONObject jo = null;
            try {
                if (m.containsKey("content")) {
                    jo = JSONObject.parseObject((String) m.get("content"));
                    jo.put("id", m.get("id"));
                    jo.put("cust_id", m.get("cust_id"));
                    jo.put("cust_group_id", m.get("cust_group_id"));
                    jo.put("cust_user_id", m.get("cust_user_id"));
                    jo.put("create_id", m.get("create_id"));
                    jo.put("create_date", m.get("create_date"));
                    jo.put("update_id", m.get("update_id"));
                    jo.put("update_date", m.get("update_date"));
                    if (m.get("ext_1") != null && !"".equals(m.get("ext_1")))
                        jo.put("ext_1", m.get("ext_1"));
                    if (m.get("ext_2") != null && !"".equals(m.get("ext_2")))
                        jo.put("ext_2", m.get("ext_2"));
                    if (m.get("ext_3") != null && !"".equals(m.get("ext_3")))
                        jo.put("ext_3", m.get("ext_3"));
                    if (m.get("ext_4") != null && !"".equals(m.get("ext_4")))
                        jo.put("ext_4", m.get("ext_4"));
                    if (m.get("ext_5") != null && !"".equals(m.get("ext_5")))
                        jo.put("ext_5", m.get("ext_5"));
                } else
                    jo = JSONObject.parseObject(JSONObject.toJSONString(m));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (jo == null) { //jo异常导致为空时，只填充id
                jo = new JSONObject();
                jo.put("id", m.get("id"));
            }
            data.add(jo);
        }
        return data;
    }


    private Map<String, Object> getCustomerInfo(String custId) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(1);
        CustomerRegistDTO customerRegistDTO = new CustomerRegistDTO();
        customerRegistDTO.setCustId(custId);
        PageList pageList = customerService.getCustomerInfo(pageParam, customerRegistDTO);
        List list = pageList.getList();
        return (Map<String, Object>) list.get(0);
    }


    private String cdCheck(Map<String, Object> m, List<Map<String, Object>> dsList, Map<String, Object> customerInfo) throws TouchException {
        if (!customerInfo.containsKey("sender_id") || null == customerInfo.get("sender_id") || "".equals(customerInfo.get("sender_id"))) {
            throw new TouchException("2001", "核心字段sender_id缺失");
        }
        String bdmessage = "舱单%s，缺失%s";
        String filedName = "";

        String mainContent = (String) m.get("content");
        JSONObject mainjson = JSONObject.parseObject(mainContent);

        if (!mainjson.containsKey("bill_no") || StringUtil.isEmpty(mainjson.getString("bill_no"))) {
            filedName += "," + CDReportEnum.BillNo.getName();
        }

        if (!mainjson.containsKey("voyage_no") || StringUtil.isEmpty(mainjson.getString("voyage_no"))) {
            filedName += "," + CDReportEnum.VoyageNo.getName();
        }

        if (!mainjson.containsKey("i_e_flag") || StringUtil.isEmpty(mainjson.getString("i_e_flag"))) {
            filedName += "," + CDReportEnum.IEFlag.getName();
        }

        if (!mainjson.containsKey("traf_name") || StringUtil.isEmpty(mainjson.getString("traf_name"))) {
            filedName += "," + CDReportEnum.TrafCnName.getName();
        }
        if (!mainjson.containsKey("traf_name_en") || StringUtil.isEmpty(mainjson.getString("traf_name_en"))) {
            filedName += "," + CDReportEnum.TrafEnName.getName();
        }
        if (!mainjson.containsKey("gross_wt") || StringUtil.isEmpty(mainjson.getString("gross_wt"))) {
            filedName += "," + CDReportEnum.GrossWt.getName();
        }

        if (!mainjson.containsKey("pack_no") || StringUtil.isEmpty(mainjson.getString("pack_no"))) {
            filedName += "," + CDReportEnum.PackNo.getName();
        }

        if (!mainjson.containsKey("single_batch_num") || StringUtil.isEmpty(mainjson.getString("single_batch_num"))) {
            filedName += "," + CDReportEnum.BillNum.getName();
        }
        if (!mainjson.containsKey("traf_mode") || StringUtil.isEmpty(mainjson.getString("traf_mode"))) {
            filedName += "," + CDReportEnum.TrafMode.getName();
        }
        if (!mainjson.containsKey("i_e_date") || StringUtil.isEmpty(mainjson.getString("i_e_date"))) {
            filedName += "," + CDReportEnum.IEDate.getName();
        }

        if (!mainjson.containsKey("depart_arrival_port") || StringUtil.isEmpty(mainjson.getString("depart_arrival_port"))) {
            filedName += "," + CDReportEnum.DestinationPort.getName();
        }

        if (!mainjson.containsKey("decl_port") || StringUtil.isEmpty(mainjson.getString("decl_port"))) {//进出口岸取申报地海关的值
            filedName += "," + CDReportEnum.IEPort.getName();
        }

        if (StringUtil.isEmpty((String) customerInfo.get("agent_code"))) {
            filedName += "," + CDReportEnum.TradeCo.getName();
        }
        if (StringUtil.isEmpty((String) customerInfo.get("enterpriseName"))) {
            filedName += "," + CDReportEnum.TradeName.getName();
        }

        if (StringUtil.isEmpty((String) customerInfo.get("input_no"))) {//录入人卡号
            filedName += "," + CDReportEnum.InputNo.getName();
        }

        if (StringUtil.isEmpty((String) customerInfo.get("input_name"))) {
            filedName += "," + CDReportEnum.InputOpName.getName();
        }

        if (StringUtil.isEmpty((String) customerInfo.get("agent_code"))) {
            filedName += "," + CDReportEnum.InputCompanyCode.getName();
        }

        if (StringUtil.isEmpty((String) customerInfo.get("enterpriseName"))) {
            filedName += "," + CDReportEnum.InputCompanyName.getName();
        }

        filedName = cdfCheck(dsList, filedName);

        if (StringUtil.isNotEmpty(filedName)) {
            String message = String.format(bdmessage, mainjson.getString("bill_no"), filedName);
            throw new TouchException("2000", message);
        }
        return "success";
    }

    private String cdfCheck(List<Map<String, Object>> fdList, String filedName) {
        for (Map<String, Object> m : fdList) {
            String content = (String) m.get("content");
            JSONObject json = JSONObject.parseObject(content);
            String billNo = json.getString("bill_no");
            Boolean hasError = false;
            String msg = "分运单 ";
            msg += billNo + "，缺失，";

            if (!json.containsKey("main_gname") || StringUtil.isEmpty(json.getString("main_gname"))) {
                msg += "主要商品名称,";
                hasError = true;
            }
            if (!json.containsKey("pack_no") || StringUtil.isEmpty(json.getString("pack_no"))) {
                msg += "件数,";
                hasError = true;
            }
            if (!json.containsKey("weight") || StringUtil.isEmpty(json.getString("weight"))) {
                msg += "商品毛重,";
                hasError = true;
            }
            if (!json.containsKey("total_value") || StringUtil.isEmpty(json.getString("total_value"))) {
                msg += "价值,";
                hasError = true;
            }
            if (!json.containsKey("curr_code") || StringUtil.isEmpty(json.getString("curr_code"))) {
                msg += "成交币制 ";
                hasError = true;
            }

            if (!hasError) {
                continue;
            } else {
                filedName += "\n\r" + msg;
            }
        }
        return filedName;
    }


    /**
     * 舱单报文必填字段枚举
     */
    public enum CDReportEnum {
        sender_id("sender_id", "sender_id"),
        BillNo("bill_no", "总运单号"),
        VoyageNo("voyage_no", "运输工具航次(班)号"),
        IEFlag("i_e_flag", "进出口标志"),
        TrafCnName("traf_name", "运输工具中文名称"),
        TrafEnName("traf_name_en", "运输工具英文名称"),
        GrossWt("gross_wt", "毛重"),
        PackNo("pack_no", "件数"),
        BillNum("single_batch_num", "分运单数"),
        TrafMode("traf_mode", "运输方式代码"),
        IEDate("i_e_date", "进出口日期"),
        DestinationPort("depart_arrival_port", "指运港(抵运港)"),
        IEPort("i_e_port", "申报地海关"),//
        TradeCo("s_c_code_busi_unit", "经营单位编号"),
        TradeName("business_unit_name", "经营单位名称"),
        InputNo("input_no", "录入人卡号"),
        InputOpName("input_name", "录入人姓名"),
        InputCompanyCode("s_c_code_shipper", "录入单位代码"),
        InputCompanyName("enterpriseName", "录入单位名称");

        private String key;
        private String name;

        CDReportEnum(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }


    }
}

package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.BaoguandanXmlEXP301;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.PageList;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * 报关单.分单
 */
@Service("busi_bgd_f")
public class BgdFService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(BgdFService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private BaoguandanXmlEXP301 baoguandanXmlEXP301;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerUserDao customerUserDao;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // TODO Auto-generated method stub
        Integer pid = info.getInteger("pid");
        String billNo = info.getString("bill_no");
        if (pid == null) {
            log.error("主单id不能为空");
            throw new TouchException("主单id不能为空");
        }
        if (StringUtil.isEmpty(billNo)) {
            log.error("分单号不能为空");
            throw new TouchException("分单号不能为空");
        }
        HBusiDataManager bgdzd = serviceUtils.getObjectByIdAndType(cust_id,pid.longValue(), BusiTypeEnum.BZ.getType());
        if(bgdzd==null){
            throw new TouchException("无权操作");
        }
        List<HBusiDataManager> list = serviceUtils.listDataByPid(cust_id,busiType, pid.longValue(),BusiTypeEnum.BZ.getType());
        if (list != null && list.size() > 0) {
            for (HBusiDataManager hBusiDataManager : list) {
                if(billNo.equals(hBusiDataManager.getExt_3())){
                    log.error("分单号【" + billNo + "】在主单【" + bgdzd.getExt_3() + "】中已经存在");
                    throw new TouchException("分单号【" + billNo + "】在主单【" + bgdzd.getExt_3() + "】中已经存在");
                }
            }
        }
        info.put("type", BusiTypeEnum.BF.getType());
        info.put("check_status", "0");
        info.put("idcard_pic_flag", "0");
        info.put("main_gname", "");
        info.put("low_price_goods", 0);
        info.put("id", id);
        info.put("pid", pid);
        info.put("opt_type", "APD");
        info.put("ext_3",billNo);
        info.put("ext_4",bgdzd.getExt_3());
        info.put("main_bill_no",bgdzd.getExt_3());
        serviceUtils.addDataToES(id.toString(), busiType, info);
        JSONObject jsonObject = JSONObject.parseObject(bgdzd.getContent());
        if (info.containsKey("weight") && info.getString("weight") != null) {
            if (jsonObject.containsKey("weight_total")) {
                String weight_total = jsonObject.getString("weight_total");
                if (StringUtil.isNotEmpty(weight_total)) {
                    weight_total = String.valueOf(Float.valueOf(weight_total) + Float.valueOf(info.getString("weight")));
                    jsonObject.put("weight_total", weight_total);//总重量
                }
            }
        }
        int value = 1;
        if (jsonObject.containsKey("party_total")) {
            value = jsonObject.getInteger("party_total") + value;
        }
        jsonObject.put("party_total", value);//分单总数

        bgdzd.setContent(jsonObject.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(bgdzd);
        serviceUtils.updateDataToES(BusiTypeEnum.BZ.getType(), bgdzd.getId().toString(), jsonObject);

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        {
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id,id, busiType);
            if(dbManager==null){
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
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) throws TouchException {
        // 提交至海关平台
        if ("HAIGUAN".equals(param.getString("_rule_"))) {
            String sql = "select id, content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from "+ HMetaDataDef.getTable(busiType,"")+" where type=? and id=? ";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, busiType, id);
            if (list.size() == 0) {
                log.warn("报关单分单数据不存在[" + busiType + "]" + id);
                throw new TouchException("1000", "报关单分单数据不存在");
            }
            Map m = list.get(0);
            //Map m = jdbcTemplate.queryForMap(sql, busiType, id);
            String cdContent = String.valueOf(m.get("content"));
            if ("1".equals(String.valueOf(m.get("ext_1"))) && StringUtil.isNotEmpty(cdContent)
                    && "1.".equals(JSON.parseObject(cdContent).getString("send_status"))) {
                log.warn("报关单分单:[" + id + "]已提交至海关");
                throw new TouchException("报关单分单:[" + id + "]已提交至海关");
            }
            // 更新报关单主单信息
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

            //start to create xml
            String mainsql = "select id,content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from "+ HMetaDataDef.getTable(BusiTypeEnum.BZ.getType(),"")+" where type=? and id=? ";
            list = jdbcTemplate.queryForList(mainsql, BusiTypeEnum.BZ.getType(), jo.getString("pid"));
            Map<String,Object> mainMap = list.get(0);
            List<HBusiDataManager> list2 = serviceUtils.listSdByBillNo(cust_id,BusiTypeEnum.BS.getType(),mainMap.get("ext_3").toString(),jo.getString("bill_no"));
            Map<String,Object> customerInfo = getCustomerInfo(cust_id);
            CustomerUserPropertyDO propertyDO = customerUserDao.getProperty(cust_user_id.toString(),"declare_no");
            CustomerUserPropertyDO iObj = customerUserDao.getProperty(cust_user_id.toString(),"i");
            String sendId="";
            if(iObj != null) {
                String value = iObj.getPropertyValue();
                JSONObject iJson = JSONObject.parseObject(value);
                sendId = iJson.getString("sender_id");
            }
            customerInfo.put("send_id",sendId);
            CustomerUser customerUser = customerUserDao.get(cust_user_id);
            customerInfo.put("input_name","");
            customerInfo.put("declare_no","");
            if(customerUser!=null){
                customerInfo.put("input_name",customerUser.getRealname());
            }
            if(propertyDO!=null){
                customerInfo.put("declare_no",propertyDO.getPropertyValue());
            }
            String xmlString = baoguandanXmlEXP301.createXml(mainMap,m,list2,customerInfo);
            info.put("xml",xmlString);

            sql = "UPDATE "+HMetaDataDef.getTable(busiType,"")+" SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE id = ? AND type = ? AND IFNULL(ext_1,'') <>'1' ";
            jdbcTemplate.update(sql, jo.toJSONString(), id, busiType);
            serviceUtils.updateDataToES(BusiTypeEnum.BF.getType(), id.toString(), jo);

        }else {
            // 查询报关单主单数据,合并到分单中
            long pid = info.getLong("pid");
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id,pid, BusiTypeEnum.BZ.getType());
            String content = null;
            if (dbManager != null) {
                content = dbManager.getContent();
            }
            if (StringUtil.isNotEmpty(content)) {
                JSONObject mainData = JSONObject.parseObject(content);
                Iterator keys = mainData.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (!info.containsKey(key)) {
                        info.put(key, mainData.get(key));
                    }
                }
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
        //报关单主单赋值到分单中
        HBusiDataManager bgdZ = serviceUtils.getObjectByIdAndType(cust_id,info.getLongValue("pid"), BusiTypeEnum.BZ.getType());
        if (bgdZ != null) {
            JSONObject jo = JSONObject.parseObject(bgdZ.getContent());
            Iterator keys = jo.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (!info.containsKey(key)) {
                    info.put(key, jo.get(key));
                }
            }

        }

    }


    private Map<String,Object> getCustomerInfo(String custId){
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(1);
        CustomerRegistDTO customerRegistDTO = new CustomerRegistDTO();
        customerRegistDTO.setCustId(custId);
        PageList pageList = customerService.getCustomerInfo(pageParam,customerRegistDTO);
        List list = pageList.getList();
        return (Map<String, Object>) list.get(0);
    }

}

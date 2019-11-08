package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.be.service.BusiEntityService;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.third.zhianxin.dto.BaseResult;
import com.bdaim.common.third.zhianxin.service.SearchListService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customersea.dto.CustomSeaTouchInfoDTO;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/***
 * B2B企业套餐包管理
 *
 */
@Service("busi_b2b_tcb")
@Transactional
public class B2BTcbService implements BusiService {
    private static Logger LOG = LoggerFactory.getLogger(B2BTcbService.class);

    @Autowired
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    private SearchListService searchListService;

    @Autowired
    private CustomerSeaService seaService;
    @Autowired
    private BusiEntityService busiEntityService;
    @Autowired
    private B2BTcbLogService b2BTcbLogService;

    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        String sql = "select id,content from " + HMetaDataDef.getTable(busiType, "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> countList = jdbcTemplate.queryForList(sql, busiType, cust_id);
        if (countList != null && countList.size() > 0) {
            JSONObject jsonObject = JSON.parseObject(String.valueOf(countList.get(0).get("content")));
            if (jsonObject.getLongValue("remain_num") > 0L) {
                LOG.warn("当前还有有效的套餐:{}不能再开通新的套餐", JSON.toJSONString(countList));
                throw new TouchException("当前还有有效的套餐,不能再开通新的套餐");
            }
        }
        info.put("ext_2", info.getString("name"));
        info.put("ext_3", info.getString("type"));
        info.put("ext_4", info.getString("status"));
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws TouchException {
        info.put("ext_2", info.getString("name"));
        info.put("ext_3", info.getString("type"));
        info.put("ext_4", info.getString("status"));
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5,update_date from "
                + HMetaDataDef.getTable(busiType, "") + " where type='").append(busiType).append("'");
        String name = params.getString("name");

        if (!"all".equals(cust_id))
            sqlstr.append(" and cust_id='").append(cust_id).append("'");
        if (StringUtil.isNotEmpty(name)) {
            sqlstr.append(" and content->'$.name'='").append(name).append("'");
        }
        return sqlstr.toString();
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub
    }

    /**
     * 获取企业B2B套餐包剩余量(只有1个套餐包有效)
     *
     * @param custId
     * @return
     */
    public long getB2BTcbQuantity(String custId) {
        String sql = "select id,content from " + HMetaDataDef.getTable(BusiTypeEnum.B2B_TC.getType(), "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, BusiTypeEnum.B2B_TC.getType(), custId);
        if (list == null || list.size() == 0) {
            return 0L;
        }
        return JSON.parseObject(String.valueOf(list.get(0).get("content"))).getLongValue("remain_num");
    }

    /**
     * 获取企业再用的套餐包
     *
     * @param custId
     * @return
     */
    public JSONObject getUseB2BTcb(String custId) {
        String sql = "select id,content from " + HMetaDataDef.getTable(BusiTypeEnum.B2B_TC.getType(), "") + " where type=? and cust_id = ? and ext_4 = 1 ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, BusiTypeEnum.B2B_TC.getType(), custId);
        if (list == null || list.size() == 0) {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(list.get(0)));
        jsonObject.putAll(JSON.parseObject(String.valueOf(list.get(0).get("content"))));
        return jsonObject;
    }

    /**
     * @param custId
     * @param userId
     * @param seaType    1-公海 2-私海
     * @param mode       1-领取所选 2-指定数量
     * @param seaId      公海或私海ID
     * @param companyIds 企业Id集合
     * @param getNumber  领取数量
     * @param busiType
     * @param param
     * @return
     * @throws Exception
     */
    public int doClueDataToSea(String custId, long userId, int seaType, int mode, String seaId, List<String> companyIds, long getNumber, String busiType, JSONObject param) throws Exception {
        // 判断套餐余量
        long quantity = getB2BTcbQuantity(custId);
        if (quantity == 0) {
            throw new TouchException("套餐余量为0");
        }
        if ((companyIds != null && companyIds.size() > quantity) || getNumber > quantity) {
            throw new TouchException("套餐余量不足");
        }
        // 查询企业在使用的套餐包
        JSONObject useB2BTcb = getUseB2BTcb(custId);
        if (useB2BTcb == null) {
            throw new TouchException("企业无可用套餐包");
        }
        LocalDateTime eTime = LocalDateTime.parse(useB2BTcb.getString("e_time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (eTime.isBefore(LocalDateTime.now())) {
            throw new TouchException("企业套餐已过期");
        }

        Map<String, Object> superData = new HashMap(16) {{
            put("SYS007", "未跟进");
        }};

        CustomSeaTouchInfoDTO dto = null;
        BaseResult companyDetail = null, companyContact;
        JSONObject detailData = null, contactData = null;
        String entName = "", companyId = "";
        JSONObject log;
        Map<String, JSONObject> data = new HashMap(16);
        if (mode == 1) {
            for (String id : companyIds) {
                // 查询企业名称
                companyDetail = searchListService.getCompanyDetail(id, "", "1001");
                detailData = (JSONObject) companyDetail.getData();
                // 查询企业联系方式
                companyContact = searchListService.getCompanyDetail(id, "", "1039");
                contactData = (JSONObject) companyContact.getData();
                contactData.putAll(detailData);
                data.put(id, contactData);
            }
            // 指定数量
        } else if (mode == 2) {
            //领取，只返回id
            param.put("fieldType", false);
            long pageNo = 0L, pageSize = getNumber * 5;
            while (getNumber > data.size()) {
                param.put("pageNum", pageNo);
                param.put("pageSize", pageSize);
                BaseResult baseResult = searchListService.pageSearchIds(custId, "", userId, busiType, param);
                JSONObject resultData = (JSONObject) baseResult.getData();
                JSONArray list = resultData.getJSONArray("list");
                pageNo++;
                if (list == null || list.size() == 0) {
                    continue;
                }
                for (int i = 0; i < list.size(); i++) {
                    // 已经领取过不可重复领取
                    if (b2BTcbLogService.checkClueGetStatus(custId, list.getString(i))) {
                        continue;
                    }
                    if (getNumber > data.size()) {
                        companyContact = searchListService.getCompanyDetail(list.getString(i), "", "1039");
                        contactData = (JSONObject) companyContact.getData();
                        if (contactData == null || contactData.size() == 0) {
                            continue;
                        }
                        // 查询企业名称
                        companyDetail = searchListService.getCompanyDetail(list.getString(i), "", "1001");
                        detailData = (JSONObject) companyDetail.getData();
                        contactData.putAll(detailData);
                        data.put(list.getString(i), contactData);
                    } else {
                        break;
                    }
                }
            }
        }
        if (data.size() == 0) {
            throw new TouchException("未查询到匹配企业数据");
        }

        String batchId = UUID.randomUUID().toString().replaceAll("-", "");
        Iterator keys = data.entrySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONArray pNumbers = data.get(key).getJSONArray("phoneNumber");
            if (pNumbers != null) {
                for (int i = 0; i < pNumbers.size(); i++) {
                    dto = new CustomSeaTouchInfoDTO("", custId, String.valueOf(userId), "", "",
                            "", "", "", pNumbers.getString(i),
                            "", "", "",
                            seaId, superData, "", "", "", "",
                            "", "", entName);
                    dto.setEntId(key);
                    dto.setRegLocation(detailData.getString("regLocation"));
                    dto.setRegCapital(detailData.getString("regCap"));
                    dto.setRegStatus(detailData.getString("entStatus"));
                    dto.setRegTime(detailData.getString("fromTime"));
                    dto.setEntPersonNum(pNumbers.size());
                    // 保存线索
                    int status = seaService.addClueData0(dto, seaType);
                    log = new JSONObject();
                    // B2B数据企业ID
                    log.put("ext_1", companyId);
                    // 套餐包ID 扩展字段2
                    log.put("tcbId", useB2BTcb.getString("id"));
                    // 领取批次ID 扩展字段3
                    log.put("batchId", batchId);
                    // 线索ID 扩展字段4
                    log.put("superId", dto.getSuper_id());
                    log.put("content", JSON.toJSON(dto));
                    busiEntityService.saveInfo(custId, "", userId, BusiTypeEnum.B2B_TC_LOG.getType(), 0L, log);
                }
            }

        }
        return 0;
    }

}

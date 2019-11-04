package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub
    }

    /**
     * 获取企业B2B套餐包剩余量(只有1个套餐包有效)
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

}

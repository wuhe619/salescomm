package com.bdaim.api.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
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
import java.util.List;

/***
 * api批量测试任务
 */
@Service("busi_b_test_task")
@Transactional
public class BatchTestTaskZService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(BatchTestTaskZService.class);

    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private CustomerUserDao customerUserDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;


    @Autowired
    private ServiceUtils serviceUtils;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        busiType = BusiTypeEnum.BATCH_TEST_TASK.getType();
        String sql1 = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, ext_2,ext_3, ext_4 ) value(?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)";
        String batchId = info.getString("bill_no");
        String batchName = info.getString("batch_name");
        //查询批次详情数据
        String detailStr = info.getString("details");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 0);
        if (StringUtil.isNotEmpty(detailStr)) {
            String details[] = detailStr.split(",");
            if (details.length > 0) {
                for (int i = 0; i < details.length; i++) {
                    String detail = details[i];
                    id = sequenceService.getSeq(busiType);
                    jsonObject.put("detailId", detail);
                    jsonObject.put("custId", cust_id);
                    jsonObject.put("batchId", batchId);
                    jdbcTemplate.update(sql1, id, busiType, jsonObject.toJSONString(), cust_id, cust_group_id, cust_user_id, cust_user_id, 0, detail, batchId);
                }
            }
            //构造住批次信息
            //根据企业id查询企业账号和企业名称
            String enterpriseName = customerDao.getEnterpriseName(cust_id);
            CustomerUser custUser = customerUserDao.selectPropertyByType(1,cust_id );
            if (custUser!=null){
                info.put("account", custUser.getAccount());
            }
            info.put("custName", enterpriseName);
            //批次名称
            info.put("ext_5", batchName);
            //批次状态
            info.put("status", 0);
            //批次id
            info.put("ext_3", batchId);
            //核验成功数量
            info.put("successNum", 0);
            info.put("create_date",System.currentTimeMillis());
        }
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {
        HBusiDataManager manager = serviceUtils.getObjectByIdAndType(cust_id, id, busiType);
        if (manager == null) {
            throw new TouchException("无权操作");
        }
        if ("Y".equals(manager.getExt_1()) || "Y".equals(manager.getExt_2())) {
            throw new TouchException("已经被提交，无法删除");
        }

        List<HBusiDataManager> list = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), id);
        for (HBusiDataManager hBusiDataManager : list) {
            List<HBusiDataManager> slist = serviceUtils.getDataList(BusiTypeEnum.SS.getType(), hBusiDataManager.getId().longValue());//所有税单
            for (HBusiDataManager shBusiDataManager : slist) {
                serviceUtils.deleteDatafromES(BusiTypeEnum.SS.getType(), shBusiDataManager.getId().toString());
            }
            serviceUtils.deleteDatafromES(BusiTypeEnum.SF.getType(), hBusiDataManager.getId().toString());
            serviceUtils.delDataListByPid(BusiTypeEnum.SS.getType(), hBusiDataManager.getId().longValue());
        }
        serviceUtils.delDataListByPid(BusiTypeEnum.SF.getType(), id);

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }



}

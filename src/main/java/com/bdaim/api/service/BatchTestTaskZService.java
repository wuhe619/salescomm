package com.bdaim.api.service;

import com.alibaba.fastjson.JSON;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * api批量测试任务-批次信息
 */
@Service("busi_b_test_task_z")
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
        //busiType = BusiTypeEnum.BATCH_TEST_TASK.getType();
        String sql1 = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_user_id, create_id, create_date, ext_2,ext_3, ext_4 ) value(?, ?, ?, ?, ?, ?, now(), ?, ?, ?)";
        String batchName = info.getString("batch_name");
        String apiId = info.getString("api_id");
        String taskId = info.getString("task_id");
        Integer number = info.getInteger("number");

        //查询task表中的数据并对usedNum进行重新计算
        String sql = " select content from "+HMetaDataDef.getTable(BusiTypeEnum.BATCH_TEST_TASK.getType(), "")+" where id=?";
        Map<String,Object> taskObj = jdbcTemplate.queryForMap(sql,taskId);
        if(taskObj != null){
            String contentStr = (String) taskObj.get("content");
            if(StringUtil.isNotEmpty(contentStr)){
                JSONObject json = JSON.parseObject(contentStr);
                Integer limitNum = json.getInteger("limitNum");
                Integer usedNum = json.getInteger("usedNum");
                usedNum += number;
                if(usedNum>limitNum){
                    throw new Exception("批量测试数量已超过受限数量");
                }
                json.put("usedNum",usedNum);
                String updateSql = "update "+HMetaDataDef.getTable(BusiTypeEnum.BATCH_TEST_TASK.getType(), "") +" set content=? where id=?";
                jdbcTemplate.update(updateSql,json.toJSONString(),taskId);
            }else{
                throw new Exception("批量测试任务参数错误");
            }
        }else{
            throw new Exception("批量测试任务不存在");
        }

        //批次详情数据
        String detailStr = info.getString("details");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 0);
        if (StringUtil.isNotEmpty(detailStr)) {
            String details[] = detailStr.split(",");
            if (details.length > 0) {
                for (int i = 0; i < details.length; i++) {
                    String detail = details[i];
                    Long xid = sequenceService.getSeq(BusiTypeEnum.BATCH_TEST_TASK_X.getType());
                    jsonObject.put("detail", detail);
                    jsonObject.put("custId", cust_id);
                    jsonObject.put("batchId", id);
                    jsonObject.put("apiId",apiId);
                    jdbcTemplate.update(sql1, xid, BusiTypeEnum.BATCH_TEST_TASK_X.getType(), jsonObject.toJSONString(), cust_id, cust_user_id, cust_user_id, 0, apiId,id);
                }
            }

            //构造主批次信息
            //根据企业id查询企业账号和企业名称
            String enterpriseName = customerDao.getEnterpriseName(cust_id);
            CustomerUser custUser = customerUserDao.selectPropertyByType(1,cust_id );
            if (custUser!=null){
                info.put("account", custUser.getAccount());
            }
            info.put("custName", enterpriseName);
            //批次名称
            info.put("ext_5", batchName);
            info.put("totalNum",number);
            //批次状态
            info.put("status", 0);// 0：处理中 1:处理完成
            info.put("ext_2",apiId);
            info.put("taskId",taskId);
            info.put("ext_1",taskId);
            //核验成功数量
            info.put("successNum", 0);
            info.put("failedNum",0);
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
        sqlParams.clear();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3," +
                " ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=? ");
        sqlParams.add(busiType);
        if (!"all".equals(cust_id)){
            sqlParams.add(cust_id);
            sqlstr.append(" and cust_id=? ");
        }
        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = String.valueOf(params.get(key));
            if (StringUtil.isEmpty(value)) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key))
                continue;
            if ("cust_id".equals(key)) {
                sqlstr.append(" and cust_id=?");
                sqlParams.add(value);
            }else if("task_id".equals(key)){
                sqlstr.append("and ext_1=?");
                sqlParams.add(value);
            }else if("batch_id".equals(key)){
                sqlstr.append("and ext_3=?");
                sqlParams.add(value);
            }else if("batch_name".equals(key)){
                sqlstr.append("and ext_5 like ?");
                sqlParams.add("%"+value+"%");
            }
            //sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key + "')=?");

        }
        return sqlstr.toString();
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }



}

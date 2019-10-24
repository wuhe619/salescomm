package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.dto.XzCompanyCallcenterParam;
import com.bdaim.callcenter.util.XzCallCenterUtil;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customer.dao.CustomerCallCenterAccountDao;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerCallCenterAccount;
import com.bdaim.util.DateUtil;
import com.bdaim.util.StringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 */
@Service("customerCallCenterAccountService")
@Transactional
public class CustomerCallCenterAccountService {
    private static Logger logger = Logger.getLogger(CustomerCallCenterAccountService.class);

    @Resource
    private CustomerCallCenterAccountDao customerCallCenterAccountDao;

    @Resource
    private CustomerDao customerDao;

    /**
     * 分页查询
     * @param customerName
     * @param customerUserName
     * @param callCenterId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public JSONObject page(String customerName, String customerUserName, String callCenterId, Integer pageNum, Integer pageSize){
        JSONObject jsonObject = new JSONObject();
        String sql=" select a.*,b.enterprise_name,c.account from t_customer_callcenter a left join t_customer b on a.cust_id=b.cust_id left join t_customer_user c on a.cust_id=c.cust_id and c.user_type=1 where 1=1 ";
        if(StringUtil.isNotEmpty(customerName)){
            sql+= " and b.enterprise_name = '"+customerName+"'";
        }
        if(StringUtil.isNotEmpty(customerUserName)){
            sql+=" and c.account='"+customerUserName+"'";
        }
        if(StringUtil.isNotEmpty(callCenterId)){
            sql+= " and a.id = "+callCenterId;
        }
        sql+=" order by a.create_time desc ";
        String countSql="select count(0) from ("+sql+")a";
        String total = customerCallCenterAccountDao.queryForObject(countSql);
        if(StringUtil.isNotEmpty(total) && Integer.valueOf(total)>0){
            sql+=" limit "+pageNum+","+pageSize;
        }else{
            jsonObject.put("total",0);
            return jsonObject;
        }
        List<Map<String, Object>> accounts = customerCallCenterAccountDao.queryListBySql(sql);
        jsonObject.put("data",accounts);
        return jsonObject;
    }

    /**
     * 保存/编辑
     * @param customerCallCenterAccount
     * @param lu
     * @throws TouchException
     */
    public void save(CustomerCallCenterAccount customerCallCenterAccount, LoginUser lu) throws Exception {

        if (customerCallCenterAccount.getId() != null) {
            CustomerCallCenterAccount dbobj = customerCallCenterAccountDao.get(customerCallCenterAccount.getId());
            if(dbobj==null || dbobj.getStatus()==2){
                throw new TouchException("数据不存在或已删除");
            }
            if(customerCallCenterAccount.getAuthorizedSeats()!=null){
                dbobj.setAuthorizedSeats(customerCallCenterAccount.getAuthorizedSeats());
            }
            if(customerCallCenterAccount.getMaxOccurs()!=null){
                dbobj.setMaxOccurs(customerCallCenterAccount.getMaxOccurs());
            }
            if(customerCallCenterAccount.getStatus()!=null){
                dbobj.setStatus(customerCallCenterAccount.getStatus());
            }
            dbobj.setLastUpdateUser(lu.getId().toString());
            dbobj.setLastUpdateTime(new Date());

            XzCompanyCallcenterParam param = openAccountRequestParam(dbobj);
            JSONObject json = XzCallCenterUtil.updateCompanytoXzCallCenter(param);
            logger.info("updateCompanytoXzCallCenter.json:"+json.toJSONString());
            if (json != null && "0".equals(json.getString("code"))) {
                customerCallCenterAccountDao.save(dbobj);
            }else{
                logger.info("updateCompanytoXzCallCenter failed ");
            }
        } else {
            List<CustomerCallCenterAccount> list = customerCallCenterAccountDao.findBy("custId",customerCallCenterAccount.getCustId());
            if(list!=null && list.size()>0){
                throw new TouchException("企业 "+customerCallCenterAccount.getCustomerName()+" 已开通过账号");
            }
            Date d = new Date();
            customerCallCenterAccount.setCreateTime(d);
            customerCallCenterAccount.setLastUpdateTime(d);
            //Long id = IDHelper.getID();
            //customerCallCenterAccount.setId(id);
//            customerCallCenterAccount.setCallCenterId(IDHelper.getOrderNoByAtomic(""));
            customerCallCenterAccount.setCreateUser(lu.getId().toString());
            customerCallCenterAccount.setLastUpdateUser(lu.getId().toString());
            customerCallCenterAccountDao.save(customerCallCenterAccount);
            XzCompanyCallcenterParam param = openAccountRequestParam(customerCallCenterAccount);
            try {
                JSONObject json = XzCallCenterUtil.addCompanytoXzCallCenter(param);
                logger.info("addCompanytoXzCallCenter.json:" + json.toJSONString());
                if (json != null && "0".equals(json.getString("code"))) {
                    logger.info("addCompanytoXzCallCenter success");
                } else {
                    logger.info("addCompanytoXzCallCenter failed");
                    customerCallCenterAccountDao.delete(customerCallCenterAccount.getId());
                }
            }catch (Exception e){
                logger.error("addCompanytoXzCallCenter error : "+e.getMessage());
                customerCallCenterAccountDao.delete(customerCallCenterAccount.getId());
            }
        }
    }


    /**
     * 拼装参数
     * @param dbobj
     * @return
     */
    private XzCompanyCallcenterParam openAccountRequestParam(CustomerCallCenterAccount dbobj){
        XzCompanyCallcenterParam param = new XzCompanyCallcenterParam();
        param.setAmountagentauth(dbobj.getAuthorizedSeats());
        param.setBegintime(DateUtil.fmtDateToStr(dbobj.getStartTime(),"yyyy-MM-dd"));
        param.setEndtime(DateUtil.fmtDateToStr(dbobj.getEndTime(),"yyyy-MM-dd"));
        Customer customer = customerDao.get(dbobj.getCustId());
        param.setCompanyname(customer.getEnterpriseName());
        param.setCompid(dbobj.getId().toString());
        param.setEnable(dbobj.getStatus());
        param.setExpirerecord(6);
        return param;
    }

    /**
     * 返回详情
     * @param id
     * @return
     */
    public CustomerCallCenterAccount getDetailById(Long id){
        return customerCallCenterAccountDao.get(id);
    }
}

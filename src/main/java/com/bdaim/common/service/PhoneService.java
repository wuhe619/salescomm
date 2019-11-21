package com.bdaim.common.service;

import com.bdaim.callcenter.dto.XzPullPhoneDTO;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.dao.CustomerGroupListDao;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.util.ConstantsUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.bdaim.util.http.HttpUtil;
import com.bdaim.util.redis.RedisUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2019/3/28
 * @description
 */
@Service("phoneService")
@Transactional
public class PhoneService {

    private static final Logger LOG = LoggerFactory.getLogger(PhoneService.class);

    private static final String PHONE_SQL = "SELECT phone from u WHERE id=?";

    private static final int CG_PHONE_KEY_EXPIRE = 60 * 60 * 24;

    private static final String CG_PHONE_KEY_SPLIT = ":";

    @Resource
    private CustomGroupDao customGroupDao;

    @Resource
    private MarketTaskDao marketTaskDao;

    @Resource
    private CustomerSeaDao customerSeaDao;

    @Resource
    private RedisUtil redisUtil;


    /**
     * 根据身份ID获取手机号
     *
     * @param id
     * @return
     */
    public String getPhoneBySuperId(String id) {
        String phone = getValueByIdFromRedis(id);
        if (StringUtil.isEmpty(phone)) {
            List<Map<String, Object>> phoneList = this.customGroupDao.sqlQuery(PHONE_SQL, id);
            if (phoneList.size() > 0) {
                phone = String.valueOf(phoneList.get(0).get("phone"));
            }
        }
        return phone;
    }

    /**
     * 获取身份ID
     *
     * @param customerGroupId
     * @param phone
     * @return
     */
    public String getSuperIdByPhone(String customerGroupId, String phone) {
        String superId = getValueByIdFromRedis(customerGroupId + CG_PHONE_KEY_SPLIT + phone);
        if (StringUtil.isEmpty(superId)) {
            List<Map<String, Object>> superIdList = customGroupDao.sqlQuery("SELECT id FROM u WHERE (phone = ? OR phone = ?) ", phone, ConstantsUtil.BLACK_PHONE_PREFIX + phone);
            if (superIdList.size() > 0) {
                List<Map<String, Object>> superIds;
                for (Map<String, Object> superIdMap : superIdList) {
                    try {
                        superIds = customGroupDao.sqlQuery("SELECT id FROM t_customer_group_list_" + customerGroupId + " WHERE id = ?", superIdMap.get("id"));
                        if (superIds.size() > 0) {
                            superId = String.valueOf(superIds.get(0).get("id"));
                            LOG.info("客户群:" + customerGroupId + "获取到的superId:" + superId);
                            return superId;
                        }
                    } catch (Exception e) {
                        LOG.error("根据手机号和客户群ID获取身份ID失败,", e);
                        continue;
                    }
                }
            }
        }
        return superId;
    }

    /**
     * 根据公海ID和手机号获取身份ID
     *
     * @param seaId
     * @param phone
     * @return
     */
    public String getSeaSuperIdByPhone(String seaId, String phone) {
        String superId = getValueByIdFromRedis(seaId + CG_PHONE_KEY_SPLIT + phone);
        return superId;
    }

    /**
     * 批量根据ID获取手机号
     *
     * @param ids
     * @return
     */
    public Map<String, Object> getPhoneMap(Set<String> ids) {
        // 查询手机号
        Map<String, Object> phoneMap = new HashMap<>();
        if (ids.size() > 0) {
            for (String u : ids) {
                // phoneMap.put(u, getPhoneBySuperId(u));
                phoneMap.put(u, getValueByIdFromRedis(u));
            }
        }
        return phoneMap;
    }

    /**
     * 从redis中通过id获取手机号
     *
     * @param id
     * @return
     */
    private String getValueByIdFromRedis(String id) {
        String phone = redisUtil.get(id);
        return phone;
    }

    /**
     * 保存客群和手机号对应的身份ID到redis
     *
     * @param customerGroupId 客群ID
     * @param superId         身份ID
     * @param phone           手机号
     * @return
     */
    public void setCGroupDataToRedis(String customerGroupId, String superId, String phone) {
        redisUtil.setex(customerGroupId + CG_PHONE_KEY_SPLIT + phone, CG_PHONE_KEY_EXPIRE, superId);
    }

    /**
     * 保存数据
     *
     * @param key
     * @param value
     * @return
     */
    public void setValueByIdFromRedis(String key, String value) {
        redisUtil.set(key, value);
    }

    /**
     * 隐藏手机号中间4位
     *
     * @param phone
     * @return
     */
    public String hidePhone(String phone) {
        String phoneNumber = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        if (StringUtil.isEmpty(phoneNumber)) {
            phoneNumber = "";
        }
        return phoneNumber;
    }

    /**
     * 保存手机号至API服务
     *
     * @param phone
     */
    public String savePhoneToAPI(String phone) {
        if (StringUtil.isEmpty(phone)) {
            LOG.warn("保存手机号至API服务手机号不能为空:{}", phone);
            return null;
        }
        String uid = null;
        try {
            uid = HttpUtil.httpPost("http://api.core:1010/pn/pnu?pn=" + phone.trim().replaceAll(" ", ""), "", null);
        } catch (Exception e) {
            LOG.error("保存手机号至API服务手机号:{}异常:{}", phone, e);
        }
        return uid;
    }

    /**
     * 获取手机号从API服务
     *
     * @param uid
     * @return
     */
    public String getPhoneFromAPI(String uid) {
        if (StringUtil.isEmpty(uid)) {
            LOG.warn("获取手机号API服务uid不能为空:{}", uid);
            return null;
        }
        String phone = null;
        try {
            phone = HttpUtil.httpPost("http://api.core:1010/pn/upn?uid=" + uid.trim().replaceAll(" ", ""), "", null);
        } catch (Exception e) {
            LOG.error("保存手机号至API服务uid:{}异常:{}", uid, e);
        }
        return phone;
    }

    /**
     * 批量保存手机号,返回uid
     *
     * @param map
     * @return
     */
    public Map<String, String> batchSavePhone(Map<String, String> map) {
        Map<String, String> data = new HashMap<>();
        String uid;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            uid = savePhoneToAPI(entry.getValue());
            data.put(entry.getValue(), uid);
        }
        return data;
    }

    /**
     * 根据第三方任务ID拉取号码
     *
     * @param type     1-公海 2-客群 3-营销任务
     * @param taskId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<XzPullPhoneDTO> pagePhoneByTaskId(int type, String taskId, Integer pageNum, Integer pageSize) {
        LOG.info("拉取号码任务ID:" + taskId + ",拉取手机号,pageNum:" + pageNum + ",pageSize:" + pageSize);
        String sId = "";
        int phoneIndex = -1;
        String table = "", cgId = "";
        CustomerSea customerSea = null;
        CustomGroup customGroup = null;
        MarketTask marketTask = null;
        // 公海
        if (type == 1) {
            customerSea = customerSeaDao.getCustomerSeaByTaskId(taskId);
            if (customerSea != null) {
                sId = customerSea.getId().toString();
                if (customerSea.getTaskPhoneIndex() != null) {
                    phoneIndex = customerSea.getTaskPhoneIndex();
                }
                table = ConstantsUtil.SEA_TABLE_PREFIX;
            }
        } else if (type == 2) {
            // 客群
            customGroup = customGroupDao.getCustomGroupByTaskId(taskId);
            if (customGroup != null) {
                sId = customGroup.getId().toString();
                cgId = sId;
                if (customGroup.getTaskPhoneIndex() != null) {
                    phoneIndex = customGroup.getTaskPhoneIndex();
                }
                table = ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX;
            }
        } else if (type == 3) {
            // 营销任务
            marketTask = marketTaskDao.getMarketTaskByTaskId(taskId);
            if (marketTask != null) {
                cgId = marketTask.getCustomerGroupId().toString();
                sId = marketTask.getId();
                if (marketTask.getTaskPhoneIndex() != null) {
                    phoneIndex = marketTask.getTaskPhoneIndex();
                }
                table = ConstantsUtil.MARKET_TASK_TABLE_PREFIX;
            }
        } else {
            LOG.warn("拉取号码无对应类型:{}", type);
            return new ArrayList<>();
        }
        if (StringUtil.isEmpty(sId) || StringUtil.isEmpty(table)) {
            LOG.warn("拉取号码未查询到记录type:{},sId:{}", type, sId);
            return new ArrayList<>();
        }
        LOG.info("拉取号码:" + sId + ",phoneIndex:" + phoneIndex);
        StringBuffer sb = new StringBuffer();
        sb.append("select custG.id, custG.batch_id from ").append(table).append(sId).append(" custG ORDER BY custG.n_id ASC ");
        // 如果记录的号码index大于拉取的index,则从记录号码的index开始拉取,防止重复拨打
        if (phoneIndex > pageNum) {
            LOG.warn("拉取号码:" + sId + ",记录的index:" + phoneIndex + ",拉取的index:" + pageNum);
            pageNum = phoneIndex;
        }
        sb.append(" LIMIT " + pageNum + "," + pageSize);

        List<Map<String, Object>> ids;
        List<XzPullPhoneDTO> phoneList = new ArrayList<>();
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            if (ids == null || ids.size() == 0) {
                LOG.info("拉取号码:" + sId + ",手机号拉取完成,phoneIndex:" + phoneIndex);
                return phoneList;
            }
            phoneIndex += ids.size();
            String phone;
            for (Map<String, Object> id : ids) {
                if (id != null) {
                    if (type == 1) {
                        cgId = String.valueOf(id.get("batch_id"));
                    }
                    phone = getPhoneBySuperId(String.valueOf(id.get("id")));
                    phoneList.add(new XzPullPhoneDTO(phone, String.valueOf(id.get("id"))));
                    //保存客群和手机号对应的身份ID到redis
                    setCGroupDataToRedis(cgId, String.valueOf(id.get("id")), phone);
                    if (type == 1) {
                        setCGroupDataToRedis(String.valueOf(customerSea.getId()), String.valueOf(id.get("id")), phone);
                    }
                }
            }
            if (type == 1) {
                customerSea.setTaskPhoneIndex(phoneIndex);
                customerSeaDao.update(customerSea);
            } else if (type == 2) {
                // 客群
                customGroup.setTaskPhoneIndex(phoneIndex);
                customGroupDao.update(customGroup);
            } else if (type == 3) {
                // 营销任务
                marketTask.setTaskPhoneIndex(phoneIndex);
                marketTaskDao.update(marketTask);
            }
        } catch (Exception e) {
            LOG.error(sId + "拉取手机号失败,", e);
            phoneList = new ArrayList<>();
        }
        return phoneList;
    }

    /**
     * 根据ID拉取号码
     *
     * @param type     1-公海 2-客群 3-营销任务
     * @param cId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<XzPullPhoneDTO> pagePhoneById(int type, String cId, Integer pageNum, Integer pageSize) {
        LOG.info("拉取号码任务ID:" + cId + ",拉取手机号,pageNum:" + pageNum + ",pageSize:" + pageSize);
        String sId = "";
        int phoneIndex = -1;
        String table = "", cgId = "";
        CustomerSea customerSea = null;
        CustomGroup customGroup = null;
        MarketTask marketTask = null;
        // 公海
        if (type == 1) {
            customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(cId));
            if (customerSea != null) {
                sId = customerSea.getId().toString();
                if (customerSea.getTaskPhoneIndex() != null) {
                    phoneIndex = customerSea.getTaskPhoneIndex();
                }
                table = ConstantsUtil.SEA_TABLE_PREFIX;
            }
        } else if (type == 2) {
            // 客群
            customGroup = customGroupDao.get(NumberConvertUtil.parseInt(cId));
            if (customGroup != null) {
                sId = customGroup.getId().toString();
                cgId = sId;
                if (customGroup.getTaskPhoneIndex() != null) {
                    phoneIndex = customGroup.getTaskPhoneIndex();
                }
                table = ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX;
            }
        } else if (type == 3) {
            // 营销任务
            marketTask = marketTaskDao.get(cId);
            if (marketTask != null) {
                cgId = marketTask.getCustomerGroupId().toString();
                sId = marketTask.getId();
                if (marketTask.getTaskPhoneIndex() != null) {
                    phoneIndex = marketTask.getTaskPhoneIndex();
                }
                table = ConstantsUtil.MARKET_TASK_TABLE_PREFIX;
            }
        } else {
            LOG.warn("拉取号码无对应类型:{}", type);
            return new ArrayList<>();
        }
        if (StringUtil.isEmpty(sId) || StringUtil.isEmpty(table)) {
            LOG.warn("拉取号码未查询到记录type:{},sId:{}", type, sId);
            return new ArrayList<>();
        }
        LOG.info("拉取号码:" + sId + ",phoneIndex:" + phoneIndex);
        StringBuffer sb = new StringBuffer();
        sb.append("select custG.id, custG.batch_id from ").append(table).append(sId).append(" custG ORDER BY custG.n_id ASC ");
        // 如果记录的号码index大于拉取的index,则从记录号码的index开始拉取,防止重复拨打
        if (phoneIndex > pageNum) {
            LOG.warn("拉取号码:" + sId + ",记录的index:" + phoneIndex + ",拉取的index:" + pageNum);
            pageNum = phoneIndex;
        }
        sb.append(" LIMIT " + pageNum + "," + pageSize);

        List<Map<String, Object>> ids;
        List<XzPullPhoneDTO> phoneList = new ArrayList<>();
        try {
            ids = customGroupDao.sqlQuery(sb.toString());
            if (ids == null || ids.size() == 0) {
                LOG.info("拉取号码:" + sId + ",手机号拉取完成,phoneIndex:" + phoneIndex);
                return phoneList;
            }
            phoneIndex += ids.size();
            String phone;
            for (Map<String, Object> id : ids) {
                if (id != null) {
                    if (type == 1) {
                        cgId = String.valueOf(id.get("batch_id"));
                    }
                    phone = getPhoneBySuperId(String.valueOf(id.get("id")));
                    phoneList.add(new XzPullPhoneDTO(phone, String.valueOf(id.get("id"))));
                    //保存客群和手机号对应的身份ID到redis
                    setCGroupDataToRedis(cgId, String.valueOf(id.get("id")), phone);
                    if (type == 1) {
                        setCGroupDataToRedis(String.valueOf(customerSea.getId()), String.valueOf(id.get("id")), phone);
                    }
                }
            }
            if (type == 1) {
                customerSea.setTaskPhoneIndex(phoneIndex);
                customerSeaDao.update(customerSea);
            } else if (type == 2) {
                // 客群
                customGroup.setTaskPhoneIndex(phoneIndex);
                customGroupDao.update(customGroup);
            } else if (type == 3) {
                // 营销任务
                marketTask.setTaskPhoneIndex(phoneIndex);
                marketTaskDao.update(marketTask);
            }
        } catch (Exception e) {
            LOG.error(sId + "拉取手机号失败,", e);
            phoneList = new ArrayList<>();
        }
        return phoneList;
    }


    /*public static void main(String[] args) {
        PhoneService phoneService = new PhoneService();
        String result = phoneService.setCGroupDataToRedis("52", "-1", "18630016545");
        System.out.println(result);
        String superId = phoneService.getSuperIdByPhone("52", "18630016545");
        System.out.println(superId);
        String phone = phoneService.getPhoneBySuperId("-1");
        System.out.println(phone);
    }*/
}

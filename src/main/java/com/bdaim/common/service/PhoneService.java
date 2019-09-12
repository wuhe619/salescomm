package com.bdaim.common.service;

import com.bdaim.common.util.redis.RedisUtil;
import com.bdaim.common.util.ConstantsUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customgroup.dao.CustomGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

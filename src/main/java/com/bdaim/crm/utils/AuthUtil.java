package com.bdaim.crm.utils;

import cn.hutool.core.util.StrUtil;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmAdminMenuDao;
import com.bdaim.crm.dao.LkCrmAdminUserDao;
import com.bdaim.crm.erp.admin.service.AdminRoleService;
import com.bdaim.crm.erp.admin.service.AdminUserService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wyq
 */
@Service
@Transactional
public class AuthUtil {

    public static final Logger LOG = LoggerFactory.getLogger(AuthUtil.class);

    private static AdminUserService adminUserService;

    private static LkCrmAdminUserDao crmAdminUserDao;

    private static AdminRoleService adminRoleService;

    private static LkCrmAdminMenuDao crmAdminMenuDao;

    public static AdminUserService getAdminUserService() {
        return adminUserService;
    }

    @Resource
    public void setAdminUserService(AdminUserService adminUserService) {
        AuthUtil.adminUserService = adminUserService;
    }

    public LkCrmAdminUserDao getCrmAdminUserDao() {
        return crmAdminUserDao;
    }

    @Resource
    public void setCrmAdminUserDao(LkCrmAdminUserDao crmAdminUserDao) {
        AuthUtil.crmAdminUserDao = crmAdminUserDao;
    }

    public AdminRoleService getAdminRoleService() {
        return adminRoleService;
    }

    @Resource
    public void setAdminRoleService(AdminRoleService adminRoleService) {
        AuthUtil.adminRoleService = adminRoleService;
    }

    public LkCrmAdminMenuDao getCrmAdminMenuDao() {
        return crmAdminMenuDao;
    }

    @Resource
    public void setCrmAdminMenuDao(LkCrmAdminMenuDao crmAdminMenuDao) {
        AuthUtil.crmAdminMenuDao = crmAdminMenuDao;
    }

    public static Map<String, String> getCrmTablePara(String urlPrefix) {
        Map<String, String> tableParaMap = new HashMap<>();
        switch (urlPrefix) {
            case "CrmCustomer":
                tableParaMap.put("tableName", "lkcrm_crm_customer");
                tableParaMap.put("tableId", "customer_id");
                break;
            case "CrmLeads":
                tableParaMap.put("tableName", "lkcrm_crm_leads");
                tableParaMap.put("tableId", "leads_id");
                break;
            case "CrmContract":
                tableParaMap.put("tableName", "lkcrm_crm_contract");
                tableParaMap.put("tableId", "contract_id");
                break;
            case "CrmContacts":
                tableParaMap.put("tableName", "lkcrm_crm_contacts");
                tableParaMap.put("tableId", "contacts_id");
                break;
            case "CrmBusiness":
                tableParaMap.put("tableName", "lkcrm_crm_business");
                tableParaMap.put("tableId", "business_id");
                break;
            case "CrmReceivables":
                tableParaMap.put("tableName", "lkcrm_crm_receivables");
                tableParaMap.put("tableId", "receivables_id");
                break;
            case "t_customer_sea":
                tableParaMap.put("tableName", "t_customer_sea");
                tableParaMap.put("tableId", "id");
                break;
            default:
                return null;
        }
        return tableParaMap;
    }

    public static boolean isCrmAuth(Map<String, String> tablePara, Object id) {
        if (tablePara == null) {
            return false;
        }
        Long userId = BaseUtil.getUserId();
        List<Long> longs = adminUserService.queryUserByAuth(userId, null);
        StringBuilder authSql = new StringBuilder("select count(*) from ");
        authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ? ");
        if (longs != null && longs.size() > 0) {
            authSql.append(" and owner_user_id in (").append(StrUtil.join(",", longs)).append(")");
            if ("lkcrm_crm_customer".equals(tablePara.get("tableName")) || "lkcrm_crm_business".equals(tablePara.get("tableName")) || "lkcrm_crm_contract".equals(tablePara.get("tableName"))) {
                authSql.append(" or ro_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or rw_user_id like CONCAT('%,','").append(userId).append("',',%')");
            }
        }
        LOG.info("authSql:{},id:{}", authSql.toString(), id);
        return crmAdminUserDao.queryForInt(authSql.toString(),id) == 0;
    }

    public static boolean oaAnth(Record record) {
        boolean auth = false;
        if (record.getStr("business_ids") != null) {
            auth = isCrmAuth(getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), Integer.valueOf(record.getStr("business_ids")));
        } else if (record.getStr("contacts_ids") != null) {
            auth = isCrmAuth(getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()), Integer.valueOf(record.getStr("contacts_ids")));
        } else if (record.getStr("contract_ids") != null) {
            auth = isCrmAuth(getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), Integer.valueOf(record.getStr("contract_ids")));
        } else if (record.getStr("customer_ids") != null) {
            auth = isCrmAuth(getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), Integer.valueOf(record.getStr("customer_ids")));
        }
        return auth;
    }

    public static boolean isOaAuth(Integer type, Integer id) {
        if (id == null) {
            return false;
        }
        Map<String, String> tablePara = getOaTablePara(type);
        Long userId = BaseUtil.getUserId();
        List<Integer> roleIds = adminRoleService.queryRoleIdsByUserId(userId);

        //超级管理员角色拥有最高权限
        if (roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID) || userId.equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return false;
        }
        if (tablePara == null) {
            List<Long> userIds = crmAdminUserDao.queryListBySql("select create_user_id from `lkcrm_oa_examine` where examine_id = ? union all select b.examine_user from `lkcrm_oa_examine_record` as a left join `lkcrm_oa_examine_log` b on a.record_id = b.record_id where a.examine_id = ?", id, id);
            return !userIds.contains(userId);
        }
        StringBuilder authSql = new StringBuilder("select count(*) from  ");
        if (!"lkcrm_task".equals(tablePara.get("tableName"))) {
            authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id).append(" and create_user_id = ").append(userId);
        } else {
            List<Long> childIdList = adminUserService.queryChileUserIds(userId, 20);
            authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id);
            if (childIdList != null && childIdList.size() > 0) {
                authSql.append(" and (").append(" (owner_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or main_user_id = ").append(userId).append(") ");
                childIdList.forEach(childId -> {
                    authSql.append(" or (owner_user_id like CONCAT('%,','").append(childId).append("',',%')").append(" or main_user_id = ").append(childId).append(") ");
                });
                authSql.append(") ");
            } else {
                authSql.append(" and ").append(" (owner_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or main_user_id = ").append(userId).append(") ");
            }
        }
        return crmAdminUserDao.queryForInt(authSql.toString()) == 0;
    }

    private static Map<String, String> getOaTablePara(Integer type) {
        Map<String, String> tableParaMap = new HashMap<>();
        switch (type) {
            case 1:
                tableParaMap.put("tableName", "lkcrm_oa_log");
                tableParaMap.put("tableId", "log_id");
                break;
            case 2:
                tableParaMap.put("tableName", "lkcrm_oa_event");
                tableParaMap.put("tableId", "event_id");
                break;
            case 3:
                tableParaMap.put("tableName", "lkcrm_oa_announcement");
                tableParaMap.put("tableId", "announcement_id");
                break;
            case 4:
                tableParaMap.put("tableName", "lkcrm_task");
                tableParaMap.put("tableId", "task_id");
                break;
            case 5:
                tableParaMap.put("tableName", "lkcrm_oa_examine");
                tableParaMap.put("tableId", "examine_id");
                break;
            default:
                return null;
        }
        return tableParaMap;
    }


    public static boolean isWorkAdmin() {
        if (BaseUtil.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return true;
        } else {
            return BaseUtil.getUser().getRoles().contains(BaseConstant.WORK_ADMIN_ROLE_ID) || BaseUtil.getUser().getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID);
        }
    }

    public static boolean isWorkAuth(String workId, String realm) {
        LoginUser user = BaseUtil.getUser();
        Integer roleId = crmAdminUserDao.queryForInt("select role_id from `lkcrm_work_user` where work_id = ? and user_id = ?", workId, user.getUserId());
        //判断是否是超级管理员、项目管理员
        if (user.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID) || user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID) || user.getRoles().contains(BaseConstant.WORK_ADMIN_ROLE_ID) || (roleId != null && roleId.equals(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID))) {
            return true;
        } else {
            List<String> realmData = crmAdminMenuDao.queryRealmByRoleId(roleId);
            if (realmData == null || realmData.size() == 0) {
                return false;
            }
            return realmData.contains(realm);
        }
    }

}

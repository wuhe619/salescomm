package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminExamineRecordService {

    @Resource
    private LkCrmAdminRecordDao crmAdminRecordDao;
    @Resource
    private LkCrmAdminExamineLogDao crmAdminExamineLogDao;
    @Resource
    private LkCrmAdminExamineRecordDao crmAdminExamineRecordDao;
    @Resource
    private LkCrmAdminExamineStepDao crmAdminExamineStepDao;
    @Resource
    private LkCrmAdminExamineDao crmAdminExamineDao;
    @Resource
    private LkCrmContactsDao crmContactsDao;
    @Resource
    private LkCrmReceivablesDao crmReceivablesDao;
    @Resource
    private LkCrmContractDao crmContractDao;
    @Resource
    private LkCrmCustomerDao crmCustomerDao;

    /**
     * 第一次添加审核记录和审核日志 type 1 合同 2 回款 userId:授权审批人
     */
    public Map<String, Integer> saveExamineRecord(Integer type, Long userId, Long ownerUserId, Integer recordId) {
        LoginUser user = BaseUtil.getUser();
        Map<String, Integer> map = new HashMap<>();
        //创建审核记录
        LkCrmAdminExamineRecordEntity examineRecord = new LkCrmAdminExamineRecordEntity();
        if (recordId != null) {
            examineRecord = crmAdminExamineRecordDao.get(recordId);
            crmAdminExamineLogDao.updateExamineLogIsRecheckByRecordId(recordId);
            //Db.update(Db.getSql("admin.examineLog.updateExamineLogIsRecheckByRecordId"), recordId);
        } else {
            examineRecord.setCreateTime(DateUtil.date().toTimestamp());
            examineRecord.setCreateUser(user.getUserId());
        }
        //创建审核日志
        LkCrmAdminExamineLogEntity examineLog = new LkCrmAdminExamineLogEntity();
        examineRecord.setExamineStatus(0);
        examineLog.setCreateTime(DateUtil.date().toTimestamp());
        examineLog.setCreateUser(user.getUserId());
        examineLog.setExamineStatus(0);
        examineLog.setOrderId(1);
        //根据type查询当前启用审批流程
        LkCrmAdminExamineEntity examine = crmAdminExamineRecordDao.getExamineByCategoryType(type);
        //LkCrmAdminExamineEntity examine = AdminExamine.dao.findFirst(Db.getSql("admin.examine.getExamineByCategoryType"), type);
        if (examine == null) {
            map.put("status", 0);
        } else {
            examineRecord.setExamineId(examine.getExamineId());
            //先判断该审批流程是否为固定审批
            if (examine.getExamineType() == 1) {
                //固定审批
                //先查询该审批流程的审批步骤的第一步
                LkCrmAdminExamineStepEntity examineStep = crmAdminExamineStepDao.queryExamineStepByExamineIdOrderByStepNum(examine.getExamineId());
                examineRecord.setExamineStepId(examineStep.getStepId());
                examineLog.setExamineStepId(examineStep.getStepId());
                if (recordId == null) {
                    int rId = (int) crmAdminExamineRecordDao.saveReturnPk(examineRecord);
                    examineRecord.setRecordId(rId);
                } else {
                    crmAdminExamineRecordDao.update(examineRecord);
                }

                if (examineStep.getStepType() == 2 || examineStep.getStepType() == 3) {
                    String[] userIds = examineStep.getCheckUserId().split(",");
                    for (String id : userIds) {
                        if (StrUtil.isNotEmpty(id)) {
                            //examineLog.setLogId(null);
                            examineLog.setExamineUser(Long.valueOf(id));
                            examineLog.setRecordId(examineRecord.getRecordId());
                            examineLog.setIsRecheck(0);
                            Long logId = (Long) crmAdminExamineLogDao.saveReturnPk(examineLog);
                        }
                    }
                } else if (examineStep.getStepType() == 1) {
                    //如果是负责人主管审批 获取主管ID
                    Record r = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryUserByUserId(ownerUserId).get(0));
                    if (r == null || r.getLong("user_id") == null) {
                        examineLog.setExamineUser(BaseUtil.getAdminUserId());
                    } else {
                        examineLog.setExamineUser(r.getLong("user_id"));
                    }
                    examineLog.setRecordId(examineRecord.getRecordId());
                    examineLog.setIsRecheck(0);
                    //examineLog.save();
                    crmAdminExamineLogDao.save(examineLog);
                } else {
                    //如果是负责人主管审批 获取主管的主管ID
                    Record r = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryUserByUserId(JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryUserByUserId(ownerUserId).get(0)).getLong("user_id")).get(0));
                    //Record r = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), ownerUserId).getLong("user_id"));
                    if (r == null || r.getLong("user_id") == null) {
                        examineLog.setExamineUser(BaseUtil.getAdminUserId());
                    } else {
                        examineLog.setExamineUser(r.getLong("user_id"));
                    }
                    examineLog.setRecordId(examineRecord.getRecordId());
                    examineLog.setIsRecheck(0);
                    //examineLog.save();
                    crmAdminExamineLogDao.save(examineLog);
                }

            } else {
                //授权审批
                examineLog.setExamineUser(userId);
                if (recordId == null) {
                    //examineRecord.save();
                    Integer rId = (Integer) crmAdminExamineRecordDao.saveReturnPk(examineRecord);
                    examineRecord.setRecordId(rId);
                } else {
                    crmAdminExamineRecordDao.update(examineRecord);
                    //examineRecord.update();
                }
                examineLog.setRecordId(examineRecord.getRecordId());
                examineLog.setIsRecheck(0);
                // examineLog.save();
                crmAdminExamineLogDao.save(examineLog);
            }

            map.put("status", 1);
            map.put("id", examineRecord.getRecordId());
        }
        return map;
    }

    /**
     * 审核合同或者回款 recordId:审核记录id status:审批状态：审核状态  1 审核通过 2 审核拒绝 4 已撤回
     * remarks:审核备注 id:审核对象的id（合同或者回款的id）nextUserId:下一个审批人 ownerUserId:负责人
     */
    public R auditExamine(Integer recordId, Integer status, String remarks, Integer id, Long nextUserId, Long ownerUserId) {
        LoginUser user = BaseUtil.getUser();
        //当前审批人
        Long auditUserId = user.getUserId();

        //根据审核记录id查询审核记录
        LkCrmAdminExamineRecordEntity examineRecord = crmAdminExamineRecordDao.get(recordId);
        if (status == 4) {
            if (!examineRecord.getCreateUser().equals(auditUserId) && !auditUserId.equals(BaseUtil.getAdminUserId())) {
                return R.error("当前用户没有审批权限！");
            }
        } else {
            //【判断当前审批人是否有审批权限
            Record reco = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryExamineLog(recordId, auditUserId, String.valueOf(examineRecord.getExamineStepId())));
            if (reco == null) {
                return R.error("当前用户没有审批权限！");
            }
        }
        examineRecord.setExamineStatus(status);
        //查询审批流程
        LkCrmAdminExamineEntity examine = crmAdminExamineDao.get(examineRecord.getExamineId());
        if (examine.getCategoryType() == 1) {
            LkCrmContractEntity contactsEntity = crmContractDao.get(id);
            ownerUserId = contactsEntity.getOwnerUserId();
        } else {
            ownerUserId = crmReceivablesDao.get(id).getOwnerUserId();
        }
        //查询当前审批步骤
        LkCrmAdminExamineStepEntity examineStep = null;
        if (examineRecord.getExamineStepId() != null) {
            examineStep = crmAdminExamineStepDao.get(examineRecord.getExamineStepId());
        }
        //查询当前审核日志
        LkCrmAdminExamineLogEntity nowadayExamineLog = null;
        if (examine.getExamineType() == 1) {
            nowadayExamineLog = crmAdminExamineLogDao.queryNowadayExamineLogByRecordIdAndStepId(examineRecord.getRecordId(), examineRecord.getExamineStepId(), auditUserId);
            //nowadayExamineLog = AdminExamineLog.dao.findFirst(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStepId"), examineRecord.getRecordId(), examineRecord.getExamineStepId(), auditUserId);
        } else {
            nowadayExamineLog = crmAdminExamineLogDao.queryNowadayExamineLogByRecordIdAndStatus(examineRecord.getRecordId(), auditUserId);
            //nowadayExamineLog = AdminExamineLog.dao.findFirst(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStatus"), examineRecord.getRecordId(), auditUserId);
        }

        //审核日志 添加审核人
        if (nowadayExamineLog != null) {
            nowadayExamineLog.setExamineTime(DateUtil.date().toTimestamp());
            nowadayExamineLog.setRemarks(remarks);
        }

        if (status == 2) {
            //判断审核拒绝
            nowadayExamineLog.setExamineStatus(status);
            if (examineStep != null && examineStep.getStepType() == 2) {
                examineRecord.setExamineStatus(3);
                Record record = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryCountByStepId(recordId, examineStep.getStepId()));
                if (record.getInt("toCount") == 0) {
                    examineRecord.setExamineStatus(status);
                }
            }

            if (examine.getCategoryType() == 1) {
                //合同
                crmContractDao.updateCheckStatusById(3, id);
                //Db.update(Db.getSql("crm.contract.updateCheckStatusById"), 3, id);
            } else {
                //回款
                crmReceivablesDao.updateCheckStatusById(3, id);
                //Db.update(Db.getSql("crm.receivables.updateCheckStatusById"), 3, id);
            }
        } else if (status == 4) {
            //先查询该审批流程的审批步骤的第一步
            LkCrmAdminExamineStepEntity oneExamineStep = crmAdminExamineStepDao.queryExamineStepByExamineIdOrderByStepNum(examine.getExamineId());
            //判断审核撤回
            LkCrmAdminExamineLogEntity examineLog = new LkCrmAdminExamineLogEntity();
            //examineLog.setLogId(null);
            examineLog.setExamineUser(auditUserId);
            examineLog.setCreateTime(DateUtil.date().toTimestamp());
            examineLog.setCreateUser(auditUserId);
            examineLog.setExamineStatus(status);
            examineLog.setExamineTime(DateUtil.date().toTimestamp());
            examineLog.setIsRecheck(0);
            if (examine.getExamineType() == 1) {
                examineRecord.setExamineStepId(oneExamineStep.getStepId());
                examineLog.setExamineStepId(examineStep.getStepId());
                examineLog.setOrderId(examineStep.getStepNum());
            } else {
                Integer orderId = crmAdminExamineRecordDao.queryForInt("select order_id from lkcrm_admin_examine_log where record_id = ? and is_recheck = 0 and examine_status !=0 order by order_id desc limit 1 ", recordId);
                if (orderId == null) {
                    orderId = 1;
                }
                examineLog.setOrderId(orderId);
            }
            examineLog.setRecordId(examineRecord.getRecordId());
            examineLog.setRemarks(remarks);
            crmAdminExamineLogDao.saveReturnPk(examineLog);
            if (examine.getCategoryType() == 1) {
                //合同
                LkCrmContractEntity contract = crmContractDao.get(id);
                if (contract.getCheckStatus() == 2) {
                    return R.error("该合同已审核通过，不能撤回！");
                }
                crmContractDao.updateCheckStatusById(4, id);
                //Db.update(Db.getSql("crm.contract.updateCheckStatusById"), 4, id);
            } else {
                //回款
                LkCrmReceivablesEntity receivables = crmReceivablesDao.get(id);
                if (receivables.getCheckStatus() == 2) {
                    return R.error("该回款已审核通过，不能撤回！");
                }
                crmReceivablesDao.updateCheckStatusById(4, id);
                //Db.update(Db.getSql("crm.receivables.updateCheckStatusById"), 4, id);
            }
        } else {
            //审核通过
            nowadayExamineLog.setExamineStatus(status);
            //判断该审批流程类型
            if (examine.getExamineType() == 1) {
                //固定审批

                //查询下一个审批步骤
                LkCrmAdminExamineStepEntity nextExamineStep = crmAdminExamineStepDao.queryExamineStepByNextExamineIdOrderByStepId(examine.getExamineId(), examineRecord.getExamineStepId());
                //AdminExamineStep.dao.findFirst(Db.getSql("admin.examineStep.queryExamineStepByNextExamineIdOrderByStepId"), examine.getExamineId(), examineRecord.getExamineStepId());

                Boolean flag = true;
                //判断是否是并签
                if (examineStep.getStepType() == 3) {
                    //查询当前并签是否都完成
                    //根据审核记录ID，审核步骤ID，查询审核日志
                    // List<AdminExamineLog> examineLogs = AdminExamineLog.dao.find(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStepId"),examineRecord.getRecordId(),examineRecord.getExamineStepId());
                    //当前并签人员
                    //nowadayExamineLog.update();
                    crmAdminExamineLogDao.update(nowadayExamineLog);
                    String[] userIds = examineStep.getCheckUserId().split(",");
                    for (String userId : userIds) {
                        if (StrUtil.isNotEmpty(userId)) {
                            LkCrmAdminExamineLogEntity examineLog = crmAdminExamineLogDao.queryNowadayExamineLogByRecordIdAndStepId(examineRecord.getRecordId(), examineRecord.getExamineStepId(), NumberConvertUtil.parseLong(userId));
                            if (examineLog.getExamineStatus() == 0) {
                                //并签未走完
                                flag = false;
                                break;
                            }
                        }
                    }
                    //并签未完成
                    if (!flag) {
                        examineRecord.setExamineStatus(3);
                        if (examine.getCategoryType() == 1) {
                            //合同
                            crmContractDao.updateCheckStatusById(1, id);
                            //Db.update(Db.getSql("crm.contract.updateCheckStatusById"), 1, id);
                        } else {
                            //回款
                            crmReceivablesDao.updateCheckStatusById(1, id);
                            //Db.update(Db.getSql("crm.receivables.updateCheckStatusById"), 1, id);
                        }
                    }
                }
                if (flag) {
                    //判断是否有下一步流程
                    if (nextExamineStep != null) {
                        //有下一步流程
                        examineRecord.setExamineStatus(3);
                        examineRecord.setExamineStepId(nextExamineStep.getStepId());

                        LkCrmAdminExamineLogEntity examineLog = new LkCrmAdminExamineLogEntity();
                        examineLog.setOrderId(nextExamineStep.getStepNum());
                        if (nextExamineStep.getStepType() == 2 || nextExamineStep.getStepType() == 3) {
                            //并签或者或签
                            String[] userIds = nextExamineStep.getCheckUserId().split(",");
                            for (String uid : userIds) {
                                if (StrUtil.isNotEmpty(uid)) {
                                    ///examineLog.setLogId(null);
                                    examineLog.setExamineUser(Long.valueOf(uid));
                                    examineLog.setCreateTime(DateUtil.date().toTimestamp());
                                    examineLog.setCreateUser(user.getUserId());
                                    examineLog.setExamineStatus(0);
                                    examineLog.setIsRecheck(0);
                                    examineLog.setExamineStepId(nextExamineStep.getStepId());
                                    examineLog.setRecordId(examineRecord.getRecordId());

                                    crmAdminExamineLogDao.save(examineLog);
                                }
                            }
                        } else if (nextExamineStep.getStepType() == 1) {
                            List<Map<String, Object>> maps = crmAdminExamineLogDao.queryUserByUserId(ownerUserId);
                            Record r = JavaBeanUtil.mapToRecord(maps.size() > 0 ? maps.get(0) : null);
                            examineLog.setLogId(null);
                            if (r == null || r.getLong("user_id") == null) {
                                examineLog.setExamineUser(BaseUtil.getAdminUserId());
                            } else {
                                examineLog.setExamineUser(r.getLong("user_id"));

                            }
                            examineLog.setExamineStatus(0);
                            examineLog.setCreateTime(DateUtil.date().toTimestamp());
                            examineLog.setCreateUser(user.getUserId());
                            examineLog.setIsRecheck(0);
                            examineLog.setExamineStepId(nextExamineStep.getStepId());
                            examineLog.setRecordId(examineRecord.getRecordId());
                            crmAdminExamineLogDao.save(examineLog);
                        } else {
                            List<Map<String, Object>> maps = crmAdminExamineLogDao.queryUserByUserId(ownerUserId);
                            Record r = JavaBeanUtil.mapToRecord(maps.size() > 0 ? maps.get(0) : null);
                            //Record r = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), ownerUserId).getLong("user_id"));
                            examineLog.setLogId(null);
                            if (r == null || r.getLong("user_id") == null) {
                                examineLog.setExamineUser(BaseUtil.getAdminUserId());
                            } else {
                                examineLog.setExamineUser(r.getLong("user_id"));

                            }
                            examineLog.setExamineStatus(0);
                            examineLog.setCreateTime(DateUtil.date().toTimestamp());
                            examineLog.setCreateUser(user.getUserId());
                            examineLog.setExamineStepId(nextExamineStep.getStepId());
                            examineLog.setRecordId(examineRecord.getRecordId());
                            examineLog.setIsRecheck(0);
                            crmAdminExamineLogDao.save(examineLog);
                        }

                        // AdminExamineLog examineLog = new AdminExamineLog();
                        if (examine.getCategoryType() == 1) {
                            //合同
                            crmContractDao.updateCheckStatusById(1, id);
                            //Db.update(Db.getSql("crm.contract.updateCheckStatusById"), 1, id);
                        } else {
                            //回款
                            crmReceivablesDao.updateCheckStatusById(1, id);
                            //Db.update(Db.getSql("crm.receivables.updateCheckStatusById"), 1, id);
                        }
                    } else {
                        //没有下一审批流程步骤
                        if (examine.getCategoryType() == 1) {
                            //合同
                            crmContractDao.updateCheckStatusById(2, id);
                            //Db.update(Db.getSql("crm.contract.updateCheckStatusById"), 2, id);
                            LkCrmContractEntity contract = crmContractDao.get(id);
                            crmCustomerDao.updateDealStatusById("已成交", contract.getCustomerId());
                            //Db.update(Db.getSql("crm.customer.updateDealStatusById"), "已成交", contract.getCustomerId());
                        } else {
                            //回款
                            crmReceivablesDao.updateCheckStatusById(2, id);
                            //Db.update(Db.getSql("crm.receivables.updateCheckStatusById"), 2, id);
                        }

                    }
                }
            } else {
                //授权审批
                if (nextUserId != null) {
                    //有下一审批人
                    examineRecord.setExamineStatus(3);
                    LkCrmAdminExamineLogEntity examineLog = new LkCrmAdminExamineLogEntity();
                    examineLog.setCreateTime(DateUtil.date().toTimestamp());
                    examineLog.setCreateUser(user.getUserId());
                    examineLog.setExamineUser(nextUserId);
                    examineLog.setExamineStatus(0);
                    examineLog.setIsRecheck(0);
                    examineLog.setRecordId(examineRecord.getRecordId());
                    examineLog.setOrderId(nowadayExamineLog.getOrderId() + 1);
                    crmAdminExamineLogDao.save(examineLog);
                    if (examine.getCategoryType() == 1) {
                        //合同
                        crmContractDao.updateCheckStatusById(1, id);
                        //Db.update(Db.getSql("crm.contract.updateCheckStatusById"), 1, id);
                    } else {
                        //回款
                        crmReceivablesDao.updateCheckStatusById(1, id);
                        //Db.update(Db.getSql("crm.receivables.updateCheckStatusById"), 1, id);
                    }
                } else {
                    //没有下一审批人
                    if (examine.getCategoryType() == 1) {
                        //合同
                        crmContractDao.updateCheckStatusById(2, id);
                        //Db.update(Db.getSql("crm.contract.updateCheckStatusById"), 2, id);
                       /* CrmContract contract = CrmContract.dao.findById(id);
                        Db.update(Db.getSql("crm.customer.updateDealStatusById"), "已成交", contract.getCustomerId());*/
                        LkCrmContractEntity contract = crmContractDao.get(id);
                        crmCustomerDao.updateDealStatusById("已成交", contract.getCustomerId());
                    } else {
                        //回款
                        crmReceivablesDao.updateCheckStatusById(2, id);
                        //Db.update(Db.getSql("crm.receivables.updateCheckStatusById"), 2, id);
                    }
                }

            }
        }
        if (status != 4) {
            //nowadayExamineLog.update();
            crmAdminExamineLogDao.update(nowadayExamineLog);
        }
        crmAdminExamineRecordDao.update(examineRecord);
        return R.ok();
    }

    public R queryExamineLogList(Integer recordId) {
        //根据审核记录id查询审核记录
        LkCrmAdminExamineRecordEntity examineRecord = crmAdminExamineRecordDao.get(recordId);
        LkCrmAdminExamineEntity adminExamine = crmAdminExamineDao.get(examineRecord.getExamineId());
        List<Record> logs = null;
        if (adminExamine.getExamineType() == 1) {
            logs = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryExamineLogByRecordIdByStep(recordId));
        } else {
            logs = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryExamineLogByRecordIdByStep1(recordId));
            //logs = Db.find(Db.getSql("admin.examineLog.queryExamineLogByRecordIdByStep1"), recordId);
        }

        return R.ok().put("data", logs);
    }

    /**
     * 根据审核记录id，查询审核日志
     * ownerUserId 负责人ID
     */
    public R queryExamineRecordList(Integer recordId, Long ownerUserId) {
        JSONObject jsonObject = new JSONObject();

        Record examineRecord = JavaBeanUtil.mapToRecord(crmAdminExamineRecordDao.queryExamineRecordById(recordId));
        //如果当前审批已撤回
        if (examineRecord.getInt("examine_status") == 4) {
            jsonObject.put("examineType", 1);

            List<Record> user = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByRecordId(recordId));
            examineRecord.set("userList", user);
            List<Record> records = new ArrayList<>();
            records.add(examineRecord);
            jsonObject.put("steps", records);

            return R.ok().put("data", jsonObject);
        }
        LkCrmAdminExamineEntity adminExamine = crmAdminExamineDao.get(examineRecord.getInt("examine_id"));
        List<Record> list = new ArrayList<>();
        Record rec = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryRecordAndId(recordId));

        //当前审批人
        Long auditUserId = BaseUtil.getUser().getUserId();
        //jsonObject.put("isRecheck",0);
        //判断是否有撤回权限
        if ((auditUserId.equals(examineRecord.getLong("create_user")) || auditUserId.equals(BaseUtil.getAdminUserId()))
                && (examineRecord.getInt("examine_status") == 0 || examineRecord.getInt("examine_status") == 3)) {
            jsonObject.put("isRecheck", 1);
        } else {
            jsonObject.put("isRecheck", 0);
        }
        if (adminExamine.getExamineType() == 2) {
            Record log = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryRecordByUserIdAndStatus(rec.getDate("examineTime"), rec.getLong("create_user")));
            rec.set("examinUser", log);
            list.add(rec);
            //授权审批
            List<Record> logs = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryExamineLogAndUserByRecordId(recordId));
            logs.forEach(r -> {
                Record l = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryExamineLogAndUserByLogId(r.getInt("log_id")));
                r.set("examinUser", l);
            });
            list.addAll(logs);
            //判断当前用户有没有权限审核
            Record reco = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryExamineLog(recordId, auditUserId, ""));
           /* Record reco = Db.findFirst(Db.getSqlPara("admin.examineLog.queryExamineLog",
                    Kv.by("recordId", recordId).set("auditUserId", auditUserId)));*/
            if (reco != null) {
                jsonObject.put("isCheck", 1);
            } else {
                jsonObject.put("isCheck", 0);
            }
            jsonObject.put("examineType", 2);
            jsonObject.put("steps", list);

        } else {
            jsonObject.put("examineType", 1);
            //固定审批
            List<Record> steps = JavaBeanUtil.mapToRecords(crmAdminExamineRecordDao.sqlQuery("select * from lkcrm_admin_examine_step where  examine_id = ? ORDER BY step_num", adminExamine.getExamineId()));

            steps.forEach(step -> {
                if (step.getInt("step_type") == 1) {
                    //负责人主管
                    List<Record> logs = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByRecordIdAndStepIdAndStatus(recordId, step.getInt("step_id")));
                    if (logs.size() == 1) {
                        if (logs.get(0).getInt("user_id") == null) {
                            logs = null;
                        }

                    }
                    //已经创建审核日志
                    if (logs != null && logs.size() != 0) {
                        for (Record record : logs) {
                            step.set("examine_status", record.getInt("examine_status"));
                        }
                        step.set("userList", logs);
                    } else {
                        step.set("examine_status", 0);
                        //还未创建审核日志
                        //查询负责人主管
                        List<Record> r = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByUserId(ownerUserId));
                        if (r.size() == 1) {
                            if (r.get(0).getInt("user_id") == null) {
                                r = null;
                            }

                        }
                        if (r == null || r.size() == 0) {
                            r = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByUserIdAnd(BaseUtil.getAdminUserId()));
                            //r = Db.find(Db.getSql("admin.examineLog.queryUserByUserIdAnd"), BaseConstant.SUPER_ADMIN_USER_ID);
                        }
                        step.set("userList", r);
                    }
                } else if (step.getInt("step_type") == 2 || step.getInt("step_type") == 3) {
                    //先判断是否已经审核过
                    List<Record> logs = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByRecordIdAndStepIdAndStatus(recordId, step.getInt("step_id")));
                    if (logs != null && logs.size() != 0) {
                        //已经创建审核日志
                        int status = 0;

                        if (step.getInt("step_type") == 2) {
                            int i = 0;
                            for (Record record : logs) {
                                if (record.getInt("examine_status") == 1) {
                                    status = 1;
                                }
                                if (record.getInt("examine_status") == 2) {
                                    i++;
                                }
                            }
                            if (i == logs.size()) {
                                status = 2;
                            }
                        }
                        if (step.getInt("step_type") == 3) {
                            int i = 0;
                            for (Record record : logs) {
                                if (record.getInt("examine_status") == 2) {
                                    status = 2;
                                }
                                if (record.getInt("examine_status") == 1) {
                                    i++;
                                }
                            }
                            if (i == logs.size()) {
                                status = 1;
                            }
                        }
                        step.set("examine_status", status);
                        step.set("userList", logs);
                    } else {
                        //该步骤还未审核
                        logs = new ArrayList<>();
                        String[] userIds = step.getStr("check_user_id").split(",");
                        for (String userId : userIds) {
                            if (StrUtil.isNotEmpty(userId)) {
                                Record user = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryUserByUserIdAndStatus(NumberConvertUtil.parseLong(userId)));
                                //Record user = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserIdAndStatus"), userId);
                                if (user != null) {
                                    logs.add(user);
                                }
                            }
                        }
                        step.set("examine_status", 0);
                        step.set("userList", logs);
                    }
                } else {
                    //主管的主管
                    List<Record> logs = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByRecordIdAndStepIdAndStatus(recordId, step.getInt("step_id")));
                    if (logs.size() == 1) {
                        if (logs.get(0).getInt("user_id") == null) {
                            logs = null;
                        }

                    }
                    //已经创建审核日志
                    if (logs != null && logs.size() != 0) {
                        for (Record record : logs) {
                            step.set("examine_status", record.getInt("examine_status"));
                        }
                        step.set("userList", logs);
                    } else {
                        step.set("examine_status", 0);
                        //还未创建审核日志
                        //查询负责人主管的主管
                        List<Map<String, Object>> maps = crmAdminExamineLogDao.queryUserByUserId(ownerUserId);
                        List<Record> r = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByUserId(NumberConvertUtil.parseLong(maps.get(0).get("user_id"))));
                        if (r.size() == 1) {
                            if (r.get(0).getInt("user_id") == null) {
                                r = null;
                            }
                        }
                        if (r == null) {
                            r = JavaBeanUtil.mapToRecords(crmAdminExamineLogDao.queryUserByUserIdAnd(BaseUtil.getAdminUserId()));
                        }
                        step.set("userList", r);
                    }
                }
            });
            //判断当前用户有没有权限审核
            Record reco = JavaBeanUtil.mapToRecord(crmAdminExamineLogDao.queryExamineLog(recordId, auditUserId, examineRecord.getStr("examine_step_id")));
            if (reco != null) {
                jsonObject.put("isCheck", 1);
            } else {
                jsonObject.put("isCheck", 0);
            }
            Map<String, Object> stringObjectMap = crmAdminExamineLogDao.queryRecordByUserIdAndStatus(rec.getDate("examineTime"), rec.getLong("create_user"));
            List<Record> logs = new ArrayList<>();
            logs.add(JavaBeanUtil.mapToRecord(stringObjectMap));
            rec.set("userList", logs);
            list.add(rec);
            list.addAll(steps);
            jsonObject.put("steps", list);
        }
        return R.ok().put("data", jsonObject);
    }
}

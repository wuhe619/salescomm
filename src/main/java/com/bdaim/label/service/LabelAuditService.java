package com.bdaim.label.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.Constant;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.dataexport.service.DataExportApplyService;
import com.bdaim.label.dao.CommonService;
import com.bdaim.label.dao.LabelAuditDao;
import com.bdaim.label.dao.LabelCoverDao;
import com.bdaim.label.entity.LabelAudit;
import com.bdaim.label.entity.LabelInfo;
import com.bdaim.rbac.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

@Service("labelAuditService")
@Transactional
public class LabelAuditService {
	private static Logger log = LoggerFactory.getLogger(LabelAuditService.class);
	@Resource
	private LabelAuditDao labelAuditDao;
	@Resource
	private LabelInfoService labelInfoService;
	@Resource
	private CustomGroupService customGroupService;
	@Resource
	private DataExportApplyService dataExportApplyService;
	@Resource
	private AuditFlowService auditFlowService;
	@Resource
	private LabelCoverDao labelCoverDao;
	@Resource
	private LabelInterfaceService labelInterfaceService;
	@Resource
	private CommonService commonService;
	
	
	/**
	 * @param audit
	 * @param nodeId
	 * @return
	 */
	public LabelAudit getLabelAudit(LabelAudit audit, Integer nodeId) {
		Integer apply = audit.getApplyType();
		Integer auditType = audit.getAuditType();
		Integer aid = audit.getAid();
		Date date = new Date();
		LabelAudit last = getLastAuditInfo(aid, auditType);
		// nodeId为空属于新建类型
		if (null == nodeId) {
			// 下线请求 需要更改最新一条记录 创建请求只需要新建
			if (Constant.OFFLINE_AUDIT_APPLYS.contains(apply)) {
				if (null != last) {
					last.setLastFlag(Constant.AUDIT_LAST_FLAG_NO);
					labelAuditDao.update(last); // 将最新记录更新为否
				}
			}
		} else {
			// nodeId 不为空需要复制部分最新审核记录的数据
			if (null != last) {
				if (last.getStatus().equals(audit.getStatus())) {
					if (audit.getApplyType().equals(last.getApplyType())
							&& null == audit.getAuditResult()) {
						last.setApplyTime(date);
						last.setOfflineMsg(audit.getOfflineMsg());
						last.setApplyUser(audit.getApplyUser());
						labelAuditDao.update(last);
						return null;
					}
					last.setLastFlag(Constant.AUDIT_LAST_FLAG_NO);
					labelAuditDao.update(last);
					if (null == audit.getApplyTime())
						audit.setApplyTime(last.getApplyTime());
					if (null == audit.getOfflineStatus())
						audit.setOfflineStatus(last.getOfflineStatus());
					if (null == audit.getApplyUser())
						audit.setApplyUser(last.getApplyUser());
					if (null == audit.getApplyType())
						audit.setApplyType(last.getApplyType());
					if (null == audit.getAuditType())
						audit.setAuditType(last.getAuditType());
					if (null == audit.getName())
						audit.setName(last.getName());
					if (null == audit.getOfflineMsg())
						audit.setOfflineMsg(last.getOfflineMsg());
					if (null== audit.getDevUser())
						audit.setDevUser(last.getDevUser());
				} else {
					last.setLastFlag(Constant.AUDIT_LAST_FLAG_NO);
					labelAuditDao.update(last);
					if (null == audit.getApplyTime())
						audit.setApplyTime(last.getApplyTime());
					if (null == audit.getOfflineStatus())
						audit.setOfflineStatus(last.getOfflineStatus());
					if (null == audit.getApplyUser())
						audit.setApplyUser(last.getApplyUser());
					if (null == audit.getApplyType())
						audit.setApplyType(last.getApplyType());
					if (null == audit.getAuditType())
						audit.setAuditType(last.getAuditType());
					if (null == audit.getName())
						audit.setName(last.getName());
					if (null == audit.getOfflineMsg())
						audit.setOfflineMsg(last.getOfflineMsg());
					if (null== audit.getDevUser())
						audit.setDevUser(last.getDevUser());
				}
				if (null == audit.getAuditResult()
						&& (null != last.getAuditResult())) {
					if(!nodeId.equals(Constant.NODE_DEVELOP)){
						audit.setAuditResult(last.getAuditResult());

					}
				}
			}
		}
		if (auditType.equals(Constant.AUDIT_TYPE_LABEL)
				|| auditType.equals(Constant.AUDIT_TYPE_CATEGORY)||auditType.equals(Constant.AUDIT_TYPE_SIGNATURE)) {
			audit.setLabelInfo(labelInfoService.getLabelById(audit.getAid()));
		} else if (auditType.equals(Constant.AUDIT_TYPE_GROUP)) {
			audit.setCustomGroup(customGroupService.getCustomGroupById(audit
					.getAid()));
		}
		if (null == audit.getApplyTime())
			audit.setApplyTime(date);
		if (null == audit.getAuditTime())
			audit.setAuditTime(date);
		if (null == audit.getAvailably())
			audit.setAvailably(Constant.AVAILABLY);
		audit.setLastFlag(Constant.AUDIT_LAST_FLAG_YES);
		if(audit.getStatus()==null)
			audit.setStatus(getAuditStatusByNode(nodeId,audit));
		return audit;
	}

	/**
	 * 审核操作
	 */
	public void addAuditInfo(LabelAudit audit) {
		if (audit.getCreateTime() == null)
			audit.setCreateTime(new Date());
		labelAuditDao.save(audit);
	}

	/**
	 * 查询标签的审核生命周期
	 */
	@SuppressWarnings("unchecked")
	public List<LabelAudit> getLabelAuditCycleByLid(Integer lid) {
		return labelAuditDao.createQuery(
				"from LableAudit where availably=1 and aid=?", lid).list();
	}

	@SuppressWarnings("unchecked")
	public LabelAudit getLastAuditInfo(Integer lid, Integer auditType) {
		String hql = "From LabelAudit where lastFlag =1 and aid = ? and auditType=? and availably = 1 ORDER BY id DESC ";
		List<LabelAudit> audits = (List<LabelAudit>) labelAuditDao.createQuery(
				hql, lid, auditType).list();
		if (audits.isEmpty())
			return null;
		return audits.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getAuditsGroupByStatus(String type,
			User user) {
		StringBuffer hql = new StringBuffer(
				"select new map(count(1) as count,t.status as status,t.auditType as label) from LabelAudit t where t.lastFlag =1 and t.availably =1 ");
		if (type.equalsIgnoreCase("dept")) {
			hql.append(" and t.applyUser.department.id=").append(user.getDepartment().getId());
		} else if (type.equalsIgnoreCase("mine")) {
			hql.append(" and t.applyUser.id=").append(user.getId());
		}
		hql.append(" group by t.status,t.auditType ");
		List<Map<String, Object>> result = labelAuditDao.createQuery(
				hql.toString()).list();
		return result;
	}

	/*@SuppressWarnings("unchecked")
	public List<LabelAudit> getAuditsByCondition(Map<String, Object> map,
			Map<String, Object> likeMap, Page page) {
		String hql = "From LabelAudit t ";
		String conditionHql = "where t.applyType in (:applys) and t.status in(:status) and t.lastFlag =1 and t.availably =1";
		if (map.containsKey(Constant.FILTER_KEY_PREFIX + "nodeId")) {
			Integer nodeId = (Integer) map.get(Constant.FILTER_KEY_PREFIX
					+ "nodeId");
			conditionHql = getQuerySql(nodeId, map);
		}
		List<LabelAudit> audits = new ArrayList<LabelAudit>();
		if (null == page) {
			audits = labelAuditDao.getHqlQuery(hql + conditionHql, map,
					likeMap, "id")
			// .setParameterList("applys", applys)
			// .setParameterList("status", status)
					.list();
		} else {
			audits = labelAuditDao
					.getHqlQuery(hql + conditionHql, map, likeMap, "id")
					// .setParameterList("applys", applys)
					// .setParameterList("status", status)
					.setFirstResult(page.getStart())
					.setMaxResults(page.getLimit()).list();
		}
		return audits;
	}*/

	public Integer getAuditsCountByCondition(Map<String, Object> map,
			Map<String, Object> likeMap) {
		String hql = "select count(id) From LabelAudit t ";
		String conditionHql = "where t.applyType in (:applys) and t.status in (:status) and t.lastFlag =1 and t.availably =1";
		if (map.containsKey(Constant.FILTER_KEY_PREFIX + "nodeId")) {
			Integer nodeId = (Integer) map.get(Constant.FILTER_KEY_PREFIX
					+ "nodeId");
			conditionHql = getQuerySql(nodeId, map);
		}
		Object count = labelAuditDao
				.getHqlQuery(hql + conditionHql, map, likeMap, null)
				// .setParameterList("applys", applys)
				// .setParameterList("status", status)
				.list().get(0);
		return count == null ? 0 : Integer.parseInt(count.toString());
	}

	/*public Map<String, Object> getAuditDetailById(Integer id) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		LabelAudit audit = labelAuditDao.get(id);
		Integer aid = audit.getAid();
		Integer auditType = audit.getAuditType();
		Integer applyType = audit.getApplyType();
		User applyUser = audit.getApplyUser();
		if (null != applyUser) {
			resultMap.put("applyUser", applyUser.getName());
		}
		resultMap.put("applyTime", CalendarUtil.getDateString(audit.getApplyTime(), CalendarUtil.SHORT_DATE_FORMAT));
		resultMap.put("auditType", audit.getAuditType());
		resultMap.put("applyType", applyType);
		User devUser = audit.getDevUser();
		if (null != devUser) {
			resultMap.put("devUser", devUser.getName());
		}
		resultMap.put("offlineMsg", audit.getOfflineMsg());// 下线理由
		resultMap.put("message", audit.getAuditMsg());// 审批理由
		resultMap.put("auditResult", audit.getAuditResult());
		if (auditType.equals(Constant.AUDIT_TYPE_LABEL)
				|| auditType.equals(Constant.AUDIT_TYPE_CATEGORY)||auditType.equals(Constant.AUDIT_TYPE_SIGNATURE)) {
			LabelInfo label = labelInfoService.getLabelById(aid);
			resultMap.putAll(commonService.getLabelMap(label));
			if(null != label){
				String hql = "from LabelCover t where t.cycle = 0 and t.label.id="+label.getId();
				List<LabelCover> covers = labelCoverDao.createQuery(hql).list();
				if(null != covers && covers.size()>0){
					resultMap.put("customerNum", covers.get(0).getCoverNum());
					resultMap.put("total", covers.get(0).getTotal());
				}
			}
		} else if (auditType.equals(Constant.AUDIT_TYPE_GROUP)) {
			CustomGroupDO customGroup = customGroupService
					.getCustomGroupById(aid);
			if(null != customGroup)
				resultMap.putAll(commonService.getCustomGroupMap(customGroup));
		}else if(auditType.equals(Constant.AUDIT_TYPE_EXPORT)){
			DataExportApply apply = dataExportApplyService.getDataExportApplyById(aid);
			if(null != apply)
				resultMap.putAll(CommonService.getDataExportApplyMap(apply));
		}
		Integer status = audit.getStatus();
		if (null != status) {
			resultMap.put("status", status);
			resultMap.put("statusCn", Constant.STATUS_MAP.get(status));
		}
		return resultMap;
	}*/

	public Map<String, Object> deleteAuditInfo(LabelAudit audit) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("msg", "删除成功");
		Integer auditType = audit.getAuditType();
		Integer aid = audit.getAid();
		Integer status = audit.getStatus();
		if (auditType.equals(Constant.AUDIT_TYPE_LABEL)
				|| auditType.equals(Constant.AUDIT_TYPE_CATEGORY)) {
			if (!status.equals(Constant.AUDITING)) {
				resultMap.put("msg", "删除失败 无法删除非申请中的数据");
				return resultMap;
			}
			try {
				LabelInfo label = labelInfoService.getLabelById(aid);
				if(!label.getStatus().equals(Constant.ONLINE)){
					label.setAvailably(Constant.UNAVAILABLY);
					label.setUpdateTime(new Date());
					User auditUser = audit.getAuditUser();
					if (null != auditUser) {
						label.setLabelUpdateUser(audit.getAuditUser());
					}
					labelInfoService.updateLabelInfo(label);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return resultMap;
			}
		} else if (auditType.equals(Constant.AUDIT_TYPE_GROUP)) {
			if (!status.equals(Constant.AUDITING)) {
				resultMap.put("msg", "删除失败 无法删除非申请中的数据");
				return resultMap;
			}
			CustomGroup customGroup = customGroupService.getCustomGroupById(aid);
			if(!customGroup.getStatus().equals(Constant.ONLINE)){
				customGroup.setUpdateTime(new Date());
//				customGroup.setUpdateUser(audit.getAuditUser());
				customGroup.setStatus(Constant.OFFLINE);
				customGroup.setAvailably(Constant.UNAVAILABLY);
				customGroupService.updateCustomGroup(customGroup);
			}
		}
		audit.setAvailably(Constant.UNAVAILABLY);
		audit.setAuditTime(new Date());
		audit.setLastFlag(Constant.AUDIT_LAST_FLAG_NO);
		labelAuditDao.update(audit);
		return resultMap;
	}

	public LabelAudit getLabelAuditById(Integer id) {
		return labelAuditDao.get(id);
	}
	/**
	 * 根据当前流程节点获取有效的申请类型
	 * 
	 * @param node
	 * @return
	 */
	public static List<Integer> getAuditApplaysByNode(Integer node) {
		List<Integer> applys = new ArrayList<Integer>();
		for (Integer apply : Constant.AUDIT_APPLYS) {
			applys.add(new Integer(apply));
		}
		if (node.equals(Constant.NODE_AUDIT)) {
			applys.remove(Constant.APPLY_TYPE_CATEGORY_CREATE);
			applys.remove(Constant.APPLY_TYPE_CATEGORY_OFFLIN);
			applys.remove(Constant.APPLY_TYPE_SIGNATURE_CREATE);
			applys.remove(Constant.APPLY_TYPE_CUSTOM_GROUP_CREATE);
		} else if (node.equals(Constant.NODE_DEVELOP)) {
			applys.remove(Constant.APPLY_TYPE_CATEGORY_CREATE);
			applys.remove(Constant.APPLY_TYPE_CATEGORY_OFFLIN);
			applys.remove(Constant.APPLY_TYPE_SIGNATURE_CREATE);
			applys.remove(Constant.APPLY_TYPE_CUSTOM_GROUP_CREATE);
		}
		return applys;
	}

	/**
	 * 根据审批流程环节获取当前节点下拥有的审批状态
	 * 
	 * @param node
	 * @return
	 */
	public static List<Integer> getAuditStatusByNode(Integer node) {
		List<Integer> list = new ArrayList<Integer>();
		for (Integer status : Constant.AUDIT_STATUS) {
			list.add(new Integer(status));
		}
		if (node.equals(Constant.NODE_AUDIT)) {
			list.remove(Constant.DEVELOPING);
			list.remove(Constant.DEVELOP_FINISH);
			list.remove(Constant.OFFLINE);
			list.remove(Constant.ONLINE);
		} else if (node.equals(Constant.NODE_DEVELOP)) {
			list.remove(Constant.AUDITING);
			list.remove(Constant.DEVELOP_FINISH);
			list.remove(Constant.OFFLINE);
			list.remove(Constant.ONLINE);
		} else if (node.equals(Constant.NODE_PUBLISH)) {
			list.remove(Constant.OFFLINE);
			list.remove(Constant.ONLINE);
		}
		return list;
	}

	/**
	 * 根据当前流程节点和申请类型获得通过后的审核状态
	 * 
	 * @param node
	 * @param applyType
	 * @return
	 */
	public Integer getAuditStatusByNode(Integer node, LabelAudit audit) {
		Map<String,Object> map = new HashMap<String,Object>();
		Integer applyType = audit.getApplyType();
		Integer auditResult = audit.getAuditResult();
		Integer auditType = audit.getAuditType();
		if(null != node) map.put("nodeId", node);
		if(null != applyType) 	map.put("applyType", applyType);
		if(null != auditType) 	map.put("auditType", auditType);
		if(null != auditResult) map.put("auditResult", auditResult);
//		Integer auditStatus = Constant.AUDITING;
//		Integer offlineStatus = audit.getOfflineStatus();
//		if (node.equals(Constant.NODE_AUDIT)) {
//			if (getAuditApplaysByNode(node).contains(applyType)) {
//				if (auditResult.equals(Constant.AUDIT_APPROVE)) {
//					auditStatus = Constant.DEVELOPING;
//				}
//			}
//		} else if (node.equals(Constant.NODE_DEVELOP)) {
//			if (applyType.equals(Constant.APPLY_TYPE_BASELABEL_OFFLINE)
//					|| applyType.equals(Constant.APPLY_TYPE_SIGNATURE_OFFLINE)) {
//				if (offlineStatus.equals(1)) {
//					auditStatus = Constant.DEVELOP_FINISH;
//				}
//			} else {
//				auditStatus = Constant.DEVELOP_FINISH;
//			}
//		} else if (node.equals(Constant.NODE_PUBLISH)) {
//			if (applyType.equals(Constant.APPLY_TYPE_BASELABEL_OFFLINE)
//				|| applyType.equals(Constant.APPLY_TYPE_SIGNATURE_OFFLINE)
//				) {
//				if (auditResult.equals(Constant.AUDIT_APPROVE)) {
//					auditStatus = Constant.OFFLINE;
//				} else {
//					auditStatus = Constant.DEVELOPING;
//				}
//			} else if(applyType.equals(Constant.APPLY_TYPE_CATEGORY_OFFLIN)){
//				if (auditResult.equals(Constant.AUDIT_APPROVE)) {
//					auditStatus = Constant.OFFLINE;
//				} else {
//					auditStatus = Constant.AUDITING;
//				}
//			}else{
//				if (auditResult.equals(Constant.AUDIT_APPROVE)) {
//					auditStatus = Constant.ONLINE;
//				} else {
//					if (applyType.equals(Constant.APPLY_TYPE_SIGNATURE_CREATE)
//					|| applyType.equals(Constant.APPLY_TYPE_CUSTOM_GROUP_CREATE)
//					|| applyType.equals(Constant.APPLY_TYPE_CATEGORY_CREATE)
//							) {
//						auditStatus = Constant.AUDITING;
//					} else {
//						auditStatus = Constant.DEVELOPING;
//					}
//				}
//			}
//		}
		Integer auditStatus = auditFlowService.getAuditStatus(map);
		return auditStatus;
	}

	/**
	 * 根据当前流程节点获取标签状态
	 * 
	 * @param node
	 * @param applyType
	 * @return
	 */
	public Integer getLabelStatusByNode(Integer node, LabelAudit audit) {
		Integer applyType = audit.getApplyType();
		Integer labelStatus = null;
		if (applyType.equals(Constant.APPLY_TYPE_BASELABEL_OFFLINE)
				|| applyType.equals(Constant.APPLY_TYPE_SIGNATURE_OFFLINE)) {
			if (node.equals(Constant.NODE_PUBLISH)) {
				labelStatus = Constant.OFFLINE;
			} else {
				labelStatus = Constant.ONLINE;
			}
		} else {
			labelStatus = getAuditStatusByNode(node, audit);
		}
		return labelStatus;
	}


	/**
	 * 拼装过程管理中的查询hql语句
	 * 
	 * @param nodeId
	 * @param map
	 * @return
	 */
	public static String getQuerySql(Integer nodeId, Map<String, Object> map) {
		String hql = "where  t.lastFlag =1 and t.availably =1 ";
		List<Integer> applys = getAuditApplaysByNode(nodeId);
		List<Integer> statusList = new ArrayList<Integer>();
		if (map.containsKey("status")) {
			statusList.add((Integer) map.get("status"));
		} else {
			statusList = getAuditStatusByNode(nodeId);
		}
		if (nodeId.equals(Constant.NODE_APPLY)
				|| nodeId.equals(Constant.NODE_AUDIT)
				|| nodeId.equals(Constant.NODE_DEVELOP)) {
			if (applys.size() > 0) {
				hql += "and t.applyType in(" + getStringFromList(applys) + ")";
			}
			if (statusList.size() > 0) {
				hql += "and t.status in(" + getStringFromList(statusList) + ")";
			}
		} else if (nodeId.equals(Constant.NODE_PUBLISH)) {
			hql += "and ((t.applyType in("
					+ getStringFromList(Constant.LEVLE1_AUDIT_APPLYS) + ")";
			hql += "and t.status in(" + Constant.AUDITING + ")) ";
			hql += "or (t.applyType in("
					+ getStringFromList(Constant.LEVLE2_AUDIT_APPLYS) + ")";
			hql += "and t.status in(" + Constant.DEVELOP_FINISH + "))) ";
		}
		return hql;
	}

	private static String getStringFromList(List<Integer> list) {
		String str = "";
		for (Integer data : list) {
			str += data.toString() + ",";
		}
		if (str.length() > 1) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}
	
	public boolean queryByDevelopHiveTable(String hivetablename) {
		// TODO Auto-generated method stub
		//到REST中查询这个表名是否已存在
		try
		{
			String js = labelInterfaceService.queryByDevelopHiveTable(hivetablename);
			if (null == js || "".equals(js))
				return false;
			JSONObject josnstr = JSONObject.parseObject(js);
			if (null == josnstr)
				return false;
			int isSuccess = josnstr.getIntValue("isSuccess");
			if (1 != isSuccess)
				return false;
			Object obj = josnstr.get("data");
			if (null == obj)
				return true;
			return false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
	
}

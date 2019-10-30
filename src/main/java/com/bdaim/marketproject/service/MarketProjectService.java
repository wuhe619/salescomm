package com.bdaim.marketproject.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.common.CallUtil;
import com.bdaim.callcenter.common.PhoneAreaUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerLabelService;
import com.bdaim.customer.user.service.UserGroupService;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.dto.CustomerSeaParam;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.dto.CustomGroupDTO;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.label.dao.IndustryInfoDao;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.dto.MarketProjectDTO;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.marketproject.entity.MarketProjectProperty;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.template.dao.MarketTemplateDao;
import com.bdaim.template.entity.MarketTemplate;
import com.bdaim.util.*;
import com.bdaim.util.excel.ExcelAfterWriteHandlerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/19
 * @description
 */
@Service("marketProjectService")
@Transactional
public class MarketProjectService {

    public static final Logger LOG = LoggerFactory.getLogger(MarketProjectService.class);

    private static long projectExportTime = 0;

    @Resource
    private MarketProjectDao marketProjectDao;

    @Resource
    private CustomerDao customerDao;

    @Resource
    private CustomGroupDao customGroupDao;

    @Resource
    private CustomerUserDao customerUserDao;

    @Resource
    private PhoneService phoneService;

    @Resource
    private IndustryInfoDao industryInfoDao;

    @Resource
    private UserGroupService userGroupService;

    @Resource
    private CustomerLabelService customerLabelService;

    @Resource
    private CustomerSeaService customerSeaService;

    @Resource
    private CustomerSeaDao customerSeaDao;

    @Resource
    private MarketTaskDao marketTaskDao;

    @Resource
    private MarketTemplateDao marketTemplateDao;

    @Resource
    private CustomerLabelDao customerLabelDao;


    /**
     * 保存营销项目
     *
     * @param dto
     * @return
     */
    public int saveMarketProject(MarketProjectDTO dto) {
        MarketProject marketProject = new MarketProject();
        marketProject.setIndustryId(dto.getIndustryId());
        marketProject.setName(dto.getName());
        marketProject.setStatus(1);
        marketProject.setCreateTime(new Timestamp(System.currentTimeMillis()));
        try {
            marketProjectDao.saveReturnPk(marketProject);
            return 1;
        } catch (Exception e) {
            LOG.error("保存营销项目异常,", e);
            return 0;
        }
    }

    /**
     * 创建项目和公海
     *
     * @param dto
     * @param custId
     * @param userId
     * @return
     */
    public int saveMarketProjectAndSea(MarketProjectDTO dto, String custId, long userId) {
        MarketProject marketProject = new MarketProject();
        marketProject.setIndustryId(dto.getIndustryId());
        marketProject.setName(dto.getName());
        marketProject.setStatus(1);
        marketProject.setCustId(custId);
        marketProject.setCreateTime(new Timestamp(System.currentTimeMillis()));
        try {
            int id = (int) marketProjectDao.saveReturnPk(marketProject);
            dto.setId(id);
            if (StringUtil.isNotEmpty(custId)) {
                // 处理项目和客户的关联关系
                String marketProjectId = String.valueOf(id);
                List<String> custIds = new ArrayList<>();
                custIds.add(custId);
                this.saveMarketProjectRelationEnterprises(marketProjectId, custIds);
                //服务权限 1-营销任务 2-公海 多个逗号隔开
                CustomerProperty cpd = customerDao.getProperty(custId, CustomerPropertyEnum.SERVICE_MODE.getKey());
                if (cpd != null && "2".equals(cpd.getPropertyValue())) {
                    // 客户级别的项目自动创建公海
                    CustomerSeaParam customerSea = new CustomerSeaParam();
                    customerSea.setName(dto.getName() + "公海");
                    customerSea.setMarketProjectId(id);
                    customerSea.setCustId(custId);
                    customerSea.setCreateUid(userId);
                    customerSeaService.save(customerSea);
                }
            }
            return 1;
        } catch (Exception e) {
            LOG.error("保存营销项目异常,", e);
            return 0;
        }
    }

    /**
     * 修改营销项目
     *
     * @param marketProjectDTO
     * @return
     */
    public int updateMarketProject(MarketProjectDTO marketProjectDTO, int operation, String projectUserId, String custId) {
        int code = 0;
        // 修改基本信息
        if (operation == 1 || operation == 0) {
            MarketProject result = marketProjectDao.selectMarketProject(marketProjectDTO.getId());
            if (result != null) {
                if (marketProjectDTO.getStatus() != null) {
                    result.setStatus(marketProjectDTO.getStatus());
                }
                if (StringUtil.isNotEmpty(marketProjectDTO.getName())) {
                    result.setName(marketProjectDTO.getName());
                }
                if (marketProjectDTO.getIndustryId() != null) {
                    result.setIndustryId(marketProjectDTO.getIndustryId());
                }
                customerDao.saveOrUpdate(result);
                // status=2时关闭项目下关联的公海
                if (StringUtil.isNotEmpty(custId) &&
                        marketProjectDTO.getStatus() != null && marketProjectDTO.getStatus() == 2) {
                    customerSeaDao.updateCustomerSea(custId, NumberConvertUtil.parseInt(result.getId()), 2);
                }
                code = 1;
            } else {
                LOG.warn("项目id:" + marketProjectDTO.getId() + "未查询到");
                code = 0;
            }
        } else if (operation == 2) {
            // 变更项目管理员
            String sql = "select t1.user_id,t1.property_name,t1.property_value from t_customer_user_property t1 join t_customer_user t2 on t2.id = t1.user_id" +
                    " where t1.property_name = 'hasMarketProject' and find_in_set(?,t1.property_value) and t2.cust_id = ?";
            List<Map<String, Object>> list = marketProjectDao.sqlQuery(sql, marketProjectDTO.getId(), custId);
            Map<String, Object> m;
            String propertyValue = "";
            CustomerUserPropertyDO cp;
            for (int i = 0; i < list.size(); i++) {
                m = list.get(i);
                if (!projectUserId.equals(String.valueOf(m.get("user_id")))) {
                    propertyValue = String.valueOf(m.get("property_value"));
                    propertyValue = propertyValue.replaceAll("," + marketProjectDTO.getId(), "");
                    cp = customerUserDao.getProperty(String.valueOf(m.get("user_id")), "hasMarketProject");
                    if (cp != null) {
                        cp.setPropertyValue(propertyValue);
                        customerUserDao.saveOrUpdate(cp);
                    }
                }
            }
            // 处理设置项目管理员
            cp = customerUserDao.getProperty(projectUserId, "hasMarketProject");
            if (cp == null) {
                propertyValue = "," + marketProjectDTO.getId();
                cp = new CustomerUserPropertyDO(projectUserId, "hasMarketProject", propertyValue, new Timestamp(System.currentTimeMillis()));
            } else {
                if (StringUtil.isNotEmpty(cp.getPropertyValue())) {
                    propertyValue = cp.getPropertyValue();
                }
                if (propertyValue.indexOf("," + marketProjectDTO.getId()) < 0) {
                    propertyValue += "," + marketProjectDTO.getId();
                }
                cp.setPropertyValue(propertyValue);
            }
            customerUserDao.saveOrUpdate(cp);
            code = 1;
        }
        return code;
    }

    /**
     * 关闭项目
     *
     * @param marketProjectDTO
     * @return
     */
    public int closeMarketProject(MarketProjectDTO marketProjectDTO) {
        MarketProject result = marketProjectDao.selectMarketProject(marketProjectDTO.getId());
        if (result != null) {
            result.setStatus(marketProjectDTO.getStatus());
            customerDao.saveOrUpdate(result);
            return 1;
        } else {
            LOG.warn("营销记录id:" + marketProjectDTO.getId() + "未查询到相关记录");
            return 0;
        }
    }

    /**
     * 后台项目分页
     *
     * @param pageNum
     * @param pageSize
     * @param param
     * @return
     */
    public Page pageMarketProject(int pageNum, int pageSize, MarketProjectDTO param) {
        Page page = marketProjectDao.pageMarketProject(pageNum, pageSize, param);
        //查询出所有全局项目
        MarketProjectDTO allProject = new MarketProjectDTO();
        allProject.setType("1");
        List<MarketProjectDTO> allPage = marketProjectDao.listMarketProject(allProject);
        Set<Integer> globalProjects = new HashSet<>();
        if (allPage != null && allPage.size() > 0) {
            for (MarketProjectDTO dto : allPage) {
                globalProjects.add(dto.getId());
            }
            LOG.info("查询出的全局项目id是:" + globalProjects);
        }
        if (page != null && page.getData() != null && page.getData().size() > 0) {
            MarketProject marketProject;
            MarketProjectDTO marketProjectDTO;
            List<MarketProjectDTO> data = new ArrayList<>();
            Customer c;
            List<CustomerDTO> custList;
            for (int i = 0; i < page.getData().size(); i++) {
                marketProject = (MarketProject) page.getData().get(i);
                //查询项目关联的企业名字
                if (marketProject == null) {
                    continue;
                }
                marketProjectDTO = new MarketProjectDTO(marketProject);
                // 查看行业名称
                if (marketProject.getIndustryId() != null) {
                    marketProjectDTO.setIndustryName(industryInfoDao.getIndustryName(marketProjectDTO.getIndustryId()));
                }
                //判断是否是全局项目
                boolean contains = globalProjects.contains(marketProject.getId());
                LOG.info("项目id:[" + marketProject.getId() + "]判断是否是全局项目:" + contains);
                if (contains) {
                    //说明此id是全局项目 不需要查询企业名称
                    marketProjectDTO.setType("1");
                } else {
                    marketProjectDTO.setType("2");
                    //查看企业名称
                    String enterpriseName = null;
                    //查询项目主表的企业id是否存在
                    enterpriseName = customerDao.getEnterpriseName(marketProject.getCustId());
                    //查询企业名字
                    String querySql = "SELECT GROUP_CONCAT(enterprise_name) enterpriseNames FROM t_customer c LEFT JOIN t_customer_property m on c.cust_id = m.cust_id ";
                    querySql += " WHERE property_name = '" + CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + String.valueOf(marketProject.getId()) + "' AND property_value =" + String.valueOf(marketProject.getId());
                    List<Map<String, Object>> list = customerDao.sqlQuery(querySql, null);
                    if (list.size() > 0 && list.get(0).get("enterpriseNames") != null) {
                        String enterpriseNames = String.valueOf(list.get(0).get("enterpriseNames"));
                        if (StringUtil.isNotEmpty(enterpriseName)) {
                            boolean flag = enterpriseNames.contains(enterpriseName);
                            if (!flag) {
                                enterpriseNames += "," + enterpriseName;
                            }
                        }
                        marketProjectDTO.setEnterpriseName(enterpriseNames);
                    } else {
                        marketProjectDTO.setEnterpriseName(enterpriseName);
                    }
                }
                // 查询项目下关联的企业数
                c = new Customer();
                c.setStatus(0);
                custList = customerDao.listCustomer(c, CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProject.getId(), String.valueOf(marketProject.getId()));
                marketProjectDTO.setRelationCustNum(custList.size());
                data.add(marketProjectDTO);
            }
            page.setData(data);
        }
        return page;
    }

    /**
     * 后台项目列表
     *
     * @param marketProject
     * @return
     */
    public List<MarketProjectDTO> listMarketProject(MarketProjectDTO marketProject) {
        List<MarketProjectDTO> list = marketProjectDao.listMarketProject(marketProject);
        MarketProjectDTO marketProjectDTO;
        for (int i = 0; i < list.size(); i++) {
            marketProjectDTO = list.get(i);
            if (marketProjectDTO != null) {
                if (null != marketProjectDTO.getIndustryId()) {
                    marketProjectDTO.setIndustryName(industryInfoDao.getIndustryName(marketProjectDTO.getIndustryId()));
                }
            }
        }
        return list;
    }

    /**
     * 查询项目下关联的企业
     *
     * @param marketProjectId
     * @param enterpriseName
     * @return 返回选中的企业和未选择的企业
     */
    public Map<String, Object> listSelectCustomerByMarketProjectId(String marketProjectId, String enterpriseName, LoginUser lu) {
        // 查询所有企业
        Customer param = new Customer();
        param.setStatus(0);
        if (StringUtil.isNotEmpty(enterpriseName)) {
            param.setEnterpriseName(enterpriseName);
        }
        List<CustomerDTO> list = customerDao.listCustomer(param, "", "");
        String custIdsStr = "";
        if ("ROLE_USER".equals(lu.getRole())) {
            custIdsStr = userGroupService.getCustomerIdByuId(lu.getId().toString());
            if (StringUtil.isNotEmpty(custIdsStr)) {
                custIdsStr = custIdsStr.replace("\'", "");
            }
        }

        // 处理项目下已选企业
        Iterator<CustomerDTO> it = list.iterator();
        CustomerDTO m;
        CustomerProperty cp;
        List<CustomerDTO> selectedList = new ArrayList<>();
        while (it.hasNext()) {
            m = it.next();
            if (StringUtil.isNotEmpty(custIdsStr)) {
                if (!custIdsStr.contains(m.getId())) {
                    it.remove();
                    continue;
                }
            }
            cp = customerDao.getProperty(m.getId(), CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId);
            if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
                selectedList.add(m);
                it.remove();
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("selected", selectedList);
        data.put("unselected", list);
        return data;
    }

    /**
     * 保存项目和企业的关联关系
     *
     * @param marketProjectId
     * @param custIds
     * @return
     */
    public int saveMarketProjectRelationEnterprises(String marketProjectId, List<String> custIds) {
        // 查询项目下所有关联企业
        Customer param = new Customer();
        param.setStatus(0);
        List<CustomerDTO> selectCustList = customerDao.listCustomer(param, CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId, marketProjectId);
        CustomerProperty cp;
        for (CustomerDTO m : selectCustList) {
            // 更新项目和企业的关联状态为2
            if (custIds != null && !custIds.contains(m.getId())) {
                cp = customerDao.getProperty(m.getId(), CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId);
                if (cp != null) {
                    cp.setPropertyValue("");
                    customerDao.saveOrUpdate(cp);
                }
            }
        }

        // 保存选择的项目和企业关联关系
        for (String custId : custIds) {
            cp = new CustomerProperty(custId, CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId, marketProjectId);
            customerDao.saveOrUpdate(cp);
        }

        return 1;
    }

    /**
     * 查询企业关联的项目列表
     *
     * @param custId
     * @param projectUserId 项目管理员用户ID
     * @return
     */
    public List<MarketProjectDTO> listCustomerMarketProject(String custId, String projectUserId) {
        List<MarketProjectDTO> list = marketProjectDao.listCustomerMarketProject(custId);
        // 处理项目管理员负责的项目ID
        if (StringUtil.isNotEmpty(projectUserId)) {
            List<String> projectIds = customerUserDao.listProjectByUserId(NumberConvertUtil.parseLong(projectUserId));
            if (projectIds == null || projectIds.size() == 0) {
                return new ArrayList<>();
            }
            List<MarketProjectDTO> projects = new ArrayList<>();
            for (MarketProjectDTO dto : list) {
                if (projectIds.contains(String.valueOf(dto.getId()))) {
                    projects.add(dto);
                }
            }
            return projects;
        }
        return list;
    }

    /**
     * 前台项目分页
     *
     * @param lu
     * @param custId
     * @param projectName
     * @param id
     * @param status
     * @param startTime
     * @param endTime
     * @param startIndex
     * @param pagesize
     * @param projectUser
     * @return
     */
    public JSONObject getMarketProjectList(LoginUser lu, String custId, String projectName, String id,
                                           Integer status, String startTime, String endTime,
                                           Integer startIndex, Integer pagesize, String projectUser) {
        JSONObject result = new JSONObject();

        String sql = "select t2.name,t2.create_time,t2.id,t2.status,t2.industry_id,(select COUNT(0) from t_customer_sea where market_project_id = t2.id AND cust_id = t1.cust_id) seaNum from t_customer_property t1 left join" +
                " t_market_project t2 on t1.property_value=t2.id  where t1.cust_id='" + custId + "' and t1.property_name like 'marketProject_%' " +
                " and (t1.property_value is not null and t1.property_value!='')";
        List<Map<String, Object>> p_managers = getProjectManager(custId);
        LOG.info("p_managers: " + JSON.toJSONString(p_managers));
        LOG.info(lu.getRole() + ";" + lu.getUserType() + ";" + lu.getId());
        if ("ROLE_CUSTOMER".equals(lu.getRole()) && "3".equals(lu.getUserType())) {
            if (p_managers == null || p_managers.isEmpty()) {
                LOG.info("p_managers is empty");
                return result;
            }
            LOG.info("p_managers.size=" + p_managers.size());
            String idStr = "";
            for (Map<String, Object> map : p_managers) {
                if (lu.getId().toString().equals(String.valueOf(map.get("user_id")))) {
                    String v = (String) map.get("property_value");
                    if (v.startsWith(",")) v = v.substring(1);
                    if (v.endsWith(",")) v = v.substring(0, v.length() - 1);
                    idStr += "," + v;
                }
            }

            LOG.info("uidstr=" + idStr);
            if (idStr.length() > 0) {
                idStr = idStr.substring(1);
            } else {
                return result;
            }
            sql += " and t2.id in(" + idStr + ")";
        }

        if (StringUtil.isNotEmpty(projectName)) {
            sql += " and t2.name='" + projectName + "'";
        }
        if (StringUtil.isNotEmpty(id)) {
            sql += " and t2.id=" + id;
        }
        if (status != null) {
            sql += " and t2.status=" + status;
        }
        if (StringUtil.isNotEmpty(startTime)) {
            startTime = startTime.substring(0, 10);
            startTime = startTime.replaceAll("/", "-");
            sql += " and t2.create_time>='" + startTime + "'";
        }
        if (StringUtil.isNotEmpty(endTime)) {
            endTime = endTime.substring(0, 10);
            endTime = endTime.replaceAll("/", "-");
            sql += " and t2.create_time<='" + endTime + "'";
        }
        // 项目管理员检索
        if (StringUtil.isNotEmpty(projectUser)) {
            CustomerUser customerUser = customerUserDao.getCustomerUserByLoginName(projectUser);
            if (customerUser == null) {
                result.put("data", new ArrayList<>());
                result.put("total", 0);
                return result;
            }
            List<String> projectIds = null;
            projectIds = customerUserDao.listProjectByUserId(customerUser.getId());
            if (projectIds == null || projectIds.size() == 0) {
                result.put("data", new ArrayList<>());
                result.put("total", 0);
                return result;
            }
            sql += " and t2.id IN (" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + ")";
        }
        sql += " order by t2.create_time desc";
        String countSql = "select count(0) from (" + sql + ")a";

        LOG.info("countsql=" + countSql);
        List totalList = customerDao.getSQLQuery(countSql).list();
        if (totalList.size() > 0) {
            BigInteger total = (BigInteger) totalList.get(0);
            if (total.intValue() > 0) {
                sql += " limit " + startIndex + "," + pagesize;
            }
            result.put("total", total.intValue());
        }

        List<Map<String, Object>> projects = customerDao.queryListBySql(sql);
        if (projects == null || projects.isEmpty()) {
            return result;
        }

        for (Map<String, Object> obj : projects) {
            String _sql = "select count(0) from t_market_task where customer_group_id in(select id from customer_group where cust_id='" + custId + "' and market_project_id =" + obj.get("id") + ")";
            String taskNum = customerDao.queryForObject(_sql, null);
            obj.put("taskNum", taskNum);
            obj.put("projectManagerAccount", ""); //项目管理员账号
            obj.put("projectManagerId", "");//项目管理员id
            //行业名称
            obj.put("industryName", "");
            if (obj.get("industry_id") != null) {
                obj.put("industryName", industryInfoDao.getIndustryName(NumberConvertUtil.parseInt(obj.get("industry_id"))));
            }
            if (p_managers != null && p_managers.size() > 0) {
                for (Map<String, Object> map : p_managers) {
                    String projectIds = (String) map.get("property_value");
                    List<String> projectIdList = Arrays.asList(projectIds.split(","));
                    projectIdList.remove(",");
                    if (projectIdList.contains(String.valueOf(obj.get("id")))) {
                        LOG.info("map-=" + map);
                        CustomerUser user = customerUserDao.get(((BigInteger) map.get("user_id")).longValue());
                        if (user != null) {
                            obj.put("projectManagerAccount", user.getAccount()); //项目管理员账号
                        }
                        obj.put("projectManagerId", map.get("user_id"));//项目管理员id
                        break;
                    }
                }
            }
        }
        result.put("data", projects);
        return result;
    }

    /**
     * 客户下的项目管理员列表
     *
     * @param custId
     * @return
     */
    public List<Map<String, Object>> getProjectManager(String custId) {
        String _sql = "select * from t_customer_user where user_type=3 and cust_id=" + custId;
        List<Map<String, Object>> users = customerDao.queryListBySql(_sql);
        Map<String, String> userMap = new HashMap<>();
        if (users != null && users.size() > 0) {
            String ids = "";
            for (Map<String, Object> map : users) {
                ids += "," + map.get("id");
                userMap.put(map.get("id").toString(), map.get("account").toString());
            }
            if (ids.length() > 0) ids = ids.substring(1);
            _sql = "select * from t_customer_user_property where user_id in(" + ids + ") and property_name='hasMarketProject' and property_value is not null and property_value <>''";
            List<Map<String, Object>> userproperties = customerDao.queryListBySql(_sql);
            if (userproperties != null && userproperties.size() > 0) {
                for (Map<String, Object> map : userproperties) {
                    map.put("account", userMap.get(map.get("user_id")));
                }
                return userproperties;
            }
        }
        return null;
    }

    /**
     *
     */
    /**
     * 导出项目成功单
     *
     * @param response
     * @param loginUser
     * @param marketProjectId
     * @param labelId
     * @param labelName
     * @param labelValue
     * @param startTime
     * @param endTime
     */
    public void exportProjectSuccessToExcel(HttpServletResponse response, LoginUser loginUser, int marketProjectId, String labelId, String labelName, String labelValue, String startTime, String endTime) {
        Map<String, Object> msg = new HashMap<>();
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            String custId = loginUser.getCustId();
            MarketProject marketProject = marketProjectDao.selectMarketProject(marketProjectId);
            if (marketProject == null) {
                msg.put("msg", "未查询到项目:" + marketProjectId);
                msg.put("data", projectExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            if (StringUtil.isNotEmpty(custId)) {
                CustomerProperty cp = customerDao.getProperty(custId, CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId);
                if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
                    msg.put("msg", "企业" + custId + "未开通项目:" + marketProjectId);
                    msg.put("data", projectExportTime);
                    LOG.warn("企业" + custId + "未关联项目" + marketProject);
                    outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                    return;
                }
            }
            // 处理导出间隔时间
            if (projectExportTime >= 0 && (System.currentTimeMillis() - projectExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", projectExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            projectExportTime = System.currentTimeMillis();
            List<CustomerDTO> customerList = null;
            // 后台导出项目成功单
            if (StringUtil.isEmpty(custId)) {
                // 查询项目关联的客户
                Customer param = new Customer();
                param.setStatus(0);
                customerList = customerDao.listCustomer(param, CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId, String.valueOf(marketProjectId));
            } else {
                customerList = new ArrayList<>();
                CustomerDTO customerDTO = new CustomerDTO();
                customerDTO.setId(custId);
                customerList.add(customerDTO);
            }
            // 根据客户和项目查询客群
            if (customerList == null || customerList.size() == 0) {
                msg.put("msg", "无满足条件的数据");
                msg.put("data", projectExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            List<List<String>> headers = null, data = null;
            CustomGroup param;
            List<CustomGroupDTO> customGroupList;
            Sheet sheet;
            int i = 1;
            List<CustomerLabelDTO> labels;
            for (CustomerDTO dto : customerList) {
                // 查询指定自建属性id
                if (StringUtil.isEmpty(labelId) && StringUtil.isNotEmpty(labelName)) {
                    labels = customerLabelDao.listLabelIds(dto.getId(), marketProjectId, true);
                    for (CustomerLabelDTO map : labels) {
                        if (labelName.equals(map.getLabelName())) {
                            labelId = map.getLabelId();
                            break;
                        }
                    }
                }
                // 查询客户和项目下的客群列表
                param = new CustomGroup();
                param.setCustId(dto.getId());
                param.setMarketProjectId(marketProjectId);
                customGroupList = customGroupDao.listCustomGroup(param);
                // 根据客户和项目ID查询表头
                headers = getCustomGroupSuccessExcelHeads(dto.getId(), marketProjectId);
                data = new ArrayList<>();
                for (CustomGroupDTO cg : customGroupList) {
                    data.addAll(generateCustomGroupSuccessData(loginUser, cg, dto.getId(), labelId, labelValue, startTime, endTime));
                }
                if (data.size() == 0) {
                    continue;
                }
                sheet = new Sheet(i, 0);
                sheet.setHead(headers);
                sheet.setSheetName(dto.getId() + "-成功单");
                writer.write0(data, sheet);
                i++;
            }
            if (i == 1) {
                msg.put("msg", "无满足条件的数据");
                msg.put("data", projectExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            } else {
                final String fileType = ".xlsx";
                String fileName = "项目-" + marketProject.getName() + "-成功单";
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                writer.finish();
            }
        } catch (Exception e) {
            LOG.error("导出项目成功单异常,Id:" + marketProjectId, e);
        } finally {
            projectExportTime = 0;
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                LOG.error("导出项目成功单异常,Id:" + marketProjectId, e);
            }
        }
    }

    /**
     * 获取客户群导出成功单excel表头
     *
     * @param custId
     * @param marketProjectId
     * @return
     */
    public List<List<String>> getCustomGroupSuccessExcelHeads(String custId, int marketProjectId) {
        List<CustomerLabelDTO> labels = customerLabelDao.listLabelIds(custId, marketProjectId, true);
        // 处理excel表头
        List<List<String>> headers = new ArrayList<>();
        List<String> head;
        Set<String> headNames = new HashSet<>();
        headNames.add("身份ID");
        headNames.add("客户群ID");
        headNames.add("营销任务ID");
        headNames.add("手机号");
        headNames.add("归属地");
        headNames.add("操作人");
        headNames.add("登录账号");
        headNames.add("时间");
        headNames.add("录音");
        headNames.add("意向度");
        headNames.add("人工审核");
        headNames.add("审核失败原因");

        for (CustomerLabelDTO map : labels) {
            if (StringUtil.isNotEmpty(map.getLabelName())) {
                head = new ArrayList<>();
                if (headNames.contains(map.getLabelName())) {
                    head.add(map.getLabelName() + map.getLabelId());
                } else {
                    head.add(map.getLabelName());
                    headNames.add(map.getLabelName());
                }
                headers.add(head);
            }
        }
        head = new ArrayList<>();
        head.add("身份ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("客户群ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("营销任务ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("手机号");
        headers.add(head);

        head = new ArrayList<>();
        head.add("归属地");
        headers.add(head);

        head = new ArrayList<>();
        head.add("操作人");
        headers.add(head);

        head = new ArrayList<>();
        head.add("登录账号");
        headers.add(head);

        head = new ArrayList<>();
        head.add("时间");
        headers.add(head);

        head = new ArrayList<>();
        head.add("录音");
        headers.add(head);

        head = new ArrayList<>();
        head.add("意向度");
        headers.add(head);

        head = new ArrayList<>();
        head.add("人工审核");
        headers.add(head);

        head = new ArrayList<>();
        head.add("审核失败原因");
        headers.add(head);
        return headers;
    }

    /**
     * 生成客群成功单数据
     *
     * @param loginUser
     * @param customGroup
     * @param custId
     * @param labelId
     * @param labelValue
     * @param startTime
     * @param endTime
     * @return
     */
    public List<List<String>> generateCustomGroupSuccessData(LoginUser loginUser, CustomGroupDTO customGroup, String custId, String labelId,
                                                             String labelValue, String startTime, String endTime) {
        List<List<String>> data = new ArrayList<>();
        // 处理时间
        String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DatetimeUtils.DATE_TIME_FORMATTER);
        String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DatetimeUtils.DATE_TIME_FORMATTER);
        if (StringUtil.isNotEmpty(startTime)) {
            startTimeStr = LocalDateTime.parse(startTime, DatetimeUtils.DATE_TIME_FORMATTER).format(DatetimeUtils.DATE_TIME_FORMATTER);
        }
        if (StringUtil.isNotEmpty(endTime)) {
            endTimeStr = LocalDateTime.parse(endTime, DatetimeUtils.DATE_TIME_FORMATTER).format(DatetimeUtils.DATE_TIME_FORMATTER);
        }
        if (StringUtil.isNotEmpty(labelId)) {
            String nowMonth = DateUtil.getNowMonthToYYYYMM();
            if (StringUtil.isNotEmpty(startTimeStr) && StringUtil.isNotEmpty(endTimeStr)) {
                nowMonth = LocalDateTime.parse(endTimeStr, DatetimeUtils.DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyyMM"));
            }
            List<CustomerLabelDTO> labels = customerLabelDao.listLabelIds(custId, customGroup.getMarketProjectId(), true);
            // 获取所有自建属性数据
            List<String> labelIdList = new ArrayList<>();
            for (CustomerLabelDTO map : labels) {
                if (StringUtil.isNotEmpty(map.getLabelName())) {
                    labelIdList.add(map.getLabelId());
                }
            }
            StringBuffer sql = new StringBuffer();
            String likeValue = labelId + "\":\"" + labelValue + "\"";
            // 获取邀约成功,拨打电话成功用户的通话记录
            sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.market_task_id, voice.superid, voice.recordurl, voice.clue_audit_status, voice.clue_audit_reason, ")
                    .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                    .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street, t.intent_level ")
                    .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                    .append(" JOIN " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customGroup.getId() + " t ON t.id = voice.superid ")
                    .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? ")
                    .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                    .append(" AND voice.status = 1001 ")
                    .append(" AND (t.super_data LIKE '%" + likeValue + "%' OR t.intent_level IS NOT NULL)");
            if ("2".equals(loginUser.getUserType())) {
                // 组长查组员列表
                if ("1".equals(loginUser.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), custId);
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }
                    }
                } else {
                    sql.append(" AND voice.user_id = '" + loginUser.getId() + "'");
                }
            }

            List<Map<String, Object>> callLogList = null;
            try {
                callLogList = marketProjectDao.sqlQuery(sql.toString(), custId, customGroup.getId(), startTimeStr, endTimeStr);
            } catch (Exception e) {
                LOG.error("生成客群成功单数据异常,客群ID:" + customGroup.getId(), e);
            }
            // 处理营销记录
            if (callLogList != null && callLogList.size() > 0) {
                final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                // 组合拼装为map,方便通过label_id和super_id快速查找数据
                Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);

                // 当前营销任务下满足条件的身份ID集合
                Set<String> superIdSets = new HashSet<>();
                Set<String> userIdSets = new HashSet<>();
                Map<String, Object> labelData;
                String recordUrl = "";
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("superid") != null) {
                            superIdSets.add(String.valueOf(map.get("superid")));
                        }
                        if (map.get("user_id") != null) {
                            userIdSets.add(String.valueOf(map.get("user_id")));
                        }
                        // 拆解用户勾选的自建属性
                        if (map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                            labelData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                            if (labelData != null && labelData.size() > 0) {
                                for (Map.Entry<String, Object> key : labelData.entrySet()) {
                                    invitationCustGroupSuperMap.put(customGroup.getId() + "_" + map.get("superid"), key.getValue());
                                    invitationSuperLabelMap.put(customGroup.getId() + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                }
                            }
                        }
                    }
                }
                // 查询用户姓名
                Map<String, Object> realNameMap = new HashMap<>();
                Map<String, Object> accountMap = new HashMap<>();
                if (userIdSets.size() > 0) {
                    List<Map<String, Object>> userList = marketProjectDao.sqlQuery("SELECT id, REALNAME, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                    for (Map<String, Object> map : userList) {
                        realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                        accountMap.put(String.valueOf(map.get("id")), map.get("account"));
                    }
                }

                // 根据superId查询手机号
                Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);
                List<String> columnList;
                String monthYear = null;
                for (Map<String, Object> row : callLogList) {
                    columnList = new ArrayList<>();
                    for (String header : labelIdList) {
                        if (invitationSuperLabelMap.get(customGroup.getId() + "_" + header + "_" + row.get("superid")) != null) {
                            columnList.add(String.valueOf(invitationSuperLabelMap.get(customGroup.getId() + "_" + header + "_" + row.get("superid"))));
                        } else {
                            columnList.add("");
                        }
                    }
                    columnList.add(String.valueOf(row.get("superid")));
                    columnList.add(String.valueOf(row.get("customer_group_id")));
                    columnList.add(String.valueOf(row.get("market_task_id")));
                    columnList.add(PhoneAreaUtil.replacePhone(phoneMap.get(String.valueOf(row.get("superid")))));
                    //归属地
                    columnList.add(String.valueOf(row.get("phonearea")));
                    columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                    columnList.add(String.valueOf(accountMap.get(String.valueOf(row.get("user_id")))));
                    columnList.add(String.valueOf(row.get("create_time")));

                    if (StringUtil.isNotEmpty(String.valueOf(row.get("create_time")))) {
                        monthYear = LocalDateTime.parse(String.valueOf(row.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                    }
                    columnList.add(CallUtil.generateRecordUrlMp3(monthYear, row.get("user_id"), row.get("touchId")));
                    columnList.add(String.valueOf(row.get("intent_level")));
                    // 通话审核状态
                    columnList.add(CallUtil.getClueAuditStatusName(String.valueOf(row.get("clue_audit_status"))));
                    // 通话审核失败原因
                    columnList.add(String.valueOf(row.get("clue_audit_reason")));
                    data.add(columnList);
                }
            }
        }
        return data;
    }

    /**
     * 查询项目下的客群用户呼叫统计数据
     *
     * @param timeType
     * @param marketProjectId
     * @param userQueryParam
     * @param startTime
     * @param endTime
     * @return
     */
    public Map<String, Object> statCGUserCallDataByProjectId(int timeType, String marketProjectId, UserQueryParam
            userQueryParam, String startTime, String endTime) {
        Map<String, Object> data = new HashMap<>();
        try {
            if (StringUtil.isEmpty(marketProjectId)) {
                LOG.warn("marketProjectId参数异常");
                return data;
            }
            CustomGroup param = new CustomGroup();
            param.setMarketProjectId(NumberConvertUtil.parseInt(marketProjectId));
            param.setCustId(userQueryParam.getCustId());
            List<CustomGroupDTO> cgList = customGroupDao.listCustomGroup(param);
            if (cgList == null || cgList.size() == 0) {
                LOG.warn("项目:" + marketProjectId + "无客群");
                return data;
            }
            List<String> cgIds = new ArrayList<>();
            for (CustomGroupDTO dto : cgList) {
                cgIds.add(String.valueOf(dto.getId()));
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询项目统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return data;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 查询用户呼叫数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");

            Page page;
            // 处理组长权限
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                } else {
                    sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                }
            }
            sqlSb.append(" GROUP BY user_id ");
            page = this.marketProjectDao.sqlPageQuery0(sqlSb.toString(), userQueryParam.getPageNum(), userQueryParam.getPageSize(), startTime, endTime);

            // 呼叫量,接通量,未通量, 成单量
            long calledSum = 0L, successSum = 0L;
            if (page.getData() != null && page.getData().size() > 0) {
                Map<String, Object> m;
                for (int i = 0; i < page.getData().size(); i++) {
                    m = (Map<String, Object>) page.getData().get(i);
                    System.out.println(m);
                    m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                    calledSum += NumberConvertUtil.parseLong(m.get("called_sum"));
                    successSum += NumberConvertUtil.parseLong(m.get("order_sum"));
                }
            }

            data.put("list", page.getData());
            data.put("total", page.getTotal());
            data.put("calledSum", calledSum);
            // 成功量
            data.put("successSum", successSum);
        } catch (Exception e) {
            LOG.error("获取项目:" + marketProjectId + "统计分析异常,", e);
        }
        return data;
    }

    /**
     * 统计单个项目呼叫数据
     *
     * @param timeType
     * @param marketProjectId
     * @param userQueryParam
     * @param startTime
     * @param endTime
     * @return
     */
    public Map<String, Object> statMarketProjectCallData(int timeType, String marketProjectId, UserQueryParam userQueryParam, String startTime, String endTime, String workPlaceId) {
        Map<String, Object> data = new HashMap<>();
        // 呼叫量
        data.put("callSum", 0);
        // 接通量
        data.put("calledSum", 0);
        // 成功量
        data.put("successSum", 0);
        // 未通量
        data.put("failSum", 0);
        // 参与坐席数
        data.put("callSeatSum", 0);

        data.put("labelListData", new ArrayList<>());
        //通话时长范围统计
        data.put("durationType1", 0);
        data.put("durationType2", 0);
        data.put("durationType3", 0);
        data.put("durationType4", 0);
        data.put("durationType5", 0);
        data.put("durationType6", 0);

        try {
            if (StringUtil.isEmpty(marketProjectId)) {
                LOG.warn("marketProjectId参数异常");
                return data;
            }
            CustomGroup param = new CustomGroup();
            param.setMarketProjectId(NumberConvertUtil.parseInt(marketProjectId));
            param.setCustId(userQueryParam.getCustId());
            List<CustomGroupDTO> cgList = customGroupDao.listCustomGroup(param);
            if (cgList == null || cgList.size() == 0) {
                LOG.warn("项目:" + marketProjectId + "无客群");
                return data;
            }
            List<String> cgIds = new ArrayList<>();
            for (CustomGroupDTO dto : cgList) {
                cgIds.add(String.valueOf(dto.getId()));
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询营销任务统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return data;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));
            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum, IFNULL(SUM(called_duration_type1),0) durationType1, IFNULL(SUM(called_duration_type2),0) durationType2, IFNULL(SUM(called_duration_type3),0) durationType3, IFNULL(SUM(called_duration_type4),0) durationType4, " +
                    " IFNULL(SUM(called_duration_type5),0) durationType5, IFNULL(SUM(called_duration_type6),0) durationType6, count(distinct(user_id)) callSeatSum FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");

            List<Map<String, Object>> statCallList;
            // 通话记录查询用户权限
            Set voiceUserIds = new HashSet();
            //普通用户权限处理
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiceUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        voiceUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    voiceUserIds.add(userQueryParam.getUserId());
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    return data;
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            statCallList = this.marketProjectDao.sqlQuery(sqlSb.toString(), startTime, endTime);
            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L, callSeatSum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L, durationType1 = 0L, durationType2 = 0L, durationType3 = 0L, durationType4 = 0L, durationType5 = 0L, durationType6 = 0L;
            if (statCallList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(statCallList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(statCallList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(statCallList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(statCallList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(statCallList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(statCallList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(statCallList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(statCallList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(statCallList.get(0).get("order_sum"));
                callSeatSum = NumberConvertUtil.parseLong(statCallList.get(0).get("callSeatSum"));
                // 通话时长范围统计
                durationType1 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType1", 0));
                durationType2 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType2", 0));
                durationType3 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType3", 0));
                durationType4 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType4", 0));
                durationType5 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType5", 0));
                durationType6 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType6", 0));
            }

            // 呼叫量
            data.put("callSum", callSum);
            // 接通量
            data.put("calledSum", calledSum);
            // 未通量
            data.put("failSum", failSum);
            data.put("busySum", busySum);
            data.put("noServiceSum", noServiceSum);
            data.put("phoneOverdueSum", phoneOverdueSum);
            data.put("phoneShutdownSum", phoneShutdownSum);
            data.put("spacePhoneSum", spacePhoneSum);
            data.put("otherSum", otherSum);
            data.put("callSeatSum", callSeatSum);
            // 成功量
            data.put("successSum", successSum);
            // 通话范围统计
            data.put("durationType1", durationType1);
            data.put("durationType2", durationType2);
            data.put("durationType3", durationType3);
            data.put("durationType4", durationType4);
            data.put("durationType5", durationType5);
            data.put("durationType6", durationType6);

            // 处理自建属性标记数据
            Map<Object, Object> singleLabel = customerLabelService.getCustomAndSystemLabel(userQueryParam.getCustId());

            // 查询自建属性标记数据
            StringBuilder superSql = new StringBuilder();
            superSql.append("SELECT label_id, GROUP_CONCAT(option_value) option_value, GROUP_CONCAT(tag_sum) tag_sum, IFNULL(SUM(tag_sum),0) sum FROM stat_u_label_data WHERE customer_group_id IN(" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ") AND stat_time BETWEEN ? AND ?  ");
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    return data;
                }
                superSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            superSql.append("GROUP BY label_id");

            List<Map<String, Object>> labelList = customGroupDao.sqlQuery(superSql.toString(), startTime, endTime);
            List<Map<String, Object>> labelListData = new ArrayList<>();
            if (labelList != null && labelList.size() > 0) {
                List<String> labelOptionNameList;
                Map<String, Object> labelMapData, valueMap;
                Map<String, Integer> optionValueMap;
                List<Map<String, Object>> labelOptionListData;
                String optionValue, tagSum, percent;
                String[] options, tags;
                long sum = 0;
                for (Map<String, Object> m : labelList) {
                    labelOptionListData = new ArrayList<>();
                    labelOptionNameList = new ArrayList<>();
                    labelMapData = new HashMap<>();
                    optionValueMap = new HashMap<>();
                    labelMapData.put("labelId", m.get("label_id"));
                    labelMapData.put("title", singleLabel.get(m.get("label_id")));
                    optionValue = String.valueOf(m.get("option_value"));
                    tagSum = String.valueOf(m.get("tag_sum"));
                    // 单个自建属性标记总数
                    sum = NumberConvertUtil.parseLong(m.get("sum"));
                    if (StringUtil.isNotEmpty(optionValue) && StringUtil.isNotEmpty(tagSum)) {
                        options = optionValue.split(",");
                        tags = tagSum.split(",");
                        for (int i = 0; i < options.length; i++) {
                            if (optionValueMap.get(options[i]) != null) {
                                optionValueMap.put(options[i], optionValueMap.get(options[i]) + NumberConvertUtil.parseInt(tags[i]));
                            } else {
                                optionValueMap.put(options[i], NumberConvertUtil.parseInt(tags[i]));
                            }
                        }
                        for (Map.Entry<String, Integer> v : optionValueMap.entrySet()) {
                            valueMap = new HashMap<>();
                            valueMap.put("name", v.getKey());
                            valueMap.put("count", v.getValue());
                            percent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(v.getValue()), sum);
                            valueMap.put("percent", percent);
                            labelOptionNameList.add(v.getKey());
                            labelOptionListData.add(valueMap);
                        }
                    }
                    labelMapData.put("names", labelOptionNameList);
                    labelMapData.put("values", labelOptionListData);
                    labelListData.add(labelMapData);
                }
            }
            data.put("labelListData", labelListData);
        } catch (Exception e) {
            LOG.error("获取项目:" + marketProjectId + "统计分析异常,", e);
        }
        return data;
    }


    public void exportMarketProjectCallData(int timeType, String marketProjectId, UserQueryParam userQueryParam, String startTime, String endTime, HttpServletResponse response, String workPlaceId) {
        if (StringUtil.isEmpty(marketProjectId)) {
            LOG.warn("marketProjectId参数异常");
            return;
        }
        try (OutputStream outputStream = response.getOutputStream()) {
            CustomGroup param = new CustomGroup();
            param.setMarketProjectId(NumberConvertUtil.parseInt(marketProjectId));
            param.setCustId(userQueryParam.getCustId());
            List<CustomGroupDTO> cgList = customGroupDao.listCustomGroup(param);
            if (cgList == null || cgList.size() == 0) {
                LOG.warn("项目:" + marketProjectId + "无客群");
                return;
            }
            List<String> cgIds = new ArrayList<>();
            for (CustomGroupDTO dto : cgList) {
                cgIds.add(String.valueOf(dto.getId()));
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询营销任务统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum, IFNULL(SUM(called_duration_type1),0) durationType1, IFNULL(SUM(called_duration_type2),0) durationType2, IFNULL(SUM(called_duration_type3),0) durationType3, IFNULL(SUM(called_duration_type4),0) durationType4, " +
                    " IFNULL(SUM(called_duration_type5),0) durationType5, IFNULL(SUM(called_duration_type6),0) durationType6, count(distinct(user_id)) callSeatSum FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");

            List<Map<String, Object>> statList;
            // 通话记录查询用户权限
            Set voiceUserIds = new HashSet();
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiceUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        voiceUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    voiceUserIds.add(userQueryParam.getUserId());
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            statList = this.marketProjectDao.sqlQuery(sqlSb.toString(), startTime, endTime);
            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L, callSeatSum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L, durationType1 = 0L, durationType2 = 0L, durationType3 = 0L, durationType4 = 0L, durationType5 = 0L, durationType6 = 0L;
            if (statList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(statList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(statList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(statList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(statList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(statList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(statList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(statList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(statList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(statList.get(0).get("order_sum"));
                callSeatSum = NumberConvertUtil.parseLong(statList.get(0).get("callSeatSum"));
                // 通话时长范围统计
                durationType1 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType1", 0));
                durationType2 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType2", 0));
                durationType3 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType3", 0));
                durationType4 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType4", 0));
                durationType5 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType5", 0));
                durationType6 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType6", 0));
            }

            String fileName = "项目统计数据-" + marketProjectId + "-" + System.currentTimeMillis();
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            int sheetNum = 1;
            List<List<String>> data, headers;
            List<String> columnList, head;

            data = new ArrayList<>();

            headers = new ArrayList<>();
            head = new ArrayList<>();
            head.add("参与员工数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("呼叫量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("未通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);
            //构造数据
            columnList = new ArrayList<>();
            //参与员工数
            columnList.add(String.valueOf(callSeatSum));
            //呼叫量
            columnList.add(String.valueOf(callSum));
            //接通量
            columnList.add(String.valueOf(calledSum));
            //未通量
            columnList.add(String.valueOf(failSum));
            //成功量
            columnList.add(String.valueOf(successSum));
            //接通率
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSum, callSum)));
            }
            //成功率
            if (NumberConvertUtil.parseLong(calledSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSum, calledSum)));
            }

            data.add(columnList);

            Sheet sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("外呼数据统计");
            writer.write0(data, sheet);

            // 构造未接通号码统计
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("未通总量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("用户忙");
            headers.add(head);

            head = new ArrayList<>();
            head.add("不在服务区");
            headers.add(head);

            head = new ArrayList<>();
            head.add("停机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("关机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("空号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("其他");
            headers.add(head);
            //构造数量数据
            columnList = new ArrayList<>();
            columnList.add(String.valueOf(failSum));
            columnList.add(String.valueOf(busySum));
            columnList.add(String.valueOf(noServiceSum));
            columnList.add(String.valueOf(phoneOverdueSum));
            columnList.add(String.valueOf(phoneShutdownSum));
            columnList.add(String.valueOf(spacePhoneSum));
            columnList.add(String.valueOf(otherSum));
            data.add(columnList);

            //构造占比数据
            columnList = new ArrayList<>();
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(failSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(busySum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(noServiceSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(spacePhoneSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(otherSum, callSum)) : "0");
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("未接通号码统计");
            writer.write0(data, sheet);

            //　构造用户呼叫列表数据
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("员工");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总通话时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("平均通话时长");
            headers.add(head);

            // 查询用户呼叫数
            sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id IN(" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");

                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                } else {
                    sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");

                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            sqlSb.append(" GROUP BY user_id ");
            List<Map<String, Object>> list = this.marketProjectDao.sqlQuery(sqlSb.toString(), startTime, endTime);

            Map<String, Object> m;
            for (int i = 0; i < list.size(); i++) {
                m = list.get(i);
                m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                columnList = new ArrayList<>();
                columnList.add(String.valueOf(m.get("userName")));
                columnList.add(String.valueOf(m.get("called_sum")));
                columnList.add(String.valueOf(m.get("order_sum")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add(String.valueOf(0));
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(m.get("order_sum")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                columnList.add(String.valueOf(m.get("called_duration")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add("0");
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.divNumber(NumberConvertUtil.parseLong(m.get("called_duration")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                data.add(columnList);

            }
            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("员工统计");
            writer.write0(data, sheet);

            // 处理自建属性标记数据
            Map<Object, Object> singleLabel = customerLabelService.getCustomAndSystemLabel(userQueryParam.getCustId());

            // 查询自建属性标记数据
            StringBuilder superSql = new StringBuilder();
            superSql.append("SELECT label_id, GROUP_CONCAT(option_value) option_value, GROUP_CONCAT(tag_sum) tag_sum, IFNULL(SUM(tag_sum),0) sum FROM stat_u_label_data WHERE customer_group_id IN(" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ") AND stat_time BETWEEN ? AND ?  ");
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                superSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            superSql.append(" GROUP BY label_id");
            List<Map<String, Object>> labelList = customGroupDao.sqlQuery(superSql.toString(), startTime, endTime);
            String optionValue, tagSum, percent;
            long sum;
            String[] options, tags;
            Map<String, Integer> optionValueMap;
            Set<String> labelSheetName = new HashSet<>();
            for (Map<String, Object> map : labelList) {
                data = new ArrayList<>();
                headers = new ArrayList<>();
                optionValueMap = new HashMap<>();

                head = new ArrayList<>();
                head.add(String.valueOf(singleLabel.get(map.get("label_id"))));
                headers.add(head);

                head = new ArrayList<>();
                head.add("数量");
                headers.add(head);

                head = new ArrayList<>();
                head.add("占比");
                headers.add(head);

                optionValue = String.valueOf(map.get("option_value"));
                tagSum = String.valueOf(map.get("tag_sum"));
                // 单个自建属性标记总数
                sum = NumberConvertUtil.parseLong(map.get("sum"));
                if (StringUtil.isNotEmpty(optionValue) && StringUtil.isNotEmpty(tagSum)) {
                    options = optionValue.split(",");
                    tags = tagSum.split(",");
                    for (int i = 0; i < options.length; i++) {
                        if (optionValueMap.get(options[i]) != null) {
                            optionValueMap.put(options[i], optionValueMap.get(options[i]) + NumberConvertUtil.parseInt(tags[i]));
                        } else {
                            optionValueMap.put(options[i], NumberConvertUtil.parseInt(tags[i]));
                        }
                    }
                    for (Map.Entry<String, Integer> v : optionValueMap.entrySet()) {
                        percent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(v.getValue()), sum);
                        columnList = new ArrayList<>();
                        columnList.add(v.getKey());
                        columnList.add(String.valueOf(v.getValue()));
                        columnList.add(percent);
                        data.add(columnList);
                    }
                }
                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                // Sheet已经存在
                if (labelSheetName.contains(String.valueOf(singleLabel.get(map.get("label_id"))))) {
                    sheet.setSheetName(String.valueOf(singleLabel.get(map.get("label_id"))) + map.get("label_id"));
                } else {
                    sheet.setSheetName(String.valueOf(singleLabel.get(map.get("label_id"))));
                }
                labelSheetName.add(String.valueOf(singleLabel.get(map.get("label_id"))));
                writer.write0(data, sheet);
            }
            // 通话时长分布统计
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("1-3秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("4-6秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("7-12秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("13-30秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("31-60秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("60秒以上");
            headers.add(head);

            //构造数据
            columnList = new ArrayList<>();
            columnList.add(String.valueOf(durationType1));
            columnList.add(String.valueOf(durationType2));
            columnList.add(String.valueOf(durationType3));
            columnList.add(String.valueOf(durationType4));
            columnList.add(String.valueOf(durationType5));
            columnList.add(String.valueOf(durationType6));
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("通话时长分布统计");
            writer.write0(data, sheet);

            writer.finish();
        } catch (Exception e) {
            LOG.error("导出项目:" + marketProjectId + "统计分析异常,", e);
        }
    }


    public void exportMarketProjectCallData0(int timeType, String marketProjectId, UserQueryParam userQueryParam, String startTime, String endTime, HttpServletResponse response, String workPlaceId) {
        if (StringUtil.isEmpty(marketProjectId)) {
            LOG.warn("marketProjectId参数异常");
            return;
        }
        MarketProject marketProject = marketProjectDao.selectMarketProject(NumberConvertUtil.parseInt(marketProjectId));
        if (marketProject == null) {
            LOG.warn("项目:[" + marketProjectId + "]不存在");
            return;
        }
        try (OutputStream outputStream = response.getOutputStream()) {
            CustomGroup param = new CustomGroup();
            param.setMarketProjectId(NumberConvertUtil.parseInt(marketProjectId));
            param.setCustId(userQueryParam.getCustId());
            List<CustomGroupDTO> cgList = customGroupDao.listCustomGroup(param);
            if (cgList == null || cgList.size() == 0) {
                LOG.warn("项目:" + marketProjectId + "无客群");
                return;
            }
            List<String> cgIds = new ArrayList<>();
            for (CustomGroupDTO dto : cgList) {
                cgIds.add(String.valueOf(dto.getId()));
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询营销任务统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT customer_group_id, market_task_id, IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum, IFNULL(SUM(called_duration_type1),0) durationType1, IFNULL(SUM(called_duration_type2),0) durationType2, IFNULL(SUM(called_duration_type3),0) durationType3, IFNULL(SUM(called_duration_type4),0) durationType4, " +
                    " IFNULL(SUM(called_duration_type5),0) durationType5, IFNULL(SUM(called_duration_type6),0) durationType6, count(distinct(user_id)) callSeatSum FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");

            List<Map<String, Object>> statList;
            // 通话记录查询用户权限
            Set voiceUserIds = new HashSet();
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiceUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        voiceUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    voiceUserIds.add(userQueryParam.getUserId());
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            Map<String, Integer> sheetMergeIndex = new HashMap<>();
            Map<String, String> sheetMergeName = new HashMap<>();
            String fileName = "项目统计数据-" + marketProjectId + "-" + System.currentTimeMillis();
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(null, outputStream, ExcelTypeEnum.XLSX, true, new ExcelAfterWriteHandlerImpl(sheetMergeIndex, sheetMergeName));
            int sheetNum = 1;
            List<List<String>> data, dataSheetTwo, headers;
            List<String> columnList, head;

            data = new ArrayList<>();
            dataSheetTwo = new ArrayList<>();
            headers = new ArrayList<>();

            String[] heads = new String[]{"项目名称", "时间", "任务ID", "客户群ID", "参与员工数", "呼叫量", "接通量", "未通量", "成功量", "接通率", "成功率"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }
            statList = this.marketProjectDao.sqlQuery(sqlSb.toString(), startTime, endTime);
            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L, callSeatSum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L, durationType1 = 0L, durationType2 = 0L, durationType3 = 0L, durationType4 = 0L, durationType5 = 0L, durationType6 = 0L;
            long callSumS = 0L, calledSumS = 0L, failSumS = 0L, successSumS = 0L, busySumS = 0L, callSeatSumS = 0L,
                    noServiceSumS = 0L, phoneOverdueSumS = 0L, phoneShutdownSumS = 0L, spacePhoneSumS = 0L, otherSumS = 0L, durationType1S = 0L, durationType2S = 0L, durationType3S = 0L, durationType4S = 0L, durationType5S = 0L, durationType6S = 0L;
            if (statList.size() > 0) {
                for (int i = 0; i < statList.size(); i++) {
                    callSum = NumberConvertUtil.parseLong(statList.get(i).get("caller_sum"));
                    calledSum = NumberConvertUtil.parseLong(statList.get(i).get("called_sum"));
                    busySum = NumberConvertUtil.parseLong(statList.get(i).get("busy_sum"));
                    noServiceSum = NumberConvertUtil.parseLong(statList.get(i).get("no_service_area_sum"));
                    phoneOverdueSum = NumberConvertUtil.parseLong(statList.get(i).get("phone_overdue_sum"));
                    phoneShutdownSum = NumberConvertUtil.parseLong(statList.get(i).get("phone_shutdown_sum"));
                    spacePhoneSum = NumberConvertUtil.parseLong(statList.get(i).get("space_phone_sum"));
                    otherSum = NumberConvertUtil.parseLong(statList.get(i).get("other_sum"));
                    failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                    successSum = NumberConvertUtil.parseLong(statList.get(i).get("order_sum"));
                    callSeatSum = NumberConvertUtil.parseLong(statList.get(i).get("callSeatSum"));

                    callSumS += callSum;
                    calledSumS += calledSum;
                    busySumS += busySumS;
                    noServiceSumS += noServiceSum;
                    phoneOverdueSumS += phoneOverdueSum;
                    phoneShutdownSumS += phoneShutdownSum;
                    spacePhoneSumS += spacePhoneSum;
                    otherSumS += otherSum;
                    failSumS += failSum;
                    successSumS += successSum;
                    callSeatSumS += callSeatSum;

                    //构造数据
                    columnList = new ArrayList<>();
                    //项目名称
                    columnList.add(marketProject.getName());
                    //时间
                    columnList.add(startTime + "-" + endTime);
                    //任务ID
                    columnList.add(String.valueOf(statList.get(i).get("market_task_id")));
                    //客群ID
                    columnList.add(String.valueOf(statList.get(i).get("customer_group_id")));

                    //参与员工数
                    columnList.add(String.valueOf(callSeatSum));
                    //呼叫量
                    columnList.add(String.valueOf(callSum));
                    //接通量
                    columnList.add(String.valueOf(calledSum));
                    //未通量
                    columnList.add(String.valueOf(failSum));
                    //成功量
                    columnList.add(String.valueOf(successSum));
                    //接通率
                    if (NumberConvertUtil.parseLong(callSum) == 0) {
                        columnList.add(String.valueOf(0));
                    } else {
                        columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSum, callSum)));
                    }
                    //成功率
                    if (NumberConvertUtil.parseLong(calledSum) == 0) {
                        columnList.add(String.valueOf(0));
                    } else {
                        columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSum, calledSum)));
                    }
                    data.add(columnList);

                    //构造未通号码统计数据
                    columnList = new ArrayList<>();
                    //任务ID
                    columnList.add(String.valueOf(statList.get(i).get("market_task_id")));
                    //客群ID
                    columnList.add(String.valueOf(statList.get(i).get("customer_group_id")));
                    columnList.add(String.valueOf(failSum));
                    columnList.add(String.valueOf(busySum));
                    columnList.add(String.valueOf(noServiceSum));
                    columnList.add(String.valueOf(phoneOverdueSum));
                    columnList.add(String.valueOf(phoneShutdownSum));
                    columnList.add(String.valueOf(spacePhoneSum));
                    columnList.add(String.valueOf(otherSum));
                    dataSheetTwo.add(columnList);

                    //构造未通号码占比数据
                    columnList = new ArrayList<>();
                    //任务ID
                    columnList.add(String.valueOf(statList.get(i).get("market_task_id")));
                    //客群ID
                    columnList.add(String.valueOf(statList.get(i).get("customer_group_id")));
                    columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(failSum, callSum)) + "%" : "0%");
                    columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(busySum, callSum)) + "%" : "0%");
                    columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(noServiceSum, callSum)) + "%" : "0%");
                    columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSum, callSum)) + "%" : "0%");
                    columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSum, callSum)) + "%" : "0%");
                    columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(spacePhoneSum, callSum)) + "%" : "0%");
                    columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(otherSum, callSum)) + "%" : "0%");
                    dataSheetTwo.add(columnList);
                }
            }

            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add("");
            columnList.add("");
            //参与坐席数
            columnList.add(String.valueOf(callSeatSumS));
            //呼叫量
            columnList.add(String.valueOf(callSumS));
            //接通量
            columnList.add(String.valueOf(calledSumS));
            //未通量
            columnList.add(String.valueOf(failSumS));
            //成功量
            columnList.add(String.valueOf(successSumS));
            //接通率
            if (NumberConvertUtil.parseLong(callSumS) == 0) {
                columnList.add(String.valueOf(0) + "%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSumS, callSumS)) + "%");
            }
            //成功率
            if (NumberConvertUtil.parseLong(calledSumS) == 0) {
                columnList.add(String.valueOf(0) + "%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSumS, calledSumS)) + "%");
            }
            data.add(columnList);
            sheetMergeIndex.put("外呼数据统计", 3);
            sheetMergeName.put("外呼数据统计", "汇总");
            Sheet sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("外呼数据统计");
            writer.write0(data, sheet);

            // 构造未接通号码统计
            headers = new ArrayList<>();
            heads = new String[]{"任务ID", "客户群ID", "未通总量", "用户忙", "不在服务区", "停机", "关机", "空号", "其他"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }
            //构造数量数据
            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add(String.valueOf(failSumS));
            columnList.add(String.valueOf(busySumS));
            columnList.add(String.valueOf(noServiceSumS));
            columnList.add(String.valueOf(phoneOverdueSumS));
            columnList.add(String.valueOf(phoneShutdownSumS));
            columnList.add(String.valueOf(spacePhoneSumS));
            columnList.add(String.valueOf(otherSumS));
            dataSheetTwo.add(columnList);

            //构造占比数据
            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add(NumberConvertUtil.parseLong(callSumS) > 0 ? String.valueOf(NumberConvertUtil.getPercent(failSumS, callSumS)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSumS) > 0 ? String.valueOf(NumberConvertUtil.getPercent(busySumS, callSumS)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSumS) > 0 ? String.valueOf(NumberConvertUtil.getPercent(noServiceSumS, callSumS)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSumS) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSumS, callSumS)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSumS) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSumS, callSumS)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSumS) > 0 ? String.valueOf(NumberConvertUtil.getPercent(spacePhoneSumS, callSumS)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSumS) > 0 ? String.valueOf(NumberConvertUtil.getPercent(otherSumS, callSumS)) + "%" : "0%");
            dataSheetTwo.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("未接通号码统计");
            sheetMergeIndex.put("未接通号码统计", 1);
            sheetMergeName.put("未接通号码统计", "汇总");
            writer.write0(dataSheetTwo, sheet);

            //　构造用户呼叫列表数据
            data = new ArrayList<>();
            headers = new ArrayList<>();
            heads = new String[]{"任务ID", "客户群ID", "员工", "接通数", "成功数", "成功率", "总通话时长", "平均通话时长"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }

            // 查询用户呼叫数
            sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id,market_task_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id IN(" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");

                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                } else {
                    sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");

                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            sqlSb.append(" GROUP BY user_id ");
            List<Map<String, Object>> list = this.marketProjectDao.sqlQuery(sqlSb.toString(), startTime, endTime);

            Map<String, Object> m;
            long userCalledSum = 0L, userOrderSum = 0L, userDuration = 0L;
            for (int i = 0; i < list.size(); i++) {
                m = list.get(i);
                m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                columnList = new ArrayList<>();
                columnList.add(String.valueOf(m.get("customer_group_id")));
                columnList.add(String.valueOf(m.get("market_task_id")));
                columnList.add(String.valueOf(m.get("userName")));
                columnList.add(String.valueOf(m.get("called_sum")));
                userCalledSum += NumberConvertUtil.parseLong(m.get("called_sum"));
                columnList.add(String.valueOf(m.get("order_sum")));
                userOrderSum += NumberConvertUtil.parseLong(m.get("order_sum"));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add("0%");
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(m.get("order_sum")), NumberConvertUtil.parseLong(m.get("called_sum")))) + "%");
                }
                columnList.add(String.valueOf(m.get("called_duration")));
                userDuration += NumberConvertUtil.parseLong(m.get("called_duration"));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add("0%");
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.divNumber(NumberConvertUtil.parseLong(m.get("called_duration")), NumberConvertUtil.parseLong(m.get("called_sum")))) + "%");
                }
                data.add(columnList);

            }
            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add("");
            columnList.add(String.valueOf(userCalledSum));
            columnList.add(String.valueOf(userOrderSum));
            if (calledSum == 0) {
                columnList.add("0%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(userOrderSum, calledSum) + "%"));
            }
            columnList.add(String.valueOf(userDuration));
            if (userDuration == 0) {
                columnList.add("0%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.divNumber(userDuration, userCalledSum)) + "%");
            }
            data.add(columnList);
            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("员工统计");
            sheetMergeIndex.put("员工统计", 2);
            sheetMergeName.put("员工统计", "汇总");
            writer.write0(data, sheet);

            // 处理自建属性标记数据
            Map<Object, Object> singleLabel = customerLabelService.getCustomAndSystemLabel(userQueryParam.getCustId());

            // 查询自建属性标记数据
            StringBuilder superSql = new StringBuilder();
            superSql.append("SELECT customer_group_id, market_task_id, label_id, GROUP_CONCAT(option_value) option_value, GROUP_CONCAT(tag_sum) tag_sum, IFNULL(SUM(tag_sum),0) sum FROM stat_u_label_data WHERE customer_group_id IN(" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ") AND stat_time BETWEEN ? AND ? ");
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                superSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            superSql.append(" GROUP BY label_id, customer_group_id, market_task_id ");
            List<Map<String, Object>> labelList = customGroupDao.sqlQuery(superSql.toString(), startTime, endTime);
            String optionValue, tagSum, percent;
            long sum;
            String[] options, tags;
            Map<Object, List<Map<String, Object>>> labelData = new HashMap<>();
            List labelL;

            for (Map<String, Object> map : labelList) {
                if (labelData.get(map.get("label_id")) == null) {
                    labelL = new ArrayList();
                } else {
                    labelL = labelData.get(map.get("label_id"));
                }
                labelL.add(map);
                labelData.put(map.get("label_id"), labelL);
            }
            Map<String, Integer> optionValueMap;
            Map<String, Long> labelCount;
            Set<String> labelSheetName = new HashSet<>();
            int tagSums;
            for (Map.Entry<Object, List<Map<String, Object>>> l : labelData.entrySet()) {
                tagSums = 0;
                labelCount = new HashMap<>();
                data = new ArrayList<>();
                headers = new ArrayList<>();

                head = new ArrayList<>();
                head.add("任务ID");
                headers.add(head);

                head = new ArrayList<>();
                head.add("客户群ID");
                headers.add(head);

                head = new ArrayList<>();
                head.add(String.valueOf(singleLabel.get(l.getKey())));
                headers.add(head);

                head = new ArrayList<>();
                head.add("数量");
                headers.add(head);

                head = new ArrayList<>();
                head.add("占比");
                headers.add(head);

                for (Map<String, Object> k : l.getValue()) {
                    optionValueMap = new HashMap<>();
                    optionValue = String.valueOf(k.get("option_value"));
                    tagSum = String.valueOf(k.get("tag_sum"));
                    // 单个自建属性标记总数
                    sum = NumberConvertUtil.parseLong(k.get("sum"));
                    tagSums += sum;
                    if (StringUtil.isNotEmpty(optionValue) && StringUtil.isNotEmpty(tagSum)) {
                        options = optionValue.split(",");
                        tags = tagSum.split(",");
                        for (int i = 0; i < options.length; i++) {
                            if (optionValueMap.get(options[i]) != null) {
                                optionValueMap.put(options[i], optionValueMap.get(options[i]) + NumberConvertUtil.parseInt(tags[i]));
                            } else {
                                optionValueMap.put(options[i], NumberConvertUtil.parseInt(tags[i]));
                            }
                        }
                        for (Map.Entry<String, Integer> v : optionValueMap.entrySet()) {
                            percent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(v.getValue()), sum);
                            columnList = new ArrayList<>();
                            columnList.add(String.valueOf(k.get("market_task_id")));
                            columnList.add(String.valueOf(k.get("customer_group_id")));
                            columnList.add(v.getKey());
                            columnList.add(String.valueOf(v.getValue()));
                            columnList.add(percent + "%");
                            data.add(columnList);
                            if (labelCount.get(v.getKey()) == null) {
                                labelCount.put(v.getKey(), NumberConvertUtil.parseLong(v.getValue()));
                            } else {
                                labelCount.put(v.getKey(), labelCount.get(v.getKey()) + NumberConvertUtil.parseLong(v.getValue()));
                            }
                        }
                    }
                }
                for (Map.Entry<String, Long> k : labelCount.entrySet()) {
                    columnList = new ArrayList<>();
                    columnList.add("汇总");
                    columnList.add("");
                    columnList.add(k.getKey());
                    columnList.add(String.valueOf(k.getValue()));
                    columnList.add(NumberConvertUtil.getPercent(k.getValue(), tagSums) + "%");
                    data.add(columnList);
                }

                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                // Sheet已经存在
                if (labelSheetName.contains(String.valueOf(singleLabel.get(l.getKey())))) {
                    sheet.setSheetName(String.valueOf(singleLabel.get(l.getKey())) + l.getKey());
                    sheetMergeIndex.put(String.valueOf(singleLabel.get(l.getKey())) + l.getKey(), 1);
                    sheetMergeName.put(String.valueOf(singleLabel.get(l.getKey())) + l.getKey(), "汇总");
                } else {
                    sheet.setSheetName(String.valueOf(singleLabel.get(l.getKey())));
                    sheetMergeIndex.put(String.valueOf(singleLabel.get(l.getKey())), 1);
                    sheetMergeName.put(String.valueOf(singleLabel.get(l.getKey())), "汇总");
                }
                labelSheetName.add(String.valueOf(singleLabel.get(l.getKey())));

                writer.write0(data, sheet);
            }
            // 通话时长分布统计
            data = new ArrayList<>();
            headers = new ArrayList<>();
            heads = new String[]{"任务ID", "客户群ID", "1-3秒", "4-6秒", "7-12秒", "13-30秒", "31-60秒", "60秒以上"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }
            if (statList.size() > 0) {
                for (int i = 0; i < statList.size(); i++) {
                    // 通话时长范围统计
                    durationType1 = NumberConvertUtil.parseLong(statList.get(i).getOrDefault("durationType1", 0));
                    durationType2 = NumberConvertUtil.parseLong(statList.get(i).getOrDefault("durationType2", 0));
                    durationType3 = NumberConvertUtil.parseLong(statList.get(i).getOrDefault("durationType3", 0));
                    durationType4 = NumberConvertUtil.parseLong(statList.get(i).getOrDefault("durationType4", 0));
                    durationType5 = NumberConvertUtil.parseLong(statList.get(i).getOrDefault("durationType5", 0));
                    durationType6 = NumberConvertUtil.parseLong(statList.get(i).getOrDefault("durationType6", 0));

                    // 通话时长范围统计
                    durationType1S += durationType1;
                    durationType2S += durationType2;
                    durationType3S += durationType3;
                    durationType4S += durationType4;
                    durationType5S += durationType5;
                    durationType6S += durationType6;

                    //构造数据
                    columnList = new ArrayList<>();
                    columnList.add(String.valueOf(statList.get(i).get("market_task_id")));
                    columnList.add(String.valueOf(statList.get(i).get("customer_group_id")));
                    columnList.add(String.valueOf(durationType1));
                    columnList.add(String.valueOf(durationType2));
                    columnList.add(String.valueOf(durationType3));
                    columnList.add(String.valueOf(durationType4));
                    columnList.add(String.valueOf(durationType5));
                    columnList.add(String.valueOf(durationType6));
                    data.add(columnList);
                }
            }

            //构造数据
            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add(String.valueOf(""));
            columnList.add(String.valueOf(durationType1S));
            columnList.add(String.valueOf(durationType2S));
            columnList.add(String.valueOf(durationType3S));
            columnList.add(String.valueOf(durationType4S));
            columnList.add(String.valueOf(durationType5S));
            columnList.add(String.valueOf(durationType6S));
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("通话时长分布统计");
            sheetMergeIndex.put("通话时长分布统计", 1);
            sheetMergeName.put("通话时长分布统计", "汇总");
            writer.write0(data, sheet);

            writer.finish();
        } catch (Exception e) {
            LOG.error("导出项目:[" + marketProjectId + "]统计分析异常,", e);
        }
    }

    /**
     * 查询项目属性
     *
     * @param custId
     * @param marketProjectId
     * @param propertyName
     * @return
     */
    public MarketProjectProperty getProperty(String custId, String marketProjectId, String propertyName) {
        // 判断项目是否属于客户
        CustomerProperty cp = customerDao.getProperty(custId, CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId);
        if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
            LOG.warn("查询项目属性项目:" + marketProjectId + "未关联客户:" + custId);
            return null;
        }
        return marketProjectDao.getProperty(marketProjectId, propertyName);
    }

    /**
     * 保存项目属性
     *
     * @param custId
     * @param marketProjectId
     * @param propertyName
     * @param propertyValue
     * @return
     */
    public int saveProperty(String custId, String marketProjectId, String propertyName, String propertyValue) {
        // 判断项目是否属于客户
        CustomerProperty cp = customerDao.getProperty(custId, CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + marketProjectId);
        if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
            LOG.warn("保存项目属性项目:" + marketProjectId + "未关联客户:" + custId);
            return 0;
        }
        MarketProjectProperty mp = new MarketProjectProperty(marketProjectId, propertyName, propertyValue, new Timestamp(System.currentTimeMillis()));
        marketProjectDao.saveOrUpdate(mp);
        // 保存执行组时处理讯众自动外呼成员
        if (StringUtil.isNotEmpty(propertyValue) && "executionGroup".equals(propertyName)) {
            List<String> groupIds = Arrays.asList(propertyValue.split(","));
            new Thread() {
                public void run() {
                    LOG.info("开始异步处理项目执行组成员,项目ID[" + marketProjectId + "]");
                    try {
                        int code = customerSeaService.saveXzAutoMember(NumberConvertUtil.parseInt(marketProjectId), custId, groupIds);
                        LOG.info("异步处理项目执行组成员,项目ID[" + marketProjectId + "]更改状态成功,status:" + code);
                    } catch (Exception e) {
                        LOG.error("异步处理项目执行组成员,", e);
                    }
                }
            }.start();
        }

        return 1;
    }

    /**
     * 保存话术
     *
     * @param content
     * @param custId
     * @param marketProjectId
     * @param status
     * @return
     */
    public int saveTelephoneTech(String content, String custId, int marketProjectId, int status) {
        MarketTemplate marketTemplate = marketTemplateDao.selectByProjectId(marketProjectId, 4);
        if (marketTemplate == null) {
            marketTemplate = new MarketTemplate();
            marketTemplate.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        marketTemplate.setTypeCode(4);
        marketTemplate.setCustId(custId);
        marketTemplate.setMouldContent(content);
        marketTemplate.setStatus(status);
        marketTemplate.setMarketProjectId(marketProjectId);
        int flag = 0;
        marketTemplateDao.saveOrUpdate(marketTemplate);
        flag = 1;
        return flag;
    }

    /**
     * 查询话术
     *
     * @param marketProjectId
     * @return
     */
    public MarketTemplate selectTelephoneTech(int marketProjectId, String marketTaskId, long seaId, int customerGroupId) {
        if (marketProjectId == 0 && StringUtil.isNotEmpty(marketTaskId)) {
            MarketTask marketTask = marketTaskDao.get(marketTaskId);
            if (marketTask != null) {
                CustomGroup cg = customGroupDao.get(marketTask.getCustomerGroupId());
                if (cg != null && cg.getMarketProjectId() != null) {
                    marketProjectId = cg.getMarketProjectId();
                }
            }
        } else if (marketProjectId == 0 && seaId > 0) {
            CustomerSea customerSea = customerSeaDao.get(seaId);
            if (customerSea != null && customerSea.getMarketProjectId() != null) {
                marketProjectId = customerSea.getMarketProjectId();
            }
        } else if (marketProjectId == 0 && customerGroupId > 0) {
            CustomGroup cg = customGroupDao.get(customerGroupId);
            if (cg != null && cg.getMarketProjectId() != null) {
                marketProjectId = cg.getMarketProjectId();
            }
        }
        MarketTemplate marketTemplate = marketTemplateDao.selectByProjectId(marketProjectId, 4);
        return marketTemplate;
    }
}

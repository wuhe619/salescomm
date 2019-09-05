package com.bdaim.log.service;

import com.bdaim.common.dto.Page;
import com.bdaim.common.entity.Dic;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.common.util.ConfigUtil;
import com.bdaim.common.util.DatetimeUtils;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.log.dao.OperLogDao;
import com.bdaim.log.dto.UserOperLogDTO;
import com.bdaim.log.entity.OperLog;
import com.bdaim.log.entity.UserOperLog;
import com.bdaim.supplier.dao.DicDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Service("operLogService")
public class OperLogService {
    @Resource
    private OperLogDao operLogDao;
    @Resource
    private DicDao dicDao;
    @Resource
    private CustomerDao customerDao;


    public List<OperLog> getTopnObjectIdByDateAndType(String typeuri, int topn, Date date) {
        // TODO Auto-generated method stub
        return operLogDao.getTopnObjectIdByDateAndType(typeuri, topn);
    }


    public List<OperLog> getOperLogInfo(OperLog entity, Page page, Date date1, Date date2, String order_field, String order_asc) {
        // TODO Auto-generated method stub
        try {
            return operLogDao.getOperLogInfo(entity, page, DatetimeUtils.dateToDayStart(date1), DatetimeUtils.dateToDayEnd(date2), order_field, order_asc);
        } catch (Exception ex) {
            ex.printStackTrace();
//			throw ex;
            return new ArrayList<OperLog>();
        }

    }

    public long getOperLogInfoTotalCount(OperLog ol, Date date1, Date date2) {
        // TODO Auto-generated method stub
        return operLogDao.getOperLogInfoTotalCount(ol, date1, date2);
    }


    public int addOneOperLogInfo(OperLog entity) {
        // TODO Auto-generated method stub
        return (Integer) operLogDao.saveReturnPk(entity);
    }

    /**
     * 保存用户行为记录
     *
     * @param entity
     */
    public void addUserOperLog(UserOperLog entity) {
        operLogDao.saveOrUpdate(entity);
    }


    public List<OperLog> getTopnPageByDate(Date date1, Date date2, int topn) {
        // TODO Auto-generated method stub
        List<OperLog> lstoperlog = operLogDao.getTopnPageByDate(date1, date2, topn);
        long totalcount = 0;
        try {
            //get total count of this table.
            totalcount = operLogDao.getTopnPageTotalCount(date1, date2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //循环计算每个值的百分比，HIBERNATE里面计算不出来貌似
        for (int i = 0; i < lstoperlog.size(); i++) {
            OperLog ol = lstoperlog.get(i);
            if (0 == totalcount)
                ol.setPage_count_percent(0);
            else
                ol.setPage_count_percent((double) ol.getPage_count() / (double) totalcount);
        }
        return lstoperlog;
    }

    /**
     * 用户行为记录分页
     *
     * @param pageNum
     * @param pageSize
     * @param param
     * @return
     */
    public Page pageUserOperlog(int pageNum, int pageSize, UserOperLogDTO param, boolean groupBy, int beforeMonth) {
        Page page = operLogDao.pageUserOperlog(pageNum, pageSize, param, groupBy, beforeMonth);
        List<UserOperLogDTO> list = new ArrayList<>();
        if (page != null && page.getData() != null && page.getData().size() > 0) {
            UserOperLog m;
            Dic dic;
            UserOperLogDTO dto;
            Map<String, Object> productProperty;
            List<DicProperty> propertyList;
            Customer customer;
            for (int i = 0; i < page.getData().size(); i++) {
                productProperty = new HashMap<>();
                m = (UserOperLog) page.getData().get(i);
                dto = new UserOperLogDTO(m);
                // 操作类型为浏览商品
                if (m.getEventType() == 2) {
                    dic = dicDao.get(NumberConvertUtil.parseLong(m.getObjectCode()));
                    dto.setObjectName("");
                    if (dic != null) {
                        dto.setObjectName(dic.getName());
                        dto.setProductType(dic.getDicTypeId());
                        productProperty.put("name", dic.getName());
                        propertyList = dicDao.getPropertyList(dic.getId());
                        for (DicProperty p : propertyList) {
                            productProperty.put(p.getDicPropKey(), p.getDicPropValue());
                            // 单独处理机构名称
                            if ("institution".equals(p.getDicPropKey())) {
                                productProperty.put("institutionName", "");
                                customer = customerDao.get(p.getDicPropValue());
                                if (customer != null) {
                                    productProperty.put("institutionName", customer.getEnterpriseName());
                                }
                            }
                            // 处理图片服务器地址
                            if (StringUtil.isNotEmpty(p.getDicPropValue()) && (p.getDicPropValue().toLowerCase().endsWith(".jpg")
                                    || p.getDicPropValue().toLowerCase().endsWith(".png")
                                    || p.getDicPropValue().toLowerCase().endsWith(".gif"))) {
                                productProperty.put(p.getDicPropKey(), ConfigUtil.getInstance().get("pic_server_url") + "/0/" + p.getDicPropValue());
                            }
                        }
                    }
                    dto.setProductProperty(productProperty);
                }
                // TODO 根据IP库查询地区
                dto.setLoginArea("北京");
                // TODO 根据user-agent查询设备类型
                dto.setLoginClient("PC");
                list.add(dto);
            }
        }
        page.setData(list);
        return page;
    }
}

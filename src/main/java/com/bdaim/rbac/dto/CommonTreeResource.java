package com.bdaim.rbac.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bdaim.util.DateUtil;

/**这个是针对原型一个ORM映射，通用型的记录，没有在指定的类型上有一些特殊化的功能
 */
public class CommonTreeResource extends AbstractTreeResource {
    private String remark;
    private String user;
    private Date createTime;
    private Date modifyTime;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public CommonTreeResource() {
    }

    public CommonTreeResource(Long key) {
        super.setID(key);
    }

    public static List<CommonTreeResource> pop(List rs){
        List<CommonTreeResource> returnRS=null;
        CommonTreeResource resource=null;
        if (rs==null)return null;
        try {
            for (int i=0;i<rs.size();i++){
            	Map r = (Map)rs.get(i);
                if (returnRS==null)returnRS=new ArrayList<CommonTreeResource>() ;
                resource=new CommonTreeResource();
                resource.setID(Long.parseLong(String.valueOf(r.get("ID"))));
                resource.setUri(String.valueOf(r.get("URI")));
                resource.setName(String.valueOf(r.get("NAME")));
                resource.setType(String.valueOf(r.get("TYPE")));
                resource.setRemark(String.valueOf(r.get("REMARK")));
                resource.setPid(Long.parseLong(String.valueOf(r.get("PID"))));
                resource.setSn(Integer.parseInt(String.valueOf(r.get("SN"))));
                resource.setUser(String.valueOf(r.get("OPTUSER")));
                resource.setCreateTime(DateUtil.fmtStrToDate(String.valueOf(r.get("CREATE_TIME"))));
                resource.setModifyTime(DateUtil.fmtStrToDate(String.valueOf(r.get("MODIFY_TIME"))));
                returnRS.add(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnRS;
    }
}

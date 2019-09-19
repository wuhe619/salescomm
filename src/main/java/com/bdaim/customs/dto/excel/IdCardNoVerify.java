package com.bdaim.customs.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

/**
 * 身份信息未核验导出excel实体类
 *
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
public class IdCardNoVerify extends BaseRowModel {

    @ExcelProperty(value = "分单号", index = 0)
    private String fdId;

    @ExcelProperty(value = "收件人", index = 1)
    private String addressee;

    @ExcelProperty(value = "收件人身份证号", index = 2)
    private String idCard;

    public String getFdId() {
        return fdId;
    }

    public void setFdId(String fdId) {
        this.fdId = fdId;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

}

package com.bdaim.batch.dto;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/11 13:51
 */
public class ImportErr {
    private String  importSum;//导入文件错误的总记录
    private String  importClone;//重复记录的
    private String  errrString;//错误信息
    private String  ordinaryString;
    private String errorFileName;
    private String rowNumber;
    private  String errCount;
    private String batchNum;

    public String getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(String batchNum) {
        this.batchNum = batchNum;
    }

    public String getErrCount() {
        return errCount;
    }

    public void setErrCount(String errCount) {
        this.errCount = errCount;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getErrorFileName() {
        return errorFileName;
    }

    public void setErrorFileName(String errorFileName) {
        this.errorFileName = errorFileName;
    }

    public String getImportSum() {
        return importSum;
    }

    public void setImportSum(String importSum) {
        this.importSum = importSum;
    }

    public String getImportClone() {
        return importClone;
    }

    public void setImportClone(String importClone) {
        this.importClone = importClone;
    }

    public String getErrrString() {
        return errrString;
    }

    public void setErrrString(String errrString) {
        this.errrString = errrString;
    }

    public String getOrdinaryString() {
        return ordinaryString;
    }

    public void setOrdinaryString(String ordinaryString) {
        this.ordinaryString = ordinaryString;
    }
}

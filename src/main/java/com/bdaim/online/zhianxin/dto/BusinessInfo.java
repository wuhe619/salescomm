package com.bdaim.online.zhianxin.dto;

/**  
 *    
 *   
 * @author chengning@salescomm.net
 * @date 2019/9/17 11:33
 */  
public class BusinessInfo {

    private String creditCode;
    private long estiblishTime;
    private String regCapCur;
    private String entName;
    private String regCap;
    private String regLocation;
    private int sum;
    private String entStatus;
    private long id;
    public void setCreditCode(String creditCode) {
         this.creditCode = creditCode;
     }
     public String getCreditCode() {
         return creditCode;
     }

    public void setEstiblishTime(long estiblishTime) {
         this.estiblishTime = estiblishTime;
     }
     public long getEstiblishTime() {
         return estiblishTime;
     }

    public void setRegCapCur(String regCapCur) {
         this.regCapCur = regCapCur;
     }
     public String getRegCapCur() {
         return regCapCur;
     }

    public void setEntName(String entName) {
         this.entName = entName;
     }
     public String getEntName() {
         return entName;
     }

    public void setRegCap(String regCap) {
         this.regCap = regCap;
     }
     public String getRegCap() {
         return regCap;
     }

    public void setRegLocation(String regLocation) {
         this.regLocation = regLocation;
     }
     public String getRegLocation() {
         return regLocation;
     }

    public void setSum(int sum) {
         this.sum = sum;
     }
     public int getSum() {
         return sum;
     }

    public void setEntStatus(String entStatus) {
         this.entStatus = entStatus;
     }
     public String getEntStatus() {
         return entStatus;
     }

    public void setId(long id) {
         this.id = id;
     }
     public long getId() {
         return id;
     }

}
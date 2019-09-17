package com.bdaim.common.third.zhianxin.dto;

/**  
 *    
 *   
 * @author chengning@salescomm.net
 * @date 2019/9/17 11:21
 */  
public class BaseResult {

    private String code;
    private String orderNo;
    private boolean charge;
    private Data data;
    private String pcode;
    private Param param;
    private String time;
    private String message;
    public void setCode(String code) {
         this.code = code;
     }
     public String getCode() {
         return code;
     }

    public void setOrderNo(String orderNo) {
         this.orderNo = orderNo;
     }
     public String getOrderNo() {
         return orderNo;
     }

    public void setCharge(boolean charge) {
         this.charge = charge;
     }
     public boolean getCharge() {
         return charge;
     }

    public void setData(Data data) {
         this.data = data;
     }
     public Data getData() {
         return data;
     }

    public void setPcode(String pcode) {
         this.pcode = pcode;
     }
     public String getPcode() {
         return pcode;
     }

    public void setParam(Param param) {
         this.param = param;
     }
     public Param getParam() {
         return param;
     }

    public void setTime(String time) {
         this.time = time;
     }
     public String getTime() {
         return time;
     }

    public void setMessage(String message) {
         this.message = message;
     }
     public String getMessage() {
         return message;
     }

}
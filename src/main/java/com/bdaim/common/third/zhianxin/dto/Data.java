package com.bdaim.common.third.zhianxin.dto;
import java.util.List;

/**  
 *    
 *   
 * @author chengning@salescomm.net
 * @date 2019/9/17 11:33
 */  
public class Data {

    private int total;
    private int pageNo;
    private int pageSize;
    private List<List> list;
    public void setTotal(int total) {
         this.total = total;
     }
     public int getTotal() {
         return total;
     }

    public void setPageNo(int pageNo) {
         this.pageNo = pageNo;
     }
     public int getPageNo() {
         return pageNo;
     }

    public void setPageSize(int pageSize) {
         this.pageSize = pageSize;
     }
     public int getPageSize() {
         return pageSize;
     }

    public void setList(List<List> list) {
         this.list = list;
     }
     public List<List> getList() {
         return list;
     }

}
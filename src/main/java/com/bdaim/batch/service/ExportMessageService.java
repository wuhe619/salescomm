package com.bdaim.batch.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author duanliying
 * @date 2018/9/11
 * @description
 */
public interface ExportMessageService {
    /**
     * @description 失联人员信息导出功能
     * @author:duanliying
     * @method
     * @date: 2018/9/11 9:25
     */

    void exportLostContactMessage(String labelListStr, String custId, String batchId, String realName, Long userId, String userType, String enterpriseId, String id, String idCard, String status, HttpServletResponse response,String rose) throws IOException;

}

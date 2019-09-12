package com.bdaim.log.thread;

import com.bdaim.common.util.spring.SpringContextHelper;
import com.bdaim.log.entity.UserOperLog;
import com.bdaim.log.service.OperLogService;
import org.apache.log4j.Logger;

/**
 * 用户行为记录保存
 *
 * @author chengning@salescomm.net
 * @date 2019/6/13
 * @description
 */
public class UserOperLogRunnable implements Runnable {
    private static Logger LOG = Logger.getLogger(UserOperLogRunnable.class);

    UserOperLog log;
    OperLogService service;

    public UserOperLogRunnable(UserOperLog log) {
        this.log = log;
        service = (OperLogService) SpringContextHelper.getBean("operLogService");
    }

    @Override
    public void run() {
        try {
            service.addUserOperLog(log);
        } catch (Exception e) {
            LOG.error("保存用户行为异常,", e);
        }
    }
}

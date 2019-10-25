package com.bdaim.log.util;


import com.bdaim.common.spring.SpringContextHelper;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.log.entity.SuperDataOperLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公海转交记录保存
 *
 * @author chengning@salescomm.net
 * @date 2019/7/1
 * @description
 */
public class SuperDataOperLogThread implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(SuperDataOperLogThread.class);

    SuperDataOperLog log;
    CustomerSeaService service;

    public SuperDataOperLogThread(SuperDataOperLog log) {
        this.log = log;
        service = (CustomerSeaService) SpringContextHelper.getBean("customerSeaService");
    }

    @Override
    public void run() {
        try {
            service.saveSuperDataOperLog(log);
        } catch (Exception e) {
            LOG.error("公海转交记录保存异常,", e);
        }
    }
}

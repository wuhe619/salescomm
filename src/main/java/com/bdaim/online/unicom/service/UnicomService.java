package com.bdaim.online.unicom.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author chengning@salescomm.net
 * @date 2019-11-25 10:03
 */
@Service
@Transactional
public class UnicomService {

    public int unicomSeatMakeCall(String custId, long userId, String dataId) {
        // 查询客户配置的联通呼叫参数
        return 1;
    }
}

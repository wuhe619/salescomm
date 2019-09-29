package com.bdaim.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SequenceService {
    static private Logger logger = LoggerFactory.getLogger(SequenceService.class);

    private final Lock lock;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public SequenceService() {
        this.lock = new ReentrantLock();
    }

    public Long getSeq(String type) throws Exception {
        Long seq = 1L;
        try {
            lock.lock();
            String sql = "select value from sys_sequences where name='" + type + "'";
            List<Map<String, Object>> r = jdbcTemplate.queryForList(sql);
            if (r.size() == 0) {
                seq = 1L;
                sql = "insert into sys_sequences(name,value) value('" + type + "', " + seq + ")";
                jdbcTemplate.update(sql);
            } else {
                seq = (Long) r.get(0).get("value");
                if (seq <= 0)
                    seq = 1L;
                seq++;
                sql = "update sys_sequences set value=" + seq + " where name='" + type + "'";
                jdbcTemplate.update(sql);
            }
            //}
        } catch (Exception e) {
            logger.error("获取主键ID异常:[" + type + "] ", e);
            throw new Exception("获取主键ID异常");
        } finally {
            lock.unlock();
        }

        return seq;
    }
}

package com.bdaim.log.service;

import com.bdaim.log.entity.TransferLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransferLogService{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int insertLog(TransferLog transferLog) {
        String task_id = transferLog.getTaskId();
        int state = transferLog.getState();
        String sql = "insert into transfer_log(task_id, state) values ("+task_id + "," + state + ") ON DUPLICATE KEY UPDATE state=" + state;
        return jdbcTemplate.update(sql);
    }

    public int selectLog(String taskId) {
        String sql = "select count(*) from transfer_log where task_id=?";
        return jdbcTemplate.queryForObject(sql, Integer.class, taskId);
    }
}

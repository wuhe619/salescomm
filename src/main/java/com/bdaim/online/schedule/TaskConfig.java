package com.bdaim.online.schedule;

import com.bdaim.online.schedule.project.StatLabelDataDay;
import com.bdaim.online.schedule.project.StatMarketTaskUserCallJob;
import com.bdaim.online.schedule.project.StatSuccessOrderJob;
import com.bdaim.online.schedule.project.StatXzAbnormalCallRecordJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskConfig {
    private static Logger logger = LoggerFactory.getLogger(TaskConfig.class);

    @Autowired
    private StatXzAbnormalCallRecordJob statXzAbnormalCallRecordJob;

    @Autowired
    private StatSuccessOrderJob statSuccessOrderJob;

    @Autowired
    private StatMarketTaskUserCallJob statMarketTaskUserCallJob;

    @Autowired
    private StatLabelDataDay statLabelDataDay;

//    @Scheduled(cron = "0/5 * * * * *")
//    public void testTask(){
//        logger.info("schedule testTask--testTask---");
//    }


    @Scheduled(cron = "0 0/1 * * * ? ")
    public void statXzAbnormalCallRecordJob(){
        logger.info("schedule testTask---statXzAbnormalCallRecordJob--");
        statXzAbnormalCallRecordJob.run();
    }

    @Scheduled(cron = "0 0/5 * * * ? ")
    public void statSuccessOrderJob(){
        logger.info("schedule testTask---statSuccessOrderJob--");
        statSuccessOrderJob.run();
    }

    @Scheduled(cron = "0 0/5 * * * ? ")
    public void statMarketTaskUserCallJob(){
        logger.info("schedule testTask---statMarketTaskUserCallJob--");
        statMarketTaskUserCallJob.run();
    }

    @Scheduled(cron = "0 0/5 * * * ? ")
    public void statLabelDataDay(){
        logger.info("schedule testTask---statLabelDataDay--");
        statLabelDataDay.execute();
    }


}

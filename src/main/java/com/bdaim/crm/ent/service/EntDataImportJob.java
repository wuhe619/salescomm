package com.bdaim.crm.ent.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class EntDataImportJob {

    public static final Logger LOG = LoggerFactory.getLogger(EntDataImportJob.class);

    @Autowired
    private EntDataService entDataService;

    @Scheduled(cron = "0 0/1 * * * ? ")
    public void run() throws IOException {
        String path = "D:\\已处理\\";
        File d = new File(path);
        if (!d.exists()) {
            d.mkdirs();
        }
        File file = new File("C:\\Users\\ningmeng\\Desktop\\20200115");
        for (File f : file.listFiles()) {
            /*int i = entDataService.importQCCByExcel(f.getPath(), 1, 1, "企查查", "https://www.qichacha.com/");
            LOG.info(f.getName() + "导入成功:" + i);*/
            File dest = new File(path + f.getName());
            FileUtils.moveFile(f, dest);
            LOG.info(f.getName() + "移除文件成功");
        }
    }

}

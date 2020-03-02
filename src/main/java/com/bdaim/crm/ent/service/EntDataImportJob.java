package com.bdaim.crm.ent.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EntDataImportJob {

    public static final Logger LOG = LoggerFactory.getLogger(EntDataImportJob.class);

    @Autowired
    private EntDataService entDataService;

    //@Scheduled(cron = "0 0/5 * * * ? ")
    @PostConstruct
    public void run() throws IOException, InterruptedException {
        new Thread(new HandleTask(entDataService)).start();
    }

    class HandleTask implements Runnable {

        EntDataService entDataService;

        public HandleTask(EntDataService entDataService) {
            this.entDataService = entDataService;
        }

        @Override
        public void run() {
            while (true) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String path = "G:\\已处理\\" + formatter.format(LocalDateTime.now())+"\\";
                File d = new File(path);
                if (!d.exists()) {
                    d.mkdirs();
                }
                String sourcePath = "G:\\ent_data\\";
                File file = new File(sourcePath);
                for (File date : file.listFiles()) {
                    for (File f : date.listFiles()) {
                        int i = entDataService.importQCCByExcel(f.getPath(), 1, 1, "企查查", "https://www.qichacha.com/");
                        LOG.info(f.getName() + "导入成功:" + i);
                        File dest = new File(path + f.getName());
                        try {
                            FileUtils.moveFile(f, dest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        LOG.info(f.getName() + "移除文件成功");
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

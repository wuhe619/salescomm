package com.bdaim.log.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.log.entity.TransferLog;
import com.bdaim.log.service.TransferLogService;
import com.bdaim.util.ConfigUtil;
import com.bdaim.util.HttpUtil;
import com.bdaim.util.MD5Util2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(value = "/transfer")
public class TransferController {

    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    @Autowired
    private TransferLogService transferLogService;

    @Value("${data.account:0}")
    private String account;

    @Value("${data.access_key:0}")
    private String access_key;

    @Value("${data.transfer_url:0}")
    private String transfer_url;

    @Value("${data.save_path:/}")
    private String savePath;

    @Value("${data.key_type:0}")
    private String key_type;

    @Value("${data.value_type:0}")
    private String value_type;



    @PostMapping(value = "trans")
    public Map transfer(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 201);
        result.put("message", "失败");
        try {
            Map<String, File> files = new HashMap<>();
            Map<String, InputStream> streams = new HashMap<>();
            Map<String, String> texts = new HashMap();

            long timestamp = System.currentTimeMillis() / 1000;

            String token = MD5Util2.MD5Encode(account + access_key + timestamp);

            //参数
            texts.put("account", account);
            texts.put("token", token);
            texts.put("req_time", String.valueOf(timestamp));
            texts.put("key_type", key_type);
            texts.put("value_type", value_type);
            logger.info("transfer:"+texts);
            streams.put("file", file.getInputStream());
            //发送请求
            String res = HttpUtil.postForm(transfer_url,"file", files, streams, texts);
            logger.info("调用接口返回结果：" + res);
            JSONObject resJson = JSONObject.parseObject(res);
            String respcode = resJson.getString("respcode");
            String task_id = resJson.getString("task_id");

            if (null != respcode && respcode.equals("200200")) {
                TransferLog transferLog = new TransferLog(task_id, 0);
                int r = transferLogService.insertLog(transferLog);
                if (r > 0) {
                    result.put("code", 200);
                    result.put("message", "成功");
                }
            }

        } catch (Exception e) {
            logger.error("error:", e);
        } finally {
            return result;
        }
    }


    @PostMapping(value = "upload")
    public Map upload(String task_id, String key_counts, String matched_counts, MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        result.put("respcode", 400400);
        result.put("message", "失败");
        FileOutputStream fos = null;
        try {
            logger.info("task_id={}, key_counts={}, matched_counts={}, file={}",task_id,key_counts,matched_counts,file.isEmpty());
            int count = transferLogService.selectLog(task_id);
            if (count <= 0) {
                logger.error("task_id=" + task_id + "不存在");
                return result;
            }
            String path = ConfigUtil.getInstance().get("data.save_path");
            String pathName  = path + task_id + "_" + key_counts + "_" + matched_counts;
            fos = new FileOutputStream(pathName);
            fos.write(file.getBytes());
            TransferLog transferLog = new TransferLog(task_id, 1);
            int r = transferLogService.insertLog(transferLog);
            if (r > 0) {
                result.put("respcode", 200);
                result.put("message", "成功");
            }
        } catch (Exception e) {
            logger.error("error:", e);
            result.put("respcode", 400400);
            result.put("message", "失败");
        } finally {
            try {
                if(null != fos) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

}

package com.bdaim.common.controller;


import com.bdaim.auth.LoginUser;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.util.UnicomUtil;
import com.bdaim.util.wechat.WeChatUtil;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.DeleteIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;

@Controller
@RequestMapping("/wechat")
public class WeChatAction extends BasicAction {

    public static final Logger LOG = LoggerFactory.getLogger(WeChatAction.class);

    @Resource
    private WeChatUtil weChatUtil;
    @Resource
    JestClient jestClient;

    /**
     * 根据id获取配置信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/testMsg")
    public void getConfigById(Integer id) {
        weChatUtil.sendTempMsg("oFqgzwFGhEAqH-aRgI6YYJTti1oc", "BHAXK1_KvuS2tzbB5wZx_DUNuU8VJhbsP5nOrHjDtUI", "", "#FF0000", "{\"name\":{\"value\":\"开发人员\",\"color\":\"#173177\"},\"time\":{\"value\":\"2019-10-10 14:58:33\",\"color\":\"#173177\"},\"content\":{\"value\":\"test\",\"color\":\"#173177\"}}");
    }


    @ResponseBody
    @RequestMapping("/testAuto")
    public void testAuto(Integer id) {
        LoginUser user = BaseUtil.getUser();
        System.out.println(user);
    }

    /*@ResponseBody
    @RequestMapping("/delIndex")*/
    public void delIndex() {
        for (BusiTypeEnum v : BusiTypeEnum.values()) {
            deleteIndex(BusiTypeEnum.getEsIndex(v.getType()));
        }
    }

    public boolean deleteIndex(String indexName) {
        try {
            JestResult jestResult = jestClient.execute(new DeleteIndex.Builder(indexName).build());
            LOG.info("deleteIndex:{},result:{}", indexName, jestResult.isSucceeded());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}

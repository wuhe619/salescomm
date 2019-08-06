package com.bdaim.common.controller;

import com.bdaim.common.entity.DicProperty;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.DicService;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author duanliying
 * @date 2019/8/6
 * @description 字典公用
 */
@RestController
@RequestMapping("/dic")
public class DicAction extends BasicAction {
    private static Logger logger = Logger.getLogger(DicAction.class);

    @Resource
    private DicService dicService;

    /**
     * 根据id查询字典表信息
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResponseInfo getDicProperty(Long id) {
        try {
            List<DicProperty> list = dicService.getDicProperty(id);
            return new ResponseInfoAssemble().success(list);
        } catch (Exception e) {
            logger.error("查询字典表信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询字典表信息异常");
        }
    }
}

package com.bdaim.customs.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.dto.StationDto;
import com.bdaim.customs.services.StationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author duanliying
 * @date 2019/9/16
 * @description 场站信息
 */
@Controller
@RequestMapping(value = "/station")
public class StationController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(StationController.class);
    @Resource
    StationService stationService;

    /**
     * @description 创建场站信息
     * @author:duanliying
     * @method
     * @date: 2019/9/16 11:15
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo updateStation(@RequestBody StationDto stationDto) {
        try {
            if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
                if (opUser().getId() != null) {
                    stationDto.setCreateId(NumberConvertUtil.parseInt(opUser().getId()));
                }
                stationService.updateStation(stationDto);
                return new ResponseInfoAssemble().success(null);
            }
        } catch (Exception e) {
            logger.error("创建场站信息异常!", e);
            return new ResponseInfoAssemble().failure(-1, "创建场站信息失败");
        }
        return new ResponseInfoAssemble().success(null);
    }


    /**
     * @description 获取场站信息列表
     * @author:duanliying
     * @method
     * @date: 2019/9/16 16:02
     */
    @RequestMapping(value = "/getList", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo getStationList(@RequestBody StationDto stationDto) {
        try {
            if (StringUtil.isEmpty(String.valueOf(stationDto.getPageNum())) || StringUtil.isEmpty(String.valueOf(stationDto.getPageSize()))) {
                return new ResponseInfoAssemble().failure(-1, "缺少分页参数");
            }
            if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
                Page stationList = stationService.getStationList(stationDto);
                return new ResponseInfoAssemble().success(stationList);
            }
        } catch (Exception e) {
            logger.error("查询场站列表信息异常!", e);
            return new ResponseInfoAssemble().failure(-1, "查询场站列表信息失败");
        }
        return new ResponseInfoAssemble().success(null);
    }
}
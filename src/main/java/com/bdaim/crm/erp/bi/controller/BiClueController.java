package com.bdaim.crm.erp.bi.controller;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.crm.erp.bi.service.BiClueService;
import com.bdaim.crm.erp.work.entity.XStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 线索统计
 *
 * @author Chacker
 * @date 2020/04/10
 */
@RestController
@RequestMapping(value = "/biClue")
public class BiClueController {
    @Autowired
    private BiClueService clueService;

    /**
     * 线索总量统计图表
     *
     * @param xStats
     * @return
     */
    @RequestMapping(value = "/totalClueStats")
    public ResponseInfo totalClueStats(XStats xStats) {
        return clueService.totalClueStats(xStats);
    }

    /**
     * 线索详情统计列表
     *
     * @param xStats
     * @return
     */
    @RequestMapping(value = "/totalClueTable")
    public ResponseInfo totalClueTable(XStats xStats) {
        return clueService.totalClueTable(xStats);
    }

    /**
     * 线索跟进次数统计 图表
     *
     * @param xStats
     * @return
     */
    @RequestMapping(value = "/clueRecordStats")
    public ResponseInfo clueRecordStats(XStats xStats) {
        return clueService.clueRecordStats(xStats);
    }

    /**
     * 线索跟进次数统计 列表
     *
     * @param xStats
     * @return
     */
    @RequestMapping(value = "/clueRecordTable")
    public ResponseInfo clueRecordTable(XStats xStats) {
        return clueService.clueRecordTable(xStats);
    }

    /**
     * 跟进方式统计
     *
     * @param xStats
     * @return
     */
    @RequestMapping(value = "/clueRecordCategoryStats")
    public ResponseInfo clueRecordCategoryStats(XStats xStats) {
        return clueService.clueRecordCategoryStats(xStats);
    }

    /**
     * 公海统计
     *
     * @param xStats
     * @return
     */
    @RequestMapping(value = "/poolTable")
    public ResponseInfo poolTable(XStats xStats) {
        return clueService.poolTable(xStats);
    }

    /**
     * 公海统计
     *
     * @param xStats
     * @return
     */
    @RequestMapping(value = "/poolStats")
    public ResponseInfo poolStats(XStats xStats) {
        return clueService.poolStats(xStats);
    }
}

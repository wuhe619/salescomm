package com.bdaim.common.service;

import com.bdaim.common.dao.ChartConfigSingleChartServiceDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;


@Service("chartConfigSingleChartServiceImpl")
@Transactional
public class ChartConfigSingleChartService {
    @Resource
    ChartConfigSingleChartServiceDao dao;

    /*
     * (non-Javadoc)
     * @see ChartConfigSingleChartService#getLabelCoverTopn()
     * 获取TOPN的用户覆盖数
     */
    public List<Map<String, Integer>> getLabelCoverTopn() {
        // TODO Auto-generated method stub
        int topn = 50;
        List<Map<String, Integer>> lst = dao.getLabelCoverTopn(topn);
        return lst;
    }

    public List<Map<String, Integer>> getProvinceCoverNum() {
        // TODO Auto-generated method stub

        return null;
    }

}

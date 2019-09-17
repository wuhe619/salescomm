package com.bdaim.customs.services;

import com.bdaim.common.dto.Page;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.dao.StationDao;
import com.bdaim.customs.dto.StationDto;
import com.bdaim.customs.entity.Station;
import com.bdaim.supplier.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;

/**
 * @author duanliying
 * @date 2019/9/16
 * @description 场站信息
 */
@Service("stationService")
@Transactional
public class StationService {
    public static final Logger log = LoggerFactory.getLogger(SupplierService.class);
    @Resource
    private StationDao stationDao;

    /**
     * @description 场站信息创建与编辑
     * @author:duanliying
     * @method
     * @date: 2019/9/16 13:40
     */
    public void updateStation(StationDto stationDto) throws Exception {
        int optType = stationDto.getOptType();
        if (optType == 0) {
            stationDto.setCreateTime((new Timestamp(System.currentTimeMillis())));
            stationDao.saveOrUpdate(new Station(stationDto));
        } else if (optType == 1) {
            Integer id = stationDto.getId();
            log.info("编辑的场站id是：" + id);
            stationDao.saveOrUpdate(new Station(stationDto));
        } else if (optType == 2) {
            int id = stationDto.getId();
            String status = stationDto.getStatus();
            log.info("编辑的场站状态，场站id是：" + id + "要变更的状态是：" + status);
            Station station = stationDao.getStationById(id);
            station.setStatus(status);
            stationDao.saveOrUpdate(station);
        }
    }

    /**
     * @description 获取场站信息列表
     * @author:duanliying
     * @method
     * @date: 2019/9/16 16:03
     */
    public Page getStationList(StationDto stationDto) throws Exception {
        StringBuffer querySql = new StringBuffer("SELECT * FROM h_station_info where 1=1 ");
        if (StringUtil.isNotEmpty(stationDto.getProvince())) {
            querySql.append(" and province ='" + stationDto.getProvince() + "'");
        }
        if (StringUtil.isNotEmpty(stationDto.getName())) {
            querySql.append(" and name like '%" + stationDto.getName() + "%'");
        }
        if (StringUtil.isNotEmpty(stationDto.getStatus())) {
            querySql.append(" and status =" + stationDto.getStatus());
        }
        if (stationDto.getId() != null) {
            querySql.append(" and id =" + stationDto.getId());
        }
        return stationDao.sqlPageQuery(querySql.toString(), stationDto.getPageNum(), stationDto.getPageSize());
    }
}

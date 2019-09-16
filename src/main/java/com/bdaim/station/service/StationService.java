package com.bdaim.station.service;

import com.bdaim.station.dao.StationDao;
import com.bdaim.station.dto.StationDto;
import com.bdaim.station.entity.Station;
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
}

package com.bdaim.callcenter.dto;

import com.bdaim.resource.dto.MarketResourceDTO;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/24
 * @description
 */
public class CustomCallConfigDTO {

    private List<MarketResourceDTO> callCenter;
    private List<MarketResourceDTO> call2way;
    private List<MarketResourceDTO> robot;

    public List<MarketResourceDTO> getCallCenter() {
        return callCenter;
    }

    public void setCallCenter(List<MarketResourceDTO> callCenter) {
        this.callCenter = callCenter;
    }

    public List<MarketResourceDTO> getCall2way() {
        return call2way;
    }

    public void setCall2way(List<MarketResourceDTO> call2way) {
        this.call2way = call2way;
    }

    public List<MarketResourceDTO> getRobot() {
        return robot;
    }

    public void setRobot(List<MarketResourceDTO> robot) {
        this.robot = robot;
    }

    @Override
    public String toString() {
        return "CustomCallConfigDTO{" +
                "callCenter=" + callCenter +
                ", call2way=" + call2way +
                ", robot=" + robot +
                '}';
    }
}

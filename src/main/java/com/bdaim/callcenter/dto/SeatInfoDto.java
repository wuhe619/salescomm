package com.bdaim.callcenter.dto;

/**
 * @author duanliying
 * @date 2019/4/9
 * 坐席信息Dto
 */
public class SeatInfoDto {
    private String seatPassword;
    private String seatId;
    private String seatName;
    private String mainNumber;
    private int seatCustMinute;
    private int seatSupMinute;


    public SeatInfoDto() {
    }

    public String getSeatPassword() {
        return seatPassword;
    }

    public void setSeatPassword(String seatPassword) {
        this.seatPassword = seatPassword;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getMainNumber() {
        return mainNumber;
    }

    public void setMainNumber(String mainNumber) {
        this.mainNumber = mainNumber;
    }

    public int getSeatCustMinute() {
        return seatCustMinute;
    }

    public void setSeatCustMinute(int seatCustMinute) {
        this.seatCustMinute = seatCustMinute;
    }

    public int getSeatSupMinute() {
        return seatSupMinute;
    }

    public void setSeatSupMinute(int seatSupMinute) {
        this.seatSupMinute = seatSupMinute;
    }

    @Override
    public String toString() {
        return "SeatInfoDto{" +
                "seatPassword='" + seatPassword + '\'' +
                ", seatId='" + seatId + '\'' +
                ", seatName='" + seatName + '\'' +
                ", mainNumber='" + mainNumber + '\'' +
                ", seatCustMinute=" + seatCustMinute +
                ", seatSupMinute=" + seatSupMinute +
                '}';
    }
}

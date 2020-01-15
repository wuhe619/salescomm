package com.bdaim.crm.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_examine_travel", schema = "", catalog = "")
public class LkCrmOaExamineTravelEntity {
    private int travelId;
    private int examineId;
    private String startAddress;
    private Timestamp startTime;
    private String endAddress;
    private Timestamp endTime;
    private BigDecimal traffic;
    private BigDecimal stay;
    private BigDecimal diet;
    private BigDecimal other;
    private BigDecimal money;
    private String vehicle;
    private String trip;
    private BigDecimal duration;
    private String description;
    private String batchId;

    @Id
    @Column(name = "travel_id")
    public int getTravelId() {
        return travelId;
    }

    public void setTravelId(int travelId) {
        this.travelId = travelId;
    }

    @Basic
    @Column(name = "examine_id")
    public int getExamineId() {
        return examineId;
    }

    public void setExamineId(int examineId) {
        this.examineId = examineId;
    }

    @Basic
    @Column(name = "start_address")
    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    @Basic
    @Column(name = "start_time")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "end_address")
    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    @Basic
    @Column(name = "end_time")
    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    @Basic
    @Column(name = "traffic")
    public BigDecimal getTraffic() {
        return traffic;
    }

    public void setTraffic(BigDecimal traffic) {
        this.traffic = traffic;
    }

    @Basic
    @Column(name = "stay")
    public BigDecimal getStay() {
        return stay;
    }

    public void setStay(BigDecimal stay) {
        this.stay = stay;
    }

    @Basic
    @Column(name = "diet")
    public BigDecimal getDiet() {
        return diet;
    }

    public void setDiet(BigDecimal diet) {
        this.diet = diet;
    }

    @Basic
    @Column(name = "other")
    public BigDecimal getOther() {
        return other;
    }

    public void setOther(BigDecimal other) {
        this.other = other;
    }

    @Basic
    @Column(name = "money")
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Basic
    @Column(name = "vehicle")
    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    @Basic
    @Column(name = "trip")
    public String getTrip() {
        return trip;
    }

    public void setTrip(String trip) {
        this.trip = trip;
    }

    @Basic
    @Column(name = "duration")
    public BigDecimal getDuration() {
        return duration;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaExamineTravelEntity that = (LkCrmOaExamineTravelEntity) o;
        return travelId == that.travelId &&
                examineId == that.examineId &&
                Objects.equals(startAddress, that.startAddress) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endAddress, that.endAddress) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(traffic, that.traffic) &&
                Objects.equals(stay, that.stay) &&
                Objects.equals(diet, that.diet) &&
                Objects.equals(other, that.other) &&
                Objects.equals(money, that.money) &&
                Objects.equals(vehicle, that.vehicle) &&
                Objects.equals(trip, that.trip) &&
                Objects.equals(duration, that.duration) &&
                Objects.equals(description, that.description) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(travelId, examineId, startAddress, startTime, endAddress, endTime, traffic, stay, diet, other, money, vehicle, trip, duration, description, batchId);
    }
}

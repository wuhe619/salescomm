package com.bdaim.crm.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_achievement", schema = "", catalog = "")
public class LkCrmAchievementEntity {
    private int achievementId;
    private Integer objId;
    private Integer type;
    private String year;
    private BigDecimal january;
    private BigDecimal february;
    private BigDecimal march;
    private BigDecimal april;
    private BigDecimal may;
    private BigDecimal june;
    private BigDecimal july;
    private BigDecimal august;
    private BigDecimal september;
    private BigDecimal october;
    private BigDecimal november;
    private BigDecimal december;
    private Integer status;
    private BigDecimal yeartarget;

    @Id
    @Column(name = "achievement_id")
    public int getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(int achievementId) {
        this.achievementId = achievementId;
    }

    @Basic
    @Column(name = "obj_id")
    public Integer getObjId() {
        return objId;
    }

    public void setObjId(Integer objId) {
        this.objId = objId;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "year")
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Basic
    @Column(name = "january")
    public BigDecimal getJanuary() {
        return january;
    }

    public void setJanuary(BigDecimal january) {
        this.january = january;
    }

    @Basic
    @Column(name = "february")
    public BigDecimal getFebruary() {
        return february;
    }

    public void setFebruary(BigDecimal february) {
        this.february = february;
    }

    @Basic
    @Column(name = "march")
    public BigDecimal getMarch() {
        return march;
    }

    public void setMarch(BigDecimal march) {
        this.march = march;
    }

    @Basic
    @Column(name = "april")
    public BigDecimal getApril() {
        return april;
    }

    public void setApril(BigDecimal april) {
        this.april = april;
    }

    @Basic
    @Column(name = "may")
    public BigDecimal getMay() {
        return may;
    }

    public void setMay(BigDecimal may) {
        this.may = may;
    }

    @Basic
    @Column(name = "june")
    public BigDecimal getJune() {
        return june;
    }

    public void setJune(BigDecimal june) {
        this.june = june;
    }

    @Basic
    @Column(name = "july")
    public BigDecimal getJuly() {
        return july;
    }

    public void setJuly(BigDecimal july) {
        this.july = july;
    }

    @Basic
    @Column(name = "august")
    public BigDecimal getAugust() {
        return august;
    }

    public void setAugust(BigDecimal august) {
        this.august = august;
    }

    @Basic
    @Column(name = "september")
    public BigDecimal getSeptember() {
        return september;
    }

    public void setSeptember(BigDecimal september) {
        this.september = september;
    }

    @Basic
    @Column(name = "october")
    public BigDecimal getOctober() {
        return october;
    }

    public void setOctober(BigDecimal october) {
        this.october = october;
    }

    @Basic
    @Column(name = "november")
    public BigDecimal getNovember() {
        return november;
    }

    public void setNovember(BigDecimal november) {
        this.november = november;
    }

    @Basic
    @Column(name = "december")
    public BigDecimal getDecember() {
        return december;
    }

    public void setDecember(BigDecimal december) {
        this.december = december;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "yeartarget")
    public BigDecimal getYeartarget() {
        return yeartarget;
    }

    public void setYeartarget(BigDecimal yeartarget) {
        this.yeartarget = yeartarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAchievementEntity that = (LkCrmAchievementEntity) o;
        return achievementId == that.achievementId &&
                Objects.equals(objId, that.objId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(year, that.year) &&
                Objects.equals(january, that.january) &&
                Objects.equals(february, that.february) &&
                Objects.equals(march, that.march) &&
                Objects.equals(april, that.april) &&
                Objects.equals(may, that.may) &&
                Objects.equals(june, that.june) &&
                Objects.equals(july, that.july) &&
                Objects.equals(august, that.august) &&
                Objects.equals(september, that.september) &&
                Objects.equals(october, that.october) &&
                Objects.equals(november, that.november) &&
                Objects.equals(december, that.december) &&
                Objects.equals(status, that.status) &&
                Objects.equals(yeartarget, that.yeartarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(achievementId, objId, type, year, january, february, march, april, may, june, july, august, september, october, november, december, status, yeartarget);
    }
}

package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_event_notice", schema = "crm", catalog = "")
public class LkCrmOaEventNoticeEntity {
    private int id;
    private int eventId;
    private String noticetype;
    private String repeat;
    private int startTime;
    private int stopTime;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "event_id")
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Basic
    @Column(name = "noticetype")
    public String getNoticetype() {
        return noticetype;
    }

    public void setNoticetype(String noticetype) {
        this.noticetype = noticetype;
    }

    @Basic
    @Column(name = "repeat")
    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    @Basic
    @Column(name = "start_time")
    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "stop_time")
    public int getStopTime() {
        return stopTime;
    }

    public void setStopTime(int stopTime) {
        this.stopTime = stopTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaEventNoticeEntity that = (LkCrmOaEventNoticeEntity) o;
        return id == that.id &&
                eventId == that.eventId &&
                startTime == that.startTime &&
                stopTime == that.stopTime &&
                Objects.equals(noticetype, that.noticetype) &&
                Objects.equals(repeat, that.repeat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventId, noticetype, repeat, startTime, stopTime);
    }
}

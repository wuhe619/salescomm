package com.bdaim.api.entity;

import java.util.List;

public class CheckData {
    private int count;
    private int errCount;
    private int sucCount;
    private List<String> errLsit;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getErrCount() {
        return errCount;
    }

    public void setErrCount(int errCount) {
        this.errCount = errCount;
    }

    public int getSucCount() {
        return sucCount;
    }

    public void setSucCount(int sucCount) {
        this.sucCount = sucCount;
    }

    public List<String> getErrLsit() {
        return errLsit;
    }

    public void setErrLsit(List<String> errLsit) {
        this.errLsit = errLsit;
    }
}

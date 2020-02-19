package com.bdaim.crm.utils;


import com.bdaim.common.dto.Page;

import java.util.List;

public class CrmPage {

    private List list;
    private int totalRow;

    public CrmPage() {
    }

    public CrmPage(List list, int totalRow) {
        this.list = list;
        this.totalRow = totalRow;
    }
    public CrmPage(Page page) {
        this.list = page.getData();
        this.totalRow = page.getTotal();
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public int getTotalRow() {
        return totalRow;
    }

    public void setTotalRow(int totalRow) {
        this.totalRow = totalRow;
    }
}

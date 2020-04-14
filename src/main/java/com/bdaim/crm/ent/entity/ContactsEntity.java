package com.bdaim.crm.ent.entity;

/**
 * 联系人数据
 */
public class ContactsEntity {
    /**
     * 姓名
     */
    private String name;
    /**
     * 职位
     */
    private String position;

    /**
     * 关联公司数量
     */
    private int companyNumbers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getCompanyNumbers() {
        return companyNumbers;
    }

    public void setCompanyNumbers(int companyNumbers) {
        this.companyNumbers = companyNumbers;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", companyNumbers=" + companyNumbers +
                '}';
    }
}

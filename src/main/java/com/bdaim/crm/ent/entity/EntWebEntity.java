package com.bdaim.crm.ent.entity;

import java.util.List;

/**
 * 网站信息
 */
public class EntWebEntity {

    /**
     * 网站首页(多个)
     */
    private List<String> homePages;

    /**
     * 网站名称
     */
    private String webName;

    /**
     * 域名(多个)
     */
    private List<String> domains;

    /**
     * 备案号
     */
    private String icp;

    public List<String> getHomePages() {
        return homePages;
    }

    public void setHomePages(List<String> homePages) {
        this.homePages = homePages;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public String getIcp() {
        return icp;
    }

    public void setIcp(String icp) {
        this.icp = icp;
    }

    @Override
    public String toString() {
        return "EntWeb{" +
                "homePages=" + homePages +
                ", webName='" + webName + '\'' +
                ", domains=" + domains +
                ", icp='" + icp + '\'' +
                '}';
    }
}

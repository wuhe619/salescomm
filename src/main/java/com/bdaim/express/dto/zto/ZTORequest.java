package com.bdaim.express.dto.zto;

public class ZTORequest {
    private ZTO content;
    private String partner;
    private String datetime;
    private String verify;

    public ZTO getContent() {
        return content;
    }

    public void setContent(ZTO content) {
        this.content = content;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }
}

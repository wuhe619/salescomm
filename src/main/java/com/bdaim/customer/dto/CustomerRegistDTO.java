package com.bdaim.customer.dto;

import java.io.Serializable;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
public class CustomerRegistDTO implements Serializable {
    private String userId;
    //企业帐号
    private String name;
    private String userName;
    private String password;
    private int userPwdLevel;
    private String enterpriseName;
    private String province;
    private String city;
    private String country;
    private String address;
    private String bliNumber;
    private String bliPath;
    private String taxPayerId;
    private String taxpayerCertificatePath;
    private String bank;
    private String bankAccount;
    private String bankAccountCertificate;
    private String realName;
    private String mobile;
    private String email;
    private String custId;
    private String startTime;
    private String endTime;
    private String dealType;
    private String status;
    //联系人职位
    private String title;
    //所属行业
    private String industry;
    //销售负责人
    private String salePerson;
    //渠道
    private String channel;
    //打印员
    private String printer;
    //封装员
    private String packager;
    //身份证正面
    private String idCardFront;
    //身份证反面
    private String idCardBack;
    //快递设置 1 常用地址（多个） 2 常用地址（多个）
    private String expressConfig;
    private String county;
    private String serviceMode;
    private String resource;
    private String createId;

    /**
     * 注册来源
     *
     * 0-后台开户 3-联客官网 4-众麦官网
     */
    private String source;


    /**
     * 营销类型:1-B2C营销  2-B2B营销
     */
    private Integer marketingType;

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    /**
     * 所属行业ID
     */
    private String industryId;
    /**
     * 场站id
     */
    private String stationId;
    /**
     * 货主单位名称
     */
    private String owner_name;
    /**
     * 申报单位代码
     */
    private String agent_code;
    /**
     * 报关员代码
     */
    private String declare_no;

    /**
     * IC卡号
     */
    private String input_no;
    /**
     * 行业画像
     */
    private String intenIndustry;

    /**
     * 所属品牌
     */
    private String brand;
    /**
     * ip白名单
     */
    private String whiteIps;

    /**
     * 余额预警
     */
    private String balance_warning_config;

    private String warning_money;
    private String email_link;
    private String short_msg_link;
    private String api_token;
    private String industryPictureValue;
    private String remain_amount;
    /**
     * 结算方式（0：预付1：后付）
     */
    private String settlement_method;

    /**
     * 代理商ID
     */
    private String agentId;

    /**
     * 佣金比例(百分比)
     */
    private String commissionRate;

    /**
     * 余额提醒配置
     * {"balanceLower":"余额下限","smsConfig":{"status":true,"value":"15166662222"},"emailConfig":{"status":true,"value":"www@qq.com"}}
     */
    private String balanceRemind;

    public String getSettlement_method() {
        return settlement_method;
    }

    public void setSettlement_method(String settlement_method) {
        this.settlement_method = settlement_method;
    }

    public String getRemain_amount() {
        return remain_amount;
    }

    public void setRemain_amount(String remain_amount) {
        this.remain_amount = remain_amount;
    }

    public String getIndustryPictureValue() {
        return industryPictureValue;
    }

    public void setIndustryPictureValue(String industryPictureValue) {
        this.industryPictureValue = industryPictureValue;
    }

    public String getApi_token() {
        return api_token;
    }

    public void setApi_token(String api_token) {
        this.api_token = api_token;
    }

    public String getEmail_link() {
        return email_link;
    }

    public void setEmail_link(String email_link) {
        this.email_link = email_link;
    }

    public String getShort_msg_link() {
        return short_msg_link;
    }

    public void setShort_msg_link(String short_msg_link) {
        this.short_msg_link = short_msg_link;
    }

    public String getWarning_money() {

        return warning_money;
    }

    public void setWarning_money(String warning_money) {
        this.warning_money = warning_money;
    }

    public String getBalance_warning_config() {
        return balance_warning_config;
    }

    public void setBalance_warning_config(String balance_warning_config) {
        this.balance_warning_config = balance_warning_config;
    }

    public String getWhiteIps() {
        return whiteIps;
    }

    public void setWhiteIps(String whiteIps) {
        this.whiteIps = whiteIps;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getIntenIndustry() {
        return intenIndustry;
    }

    public void setIntenIndustry(String intenIndustry) {
        this.intenIndustry = intenIndustry;
    }

    public String getInput_no() {
        return input_no;
    }

    public void setInput_no(String input_no) {
        this.input_no = input_no;
    }

    public String getDeclare_no() {
        return declare_no;
    }

    public void setDeclare_no(String declare_no) {
        this.declare_no = declare_no;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getAgent_code() {
        return agent_code;
    }

    public void setAgent_code(String agent_code) {
        this.agent_code = agent_code;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getExpressConfig() {
        return expressConfig;
    }

    public void setExpressConfig(String expressConfig) {
        this.expressConfig = expressConfig;
    }

    public String getIdCardFront() {
        return idCardFront;
    }

    public void setIdCardFront(String idCardFront) {
        this.idCardFront = idCardFront;
    }

    public String getIdCardBack() {
        return idCardBack;
    }

    public void setIdCardBack(String idCardBack) {
        this.idCardBack = idCardBack;
    }

    public String getPrinter() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer = printer;
    }

    public String getPackager() {
        return packager;
    }

    public void setPackager(String packager) {
        this.packager = packager;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDealType() {
        return dealType;
    }

    public void setDealType(String dealType) {
        this.dealType = dealType;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getSalePerson() {
        return salePerson;
    }

    public void setSalePerson(String salePerson) {
        this.salePerson = salePerson;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBliNumber() {
        return bliNumber;
    }

    public void setBliNumber(String bliNumber) {
        this.bliNumber = bliNumber;
    }

    public String getBliPath() {
        return bliPath;
    }

    public void setBliPath(String bliPath) {
        this.bliPath = bliPath;
    }

    public String getTaxPayerId() {
        return taxPayerId;
    }

    public void setTaxPayerId(String taxPayerId) {
        this.taxPayerId = taxPayerId;
    }

    public String getTaxpayerCertificatePath() {
        return taxpayerCertificatePath;
    }

    public void setTaxpayerCertificatePath(String taxpayerCertificatePath) {
        this.taxpayerCertificatePath = taxpayerCertificatePath;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAccountCertificate() {
        return bankAccountCertificate;
    }

    public void setBankAccountCertificate(String bankAccountCertificate) {
        this.bankAccountCertificate = bankAccountCertificate;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUserPwdLevel() {
        return userPwdLevel;
    }

    public void setUserPwdLevel(int userPwdLevel) {
        this.userPwdLevel = userPwdLevel;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }

    public String getIndustryId() {
        return industryId;
    }

    public void setIndustryId(String industryId) {
        this.industryId = industryId;
    }

    public Integer getMarketingType() {
        return marketingType;
    }

    public void setMarketingType(Integer marketingType) {
        this.marketingType = marketingType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(String commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getBalanceRemind() {
        return balanceRemind;
    }

    public void setBalanceRemind(String balanceRemind) {
        this.balanceRemind = balanceRemind;
    }
}

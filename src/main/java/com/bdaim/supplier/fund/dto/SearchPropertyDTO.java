package com.bdaim.supplier.fund.dto;

/**
 * @author duanliying
 * @date 2019/6/3
 * @description 列表检索条件Dto
 */
public class SearchPropertyDTO {
    //投资期限
    private String investmentTerm;
    //特点（理财）
    private String productSpecial;
    //特色标签（理财）
    private String featuredLabels;
    //安全等级
    private String safeRank;
    //币种
    private String currency;
    //卡片组织
    private String cardOrganization;
    //卡片等级
    private String cardLevel;
    //卡片用途
    private String useClass;
    //公积金
    private String accumulationFund;
    //总经营流水
    private String manageAmount;
    //总经营年限
    private String manageMonth;
    /**
     * 经营执照  1本地 2 外地
     */
    private String manageArea;
    /**
     * 对公账户收入
     */
    private String businessOwnerConfig;
    /**
     * 是否注册过营业执照  1:注册  我：未注册
     */
    private String manageLicenseStatus;
    //社保连续缴存时间
    private String accumulationFundTime;
    //月收入
    private String revenueConfigValue;
    /**
     * 收入配置 1 现金 2 发卡
     */
    private String revenueConfig;
    //缴存基数
    private String accumulationFundValue;
    //社保
    private String socialSecurity;
    //贷款开始时间
    private String termStart;
    //贷款结束时间
    private String termEnd;
    //贷款金额开始区间
    private String loanAmountStart;
    //贷款金额结束区间
    private String loanAmountEnd;
    //商品状态
    private String status;
    //是否抵押 1  无需抵押  2  抵押
    private String ifMortgage;
    //特点检索条件 无抵押  审批快  通过率高
    private String trait;
    //商品状态
    private String city;
    //列表类型
    private String type;
    //商品id
    private String productId;
    //商品名称
    private String name;
    //商品类型
    private String dicType;
    //机构id
    private String institutionId;
    //品牌id
    private String brandId;
    //品牌名称
    private String brandName;
    //是否是优质品牌
    private String isQualityBrand;
    //职业身份  1上班族  2个体户  3企业主  4无固定职位 5学生
    private String professionalIdentity;
    //房产  1无产权 2 小产权 3经济适用
    private String houseConfig;
    private String houseValuetion;
    //是否有车  1无车  2无车准备买  3名下有车
    private String carType;
    //信用情况 1超三次超过90天  2 少三次少90   3信用良好 4无信用卡
    private String creditSituation;
    //行业类型1:银行 2：保险 3：互金
    private String industryType;
    //推荐设置（热门推荐和主题推荐）
    private String recommendConfig;
    //综合排序 0 正序 1 倒叙
    private String comprehensive;
    //额度排序 0 正序 1 倒叙
    private String quota;
    //申请人数排序 0 正序 1 倒叙
    private String applicantsNum;
    //理财
    //风险等级
    private String riskLevel;
    //封闭期类型 1：1到3个月   2：3到6个月 3：6个月以上
    private String timeType;
    //封闭期
    private String durationDay;
    //起购额度
    private String minPurchaseAmount;

    //推荐商品不满足pageSize 是否需要填充数据 1 是 2 否
    private String fillType;
    private Integer pageNum;
    private Integer pageSize;

    /**
     * 渠道类型 1-自有渠道 2-外部渠道
     */
    private Integer channelType;
    /**
     * 推广活动开始时间
     */
    private String activityStartTime;
    /**
     * 推广活动结束时间
     */
    private String activityEndTime;
    /**
     * 推广活动所属渠道
     */
    private String extensionChannel;
    /**
     * 推广链接
     */
    private String extensionUrl;
    /**
     * 触达方式
     */
    private String touchType;

    /**
     * 广告位所属平台 H5;WEB;APP
     */
    private String adPlatform;
    /**
     * 广告位唯一编码
     */
    private String adCode;

    public String getHouseValuetion() {
        return houseValuetion;
    }

    public void setHouseValuetion(String houseValuetion) {
        this.houseValuetion = houseValuetion;
    }

    public String getTimeType() {
        return timeType;
    }

    public void setTimeType(String timeType) {
        this.timeType = timeType;
    }

    public String getFeaturedLabels() {
        return featuredLabels;
    }

    public void setFeaturedLabels(String featuredLabels) {
        this.featuredLabels = featuredLabels;
    }

    public String getMinPurchaseAmount() {
        return minPurchaseAmount;
    }

    public void setMinPurchaseAmount(String minPurchaseAmount) {
        this.minPurchaseAmount = minPurchaseAmount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getComprehensive() {
        return comprehensive;
    }

    public void setComprehensive(String comprehensive) {
        this.comprehensive = comprehensive;
    }

    public String getDurationDay() {
        return durationDay;
    }

    public void setDurationDay(String durationDay) {
        this.durationDay = durationDay;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public String getApplicantsNum() {
        return applicantsNum;
    }

    public void setApplicantsNum(String applicantsNum) {
        this.applicantsNum = applicantsNum;
    }

    public String getRecommendConfig() {
        return recommendConfig;
    }

    public void setRecommendConfig(String recommendConfig) {
        this.recommendConfig = recommendConfig;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getCardOrganization() {
        return cardOrganization;
    }

    public void setCardOrganization(String cardOrganization) {
        this.cardOrganization = cardOrganization;
    }

    public String getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(String cardLevel) {
        this.cardLevel = cardLevel;
    }

    public String getUseClass() {
        return useClass;
    }

    public void setUseClass(String useClass) {
        this.useClass = useClass;
    }

    public String getRevenueConfigValue() {
        return revenueConfigValue;
    }

    public void setRevenueConfigValue(String revenueConfigValue) {
        this.revenueConfigValue = revenueConfigValue;
    }

    public String getManageAmount() {
        return manageAmount;
    }

    public void setManageAmount(String manageAmount) {
        this.manageAmount = manageAmount;
    }

    public String getManageMonth() {
        return manageMonth;
    }

    public void setManageMonth(String manageMonth) {
        this.manageMonth = manageMonth;
    }

    public String getManageLicenseStatus() {
        return manageLicenseStatus;
    }

    public void setManageLicenseStatus(String manageLicenseStatus) {
        this.manageLicenseStatus = manageLicenseStatus;
    }

    public String getAccumulationFundTime() {
        return accumulationFundTime;
    }

    public String getFillType() {
        return fillType;
    }

    public void setFillType(String fillType) {
        this.fillType = fillType;
    }

    public void setAccumulationFundTime(String accumulationFundTime) {
        this.accumulationFundTime = accumulationFundTime;
    }

    public String getAccumulationFundValue() {
        return accumulationFundValue;
    }

    public void setAccumulationFundValue(String accumulationFundValue) {
        this.accumulationFundValue = accumulationFundValue;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getAccumulationFund() {
        return accumulationFund;
    }

    public void setAccumulationFund(String accumulationFund) {
        this.accumulationFund = accumulationFund;
    }

    public String getSocialSecurity() {
        return socialSecurity;
    }

    public void setSocialSecurity(String socialSecurity) {
        this.socialSecurity = socialSecurity;
    }

    public String getTrait() {
        return trait;
    }

    public void setTrait(String trait) {
        this.trait = trait;
    }

    public String getIfMortgage() {
        return ifMortgage;
    }

    public void setIfMortgage(String ifMortgage) {
        this.ifMortgage = ifMortgage;
    }

    public String getCreditSituation() {
        return creditSituation;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCreditSituation(String creditSituation) {
        this.creditSituation = creditSituation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProfessionalIdentity() {
        return professionalIdentity;
    }

    public void setProfessionalIdentity(String professionalIdentity) {
        this.professionalIdentity = professionalIdentity;
    }

    public String getHouseConfig() {
        return houseConfig;
    }

    public void setHouseConfig(String houseConfig) {
        this.houseConfig = houseConfig;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getDicType() {
        return dicType;
    }

    public void setDicType(String dicType) {
        this.dicType = dicType;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getIsQualityBrand() {
        return isQualityBrand;
    }

    public void setIsQualityBrand(String isQualityBrand) {
        this.isQualityBrand = isQualityBrand;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public Integer getChannelType() {
        return channelType;
    }

    public void setChannelType(Integer channelType) {
        this.channelType = channelType;
    }

    public String getInvestmentTerm() {
        return investmentTerm;
    }

    public void setInvestmentTerm(String investmentTerm) {
        this.investmentTerm = investmentTerm;
    }

    public String getProductSpecial() {
        return productSpecial;
    }

    public void setProductSpecial(String productSpecial) {
        this.productSpecial = productSpecial;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSafeRank() {
        return safeRank;
    }

    public void setSafeRank(String safeRank) {
        this.safeRank = safeRank;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getActivityStartTime() {
        return activityStartTime;
    }

    public void setActivityStartTime(String activityStartTime) {
        this.activityStartTime = activityStartTime;
    }

    public String getActivityEndTime() {
        return activityEndTime;
    }

    public void setActivityEndTime(String activityEndTime) {
        this.activityEndTime = activityEndTime;
    }

    public String getExtensionChannel() {
        return extensionChannel;
    }

    public void setExtensionChannel(String extensionChannel) {
        this.extensionChannel = extensionChannel;
    }

    public String getExtensionUrl() {
        return extensionUrl;
    }

    public void setExtensionUrl(String extensionUrl) {
        this.extensionUrl = extensionUrl;
    }

    public String getTouchType() {
        return touchType;
    }

    public void setTouchType(String touchType) {
        this.touchType = touchType;
    }

    public String getAdPlatform() {
        return adPlatform;
    }

    public void setAdPlatform(String adPlatform) {
        this.adPlatform = adPlatform;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getTermStart() {
        return termStart;
    }

    public void setTermStart(String termStart) {
        this.termStart = termStart;
    }

    public String getTermEnd() {
        return termEnd;
    }

    public void setTermEnd(String termEnd) {
        this.termEnd = termEnd;
    }

    public String getLoanAmountStart() {
        return loanAmountStart;
    }

    public void setLoanAmountStart(String loanAmountStart) {
        this.loanAmountStart = loanAmountStart;
    }

    public String getLoanAmountEnd() {
        return loanAmountEnd;
    }

    public void setLoanAmountEnd(String loanAmountEnd) {
        this.loanAmountEnd = loanAmountEnd;
    }

    public String getRevenueConfig() {
        return revenueConfig;
    }

    public void setRevenueConfig(String revenueConfig) {
        this.revenueConfig = revenueConfig;
    }

    public String getManageArea() {
        return manageArea;
    }

    public void setManageArea(String manageArea) {
        this.manageArea = manageArea;
    }

    public String getBusinessOwnerConfig() {
        return businessOwnerConfig;
    }

    public void setBusinessOwnerConfig(String businessOwnerConfig) {
        this.businessOwnerConfig = businessOwnerConfig;
    }
}

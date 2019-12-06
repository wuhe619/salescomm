package com.bdaim.api.Dto;

import com.bdaim.api.entity.ApiEntity;

import java.util.List;

public class ApiData {
    private String apiName;
    private String apiVersion;
    private String context;
    private String contextTemplate;
    private String visibility;
    private String apiThumb;
    private String description;
    private String tags;
    private String endpointType;
    private String productionendpoints;
    private String defaultVersion;
    private String tier;
    private String transportHttp;
    private String toggleThrottle;
    private int productionTps;
    private String responseCache;
    private List<ApiResource> resourceList;
    private ApiDefine api_define;
    private String rsIds;

    private int urlMappingId;
    private String status;
    private int apiId;

    public ApiData() {
    }

    public ApiData(ApiEntity entity) {
        this.apiName = entity.getName();
        this.context = entity.getContext();
        this.contextTemplate = entity.getContextTexplate();
        this.apiId = entity.getApiId();
        this.apiVersion=entity.getVersion();
        this.status=String.valueOf(entity.getStatus());

    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRsIds() {
        return rsIds;
    }

    public void setRsIds(String rsIds) {
        this.rsIds = rsIds;
    }

    public int getUrlMappingId() {
        return urlMappingId;
    }

    public void setUrlMappingId(int urlMappingId) {
        this.urlMappingId = urlMappingId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContextTemplate() {
        return contextTemplate;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getApiThumb() {
        return apiThumb;
    }

    public void setApiThumb(String apiThumb) {
        this.apiThumb = apiThumb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getProductionendpoints() {
        return productionendpoints;
    }

    public void setProductionendpoints(String productionendpoints) {
        this.productionendpoints = productionendpoints;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getTransportHttp() {
        return transportHttp;
    }

    public void setTransportHttp(String transportHttp) {
        this.transportHttp = transportHttp;
    }

    public String getToggleThrottle() {
        return toggleThrottle;
    }

    public void setToggleThrottle(String toggleThrottle) {
        this.toggleThrottle = toggleThrottle;
    }

    public int getProductionTps() {
        return productionTps;
    }

    public void setProductionTps(int productionTps) {
        this.productionTps = productionTps;
    }

    public String getResponseCache() {
        return responseCache;
    }

    public void setResponseCache(String responseCache) {
        this.responseCache = responseCache;
    }

    public List<ApiResource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ApiResource> resourceList) {
        this.resourceList = resourceList;
    }

    public ApiDefine getApi_define() {
        return api_define;
    }

    public void setApi_define(ApiDefine api_define) {
        this.api_define = api_define;
    }
}

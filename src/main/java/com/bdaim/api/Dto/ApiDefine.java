package com.bdaim.api.Dto;

public class ApiDefine {
    private String resource_url_pattern;
    private String request_method;
    private String description;
    private String params;

    public String getResource_url_pattern() {
        return resource_url_pattern;
    }

    public void setResource_url_pattern(String resource_url_pattern) {
        this.resource_url_pattern = resource_url_pattern;
    }

    public String getRequest_method() {
        return request_method;
    }

    public void setRequest_method(String request_method) {
        this.request_method = request_method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}

package com.bdaim.api.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="am_api_url_mapping")
public class ApiUrlMappingEntity implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "URL_MAPPING_ID")
    private int id;
    @Column(name = "API_ID")
    private int apiId;
    @Column(name = "HTTP_METHOD")
    private String httpMethod;
    @Column(name = "AUTH_SCHEME")
    private String authScheme;
    @Column(name = "URL_PATTERN")
    private String urlPattern;
    @Column(name = "THROTTLING_TIER")
    private String throttlingTier;
    @Column(name = "MEDIATION_SCRIPT")
    private String mediationScript;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getThrottlingTier() {
        return throttlingTier;
    }

    public void setThrottlingTier(String throttlingTier) {
        this.throttlingTier = throttlingTier;
    }

    public String getMediationScript() {
        return mediationScript;
    }

    public void setMediationScript(String mediationScript) {
        this.mediationScript = mediationScript;
    }
}

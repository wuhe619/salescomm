package com.bdaim.common.util.wechat.entity;


import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * <pre>
 * 模板列表信息
 * Created by Binary Wang on 2016-10-17.
 * </pre>
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
public class WxMpTemplate implements Serializable {
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final long serialVersionUID = -7366474522571199372L;

    /**
     * template_id.
     * 模板ID
     */
    @SerializedName("template_id")
    private String templateId;
    /**
     * title.
     * 模板标题
     */
    @SerializedName("title")
    private String title;
    /**
     * primary_industry.
     * 模板所属行业的一级行业
     */
    @SerializedName("primary_industry")
    private String primaryIndustry;
    /**
     * deputy_industry.
     * 模板所属行业的二级行业
     */
    @SerializedName("deputy_industry")
    private String deputyIndustry;
    /**
     * content.
     * 模板内容
     */
    @SerializedName("content")
    private String content;
    /**
     * example.
     * 模板示例
     */
    @SerializedName("example")
    private String example;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrimaryIndustry() {
        return primaryIndustry;
    }

    public void setPrimaryIndustry(String primaryIndustry) {
        this.primaryIndustry = primaryIndustry;
    }

    public String getDeputyIndustry() {
        return deputyIndustry;
    }

    public void setDeputyIndustry(String deputyIndustry) {
        this.deputyIndustry = deputyIndustry;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}

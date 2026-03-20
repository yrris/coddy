package com.yrris.coddy.ai.model;

import dev.langchain4j.model.output.structured.Description;

@Description("Generated result for native multi-file website mode")
public class MultiFileCodeResult {

    @Description("index.html content")
    private String htmlCode;

    @Description("style.css content")
    private String cssCode;

    @Description("script.js content")
    private String jsCode;

    @Description("Short summary of the generated website")
    private String description;

    public String getHtmlCode() {
        return htmlCode;
    }

    public void setHtmlCode(String htmlCode) {
        this.htmlCode = htmlCode;
    }

    public String getCssCode() {
        return cssCode;
    }

    public void setCssCode(String cssCode) {
        this.cssCode = cssCode;
    }

    public String getJsCode() {
        return jsCode;
    }

    public void setJsCode(String jsCode) {
        this.jsCode = jsCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

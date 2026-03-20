package com.yrris.coddy.ai.model;

import dev.langchain4j.model.output.structured.Description;

@Description("Generated result for native single-file HTML mode")
public class HtmlCodeResult {

    @Description("Complete HTML code including inline CSS and JavaScript")
    private String htmlCode;

    @Description("Short summary of the generated website")
    private String description;

    public String getHtmlCode() {
        return htmlCode;
    }

    public void setHtmlCode(String htmlCode) {
        this.htmlCode = htmlCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

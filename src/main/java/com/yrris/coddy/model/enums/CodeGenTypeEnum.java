package com.yrris.coddy.model.enums;

import org.springframework.util.StringUtils;

public enum CodeGenTypeEnum {

    HTML_SINGLE("Native HTML single file", "HTML_SINGLE"),
    HTML_MULTI("Native HTML multi file", "HTML_MULTI"),
    REACT_VITE("React Vite project", "REACT_VITE");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public static CodeGenTypeEnum fromValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        for (CodeGenTypeEnum codeGenTypeEnum : CodeGenTypeEnum.values()) {
            if (codeGenTypeEnum.value.equalsIgnoreCase(value)
                    || codeGenTypeEnum.name().equalsIgnoreCase(value)) {
                return codeGenTypeEnum;
            }
        }
        return null;
    }
}

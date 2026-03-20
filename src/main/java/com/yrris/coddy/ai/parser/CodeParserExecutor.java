package com.yrris.coddy.ai.parser;

import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class CodeParserExecutor {

    private final HtmlSingleCodeParser htmlSingleCodeParser;

    private final HtmlMultiCodeParser htmlMultiCodeParser;

    public CodeParserExecutor(HtmlSingleCodeParser htmlSingleCodeParser, HtmlMultiCodeParser htmlMultiCodeParser) {
        this.htmlSingleCodeParser = htmlSingleCodeParser;
        this.htmlMultiCodeParser = htmlMultiCodeParser;
    }

    public Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        if (codeGenType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Code generation type is required");
        }
        return switch (codeGenType) {
            case HTML_SINGLE -> htmlSingleCodeParser.parseCode(codeContent);
            case HTML_MULTI -> htmlMultiCodeParser.parseCode(codeContent);
        };
    }
}

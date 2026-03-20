package com.yrris.coddy.ai.parser;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlSingleCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN =
            Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        String normalizedContent = StringUtils.hasText(codeContent) ? codeContent : "";

        String htmlCode = extractCodeByPattern(normalizedContent, HTML_CODE_PATTERN);
        if (StringUtils.hasText(htmlCode)) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            result.setHtmlCode(normalizedContent.trim());
        }

        return result;
    }

    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}

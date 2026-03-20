package com.yrris.coddy.ai.parser;

import com.yrris.coddy.ai.model.MultiFileCodeResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlMultiCodeParser implements CodeParser<MultiFileCodeResult> {

    private static final Pattern HTML_CODE_PATTERN =
            Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern CSS_CODE_PATTERN =
            Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_CODE_PATTERN =
            Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        String normalizedContent = StringUtils.hasText(codeContent) ? codeContent : "";

        String htmlCode = extractCodeByPattern(normalizedContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(normalizedContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(normalizedContent, JS_CODE_PATTERN);

        if (StringUtils.hasText(htmlCode)) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            result.setHtmlCode(buildFallbackHtml());
        }
        result.setCssCode(StringUtils.hasText(cssCode) ? cssCode.trim() : "");
        result.setJsCode(StringUtils.hasText(jsCode) ? jsCode.trim() : "");

        return result;
    }

    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String buildFallbackHtml() {
        return "<!doctype html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\" />\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "  <title>Generated Site</title>\n"
                + "  <link rel=\"stylesheet\" href=\"style.css\" />\n"
                + "</head>\n"
                + "<body>\n"
                + "  <main>Generated content is unavailable.</main>\n"
                + "  <script src=\"script.js\"></script>\n"
                + "</body>\n"
                + "</html>\n";
    }
}

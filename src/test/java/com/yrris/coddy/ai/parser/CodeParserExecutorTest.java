package com.yrris.coddy.ai.parser;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CodeParserExecutorTest {

    private final CodeParserExecutor codeParserExecutor = new CodeParserExecutor(
            new HtmlSingleCodeParser(),
            new HtmlMultiCodeParser()
    );

    @Test
    void shouldParseHtmlSingleCode() {
        String content = """
                ```html
                <!doctype html>
                <html><body><h1>Hello</h1></body></html>
                ```
                """;

        Object parsed = codeParserExecutor.executeParser(content, CodeGenTypeEnum.HTML_SINGLE);
        Assertions.assertInstanceOf(HtmlCodeResult.class, parsed);
        HtmlCodeResult result = (HtmlCodeResult) parsed;
        Assertions.assertTrue(result.getHtmlCode().contains("<h1>Hello</h1>"));
    }

    @Test
    void shouldParseHtmlMultiCode() {
        String content = """
                ```html
                <!doctype html>
                <html>
                  <head><link rel="stylesheet" href="style.css"></head>
                  <body><script src="script.js"></script></body>
                </html>
                ```
                ```css
                body { background: #000; }
                ```
                ```javascript
                console.log('ready');
                ```
                """;

        Object parsed = codeParserExecutor.executeParser(content, CodeGenTypeEnum.HTML_MULTI);
        Assertions.assertInstanceOf(MultiFileCodeResult.class, parsed);
        MultiFileCodeResult result = (MultiFileCodeResult) parsed;
        Assertions.assertTrue(result.getHtmlCode().contains("style.css"));
        Assertions.assertTrue(result.getCssCode().contains("background"));
        Assertions.assertTrue(result.getJsCode().contains("ready"));
    }
}

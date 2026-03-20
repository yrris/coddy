package com.yrris.coddy.ai.service;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
import com.yrris.coddy.config.AiGenerationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock AI service for local development and automated tests.
 * It follows the same output contracts as the LangChain4j implementation.
 */
@Service
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockAiCodeGeneratorService implements AiCodeGeneratorService {

    private final AiGenerationProperties aiGenerationProperties;

    public MockAiCodeGeneratorService(AiGenerationProperties aiGenerationProperties) {
        this.aiGenerationProperties = aiGenerationProperties;
    }

    @Override
    public HtmlCodeResult generateHtmlCode(String userMessage) {
        HtmlCodeResult result = new HtmlCodeResult();
        result.setHtmlCode(buildSingleHtmlCode(normalizePrompt(userMessage)));
        result.setDescription("Mock generated single-file HTML website");
        return result;
    }

    @Override
    public MultiFileCodeResult generateMultiFileCode(String userMessage) {
        String prompt = normalizePrompt(userMessage);
        MultiFileCodeResult result = new MultiFileCodeResult();
        result.setHtmlCode(buildMultiHtmlCode(prompt));
        result.setCssCode(buildMultiCssCode());
        result.setJsCode(buildMultiJsCode());
        result.setDescription("Mock generated multi-file website");
        return result;
    }

    @Override
    public Flux<String> generateHtmlCodeStream(String userMessage) {
        String content = "```html\n" + buildSingleHtmlCode(normalizePrompt(userMessage)) + "\n```";
        return buildStream(content);
    }

    @Override
    public Flux<String> generateMultiFileCodeStream(String userMessage) {
        String prompt = normalizePrompt(userMessage);
        String content = "```html\n"
                + buildMultiHtmlCode(prompt)
                + "\n```\n"
                + "```css\n"
                + buildMultiCssCode()
                + "\n```\n"
                + "```javascript\n"
                + buildMultiJsCode()
                + "\n```";
        return buildStream(content);
    }

    private Flux<String> buildStream(String content) {
        int chunkSize = Math.max(1, aiGenerationProperties.getStreamChunkSize());
        long delayMs = Math.max(0, aiGenerationProperties.getStreamDelayMs());
        List<String> chunks = splitToChunks(content, chunkSize);
        Flux<String> stream = Flux.fromIterable(chunks);
        if (delayMs > 0) {
            return stream.delayElements(Duration.ofMillis(delayMs));
        }
        return stream;
    }

    private List<String> splitToChunks(String content, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int index = 0; index < content.length(); index += chunkSize) {
            int endIndex = Math.min(index + chunkSize, content.length());
            chunks.add(content.substring(index, endIndex));
        }
        return chunks;
    }

    private String normalizePrompt(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            return "Generated Website";
        }
        return userMessage.replaceAll("[\\r\\n]+", " ").trim();
    }

    private String buildSingleHtmlCode(String title) {
        return "<!doctype html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\" />\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "  <title>" + title + "</title>\n"
                + "  <style>\n"
                + "    :root { color-scheme: dark; }\n"
                + "    body { margin: 0; font-family: Arial, sans-serif; background: #0b1220; color: #e2e8f0; }\n"
                + "    .shell { max-width: 900px; margin: 52px auto; padding: 24px; background: #111827; border-radius: 14px; }\n"
                + "    h1 { margin: 0 0 12px; font-size: 30px; }\n"
                + "    p { color: #94a3b8; }\n"
                + "    button { margin-top: 18px; border: 0; border-radius: 9px; padding: 10px 14px; background: #2563eb; color: #fff; cursor: pointer; }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <main class=\"shell\">\n"
                + "    <h1>" + title + "</h1>\n"
                + "    <p>Mock provider output. Switch to LangChain4j mode for real model generation.</p>\n"
                + "    <button id=\"open\">Try Interaction</button>\n"
                + "  </main>\n"
                + "  <script>\n"
                + "    document.getElementById('open')?.addEventListener('click', () => alert('Interaction ready.'));\n"
                + "  </script>\n"
                + "</body>\n"
                + "</html>";
    }

    private String buildMultiHtmlCode(String title) {
        return "<!doctype html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\" />\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "  <title>" + title + "</title>\n"
                + "  <link rel=\"stylesheet\" href=\"style.css\" />\n"
                + "</head>\n"
                + "<body>\n"
                + "  <main class=\"shell\">\n"
                + "    <h1>" + title + "</h1>\n"
                + "    <p>Mock multi-file output for local validation.</p>\n"
                + "    <ul id=\"timeline\"></ul>\n"
                + "  </main>\n"
                + "  <script src=\"script.js\"></script>\n"
                + "</body>\n"
                + "</html>";
    }

    private String buildMultiCssCode() {
        return ":root { color-scheme: dark; }\n"
                + "body { margin: 0; font-family: Arial, sans-serif; background: #020617; color: #e2e8f0; }\n"
                + ".shell { max-width: 860px; margin: 48px auto; padding: 24px; border: 1px solid #1e293b; border-radius: 14px; background: #0f172a; }\n"
                + "h1 { margin-bottom: 8px; }\n"
                + "p { color: #94a3b8; }\n"
                + "li { margin: 8px 0; }";
    }

    private String buildMultiJsCode() {
        return "const timeline = document.getElementById('timeline');\n"
                + "['Discover', 'Design', 'Deliver'].forEach((phase, index) => {\n"
                + "  const li = document.createElement('li');\n"
                + "  li.textContent = `${index + 1}. ${phase}`;\n"
                + "  timeline?.appendChild(li);\n"
                + "});";
    }
}

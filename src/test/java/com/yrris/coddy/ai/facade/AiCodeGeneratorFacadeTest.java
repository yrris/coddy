package com.yrris.coddy.ai.facade;

import com.yrris.coddy.ai.model.CodeGenerationOutput;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void shouldGenerateAndSaveHtmlSingle() {
        CodeGenerationOutput output = aiCodeGeneratorFacade.generateAndSaveCode(
                "Build a landing page for a SaaS analytics product",
                CodeGenTypeEnum.HTML_SINGLE
        );

        Assertions.assertNotNull(output);
        Assertions.assertEquals("HTML_SINGLE", output.getCodeGenType().getValue());
        Assertions.assertTrue(output.getFiles().containsKey("index.html"));
        Assertions.assertTrue(new File(output.getOutputDir()).exists());
    }

    @Test
    void shouldGenerateAndSaveHtmlMultiWithStream() {
        StringBuilder streamContent = new StringBuilder();

        CodeGenerationOutput output = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "Build a simple task board",
                CodeGenTypeEnum.HTML_MULTI,
                streamContent::append
        );

        Assertions.assertNotNull(output);
        Assertions.assertFalse(streamContent.isEmpty());
        Assertions.assertTrue(output.getFiles().containsKey("index.html"));
        Assertions.assertTrue(output.getFiles().containsKey("style.css"));
        Assertions.assertTrue(output.getFiles().containsKey("script.js"));
        Assertions.assertTrue(new File(output.getOutputDir()).exists());
    }
}

package com.yrris.coddy.ai.saver;

import com.yrris.coddy.config.AiGenerationProperties;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;

@Component
public class CodeFileSaverExecutor {

    private final HtmlSingleCodeFileSaverTemplate htmlSingleCodeFileSaver;

    private final HtmlMultiCodeFileSaverTemplate htmlMultiCodeFileSaver;

    public CodeFileSaverExecutor(AiGenerationProperties aiGenerationProperties) {
        String outputRootDir = aiGenerationProperties.getOutputRootDir();
        if (!Paths.get(outputRootDir).isAbsolute()) {
            outputRootDir = Paths.get(System.getProperty("user.dir"), outputRootDir).toString();
        }
        this.htmlSingleCodeFileSaver = new HtmlSingleCodeFileSaverTemplate(outputRootDir);
        this.htmlMultiCodeFileSaver = new HtmlMultiCodeFileSaverTemplate(outputRootDir);
    }

    public File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        if (codeGenType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Code generation type is required");
        }
        return switch (codeGenType) {
            case HTML_SINGLE -> htmlSingleCodeFileSaver.saveCode((com.yrris.coddy.ai.model.HtmlCodeResult) codeResult,
                    CodeGenTypeEnum.HTML_SINGLE);
            case HTML_MULTI -> htmlMultiCodeFileSaver.saveCode((com.yrris.coddy.ai.model.MultiFileCodeResult) codeResult,
                    CodeGenTypeEnum.HTML_MULTI);
        };
    }
}

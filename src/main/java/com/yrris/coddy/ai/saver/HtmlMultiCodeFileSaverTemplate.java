package com.yrris.coddy.ai.saver;

import com.yrris.coddy.ai.model.MultiFileCodeResult;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import org.springframework.util.StringUtils;

public class HtmlMultiCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    public HtmlMultiCodeFileSaverTemplate(String outputRootDir) {
        super(outputRootDir);
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        if (!StringUtils.hasText(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Generated HTML content is empty");
        }
    }
}

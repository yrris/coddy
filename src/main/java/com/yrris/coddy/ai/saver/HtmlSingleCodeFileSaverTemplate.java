package com.yrris.coddy.ai.saver;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import org.springframework.util.StringUtils;

public class HtmlSingleCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    public HtmlSingleCodeFileSaverTemplate(String outputRootDir) {
        super(outputRootDir);
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (!StringUtils.hasText(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Generated HTML content is empty");
        }
    }
}

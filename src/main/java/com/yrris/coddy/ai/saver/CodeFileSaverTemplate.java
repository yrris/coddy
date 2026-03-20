package com.yrris.coddy.ai.saver;

import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public abstract class CodeFileSaverTemplate<T> {

    private final String outputRootDir;

    protected CodeFileSaverTemplate(String outputRootDir) {
        this.outputRootDir = outputRootDir;
    }

    public final File saveCode(T result, CodeGenTypeEnum codeGenType) {
        validateInput(result);
        String baseDirPath = buildUniqueDir(codeGenType);
        saveFiles(result, baseDirPath);
        return new File(baseDirPath);
    }

    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Code result cannot be null");
        }
    }

    protected final String buildUniqueDir(CodeGenTypeEnum codeGenType) {
        String uniqueDirName = codeGenType.getValue().toLowerCase() + "_" + UUID.randomUUID().toString().replace("-", "");
        Path dirPath = Paths.get(outputRootDir, uniqueDirName);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create output directory");
        }
        return dirPath.toAbsolutePath().toString();
    }

    protected final void writeToFile(String dirPath, String fileName, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        Path filePath = Paths.get(dirPath, fileName);
        try {
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to write generated file: " + fileName);
        }
    }

    protected abstract void saveFiles(T result, String baseDirPath);
}

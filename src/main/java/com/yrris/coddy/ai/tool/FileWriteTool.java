package com.yrris.coddy.ai.tool;

import com.yrris.coddy.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileWriteTool {

    private static final Logger log = LoggerFactory.getLogger(FileWriteTool.class);

    @Tool("Write content to a file at the specified relative path within the project directory")
    public String writeFile(
            @ToolMemoryId Long appId,
            @P("The relative file path within the project, e.g. src/App.jsx") String relativePath,
            @P("The complete file content to write") String content
    ) {
        if (appId == null || appId <= 0) {
            return "Error: invalid appId";
        }
        if (relativePath == null || relativePath.isBlank()) {
            return "Error: relativePath is required";
        }
        if (relativePath.contains("..") || relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            return "Error: relativePath must be a relative path without '..'";
        }

        String baseDir = AppConstant.CODE_OUTPUT_ROOT_DIR + "/react_vite_" + appId;
        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(relativePath).normalize();

        // Security: ensure the resolved path is still within the base directory
        if (!targetPath.startsWith(basePath)) {
            return "Error: path escapes project directory";
        }

        try {
            Files.createDirectories(targetPath.getParent());
            Files.writeString(targetPath, content != null ? content : "", StandardCharsets.UTF_8);
            log.info("FileWriteTool: wrote {} for appId={}", relativePath, appId);
            return "File written: " + relativePath;
        } catch (IOException e) {
            log.error("FileWriteTool: failed to write {} for appId={}: {}", relativePath, appId, e.getMessage());
            return "Error writing file: " + e.getMessage();
        }
    }
}

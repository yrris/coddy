package com.yrris.coddy.ai.tool;

import com.yrris.coddy.constant.AppConstant;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Runs npm install + npm run build for React+Vite projects.
 */
@Service
public class ReactViteBuildService {

    private static final Logger log = LoggerFactory.getLogger(ReactViteBuildService.class);

    private static final long NPM_INSTALL_TIMEOUT_MS = 300_000; // 5 min
    private static final long NPM_BUILD_TIMEOUT_MS = 180_000;   // 3 min

    public boolean buildProject(long appId) {
        String projectDir = AppConstant.CODE_OUTPUT_ROOT_DIR + "/react_vite_" + appId;
        Path projectPath = Paths.get(projectDir);

        if (!Files.exists(projectPath.resolve("package.json"))) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "No package.json found in project directory");
        }

        log.info("Starting npm install for appId={}", appId);
        int installExit = runCommand(projectDir, NPM_INSTALL_TIMEOUT_MS, "npm", "install", "--legacy-peer-deps");
        if (installExit != 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "npm install failed with exit code: " + installExit);
        }
        log.info("npm install completed for appId={}", appId);

        log.info("Starting npm run build for appId={}", appId);
        int buildExit = runCommand(projectDir, NPM_BUILD_TIMEOUT_MS, "npm", "run", "build");
        if (buildExit != 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "npm run build failed with exit code: " + buildExit);
        }
        log.info("npm run build completed for appId={}", appId);

        Path distDir = projectPath.resolve("dist");
        if (!Files.exists(distDir) || !Files.isDirectory(distDir)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Build produced no dist directory");
        }

        return true;
    }

    private int runCommand(String workDir, long timeoutMs, String... command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(Paths.get(workDir).toFile())
                    .redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read output to prevent buffer blocking
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[npm] {}", line);
                }
            }

            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "Command timed out: " + String.join(" ", command));
            }

            return process.exitValue();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Failed to execute command: " + e.getMessage());
        }
    }
}

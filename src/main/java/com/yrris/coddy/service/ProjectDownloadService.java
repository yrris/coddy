package com.yrris.coddy.service;

import com.yrris.coddy.constant.AppConstant;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProjectDownloadService {

    private static final Set<String> IGNORED_DIRS = Set.of(
            "node_modules", "dist", "build", ".git", ".cache", ".vite"
    );

    private static final Set<String> IGNORED_FILES = Set.of(
            ".DS_Store", ".env"
    );

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log", ".tmp"
    );

    public void writeProjectZip(long appId, CodeGenTypeEnum codeGenType, OutputStream outputStream) {
        Path sourceDir = getSourceDir(appId, codeGenType);
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Project source code not found");
        }

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    String dirName = dir.getFileName().toString();
                    if (IGNORED_DIRS.contains(dirName) && !dir.equals(sourceDir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (IGNORED_FILES.contains(fileName) || isIgnoredExtension(fileName)) {
                        return FileVisitResult.CONTINUE;
                    }

                    String relativePath = sourceDir.relativize(file).toString();
                    zos.putNextEntry(new ZipEntry(relativePath));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
            zos.flush();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create ZIP: " + e.getMessage());
        }
    }

    private Path getSourceDir(long appId, CodeGenTypeEnum codeGenType) {
        String dirName = codeGenType.getValue().toLowerCase(Locale.ROOT) + "_" + appId;
        return Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, dirName);
    }

    private boolean isIgnoredExtension(String fileName) {
        for (String ext : IGNORED_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}

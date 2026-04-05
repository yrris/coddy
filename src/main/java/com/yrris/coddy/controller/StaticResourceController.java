package com.yrris.coddy.controller;

import com.yrris.coddy.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/static")
public class StaticResourceController {

    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    @GetMapping("/{previewKey}/**")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String previewKey,
            HttpServletRequest request
    ) {
        if (previewKey.contains("..") || previewKey.contains("/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            String requestUri = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
            String prefix = request.getContextPath() + "/static/" + previewKey;
            String resourcePath = requestUri.length() > prefix.length()
                    ? requestUri.substring(prefix.length())
                    : "";

            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.LOCATION, request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            if ("/".equals(resourcePath)) {
                // React projects serve from dist/index.html
                if (previewKey.startsWith("react_vite_")) {
                    resourcePath = "/dist/index.html";
                } else {
                    resourcePath = "/index.html";
                }
            }
            if (resourcePath.contains("..")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            String filePath = PREVIEW_ROOT_DIR + "/" + previewKey + resourcePath;
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                // SPA fallback for React projects: serve dist/index.html for client-side routes
                if (previewKey.startsWith("react_vite_")) {
                    File spaFallback = new File(PREVIEW_ROOT_DIR + "/" + previewKey + "/dist/index.html");
                    if (spaFallback.exists()) {
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                                .body(new FileSystemResource(spaFallback));
                    }
                }
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, getContentType(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (filePath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (filePath.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (filePath.endsWith(".png")) {
            return "image/png";
        }
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (filePath.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (filePath.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (filePath.endsWith(".jsx") || filePath.endsWith(".tsx") || filePath.endsWith(".mjs")) {
            return "application/javascript; charset=UTF-8";
        }
        if (filePath.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (filePath.endsWith(".woff")) {
            return "font/woff";
        }
        if (filePath.endsWith(".woff2")) {
            return "font/woff2";
        }
        if (filePath.endsWith(".ttf")) {
            return "font/ttf";
        }
        if (filePath.endsWith(".map")) {
            return "application/json";
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}

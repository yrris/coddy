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
@RequestMapping("/deployed")
public class DeployedResourceController {

    private static final String DEPLOY_ROOT_DIR = AppConstant.CODE_DEPLOY_ROOT_DIR;

    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveDeployedResource(
            @PathVariable String deployKey,
            HttpServletRequest request
    ) {
        if (deployKey.contains("..") || deployKey.contains("/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            String requestUri = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
            String prefix = request.getContextPath() + "/deployed/" + deployKey;
            String resourcePath = requestUri.length() > prefix.length()
                    ? requestUri.substring(prefix.length())
                    : "";

            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.LOCATION, request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            if ("/".equals(resourcePath)) {
                resourcePath = "/index.html";
            }
            if (resourcePath.contains("..")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            String filePath = DEPLOY_ROOT_DIR + "/" + deployKey + resourcePath;
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
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
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}

package com.yrris.coddy.controller;

import com.yrris.coddy.constant.AppConstant;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/app/screenshot")
public class ScreenshotController {

    @GetMapping("/image/{appId}")
    public ResponseEntity<Resource> getScreenshotImage(@PathVariable Long appId) {
        Path imagePath = Paths.get(AppConstant.SCREENSHOT_ROOT_DIR, "app_" + appId + ".png");
        Resource resource = new FileSystemResource(imagePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(resource);
    }
}

package com.yrris.coddy.controller;

import com.yrris.coddy.common.ApiResponse;
import com.yrris.coddy.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/ping")
    public ApiResponse<Map<String, String>> ping() {
        return ResultUtils.success(Map.of(
                "service", "coddy-backend",
                "status", "UP"
        ));
    }
}

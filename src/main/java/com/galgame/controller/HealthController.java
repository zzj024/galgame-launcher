package com.galgame.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查 + 基础信息接口。
 * 用于验证后端是否正常启动。
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "service", "galgame-launcher",
            "version", "0.1.0-SNAPSHOT",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}

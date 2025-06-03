package com.yancey.appupdate.controller;

import com.yancey.appupdate.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 公共控制器
 * 处理不需要认证的公共API端点
 * 
 * @author yancey
 * @version 1.0
 * @since 2025-06-03
 */
@Slf4j
@RestController
@RequestMapping("/api/public")
public class PublicController {

    /**
     * 测试连接端点 - 不需要认证
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        log.info("Ping测试连接");
        return ResponseEntity.ok(ApiResponse.success("连接正常", "pong"));
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        log.info("健康检查");
        return ResponseEntity.ok(ApiResponse.success("服务正常", "healthy"));
    }
} 
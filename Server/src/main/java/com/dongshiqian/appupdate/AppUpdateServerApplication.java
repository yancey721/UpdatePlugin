package com.dongshiqian.appupdate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 应用更新服务主启动类
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@SpringBootApplication
@EnableConfigurationProperties
public class AppUpdateServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppUpdateServerApplication.class, args);
    }

} 
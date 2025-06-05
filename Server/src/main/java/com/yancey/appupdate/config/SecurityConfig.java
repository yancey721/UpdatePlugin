package com.yancey.appupdate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 * 只对管理端API启用认证，移动端API保持开放
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties appProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // 启用CORS
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            
            // 禁用CSRF，因为我们使用的是无状态API
            .csrf().disable()
            
            // 禁用默认的登录表单
            .formLogin().disable()
            
            // 禁用HTTP Basic认证
            .httpBasic().disable()
            
            // 设置会话策略为无状态
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            .and()
            
            // 配置授权规则
            .authorizeHttpRequests()
                // CORS预检请求（OPTIONS方法）保持开放
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 公共API保持开放
                .antMatchers("/api/public/**").permitAll()
                
                // 移动端API保持开放
                .antMatchers("/api/app/**").permitAll()
                
                // H2数据库控制台保持开放（仅开发环境）
                .antMatchers("/h2-console/**").permitAll()
                
                // 静态资源保持开放
                .antMatchers("/", "/index.html", "/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                
                // 健康检查等保持开放
                .antMatchers("/actuator/**").permitAll()
                
                // 管理端API需要认证
                .antMatchers("/api/admin/**").permitAll()
                
                // 其他所有请求都允许
                .anyRequest().permitAll()
            
            .and()
            
            // 禁用frame options，允许H2控制台在iframe中显示
            .headers().frameOptions().disable()
            
            .and()
            
            // 添加API密钥认证过滤器
            .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的域名
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:5173", // Vite默认端口
            "http://127.0.0.1:5173",
            "http://192.168.210.22:3000", // 局域网访问前端
            "http://192.168.210.22:5173"  // 局域网访问前端Vite端口
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送认证信息（如cookies, authorization headers）
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        return new ApiKeyAuthenticationFilter(appProperties);
    }
} 
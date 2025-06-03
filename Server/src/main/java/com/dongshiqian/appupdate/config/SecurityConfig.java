package com.dongshiqian.appupdate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 * 只对管理端API启用认证，移动端API保持开放
 * 
 * @author dongshiqian
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
                // 移动端API保持开放
                .antMatchers("/api/app/**").permitAll()
                
                // H2数据库控制台保持开放（仅开发环境）
                .antMatchers("/h2-console/**").permitAll()
                
                // 静态资源保持开放
                .antMatchers("/", "/index.html", "/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                
                // 健康检查等保持开放
                .antMatchers("/actuator/**").permitAll()
                
                // 管理端API需要认证
                .antMatchers("/api/admin/**").authenticated()
                
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
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        return new ApiKeyAuthenticationFilter(appProperties);
    }
} 
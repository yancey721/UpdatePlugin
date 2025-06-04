package com.yancey.appupdate.config;

import com.alibaba.fastjson.JSON;
import com.yancey.appupdate.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * API密钥认证过滤器
 * 用于验证管理端API的访问权限
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String ADMIN_API_PATH_PREFIX = "/api/admin";
    
    private final AppProperties appProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("API密钥认证过滤器处理请求: {} {}", method, requestURI);
        
        // 跳过CORS预检请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("CORS预检请求，跳过认证: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 只对管理端API进行认证检查
        if (!requestURI.startsWith(ADMIN_API_PATH_PREFIX)) {
            log.debug("非管理端API请求，跳过认证: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取请求头中的API密钥
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        // 检查API密钥是否存在
        if (!StringUtils.hasText(apiKey)) {
            log.warn("管理端API请求缺少API密钥: {} {}", method, requestURI);
            sendErrorResponse(response, 401, "缺少API密钥，请在请求头中添加X-API-KEY");
            return;
        }
        
        // 验证API密钥是否正确
        String configuredApiKey = appProperties.getAdmin().getApiKey();
        if (!apiKey.equals(configuredApiKey)) {
            log.warn("管理端API请求使用了无效的API密钥: {} {}, apiKey: {}", method, requestURI, apiKey);
            sendErrorResponse(response, 403, "无效的API密钥");
            return;
        }
        
        log.debug("API密钥验证成功: {} {}", method, requestURI);
        
        // 设置认证信息到Spring Security上下文
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                "admin", 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 认证通过，继续执行
        filterChain.doFilter(request, response);
    }

    /**
     * 发送错误响应
     * 
     * @param response HTTP响应
     * @param status HTTP状态码
     * @param message 错误消息
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(status);
        apiResponse.setMessage(message);
        apiResponse.setData(null);
        apiResponse.setTimestamp(System.currentTimeMillis());
        
        String jsonResponse = JSON.toJSONString(apiResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
} 
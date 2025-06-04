package com.yancey.appupdate.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 创建应用请求DTO
 *
 * @author yancey
 * @version 1.0
 * @since 2024-06-03
 */
@Data
public class CreateAppRequest {

    /**
     * 包名（作为应用唯一标识）
     */
    @NotBlank(message = "包名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*(\\.([a-zA-Z][a-zA-Z0-9_]*))*$", 
             message = "包名格式不正确，应符合Java包名规范")
    @Size(max = 100, message = "包名长度不能超过100个字符")
    private String packageName;

    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 200, message = "应用名称长度不能超过200个字符")
    private String appName;

    /**
     * 应用描述
     */
    @Size(max = 1000, message = "应用描述长度不能超过1000个字符")
    private String appDescription;

    /**
     * 是否强制更新
     */
    private Boolean forceUpdate = false;
} 
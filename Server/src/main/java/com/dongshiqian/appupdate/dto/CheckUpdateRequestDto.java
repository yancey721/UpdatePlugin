package com.dongshiqian.appupdate.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 移动端检查更新请求DTO
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class CheckUpdateRequestDto {

    /**
     * 应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;

    /**
     * 当前版本号
     */
    @NotNull(message = "当前版本号不能为空")
    private Integer currentVersionCode;
} 
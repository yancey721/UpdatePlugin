package com.dongshiqian.appupdate.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 更新版本状态请求DTO
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class UpdateStatusRequestDto {

    /**
     * 版本状态
     * 0: 禁用
     * 1: 启用
     * 2: 测试
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值必须在0-2之间")
    @Max(value = 2, message = "状态值必须在0-2之间")
    private Integer status;
} 
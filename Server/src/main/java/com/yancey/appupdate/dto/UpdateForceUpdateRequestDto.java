package com.yancey.appupdate.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新应用强制更新设置请求DTO
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-06-04
 */
@Data
public class UpdateForceUpdateRequestDto {

    /**
     * 是否强制更新
     */
    @NotNull(message = "强制更新设置不能为空")
    private Boolean forceUpdate;
} 
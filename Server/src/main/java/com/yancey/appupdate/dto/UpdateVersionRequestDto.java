package com.yancey.appupdate.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改版本信息请求DTO
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class UpdateVersionRequestDto {

    /**
     * 更新说明
     */
    @Size(max = 1000, message = "更新说明长度不能超过1000字符")
    private String updateDescription;

    /**
     * 是否强制更新
     */
    private Boolean forceUpdate;

    /**
     * 版本状态
     */
    private Integer status;
} 
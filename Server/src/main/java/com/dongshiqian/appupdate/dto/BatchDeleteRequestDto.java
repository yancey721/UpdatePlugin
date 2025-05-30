package com.dongshiqian.appupdate.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量删除版本请求DTO
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class BatchDeleteRequestDto {

    /**
     * 版本ID列表
     */
    @NotEmpty(message = "版本ID列表不能为空")
    private List<Long> versionIds;

    /**
     * 是否强制删除（删除文件）
     */
    private Boolean forceDelete = false;
} 
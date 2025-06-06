package com.yancey.sdk.callback

import java.io.File

/**
 * 下载进度回调接口
 */
interface DownloadCallback {
    
    /**
     * 下载开始
     */
    fun onDownloadStart()
    
    /**
     * 下载进度更新
     * @param progress 下载进度 (0-100)
     * @param currentBytes 已下载字节数
     * @param totalBytes 总字节数
     */
    fun onDownloadProgress(progress: Int, currentBytes: Long, totalBytes: Long)
    
    /**
     * 下载完成
     * @param file 下载的APK文件
     */
    fun onDownloadComplete(file: File)
    
    /**
     * 下载失败
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    fun onDownloadError(errorCode: Int, errorMessage: String)
    
    /**
     * 下载取消
     */
    fun onDownloadCancel()
} 
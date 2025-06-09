package com.yancey.sdk.callback

import java.io.File

/**
 * APK安装回调接口
 */
interface InstallCallback {
    
    /**
     * 安装启动成功
     * @param apkFile APK文件
     */
    fun onInstallStart(apkFile: File)
    
    /**
     * 需要安装权限，询问用户是否前往设置页面
     * @param onUserConfirm 用户确认前往设置的回调
     * @param onUserCancel 用户取消的回调
     */
    fun onInstallPermissionRequired(onUserConfirm: () -> Unit, onUserCancel: () -> Unit)
    
    /**
     * 安装失败
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    fun onInstallError(errorCode: Int, errorMessage: String)
} 
package com.yancey.sdk.callback

import com.yancey.sdk.data.UpdateInfo

/**
 * UI操作回调接口
 */
interface UICallback {
    
    /**
     * 用户选择立即更新
     * @param updateInfo 更新信息
     */
    fun onUserConfirmUpdate(updateInfo: UpdateInfo)
    
    /**
     * 用户选择稍后提醒（仅在非强制更新时可用）
     * @param updateInfo 更新信息  
     */
    fun onUserCancelUpdate(updateInfo: UpdateInfo)
    
    /**
     * 对话框被系统关闭（如按返回键等）
     * @param updateInfo 更新信息
     */
    fun onDialogDismissed(updateInfo: UpdateInfo)
} 
package com.yancey.sdk.callback

import com.yancey.sdk.data.UpdateInfo

/**
 * 更新检查回调接口
 */
interface UpdateCallback {
    
    /**
     * 检查更新成功
     * @param updateInfo 更新信息，如果hasUpdate为false则表示无更新
     */
    fun onUpdateCheckSuccess(updateInfo: UpdateInfo)
    
    /**
     * 检查更新失败
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    fun onError(errorCode: Int, errorMessage: String)
} 
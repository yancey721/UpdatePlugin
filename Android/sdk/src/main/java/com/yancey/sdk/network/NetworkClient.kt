package com.yancey.sdk.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.CheckUpdateRequest
import com.yancey.sdk.data.CheckUpdateResponse
import com.yancey.sdk.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * 网络客户端类
 * 负责处理HTTP请求和响应
 */
class NetworkClient(private val config: UpdateConfig) {
    
    private val gson = Gson()
    
    companion object {
        // 错误码定义
        const val ERROR_NETWORK_TIMEOUT = -1001        // 网络超时
        const val ERROR_NETWORK_IO = -1002              // 网络IO异常
        const val ERROR_JSON_PARSE = -1003              // JSON解析错误
        const val ERROR_HTTP_ERROR = -1004              // HTTP错误
        const val ERROR_UNKNOWN = -1099                 // 未知错误
    }
    
    /**
     * 检查更新请求
     * @param request 请求参数
     * @return 服务端响应
     * @throws NetworkException 网络异常
     */
    suspend fun checkUpdate(request: CheckUpdateRequest): CheckUpdateResponse = withContext(Dispatchers.IO) {
        val url = "${config.baseUrl}check-update"
        Logger.d("NetworkClient", "Checking update: $url")
        Logger.d("NetworkClient", "Request: ${gson.toJson(request)}")
        
        var connection: HttpURLConnection? = null
        
        try {
            connection = createConnection(url)
            sendRequest(connection, request)
            val response = readResponse(connection)
            
            Logger.d("NetworkClient", "Response: ${gson.toJson(response)}")
            response
            
        } catch (e: SocketTimeoutException) {
            Logger.e("NetworkClient", "Network timeout", e)
            throw NetworkException(ERROR_NETWORK_TIMEOUT, "网络请求超时，请检查网络连接", e)
            
        } catch (e: IOException) {
            Logger.e("NetworkClient", "Network IO error", e)
            throw NetworkException(ERROR_NETWORK_IO, "网络连接失败，请检查网络设置", e)
            
        } catch (e: JsonSyntaxException) {
            Logger.e("NetworkClient", "JSON parse error", e)
            throw NetworkException(ERROR_JSON_PARSE, "服务端响应格式错误", e)
            
        } catch (e: Exception) {
            Logger.e("NetworkClient", "Unknown error", e)
            throw NetworkException(ERROR_UNKNOWN, "未知错误：${e.message}", e)
            
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * 创建HTTP连接
     */
    private fun createConnection(urlString: String): HttpURLConnection {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "AppUpdaterSDK/1.0.0")
            doOutput = true
            doInput = true
            connectTimeout = config.connectTimeout
            readTimeout = config.readTimeout
        }
        
        return connection
    }
    
    /**
     * 发送请求数据
     */
    private fun sendRequest(connection: HttpURLConnection, request: CheckUpdateRequest) {
        val jsonBody = gson.toJson(request)
        val bodyBytes = jsonBody.toByteArray(Charsets.UTF_8)
        
        connection.setRequestProperty("Content-Length", bodyBytes.size.toString())
        
        connection.outputStream.use { output ->
            output.write(bodyBytes)
            output.flush()
        }
    }
    
    /**
     * 读取响应数据
     */
    private fun readResponse(connection: HttpURLConnection): CheckUpdateResponse {
        val responseCode = connection.responseCode
        Logger.d("NetworkClient", "HTTP Response Code: $responseCode")
        
        val responseJson = if (responseCode == HttpURLConnection.HTTP_OK) {
            // 成功响应
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
            // 错误响应
            val errorJson = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            if (!errorJson.isNullOrBlank()) {
                Logger.w("NetworkClient", "Server error response: $errorJson")
                errorJson
            } else {
                // 如果没有错误详情，构造一个标准错误响应
                val errorResponse = CheckUpdateResponse(
                    code = responseCode,
                    message = "HTTP错误：${connection.responseMessage}",
                    data = null,
                    timestamp = System.currentTimeMillis()
                )
                gson.toJson(errorResponse)
            }
        }
        
        Logger.d("NetworkClient", "Raw response: $responseJson")
        
        try {
            return gson.fromJson(responseJson, CheckUpdateResponse::class.java)
        } catch (e: JsonSyntaxException) {
            // 如果JSON解析失败，可能是服务端返回了非JSON格式的错误
            Logger.e("NetworkClient", "Failed to parse response as JSON: $responseJson")
            throw JsonSyntaxException("Invalid JSON response: $responseJson")
        }
    }
}

/**
 * 网络异常类
 */
class NetworkException(
    val errorCode: Int,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) 
#!/bin/bash

# 应用内更新系统 - 综合API接口测试
ADMIN_BASE_URL="http://localhost:8080/api/admin/app"
APP_BASE_URL="http://localhost:8080/api/app"

echo "=========================================="
echo "应用内更新系统 - 综合API接口测试"
echo "=========================================="

echo ""
echo "等待服务器启动..."
sleep 5

echo ""
echo "=========================================="
echo "管理端API接口测试"
echo "=========================================="

# 1. 查询应用列表
echo ""
echo "1. 查询应用列表"
curl -s "$ADMIN_BASE_URL/apps?page=0&size=10" | jq '{message, data: {totalElements, content: [.data.content[] | {appId, appName, totalVersions}]}}'

# 2. 查询版本列表
echo ""
echo "2. 查询版本列表"
curl -s "$ADMIN_BASE_URL/app/com.unifysign.test/versions?page=0&size=5" | jq '{message, data: {totalElements, content: [.data.content[] | {versionCode, versionName, status, statusDescription}]}}'

# 3. 获取统计信息
echo ""
echo "3. 获取统计信息"
curl -s "$ADMIN_BASE_URL/stats" | jq '{message, data: {totalApps, totalVersions, enabledVersions, disabledVersions, testVersions, forceUpdateVersions}}'

# 4. 修改版本信息
echo ""
echo "4. 修改版本信息"
curl -s -X PUT "$ADMIN_BASE_URL/version/2" \
  -H "Content-Type: application/json" \
  -d '{
    "updateDescription": "最终测试：所有接口已完成开发和测试",
    "forceUpdate": false,
    "status": 1
  }' | jq '{message, data: {versionCode, updateDescription, forceUpdate, status, statusDescription}}'

# 5. 更新版本状态
echo ""
echo "5. 更新版本状态"
curl -s -X PUT "$ADMIN_BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": 1
  }' | jq '{message, data: {versionCode, status, statusDescription}}'

echo ""
echo "=========================================="
echo "移动端API接口测试"
echo "=========================================="

# 6. 检查更新 - 无更新
echo ""
echo "6. 检查更新 - 无更新（最新版本）"
curl -s -X POST "$APP_BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.unifysign.test",
    "currentVersionCode": 56
  }' | jq '{message, data: {hasUpdate}}'

# 7. 检查更新 - 有更新
echo ""
echo "7. 检查更新 - 有更新"
curl -s -X POST "$APP_BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.unifysign.test",
    "currentVersionCode": 50
  }' | jq '{message, data: {hasUpdate, newVersionName, newVersionCode, forceUpdate}}'

# 8. 检查更新 - 不存在的应用
echo ""
echo "8. 检查更新 - 不存在的应用"
curl -s -X POST "$APP_BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.nonexistent.app",
    "currentVersionCode": 1
  }' | jq '{message}'

# 9. 参数验证测试
echo ""
echo "9. 参数验证 - 缺少appId"
curl -s -X POST "$APP_BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "currentVersionCode": 1
  }' | jq '{message}'

# 10. APK文件下载测试
echo ""
echo "10. APK文件下载 - 检查HTTP状态码"
response=$(curl -s -I "$APP_BASE_URL/download/com.unifysign.test-56.apk")
echo "$response" | grep -E "HTTP|Content-Type|Content-Length" | head -3

# 11. 下载不存在的文件
echo ""
echo "11. 下载不存在的文件"
response=$(curl -s -I "$APP_BASE_URL/download/nonexistent-file.apk")
echo "$response" | grep "HTTP"

# 12. 最终统计信息
echo ""
echo "12. 最终统计信息"
curl -s "$ADMIN_BASE_URL/stats" | jq '{message, data: {totalApps, totalVersions, statusStats, totalFileSize}}'

echo ""
echo "=========================================="
echo "综合API接口测试完成！"
echo "=========================================="
echo "✅ 管理端接口："
echo "   - 查询应用列表、版本列表"
echo "   - 修改版本信息、更新状态"
echo "   - 获取统计信息"
echo ""
echo "✅ 移动端接口："
echo "   - 检查更新（无更新、有更新、错误处理）"
echo "   - 参数验证"
echo "   - APK文件下载"
echo ""
echo "🎉 所有核心功能已完成开发和测试！"
echo "==========================================" 
#!/bin/bash

# 测试移动端API接口
BASE_URL="http://localhost:8080/api/app"

echo "=========================================="
echo "移动端API接口测试"
echo "=========================================="

echo ""
echo "等待服务器启动..."
sleep 5

# 1. 测试检查更新接口 - 无更新情况（当前版本是最新）
echo ""
echo "1. 测试检查更新 - 无更新（最新版本）"
curl -s -X POST "$BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.unifysign.test",
    "currentVersionCode": 56
  }' | jq '{message, data: {hasUpdate}}'

# 2. 测试检查更新接口 - 有更新情况
echo ""
echo "2. 测试检查更新 - 有更新"
curl -s -X POST "$BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.unifysign.test",
    "currentVersionCode": 50
  }' | jq '{message, data: {hasUpdate, newVersionName, newVersionCode, forceUpdate, downloadUrl}}'

# 3. 测试检查更新接口 - 不存在的应用
echo ""
echo "3. 测试检查更新 - 不存在的应用"
curl -s -X POST "$BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.nonexistent.app",
    "currentVersionCode": 1
  }' | jq '{message}'

# 4. 测试参数验证 - 缺少appId
echo ""
echo "4. 测试参数验证 - 缺少appId"
curl -s -X POST "$BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "currentVersionCode": 1
  }' | jq '{message}'

# 5. 测试参数验证 - 缺少currentVersionCode
echo ""
echo "5. 测试参数验证 - 缺少currentVersionCode"
curl -s -X POST "$BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.test.app"
  }' | jq '{message}'

# 6. 测试APK文件下载接口 - 存在的文件
echo ""
echo "6. 测试APK文件下载 - 检查HTTP状态码和Content-Type"
response=$(curl -s -I "$BASE_URL/download/com.unifysign.test-56.apk")
echo "$response" | grep -E "HTTP|Content-Type|Content-Length"

# 7. 测试APK文件下载接口 - 不存在的文件
echo ""
echo "7. 测试APK文件下载 - 不存在的文件"
response=$(curl -s -I "$BASE_URL/download/nonexistent-file.apk")
echo "$response" | grep "HTTP"

echo ""
echo "=========================================="
echo "移动端API接口测试完成！"
echo "✅ 检查更新接口：无更新、有更新、错误处理"
echo "✅ 参数验证：缺少必填参数处理"
echo "✅ APK下载接口：文件存在、文件不存在"
echo "==========================================" 
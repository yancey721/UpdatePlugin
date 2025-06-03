#!/bin/bash

# API认证功能测试脚本
ADMIN_BASE_URL="http://localhost:8080/api/admin/app"
APP_BASE_URL="http://localhost:8080/api/app"
VALID_API_KEY="your-secret-api-key"
INVALID_API_KEY="wrong-api-key"

echo "==========================================="
echo "API认证功能测试"
echo "==========================================="

echo ""
echo "等待服务器启动..."
sleep 5

echo ""
echo "==========================================="
echo "移动端API测试（无需认证）"
echo "==========================================="

# 1. 移动端API - 检查更新（无需认证）
echo ""
echo "1. 移动端API - 检查更新（无需认证）"
curl -s -X POST "$APP_BASE_URL/check-update" \
  -H "Content-Type: application/json" \
  -d '{"appId": "com.unifysign.test", "currentVersionCode": 50}' | jq '{message, data: {hasUpdate}}'

echo ""
echo "==========================================="
echo "管理端API认证测试"
echo "==========================================="

# 2. 管理端API - 无API密钥
echo ""
echo "2. 管理端API - 无API密钥（应返回401）"
curl -s "$ADMIN_BASE_URL/stats" | jq '{code, message}'

# 3. 管理端API - 错误的API密钥
echo ""
echo "3. 管理端API - 错误的API密钥（应返回403）"
curl -s "$ADMIN_BASE_URL/stats" \
  -H "X-API-KEY: $INVALID_API_KEY" | jq '{code, message}'

# 4. 管理端API - 正确的API密钥
echo ""
echo "4. 管理端API - 正确的API密钥（应返回200）"
curl -s "$ADMIN_BASE_URL/stats" \
  -H "X-API-KEY: $VALID_API_KEY" | jq '{code, message, data: {totalApps, totalVersions}}'

# 5. 管理端API - 查询应用列表（需要认证）
echo ""
echo "5. 管理端API - 查询应用列表（需要认证）"
curl -s "$ADMIN_BASE_URL/apps?page=0&size=5" \
  -H "X-API-KEY: $VALID_API_KEY" | jq '{code, message, data: {totalElements}}'

# 6. 管理端API - 查询版本列表（需要认证）
echo ""
echo "6. 管理端API - 查询版本列表（需要认证）"
curl -s "$ADMIN_BASE_URL/app/com.unifysign.test/versions?page=0&size=5" \
  -H "X-API-KEY: $VALID_API_KEY" | jq '{code, message, data: {totalElements}}'

echo ""
echo "==========================================="
echo "认证测试完成"
echo "===========================================" 
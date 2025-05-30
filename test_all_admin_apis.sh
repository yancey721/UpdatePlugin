#!/bin/bash

# 综合测试所有管理端API接口
# 测试服务器地址
BASE_URL="http://localhost:8080/api/admin/app"

echo "=========================================="
echo "开始测试所有管理端API接口"
echo "=========================================="

# 1. 测试查询应用列表
echo ""
echo "1. 测试查询应用列表"
echo "GET $BASE_URL/apps"
curl -s -X GET "$BASE_URL/apps" | jq '.'

# 2. 测试查询应用版本列表
echo ""
echo "2. 测试查询应用版本列表"
echo "GET $BASE_URL/app/com.unifysign.android/versions"
curl -s -X GET "$BASE_URL/app/com.unifysign.android/versions" | jq '.'

# 3. 测试获取版本统计信息
echo ""
echo "3. 测试获取版本统计信息"
echo "GET $BASE_URL/stats"
curl -s -X GET "$BASE_URL/stats" | jq '.'

# 4. 测试修改版本信息
echo ""
echo "4. 测试修改版本信息"
echo "PUT $BASE_URL/version/1"
curl -s -X PUT "$BASE_URL/version/1" \
  -H "Content-Type: application/json" \
  -d '{
    "updateDescription": "修改后的更新说明：修复了一些已知问题，提升了应用稳定性",
    "forceUpdate": true,
    "status": 1
  }' | jq '.'

# 5. 测试更新版本状态
echo ""
echo "5. 测试更新版本状态为测试状态"
echo "PUT $BASE_URL/version/1/status"
curl -s -X PUT "$BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": 2
  }' | jq '.'

# 6. 再次查询版本信息确认修改
echo ""
echo "6. 再次查询版本信息确认修改"
echo "GET $BASE_URL/app/com.unifysign.android/versions"
curl -s -X GET "$BASE_URL/app/com.unifysign.android/versions" | jq '.'

# 7. 测试删除版本（如果有多个版本的话）
echo ""
echo "7. 测试删除版本（先检查是否有多个版本）"
VERSION_COUNT=$(curl -s -X GET "$BASE_URL/app/com.unifysign.android/versions" | jq '.data.totalElements')
echo "当前版本总数: $VERSION_COUNT"

if [ "$VERSION_COUNT" -gt 1 ]; then
    echo "有多个版本，测试删除最新版本"
    LATEST_VERSION_ID=$(curl -s -X GET "$BASE_URL/app/com.unifysign.android/versions" | jq '.data.content[0].id')
    echo "DELETE $BASE_URL/version/$LATEST_VERSION_ID"
    curl -s -X DELETE "$BASE_URL/version/$LATEST_VERSION_ID?forceDelete=false" | jq '.'
else
    echo "只有一个版本，跳过删除测试"
fi

# 8. 测试批量删除（创建测试数据）
echo ""
echo "8. 测试批量删除功能"
echo "DELETE $BASE_URL/versions"
curl -s -X DELETE "$BASE_URL/versions" \
  -H "Content-Type: application/json" \
  -d '{
    "versionIds": [999, 1000],
    "forceDelete": false
  }' | jq '.'

# 9. 测试错误情况 - 修改不存在的版本
echo ""
echo "9. 测试错误情况 - 修改不存在的版本"
echo "PUT $BASE_URL/version/999"
curl -s -X PUT "$BASE_URL/version/999" \
  -H "Content-Type: application/json" \
  -d '{
    "updateDescription": "测试错误情况"
  }' | jq '.'

# 10. 测试错误情况 - 删除不存在的版本
echo ""
echo "10. 测试错误情况 - 删除不存在的版本"
echo "DELETE $BASE_URL/version/999"
curl -s -X DELETE "$BASE_URL/version/999" | jq '.'

# 11. 测试错误情况 - 无效状态值
echo ""
echo "11. 测试错误情况 - 无效状态值"
echo "PUT $BASE_URL/version/1/status"
curl -s -X PUT "$BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": 5
  }' | jq '.'

# 12. 最终统计信息
echo ""
echo "12. 最终统计信息"
echo "GET $BASE_URL/stats"
curl -s -X GET "$BASE_URL/stats" | jq '.'

echo ""
echo "=========================================="
echo "所有API接口测试完成"
echo "==========================================" 
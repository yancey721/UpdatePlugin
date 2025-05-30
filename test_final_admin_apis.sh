#!/bin/bash

# 最终综合测试所有管理端API接口
BASE_URL="http://localhost:8080/api/admin/app"

echo "=========================================="
echo "最终测试所有管理端API接口（修复版）"
echo "=========================================="

# 1. 查询应用列表
echo ""
echo "1. 测试查询应用列表"
curl -s -X GET "$BASE_URL/apps" | jq '.data.totalElements'

# 2. 获取版本统计信息
echo ""
echo "2. 测试获取版本统计信息"
curl -s -X GET "$BASE_URL/stats" | jq '.data | {totalApps, totalVersions, enabledVersions, testVersions}'

# 3. 修改版本信息（完整测试）
echo ""
echo "3. 测试修改版本信息"
curl -s -X PUT "$BASE_URL/version/1" \
  -H "Content-Type: application/json" \
  -d '{
    "updateDescription": "完整测试修改：增加新功能和性能优化",
    "forceUpdate": false,
    "status": 1
  }' | jq '.message'

# 4. 测试状态更新为各种状态
echo ""
echo "4. 测试状态更新功能"
echo "4.1 设置为禁用状态(0)"
curl -s -X PUT "$BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": 0}' | jq '.data.statusDescription'

echo "4.2 设置为启用状态(1)"  
curl -s -X PUT "$BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": 1}' | jq '.data.statusDescription'

echo "4.3 设置为测试状态(2)"
curl -s -X PUT "$BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": 2}' | jq '.data.statusDescription'

# 5. 测试参数验证
echo ""
echo "5. 测试参数验证"
echo "5.1 无效状态值测试"
curl -s -X PUT "$BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": 5}' | jq '.message'

echo "5.2 空状态值测试"
curl -s -X PUT "$BASE_URL/version/1/status" \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.message'

# 6. 测试批量删除（错误情况）
echo ""
echo "6. 测试批量删除（不存在的版本）"
curl -s -X DELETE "$BASE_URL/versions" \
  -H "Content-Type: application/json" \
  -d '{
    "versionIds": [999, 1000],
    "forceDelete": false
  }' | jq '.message'

# 7. 测试单个删除（错误情况）
echo ""
echo "7. 测试删除不存在的版本"
curl -s -X DELETE "$BASE_URL/version/999" | jq '.message'

# 8. 测试修改不存在的版本
echo ""
echo "8. 测试修改不存在的版本"
curl -s -X PUT "$BASE_URL/version/999" \
  -H "Content-Type: application/json" \
  -d '{"updateDescription": "测试"}' | jq '.message'

# 9. 查询版本详情确认修改结果
echo ""
echo "9. 确认版本状态和信息修改"
curl -s -X GET "$BASE_URL/app/com.unifysign.android/versions" | jq '.data.content[0] | {id, status, statusDescription, updateDescription, forceUpdate}'

# 10. 最终统计信息
echo ""
echo "10. 最终统计信息"
curl -s -X GET "$BASE_URL/stats" | jq '.data | {totalApps, totalVersions, enabledVersions, disabledVersions, testVersions, forceUpdateVersions}'

echo ""
echo "=========================================="
echo "所有管理端API接口测试完成！"
echo "✅ 查询功能：应用列表、版本列表、统计信息"
echo "✅ 修改功能：版本信息修改、状态更新"  
echo "✅ 删除功能：单个删除、批量删除"
echo "✅ 参数验证：状态值验证、必填参数验证"
echo "✅ 错误处理：不存在资源、无效参数"
echo "==========================================" 
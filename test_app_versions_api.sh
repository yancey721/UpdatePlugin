#!/bin/bash

# 应用版本列表查询API测试脚本
# 测试新开发的 GET /api/admin/app/{appId}/versions 接口

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
BASE_URL="http://localhost:8080"
API_KEY="your-secret-api-key"

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}    应用版本列表查询API测试脚本${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""

# 检查服务器是否运行
echo -e "${YELLOW}检查服务器状态...${NC}"
if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ 服务器未运行，请先启动服务器${NC}"
    echo "启动命令: cd Server && mvn spring-boot:run"
    exit 1
fi
echo -e "${GREEN}✅ 服务器运行正常${NC}"
echo ""

# 测试函数
test_versions_api() {
    local test_name="$1"
    local app_id="$2"
    local url_params="$3"
    local expected_total="$4"
    
    url="$BASE_URL/api/admin/app/app/$app_id/versions$url_params"
    
    echo -e "${YELLOW}测试: ${test_name}${NC}"
    echo "URL: $url"
    echo "应用ID: $app_id"
    
    response=$(curl -s -X GET "$url" \
        -H "X-API-KEY: $API_KEY" \
        -H "Content-Type: application/json")
    
    # 检查HTTP状态
    http_code=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$url" \
        -H "X-API-KEY: $API_KEY" \
        -H "Content-Type: application/json")
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}✅ HTTP状态码: $http_code${NC}"
        
        # 解析响应
        total_elements=$(echo "$response" | jq -r '.data.totalElements // "null"')
        page_size=$(echo "$response" | jq -r '.data.size // "null"')
        current_page=$(echo "$response" | jq -r '.data.number // "null"')
        content_count=$(echo "$response" | jq -r '.data.content | length')
        
        echo "总记录数: $total_elements"
        echo "当前页: $current_page, 页大小: $page_size"
        echo "当前页记录数: $content_count"
        
        # 显示版本信息
        if [ "$content_count" -gt 0 ]; then
            echo "版本列表:"
            echo "$response" | jq -r '.data.content[] | "  - v\(.versionName) (code:\(.versionCode)) - \(.updateDescription) - 状态: \(.statusDescription) - 强制更新: \(.forceUpdate)"'
            
            # 显示详细信息
            echo "详细信息:"
            echo "$response" | jq -r '.data.content[] | "  ID: \(.id), MD5: \(.md5), 文件大小: \(.fileSize) bytes, 创建时间: \(.createTime)"'
        else
            echo "  无版本数据"
        fi
        
        # 验证预期结果
        if [ -n "$expected_total" ] && [ "$total_elements" != "$expected_total" ]; then
            echo -e "${YELLOW}⚠️  预期总记录数: $expected_total, 实际: $total_elements${NC}"
        fi
        
    elif [ "$http_code" = "400" ]; then
        echo -e "${GREEN}✅ HTTP状态码: $http_code (预期的错误响应)${NC}"
        error_message=$(echo "$response" | jq -r '.message // "无错误信息"')
        echo "错误信息: $error_message"
    else
        echo -e "${RED}❌ HTTP状态码: $http_code${NC}"
        echo "响应内容: $response"
    fi
    
    echo ""
}

# 测试用例1: 查询存在的应用版本列表
test_versions_api "查询com.unifysign.android的版本列表" \
    "com.unifysign.android" \
    "" \
    "1"

# 测试用例2: 查询另一个存在的应用版本列表
test_versions_api "查询com.unifysign.test的版本列表" \
    "com.unifysign.test" \
    "" \
    "1"

# 测试用例3: 分页测试（每页1条）
test_versions_api "分页测试 - 每页1条记录" \
    "com.unifysign.android" \
    "?page=0&size=1"

# 测试用例4: 分页测试（第2页，但数据不足）
test_versions_api "分页测试 - 第2页（应该为空）" \
    "com.unifysign.android" \
    "?page=1&size=1"

# 测试用例5: 大页面测试
test_versions_api "大页面测试 - 每页50条" \
    "com.unifysign.android" \
    "?page=0&size=50"

# 测试用例6: 排序测试 - 按创建时间倒序
test_versions_api "排序测试 - 按创建时间倒序" \
    "com.unifysign.android" \
    "?sort=createTime,desc"

# 测试用例7: 排序测试 - 按版本号升序
test_versions_api "排序测试 - 按版本号升序" \
    "com.unifysign.android" \
    "?sort=versionCode,asc"

# 测试用例8: 查询不存在的应用ID
test_versions_api "查询不存在的应用ID" \
    "nonexistent.app" \
    "" \
    "error"

# 测试用例9: 查询空的应用ID
test_versions_api "查询空的应用ID" \
    "" \
    ""

# 测试用例10: 无API密钥测试
echo -e "${YELLOW}测试: 无API密钥访问版本列表${NC}"
response=$(curl -s -X GET "$BASE_URL/api/admin/app/app/com.unifysign.android/versions" \
    -H "Content-Type: application/json")
http_code=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/admin/app/app/com.unifysign.android/versions" \
    -H "Content-Type: application/json")

if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
    echo -e "${GREEN}✅ 正确拒绝无API密钥请求 (HTTP $http_code)${NC}"
else
    echo -e "${RED}❌ 应该拒绝无API密钥请求 (HTTP $http_code)${NC}"
fi
echo ""

# 测试用例11: 错误API密钥测试
echo -e "${YELLOW}测试: 错误API密钥${NC}"
response=$(curl -s -X GET "$BASE_URL/api/admin/app/app/com.unifysign.android/versions" \
    -H "X-API-KEY: wrong-api-key" \
    -H "Content-Type: application/json")
http_code=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/admin/app/app/com.unifysign.android/versions" \
    -H "X-API-KEY: wrong-api-key" \
    -H "Content-Type: application/json")

if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
    echo -e "${GREEN}✅ 正确拒绝错误API密钥请求 (HTTP $http_code)${NC}"
else
    echo -e "${RED}❌ 应该拒绝错误API密钥请求 (HTTP $http_code)${NC}"
fi
echo ""

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}    测试完成${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""
echo -e "${GREEN}✅ 查询应用版本列表API功能测试完成${NC}"
echo "新功能包括："
echo "  - 根据应用ID查询版本列表"
echo "  - 分页查询支持"
echo "  - 多种排序方式支持"
echo "  - 返回完整的版本信息"
echo "  - 适当的错误处理和验证"
echo "  - 安全认证机制" 
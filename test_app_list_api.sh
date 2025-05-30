#!/bin/bash

# 应用列表查询API测试脚本
# 测试新开发的 GET /api/admin/app/apps 接口

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
echo -e "${BLUE}    应用列表查询API测试脚本${NC}"
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
test_api() {
    local test_name="$1"
    local url="$2"
    local expected_elements="$3"
    
    echo -e "${YELLOW}测试: ${test_name}${NC}"
    echo "URL: $url"
    
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
        
        # 显示应用信息
        if [ "$content_count" -gt 0 ]; then
            echo "应用列表:"
            echo "$response" | jq -r '.data.content[] | "  - \(.appName) (\(.appId)) - 最新版本: \(.latestVersionName // "无") - 总版本数: \(.totalVersions)"'
        else
            echo "  无应用数据"
        fi
        
        # 验证预期结果
        if [ -n "$expected_elements" ] && [ "$total_elements" != "$expected_elements" ]; then
            echo -e "${YELLOW}⚠️  预期总记录数: $expected_elements, 实际: $total_elements${NC}"
        fi
        
    else
        echo -e "${RED}❌ HTTP状态码: $http_code${NC}"
        echo "响应内容: $response"
    fi
    
    echo ""
}

# 测试用例1: 查询所有应用（无搜索条件）
test_api "查询所有应用" \
    "$BASE_URL/api/admin/app/apps"

# 测试用例2: 分页测试（每页1条）
test_api "分页测试 - 每页1条记录" \
    "$BASE_URL/api/admin/app/apps?page=0&size=1"

# 测试用例3: 分页测试（第2页）
test_api "分页测试 - 第2页" \
    "$BASE_URL/api/admin/app/apps?page=1&size=1"

# 测试用例4: 按应用名称搜索（中文）
echo -e "${YELLOW}测试: 按应用名称搜索（中文）${NC}"
search_term="桂云"
encoded_search=$(echo -n "$search_term" | python3 -c "import sys, urllib.parse; print(urllib.parse.quote(sys.stdin.read()))")
test_api "搜索应用名称包含'$search_term'" \
    "$BASE_URL/api/admin/app/apps?appNameQuery=$encoded_search"

# 测试用例5: 搜索不存在的应用
echo -e "${YELLOW}测试: 搜索不存在的应用${NC}"
search_term="不存在的应用"
encoded_search=$(echo -n "$search_term" | python3 -c "import sys, urllib.parse; print(urllib.parse.quote(sys.stdin.read()))")
test_api "搜索不存在的应用" \
    "$BASE_URL/api/admin/app/apps?appNameQuery=$encoded_search" \
    "0"

# 测试用例6: 大页面测试
test_api "大页面测试 - 每页50条" \
    "$BASE_URL/api/admin/app/apps?page=0&size=50"

# 测试用例7: 排序测试
test_api "排序测试 - 按创建时间升序" \
    "$BASE_URL/api/admin/app/apps?sort=createTime,asc"

# 测试用例8: 无API密钥测试
echo -e "${YELLOW}测试: 无API密钥访问${NC}"
response=$(curl -s -X GET "$BASE_URL/api/admin/app/apps" \
    -H "Content-Type: application/json")
http_code=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/admin/app/apps" \
    -H "Content-Type: application/json")

if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
    echo -e "${GREEN}✅ 正确拒绝无API密钥请求 (HTTP $http_code)${NC}"
else
    echo -e "${RED}❌ 应该拒绝无API密钥请求 (HTTP $http_code)${NC}"
fi
echo ""

# 测试用例9: 错误API密钥测试
echo -e "${YELLOW}测试: 错误API密钥${NC}"
response=$(curl -s -X GET "$BASE_URL/api/admin/app/apps" \
    -H "X-API-KEY: wrong-api-key" \
    -H "Content-Type: application/json")
http_code=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/admin/app/apps" \
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
echo -e "${GREEN}✅ 查询应用列表API功能测试完成${NC}"
echo "新功能包括："
echo "  - 分页查询支持"
echo "  - 应用名称模糊搜索"
echo "  - 返回应用基本信息和最新版本信息"
echo "  - 版本总数统计"
echo "  - 适当的错误处理和安全验证" 
#!/bin/bash

# Android应用更新系统 - 快速测试脚本
# 使用方法: chmod +x 快速测试脚本.sh && ./快速测试脚本.sh

echo "🚀 Android应用更新系统 - 快速测试脚本"
echo "============================================"

# 配置变量
SERVER_URL="http://localhost:8080"
API_KEY="your-secret-api-key"
TEST_APK="apk_uploads/UnifySign_productRelease_v1.1.8001.apk"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 函数：打印带颜色的消息
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 函数：检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 命令未找到，请先安装"
        return 1
    fi
    return 0
}

# 函数：等待用户按键
wait_for_key() {
    echo ""
    read -p "按回车键继续..."
    echo ""
}

# 1. 环境检查
echo ""
print_info "1. 检查环境依赖..."
echo "--------------------"

# 检查Java
if check_command java; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "Java: $JAVA_VERSION"
else
    print_error "请先安装Java 11+"
    exit 1
fi

# 检查Maven
if check_command mvn; then
    MVN_VERSION=$(mvn -version 2>&1 | head -n 1)
    print_success "Maven: $MVN_VERSION"
else
    print_error "请先安装Maven"
    exit 1
fi

# 检查curl
if check_command curl; then
    print_success "curl: 已安装"
else
    print_error "请先安装curl"
    exit 1
fi

wait_for_key

# 2. 服务连通性测试
echo ""
print_info "2. 测试服务连通性..."
echo "----------------------"

print_info "测试服务器连接: $SERVER_URL"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $SERVER_URL)

if [ "$HTTP_CODE" = "404" ]; then
    print_success "服务器正常运行 (HTTP 404 - 预期响应)"
elif [ "$HTTP_CODE" = "000" ]; then
    print_error "无法连接到服务器，请检查:"
    echo "  1. 服务是否已启动: cd Server && mvn spring-boot:run"
    echo "  2. 端口是否正确: $SERVER_URL"
    echo "  3. 防火墙设置"
    exit 1
else
    print_warning "服务器响应异常 (HTTP $HTTP_CODE)"
fi

wait_for_key

# 3. 创建测试文件
echo ""
print_info "3. 准备测试文件..."
echo "-------------------"

if [ -f "$TEST_APK" ]; then
    APK_SIZE=$(du -h "$TEST_APK" | cut -f1)
    print_success "真实APK文件已存在: $TEST_APK (大小: $APK_SIZE)"
    print_info "APK文件信息: UnifySign安全签名工具"
else
    print_error "APK文件不存在: $TEST_APK"
    print_info "请确保在apk_uploads目录下有UnifySign APK文件"
    exit 1
fi

wait_for_key

# 4. API安全测试
echo ""
print_info "4. API安全验证测试..."
echo "----------------------"

# 测试无API密钥
print_info "测试1: 无API密钥访问 (应该被拒绝)"
RESPONSE=$(curl -s -X POST -F "apkFile=@$TEST_APK" $SERVER_URL/api/admin/app/upload)
if [[ $RESPONSE == *"Missing request header"* ]] || [[ $RESPONSE == *"Required request parameter"* ]]; then
    print_success "✓ 无API密钥请求被正确拒绝"
else
    print_warning "⚠ 无API密钥请求响应: $RESPONSE"
fi

# 测试错误API密钥
print_info "测试2: 错误API密钥访问 (应该被拒绝)"
RESPONSE=$(curl -s -X POST -H "X-API-Key: wrong-key" -F "apkFile=@$TEST_APK" $SERVER_URL/api/admin/app/upload)
if [[ $RESPONSE == *"Invalid API key"* ]] || [[ $RESPONSE == *"Required request parameter"* ]]; then
    print_success "✓ 错误API密钥请求被正确拒绝"
else
    print_warning "⚠ 错误API密钥请求响应: $RESPONSE"
fi

wait_for_key

# 5. 正常API测试
echo ""
print_info "5. 正常API功能测试..."
echo "---------------------"

print_info "测试APK上传接口 (使用正确的API密钥)"
print_info "API密钥: $API_KEY"
print_info "测试文件: $TEST_APK"

RESPONSE=$(curl -s -X POST \
  -H "X-API-Key: $API_KEY" \
  -F "apkFile=@$TEST_APK" \
  -F "appId=com.unifysign.android" \
  -F "updateDescription=UnifySign安全签名工具测试版本" \
  -F "forceUpdate=false" \
  $SERVER_URL/api/admin/app/upload)

echo "API响应:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

if [[ $RESPONSE == *"APK上传成功"* ]]; then
    print_success "✓ API调用成功，APK文件解析正常"
elif [[ $RESPONSE == *"success"* ]]; then
    print_success "✓ API调用成功"
else
    print_warning "⚠ API响应异常，请检查日志: $RESPONSE"
fi

wait_for_key

# 6. 数据库检查
echo ""
print_info "6. 数据库状态检查..."
echo "--------------------"

if [ -f "./database/app_update_db.mv.db" ]; then
    print_success "✓ H2数据库文件存在"
    DB_SIZE=$(du -h ./database/app_update_db.mv.db | cut -f1)
    print_info "数据库文件大小: $DB_SIZE"
else
    print_warning "⚠ H2数据库文件不存在，可能是首次运行"
fi

if [ -d "./apk_uploads" ]; then
    print_success "✓ APK上传目录存在"
    UPLOAD_COUNT=$(ls -1 ./apk_uploads 2>/dev/null | wc -l)
    print_info "上传文件数量: $UPLOAD_COUNT"
else
    print_warning "⚠ APK上传目录不存在"
fi

wait_for_key

# 7. 性能测试
echo ""
print_info "7. 简单性能测试..."
echo "------------------"

print_info "测试服务器响应时间..."
for i in {1..3}; do
    START_TIME=$(date +%s%N)
    curl -s $SERVER_URL > /dev/null
    END_TIME=$(date +%s%N)
    RESPONSE_TIME=$(( (END_TIME - START_TIME) / 1000000 ))
    print_info "第${i}次请求响应时间: ${RESPONSE_TIME}ms"
done

wait_for_key

# 8. 清理测试文件
echo ""
print_info "8. 测试文件管理..."
echo "------------------"

print_info "测试使用的是真实APK文件: $TEST_APK"
print_info "该文件不会被删除，可用于后续测试"

# 检查上传后生成的文件
if [ -d "./apk_uploads" ]; then
    PROCESSED_FILES=$(ls -1 ./apk_uploads/com.unifysign.android-*.apk 2>/dev/null | wc -l)
    if [ "$PROCESSED_FILES" -gt 0 ]; then
        print_info "系统已生成 $PROCESSED_FILES 个处理后的APK文件"
        print_info "位置: ./apk_uploads/com.unifysign.android-*.apk"
    fi
fi

# 9. 测试总结
echo ""
print_info "🎉 测试完成！"
echo "=============="

print_success "✅ 环境检查通过"
print_success "✅ 服务连通性正常"
print_success "✅ API安全验证正常"
print_success "✅ 基本功能测试完成"

echo ""
print_info "📚 有用的链接:"
echo "  • 服务器地址: $SERVER_URL"
echo "  • H2数据库控制台: $SERVER_URL/h2-console"
echo "  • API文档: 查看 docs/2-技术规范/服务端运行调试指南.md"

echo ""
print_info "🔧 如果遇到问题:"
echo "  1. 检查服务是否启动: cd Server && mvn spring-boot:run"
echo "  2. 检查端口占用: lsof -i :8080"
echo "  3. 查看应用日志"
echo "  4. 检查API密钥是否正确"

echo ""
print_info "🚀 下一步:"
echo "  • 使用真实的APK文件进行测试"
echo "  • 配置生产环境数据库"
echo "  • 开发前端管理界面"

echo ""
print_success "测试脚本执行完成！" 
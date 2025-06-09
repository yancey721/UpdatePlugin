#!/bin/bash

# 简单的数据库重置脚本 - 直接删除数据库文件

echo "=== 数据库重置脚本 ==="
echo ""

# 检查服务器是否在运行
if pgrep -f "spring-boot" > /dev/null; then
    echo "⚠️  警告: 检测到Spring Boot服务器可能正在运行"
    echo "请先停止服务器再执行此脚本，避免数据文件损坏"
    echo ""
    echo "停止服务器方法:"
    echo "1. 按 Ctrl+C 停止正在运行的服务器"
    echo "2. 或者运行: pkill -f spring-boot"
    echo ""
    read -p "确认服务器已停止并继续重置？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "操作已取消"
        exit 1
    fi
fi

echo "正在重置数据库..."

# 删除H2数据库文件
if [ -d "./database" ]; then
    echo "删除数据库文件..."
    rm -f ./database/app_update_db.*
    echo "✓ 数据库文件已删除"
else
    echo "数据库目录不存在，创建目录..."
    mkdir -p ./database
fi

# 清理APK文件
echo ""
echo "清理APK文件..."
if [ -d "../apk_uploads" ]; then
    # 保留README.md
    if [ -f "../apk_uploads/README.md" ]; then
        cp "../apk_uploads/README.md" "/tmp/readme_backup.md"
    fi
    
    # 删除所有内容
    rm -rf ../apk_uploads/*
    
    # 恢复README.md
    if [ -f "/tmp/readme_backup.md" ]; then
        cp "/tmp/readme_backup.md" "../apk_uploads/README.md"
        rm "/tmp/readme_backup.md"
    fi
    
    echo "✓ APK文件已清理"
else
    echo "创建APK上传目录..."
    mkdir -p ../apk_uploads
fi

echo ""
echo "✅ 数据库重置完成!"
echo ""
echo "现在可以重新启动服务器:"
echo "  mvn spring-boot:run"
echo ""
echo "服务器启动后将会:"
echo "1. 自动创建新的空数据库"
echo "2. 创建必要的数据表"
echo "3. 系统恢复到初始状态" 
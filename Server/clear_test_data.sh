#!/bin/bash

# 清理测试数据脚本（数据库+文件）

echo "=== 清理测试数据 ==="

# 数据库连接信息（H2数据库）
DB_URL="jdbc:h2:file:./database/app_update_db"
DB_USER="sa"
DB_PASS=""

echo ""
echo "1. 检查H2数据库连接..."

# 检查H2数据库文件是否存在
if [ ! -f "./database/app_update_db.mv.db" ]; then
    echo "警告: H2数据库文件不存在，可能服务器从未启动过"
    echo "数据库路径: ./database/app_update_db.mv.db"
else
    echo "✓ H2数据库文件存在"
fi

echo ""
echo "2. 清理数据库数据..."

# 方式1: 如果有H2的客户端工具，可以直接执行SQL
if command -v java >/dev/null 2>&1; then
    echo "尝试使用H2数据库工具执行清理脚本..."
    
    # 下载H2工具（如果不存在）
    if [ ! -f "h2-*.jar" ]; then
        echo "下载H2数据库工具..."
        curl -L -o h2.jar "https://repo1.maven.org/maven2/com/h2database/h2/2.1.214/h2-2.1.214.jar" 2>/dev/null
    fi
    
    if [ -f "h2.jar" ] || ls h2-*.jar 1> /dev/null 2>&1; then
        H2_JAR=$(ls h2*.jar | head -1)
        echo "使用H2工具: $H2_JAR"
        
        # 执行清理SQL脚本
        echo "执行SQL清理脚本..."
        java -cp "$H2_JAR" org.h2.tools.RunScript \
            -url "$DB_URL" \
            -user "$DB_USER" \
            -password "$DB_PASS" \
            -script "./clear_database.sql" \
            -continueOnError
        
        if [ $? -eq 0 ]; then
            echo "✓ 数据库清理完成"
        else
            echo "❌ 数据库清理失败"
        fi
    else
        echo "警告: 未找到H2工具，请手动清理数据库"
        echo "可以通过以下方式清理:"
        echo "1. 启动服务器，访问 http://localhost:8080/h2-console"
        echo "2. 连接数据库并执行 clear_database.sql 中的SQL语句"
        echo "3. 或者直接删除数据库文件: rm -f database/app_update_db.*"
    fi
else
    echo "警告: 未安装Java，无法使用H2工具"
fi

echo ""
echo "3. 清理APK文件..."

# 清理APK上传目录
if [ -d "../apk_uploads" ]; then
    echo "清理APK上传目录: ../apk_uploads"
    
    # 备份目录结构说明
    if [ -f "../apk_uploads/README.md" ]; then
        cp "../apk_uploads/README.md" "/tmp/apk_uploads_readme_backup.md"
        echo "备份README.md到: /tmp/apk_uploads_readme_backup.md"
    fi
    
    # 删除所有APK文件
    find ../apk_uploads -name "*.apk" -delete
    echo "已删除所有APK文件"
    
    # 删除空的应用目录（保留根目录）
    find ../apk_uploads -type d -empty -not -path "../apk_uploads" -delete
    echo "已删除空的应用目录"
    
    # 恢复README.md
    if [ -f "/tmp/apk_uploads_readme_backup.md" ]; then
        cp "/tmp/apk_uploads_readme_backup.md" "../apk_uploads/README.md"
        rm "/tmp/apk_uploads_readme_backup.md"
        echo "恢复README.md"
    fi
    
    echo "✓ APK文件清理完成"
else
    echo "APK上传目录不存在: ../apk_uploads"
fi

echo ""
echo "4. 清理临时文件..."

# 清理可能的临时文件
rm -f test_apks/*.apk 2>/dev/null
rm -rf test_apks 2>/dev/null
echo "✓ 临时文件清理完成"

echo ""
echo "5. 显示清理结果..."

echo "数据库状态:"
if [ -f "./database/app_update_db.mv.db" ]; then
    echo "  H2数据库文件大小: $(du -h ./database/app_update_db.mv.db | cut -f1)"
else
    echo "  H2数据库文件: 不存在"
fi

echo "APK文件状态:"
if [ -d "../apk_uploads" ]; then
    APK_COUNT=$(find ../apk_uploads -name "*.apk" | wc -l)
    echo "  APK文件数量: $APK_COUNT"
    if [ $APK_COUNT -gt 0 ]; then
        echo "  剩余APK文件:"
        find ../apk_uploads -name "*.apk" | sort
    fi
else
    echo "  APK上传目录: 不存在"
fi

echo ""
echo "=== 清理完成 ==="
echo ""
echo "如果需要彻底重置:"
echo "1. 停止服务器"
echo "2. 删除数据库文件: rm -f database/app_update_db.*"
echo "3. 重启服务器（会自动重新创建空数据库）" 
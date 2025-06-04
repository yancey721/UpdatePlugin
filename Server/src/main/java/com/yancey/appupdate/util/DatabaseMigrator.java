package com.yancey.appupdate.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigrator {
    
    public static void main(String[] args) {
        String url = "jdbc:h2:file:./database/app_update_db";
        String user = "sa";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            executeMigration(conn);
            System.out.println("数据库重构完成！");
        } catch (SQLException e) {
            System.err.println("数据库重构失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void executeMigration(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            
            // 清空所有测试数据
            System.out.println("清空测试数据...");
            try {
                stmt.execute("DROP TABLE IF EXISTS app_version");
                stmt.execute("DROP TABLE IF EXISTS app_info");
                System.out.println("已清空所有表");
            } catch (SQLException e) {
                System.out.println("表不存在，跳过清空");
            }
            
            // 创建新的 app_info 表结构（使用 app_id 作为主键）
            System.out.println("创建新的 app_info 表...");
            stmt.execute("CREATE TABLE app_info (" +
                "app_id VARCHAR(100) PRIMARY KEY," +  // 直接使用 packageName 作为主键
                "app_name VARCHAR(200) NOT NULL," +
                "app_description TEXT," +              // 添加应用描述字段
                "force_update BOOLEAN DEFAULT FALSE," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // 创建新的 app_version 表结构（外键直接引用 app_id）
            System.out.println("创建新的 app_version 表...");
            stmt.execute("CREATE TABLE app_version (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "app_id VARCHAR(100) NOT NULL," +      // 直接引用 app_info.app_id
                "version_code INT NOT NULL," +
                "version_name VARCHAR(50) NOT NULL," +
                "file_size BIGINT NOT NULL," +
                "md5 VARCHAR(32) NOT NULL," +
                "apk_path VARCHAR(500) NOT NULL," +
                "download_url VARCHAR(500) NOT NULL," +
                "update_description TEXT," +
                "force_update BOOLEAN DEFAULT FALSE," +
                "is_released BOOLEAN DEFAULT FALSE," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (app_id) REFERENCES app_info(app_id) ON DELETE CASCADE" +
                ")");
            
            // 添加唯一约束
            System.out.println("添加唯一约束...");
            stmt.execute("CREATE UNIQUE INDEX unique_app_version ON app_version (app_id, version_code)");
            
            System.out.println("数据库重构完成！");
            System.out.println("新结构说明：");
            System.out.println("- app_info.app_id 现在是主键，直接使用 packageName");
            System.out.println("- app_version.app_id 直接引用 app_info.app_id");
            System.out.println("- 添加了 app_description 字段用于应用描述");
            System.out.println("- 所有测试数据已清空");
        }
    }
} 
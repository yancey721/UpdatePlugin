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
            System.out.println("数据库迁移完成！");
        } catch (SQLException e) {
            System.err.println("数据库迁移失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void executeMigration(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 首先检查表是否存在，如果不存在就创建
            try {
                stmt.execute("SELECT COUNT(*) FROM app_info");
                System.out.println("app_info 表已存在");
                
                // 检查并添加 force_update 字段
                try {
                    stmt.execute("SELECT force_update FROM app_info LIMIT 1");
                    System.out.println("force_update 字段已存在");
                } catch (SQLException e) {
                    System.out.println("添加 force_update 字段...");
                    stmt.execute("ALTER TABLE app_info ADD COLUMN force_update BOOLEAN DEFAULT FALSE");
                }
                
            } catch (SQLException e) {
                System.out.println("创建 app_info 表...");
                stmt.execute("CREATE TABLE app_info (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "app_id VARCHAR(100) NOT NULL UNIQUE," +
                    "app_name VARCHAR(100) NOT NULL," +
                    "package_name VARCHAR(100) NOT NULL," +
                    "force_update BOOLEAN DEFAULT FALSE," +
                    "create_time TIMESTAMP," +
                    "update_time TIMESTAMP" +
                    ")");
            }
            
            try {
                stmt.execute("SELECT COUNT(*) FROM app_version");
                System.out.println("app_version 表已存在");
                
                // 检查并删除 status 字段（如果存在）
                try {
                    stmt.execute("SELECT status FROM app_version LIMIT 1");
                    System.out.println("删除 status 字段...");
                    stmt.execute("ALTER TABLE app_version DROP COLUMN status");
                } catch (SQLException e) {
                    System.out.println("status 字段不存在或已删除");
                }
                
                // 检查并添加 is_released 字段
                try {
                    stmt.execute("SELECT is_released FROM app_version LIMIT 1");
                    System.out.println("is_released 字段已存在");
                } catch (SQLException e) {
                    System.out.println("添加 is_released 字段...");
                    stmt.execute("ALTER TABLE app_version ADD COLUMN is_released BOOLEAN DEFAULT FALSE");
                }
                
            } catch (SQLException e) {
                System.out.println("创建 app_version 表...");
                stmt.execute("CREATE TABLE app_version (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "app_info_id BIGINT NOT NULL," +
                    "version_code INT NOT NULL," +
                    "version_name VARCHAR(50) NOT NULL," +
                    "file_size BIGINT NOT NULL," +
                    "md5 VARCHAR(32) NOT NULL," +
                    "apk_path VARCHAR(200) NOT NULL," +
                    "download_url VARCHAR(500) NOT NULL," +
                    "update_description TEXT," +
                    "force_update BOOLEAN DEFAULT FALSE," +
                    "is_released BOOLEAN DEFAULT FALSE," +
                    "create_time TIMESTAMP," +
                    "update_time TIMESTAMP," +
                    "FOREIGN KEY (app_info_id) REFERENCES app_info(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_app_version (app_info_id, version_code)" +
                    ")");
            }
            
            System.out.println("数据库结构验证完成");
        }
    }
} 
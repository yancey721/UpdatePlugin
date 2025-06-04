package com.yancey.appupdate.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TestDataInserter {
    
    public static void main(String[] args) {
        String url = "jdbc:h2:file:./database/app_update_db";
        String user = "sa";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            insertTestData(conn);
            System.out.println("测试数据插入成功！");
        } catch (SQLException e) {
            System.err.println("插入测试数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void insertTestData(Connection conn) throws SQLException {
        // 清空现有数据
        conn.prepareStatement("DELETE FROM app_version").executeUpdate();
        conn.prepareStatement("DELETE FROM app_info").executeUpdate();
        
        // 插入应用信息
        String appInfoSql = "INSERT INTO app_info (app_id, app_name, package_name, force_update, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(appInfoSql)) {
            // 应用1
            ps.setString(1, "com.test.app1");
            ps.setString(2, "测试应用1");
            ps.setString(3, "com.test.app1");
            ps.setBoolean(4, false);
            ps.setObject(5, LocalDateTime.now());
            ps.setObject(6, LocalDateTime.now());
            ps.executeUpdate();
            
            // 应用2
            ps.setString(1, "com.test.app2");
            ps.setString(2, "测试应用2");
            ps.setString(3, "com.test.app2");
            ps.setBoolean(4, true);
            ps.setObject(5, LocalDateTime.now());
            ps.setObject(6, LocalDateTime.now());
            ps.executeUpdate();
        }
        
        // 插入版本信息
        String versionSql = "INSERT INTO app_version (app_info_id, version_code, version_name, file_size, md5, apk_path, download_url, update_description, force_update, is_released, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(versionSql)) {
            // 查询app_info的ID
            var stmt = conn.prepareStatement("SELECT id FROM app_info WHERE app_id = ?");
            
            // 应用1的版本
            stmt.setString(1, "com.test.app1");
            var rs = stmt.executeQuery();
            if (rs.next()) {
                Long appInfoId = rs.getLong("id");
                
                // 版本1.0.0
                ps.setLong(1, appInfoId);
                ps.setInt(2, 100);
                ps.setString(3, "1.0.0");
                ps.setLong(4, 1024000L);
                ps.setString(5, "abc123def456");
                ps.setString(6, "/apps/com.test.app1/app1_v1.0.0.apk");
                ps.setString(7, "http://localhost:8080/download/app1_v1.0.0.apk");
                ps.setString(8, "初始版本");
                ps.setBoolean(9, false);
                ps.setBoolean(10, false);
                ps.setObject(11, LocalDateTime.now().minusHours(2));
                ps.setObject(12, LocalDateTime.now().minusHours(2));
                ps.executeUpdate();
                
                // 版本1.0.1 (当前发布版本)
                ps.setLong(1, appInfoId);
                ps.setInt(2, 101);
                ps.setString(3, "1.0.1");
                ps.setLong(4, 1024500L);
                ps.setString(5, "def456ghi789");
                ps.setString(6, "/apps/com.test.app1/app1_v1.0.1.apk");
                ps.setString(7, "http://localhost:8080/download/app1_v1.0.1.apk");
                ps.setString(8, "修复BUG，提升稳定性");
                ps.setBoolean(9, false);
                ps.setBoolean(10, true); // 设为发布版本
                ps.setObject(11, LocalDateTime.now().minusHours(1));
                ps.setObject(12, LocalDateTime.now().minusHours(1));
                ps.executeUpdate();
                
                // 版本1.0.2
                ps.setLong(1, appInfoId);
                ps.setInt(2, 102);
                ps.setString(3, "1.0.2");
                ps.setLong(4, 1025000L);
                ps.setString(5, "ghi789jkl012");
                ps.setString(6, "/apps/com.test.app1/app1_v1.0.2.apk");
                ps.setString(7, "http://localhost:8080/download/app1_v1.0.2.apk");
                ps.setString(8, "新增功能：\n- 支持夜间模式\n- 优化性能");
                ps.setBoolean(9, false);
                ps.setBoolean(10, false);
                ps.setObject(11, LocalDateTime.now().minusMinutes(30));
                ps.setObject(12, LocalDateTime.now().minusMinutes(30));
                ps.executeUpdate();
            }
            
            // 应用2的版本
            stmt.setString(1, "com.test.app2");
            rs = stmt.executeQuery();
            if (rs.next()) {
                Long appInfoId = rs.getLong("id");
                
                // 版本2.0.0 (当前发布版本)
                ps.setLong(1, appInfoId);
                ps.setInt(2, 200);
                ps.setString(3, "2.0.0");
                ps.setLong(4, 2048000L);
                ps.setString(5, "jkl012mno345");
                ps.setString(6, "/apps/com.test.app2/app2_v2.0.0.apk");
                ps.setString(7, "http://localhost:8080/download/app2_v2.0.0.apk");
                ps.setString(8, "第二个应用初始版本");
                ps.setBoolean(9, true);
                ps.setBoolean(10, true); // 设为发布版本
                ps.setObject(11, LocalDateTime.now().minusHours(3));
                ps.setObject(12, LocalDateTime.now().minusHours(3));
                ps.executeUpdate();
            }
        }
    }
} 
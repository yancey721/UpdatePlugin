-- 插入测试应用数据
INSERT INTO app_info (id, app_id, app_name, package_name, force_update, create_time, update_time) VALUES
(1, 'com.test.app1', '测试应用1', 'com.test.app1', false, '2024-06-04 16:00:00', '2024-06-04 16:00:00'),
(2, 'com.test.app2', '测试应用2', 'com.test.app2', true, '2024-06-04 16:00:00', '2024-06-04 16:00:00');

-- 插入测试版本数据
INSERT INTO app_version (id, app_info_id, version_code, version_name, file_size, md5, apk_path, download_url, update_description, force_update, is_released, create_time, update_time) VALUES
(1, 1, 100, '1.0.0', 1024000, 'abc123def456', '/apps/com.test.app1/app1_v1.0.0.apk', 'http://localhost:8080/download/app1_v1.0.0.apk', '初始版本', false, false, '2024-06-04 16:00:00', '2024-06-04 16:00:00'),
(2, 1, 101, '1.0.1', 1024500, 'def456ghi789', '/apps/com.test.app1/app1_v1.0.1.apk', 'http://localhost:8080/download/app1_v1.0.1.apk', '修复BUG', false, true, '2024-06-04 16:01:00', '2024-06-04 16:01:00'),
(3, 1, 102, '1.0.2', 1025000, 'ghi789jkl012', '/apps/com.test.app1/app1_v1.0.2.apk', 'http://localhost:8080/download/app1_v1.0.2.apk', '新功能', false, false, '2024-06-04 16:02:00', '2024-06-04 16:02:00'),
(4, 2, 200, '2.0.0', 2048000, 'jkl012mno345', '/apps/com.test.app2/app2_v2.0.0.apk', 'http://localhost:8080/download/app2_v2.0.0.apk', '第二个应用初始版本', true, true, '2024-06-04 16:00:00', '2024-06-04 16:00:00'); 
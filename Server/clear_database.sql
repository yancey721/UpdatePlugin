-- 清空数据库测试数据脚本
-- 注意：此脚本会删除所有应用版本和应用信息数据，请谨慎使用

-- 关闭外键约束检查（H2数据库）
SET REFERENTIAL_INTEGRITY FALSE;

-- 清空应用版本表
DELETE FROM app_version;

-- 清空应用信息表  
DELETE FROM app_info;

-- 重置自增ID（如果需要）
ALTER TABLE app_version ALTER COLUMN id RESTART WITH 1;

-- 开启外键约束检查
SET REFERENTIAL_INTEGRITY TRUE;

-- 显示清理结果
SELECT 'app_info 表记录数:' AS table_name, COUNT(*) AS record_count FROM app_info
UNION ALL
SELECT 'app_version 表记录数:' AS table_name, COUNT(*) AS record_count FROM app_version;

-- 完成提示
SELECT '数据库清理完成！' AS message; 
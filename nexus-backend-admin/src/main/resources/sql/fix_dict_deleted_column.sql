-- 修复 system_dict 表缺少 deleted 字段的问题
-- 执行日期: 2025-10-29

-- 检查并添加 deleted 字段
ALTER TABLE `system_dict`
ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除';

-- 为 deleted 字段创建索引（可选，但推荐用于提升查询性能）
ALTER TABLE `system_dict`
ADD INDEX `idx_deleted` (`deleted`);
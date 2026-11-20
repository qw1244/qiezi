-- 合集加标签字段：tags 存逗号分隔的标签，如 重生,男频
-- 在服务器 MySQL 里执行一次（数据库名按实际的来，一般是 cms_video）
ALTER TABLE `series` ADD COLUMN `tags` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' AFTER `category`;

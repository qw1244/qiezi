-- ============================================
-- 茄子短剧：用户自建合集投稿（v2，已有数据库执行一次）
-- 在服务器上执行：mysql cms_video < user_series.sql
-- 注意：不要重复执行 v1 的 user_upload.sql（up_job 的 user_id 字段已加过）
-- ============================================

-- 合集表加 user_id：管理员建的合集 user_id 为空，用户新建的合集 user_id 为投稿人
ALTER TABLE `series` ADD COLUMN `user_id` bigint NULL DEFAULT NULL AFTER `id`, ADD INDEX `idx_user` (`user_id`);

-- 新装部署才需要在 cms_video.sql 的 series 建表里 id 后面加一行：
-- `user_id` bigint NULL DEFAULT NULL,

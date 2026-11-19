-- ============================================
-- 茄子短剧：用户投稿功能（已有数据库执行一次）
-- 在 Navicat 里连上 cms_video 库，新建查询，粘贴执行即可
-- ============================================

-- 上传任务表加 user_id：管理员上传的任务 user_id 为空，用户投稿的任务 admin_id 为 0、user_id 为投稿人
ALTER TABLE `up_job` ADD COLUMN `user_id` bigint NULL DEFAULT NULL AFTER `admin_id`, ADD INDEX `idx_user` (`user_id`);

-- 新装部署才需要在 cms_video.sql 的 up_job 建表里 admin_id 后面加一行：
-- `user_id` bigint NULL DEFAULT NULL,

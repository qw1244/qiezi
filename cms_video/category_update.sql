-- ============================================
-- 茄子短剧：给合集表加分类字段（已有数据库执行一次）
-- 在 Navicat 里连上 cms_video 库，新建查询，粘贴执行即可
-- ============================================

-- 加 category 字段（分类：AI / AI），已有合集默认归为“AI”
ALTER TABLE `series`
  ADD COLUMN `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AI' AFTER `intro`,
  ADD INDEX `idx_category` (`category`);

-- 验证：应看到 category 列，现有合集显示为 AI
-- SELECT id, title, category FROM `series`;

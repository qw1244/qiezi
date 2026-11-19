/*
 Navicat Premium Data Transfer

 Source Server         : MySQL80
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : cms_video

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 08/11/2026 20:17:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$4rwnTMgzh6yfwcHISJ/.gec5gV6WkgeAhck3ngtZQbvTBC4PG2bji', '管理员', 1, '2026-11-05 20:51:47', '2026-11-05 20:54:51');

-- ----------------------------
-- Table structure for cos_cfg
-- ----------------------------
DROP TABLE IF EXISTS `cos_cfg`;
CREATE TABLE `cos_cfg`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `secret_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `secret_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `bucket` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cdn_host` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `path_prefix` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'video/',
  `enabled` tinyint NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cos_cfg
-- ----------------------------
INSERT INTO `cos_cfg` VALUES (1, '666139cc5736fa8d9a7e570b1dbd423f', 'e805c7422999283478609cd7fbc7c2aafa5278c44c0b8f4219db5daee123896c', 'c78ec5f3f8168a38d631cc740975fd7e', 'qiezi2', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev', 'video/', 1, '2026-11-05 20:51:47');

-- ----------------------------
-- Table structure for fav
-- ----------------------------
DROP TABLE IF EXISTS `fav`;
CREATE TABLE `fav`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `series_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_series`(`user_id` ASC, `series_id` ASC) USING BTREE,
  INDEX `idx_series`(`series_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fav
-- ----------------------------
INSERT INTO `fav` VALUES (1, 2, 1, '2026-11-06 12:35:50');

-- ----------------------------
-- Table structure for op_log
-- ----------------------------
DROP TABLE IF EXISTS `op_log`;
CREATE TABLE `op_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `detail` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin`(`admin_id` ASC) USING BTREE,
  INDEX `idx_time`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of op_log
-- ----------------------------
INSERT INTO `op_log` VALUES (22, 1, 'oplog.clear', '清空操作记录', '127.0.0.1', '2026-11-06 00:51:30');
INSERT INTO `op_log` VALUES (23, 0, 'auth.login.fail', 'username=admin', '127.0.0.1', '2026-11-06 12:40:49');
INSERT INTO `op_log` VALUES (24, 1, 'auth.login.ok', 'username=admin', '127.0.0.1', '2026-11-06 12:40:58');
INSERT INTO `op_log` VALUES (25, 1, 'auth.pwd.change', 'admin changed password', '', '2026-11-06 12:41:11');
INSERT INTO `op_log` VALUES (26, 1, 'auth.login.ok', 'username=admin', '127.0.0.1', '2026-11-06 12:41:19');
INSERT INTO `op_log` VALUES (27, 1, 'auth.login.ok', 'username=admin', '127.0.0.1', '2026-11-06 14:28:23');
INSERT INTO `op_log` VALUES (28, 1, 'auth.login.ok', 'username=admin', '127.0.0.1', '2026-11-08 18:20:27');
INSERT INTO `op_log` VALUES (29, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 18:40:11');
INSERT INTO `op_log` VALUES (30, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 18:40:54');
INSERT INTO `op_log` VALUES (31, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 19:09:52');
INSERT INTO `op_log` VALUES (32, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 19:13:35');
INSERT INTO `op_log` VALUES (33, 1, 'video.del', 'id=3', '127.0.0.1', '2026-11-08 19:15:24');
INSERT INTO `op_log` VALUES (34, 1, 'video.del', 'id=5', '127.0.0.1', '2026-11-08 19:15:27');
INSERT INTO `op_log` VALUES (35, 1, 'video.del', 'id=2', '127.0.0.1', '2026-11-08 19:15:29');
INSERT INTO `op_log` VALUES (36, 1, 'video.renumber', 'seriesId=1', '127.0.0.1', '2026-11-08 19:21:14');
INSERT INTO `op_log` VALUES (37, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 19:24:09');
INSERT INTO `op_log` VALUES (38, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 19:25:41');
INSERT INTO `op_log` VALUES (39, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 20:00:32');
INSERT INTO `op_log` VALUES (40, 1, 'video.del', 'id=6', '127.0.0.1', '2026-11-08 20:02:44');
INSERT INTO `op_log` VALUES (41, 1, 'video.del', 'id=7', '127.0.0.1', '2026-11-08 20:02:44');
INSERT INTO `op_log` VALUES (42, 1, 'video.del', 'id=8', '127.0.0.1', '2026-11-08 20:02:47');
INSERT INTO `op_log` VALUES (43, 1, 'cos.save', '更新R2配置', '127.0.0.1', '2026-11-08 20:04:31');
INSERT INTO `op_log` VALUES (44, 1, 'video.del', 'id=9', '127.0.0.1', '2026-11-08 20:07:49');
INSERT INTO `op_log` VALUES (45, 1, 'video.del', 'id=10', '127.0.0.1', '2026-11-08 20:07:50');
INSERT INTO `op_log` VALUES (46, 1, 'video.del', 'id=10', '127.0.0.1', '2026-11-08 20:07:50');
INSERT INTO `op_log` VALUES (47, 1, 'video.del', 'id=11', '127.0.0.1', '2026-11-08 20:07:54');

-- ----------------------------
-- Table structure for series
-- ----------------------------
DROP TABLE IF EXISTS `series`;
CREATE TABLE `series`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cover` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `intro` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `status` tinyint NOT NULL DEFAULT 1,
  `ep_count` int NOT NULL DEFAULT 0,
  `play_count` bigint NOT NULL DEFAULT 0,
  `fav_count` bigint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of series
-- ----------------------------
INSERT INTO `series` VALUES (1, '重活一世之1970', 'https://cmsvideo-1505413267.cos.ap-chengdu.myqcloud.com/cover/20261105/a2b2121e62b2_wp-image-017-1024x1460-21-718x1024.jpg', '男主重生剧情，男频', 1, 3, 106, 1, '2026-11-05 23:20:17', '2026-11-05 23:20:17');

-- ----------------------------
-- Table structure for up_item
-- ----------------------------
DROP TABLE IF EXISTS `up_item`;
CREATE TABLE `up_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `file_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cos_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cover` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ep_no` int NOT NULL DEFAULT 0,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `duration` int NOT NULL DEFAULT 0,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `err_msg` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_job`(`job_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of up_item
-- ----------------------------
INSERT INTO `up_item` VALUES (1, 1, '618400130.720p.narrowv3.mp4', 'video/1/20261105/1d5506ddbda8_618400130.720p.narrowv3.mp4', '', 1, '618400130.720p.narrowv3', 0, 'ok', '', '2026-11-05 23:22:49', '2026-11-05 23:22:49');
INSERT INTO `up_item` VALUES (2, 2, '618400137.720p.narrowv3.mp4', 'video/1/20261105/35737415bd5d_618400137.720p.narrowv3.mp4', '', 1, '618400137.720p.narrowv3', 0, 'ok', '', '2026-11-05 23:23:59', '2026-11-05 23:23:59');
INSERT INTO `up_item` VALUES (3, 2, '618400133.720p.narrowv3.mp4', 'video/1/20261105/97cf8380c324_618400133.720p.narrowv3.mp4', '', 2, '618400133.720p.narrowv3', 0, 'ok', '', '2026-11-05 23:23:59', '2026-11-05 23:23:59');
INSERT INTO `up_item` VALUES (4, 3, '618400137.720p.narrowv3.mp4', 'video/1/20261105/48e88c48fd44_618400137.720p.narrowv3.mp4', '', 3, '618400137.720p.narrowv3', 0, 'ok', '', '2026-11-05 23:28:20', '2026-11-05 23:28:20');
INSERT INTO `up_item` VALUES (5, 4, '618400130.720p.narrowv3.mp4', 'video/1/20261105/b52dc69ac1b2_618400130.720p.narrowv3.mp4', 'https://cmsvideo-1505413267.cos.ap-chengdu.myqcloud.com/video-cover/20261105/4efe7f9ff09c_618400130.720p.narrowv3.jpg', 3, '第3集', 114, 'ok', '', '2026-11-05 23:41:53', '2026-11-05 23:41:53');
INSERT INTO `up_item` VALUES (6, 5, '618400130.720p.narrowv3.mp4', 'video/1/20261105/d12502d54477_618400130.720p.narrowv3.mp4', 'https://cmsvideo-1505413267.cos.ap-chengdu.myqcloud.com/video-cover/20261105/1d92ef934d70_618400130.720p.narrowv3.jpg', 4, '第4集', 114, 'ok', '', '2026-11-05 23:45:21', '2026-11-05 23:45:21');
INSERT INTO `up_item` VALUES (7, 6, '618400137.720p.narrowv3.mp4', 'video/1/20261105/278952637492_618400137.720p.narrowv3.mp4', 'https://cmsvideo-1505413267.cos.ap-chengdu.myqcloud.com/video-cover/20261105/befb009890d5_618400137.720p.narrowv3.jpg', 3, '第3集', 87, 'ok', '', '2026-11-05 23:51:51', '2026-11-05 23:51:51');
INSERT INTO `up_item` VALUES (8, 7, '618400130.720p.narrowv3.mp4', 'video/1/20261108/75fdfec6c42e_618400130.720p.narrowv3.mp4', 'http://localhost:5173/video-cover/20261108/20c13dad0d67_618400130.720p.narrowv3.jpg', 1, '第1集', 114, 'ok', '', '2026-11-08 19:20:43', '2026-11-08 19:20:44');
INSERT INTO `up_item` VALUES (9, 7, '618400133.720p.narrowv3.mp4', 'video/1/20261108/584a4c98c36a_618400133.720p.narrowv3.mp4', 'http://localhost:5173/video-cover/20261108/385b61187a12_618400133.720p.narrowv3.jpg', 2, '第2集', 86, 'ok', '', '2026-11-08 19:20:44', '2026-11-08 19:20:44');
INSERT INTO `up_item` VALUES (10, 7, '618400137.720p.narrowv3.mp4', 'video/1/20261108/7c52664dc549_618400137.720p.narrowv3.mp4', 'http://localhost:5173/video-cover/20261108/b04949293094_618400137.720p.narrowv3.jpg', 3, '第3集', 87, 'ok', '', '2026-11-08 19:20:44', '2026-11-08 19:20:44');
INSERT INTO `up_item` VALUES (11, 8, '618400130.720p.narrowv3.mp4', 'video/1/20261108/2ab570c13a15_618400130.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/a2720c3a17d0_618400130.720p.narrowv3.jpg', 1, '第1集', 114, 'ok', '', '2026-11-08 20:03:08', '2026-11-08 20:03:08');
INSERT INTO `up_item` VALUES (12, 8, '618400133.720p.narrowv3.mp4', 'video/1/20261108/4a68ad03144b_618400133.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/5ed19a3ec2de_618400133.720p.narrowv3.jpg', 2, '第2集', 86, 'ok', '', '2026-11-08 20:03:08', '2026-11-08 20:03:08');
INSERT INTO `up_item` VALUES (13, 8, '618400137.720p.narrowv3.mp4', 'video/1/20261108/edc1a11d7134_618400137.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/992b4b207a6a_618400137.720p.narrowv3.jpg', 3, '第3集', 87, 'ok', '', '2026-11-08 20:03:08', '2026-11-08 20:03:08');
INSERT INTO `up_item` VALUES (14, 9, '618400130.720p.narrowv3.mp4', 'video/1/20261108/abe7652f0fd9_618400130.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/4cf21e6d3334_618400130.720p.narrowv3.jpg', 1, '第1集', 114, 'ok', '', '2026-11-08 20:08:02', '2026-11-08 20:08:02');
INSERT INTO `up_item` VALUES (15, 9, '618400133.720p.narrowv3.mp4', 'video/1/20261108/f385370e729d_618400133.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/4dc4dbac8006_618400133.720p.narrowv3.jpg', 2, '第2集', 86, 'ok', '', '2026-11-08 20:08:02', '2026-11-08 20:08:02');
INSERT INTO `up_item` VALUES (16, 9, '618400137.720p.narrowv3.mp4', 'video/1/20261108/9118474ac2f0_618400137.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/0afd9445590e_618400137.720p.narrowv3.jpg', 3, '第3集', 87, 'ok', '', '2026-11-08 20:08:02', '2026-11-08 20:08:02');

-- ----------------------------
-- Table structure for up_job
-- ----------------------------
DROP TABLE IF EXISTS `up_job`;
CREATE TABLE `up_job`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `series_id` bigint NOT NULL,
  `total` int NOT NULL DEFAULT 0,
  `ok_num` int NOT NULL DEFAULT 0,
  `fail_num` int NOT NULL DEFAULT 0,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin`(`admin_id` ASC) USING BTREE,
  INDEX `idx_series`(`series_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of up_job
-- ----------------------------
INSERT INTO `up_job` VALUES (1, 1, 1, 1, 1, 0, 'done', '2026-11-05 23:22:49', '2026-11-05 23:22:49');
INSERT INTO `up_job` VALUES (2, 1, 1, 2, 2, 0, 'done', '2026-11-05 23:23:59', '2026-11-05 23:23:59');
INSERT INTO `up_job` VALUES (3, 1, 1, 1, 1, 0, 'done', '2026-11-05 23:28:20', '2026-11-05 23:28:20');
INSERT INTO `up_job` VALUES (4, 1, 1, 1, 1, 0, 'done', '2026-11-05 23:41:53', '2026-11-05 23:41:53');
INSERT INTO `up_job` VALUES (5, 1, 1, 1, 1, 0, 'done', '2026-11-05 23:45:21', '2026-11-05 23:45:21');
INSERT INTO `up_job` VALUES (6, 1, 1, 1, 1, 0, 'done', '2026-11-05 23:51:51', '2026-11-05 23:51:51');
INSERT INTO `up_job` VALUES (7, 1, 1, 3, 3, 0, 'done', '2026-11-08 19:20:43', '2026-11-08 19:20:43');
INSERT INTO `up_job` VALUES (8, 1, 1, 3, 3, 0, 'done', '2026-11-08 20:03:08', '2026-11-08 20:03:08');
INSERT INTO `up_job` VALUES (9, 1, 1, 3, 3, 0, 'done', '2026-11-08 20:08:02', '2026-11-08 20:08:02');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'zhangsan', '$2a$10$4oHo7OKA4pL.Xw0T0ShLF.4EpjVudgAKCVPcbd.6hpg/qOThGi5Iy', '??', '', 1, '2026-11-05 21:16:38', '2026-11-06 17:55:43');
INSERT INTO `user` VALUES (2, 'hjm123', '$2a$10$hcNLV.oUIyTGkfg5w0S5ue4g8Zxdk/axr6IRf7k/qDa1jsZQiCCOO', '哈基米', 'https://cmsvideo-1505413267.cos.ap-chengdu.myqcloud.com/avatar/2/20261106/d2b5b5abf9b0_微信图片_20230728151119.jpg', 1, '2026-11-06 12:16:17', '2026-11-06 12:16:17');

-- ----------------------------
-- Table structure for v_like
-- ----------------------------
DROP TABLE IF EXISTS `v_like`;
CREATE TABLE `v_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_video`(`video_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of v_like
-- ----------------------------

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `series_id` bigint NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `ep_no` int NOT NULL,
  `cos_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cover` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `duration` int NOT NULL DEFAULT 0,
  `like_count` bigint NOT NULL DEFAULT 0,
  `play_count` bigint NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_series_ep`(`series_id` ASC, `ep_no` ASC) USING BTREE,
  INDEX `idx_series`(`series_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video
-- ----------------------------
INSERT INTO `video` VALUES (12, 1, '第1集', 1, 'video/1/20261108/abe7652f0fd9_618400130.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/4cf21e6d3334_618400130.720p.narrowv3.jpg', 114, 0, 1, 1, '2026-11-08 20:08:19', '2026-11-08 20:08:19');
INSERT INTO `video` VALUES (13, 1, '第2集', 2, 'video/1/20261108/f385370e729d_618400133.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/4dc4dbac8006_618400133.720p.narrowv3.jpg', 86, 0, 0, 1, '2026-11-08 20:08:19', '2026-11-08 20:08:19');
INSERT INTO `video` VALUES (14, 1, '第3集', 3, 'video/1/20261108/9118474ac2f0_618400137.720p.narrowv3.mp4', 'https://pub-51b67f5887ee4dc388ed9160b08407da.r2.dev/video-cover/20261108/0afd9445590e_618400137.720p.narrowv3.jpg', 87, 0, 0, 1, '2026-11-08 20:08:19', '2026-11-08 20:08:19');

-- ----------------------------
-- Table structure for watch_log
-- ----------------------------
DROP TABLE IF EXISTS `watch_log`;
CREATE TABLE `watch_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `series_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  `progress` int NOT NULL DEFAULT 0,
  `duration` int NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_user_series`(`user_id` ASC, `series_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of watch_log
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

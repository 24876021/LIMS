/*
 Navicat Premium Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80406
 Source Host           : localhost:3306
 Source Schema         : shiyanshi

 Target Server Type    : MySQL
 Target Server Version : 80406
 File Encoding         : 65001

 Date: 09/06/2026 22:51:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for attendance_activity
-- ----------------------------
DROP TABLE IF EXISTS `attendance_activity`;
CREATE TABLE `attendance_activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '状态：active/completed/cancelled',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `remark` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注（存储参与人员ID列表）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `fk_attendance_activity_creator` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤活动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of attendance_activity
-- ----------------------------
INSERT INTO `attendance_activity` VALUES (17, '多个省份', '2026-06-01 13:39:43', '2026-06-01 14:09:43', 'completed', 1, '2026-06-01 13:39:43', '1');
INSERT INTO `attendance_activity` VALUES (18, '现场v下', '2026-06-01 13:40:08', '2026-06-01 14:10:08', 'completed', 1, '2026-06-01 13:40:08', '1');
INSERT INTO `attendance_activity` VALUES (19, '23424', '2026-06-01 13:59:12', '2026-06-01 14:29:12', 'completed', 1, '2026-06-01 13:59:13', '1');
INSERT INTO `attendance_activity` VALUES (20, '山东分公司', '2026-06-01 13:59:38', '2026-06-01 14:29:38', 'completed', 1, '2026-06-01 13:59:38', '1');
INSERT INTO `attendance_activity` VALUES (21, '胜多负少·', '2026-06-01 17:39:00', '2026-06-01 18:09:00', 'completed', 1, '2026-06-01 17:39:00', '1,2');
INSERT INTO `attendance_activity` VALUES (22, '4任何', '2026-06-01 19:44:14', '2026-06-01 20:14:14', 'completed', 1, '2026-06-01 19:44:14', '1,2');
INSERT INTO `attendance_activity` VALUES (24, '124312', '2026-06-01 21:46:48', '2026-06-01 22:16:48', 'completed', 1, '2026-06-01 21:46:49', '1,2');
INSERT INTO `attendance_activity` VALUES (25, '1231', '2026-06-02 01:41:42', '2026-06-02 02:11:42', 'completed', 1, '2026-06-02 01:41:42', '1,2');
INSERT INTO `attendance_activity` VALUES (26, 'hgjh', '2026-06-02 02:00:31', '2026-06-02 02:30:31', 'completed', 1, '2026-06-02 02:00:32', '1,2');
INSERT INTO `attendance_activity` VALUES (27, 'asdas', '2026-06-02 04:02:13', '2026-06-02 04:32:13', 'completed', 1, '2026-06-02 04:02:13', '1,2');
INSERT INTO `attendance_activity` VALUES (28, 'gvdfsxfdd', '2026-06-02 12:58:07', '2026-06-02 13:28:07', 'completed', 1, '2026-06-02 12:58:08', '1,2');
INSERT INTO `attendance_activity` VALUES (29, '123123', '2026-06-02 13:31:23', '2026-06-02 14:01:23', 'completed', 1, '2026-06-02 13:31:24', '1,2');
INSERT INTO `attendance_activity` VALUES (30, '2424', '2026-06-02 13:37:09', '2026-06-02 14:07:09', 'completed', 1, '2026-06-02 13:37:10', '2');
INSERT INTO `attendance_activity` VALUES (31, '23424', '2026-06-02 13:37:57', '2026-06-02 14:07:57', 'completed', 1, '2026-06-02 13:37:57', '2');
INSERT INTO `attendance_activity` VALUES (32, '231231', '2026-06-02 17:37:59', '2026-06-02 18:07:59', 'completed', 1, '2026-06-02 17:38:00', '1,2,4');
INSERT INTO `attendance_activity` VALUES (33, '234523', '2026-06-02 21:58:34', '2026-06-02 22:28:34', 'completed', 1, '2026-06-02 21:58:34', '2');
INSERT INTO `attendance_activity` VALUES (34, '23423', '2026-06-02 22:07:38', '2026-06-02 22:37:38', 'completed', 1, '2026-06-02 22:07:39', '2');
INSERT INTO `attendance_activity` VALUES (35, '2134213', '2026-06-02 22:21:20', '2026-06-02 22:51:20', 'completed', 1, '2026-06-02 22:21:20', '2,4,14');
INSERT INTO `attendance_activity` VALUES (36, '12312', '2026-06-02 22:34:47', '2026-06-02 23:04:47', 'completed', 1, '2026-06-02 22:34:47', '2,4,14');
INSERT INTO `attendance_activity` VALUES (37, '3412312', '2026-06-02 22:58:18', '2026-06-02 23:28:18', 'completed', 1, '2026-06-02 22:58:18', '1,2,4');
INSERT INTO `attendance_activity` VALUES (38, '1231', '2026-06-02 23:37:12', '2026-06-03 00:07:12', 'completed', 1, '2026-06-02 23:37:12', '2');

-- ----------------------------
-- Table structure for attendance_record
-- ----------------------------
DROP TABLE IF EXISTS `attendance_record`;
CREATE TABLE `attendance_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `personnel_id` bigint NOT NULL COMMENT '人员ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `sign_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '状态：normal/late/early_leave/absent',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_personnel_id`(`personnel_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_attendance_record_activity` FOREIGN KEY (`activity_id`) REFERENCES `attendance_activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_attendance_record_personnel` FOREIGN KEY (`personnel_id`) REFERENCES `personnel` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of attendance_record
-- ----------------------------
INSERT INTO `attendance_record` VALUES (1, 1, 11, '2026-06-01 13:00:26', 'normal');
INSERT INTO `attendance_record` VALUES (2, 1, 12, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (3, 1, 13, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (4, 1, 14, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (5, 1, 15, '2026-06-01 13:29:10', 'normal');
INSERT INTO `attendance_record` VALUES (6, 1, 16, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (7, 1, 17, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (8, 1, 18, '2026-06-01 13:44:51', 'normal');
INSERT INTO `attendance_record` VALUES (9, 1, 20, '2026-06-01 14:05:18', 'normal');
INSERT INTO `attendance_record` VALUES (10, 1, 19, '2026-06-01 14:05:39', 'normal');
INSERT INTO `attendance_record` VALUES (11, 1, 21, '2026-06-01 17:39:16', 'normal');
INSERT INTO `attendance_record` VALUES (12, 2, 21, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (13, 1, 22, '2026-06-01 19:50:55', 'normal');
INSERT INTO `attendance_record` VALUES (14, 2, 22, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (15, 1, 23, '2026-06-01 21:22:49', 'normal');
INSERT INTO `attendance_record` VALUES (16, 2, 23, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (17, 1, 24, '2026-06-01 21:46:52', 'normal');
INSERT INTO `attendance_record` VALUES (18, 2, 24, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (19, 1, 25, '2026-06-02 01:48:14', 'normal');
INSERT INTO `attendance_record` VALUES (20, 2, 25, '2026-06-02 01:52:16', 'normal');
INSERT INTO `attendance_record` VALUES (21, 1, 26, '2026-06-02 02:04:36', 'normal');
INSERT INTO `attendance_record` VALUES (22, 2, 26, '2026-06-02 02:08:17', 'normal');
INSERT INTO `attendance_record` VALUES (23, 2, 27, '2026-06-02 04:02:26', 'normal');
INSERT INTO `attendance_record` VALUES (24, 1, 27, '2026-06-02 04:17:45', 'normal');
INSERT INTO `attendance_record` VALUES (25, 1, 28, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (26, 2, 28, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (27, 2, 29, '2026-06-02 13:33:35', 'normal');
INSERT INTO `attendance_record` VALUES (28, 1, 29, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (29, 2, 30, '2026-06-02 13:37:32', 'normal');
INSERT INTO `attendance_record` VALUES (30, 2, 31, '2026-06-02 13:38:07', 'normal');
INSERT INTO `attendance_record` VALUES (31, 1, 32, '2026-06-02 17:39:40', 'normal');
INSERT INTO `attendance_record` VALUES (32, 2, 32, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (33, 4, 32, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (34, 2, 33, '2026-06-02 22:06:43', 'normal');
INSERT INTO `attendance_record` VALUES (35, 1, 33, '2026-06-02 22:07:21', 'normal');
INSERT INTO `attendance_record` VALUES (36, 2, 34, '2026-06-02 22:07:49', 'normal');
INSERT INTO `attendance_record` VALUES (37, 2, 35, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (38, 4, 35, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (39, 14, 35, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (40, 2, 36, '2026-06-02 22:40:48', 'normal');
INSERT INTO `attendance_record` VALUES (41, 4, 36, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (42, 14, 36, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (43, 1, 37, '2026-06-02 22:58:22', 'normal');
INSERT INTO `attendance_record` VALUES (44, 2, 37, '2026-06-02 22:58:30', 'normal');
INSERT INTO `attendance_record` VALUES (45, 4, 37, NULL, 'absent');
INSERT INTO `attendance_record` VALUES (46, 2, 38, '2026-06-02 23:37:23', 'normal');

-- ----------------------------
-- Table structure for authority
-- ----------------------------
DROP TABLE IF EXISTS `authority`;
CREATE TABLE `authority`  (
  `authority_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `authority_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限标识',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限描述',
  PRIMARY KEY (`authority_id`) USING BTREE,
  UNIQUE INDEX `uk_authority_name`(`authority_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of authority
-- ----------------------------
INSERT INTO `authority` VALUES (1, 'resource:all', '资源管理所有权限');
INSERT INTO `authority` VALUES (2, 'resource:add', '资源新增权限');
INSERT INTO `authority` VALUES (3, 'resource:remove', '资源删除权限');
INSERT INTO `authority` VALUES (4, 'resource:set', '资源设置权限');
INSERT INTO `authority` VALUES (5, 'resource:get', '资源查看权限');
INSERT INTO `authority` VALUES (6, 'equipment:all', '设备管理所有权限');
INSERT INTO `authority` VALUES (7, 'equipment:add', '设备新增权限');
INSERT INTO `authority` VALUES (8, 'equipment:remove', '设备删除权限');
INSERT INTO `authority` VALUES (9, 'equipment:set', '设备设置权限');
INSERT INTO `authority` VALUES (10, 'equipment:get', '设备查看权限');
INSERT INTO `authority` VALUES (11, 'personnel:all', '人员管理所有权限');
INSERT INTO `authority` VALUES (12, 'personnel:add', '人员新增权限');
INSERT INTO `authority` VALUES (13, 'personnel:remove', '人员删除权限');
INSERT INTO `authority` VALUES (14, 'personnel:set', '人员设置权限');
INSERT INTO `authority` VALUES (15, 'personnel:get', '人员查看权限');
INSERT INTO `authority` VALUES (16, 'safety:all', '安全管理所有权限');
INSERT INTO `authority` VALUES (17, 'safety:add', '安全新增权限');
INSERT INTO `authority` VALUES (18, 'safety:remove', '安全删除权限');
INSERT INTO `authority` VALUES (19, 'safety:set', '安全设置权限');
INSERT INTO `authority` VALUES (20, 'safety:get', '安全查看权限');
INSERT INTO `authority` VALUES (21, 'reservation:all', '预约管理所有权限');
INSERT INTO `authority` VALUES (22, 'reservation:add', '预约新增权限');
INSERT INTO `authority` VALUES (23, 'reservation:remove', '预约删除权限');
INSERT INTO `authority` VALUES (24, 'reservation:set', '预约设置权限');
INSERT INTO `authority` VALUES (25, 'reservation:get', '预约查看权限');
INSERT INTO `authority` VALUES (26, 'report:all', '报告管理所有权限');
INSERT INTO `authority` VALUES (27, 'report:add', '报告新增权限');
INSERT INTO `authority` VALUES (28, 'report:remove', '报告删除权限');
INSERT INTO `authority` VALUES (29, 'report:set', '报告设置权限');
INSERT INTO `authority` VALUES (30, 'report:get', '报告查看权限');
INSERT INTO `authority` VALUES (31, 'dashboard:all', '仪表盘所有权限');
INSERT INTO `authority` VALUES (32, 'dashboard:get', '仪表盘查看权限');

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `department_id` int NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `department_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `parent_id` int NULL DEFAULT NULL COMMENT '上级部门ID',
  `leader_user_id` bigint NULL DEFAULT NULL COMMENT '部门负责人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`department_id`) USING BTREE,
  UNIQUE INDEX `uk_department_name`(`department_name` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_leader`(`leader_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_departments_leader` FOREIGN KEY (`leader_user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_departments_parent` FOREIGN KEY (`parent_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门/院系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of departments
-- ----------------------------
INSERT INTO `departments` VALUES (1, '实验管理中心', NULL, NULL, '2026-06-01 17:31:31');
INSERT INTO `departments` VALUES (2, '计算机工程学院', 1, 1, '2026-06-01 17:32:17');

-- ----------------------------
-- Table structure for equipment
-- ----------------------------
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备编号',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备分类',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备位置',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'normal' COMMENT '状态：normal/borrowed/maintenance/scrapped',
  `purchase_date` datetime NULL DEFAULT NULL COMMENT '购买日期',
  `price` decimal(12, 2) NULL DEFAULT NULL COMMENT '价格',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备台账表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of equipment
-- ----------------------------
INSERT INTO `equipment` VALUES (6, '显微镜', 'E001', '光学仪器', 'A101', 'normal', '2026-06-07 00:00:00', 1000.00, '高科技显微镜！', '2026-06-07 15:44:26', '2026-06-07 15:44:26');
INSERT INTO `equipment` VALUES (7, '离心机', 'E002', '分析仪器', 'A102', 'normal', '2026-06-07 00:00:00', 2000.00, '超级离心机！', '2026-06-07 16:09:15', '2026-06-07 16:09:15');
INSERT INTO `equipment` VALUES (8, '光谱仪', 'E003', '光谱仪器', 'B201', 'normal', '2026-06-07 00:00:00', 3000.00, '无敌光谱仪！', '2026-06-07 16:10:03', '2026-06-07 16:10:03');

-- ----------------------------
-- Table structure for equipment_maintenance_record
-- ----------------------------
DROP TABLE IF EXISTS `equipment_maintenance_record`;
CREATE TABLE `equipment_maintenance_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `reporter_id` bigint NULL DEFAULT NULL COMMENT '报修人ID',
  `fault_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '故障描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '维修状态：pending/in_progress/completed',
  `repair_person` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维修人',
  `repair_cost` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维修费用',
  `report_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `repair_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '维修结果',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  CONSTRAINT `fk_equipment_maintenance_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_equipment_maintenance_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备维修记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of equipment_maintenance_record
-- ----------------------------
INSERT INTO `equipment_maintenance_record` VALUES (1, 1, 1, '12313', 'completed', '23', '123', '2026-06-01 18:31:29', '2026-06-01 18:35:24', '维修完成');
INSERT INTO `equipment_maintenance_record` VALUES (2, 2, 1, '3123', 'completed', '123', '123', '2026-06-01 18:32:59', '2026-06-01 18:35:31', '维修完成');
INSERT INTO `equipment_maintenance_record` VALUES (3, 6, 1, '123123', 'completed', '123123123', '0', '2026-06-09 13:59:16', '2026-06-09 14:00:18', '维修完成');

-- ----------------------------
-- Table structure for equipment_usage_record
-- ----------------------------
DROP TABLE IF EXISTS `equipment_usage_record`;
CREATE TABLE `equipment_usage_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `user_id` bigint NOT NULL COMMENT '使用人ID',
  `borrow_time` datetime NULL DEFAULT NULL COMMENT '借出时间',
  `return_time` datetime NULL DEFAULT NULL COMMENT '归还时间',
  `purpose` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用途',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'borrowing' COMMENT '状态：borrowing/returned',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_equipment_usage_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_equipment_usage_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备使用记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of equipment_usage_record
-- ----------------------------
INSERT INTO `equipment_usage_record` VALUES (1, 1, 1, '2026-06-01 02:26:35', '2026-06-01 02:31:22', '123', 'returned');
INSERT INTO `equipment_usage_record` VALUES (2, 1, 1, '2026-06-01 03:05:19', '2026-06-01 14:38:46', '3423', 'returned');
INSERT INTO `equipment_usage_record` VALUES (3, 1, 1, '2026-06-01 14:42:04', '2026-06-01 18:31:24', '吃饭', 'returned');
INSERT INTO `equipment_usage_record` VALUES (4, 2, 1, '2026-06-01 18:30:42', '2026-06-01 18:32:55', '23432', 'returned');
INSERT INTO `equipment_usage_record` VALUES (5, 1, 1, '2026-06-01 19:49:24', '2026-06-01 19:50:15', 'dtyhu', 'returned');
INSERT INTO `equipment_usage_record` VALUES (6, 1, 1, '2026-06-01 20:02:25', '2026-06-02 04:05:23', '557788', 'returned');
INSERT INTO `equipment_usage_record` VALUES (7, 1, 1, '2026-06-02 04:08:05', '2026-06-02 04:16:13', '12312', 'returned');
INSERT INTO `equipment_usage_record` VALUES (8, 1, 1, '2026-06-02 04:16:27', '2026-06-02 12:59:28', '1232', 'returned');
INSERT INTO `equipment_usage_record` VALUES (9, 1, 1, '2026-06-02 13:30:46', '2026-06-02 23:47:05', '123', 'returned');
INSERT INTO `equipment_usage_record` VALUES (10, 2, 1, '2026-06-02 13:31:02', '2026-06-02 23:47:36', '12313', 'returned');
INSERT INTO `equipment_usage_record` VALUES (11, 1, 2, '2026-06-02 23:47:58', '2026-06-02 23:48:05', '12312', 'returned');
INSERT INTO `equipment_usage_record` VALUES (12, 6, 1, '2026-06-08 18:25:36', '2026-06-08 18:35:29', '1233', 'returned');
INSERT INTO `equipment_usage_record` VALUES (13, 7, 2, '2026-06-08 18:30:05', '2026-06-08 18:52:05', '2312', 'returned');

-- ----------------------------
-- Table structure for lab
-- ----------------------------
DROP TABLE IF EXISTS `lab`;
CREATE TABLE `lab`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实验室名称',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实验室位置',
  `capacity` int NULL DEFAULT 0 COMMENT '容量',
  `equipment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备配置（JSON）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'available' COMMENT '状态：available/reserved/maintenance',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '实验室信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of lab
-- ----------------------------
INSERT INTO `lab` VALUES (1, '生物实验室 A501', '北校区', 20, '[\"显微镜\"]', 'available', '啊实打实大大', '2026-06-01 01:55:48', '2026-06-01 22:13:34');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者用户ID（NULL表示系统通知）',
  `receiver_id` bigint NOT NULL COMMENT '接收者用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'info' COMMENT '消息类型',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `fk_notification_sender`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `fk_notification_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_notification_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 250 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户消息通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (26, NULL, 123, '采购入库成功', '物资《13123》采购入库成功，数量 +2342个，当前库存 2574个', 'info', 0, '2026-06-02 13:49:09');
INSERT INTO `notification` VALUES (27, NULL, 123, '采购入库成功', '物资《13123》采购入库成功，数量 +123个，当前库存 2697个', 'info', 0, '2026-06-02 13:49:42');
INSERT INTO `notification` VALUES (56, NULL, 3, '角色权限变更', '管理员已更新您的角色权限，当前角色：学生，如权限未生效请刷新页面。', 'info', 1, '2026-06-02 16:50:57');
INSERT INTO `notification` VALUES (67, NULL, 4, '认证审核未通过', '123123，您的身份认证未通过审核，当前状态：rejected，请联系管理员。', 'warning', 1, '2026-06-02 17:11:17');
INSERT INTO `notification` VALUES (69, NULL, 4, '认证审核通过', '恭喜，12312，您的身份认证已通过审核！', 'success', 1, '2026-06-02 17:13:46');
INSERT INTO `notification` VALUES (71, NULL, 4, '认证审核未通过', '123123，您的身份认证未通过审核，当前状态：rejected，请联系管理员。', 'warning', 1, '2026-06-02 17:15:43');
INSERT INTO `notification` VALUES (73, NULL, 4, '认证审核未通过', '123123，很遗憾，您的身份认证未通过审核，请联系管理员了解详情。', 'warning', 1, '2026-06-02 17:16:33');
INSERT INTO `notification` VALUES (75, NULL, 4, '认证审核通过', '恭喜，1212，您的身份认证已通过审核！', 'success', 1, '2026-06-02 17:20:55');
INSERT INTO `notification` VALUES (78, NULL, 3, '考勤通知', '新的考勤活动「231231」已发布，请及时签到（06-02 17:37 - 06-02 18:07）', 'info', 0, '2026-06-02 17:38:00');
INSERT INTO `notification` VALUES (80, NULL, 4, '安全考试未通过', '您本次安全考试未通过，得分：33/100，请加强学习后重新参加考试。', 'warning', 1, '2026-06-02 18:07:22');
INSERT INTO `notification` VALUES (83, NULL, 3, '考勤缺勤提醒', '考勤活动「231231」已结束，您未签到，已记为缺勤。', 'warning', 0, '2026-06-02 18:08:00');
INSERT INTO `notification` VALUES (85, NULL, 4, '认证审核通过', '恭喜，阿斯顿，您的身份认证已通过审核！', 'success', 1, '2026-06-02 18:09:05');
INSERT INTO `notification` VALUES (86, NULL, 4, '安全考试未通过', '您本次安全考试未通过，得分：33/100，请加强学习后重新参加考试。', 'warning', 1, '2026-06-02 18:09:30');
INSERT INTO `notification` VALUES (118, NULL, 3, '考勤通知', '新的考勤活动「2134213」已发布，请及时签到（06-02 22:21 - 06-02 22:51）', 'info', 0, '2026-06-02 22:21:20');
INSERT INTO `notification` VALUES (119, NULL, 4, '考勤通知', '新的考勤活动「2134213」已发布，请及时签到（06-02 22:21 - 06-02 22:51）', 'info', 0, '2026-06-02 22:21:20');
INSERT INTO `notification` VALUES (123, NULL, 3, '考勤缺勤提醒', '考勤活动「2134213」已结束，您未签到，已记为缺勤。', 'warning', 0, '2026-06-02 22:34:40');
INSERT INTO `notification` VALUES (124, NULL, 4, '考勤缺勤提醒', '考勤活动「2134213」已结束，您未签到，已记为缺勤。', 'warning', 0, '2026-06-02 22:34:40');
INSERT INTO `notification` VALUES (127, NULL, 3, '考勤通知', '新的考勤活动「12312」已发布，请及时签到（06-02 22:34 - 06-02 23:04）', 'info', 0, '2026-06-02 22:34:47');
INSERT INTO `notification` VALUES (128, NULL, 4, '考勤通知', '新的考勤活动「12312」已发布，请及时签到（06-02 22:34 - 06-02 23:04）', 'info', 0, '2026-06-02 22:34:47');
INSERT INTO `notification` VALUES (131, NULL, 3, '考勤缺勤提醒', '考勤活动「12312」已结束，您未签到，已记为缺勤。', 'warning', 0, '2026-06-02 22:57:34');
INSERT INTO `notification` VALUES (132, NULL, 4, '考勤缺勤提醒', '考勤活动「12312」已结束，您未签到，已记为缺勤。', 'warning', 0, '2026-06-02 22:57:35');
INSERT INTO `notification` VALUES (136, NULL, 3, '考勤通知', '新的考勤活动「3412312」已发布，请及时签到（06-02 22:58 - 06-02 23:28）', 'info', 0, '2026-06-02 22:58:18');
INSERT INTO `notification` VALUES (146, NULL, 3, '考勤缺勤提醒', '考勤活动「3412312」已结束，您未签到，已记为缺勤。', 'warning', 0, '2026-06-02 23:13:31');
INSERT INTO `notification` VALUES (207, NULL, 6, '认证审核未通过', '6-段七，很遗憾，您的身份认证未通过审核，请联系管理员了解详情。', 'warning', 1, '2026-06-03 02:56:51');
INSERT INTO `notification` VALUES (210, NULL, 6, '认证审核通过', '恭喜，6-段七，您的身份认证已通过审核！', 'success', 0, '2026-06-03 02:57:40');
INSERT INTO `notification` VALUES (214, NULL, 2, '角色权限变更', '管理员已更新您的角色权限，当前角色：学生，如权限未生效请刷新页面。', 'info', 1, '2026-06-07 15:06:04');
INSERT INTO `notification` VALUES (217, NULL, 2, '角色权限变更', '管理员已更新您的角色权限，当前角色：系统管理员，如权限未生效请刷新页面。', 'info', 1, '2026-06-07 15:10:09');
INSERT INTO `notification` VALUES (218, 2, 2, '人员信息更新', '张三 的信息已更新', 'personnel', 1, '2026-06-07 15:13:00');
INSERT INTO `notification` VALUES (219, NULL, 2, '角色权限变更', '管理员已更新您的角色权限，当前角色：学生，如权限未生效请刷新页面。', 'info', 1, '2026-06-07 15:13:00');
INSERT INTO `notification` VALUES (222, NULL, 2, '设备借用成功', '您已成功借用设备：离心机', 'system', 1, '2026-06-08 18:30:05');
INSERT INTO `notification` VALUES (225, NULL, 2, '设备归还成功', '您已成功归还设备：离心机', 'system', 1, '2026-06-08 18:52:05');
INSERT INTO `notification` VALUES (228, NULL, 23, '采购入库成功', '物资《13123》采购入库成功，数量 +12个，当前库存 2832个', 'info', 0, '2026-06-08 18:53:47');
INSERT INTO `notification` VALUES (230, NULL, 2, '预约已拒绝', '您的预约已被拒绝', 'system', 1, '2026-06-08 18:56:04');
INSERT INTO `notification` VALUES (234, NULL, 2, '预约申请已提交', '您的预约申请已提交，请等待管理员审批。', 'info', 0, '2026-06-08 19:27:23');
INSERT INTO `notification` VALUES (235, 2, 2, '新建预约', '张三 提交了 生物实验室 A501 的预约申请', 'reservation', 0, '2026-06-08 19:27:23');
INSERT INTO `notification` VALUES (236, 1, 2, '预约审批通过', '您的预约已通过审批', 'system', 0, '2026-06-08 19:27:55');
INSERT INTO `notification` VALUES (238, 1, 2, '你好', '56777', 'info', 0, '2026-06-08 19:33:44');
INSERT INTO `notification` VALUES (241, NULL, 1, '设备维修完成', '设备《显微镜》已维修完成，可以正常使用，维修结果：维修完成', 'success', 0, '2026-06-09 14:00:18');
INSERT INTO `notification` VALUES (242, 1, 1, '维修完成', '显微镜 已完成维修，现在可用', 'maintenance', 0, '2026-06-09 14:00:19');
INSERT INTO `notification` VALUES (243, 1, 1, '角色权限更新', '角色权限已更新', 'personnel', 0, '2026-06-09 14:02:39');
INSERT INTO `notification` VALUES (244, 1, 1, '角色权限更新', '角色权限已更新', 'personnel', 0, '2026-06-09 14:02:47');
INSERT INTO `notification` VALUES (245, NULL, 1, '采购入库成功', '物资《13123》采购入库成功，数量 +12个，当前库存 2856个', 'info', 0, '2026-06-09 14:05:56');
INSERT INTO `notification` VALUES (246, 1, 1, '采购入库', '13123 入库 12个，单价 ¥121', 'purchase', 0, '2026-06-09 14:05:57');
INSERT INTO `notification` VALUES (247, 1, 2, '123', '123', 'info', 0, '2026-06-09 14:06:43');
INSERT INTO `notification` VALUES (248, NULL, 1, '采购入库成功', '物资《显微镜》采购入库成功，数量 +123123，当前库存 345123', 'info', 0, '2026-06-09 22:28:22');
INSERT INTO `notification` VALUES (249, 1, 1, '采购入库', '显微镜 入库 123123，单价 ¥123', 'purchase', 0, '2026-06-09 22:28:22');

-- ----------------------------
-- Table structure for personnel
-- ----------------------------
DROP TABLE IF EXISTS `personnel`;
CREATE TABLE `personnel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `employee_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位/角色',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联系统用户ID',
  `department_id` int NULL DEFAULT NULL COMMENT '关联部门ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_no`(`employee_no` ASC) USING BTREE,
  INDEX `fk_personnel_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_personnel_department`(`department_id` ASC) USING BTREE,
  CONSTRAINT `fk_personnel_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_personnel_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人员信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of personnel
-- ----------------------------
INSERT INTO `personnel` VALUES (1, '刘翰霖', 'PT001', '男', '18282648320', '18282648320@163.com', '实验管理中心', '管理员', 'active', '2026-06-01 10:58:08', '2026-06-02 16:41:16', 1, 1);
INSERT INTO `personnel` VALUES (2, '张三', 'PT002', '男', '174084', '123123123@qq.com', '计算机工程学院', '25计科1班学生', 'active', '2026-06-01 14:37:56', '2026-06-07 15:13:00', 2, 2);
INSERT INTO `personnel` VALUES (4, '流萤', 'PT003', '男', '121332123123', '1231312@163.com', '实验管理中心', '清洁工', 'active', '2026-06-02 16:49:45', '2026-06-02 16:50:57', 3, 1);
INSERT INTO `personnel` VALUES (14, '阿斯顿', 'PT005', '男', '2131231231', '123123@213', NULL, '撒大大', 'active', '2026-06-02 18:08:52', '2026-06-02 18:09:05', 4, 2);
INSERT INTO `personnel` VALUES (15, '12321', '123', '男', '8282648320', '18282648320@163.com', '实验管理中心', '1231', 'active', '2026-06-02 23:08:16', '2026-06-08 18:52:28', NULL, 1);

-- ----------------------------
-- Table structure for purchase_record
-- ----------------------------
DROP TABLE IF EXISTS `purchase_record`;
CREATE TABLE `purchase_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `resource_id` bigint NOT NULL COMMENT '物资ID',
  `quantity` int NOT NULL COMMENT '采购数量',
  `unit_price` decimal(12, 2) NULL DEFAULT NULL COMMENT '采购单价',
  `purchaser_id` bigint NULL DEFAULT NULL COMMENT '采购人ID',
  `supplier` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商',
  `purchase_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采购时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_purchaser_id`(`purchaser_id` ASC) USING BTREE,
  CONSTRAINT `fk_purchase_purchaser` FOREIGN KEY (`purchaser_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_purchase_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '采购记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of purchase_record
-- ----------------------------
INSERT INTO `purchase_record` VALUES (1, 1, 123, 12.00, 123, NULL, '2026-06-01 03:18:15');
INSERT INTO `purchase_record` VALUES (2, 1, 123, 123.00, 123123, NULL, '2026-06-01 03:19:44');
INSERT INTO `purchase_record` VALUES (3, 1, 2, 234.00, 234, NULL, '2026-06-01 13:46:02');
INSERT INTO `purchase_record` VALUES (4, 1, 7, 0.05, 355, '腾讯', '2026-06-01 20:27:41');
INSERT INTO `purchase_record` VALUES (5, 1, 2342, 123.00, 123, '123', '2026-06-02 13:49:09');
INSERT INTO `purchase_record` VALUES (6, 1, 123, 123.00, 123, '123', '2026-06-02 13:49:42');
INSERT INTO `purchase_record` VALUES (7, 1, 123, 12.00, 1, '12323', '2026-06-02 13:50:00');
INSERT INTO `purchase_record` VALUES (8, 1, 12, 12.00, 23, '123', '2026-06-08 18:53:47');
INSERT INTO `purchase_record` VALUES (10, 1, 12, 121.00, 1, '12', '2026-06-09 14:05:56');
INSERT INTO `purchase_record` VALUES (12, 2, 123, 123.00, 1, '123', '2026-06-09 22:28:22');

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '报告内容',
  `user_id` bigint NOT NULL COMMENT '提交人ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
  `score` int NULL DEFAULT NULL COMMENT '评分',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评语',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `attachment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '附件URL（多个用逗号分隔，格式：url|filename）',
  `course` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课题名称',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `fk_report_reviewer`(`reviewer_id` ASC) USING BTREE,
  CONSTRAINT `fk_report_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_report_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '实验报告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of report
-- ----------------------------
INSERT INTO `report` VALUES (1, '123', '12312', 1, 'approved', 12, '不及格', NULL, '2026-06-01 03:15:18', '2026-06-01 03:17:27', NULL, NULL);
INSERT INTO `report` VALUES (2, '简历', '234234234', 1, 'rejected', NULL, '2342', NULL, '2026-06-01 16:29:12', '2026-06-01 16:52:37', '/uploads/attachment/report_1780302540432_7337.docx|刘翰霖+后端开发.docx', NULL);
INSERT INTO `report` VALUES (3, '实验4', '1234123123', 1, 'approved', 12, '不及格', NULL, '2026-06-01 16:48:00', '2026-06-01 18:35:05', '/uploads/attachment/report_1780303678250_5205.sql|authority.sql', NULL);
INSERT INTO `report` VALUES (4, '12312', '123123', 1, 'approved', 12, 'sadfasd', NULL, '2026-06-01 16:52:25', '2026-06-01 22:20:29', NULL, NULL);
INSERT INTO `report` VALUES (5, '12312', '123123', 1, 'approved', 12, '123123', 1, '2026-06-01 17:03:13', '2026-06-01 23:13:53', NULL, NULL);
INSERT INTO `report` VALUES (6, '123', '123123', 1, 'approved', 12, '不及格', 1, '2026-06-01 17:07:49', '2026-06-02 02:13:21', NULL, '123123');
INSERT INTO `report` VALUES (7, 'ejdjej', 'ejekekek', 1, 'approved', 21, 'bjg', 1, '2026-06-01 20:32:07', '2026-06-02 04:17:09', NULL, 'hdheh');
INSERT INTO `report` VALUES (8, '123123', '123123', 2, 'approved', 12, '不及格', 1, '2026-06-02 13:19:29', '2026-06-02 13:20:37', NULL, '12323');

-- ----------------------------
-- Table structure for reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lab_id` bigint NOT NULL COMMENT '实验室ID',
  `user_id` bigint NOT NULL COMMENT '预约人ID',
  `start_time` datetime NOT NULL COMMENT '预约开始时间',
  `end_time` datetime NOT NULL COMMENT '预约结束时间',
  `purpose` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约目的',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/cancelled',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lab_id`(`lab_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `fk_reservation_approver`(`approver_id` ASC) USING BTREE,
  CONSTRAINT `fk_reservation_approver` FOREIGN KEY (`approver_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_lab` FOREIGN KEY (`lab_id`) REFERENCES `lab` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '实验室预约表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of reservation
-- ----------------------------
INSERT INTO `reservation` VALUES (2, 1, 2, '2026-06-12 15:51:00', '2026-06-12 14:54:00', '12312', 'approved', 1, '2026-06-01 14:52:24', NULL, '2026-06-01 14:51:51');
INSERT INTO `reservation` VALUES (3, 1, 1, '2026-06-12 19:50:00', '2026-06-12 19:50:00', '45666', 'rejected', NULL, '2026-06-01 19:48:08', NULL, '2026-06-01 19:48:00');
INSERT INTO `reservation` VALUES (4, 1, 1, '2026-06-26 20:37:00', '2026-06-26 20:37:00', 'ghsjsjs', 'approved', 1, '2026-06-01 20:34:48', NULL, '2026-06-01 20:33:39');
INSERT INTO `reservation` VALUES (5, 1, 1, '2026-06-26 00:53:00', '2026-06-26 20:54:00', 'ghjkk', 'rejected', NULL, '2026-06-01 20:50:47', '不行', '2026-06-01 20:50:25');
INSERT INTO `reservation` VALUES (6, 1, 2, '2026-06-20 13:25:00', '2026-06-20 13:27:00', '1123123', 'rejected', NULL, '2026-06-02 13:22:30', '234234', '2026-06-02 13:21:55');
INSERT INTO `reservation` VALUES (7, 1, 2, '2026-06-20 13:26:00', '2026-06-20 13:26:00', '1231', 'approved', 1, '2026-06-02 13:28:09', '123123', '2026-06-02 13:23:04');
INSERT INTO `reservation` VALUES (8, 1, 2, '2026-06-02 13:32:00', '2026-06-02 13:56:00', '12312312', 'rejected', NULL, '2026-06-02 13:27:58', '1231', '2026-06-02 13:27:33');
INSERT INTO `reservation` VALUES (9, 1, 1, '2026-06-02 23:57:00', '2026-06-02 23:59:00', '1231', 'pending', NULL, NULL, NULL, '2026-06-02 23:58:03');
INSERT INTO `reservation` VALUES (10, 1, 2, '2026-06-03 00:42:00', '2026-06-03 00:46:00', '12312312', 'rejected', NULL, '2026-06-08 18:56:04', NULL, '2026-06-03 00:42:02');
INSERT INTO `reservation` VALUES (11, 1, 2, '2026-06-08 19:26:00', '2026-06-08 19:29:00', '7777', 'approved', 1, '2026-06-08 19:27:55', NULL, '2026-06-08 19:27:23');

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物资名称',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物资分类',
  `specification` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格型号',
  `quantity` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '件' COMMENT '单位',
  `price` decimal(12, 2) NULL DEFAULT NULL COMMENT '单价',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存放位置',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资库存表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES (1, '13123', '1231', '1231', 2856, '个', 11.00, '1231', '', '2026-06-01 03:09:21', '2026-06-01 20:17:24');
INSERT INTO `resource` VALUES (2, '显微镜', '生物', '123', 345, '123', NULL, '123', NULL, '2026-06-01 14:32:00', '2026-06-01 14:32:00');
INSERT INTO `resource` VALUES (3, '微波炉', '烹饪', '完全', 122, '食品学院', NULL, 'A421', NULL, '2026-06-01 14:33:29', '2026-06-01 14:33:29');
INSERT INTO `resource` VALUES (4, '1231', '1231', '1231', 213, '123', 12.00, '213123', '4234234', '2026-06-01 23:20:57', '2026-06-01 23:20:57');
INSERT INTO `resource` VALUES (5, '盐酸', '化学试剂', '500ml', 0, '瓶', 2.00, '试剂库A', '小心使用', '2026-06-07 16:13:46', '2026-06-07 16:13:46');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '角色描述',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_name`(`role_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'admin', '系统管理员');
INSERT INTO `role` VALUES (2, 'teacher', '教师/实验员');
INSERT INTO `role` VALUES (3, 'student', '学生');
INSERT INTO `role` VALUES (4, 'Guest', '游客');

-- ----------------------------
-- Table structure for role_authority
-- ----------------------------
DROP TABLE IF EXISTS `role_authority`;
CREATE TABLE `role_authority`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `authority_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `authority_id`) USING BTREE,
  INDEX `idx_authority_id`(`authority_id` ASC) USING BTREE,
  CONSTRAINT `fk_role_authority_authority` FOREIGN KEY (`authority_id`) REFERENCES `authority` (`authority_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role_authority_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_authority
-- ----------------------------
INSERT INTO `role_authority` VALUES (1, 1);
INSERT INTO `role_authority` VALUES (2, 4);
INSERT INTO `role_authority` VALUES (2, 5);
INSERT INTO `role_authority` VALUES (3, 5);
INSERT INTO `role_authority` VALUES (1, 6);
INSERT INTO `role_authority` VALUES (2, 9);
INSERT INTO `role_authority` VALUES (2, 10);
INSERT INTO `role_authority` VALUES (3, 10);
INSERT INTO `role_authority` VALUES (1, 11);
INSERT INTO `role_authority` VALUES (1, 16);
INSERT INTO `role_authority` VALUES (2, 17);
INSERT INTO `role_authority` VALUES (2, 19);
INSERT INTO `role_authority` VALUES (2, 20);
INSERT INTO `role_authority` VALUES (3, 20);
INSERT INTO `role_authority` VALUES (1, 21);
INSERT INTO `role_authority` VALUES (3, 22);
INSERT INTO `role_authority` VALUES (2, 24);
INSERT INTO `role_authority` VALUES (2, 25);
INSERT INTO `role_authority` VALUES (3, 25);
INSERT INTO `role_authority` VALUES (1, 26);
INSERT INTO `role_authority` VALUES (3, 27);
INSERT INTO `role_authority` VALUES (2, 29);
INSERT INTO `role_authority` VALUES (2, 30);
INSERT INTO `role_authority` VALUES (3, 30);
INSERT INTO `role_authority` VALUES (1, 31);
INSERT INTO `role_authority` VALUES (2, 31);
INSERT INTO `role_authority` VALUES (3, 31);
INSERT INTO `role_authority` VALUES (4, 31);
INSERT INTO `role_authority` VALUES (3, 32);
INSERT INTO `role_authority` VALUES (4, 32);

-- ----------------------------
-- Table structure for safety_exam_record
-- ----------------------------
DROP TABLE IF EXISTS `safety_exam_record`;
CREATE TABLE `safety_exam_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `score` int NULL DEFAULT 0 COMMENT '得分',
  `total_score` int NULL DEFAULT 100 COMMENT '总分',
  `passed` tinyint(1) NULL DEFAULT 0 COMMENT '是否通过',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态：0=待完成，1=已完成',
  `exam_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '考试时间',
  `training_id` bigint NULL DEFAULT NULL COMMENT '培训ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_safety_exam_training`(`training_id` ASC) USING BTREE,
  CONSTRAINT `fk_safety_exam_training` FOREIGN KEY (`training_id`) REFERENCES `safety_training` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_safety_exam_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全考试记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of safety_exam_record
-- ----------------------------
INSERT INTO `safety_exam_record` VALUES (14, 2, 25, 100, 0, 1, '2026-06-02 23:44:43', 4);

-- ----------------------------
-- Table structure for safety_incident
-- ----------------------------
DROP TABLE IF EXISTS `safety_incident`;
CREATE TABLE `safety_incident`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事故标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '事故描述',
  `level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '一般' COMMENT '事故等级',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '处理状态',
  `reporter_id` bigint NULL DEFAULT NULL COMMENT '报告人ID',
  `incident_time` datetime NULL DEFAULT NULL COMMENT '发生时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发生地点',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  CONSTRAINT `fk_safety_incident_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全事故表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of safety_incident
-- ----------------------------
INSERT INTO `safety_incident` VALUES (1, NULL, '1123', '一般', 'resolved', NULL, NULL, '2026-06-01 02:47:57', NULL);
INSERT INTO `safety_incident` VALUES (2, '1212', '2342\n地点: 1212\n处理人: 1212', 'critical', 'resolved', NULL, '2026-06-14 00:00:00', '2026-06-01 02:52:07', NULL);
INSERT INTO `safety_incident` VALUES (3, '123123', 'qweqw', 'minor', 'resolved', 1, '2026-06-20 00:00:00', '2026-06-02 00:19:59', '2312');

-- ----------------------------
-- Table structure for safety_inspection
-- ----------------------------
DROP TABLE IF EXISTS `safety_inspection`;
CREATE TABLE `safety_inspection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '检查标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '检查内容',
  `inspector_id` bigint NULL DEFAULT NULL COMMENT '检查人ID',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '检查结果',
  `inspection_time` datetime NULL DEFAULT NULL COMMENT '检查时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inspector_id`(`inspector_id` ASC) USING BTREE,
  CONSTRAINT `fk_safety_inspection_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全检查表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of safety_inspection
-- ----------------------------
INSERT INTO `safety_inspection` VALUES (1, '安全检查 - 2026-06-20', '检查项: 23, 问题项: 0', 1, 'passed', '2026-06-20 00:00:00', '2026-06-01 02:51:49');
INSERT INTO `safety_inspection` VALUES (3, '安全检查 - 2026-06-12', '检查项: 12, 问题项: 1212', 1, 'passed', '2026-06-12 00:00:00', '2026-06-02 13:52:18');
INSERT INTO `safety_inspection` VALUES (4, '安全检查 - 2026-06-14', '检查项: 23, 问题项: 23', 1, 'passed', '2026-06-14 00:00:00', '2026-06-02 14:44:46');

-- ----------------------------
-- Table structure for safety_question
-- ----------------------------
DROP TABLE IF EXISTS `safety_question`;
CREATE TABLE `safety_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容',
  `option_a` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选项A',
  `option_b` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选项B',
  `option_c` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选项C',
  `option_d` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选项D',
  `answer` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '正确答案',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'single' COMMENT '题目类型：single/multi/judge',
  `training_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_safety_question_training`(`training_id` ASC) USING BTREE,
  CONSTRAINT `fk_safety_question_training` FOREIGN KEY (`training_id`) REFERENCES `safety_training` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全考试题目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of safety_question
-- ----------------------------
INSERT INTO `safety_question` VALUES (1, '13123', '1231', '1231', '1231', '阿斯顿', '1', 'single', NULL);
INSERT INTO `safety_question` VALUES (2, 'dghghjj', '正确', '错误', '', '', '0', 'judge', NULL);
INSERT INTO `safety_question` VALUES (3, 'fhhh', 'ghjh', 'ghjj', 'gjji', 'hjjj', '0,2,3', 'multi', NULL);
INSERT INTO `safety_question` VALUES (4, '123123', '12312', '31231', '31231', '231231', '0', 'single', NULL);
INSERT INTO `safety_question` VALUES (5, '112321', '123', '123', '123', '12312', '1', 'single', 1780412523529);
INSERT INTO `safety_question` VALUES (6, '123', '123', '123', '123', '123', '0', 'single', 1);

-- ----------------------------
-- Table structure for safety_regulation
-- ----------------------------
DROP TABLE IF EXISTS `safety_regulation`;
CREATE TABLE `safety_regulation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '制度标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '制度内容',
  `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '制度类型',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全制度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of safety_regulation
-- ----------------------------
INSERT INTO `safety_regulation` VALUES (1, '234', '234213', 'asdaasd', '2026-06-01 02:46:27', '2026-06-09 14:11:35');

-- ----------------------------
-- Table structure for safety_training
-- ----------------------------
DROP TABLE IF EXISTS `safety_training`;
CREATE TABLE `safety_training`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '培训标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '培训内容',
  `training_time` datetime NULL DEFAULT NULL COMMENT '培训时间',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '培训地点',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `fk_safety_training_creator` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全培训表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of safety_training
-- ----------------------------
INSERT INTO `safety_training` VALUES (4, '12312', '答题限时: 30分钟', '2026-06-02 00:00:00', '2026-06-03', 1, '2026-06-02 23:42:26');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户真实姓名',
  `sex` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '未知' COMMENT '性别',
  `account` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号（唯一）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录密码（Bcrypt加密）',
  `department_id` int NULL DEFAULT NULL COMMENT '所属部门ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电子邮箱',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1=正常启用，0=因多次输错密码被短暂禁用',
  `disable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否被管理员封禁：1=没有，0=被封禁',
  `ctime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户头像 URL',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_account`(`account` ASC) USING BTREE,
  INDEX `idx_department`(`department_id` ASC) USING BTREE,
  CONSTRAINT `fk_sys_user_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '霜天', '男', '2048097948', '$2a$10$7VoeSoVrOvQuBE6QMfLq9O7FLQwEnYrwTpd4k8ldLp4u18nwqaLCa', 1, '18282648320', '18282648320@163.com', 1, 1, '2026-06-01 01:38:43', '/uploads/avatar/avatar_1_1781015163828.png');
INSERT INTO `sys_user` VALUES (2, '张三', '男', '3333333333', '$2a$10$..A6gDjjs88G.irXtXdQseKbASvRTm1/9cpapu8Hq8tAJYn9RSozi', 2, '174084', '123123123@qq.com', 1, 1, '2026-06-01 14:35:47', '/uploads/avatar/avatar_2_1780338610478.png');
INSERT INTO `sys_user` VALUES (3, '李四', '男', '4444444444', '$2a$10$WVS2ituhN6IF4lhEQ2T8mu2QEQQMFpYVNl9fwUlXSUcbkBIo.HE/q', 1, '121332123123', '1231312@163.com', 1, 1, '2026-06-02 02:59:11', '/uploads/avatar/avatar_3_1780340433647.png');
INSERT INTO `sys_user` VALUES (4, '王五', '男', '5555555555', '$2a$10$ixGSkKqdCHcYzEkCJ361TOpNaM6vLWoJzJHLgw5mOy4wzy9UFQ3z.', 2, '2131231231', '123123@213', 1, 1, '2026-06-02 16:56:43', NULL);
INSERT INTO `sys_user` VALUES (5, '刘六', '未知', '6666666666', '$2a$10$iZMWeOriTLOmNb5.XyQ24erPTLbKE35o8.nuu/9NJzjayrMwGYbze', NULL, NULL, NULL, 1, 1, '2026-06-03 02:11:59', NULL);
INSERT INTO `sys_user` VALUES (6, '段七', '未知', '7777777777', '$2a$10$NTTK9VVsfLEYHFag/oNodOHNz.4nydspQUbUlTaiE.S9mxlD8M4CC', NULL, NULL, NULL, 1, 1, '2026-06-03 02:15:14', NULL);

-- ----------------------------
-- Table structure for usage_record
-- ----------------------------
DROP TABLE IF EXISTS `usage_record`;
CREATE TABLE `usage_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `resource_id` bigint NOT NULL COMMENT '物资ID',
  `user_id` bigint NOT NULL COMMENT '领用人ID',
  `quantity` int NOT NULL COMMENT '领用数量',
  `purpose` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用途',
  `usage_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领用时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_usage_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_usage_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资领用记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of usage_record
-- ----------------------------
INSERT INTO `usage_record` VALUES (1, 1, 213, 12, '1231', '2026-06-01 03:16:23');
INSERT INTO `usage_record` VALUES (2, 1, 1, 12, '2312312', '2026-06-01 14:28:57');
INSERT INTO `usage_record` VALUES (3, 2, 1, 12, '213', '2026-06-01 14:32:07');
INSERT INTO `usage_record` VALUES (4, 3, 1, 1, '祝福', '2026-06-01 14:33:43');
INSERT INTO `usage_record` VALUES (5, 1, 1, 122, '234234', '2026-06-02 02:12:15');
INSERT INTO `usage_record` VALUES (6, 2, 1, 12, '123', '2026-06-02 04:17:58');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1);
INSERT INTO `user_role` VALUES (2, 3);
INSERT INTO `user_role` VALUES (3, 3);
INSERT INTO `user_role` VALUES (4, 3);
INSERT INTO `user_role` VALUES (5, 3);
INSERT INTO `user_role` VALUES (6, 4);

SET FOREIGN_KEY_CHECKS = 1;

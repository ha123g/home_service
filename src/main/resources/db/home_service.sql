/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3307_docker
 Source Server Type    : MySQL
 Source Server Version : 80411 (8.4.11)
 Source Host           : localhost:3307
 Source Schema         : home_service

 Target Server Type    : MySQL
 Target Server Version : 80411 (8.4.11)
 File Encoding         : 65001

 Date: 09/09/2026 20:06:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '直接联系人',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '直接联系电话',
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区域',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `longitude` decimal(10, 7) NULL DEFAULT NULL,
  `latitude` decimal(10, 7) NULL DEFAULT NULL,
  `is_default` tinyint NULL DEFAULT 0,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVE',
  `created_at` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_address_user`(`user_id` ASC, `status` ASC, `is_default` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户服务地址' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of address
-- ----------------------------
INSERT INTO `address` VALUES (1, 7, 'haozi111', '15829402855', '陕西省', '西安市', '未央区', '陕西省西安市未央区张家堡街道西安银行凤城八路小微支行西安市人民政府', 108.9398650, 34.3431580, 0, 'NORMAL', '2026-09-09 04:45:03.969', '2026-09-09 04:45:03.969');
INSERT INTO `address` VALUES (2, 7, 'haozi111', '15829402855', '陕西省', '西安市', '未央区', '陕西省西安市未央区张家堡街道西安银行凤城八路小微支行西安市人民政府', 108.9398650, 34.3431580, 0, 'NORMAL', '2026-09-09 04:45:18.214', '2026-09-09 04:45:18.214');
INSERT INTO `address` VALUES (3, 7, 'haozi111', '15829402866', '陕西省', '西安市', '未央区', '陕西省西安市未央区张家堡街道凤城八路60号长和·上尚郡', 108.9318390, 34.3396850, 0, 'ACTIVE', '2026-09-09 09:50:24.551', '2026-09-09 09:50:24.551');

-- ----------------------------
-- Table structure for ai_request_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_request_log`;
CREATE TABLE `ai_request_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `route_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `detail_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ERROR',
  `agent_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `component_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `model_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `input_tokens` int NULL DEFAULT NULL,
  `output_tokens` int NULL DEFAULT NULL,
  `retrieval_count` int NULL DEFAULT NULL,
  `tool_call_count` int NOT NULL DEFAULT 0,
  `latency_ms` int NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `expire_time` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ai_request_log_request`(`request_id` ASC) USING BTREE,
  INDEX `idx_ai_request_log_created`(`created_time` ASC) USING BTREE,
  INDEX `idx_ai_request_log_expire`(`expire_time` ASC) USING BTREE,
  INDEX `idx_ai_request_log_route_status`(`route_type` ASC, `status` ASC, `created_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 请求元数据，不含会话正文' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_request_log
-- ----------------------------
INSERT INTO `ai_request_log` VALUES (1, 'f5152f77-e48f-4fde-ba3b-140a1ec56823', 7, 'KNOWLEDGE', 'ERROR', 'KNOWLEDGE_AGENT', NULL, 'qwen-plus', 0, 0, 0, 0, 88, 'FAILED', '502', '2026-09-07 22:46:38.960', '2026-10-07 22:46:38.960');
INSERT INTO `ai_request_log` VALUES (2, '243ca567-9e63-484a-8340-1c9358392f08', 7, 'KNOWLEDGE', 'ERROR', 'KNOWLEDGE_AGENT', NULL, 'qwen-plus', 0, 0, 0, 0, 93, 'FAILED', '502', '2026-09-07 23:29:43.989', '2026-10-07 23:29:43.989');
INSERT INTO `ai_request_log` VALUES (3, 'b1c890a9-4ece-4e18-b787-e4da46eb6fa7', 7, 'KNOWLEDGE', 'ERROR', 'KNOWLEDGE_AGENT', NULL, 'qwen-plus', 0, 0, 0, 0, 19, 'FAILED', '502', '2026-09-07 23:31:34.607', '2026-10-07 23:31:34.607');
INSERT INTO `ai_request_log` VALUES (4, 'fa035bfe-4650-4ca0-8f9d-b4744b512121', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'PROFILE_FORM', 'qwen-plus', 0, 0, 0, 0, 200, 'SUCCESS', NULL, '2026-09-08 00:44:35.413', '2026-10-08 00:44:35.413');
INSERT INTO `ai_request_log` VALUES (5, '44c93ddf-b9d1-47b1-a727-eea3b9793a57', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'BUSINESS_HELP', 'qwen-plus', 0, 0, 0, 0, 17, 'SUCCESS', NULL, '2026-09-08 00:47:05.118', '2026-10-08 00:47:05.118');
INSERT INTO `ai_request_log` VALUES (6, '270d356c-ce5b-4a83-8aa1-bc0fe008ef3d', 7, 'RECOMMENDATION', 'SAMPLED', 'RECOMMENDATION_AGENT', 'SHOP_LIST', 'qwen-plus', 0, 0, 0, 0, 37, 'SUCCESS', NULL, '2026-09-08 00:50:23.935', '2026-10-08 00:50:23.935');
INSERT INTO `ai_request_log` VALUES (7, 'd927f8d0-ddae-4a4f-b477-8676ca0edd59', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'BUSINESS_ENTRY', 'qwen-plus', 0, 0, 0, 0, 31, 'SUCCESS', NULL, '2026-09-08 01:29:09.000', '2026-10-08 01:29:09.000');
INSERT INTO `ai_request_log` VALUES (8, '5f13e77b-c809-471d-9fdb-c7b5c8565e1a', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'MERCHANT_APPLICATION_FORM', 'qwen-plus', 0, 0, 0, 0, 14, 'SUCCESS', NULL, '2026-09-08 01:36:35.732', '2026-10-08 01:36:35.732');
INSERT INTO `ai_request_log` VALUES (9, 'd9cd6684-f50f-402e-a381-7019f55e6033', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'BUSINESS_HELP', 'qwen-plus', 0, 0, 0, 0, 13, 'SUCCESS', NULL, '2026-09-08 01:37:34.157', '2026-10-08 01:37:34.157');
INSERT INTO `ai_request_log` VALUES (10, '70284f47-4bf2-4195-b9a4-92594db3a767', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'PROFILE_FORM', 'qwen-plus', 0, 0, 0, 0, 15, 'SUCCESS', NULL, '2026-09-08 06:28:32.347', '2026-10-08 06:28:32.347');
INSERT INTO `ai_request_log` VALUES (11, '3bc75d80-6c14-4218-83e7-7680de1dd698', 6, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'MERCHANT_APPLICATION_FORM', 'qwen-plus', 84, 67, 0, 0, 11056, 'SUCCESS', NULL, '2026-09-08 10:41:11.216', '2026-10-08 10:41:11.216');
INSERT INTO `ai_request_log` VALUES (12, '2446ba82-c319-4393-a778-9e41a200849f', 6, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'PASSWORD_FORM', 'qwen-plus', 83, 16, 0, 0, 8857, 'SUCCESS', NULL, '2026-09-08 10:42:19.243', '2026-10-08 10:42:19.243');
INSERT INTO `ai_request_log` VALUES (13, '744cd8b3-e23b-49ab-a576-ee8f89288793', 6, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'BUSINESS_HELP', 'qwen-plus', 84, 22, 0, 0, 8965, 'SUCCESS', NULL, '2026-09-08 10:42:52.279', '2026-10-08 10:42:52.279');
INSERT INTO `ai_request_log` VALUES (14, 'edebe0a0-f15c-411d-b4b8-85a2b1128976', 6, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'APPOINTMENT_FORM', 'qwen-plus', 87, 26, 0, 0, 9321, 'SUCCESS', NULL, '2026-09-08 10:45:02.265', '2026-10-08 10:45:02.265');
INSERT INTO `ai_request_log` VALUES (15, 'd6abf82e-4a2b-4adc-af98-d8faf58e146b', 8, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'PROFILE_FORM', 'qwen-plus', 85, 18, 0, 0, 8783, 'SUCCESS', NULL, '2026-09-08 10:54:13.421', '2026-10-08 10:54:13.421');
INSERT INTO `ai_request_log` VALUES (16, '1ceeedc9-b344-47d7-9267-e6e403d04357', 8, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'MERCHANT_APPLICATION_FORM', 'qwen-plus', 84, 67, 0, 0, 11342, 'SUCCESS', NULL, '2026-09-08 10:55:05.167', '2026-10-08 10:55:05.167');
INSERT INTO `ai_request_log` VALUES (17, 'c9e738e5-dfcd-432e-a0e7-bc156c073935', 8, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen-plus', 0, 0, 0, 0, 183, 'SUCCESS', NULL, '2026-09-08 18:21:52.906', '2026-10-08 18:21:52.906');
INSERT INTO `ai_request_log` VALUES (18, '95d57073-1f56-43f1-b9f4-8c5fc6a042fb', 8, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen-plus', 0, 0, 0, 0, 192, 'SUCCESS', NULL, '2026-09-08 18:21:57.417', '2026-10-08 18:21:57.417');
INSERT INTO `ai_request_log` VALUES (19, '0e096827-48b0-4519-a162-d9a37796369a', 8, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'MERCHANT_APPLICATION_FORM', 'qwen-plus', 0, 0, 0, 0, 987, 'SUCCESS', NULL, '2026-09-08 20:38:32.381', '2026-10-08 20:38:32.381');
INSERT INTO `ai_request_log` VALUES (20, '5a8922dc-0f2f-4272-8d0e-54c9e49d2ea6', 8, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen-plus', 0, 0, 0, 0, 438, 'SUCCESS', NULL, '2026-09-08 20:40:25.580', '2026-10-08 20:40:25.580');
INSERT INTO `ai_request_log` VALUES (21, '9910ba7e-8f13-47ea-a349-46ee7cc90941', 8, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen-plus', 0, 0, 0, 0, 353, 'SUCCESS', NULL, '2026-09-08 20:40:31.443', '2026-10-08 20:40:31.443');
INSERT INTO `ai_request_log` VALUES (22, '7007f423-753e-4728-b65a-c0f6ffa5ccf5', 9, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'PROFILE_FORM', 'qwen-plus', 0, 0, 0, 0, 696, 'SUCCESS', NULL, '2026-09-08 23:32:51.495', '2026-10-08 23:32:51.495');
INSERT INTO `ai_request_log` VALUES (23, '655dc7c5-fddb-432f-9760-080c30dc9f75', 9, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'MERCHANT_APPLICATION_FORM', 'qwen-plus', 0, 0, 0, 0, 892, 'SUCCESS', NULL, '2026-09-08 23:38:17.244', '2026-10-08 23:38:17.244');
INSERT INTO `ai_request_log` VALUES (24, '3c3b98f9-1737-45d9-b50e-ea5b4533bb72', 9, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen-plus', 0, 0, 0, 0, 649, 'SUCCESS', NULL, '2026-09-09 01:36:46.411', '2026-10-09 01:36:46.411');
INSERT INTO `ai_request_log` VALUES (25, '2253fb46-efb7-4aa7-9d9d-6ddc49387f5e', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen3.8-27b', 0, 0, 0, 0, 590, 'SUCCESS', NULL, '2026-09-09 01:52:45.808', '2026-10-09 01:52:45.808');
INSERT INTO `ai_request_log` VALUES (26, 'f6ddec26-f9cc-4b1f-a4fe-0cd57c00f6e6', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen3.8-27b', 0, 0, 0, 0, 298, 'SUCCESS', NULL, '2026-09-09 01:52:49.630', '2026-10-09 01:52:49.630');
INSERT INTO `ai_request_log` VALUES (27, '05dc079b-1970-46d8-b8ea-c91db0d93915', 7, 'BUSINESS', 'FULL', 'BUSINESS_AGENT', 'TEXT', 'qwen3.8-27b', 0, 0, 0, 0, 196, 'SUCCESS', NULL, '2026-09-09 01:52:56.604', '2026-10-09 01:52:56.604');

-- ----------------------------
-- Table structure for ai_tool_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_tool_call_log`;
CREATE TABLE `ai_tool_call_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tool_call_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_log_id` bigint NULL DEFAULT NULL,
  `tool_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `permission_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_payload_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `response_payload_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `request_payload_size` int NULL DEFAULT NULL,
  `response_payload_size` int NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `confirmation_required` tinyint NOT NULL DEFAULT 0,
  `confirmed_user_id` bigint NULL DEFAULT NULL,
  `request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `latency_ms` int NULL DEFAULT NULL,
  `expire_time` datetime(3) NOT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ai_tool_call_id`(`tool_call_id` ASC) USING BTREE,
  INDEX `idx_ai_tool_request_log`(`request_log_id` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_ai_tool_status`(`status` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_ai_tool_expire`(`expire_time` ASC) USING BTREE,
  INDEX `idx_ai_tool_request_id`(`request_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI Tool 审计元数据，不含 payload 原文' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_tool_call_log
-- ----------------------------

-- ----------------------------
-- Table structure for ai_usage_daily
-- ----------------------------
DROP TABLE IF EXISTS `ai_usage_daily`;
CREATE TABLE `ai_usage_daily`  (
  `usage_date` date NOT NULL,
  `route_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `agent_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `model_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_count` bigint NOT NULL DEFAULT 0,
  `success_count` bigint NOT NULL DEFAULT 0,
  `failure_count` bigint NOT NULL DEFAULT 0,
  `input_tokens` bigint NOT NULL DEFAULT 0,
  `output_tokens` bigint NOT NULL DEFAULT 0,
  `tool_call_count` bigint NOT NULL DEFAULT 0,
  `retrieval_count` bigint NOT NULL DEFAULT 0,
  `total_latency_ms` bigint NOT NULL DEFAULT 0,
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`usage_date`, `route_type`, `agent_name`, `model_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 每日聚合用量' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_usage_daily
-- ----------------------------
INSERT INTO `ai_usage_daily` VALUES ('2026-09-08', 'BUSINESS', 'BUSINESS_AGENT', 'qwen-plus', 12, 12, 0, 507, 216, 0, 0, 58614, '2026-09-08 18:58:24.073');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-08', 'KNOWLEDGE', 'KNOWLEDGE_AGENT', 'qwen-plus', 20, 17, 3, 2103, 57, 0, 3, 24113, '2026-09-08 18:53:24.055');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-08', 'RECOMMENDATION', 'RECOMMENDATION_AGENT', 'qwen-plus', 7, 7, 0, 189, 70, 0, 0, 9828, '2026-09-08 18:48:24.014');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-09', 'BUSINESS', 'BUSINESS_AGENT', 'qwen-plus', 7, 7, 0, 0, 0, 0, 0, 3498, '2026-09-09 09:37:14.120');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-09', 'BUSINESS', 'BUSINESS_AGENT', 'qwen3.8-27b', 3, 3, 0, 0, 0, 0, 0, 1084, '2026-09-09 09:53:29.906');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-09', 'KNOWLEDGE', 'KNOWLEDGE_AGENT', 'qwen-plus', 9, 9, 0, 0, 0, 0, 4, 6831, '2026-09-09 09:37:14.150');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-09', 'KNOWLEDGE', 'KNOWLEDGE_AGENT', 'qwen3.8-27b', 5, 5, 0, 3100, 341, 0, 2, 90099, '2026-09-09 10:08:29.967');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-09', 'RECOMMENDATION', 'RECOMMENDATION_AGENT', 'qwen-plus', 2, 2, 0, 0, 0, 0, 0, 1884, '2026-09-09 07:32:38.279');
INSERT INTO `ai_usage_daily` VALUES ('2026-09-09', 'RECOMMENDATION', 'RECOMMENDATION_AGENT', 'qwen3.8-27b', 2, 2, 0, 281, 575, 0, 0, 89841, '2026-09-09 10:03:29.951');

-- ----------------------------
-- Table structure for appointment_request
-- ----------------------------
DROP TABLE IF EXISTS `appointment_request`;
CREATE TABLE `appointment_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约申请单号',
  `user_id` bigint NOT NULL COMMENT '发起用户ID',
  `shop_id` bigint NOT NULL COMMENT '预约目标商铺ID；由应用层校验',
  `service_id` bigint NOT NULL COMMENT '预约服务项目ID；由应用层校验归属',
  `address_id` bigint NOT NULL COMMENT '服务地址ID；提交时由应用层校验归属',
  `service_title_input` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户描述的服务名称/关键词',
  `requirement_text` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户对工作内容的描述',
  `preferred_start` datetime(3) NULL DEFAULT NULL,
  `preferred_end` datetime(3) NULL DEFAULT NULL,
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING_PLATFORM' COMMENT 'PENDING_PLATFORM/CONTACTING/WORKER_ARRANGED/ORDER_PENDING/COMPLETED/CANCELLED/CLOSED',
  `platform_operator_id` bigint NULL DEFAULT NULL COMMENT '平台跟进管理员，不代表工作人员',
  `platform_note` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台线下联系记录摘要，禁止记录敏感凭证',
  `cancel_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `idempotency_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appointment_request_no`(`request_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_appointment_user_idempotency`(`user_id` ASC, `idempotency_key` ASC) USING BTREE,
  INDEX `idx_appointment_shop_status`(`shop_id` ASC, `status` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_appointment_user_status`(`user_id` ASC, `status` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_appointment_platform_status`(`status` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_appointment_schedule`(`preferred_start` ASC, `preferred_end` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户预约申请；平台与用户双方可见' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of appointment_request
-- ----------------------------
INSERT INTO `appointment_request` VALUES (1, 'AP8eca02f790c840fb9ae5e58f', 7, 4, 6, 3, '服务2', 'miaoshu', '2026-09-09 17:00:54.491', '2026-09-14 13:06:00.000', 'haozi111', '15829402866', 'ORDER_PENDING', NULL, '商家已确认服务安排', NULL, 'cb6b4dc7-3a20-4460-b509-443e56cd62f1', '2026-09-09 01:50:24.629', '2026-09-09 01:52:11.198');

-- ----------------------------
-- Table structure for appointment_request_status_history
-- ----------------------------
DROP TABLE IF EXISTS `appointment_request_status_history`;
CREATE TABLE `appointment_request_status_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_id` bigint NOT NULL,
  `from_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `to_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `operator_user_id` bigint NULL DEFAULT NULL,
  `reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `request_idempotency_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_appointment_history_request`(`request_id` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_appointment_history_operator`(`operator_user_id` ASC, `created_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约申请状态审计' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of appointment_request_status_history
-- ----------------------------
INSERT INTO `appointment_request_status_history` VALUES (1, 1, NULL, 'PENDING_PLATFORM', 7, '用户提交预约', 'cb6b4dc7-3a20-4460-b509-443e56cd62f1', '2026-09-09 01:50:24.638');
INSERT INTO `appointment_request_status_history` VALUES (2, 1, 'PENDING_PLATFORM', 'CONTACTING', 9, '商家已收到预约，正在确认需求', NULL, '2026-09-09 01:51:07.536');
INSERT INTO `appointment_request_status_history` VALUES (3, 1, 'CONTACTING', 'WORKER_ARRANGED', 9, '商家已确认服务安排', NULL, '2026-09-09 01:51:10.456');
INSERT INTO `appointment_request_status_history` VALUES (4, 1, 'WORKER_ARRANGED', 'ORDER_PENDING', 9, '已生成订单', NULL, '2026-09-09 01:52:11.198');

-- ----------------------------
-- Table structure for merchant_application
-- ----------------------------
DROP TABLE IF EXISTS `merchant_application`;
CREATE TABLE `merchant_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `apply_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `intro` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请信息',
  `shop_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `service_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `longitude` decimal(10, 7) NULL DEFAULT NULL,
  `latitude` decimal(10, 7) NULL DEFAULT NULL,
  `logo_object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `review_remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED',
  `reviewer_id` bigint NULL DEFAULT NULL,
  `applied_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `reviewed_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `service_ids` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请选择的服务项目 ID，逗号分隔',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请自定义服务标签，逗号分隔',
  `service_category_ids` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请选择的平台标准服务分类 ID，逗号分隔',
  `service_extensions` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商家自定义服务明细 JSON；base_price 为起步/参考价',
  `service_radius_km` decimal(6, 2) NULL DEFAULT NULL COMMENT '门店周边服务覆盖半径（公里）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_application_apply_no`(`apply_no` ASC) USING BTREE,
  INDEX `idx_merchant_application_user_status`(`user_id` ASC, `status` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_merchant_application_review`(`status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入驻申请' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchant_application
-- ----------------------------
INSERT INTO `merchant_application` VALUES (1, 6, 'APPLY-PLAUSER-001', '平台示例商家', '13800000006', '提供规范、透明的家政服务。', '暖居示范家政服务中心', '全市及周边区域', '示例市示例区暖居路 1 号', '示例省', '示例市', '示例区', 116.3971280, 39.9165270, NULL, NULL, 'APPROVED', 1, '2026-09-07 10:31:39.874', '2026-09-07 10:31:39.874', '2026-09-07 10:31:39.874', '2026-09-07 10:31:39.874', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `merchant_application` VALUES (2, 7, 'MA1788859586970281523', 'haozi111', '15829402855', 'jianjie', '家政服务平台2', '保洁', '陕西省西安市莲湖区北院门街道创才幼儿学园回民街', '陕西省', '西安市', NULL, 108.9310000, 34.2625000, 'u/7/merchant-logo/20260908/303023c8-753b-4971-98d1-687bc2dd7cf5.png', '', 'APPROVED', 1, '2026-09-08 01:26:26.966', '2026-09-08 06:26:11.418', '2026-09-08 01:26:26.966', '2026-09-08 06:26:11.418', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `merchant_application` VALUES (3, 8, 'MA178892868451001B059', 'user', '15829402844', '简洁', '家政服务平台3', NULL, '陕西省西安市未央区张家堡街道西安市民族事务委员会西安市人民政府', '陕西省', '西安市', NULL, 108.9397000, 34.3420000, 'u/8/merchant-logo/20260909/8121df02-0b8b-482c-a193-81f810b6ffc3.png', '', 'APPROVED', 1, '2026-09-08 20:38:04.509', '2026-09-08 20:40:55.681', '2026-09-08 20:38:04.509', '2026-09-08 20:40:55.681', 1, '2,1', NULL, NULL, NULL, 100.00);
INSERT INTO `merchant_application` VALUES (4, 9, 'MA178894728047899D01F', 'haozi111', '15829402844', '介绍', '家政服务平台4', NULL, '陕西省西安市未央区张家堡街道行政中心地铁站B3口赛高广场', '陕西省', '西安市', '未央区', 108.9462300, 34.3396140, 'u/9/merchant-logo/20260909/00c53f69-17bd-4831-a343-f399af530dcf.png', '', 'APPROVED', 1, '2026-09-09 01:48:00.477', '2026-09-09 01:48:32.332', '2026-09-09 01:48:00.477', '2026-09-09 01:48:32.332', 1, NULL, '标签1,标签2', '3,5,4', '[{\"categoryId\":4,\"title\":\"服务2\",\"summary\":\"简介\",\"description\":\"描述\",\"pricingUnit\":\"HOUR\",\"basePrice\":200,\"durationMinutes\":400,\"tags\":[\"标签2\"]}]', 500.00);

-- ----------------------------
-- Table structure for merchant_application_image
-- ----------------------------
DROP TABLE IF EXISTS `merchant_application_image`;
CREATE TABLE `merchant_application_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `image_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LOGO/QUALIFICATION/ID_FRONT/ID_BACK/OTHER',
  `object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '腾讯云 COS Object Key，不保存临时签名 URL',
  `bucket_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `sha256` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UPLOADED' COMMENT 'UPLOADING/UPLOADED/REJECTED/DELETED',
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_application_image_hash`(`application_id` ASC, `sha256` ASC) USING BTREE,
  INDEX `idx_application_image`(`application_id` ASC, `image_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入驻申请图片 COS 元数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchant_application_image
-- ----------------------------
INSERT INTO `merchant_application_image` VALUES (1, 1, 'LOGO', 'demo/plauser/logo.png', NULL, NULL, 'image/png', 102400, NULL, 'UPLOADED', '2026-09-08 02:14:24.312', '2026-09-08 02:14:24.312');
INSERT INTO `merchant_application_image` VALUES (2, 1, 'OTHER', 'demo/plauser/home-1.png', NULL, NULL, 'image/png', 204800, NULL, 'UPLOADED', '2026-09-08 02:14:24.312', '2026-09-08 02:14:24.312');
INSERT INTO `merchant_application_image` VALUES (3, 1, 'OTHER', 'demo/plauser/home-2.png', NULL, NULL, 'image/png', 198400, NULL, 'UPLOADED', '2026-09-08 02:14:24.312', '2026-09-08 02:14:24.312');
INSERT INTO `merchant_application_image` VALUES (4, 2, 'LOGO', 'u/7/merchant-logo/20260908/303023c8-753b-4971-98d1-687bc2dd7cf5.png', NULL, NULL, 'image/png', 181919, NULL, 'UPLOADED', '2026-09-08 01:26:26.966', '2026-09-08 01:26:26.966');
INSERT INTO `merchant_application_image` VALUES (5, 2, 'OTHER', 'u/7/merchant-gallery/20260908/9eddfd3f-938a-4766-adca-53c1707d12a7.png', NULL, NULL, 'image/png', 150815, NULL, 'UPLOADED', '2026-09-08 01:26:26.966', '2026-09-08 01:26:26.966');
INSERT INTO `merchant_application_image` VALUES (6, 2, 'OTHER', 'u/7/merchant-gallery/20260908/59a65622-946e-4053-8a55-4acb73128606.png', NULL, NULL, 'image/png', 87576, NULL, 'UPLOADED', '2026-09-08 01:26:26.966', '2026-09-08 01:26:26.966');
INSERT INTO `merchant_application_image` VALUES (7, 3, 'LOGO', 'u/8/merchant-logo/20260909/8121df02-0b8b-482c-a193-81f810b6ffc3.png', NULL, NULL, 'image/png', 150815, NULL, 'UPLOADED', '2026-09-08 20:38:04.509', '2026-09-08 20:38:04.509');
INSERT INTO `merchant_application_image` VALUES (8, 3, 'OTHER', 'u/8/merchant-gallery/20260909/e93e560e-9a6c-4aff-9117-530fd4419116.png', NULL, NULL, 'image/png', 87576, NULL, 'UPLOADED', '2026-09-08 20:38:04.509', '2026-09-08 20:38:04.509');
INSERT INTO `merchant_application_image` VALUES (9, 3, 'OTHER', 'u/8/merchant-gallery/20260909/f7a3d875-f5ca-4fdd-b939-6306b231d1b7.png', NULL, NULL, 'image/png', 150815, NULL, 'UPLOADED', '2026-09-08 20:38:04.509', '2026-09-08 20:38:04.509');
INSERT INTO `merchant_application_image` VALUES (10, 4, 'LOGO', 'u/9/merchant-logo/20260909/00c53f69-17bd-4831-a343-f399af530dcf.png', NULL, NULL, 'image/png', 184991, NULL, 'UPLOADED', '2026-09-09 01:48:00.477', '2026-09-09 01:48:00.477');
INSERT INTO `merchant_application_image` VALUES (11, 4, 'OTHER', 'u/9/merchant-gallery/20260909/c8d21705-4256-4e1b-9542-31a98ed7c6c9.png', NULL, NULL, 'image/png', 150815, NULL, 'UPLOADED', '2026-09-09 01:48:00.477', '2026-09-09 01:48:00.477');
INSERT INTO `merchant_application_image` VALUES (12, 4, 'OTHER', 'u/9/merchant-gallery/20260909/97a66b0a-39fa-4e5d-a78d-71f01140c79a.png', NULL, NULL, 'image/png', 87576, NULL, 'UPLOADED', '2026-09-09 01:48:00.477', '2026-09-09 01:48:00.477');

-- ----------------------------
-- Table structure for merchant_application_location
-- ----------------------------
DROP TABLE IF EXISTS `merchant_application_location`;
CREATE TABLE `merchant_application_location`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'AMAP',
  `formatted_address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `longitude` decimal(10, 7) NULL DEFAULT NULL,
  `latitude` decimal(10, 7) NULL DEFAULT NULL,
  `location_json` json NULL COMMENT '脱敏后的地图 API 响应必要字段',
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_application_location`(`application_id` ASC) USING BTREE,
  INDEX `idx_application_location_city`(`city` ASC, `district` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入驻申请地图位置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchant_application_location
-- ----------------------------
INSERT INTO `merchant_application_location` VALUES (1, 2, 'AMAP', '陕西省西安市莲湖区北院门街道创才幼儿学园回民街', '陕西省', '西安市', NULL, 108.9310000, 34.2625000, NULL, '2026-09-08 01:26:26.966', '2026-09-08 01:26:26.966');
INSERT INTO `merchant_application_location` VALUES (2, 3, 'AMAP', '陕西省西安市未央区张家堡街道西安市民族事务委员会西安市人民政府', '陕西省', '西安市', NULL, 108.9397000, 34.3420000, NULL, '2026-09-08 20:38:04.509', '2026-09-08 20:38:04.509');
INSERT INTO `merchant_application_location` VALUES (3, 4, 'AMAP', '陕西省西安市未央区张家堡街道行政中心地铁站B3口赛高广场', '陕西省', '西安市', '未央区', 108.9462300, 34.3396140, NULL, '2026-09-09 01:48:00.477', '2026-09-09 01:48:00.477');

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `provider_payment_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `idempotency_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MOCK',
  `method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MOCK',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'INIT' COMMENT 'INIT/SUCCESS/FAILED/CLOSED',
  `amount` decimal(12, 2) NULL DEFAULT NULL,
  `paid_time` datetime(3) NULL DEFAULT NULL,
  `created_time` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `failure_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_idempotency`(`idempotency_key` ASC) USING BTREE,
  UNIQUE INDEX `uk_payment_provider_id`(`provider_payment_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment_record
-- ----------------------------

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限code',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVE' COMMENT '权限描述',
  `parent_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of permission
-- ----------------------------

-- ----------------------------
-- Table structure for rag_document
-- ----------------------------
DROP TABLE IF EXISTS `rag_document`;
CREATE TABLE `rag_document`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `source` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'knowledge',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `current_version` int NOT NULL DEFAULT 0,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DELETED',
  `created_by` bigint NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rag_document_key`(`document_key` ASC) USING BTREE,
  INDEX `idx_rag_document_category_status`(`category` ASC, `status` ASC, `updated_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'RAG知识文档元数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rag_document
-- ----------------------------
INSERT INTO `rag_document` VALUES (1, 'home-service-faq', '家政服务常见问题', 'classpath:knowledge/home_service_faq.md', 'knowledge', NULL, 2, 'ACTIVE', 1, '2026-09-07 02:13:56.007', '2026-09-08 06:25:09.160');

-- ----------------------------
-- Table structure for rag_document_chunk
-- ----------------------------
DROP TABLE IF EXISTS `rag_document_chunk`;
CREATE TABLE `rag_document_chunk`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_id` bigint NOT NULL,
  `version_id` bigint NOT NULL,
  `chunk_id` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `chunk_index` int NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/INDEXED/FAILED/DELETED',
  `vector_id` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rag_chunk_id`(`chunk_id` ASC) USING BTREE,
  INDEX `idx_rag_chunk_version`(`version_id` ASC, `chunk_index` ASC) USING BTREE,
  INDEX `idx_rag_chunk_status`(`status` ASC, `updated_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'RAG知识切块及索引状态' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rag_document_chunk
-- ----------------------------
INSERT INTO `rag_document_chunk` VALUES (1, 1, 1, 'home-service-faq-v1-c0', 0, '# 家政服务常见问题（内置知识库）\n\n> 文档用途：面向用户的家政服务说明、预约流程和安全提示。本文档是可更新的 RAG 知识源，不是订单、价格或支付事实；具体商家、服务状态、金额和订单状态必须以系统实时查询结果为准。\n\n## 一、服务类别\n\n平台当前内置以下服务分类：\n\n- **保洁清洗**：家庭保洁、家电清洗、油烟机清洗、除虫除螨。\n- **维修服务**：家电维修、管道维修、房屋维修。\n- **钟点工**：住家保姆、白班保姆、钟点工。\n- **搬家服务**：居家搬家、日常搬家、企业搬家。\n- **上门安装**：空调安装、家电安装、厨卫安装、家具安装。\n\n服务项目的价格、服务范围、可预约时间和营业状态由商家资料及在线服务项目决定，知识库不生成或猜测这些实时信息。\n\n## 二、保洁与清洗\n\n### 家庭保洁包含什么？\n\n家庭保洁通常包括地面、台面、门窗可触及表面、卫生间和厨房等常规清洁。深度清洁、玻璃外侧、高空作业、重油污和大件搬移是否包含，应在预约前查看商家服务说明并向商家确认。\n\n### 家电清洗需要注意什么？\n\n空调、冰箱、洗衣机等家电清洗前，应确认设备型号、安装位置、是否通电以及是否存在漏水或故障。清洗服务不等于维修；发现故障时应另行预约家电维修。用户应提前移走贵重物品，并保留设备周围的操作空间。\n\n### 油烟机清洗前要准备什么？\n\n请清空灶台附近物品，告知油烟机型号、安装高度和长期未清洗情况。拆洗范围、耗材、严重油污附加费用和是否需要更换配件，以商家确认的服务说明为准。清洗后首次使用应观察是否漏油、漏水或异常噪声。\n\n### 除虫除螨安全吗？\n\n预约时应说明房屋面积、虫害种类、儿童或宠物情况以及过敏史。作业期间按服务人员要求离场或做好隔离，结束后按照药剂说明通风和复位。不要自行混用杀虫剂；对严重或持续虫害，应咨询有资质的专业机构。\n\n## 三、维修服务\n\n### 家电维修如何判断是否需要上门？\n\n提交预约时描述品牌、型号、故障现象、出现时间和是否有异响、漏水或焦味。不要自行拆卸仍在保修期或涉及高压、燃气的设备。上门人员现场检查后可能给出维修方案和费用，平台只记录最终结果，不提供远程安全判断。\n\n### 管道维修需要提供哪些信息？\n\n请说明漏水、堵塞、异味或无水等现象，标明厨房、卫生间或户外位置，并提供可见管件照片（不要上传身份证、银行卡等敏感信息）。若发生大量漏水，应先关闭总阀并联系物业或紧急维修，避免财产损失。\n\n### 房屋维修包含结构改造吗？\n\n普通房屋维修可涉及门窗、墙面、五金和小型设施修复。承重结构、燃气、电力主线路和需要行政许可的施工不属于普通上门维修范围，应联系物业、专业施工单位或主管部门确认。\n\n## 四、钟点工与保姆\n\n### 住家保姆、白班保姆和钟点工有什么区别？', 'FAILED', 'home-service-faq-v1-c0', '2026-09-07 02:13:56.063', '2026-09-07 02:13:56.202');
INSERT INTO `rag_document_chunk` VALUES (2, 1, 1, 'home-service-faq-v1-c1', 1, '普通房屋维修可涉及门窗、墙面、五金和小型设施修复。承重结构、燃气、电力主线路和需要行政许可的施工不属于普通上门维修范围，应联系物业、专业施工单位或主管部门确认。\n\n## 四、钟点工与保姆\n\n### 住家保姆、白班保姆和钟点工有什么区别？\n\n住家保姆通常在约定住所提供较长时段服务；白班保姆在约定白天时段工作；钟点工按小时或约定时段提供单次、周期性服务。具体工作内容、时长、休息和费用必须以服务项目及双方确认的订单为准。\n\n### 如何保护家庭隐私和安全？\n\n只向平台和商家提供完成服务所必需的信息，不要在聊天或表单中发送银行卡密码、验证码、身份证完整号码等敏感资料。贵重物品应妥善保管；如发现异常，应停止服务并联系平台。平台当前不建设工作人员档案、排班或线上交流功能，工作人员由平台线下联系安排。\n\n## 五、搬家服务\n\n### 居家搬家、日常搬家和企业搬家如何选择？\n\n居家搬家适用于家庭住所迁移，日常搬家适用于少量物品或短距离搬运，企业搬家适用于办公室、门店等批量物品迁移。预约时应填写起止地址、楼层、电梯情况、物品数量和是否需要拆装或打包，以便商家评估。\n\n### 搬家前有哪些准备？\n\n提前分类、打包并标记易碎品和贵重物品，确认小区出入口、停车和物业规定。证件、现金、首饰、电脑存储介质等贵重物品建议由用户自行携带。大件、危险品、违禁品是否承运必须事先向商家确认。\n\n## 六、上门安装\n\n### 空调安装前需要确认什么？\n\n请确认空调型号、安装位置、墙体条件、外机位置、电源和物业要求。高空、打孔、加长管线、支架及材料费用可能单独计算，实际金额以现场检查和订单为准。涉及高空作业时必须由具备相应能力的人员执行，用户不要自行攀爬操作。\n\n### 家电、厨卫和家具安装有什么注意事项？\n\n安装前准备好产品说明书、配件和安装空间，确认墙体或地面承重、进水排水和电源条件。安装服务不自动包含改造线路、燃气接入或材料采购，超出服务范围的项目须经用户确认后再处理。\n\n## 七、搜索、预约和订单\n\n### 如何搜索附近商家？\n\n可以按服务类别、关键词、省市区和距离筛选商家。允许定位时，系统使用高德地图返回的经纬度计算附近范围；请检查定位城市和地址是否正确。附近查询需要同时提供经度、纬度和半径，最大半径以接口约束为准。\n\n### 如何提交预约？\n\n登录后选择服务项目和商家，填写服务地址、联系人、联系电话、期望时间及备注，提交预约申请。预约初始状态为“待平台处理”，平台和用户均可查看。请勿在预约阶段将其理解为已生成订单或已支付。\n\n### 平台收到预约后会做什么？\n\n平台查看预约并在线下自行联系合适的工作人员，安排到指定地点服务。工作人员现场估价后，平台录入最终服务结果并生成订单草稿，再发布给用户确认。当前不提供工作人员线上排班、聊天或自动派单功能。\n\n### 订单如何支付和确认？', 'FAILED', 'home-service-faq-v1-c1', '2026-09-07 02:13:56.069', '2026-09-07 02:13:56.202');
INSERT INTO `rag_document_chunk` VALUES (3, 1, 1, 'home-service-faq-v1-c2', 2, '约后会做什么？\n\n平台查看预约并在线下自行联系合适的工作人员，安排到指定地点服务。工作人员现场估价后，平台录入最终服务结果并生成订单草稿，再发布给用户确认。当前不提供工作人员线上排班、聊天或自动派单功能。\n\n### 订单如何支付和确认？\n\n订单发布后用户可查看服务快照和应付金额，在支持的 Stripe 沙盒模式下发起支付。支付成功后仍需按照页面提示确认订单；金额、状态和支付结果以订单及支付接口返回为准，不以聊天文字为准。\n\n### 可以取消预约或订单吗？\n\n待平台处理的预约可按页面入口申请取消，系统会校验当前状态和资源归属。订单取消、退款及支付争议受订单状态和当前阶段能力限制；本阶段不提供自动退款，遇到异常应联系平台人工处理。\n\n## 八、商家入驻\n\n### 商家如何申请入驻？\n\n登录后填写商铺名称、服务范围、联系人和位置，使用地图选点或输入地址，并上传必要的门店图片。高德地图用于位置解析和校验，图片由腾讯云 COS 保存对象元数据。提交后由审计管理员审核，审核通过后才建立正式商铺并成为平台用户。\n\n### 入驻申请多久通过？\n\n审核结果取决于资料完整性和管理员处理进度。申请状态、审核备注和结果以平台页面为准；被拒后可根据备注修改资料并重新提交，重复审核会被系统拒绝以避免产生多个正式商铺。\n\n## 九、安全与平台边界\n\n平台不会要求用户提供密码、短信验证码、支付密码或完整银行卡信息。任何要求私下转账、索取验证码或绕过平台支付的行为都应立即停止并联系平台。\n\n知识库只能解释通用流程和注意事项。实时价格、商家营业状态、服务时间、预约、订单、支付和账户信息必须调用受权限保护的业务接口查询。当前暂不支持评价、投诉、售后、优惠券、营销活动、自动派单、工作人员档案/排班/线上交流、平台估价、分账和自动退款等功能；如需这些能力，请使用人工渠道。', 'FAILED', 'home-service-faq-v1-c2', '2026-09-07 02:13:56.072', '2026-09-07 02:13:56.202');
INSERT INTO `rag_document_chunk` VALUES (4, 1, 2, 'home-service-faq-v2-c0', 0, '# 家政服务常见问题（内置知识库）\n\n> 文档用途：面向用户的家政服务说明、预约流程和安全提示。本文档是可更新的 RAG 知识源，不是订单、价格或支付事实；具体商家、服务状态、金额和订单状态必须以系统实时查询结果为准。\n\n## 一、服务类别\n\n平台当前内置以下服务分类：\n\n- **保洁清洗**：家庭保洁、家电清洗、油烟机清洗、除虫除螨。\n- **维修服务**：家电维修、管道维修、房屋维修。\n- **钟点工**：住家保姆、白班保姆、钟点工。\n- **搬家服务**：居家搬家、日常搬家、企业搬家。\n- **上门安装**：空调安装、家电安装、厨卫安装、家具安装。\n\n服务项目的价格、服务范围、可预约时间和营业状态由商家资料及在线服务项目决定，知识库不生成或猜测这些实时信息。\n\n## 二、保洁与清洗\n\n### 家庭保洁包含什么？\n\n家庭保洁通常包括地面、台面、门窗可触及表面、卫生间和厨房等常规清洁。深度清洁、玻璃外侧、高空作业、重油污和大件搬移是否包含，应在预约前查看商家服务说明并向商家确认。\n\n### 家电清洗需要注意什么？\n\n空调、冰箱、洗衣机等家电清洗前，应确认设备型号、安装位置、是否通电以及是否存在漏水或故障。清洗服务不等于维修；发现故障时应另行预约家电维修。用户应提前移走贵重物品，并保留设备周围的操作空间。\n\n### 油烟机清洗前要准备什么？\n\n请清空灶台附近物品，告知油烟机型号、安装高度和长期未清洗情况。拆洗范围、耗材、严重油污附加费用和是否需要更换配件，以商家确认的服务说明为准。清洗后首次使用应观察是否漏油、漏水或异常噪声。\n\n### 除虫除螨安全吗？\n\n预约时应说明房屋面积、虫害种类、儿童或宠物情况以及过敏史。作业期间按服务人员要求离场或做好隔离，结束后按照药剂说明通风和复位。不要自行混用杀虫剂；对严重或持续虫害，应咨询有资质的专业机构。\n\n## 三、维修服务\n\n### 家电维修如何判断是否需要上门？\n\n提交预约时描述品牌、型号、故障现象、出现时间和是否有异响、漏水或焦味。不要自行拆卸仍在保修期或涉及高压、燃气的设备。上门人员现场检查后可能给出维修方案和费用，平台只记录最终结果，不提供远程安全判断。\n\n### 管道维修需要提供哪些信息？\n\n请说明漏水、堵塞、异味或无水等现象，标明厨房、卫生间或户外位置，并提供可见管件照片（不要上传身份证、银行卡等敏感信息）。若发生大量漏水，应先关闭总阀并联系物业或紧急维修，避免财产损失。\n\n### 房屋维修包含结构改造吗？\n\n普通房屋维修可涉及门窗、墙面、五金和小型设施修复。承重结构、燃气、电力主线路和需要行政许可的施工不属于普通上门维修范围，应联系物业、专业施工单位或主管部门确认。\n\n## 四、钟点工与保姆\n\n### 住家保姆、白班保姆和钟点工有什么区别？', 'FAILED', 'home-service-faq-v2-c0', '2026-09-08 06:25:09.292', '2026-09-08 06:25:09.617');
INSERT INTO `rag_document_chunk` VALUES (5, 1, 2, 'home-service-faq-v2-c1', 1, '普通房屋维修可涉及门窗、墙面、五金和小型设施修复。承重结构、燃气、电力主线路和需要行政许可的施工不属于普通上门维修范围，应联系物业、专业施工单位或主管部门确认。\n\n## 四、钟点工与保姆\n\n### 住家保姆、白班保姆和钟点工有什么区别？\n\n住家保姆通常在约定住所提供较长时段服务；白班保姆在约定白天时段工作；钟点工按小时或约定时段提供单次、周期性服务。具体工作内容、时长、休息和费用必须以服务项目及双方确认的订单为准。\n\n### 如何保护家庭隐私和安全？\n\n只向平台和商家提供完成服务所必需的信息，不要在聊天或表单中发送银行卡密码、验证码、身份证完整号码等敏感资料。贵重物品应妥善保管；如发现异常，应停止服务并联系平台。平台当前不建设工作人员档案、排班或线上交流功能，工作人员由平台线下联系安排。\n\n## 五、搬家服务\n\n### 居家搬家、日常搬家和企业搬家如何选择？\n\n居家搬家适用于家庭住所迁移，日常搬家适用于少量物品或短距离搬运，企业搬家适用于办公室、门店等批量物品迁移。预约时应填写起止地址、楼层、电梯情况、物品数量和是否需要拆装或打包，以便商家评估。\n\n### 搬家前有哪些准备？\n\n提前分类、打包并标记易碎品和贵重物品，确认小区出入口、停车和物业规定。证件、现金、首饰、电脑存储介质等贵重物品建议由用户自行携带。大件、危险品、违禁品是否承运必须事先向商家确认。\n\n## 六、上门安装\n\n### 空调安装前需要确认什么？\n\n请确认空调型号、安装位置、墙体条件、外机位置、电源和物业要求。高空、打孔、加长管线、支架及材料费用可能单独计算，实际金额以现场检查和订单为准。涉及高空作业时必须由具备相应能力的人员执行，用户不要自行攀爬操作。\n\n### 家电、厨卫和家具安装有什么注意事项？\n\n安装前准备好产品说明书、配件和安装空间，确认墙体或地面承重、进水排水和电源条件。安装服务不自动包含改造线路、燃气接入或材料采购，超出服务范围的项目须经用户确认后再处理。\n\n## 七、搜索、预约和订单\n\n### 如何搜索附近商家？\n\n可以按服务类别、关键词、省市区和距离筛选商家。允许定位时，系统使用高德地图返回的经纬度计算附近范围；请检查定位城市和地址是否正确。附近查询需要同时提供经度、纬度和半径，最大半径以接口约束为准。\n\n### 如何提交预约？\n\n登录后选择服务项目和商家，填写服务地址、联系人、联系电话、期望时间及备注，提交预约申请。预约初始状态为“待平台处理”，平台和用户均可查看。请勿在预约阶段将其理解为已生成订单或已支付。\n\n### 平台收到预约后会做什么？\n\n平台查看预约并在线下自行联系合适的工作人员，安排到指定地点服务。工作人员现场估价后，平台录入最终服务结果并生成订单草稿，再发布给用户确认。当前不提供工作人员线上排班、聊天或自动派单功能。\n\n### 订单如何支付和确认？', 'FAILED', 'home-service-faq-v2-c1', '2026-09-08 06:25:09.301', '2026-09-08 06:25:09.617');
INSERT INTO `rag_document_chunk` VALUES (6, 1, 2, 'home-service-faq-v2-c2', 2, '约后会做什么？\n\n平台查看预约并在线下自行联系合适的工作人员，安排到指定地点服务。工作人员现场估价后，平台录入最终服务结果并生成订单草稿，再发布给用户确认。当前不提供工作人员线上排班、聊天或自动派单功能。\n\n### 订单如何支付和确认？\n\n订单发布后用户可查看服务快照和应付金额，在支持的 Stripe 沙盒模式下发起支付。支付成功后仍需按照页面提示确认订单；金额、状态和支付结果以订单及支付接口返回为准，不以聊天文字为准。\n\n### 可以取消预约或订单吗？\n\n待平台处理的预约可按页面入口申请取消，系统会校验当前状态和资源归属。订单取消、退款及支付争议受订单状态和当前阶段能力限制；本阶段不提供自动退款，遇到异常应联系平台人工处理。\n\n## 八、商家入驻\n\n### 商家如何申请入驻？\n\n登录后填写商铺名称、服务范围、联系人和位置，使用地图选点或输入地址，并上传必要的门店图片。高德地图用于位置解析和校验，图片由腾讯云 COS 保存对象元数据。提交后由审计管理员审核，审核通过后才建立正式商铺并成为平台用户。\n\n### 入驻申请多久通过？\n\n审核结果取决于资料完整性和管理员处理进度。申请状态、审核备注和结果以平台页面为准；被拒后可根据备注修改资料并重新提交，重复审核会被系统拒绝以避免产生多个正式商铺。\n\n## 九、安全与平台边界\n\n平台不会要求用户提供密码、短信验证码、支付密码或完整银行卡信息。任何要求私下转账、索取验证码或绕过平台支付的行为都应立即停止并联系平台。\n\n知识库只能解释通用流程和注意事项。实时价格、商家营业状态、服务时间、预约、订单、支付和账户信息必须调用受权限保护的业务接口查询。当前暂不支持评价、投诉、售后、优惠券、营销活动、自动派单、工作人员档案/排班/线上交流、平台估价、分账和自动退款等功能；如需这些能力，请使用人工渠道。', 'FAILED', 'home-service-faq-v2-c2', '2026-09-08 06:25:09.308', '2026-09-08 06:25:09.617');

-- ----------------------------
-- Table structure for rag_document_version
-- ----------------------------
DROP TABLE IF EXISTS `rag_document_version`;
CREATE TABLE `rag_document_version`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_id` bigint NOT NULL,
  `version_no` int NOT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `checksum` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'INDEXING' COMMENT 'INDEXING/ACTIVE/RETIRED/FAILED',
  `effective_from` datetime(3) NULL DEFAULT NULL,
  `effective_to` datetime(3) NULL DEFAULT NULL,
  `chunk_count` int NOT NULL DEFAULT 0,
  `index_error` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_by` bigint NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rag_document_version`(`document_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_rag_version_status_effective`(`status` ASC, `effective_from` ASC, `effective_to` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'RAG知识文档版本与原文' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rag_document_version
-- ----------------------------
INSERT INTO `rag_document_version` VALUES (1, 1, 1, '# 家政服务常见问题（内置知识库）\n\n> 文档用途：面向用户的家政服务说明、预约流程和安全提示。本文档是可更新的 RAG 知识源，不是订单、价格或支付事实；具体商家、服务状态、金额和订单状态必须以系统实时查询结果为准。\n\n## 一、服务类别\n\n平台当前内置以下服务分类：\n\n- **保洁清洗**：家庭保洁、家电清洗、油烟机清洗、除虫除螨。\n- **维修服务**：家电维修、管道维修、房屋维修。\n- **钟点工**：住家保姆、白班保姆、钟点工。\n- **搬家服务**：居家搬家、日常搬家、企业搬家。\n- **上门安装**：空调安装、家电安装、厨卫安装、家具安装。\n\n服务项目的价格、服务范围、可预约时间和营业状态由商家资料及在线服务项目决定，知识库不生成或猜测这些实时信息。\n\n## 二、保洁与清洗\n\n### 家庭保洁包含什么？\n\n家庭保洁通常包括地面、台面、门窗可触及表面、卫生间和厨房等常规清洁。深度清洁、玻璃外侧、高空作业、重油污和大件搬移是否包含，应在预约前查看商家服务说明并向商家确认。\n\n### 家电清洗需要注意什么？\n\n空调、冰箱、洗衣机等家电清洗前，应确认设备型号、安装位置、是否通电以及是否存在漏水或故障。清洗服务不等于维修；发现故障时应另行预约家电维修。用户应提前移走贵重物品，并保留设备周围的操作空间。\n\n### 油烟机清洗前要准备什么？\n\n请清空灶台附近物品，告知油烟机型号、安装高度和长期未清洗情况。拆洗范围、耗材、严重油污附加费用和是否需要更换配件，以商家确认的服务说明为准。清洗后首次使用应观察是否漏油、漏水或异常噪声。\n\n### 除虫除螨安全吗？\n\n预约时应说明房屋面积、虫害种类、儿童或宠物情况以及过敏史。作业期间按服务人员要求离场或做好隔离，结束后按照药剂说明通风和复位。不要自行混用杀虫剂；对严重或持续虫害，应咨询有资质的专业机构。\n\n## 三、维修服务\n\n### 家电维修如何判断是否需要上门？\n\n提交预约时描述品牌、型号、故障现象、出现时间和是否有异响、漏水或焦味。不要自行拆卸仍在保修期或涉及高压、燃气的设备。上门人员现场检查后可能给出维修方案和费用，平台只记录最终结果，不提供远程安全判断。\n\n### 管道维修需要提供哪些信息？\n\n请说明漏水、堵塞、异味或无水等现象，标明厨房、卫生间或户外位置，并提供可见管件照片（不要上传身份证、银行卡等敏感信息）。若发生大量漏水，应先关闭总阀并联系物业或紧急维修，避免财产损失。\n\n### 房屋维修包含结构改造吗？\n\n普通房屋维修可涉及门窗、墙面、五金和小型设施修复。承重结构、燃气、电力主线路和需要行政许可的施工不属于普通上门维修范围，应联系物业、专业施工单位或主管部门确认。\n\n## 四、钟点工与保姆\n\n### 住家保姆、白班保姆和钟点工有什么区别？\n\n住家保姆通常在约定住所提供较长时段服务；白班保姆在约定白天时段工作；钟点工按小时或约定时段提供单次、周期性服务。具体工作内容、时长、休息和费用必须以服务项目及双方确认的订单为准。\n\n### 如何保护家庭隐私和安全？\n\n只向平台和商家提供完成服务所必需的信息，不要在聊天或表单中发送银行卡密码、验证码、身份证完整号码等敏感资料。贵重物品应妥善保管；如发现异常，应停止服务并联系平台。平台当前不建设工作人员档案、排班或线上交流功能，工作人员由平台线下联系安排。\n\n## 五、搬家服务\n\n### 居家搬家、日常搬家和企业搬家如何选择？\n\n居家搬家适用于家庭住所迁移，日常搬家适用于少量物品或短距离搬运，企业搬家适用于办公室、门店等批量物品迁移。预约时应填写起止地址、楼层、电梯情况、物品数量和是否需要拆装或打包，以便商家评估。\n\n### 搬家前有哪些准备？\n\n提前分类、打包并标记易碎品和贵重物品，确认小区出入口、停车和物业规定。证件、现金、首饰、电脑存储介质等贵重物品建议由用户自行携带。大件、危险品、违禁品是否承运必须事先向商家确认。\n\n## 六、上门安装\n\n### 空调安装前需要确认什么？\n\n请确认空调型号、安装位置、墙体条件、外机位置、电源和物业要求。高空、打孔、加长管线、支架及材料费用可能单独计算，实际金额以现场检查和订单为准。涉及高空作业时必须由具备相应能力的人员执行，用户不要自行攀爬操作。\n\n### 家电、厨卫和家具安装有什么注意事项？\n\n安装前准备好产品说明书、配件和安装空间，确认墙体或地面承重、进水排水和电源条件。安装服务不自动包含改造线路、燃气接入或材料采购，超出服务范围的项目须经用户确认后再处理。\n\n## 七、搜索、预约和订单\n\n### 如何搜索附近商家？\n\n可以按服务类别、关键词、省市区和距离筛选商家。允许定位时，系统使用高德地图返回的经纬度计算附近范围；请检查定位城市和地址是否正确。附近查询需要同时提供经度、纬度和半径，最大半径以接口约束为准。\n\n### 如何提交预约？\n\n登录后选择服务项目和商家，填写服务地址、联系人、联系电话、期望时间及备注，提交预约申请。预约初始状态为“待平台处理”，平台和用户均可查看。请勿在预约阶段将其理解为已生成订单或已支付。\n\n### 平台收到预约后会做什么？\n\n平台查看预约并在线下自行联系合适的工作人员，安排到指定地点服务。工作人员现场估价后，平台录入最终服务结果并生成订单草稿，再发布给用户确认。当前不提供工作人员线上排班、聊天或自动派单功能。\n\n### 订单如何支付和确认？\n\n订单发布后用户可查看服务快照和应付金额，在支持的 Stripe 沙盒模式下发起支付。支付成功后仍需按照页面提示确认订单；金额、状态和支付结果以订单及支付接口返回为准，不以聊天文字为准。\n\n### 可以取消预约或订单吗？\n\n待平台处理的预约可按页面入口申请取消，系统会校验当前状态和资源归属。订单取消、退款及支付争议受订单状态和当前阶段能力限制；本阶段不提供自动退款，遇到异常应联系平台人工处理。\n\n## 八、商家入驻\n\n### 商家如何申请入驻？\n\n登录后填写商铺名称、服务范围、联系人和位置，使用地图选点或输入地址，并上传必要的门店图片。高德地图用于位置解析和校验，图片由腾讯云 COS 保存对象元数据。提交后由审计管理员审核，审核通过后才建立正式商铺并成为平台用户。\n\n### 入驻申请多久通过？\n\n审核结果取决于资料完整性和管理员处理进度。申请状态、审核备注和结果以平台页面为准；被拒后可根据备注修改资料并重新提交，重复审核会被系统拒绝以避免产生多个正式商铺。\n\n## 九、安全与平台边界\n\n平台不会要求用户提供密码、短信验证码、支付密码或完整银行卡信息。任何要求私下转账、索取验证码或绕过平台支付的行为都应立即停止并联系平台。\n\n知识库只能解释通用流程和注意事项。实时价格、商家营业状态、服务时间、预约、订单、支付和账户信息必须调用受权限保护的业务接口查询。当前暂不支持评价、投诉、售后、优惠券、营销活动、自动派单、工作人员档案/排班/线上交流、平台估价、分账和自动退款等功能；如需这些能力，请使用人工渠道。', 'df19a13cdb3d99fcd3d5fa2c81b6b8d80ff315b3e06c26cda6f3a1e17a20d93e', 'RETIRED', NULL, NULL, 3, 'Embedding 服务暂时不可用', 1, '2026-09-07 02:13:56.056');
INSERT INTO `rag_document_version` VALUES (2, 1, 2, '# 家政服务常见问题（内置知识库）\n\n> 文档用途：面向用户的家政服务说明、预约流程和安全提示。本文档是可更新的 RAG 知识源，不是订单、价格或支付事实；具体商家、服务状态、金额和订单状态必须以系统实时查询结果为准。\n\n## 一、服务类别\n\n平台当前内置以下服务分类：\n\n- **保洁清洗**：家庭保洁、家电清洗、油烟机清洗、除虫除螨。\n- **维修服务**：家电维修、管道维修、房屋维修。\n- **钟点工**：住家保姆、白班保姆、钟点工。\n- **搬家服务**：居家搬家、日常搬家、企业搬家。\n- **上门安装**：空调安装、家电安装、厨卫安装、家具安装。\n\n服务项目的价格、服务范围、可预约时间和营业状态由商家资料及在线服务项目决定，知识库不生成或猜测这些实时信息。\n\n## 二、保洁与清洗\n\n### 家庭保洁包含什么？\n\n家庭保洁通常包括地面、台面、门窗可触及表面、卫生间和厨房等常规清洁。深度清洁、玻璃外侧、高空作业、重油污和大件搬移是否包含，应在预约前查看商家服务说明并向商家确认。\n\n### 家电清洗需要注意什么？\n\n空调、冰箱、洗衣机等家电清洗前，应确认设备型号、安装位置、是否通电以及是否存在漏水或故障。清洗服务不等于维修；发现故障时应另行预约家电维修。用户应提前移走贵重物品，并保留设备周围的操作空间。\n\n### 油烟机清洗前要准备什么？\n\n请清空灶台附近物品，告知油烟机型号、安装高度和长期未清洗情况。拆洗范围、耗材、严重油污附加费用和是否需要更换配件，以商家确认的服务说明为准。清洗后首次使用应观察是否漏油、漏水或异常噪声。\n\n### 除虫除螨安全吗？\n\n预约时应说明房屋面积、虫害种类、儿童或宠物情况以及过敏史。作业期间按服务人员要求离场或做好隔离，结束后按照药剂说明通风和复位。不要自行混用杀虫剂；对严重或持续虫害，应咨询有资质的专业机构。\n\n## 三、维修服务\n\n### 家电维修如何判断是否需要上门？\n\n提交预约时描述品牌、型号、故障现象、出现时间和是否有异响、漏水或焦味。不要自行拆卸仍在保修期或涉及高压、燃气的设备。上门人员现场检查后可能给出维修方案和费用，平台只记录最终结果，不提供远程安全判断。\n\n### 管道维修需要提供哪些信息？\n\n请说明漏水、堵塞、异味或无水等现象，标明厨房、卫生间或户外位置，并提供可见管件照片（不要上传身份证、银行卡等敏感信息）。若发生大量漏水，应先关闭总阀并联系物业或紧急维修，避免财产损失。\n\n### 房屋维修包含结构改造吗？\n\n普通房屋维修可涉及门窗、墙面、五金和小型设施修复。承重结构、燃气、电力主线路和需要行政许可的施工不属于普通上门维修范围，应联系物业、专业施工单位或主管部门确认。\n\n## 四、钟点工与保姆\n\n### 住家保姆、白班保姆和钟点工有什么区别？\n\n住家保姆通常在约定住所提供较长时段服务；白班保姆在约定白天时段工作；钟点工按小时或约定时段提供单次、周期性服务。具体工作内容、时长、休息和费用必须以服务项目及双方确认的订单为准。\n\n### 如何保护家庭隐私和安全？\n\n只向平台和商家提供完成服务所必需的信息，不要在聊天或表单中发送银行卡密码、验证码、身份证完整号码等敏感资料。贵重物品应妥善保管；如发现异常，应停止服务并联系平台。平台当前不建设工作人员档案、排班或线上交流功能，工作人员由平台线下联系安排。\n\n## 五、搬家服务\n\n### 居家搬家、日常搬家和企业搬家如何选择？\n\n居家搬家适用于家庭住所迁移，日常搬家适用于少量物品或短距离搬运，企业搬家适用于办公室、门店等批量物品迁移。预约时应填写起止地址、楼层、电梯情况、物品数量和是否需要拆装或打包，以便商家评估。\n\n### 搬家前有哪些准备？\n\n提前分类、打包并标记易碎品和贵重物品，确认小区出入口、停车和物业规定。证件、现金、首饰、电脑存储介质等贵重物品建议由用户自行携带。大件、危险品、违禁品是否承运必须事先向商家确认。\n\n## 六、上门安装\n\n### 空调安装前需要确认什么？\n\n请确认空调型号、安装位置、墙体条件、外机位置、电源和物业要求。高空、打孔、加长管线、支架及材料费用可能单独计算，实际金额以现场检查和订单为准。涉及高空作业时必须由具备相应能力的人员执行，用户不要自行攀爬操作。\n\n### 家电、厨卫和家具安装有什么注意事项？\n\n安装前准备好产品说明书、配件和安装空间，确认墙体或地面承重、进水排水和电源条件。安装服务不自动包含改造线路、燃气接入或材料采购，超出服务范围的项目须经用户确认后再处理。\n\n## 七、搜索、预约和订单\n\n### 如何搜索附近商家？\n\n可以按服务类别、关键词、省市区和距离筛选商家。允许定位时，系统使用高德地图返回的经纬度计算附近范围；请检查定位城市和地址是否正确。附近查询需要同时提供经度、纬度和半径，最大半径以接口约束为准。\n\n### 如何提交预约？\n\n登录后选择服务项目和商家，填写服务地址、联系人、联系电话、期望时间及备注，提交预约申请。预约初始状态为“待平台处理”，平台和用户均可查看。请勿在预约阶段将其理解为已生成订单或已支付。\n\n### 平台收到预约后会做什么？\n\n平台查看预约并在线下自行联系合适的工作人员，安排到指定地点服务。工作人员现场估价后，平台录入最终服务结果并生成订单草稿，再发布给用户确认。当前不提供工作人员线上排班、聊天或自动派单功能。\n\n### 订单如何支付和确认？\n\n订单发布后用户可查看服务快照和应付金额，在支持的 Stripe 沙盒模式下发起支付。支付成功后仍需按照页面提示确认订单；金额、状态和支付结果以订单及支付接口返回为准，不以聊天文字为准。\n\n### 可以取消预约或订单吗？\n\n待平台处理的预约可按页面入口申请取消，系统会校验当前状态和资源归属。订单取消、退款及支付争议受订单状态和当前阶段能力限制；本阶段不提供自动退款，遇到异常应联系平台人工处理。\n\n## 八、商家入驻\n\n### 商家如何申请入驻？\n\n登录后填写商铺名称、服务范围、联系人和位置，使用地图选点或输入地址，并上传必要的门店图片。高德地图用于位置解析和校验，图片由腾讯云 COS 保存对象元数据。提交后由审计管理员审核，审核通过后才建立正式商铺并成为平台用户。\n\n### 入驻申请多久通过？\n\n审核结果取决于资料完整性和管理员处理进度。申请状态、审核备注和结果以平台页面为准；被拒后可根据备注修改资料并重新提交，重复审核会被系统拒绝以避免产生多个正式商铺。\n\n## 九、安全与平台边界\n\n平台不会要求用户提供密码、短信验证码、支付密码或完整银行卡信息。任何要求私下转账、索取验证码或绕过平台支付的行为都应立即停止并联系平台。\n\n知识库只能解释通用流程和注意事项。实时价格、商家营业状态、服务时间、预约、订单、支付和账户信息必须调用受权限保护的业务接口查询。当前暂不支持评价、投诉、售后、优惠券、营销活动、自动派单、工作人员档案/排班/线上交流、平台估价、分账和自动退款等功能；如需这些能力，请使用人工渠道。', 'df19a13cdb3d99fcd3d5fa2c81b6b8d80ff315b3e06c26cda6f3a1e17a20d93e', 'FAILED', NULL, NULL, 3, 'Embedding 服务暂时不可用', 1, '2026-09-08 06:25:09.245');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime NULL DEFAULT NULL,
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '超级管理员', '2026-09-01 01:33:55', NULL, NULL);
INSERT INTO `role` VALUES (2, '安全管理员', '2026-09-01 01:33:55', NULL, NULL);
INSERT INTO `role` VALUES (3, '审计管理员', '2026-09-01 01:33:55', NULL, NULL);
INSERT INTO `role` VALUES (4, '系统管理员', '2026-09-01 01:33:55', NULL, NULL);
INSERT INTO `role` VALUES (5, '普通用户', '2026-09-01 01:33:55', NULL, NULL);
INSERT INTO `role` VALUES (6, '平台用户', '2026-09-01 01:33:55', NULL, NULL);

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `id` bigint NOT NULL,
  `permission_id` bigint NULL DEFAULT NULL,
  `role_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for service_category
-- ----------------------------
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_parent_name`(`parent_id` ASC, `name` ASC) USING BTREE,
  INDEX `idx_category_parent_sort`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_category
-- ----------------------------
INSERT INTO `service_category` VALUES (1, NULL, '保洁清洗', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (2, 1, '家庭保洁', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (3, 1, '家电清洗', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (4, 1, '油烟机清洗', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (5, 1, '除虫除螨', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (6, NULL, '维修服务', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (7, 6, '家电维修', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (8, 6, '管道维修', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (9, 6, '房屋维修', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (10, NULL, '钟点工', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (11, 10, '住家保姆', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (12, 10, '白班保姆', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (13, 10, '钟点工', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (14, NULL, '搬家服务', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (15, 14, '居家搬家', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (16, 14, '日常搬家', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (17, 14, '企业搬家', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (18, NULL, '上门安装', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (19, 18, '空调安装', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (20, 18, '家电安装', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (21, 18, '厨卫安装', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');
INSERT INTO `service_category` VALUES (22, 18, '家具安装', '2026-09-07 10:07:25.872', '2026-09-07 10:07:25.872');

-- ----------------------------
-- Table structure for service_listing
-- ----------------------------
DROP TABLE IF EXISTS `service_listing`;
CREATE TABLE `service_listing`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shop_id` bigint NULL DEFAULT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `summary` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `pricing_unit` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ORDER' COMMENT 'ORDER/HOUR/SQUARE_METER',
  `base_price` decimal(12, 2) NOT NULL,
  `duration_minutes` int NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/ONLINE/OFFLINE/REJECTED',
  `review_remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_service_shop_status`(`shop_id` ASC, `status` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_service_title`(`title` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务项目' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_listing
-- ----------------------------
INSERT INTO `service_listing` VALUES (6, 4, '服务2', '简介', '描述', 'HOUR', 200.00, 400, 'ONLINE', NULL, '2026-09-09 01:48:32.332', '2026-09-09 01:48:32.332');

-- ----------------------------
-- Table structure for service_listing_category
-- ----------------------------
DROP TABLE IF EXISTS `service_listing_category`;
CREATE TABLE `service_listing_category`  (
  `service_id` bigint NULL DEFAULT NULL,
  `category_id` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sl_category`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务分类关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_listing_category
-- ----------------------------
INSERT INTO `service_listing_category` VALUES (1, 2, 1);
INSERT INTO `service_listing_category` VALUES (2, 3, 2);
INSERT INTO `service_listing_category` VALUES (3, 22, 3);
INSERT INTO `service_listing_category` VALUES (4, 3, 6028282407585988706);
INSERT INTO `service_listing_category` VALUES (5, 2, 6095234222894695914);
INSERT INTO `service_listing_category` VALUES (6, 4, 8858433444205117697);

-- ----------------------------
-- Table structure for service_listing_tag
-- ----------------------------
DROP TABLE IF EXISTS `service_listing_tag`;
CREATE TABLE `service_listing_tag`  (
  `service_id` bigint NULL DEFAULT NULL,
  `tag_id` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sl_tag_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务标签关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_listing_tag
-- ----------------------------
INSERT INTO `service_listing_tag` VALUES (6, 2, 4752071736560339574);
INSERT INTO `service_listing_tag` VALUES (6, 1, 6321004023728064679);

-- ----------------------------
-- Table structure for service_order
-- ----------------------------
DROP TABLE IF EXISTS `service_order`;
CREATE TABLE `service_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `service_id` bigint NOT NULL COMMENT '服务ID',
  `worker_id` bigint NULL DEFAULT NULL COMMENT '外部工作人员引用；当前不建立 worker 表、不管理工作人员',
  `address_id` bigint NOT NULL COMMENT '地址ID',
  `scheduled_start` datetime NULL DEFAULT NULL COMMENT '预约开始时间',
  `scheduled_end` datetime NULL DEFAULT NULL COMMENT '预约结束时间',
  `service_address_snapshot` json NULL COMMENT '服务地址快照',
  `service_title_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务名称快照',
  `worker_name_snapshot` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务人员名称快照',
  `origin_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '原始金额',
  `payable_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '用户确认应付金额',
  `source_request_id` bigint NULL DEFAULT NULL COMMENT '来源预约申请ID',
  `published_time` datetime(3) NULL DEFAULT NULL COMMENT '订单发布给用户的时间',
  `confirmed_time` datetime(3) NULL DEFAULT NULL COMMENT '用户确认订单的时间',
  `status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_PUBLISH' COMMENT '状态：PENDING_PUBLISH(待发布)/PENDING_PAYMENT(待支付)/PAID(已支付)/CONFIRMED(已确认)/IN_SERVICE(服务中)/COMPLETED(已完成)/CANCELLED(已取消)/CLOSED(已关闭)',
  `cancel_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '支付过期时间',
  `paid_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `cancelled_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_order_source_request`(`source_request_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_service_id`(`service_id` ASC) USING BTREE,
  INDEX `idx_worker_id`(`worker_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_scheduled_start`(`scheduled_start` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_time` ASC) USING BTREE,
  INDEX `idx_user_status_created`(`user_id` ASC, `status` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_shop_status_created`(`shop_id` ASC, `status` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_worker_status_start`(`worker_id` ASC, `status` ASC, `scheduled_start` ASC) USING BTREE,
  INDEX `idx_status_expire`(`status` ASC, `expire_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_order
-- ----------------------------
INSERT INTO `service_order` VALUES (1, 'SOdda72a0eab6740b3b14e679a', 7, 4, 6, NULL, 3, '2026-09-09 17:00:54', '2026-09-14 13:06:00', '{\"city\": \"西安市\", \"detail\": \"陕西省西安市未央区张家堡街道凤城八路60号长和·上尚郡\", \"district\": \"未央区\", \"latitude\": 34.339685, \"province\": \"陕西省\", \"longitude\": 108.931839, \"receiverName\": \"haozi111\", \"receiverPhone\": \"15829402866\"}', '服务2', NULL, 50.00, 50.00, 1, '2026-09-09 01:52:11.247', NULL, 'PENDING_PAYMENT', NULL, NULL, '2026-09-10 01:52:11', NULL, NULL, '2026-09-09 01:52:11', '2026-09-09 01:52:11');

-- ----------------------------
-- Table structure for service_order_status_history
-- ----------------------------
DROP TABLE IF EXISTS `service_order_status_history`;
CREATE TABLE `service_order_status_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `from_status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `to_status` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `operator_user_id` bigint NULL DEFAULT NULL,
  `reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `idempotency_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_history_order`(`order_id` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_order_history_operator`(`operator_user_id` ASC, `created_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单状态审计历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_order_status_history
-- ----------------------------
INSERT INTO `service_order_status_history` VALUES (1, 1, NULL, 'PENDING_PUBLISH', 9, '平台根据现场估价生成订单', '16559040-8585-4197-a8b0-384e1327bbf0', '2026-09-09 01:52:11.186');
INSERT INTO `service_order_status_history` VALUES (2, 1, 'PENDING_PUBLISH', 'PENDING_PAYMENT', 9, '商家发布订单', NULL, '2026-09-09 01:52:11.247');

-- ----------------------------
-- Table structure for service_tag
-- ----------------------------
DROP TABLE IF EXISTS `service_tag`;
CREATE TABLE `service_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_service_tag_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务标签' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_tag
-- ----------------------------
INSERT INTO `service_tag` VALUES (1, '标签1', '2026-09-09 01:48:32.405');
INSERT INTO `service_tag` VALUES (2, '标签2', '2026-09-09 01:48:32.424');

-- ----------------------------
-- Table structure for shop
-- ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_user_id` bigint NOT NULL,
  `application_id` bigint NULL DEFAULT NULL,
  `shop_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `shop_logo_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `shop_logo_object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `shop_intro` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `service_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `longitude` decimal(10, 7) NULL DEFAULT NULL,
  `latitude` decimal(10, 7) NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'OPEN' COMMENT 'OPEN/CLOSED/SUSPENDED',
  `created_at` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `service_radius_km` decimal(6, 2) NULL DEFAULT NULL COMMENT '门店周边服务覆盖半径（公里）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_merchant`(`merchant_user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_shop_application`(`application_id` ASC) USING BTREE,
  INDEX `idx_shop_status`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_shop_city_district_status`(`city` ASC, `district` ASC, `status` ASC) USING BTREE,
  INDEX `idx_shop_geo_status`(`status` ASC, `latitude` ASC, `longitude` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务商店铺' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shop
-- ----------------------------
INSERT INTO `shop` VALUES (1, 6, 1, '暖居示范家政服务中心', NULL, NULL, '提供保洁清洗、家电清洗与家具安装服务。', '13800000006', '示例市及周边', '示例市示例区暖居路 1 号', '示例省', '示例市', '示例区', 116.3971280, 39.9165270, 'OPEN', '2026-09-07 10:31:39.908', '2026-09-07 10:31:39.908', NULL);
INSERT INTO `shop` VALUES (2, 7, 2, '家政服务平台2', NULL, 'u/7/merchant-logo/20260908/303023c8-753b-4971-98d1-687bc2dd7cf5.png', 'jianjie', '15829402855', '保洁', '陕西省西安市莲湖区北院门街道创才幼儿学园回民街', '陕西省', '西安市', NULL, 108.9310000, 34.2625000, 'OPEN', '2026-09-08 06:26:11.418', '2026-09-08 06:26:11.418', NULL);
INSERT INTO `shop` VALUES (3, 8, 3, '家政服务平台3', NULL, 'u/8/merchant-logo/20260909/8121df02-0b8b-482c-a193-81f810b6ffc3.png', '简洁', '15829402844', NULL, '陕西省西安市未央区张家堡街道西安市民族事务委员会西安市人民政府', '陕西省', '西安市', NULL, 108.9397000, 34.3420000, 'OPEN', '2026-09-08 20:40:55.681', '2026-09-08 20:40:55.681', 100.00);
INSERT INTO `shop` VALUES (4, 9, 4, '家政服务平台4', NULL, 'u/9/merchant-logo/20260909/00c53f69-17bd-4831-a343-f399af530dcf.png', '介绍', '15829402844', NULL, '陕西省西安市未央区张家堡街道行政中心地铁站B3口赛高广场', '陕西省', '西安市', '未央区', 108.9462300, 34.3396140, 'OPEN', '2026-09-09 01:48:32.332', '2026-09-09 01:48:32.332', 500.00);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_by` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `lock_time` datetime NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '对应 AuthUserStateEnum：normal/locked/logout',
  `created_time` datetime NULL DEFAULT NULL,
  `updated_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '$2b$10$l.H1WFKtyGvCznHKM0tRL.WmP9UMiymwgbqMuGRDwIW8iW3Eheofm', 'superadmin', 'system', NULL, 'normal', '2026-09-07 10:10:09', '2026-09-07 10:10:09');
INSERT INTO `user` VALUES (2, '$2b$10$l.H1WFKtyGvCznHKM0tRL.WmP9UMiymwgbqMuGRDwIW8iW3Eheofm', 'seadmin', 'system', NULL, 'normal', '2026-09-07 10:10:09', '2026-09-07 10:10:09');
INSERT INTO `user` VALUES (3, '$2b$10$l.H1WFKtyGvCznHKM0tRL.WmP9UMiymwgbqMuGRDwIW8iW3Eheofm', 'audadmin', 'system', NULL, 'normal', '2026-09-07 10:10:09', '2026-09-07 10:10:09');
INSERT INTO `user` VALUES (4, '$2b$10$l.H1WFKtyGvCznHKM0tRL.WmP9UMiymwgbqMuGRDwIW8iW3Eheofm', 'sysadmin', 'system', NULL, 'normal', '2026-09-07 10:10:09', '2026-09-07 10:10:09');
INSERT INTO `user` VALUES (5, '$2b$10$l.H1WFKtyGvCznHKM0tRL.WmP9UMiymwgbqMuGRDwIW8iW3Eheofm', 'user', 'system', NULL, 'normal', '2026-09-07 10:10:09', '2026-09-07 10:10:09');
INSERT INTO `user` VALUES (6, '$2b$10$l.H1WFKtyGvCznHKM0tRL.WmP9UMiymwgbqMuGRDwIW8iW3Eheofm', 'plauser', 'system', NULL, 'normal', '2026-09-07 10:10:09', '2026-09-07 10:10:09');
INSERT INTO `user` VALUES (7, '$2a$10$aA8bP1KQohocoomliJLimewljiQgQCKJbr.DYURtb2j/8FMnpz/RO', 'user2', NULL, NULL, 'normal', '2026-09-07 18:30:37', '2026-09-08 01:34:58');
INSERT INTO `user` VALUES (8, '$2a$10$/JWsmFs/nOpZWm717zwVg.eZz1xhFmBHHOq14UqgcwnHmbOx8sXKa', 'user3', NULL, NULL, 'normal', '2026-09-08 06:33:24', '2026-09-08 06:33:24');
INSERT INTO `user` VALUES (9, '$2a$10$as/DCiffFW/yF1vYFBvtEel5fMnzGC5CZz810L3JsPOd06cjuB4x6', 'user5', NULL, NULL, 'normal', '2026-09-08 20:55:38', '2026-09-08 20:55:38');

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'UNKNOWN/MALE/FEMALE',
  `created_at` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户扩展资料' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_profile
-- ----------------------------
INSERT INTO `user_profile` VALUES (1, 1, '超级管理员', NULL, 'UNKNOWN', '2026-09-07 10:07:26.223', '2026-09-07 10:07:26.223');
INSERT INTO `user_profile` VALUES (2, 2, '安全管理员', NULL, 'UNKNOWN', '2026-09-07 10:07:26.223', '2026-09-07 10:07:26.223');
INSERT INTO `user_profile` VALUES (3, 3, '审计管理员', NULL, 'UNKNOWN', '2026-09-07 10:07:26.223', '2026-09-07 10:07:26.223');
INSERT INTO `user_profile` VALUES (4, 4, '系统管理员', NULL, 'UNKNOWN', '2026-09-07 10:07:26.223', '2026-09-07 10:07:26.223');
INSERT INTO `user_profile` VALUES (5, 5, '普通用户', NULL, 'UNKNOWN', '2026-09-07 10:07:26.223', '2026-09-07 10:07:26.223');
INSERT INTO `user_profile` VALUES (6, 6, '平台用户', NULL, 'UNKNOWN', '2026-09-07 10:07:26.223', '2026-09-07 10:07:26.223');
INSERT INTO `user_profile` VALUES (7, 7, 'user2', 'u/7/avatar/20260908/9f7bca24-8b1a-4fc8-815a-83c02410d96b.png', 'MALE', '2026-09-07 18:30:36.957', '2026-09-07 22:43:48.141');
INSERT INTO `user_profile` VALUES (8, 8, 'user3', NULL, NULL, '2026-09-08 06:33:23.660', '2026-09-08 06:33:23.660');
INSERT INTO `user_profile` VALUES (9, 9, 'user5', NULL, NULL, '2026-09-08 20:55:37.557', '2026-09-08 20:55:37.557');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint NOT NULL,
  `role_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1, 1);
INSERT INTO `user_role` VALUES (2, 2, 2);
INSERT INTO `user_role` VALUES (3, 3, 3);
INSERT INTO `user_role` VALUES (4, 4, 4);
INSERT INTO `user_role` VALUES (5, 5, 5);
INSERT INTO `user_role` VALUES (6, 6, 6);
INSERT INTO `user_role` VALUES (7, 6, 7);
INSERT INTO `user_role` VALUES (8, 6, 8);
INSERT INTO `user_role` VALUES (9, 6, 9);

SET FOREIGN_KEY_CHECKS = 1;

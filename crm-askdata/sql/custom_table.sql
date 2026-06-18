DROP TABLE IF EXISTS `dws_contract_daily`;
CREATE TABLE `dws_contract_daily`  (
    `stat_date` date NOT NULL COMMENT '统计日期',
    `sign_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '签约月份（yyyy-MM）',
    `region_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区域编码',
    `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
    `contract_count` int(11) NULL DEFAULT 0 COMMENT '合同数量',
    `contract_amount` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '合同金额',
    `avg_contract_amt` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '平均合同金额',
    INDEX `idx_sign_month`(`sign_month`) USING BTREE,
    INDEX `idx_region`(`region_code`) USING BTREE,
    INDEX `idx_dept`(`dept_id`) USING BTREE
) CHARACTER SET = utf8mb4 COMMENT = '合同每日聚合宽表';

-- ----------------------------
-- Records of dws_contract_daily
-- ----------------------------
INSERT INTO `dws_contract_daily` VALUES ('2026-01-15', '2026-01', 'huadong', 101, 120, 3600000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-01-15', '2026-01', 'huabei', 102, 95, 2850000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-01-31', '2026-01', 'huadong', 101, 45, 1350000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-01-31', '2026-01', 'huabei', 102, 30, 900000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-02-15', '2026-02', 'huadong', 101, 110, 3300000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-02-15', '2026-02', 'huabei', 102, 88, 2640000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-02-28', '2026-02', 'huadong', 101, 40, 1200000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-02-28', '2026-02', 'huabei', 102, 25, 750000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-03-15', '2026-03', 'huadong', 101, 135, 4050000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-03-15', '2026-03', 'huabei', 102, 102, 3060000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-03-31', '2026-03', 'huadong', 101, 50, 1500000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-03-31', '2026-03', 'huabei', 102, 35, 1050000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-04-15', '2026-04', 'huadong', 101, 128, 3840000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-04-15', '2026-04', 'huabei', 102, 98, 2940000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-04-30', '2026-04', 'huadong', 101, 42, 1260000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-04-30', '2026-04', 'huabei', 102, 28, 840000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-05-15', '2026-05', 'huadong', 101, 142, 4260000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-05-15', '2026-05', 'huabei', 102, 108, 3240000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-05-31', '2026-05', 'huadong', 101, 48, 1440000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-05-31', '2026-05', 'huabei', 102, 32, 960000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-06-15', '2026-06', 'huadong', 101, 138, 4140000.00, 30000.00);
INSERT INTO `dws_contract_daily` VALUES ('2026-06-15', '2026-06', 'huabei', 102, 105, 3150000.00, 30000.00);




DROP TABLE IF EXISTS `dws_customer_daily`;
CREATE TABLE `dws_customer_daily`  (
    `stat_date` date NOT NULL COMMENT '统计日期',
    `create_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建月份（yyyy-MM）',
    `region_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区域编码',
    `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
    `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
    INDEX `idx_create_month`(`create_month`) USING BTREE,
    INDEX `idx_region`(`region_code`) USING BTREE,
    INDEX `idx_dept`(`dept_id`) USING BTREE
) CHARACTER SET = utf8mb4 COMMENT = '客户每日新增宽表（测试用）';

-- ----------------------------
-- Records of dws_customer_daily
-- ----------------------------
INSERT INTO `dws_customer_daily` VALUES ('2026-01-05', '2026-01', 'hangzhou', 101, 1001);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-06', '2026-01', 'hangzhou', 101, 1002);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-07', '2026-01', 'hangzhou', 101, 1003);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-08', '2026-01', 'hangzhou', 102, 1004);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-09', '2026-01', 'hangzhou', 102, 1005);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-10', '2026-01', 'ningbo', 101, 1006);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-11', '2026-01', 'ningbo', 101, 1007);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-12', '2026-01', 'ningbo', 102, 1008);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-13', '2026-01', 'wenzhou', 101, 1009);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-14', '2026-01', 'wenzhou', 102, 1010);
INSERT INTO `dws_customer_daily` VALUES ('2026-01-15', '2026-01', 'jiaxing', 101, 1011);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-05', '2026-02', 'hangzhou', 101, 2001);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-06', '2026-02', 'hangzhou', 101, 2002);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-07', '2026-02', 'hangzhou', 102, 2003);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-08', '2026-02', 'hangzhou', 102, 2004);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-09', '2026-02', 'ningbo', 101, 2005);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-10', '2026-02', 'ningbo', 102, 2006);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-11', '2026-02', 'wenzhou', 101, 2007);
INSERT INTO `dws_customer_daily` VALUES ('2026-02-12', '2026-02', 'jiaxing', 101, 2008);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-05', '2026-03', 'hangzhou', 101, 3001);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-06', '2026-03', 'hangzhou', 101, 3002);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-07', '2026-03', 'hangzhou', 101, 3003);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-08', '2026-03', 'hangzhou', 102, 3004);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-09', '2026-03', 'ningbo', 101, 3005);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-10', '2026-03', 'ningbo', 101, 3006);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-11', '2026-03', 'ningbo', 102, 3007);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-12', '2026-03', 'wenzhou', 101, 3008);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-13', '2026-03', 'wenzhou', 102, 3009);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-14', '2026-03', 'jiaxing', 101, 3010);
INSERT INTO `dws_customer_daily` VALUES ('2026-03-15', '2026-03', 'jiaxing', 102, 3011);
INSERT INTO `dws_customer_daily` VALUES ('2026-04-05', '2026-04', 'hangzhou', 101, 4001);
INSERT INTO `dws_customer_daily` VALUES ('2026-04-06', '2026-04', 'hangzhou', 101, 4002);
INSERT INTO `dws_customer_daily` VALUES ('2026-04-07', '2026-04', 'hangzhou', 102, 4003);
INSERT INTO `dws_customer_daily` VALUES ('2026-04-08', '2026-04', 'ningbo', 101, 4004);
INSERT INTO `dws_customer_daily` VALUES ('2026-04-09', '2026-04', 'ningbo', 102, 4005);
INSERT INTO `dws_customer_daily` VALUES ('2026-04-10', '2026-04', 'wenzhou', 101, 4006);
INSERT INTO `dws_customer_daily` VALUES ('2026-04-11', '2026-04', 'jiaxing', 101, 4007);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-05', '2026-05', 'hangzhou', 101, 5001);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-06', '2026-05', 'hangzhou', 101, 5002);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-07', '2026-05', 'hangzhou', 101, 5003);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-08', '2026-05', 'hangzhou', 102, 5004);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-09', '2026-05', 'hangzhou', 102, 5005);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-10', '2026-05', 'ningbo', 101, 5006);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-11', '2026-05', 'ningbo', 101, 5007);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-12', '2026-05', 'ningbo', 102, 5008);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-13', '2026-05', 'wenzhou', 101, 5009);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-14', '2026-05', 'wenzhou', 101, 5010);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-15', '2026-05', 'jiaxing', 101, 5011);
INSERT INTO `dws_customer_daily` VALUES ('2026-05-16', '2026-05', 'jiaxing', 102, 5012);
INSERT INTO `dws_customer_daily` VALUES ('2026-06-05', '2026-06', 'hangzhou', 101, 6001);
INSERT INTO `dws_customer_daily` VALUES ('2026-06-06', '2026-06', 'hangzhou', 101, 6002);
INSERT INTO `dws_customer_daily` VALUES ('2026-06-07', '2026-06', 'hangzhou', 102, 6003);
INSERT INTO `dws_customer_daily` VALUES ('2026-06-08', '2026-06', 'ningbo', 101, 6004);
INSERT INTO `dws_customer_daily` VALUES ('2026-06-09', '2026-06', 'ningbo', 102, 6005);
INSERT INTO `dws_customer_daily` VALUES ('2026-06-10', '2026-06', 'wenzhou', 101, 6006);
INSERT INTO `dws_customer_daily` VALUES ('2026-06-11', '2026-06', 'jiaxing', 101, 6007);

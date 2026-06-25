-- 自然客户开发者表（目标库 zj_sj_test，结构与源库 zq_user_36 一致）
-- 用于同步浙江省（province_code='36'）有效（state='1'）开发者数据

DROP TABLE IF EXISTS `cust_t_nature_customer_developer`;

CREATE TABLE `cust_t_nature_customer_developer` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nature_cust_id` BIGINT(20) DEFAULT NULL COMMENT '自然客户ID',
    `nature_cust_name` VARCHAR(400) DEFAULT NULL COMMENT '自然客户名称',
    `developer_id` VARCHAR(20) DEFAULT NULL COMMENT '开发者ID',
    `developer_name` VARCHAR(50) DEFAULT NULL COMMENT '开发者名称',
    `main` CHAR(2) DEFAULT NULL COMMENT '是否主负责人',
    `email` VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
    `phone_number` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `create_date` DATETIME DEFAULT NULL COMMENT '创建时间',
    `state` CHAR(2) DEFAULT NULL COMMENT '状态（1=有效）',
    `operation_code` VARCHAR(20) DEFAULT NULL COMMENT '操作编码',
    `update_date` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `dev_staff_code` VARCHAR(255) DEFAULT NULL COMMENT '开发者员工编码',
    `province_code` VARCHAR(20) DEFAULT NULL COMMENT '省份编码',
    `eparchy_code` VARCHAR(20) DEFAULT NULL COMMENT '地市编码',
    `city_code` VARCHAR(20) DEFAULT NULL COMMENT '区县编码',
    `oa_code` VARCHAR(255) DEFAULT NULL COMMENT 'OA账号',
    `channel_name` VARCHAR(255) DEFAULT NULL COMMENT '渠道名称',
    `channel_code` VARCHAR(50) DEFAULT NULL COMMENT '渠道编码',
    `org_name` VARCHAR(255) DEFAULT NULL COMMENT '组织名称',
    `service_level` CHAR(1) DEFAULT NULL COMMENT '服务等级',
    `developer_type` VARCHAR(2) DEFAULT NULL COMMENT '开发者类型',
    `org_code` VARCHAR(30) DEFAULT NULL COMMENT '组织编码',
    `tydm` VARCHAR(30) DEFAULT NULL COMMENT '统一代码',
    `reg_province_code` VARCHAR(10) DEFAULT NULL COMMENT '注册省份编码',
    `reg_eparchy_code` VARCHAR(10) DEFAULT NULL COMMENT '注册地市编码',
    `reg_city_code` VARCHAR(10) DEFAULT NULL COMMENT '注册区县编码',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自然客户开发者表（同步自zq_user_36）';

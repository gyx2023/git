-- 浙江省自然客户省内缓存表
-- 用于行前报告页面客户模糊查询

DROP TABLE IF EXISTS `zqzt_travel_nature_cust`;

CREATE TABLE `zqzt_travel_nature_cust` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nature_cust_id` VARCHAR(64) NOT NULL COMMENT '自然客户编码',
    `nature_cust_name` VARCHAR(255) NOT NULL COMMENT '自然客户名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_nature_cust_id` (`nature_cust_id`),
    INDEX `idx_nature_cust_name` (`nature_cust_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浙江省自然客户缓存表（行前报告客户查询）';

-- 通用特殊名单表
DROP TABLE IF EXISTS `zqzt_sys_speciallist`;
CREATE TABLE `zqzt_sys_speciallist` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `scene_code` VARCHAR(64) DEFAULT NULL COMMENT '场景编码，区分不同业务场景',
    `target_type` VARCHAR(32) DEFAULT NULL COMMENT '目标类型：PHONE-手机号, USER_ID-用户ID, CUST_ID-客户ID, IP-IP地址',
    `target_value` VARCHAR(255) DEFAULT NULL COMMENT '特殊名单目标值（存放具体号码、用户ID、IP等）',
    `status` CHAR(1) DEFAULT NULL COMMENT '状态：0-失效（停用）、1-生效（启用）',
    `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间（NULL=立即生效，仅在status=1时有效）',
    `expire_time` DATETIME DEFAULT NULL COMMENT '失效时间（NULL=永不过期，仅在status=1时有效）',
    `reason` VARCHAR(512) DEFAULT NULL COMMENT '加入特殊名单原因（便于运营追溯）',
    `create_by` INT(11) DEFAULT NULL COMMENT '创建人ID，关联 sys_user.id_',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `update_by` INT(11) DEFAULT NULL COMMENT '更新人ID，关联 sys_user.id_',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scene_target_value` (`scene_code`, `target_type`, `target_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用特殊名单表';

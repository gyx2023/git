CREATE TABLE `identify_score_customer` (
                                           `id_` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `area_id` int(11) DEFAULT NULL COMMENT '地市id，上传人员归属的地市',
                                           `county_id` int(11) DEFAULT NULL COMMENT '县分id，上传人员归属的县分',
                                           `cust_id` varchar(50) DEFAULT NULL COMMENT '客户编号，本次先为空，后续有需要再沉淀',
                                           `cust_name` varchar(100) DEFAULT NULL COMMENT '客户名称',
                                           `file_name` varchar(200) DEFAULT NULL COMMENT '附件名称',
                                           `file_url_id` int(11) DEFAULT NULL COMMENT '附件存放地址编号',
                                           `total_score` varchar(200) DEFAULT NULL COMMENT '评估总分',
                                           `content` text DEFAULT NULL COMMENT '评估详情',
                                           `score_file_name` varchar(200) DEFAULT NULL COMMENT '评估结果附件名称（暂时不用）',
                                           `score_file_url_id` int(11) DEFAULT NULL COMMENT '评估结果附件存放地址编号（暂时不用）',
                                           `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                           `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                           `creator_id` int(11) DEFAULT NULL COMMENT '创建人员ID',
                                           `updater_id` int(11) DEFAULT NULL COMMENT '更新人员ID',
                                           PRIMARY KEY (`id_`) USING BTREE,
    -- 新增常用索引，提升查询效率（根据业务场景优化）
                                           INDEX `idx_area_county` (`area_id`, `county_id`) USING BTREE,
                                           INDEX `idx_cust_id` (`cust_id`) USING BTREE,
                                           INDEX `idx_update_time` (`update_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三清单智能评分记录表';

ALTER TABLE `zj_sj`.`identify_score`
    MODIFY COLUMN `id_` INT(11) NOT NULL AUTO_INCREMENT  FIRST,
    ADD COLUMN `customer_id` INT(11) NULL COMMENT '对应 identify_score_customer表的id',
    MODIFY COLUMN `total_score` VARCHAR(200)  CHARACTER SET utf8 COLLATE  utf8_general_ci NULL COMMENT '总分' AFTER `customer_id`,
    MODIFY COLUMN `content` TEXT  CHARACTER SET utf8 COLLATE  utf8_general_ci NULL COMMENT '智能体接口返回详细内容' AFTER `total_score`,
    MODIFY COLUMN `name_` VARCHAR(200)  CHARACTER SET utf8 COLLATE  utf8_general_ci NULL COMMENT '附件原始名称' AFTER `content`,
    MODIFY COLUMN `score_status` VARCHAR(2)  CHARACTER SET utf8 COLLATE  utf8_general_ci NULL COMMENT '调用接口状态,0待调用，1成功,2失败' AFTER `name_`,
    MODIFY COLUMN `upload_time` DATETIME(0) NULL COMMENT '上传时间' AFTER `score_status`,
    MODIFY COLUMN `uploader_` INT(11) NULL COMMENT '上传人员' AFTER `upload_time`;

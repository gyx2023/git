CREATE TABLE `zqzt_jt_notice_record` (
                                         `id_` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增，唯一标识每条记录）',
                                         `order_id` varchar(64) NOT NULL COMMENT '流程实例ID',
                                         `order_operator_id` varchar(64) DEFAULT NULL COMMENT '订单创建者OA工号',
                                         `order_operator_name` varchar(64) DEFAULT NULL COMMENT '订单创建者OA名称',
                                         `order_province_code` varchar(64) DEFAULT NULL COMMENT '订单创建者省份编码',
                                         `order_city_code` varchar(20) DEFAULT NULL COMMENT '订单创建者地市编码',
                                         `order_status` varchar(32) DEFAULT NULL COMMENT '流程状态（3=结束，1=在途）',
                                         `worksheet_id` varchar(64) DEFAULT NULL COMMENT '任务ID（流程启动/完工等场景使用，与任务无关时为空）',
                                         `task_code` varchar(64) DEFAULT NULL COMMENT '环节编码',
                                         `task_name` varchar(128) DEFAULT NULL COMMENT '任务名称',
                                         `biz_code` varchar(64) DEFAULT NULL COMMENT '流程编码（流程唯一标识）',
                                         `deal_user_code` varchar(64) DEFAULT NULL COMMENT '处理人工号（系统自动任务时可为空）',
                                         `deal_user_name` varchar(128) DEFAULT NULL COMMENT '处理人名称',
                                         `deal_result_code` varchar(32) DEFAULT NULL COMMENT '任务处理状态编码',
                                         `body` longtext DEFAULT NULL COMMENT '入参报文体',
                                         `form_template` longtext DEFAULT NULL COMMENT '表单报文结构（含处理意见和附件）',
                                         `res_body` longtext DEFAULT NULL COMMENT '返回报文体',
                                         `fail_reason` varchar(500) DEFAULT NULL COMMENT '审核不通过/退单原因',
                                         `create_time` datetime DEFAULT NULL COMMENT '记录创建时间',
                                         `update_time` datetime DEFAULT NULL COMMENT '记录更新时间',
                                         PRIMARY KEY (`id_`)
) COMMENT = '集团任务前置通知接口记录表';


CREATE TABLE `zqzt_rpa_form_mapping` (
                                         `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                         `biz_code` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '流程编码',
                                         `task_code` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '环节编码',
                                         `task_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '环节名称',
                                         `form_key` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表单key值',
                                         `form_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表单名称',
                                         `component_key` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '组件key',
                                         `is_component` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否组件0:否，1:是',
                                         `component_template` longtext COLLATE utf8mb4_bin DEFAULT NULL COMMENT '组件模板',
                                         `source_key` varchar(1000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表单对应的取值key',
                                         `source_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表单对应取值名称',
                                         `source_type` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '取值类型:1-表单，2-文件',
                                         `conditions_show` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否条件展示，0-不是，1-是',
                                         `conditions_value` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '条件值',
                                         `value_type` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '默认值类型',
                                         `default_value` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '默认值',
                                         `is_required` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否必填，0-否，1-是',
                                         `create_by` varchar(64) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '创建者',
                                         `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                         `update_by` varchar(64) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '更新者',
                                         `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`)
)COMMENT = 'rpa表单映射表';


INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('739q92rb0h4', '1个月', '1', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('llq461r3obw', '3个月', '2', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('p1oklmnyw0k', '半年', '3', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('mvytdqm08q1', '1年', '4', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('l8rrqzf2rko', '2年', '5', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('ohnrgpuxy8x', '3年', '6', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('ohnrgpuxy8x', '5年', '7', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('9o6syntaj1t', '自动顺延', '8', 'RPA延展期限映射关系', 'timeLimit', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('1', '联通客户双方线下盖章', '0', 'RPA盖章模式映射关系', 'paperlessSign', 'string', '0');
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `remark_`, `type_`, `input_type`, `is_system`) VALUES ('2', '联通线上客户线下盖章', '1', 'RPA盖章模式映射关系', 'paperlessSign', 'string', '0');

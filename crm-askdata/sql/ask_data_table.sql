-- 智能问数-外部系统认证配置表
CREATE TABLE `askdata_auth_config` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `system_name` varchar(100) NOT NULL COMMENT '外部系统名称（如：结算系统、CRM系统）',
    `system_desc` varchar(500) DEFAULT NULL COMMENT '系统描述',
    `caller_id` varchar(100) DEFAULT NULL COMMENT '调用方标识（服务名/应用ID，被调用方可用来做来源校验）',
    `auth_type` varchar(20) NOT NULL DEFAULT 'NONE' COMMENT '认证方式: NONE/SA_TOKEN/API_KEY/BASIC/OAUTH2_CLIENT',
    `auth_config_json` text DEFAULT NULL COMMENT '认证配置JSON（加密存储，不同auth_type结构不同）',
    `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_name` (`system_name`) ,
    KEY `idx_auth_type` (`auth_type`),
    KEY `idx_status` (`status`) ,
    KEY `idx_del_flag` (`del_flag`)
) DEFAULT CHARSET = utf8mb4  COMMENT = '智能问数-外部系统认证配置表';

-- 语义层指标定义表
CREATE TABLE `askdata_meta_metric` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `metric_code` varchar(50) NOT NULL COMMENT '指标代码 (唯一，小写字母+数字+下划线)',
    `metric_name` varchar(100) NOT NULL COMMENT '指标名称',
    `expression` varchar(500) NOT NULL COMMENT '计算表达式',
    `data_type` varchar(20) NOT NULL COMMENT '数据类型：money/percent/count/ratio/date',
    `security_level` int(11) NOT NULL DEFAULT '1' COMMENT '安全等级：1-公开 2-内部 3-保密',
    `unit` varchar(20) DEFAULT NULL COMMENT '单位',
    `allowed_values` json DEFAULT NULL COMMENT '枚举值列表 (防止 AI 参数幻觉)',
    `version` int(11) NOT NULL DEFAULT '1' COMMENT '版本号 (乐观锁)',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1-启用 0-禁用',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `source_table` varchar(200) DEFAULT NULL COMMENT '数据来源表名',
    `source_datasource` varchar(50) DEFAULT 'master' COMMENT '数据源名称（对应 spring.datasource.dynamic.datasource key）',
    `column_definitions` json DEFAULT NULL COMMENT '目标表字段定义，如[{\"name\":\"sign_month\",\"type\":\"string\",\"description\":\"签约月份，格式yyyy-MM\"}]',
    `category` varchar(50) DEFAULT NULL COMMENT '业务分类：sales/contract/customer/finance',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_metric_code` (`metric_code`),
    KEY `idx_status` (`status`) ,
    KEY `idx_version` (`version`)
) DEFAULT CHARSET = utf8mb4 COMMENT = '语义层指标定义表';

-- 智能问数-元数据变更事件表
CREATE TABLE `askdata_meta_change_event` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `event_type` varchar(50) NOT NULL COMMENT '事件类型：CREATE/UPDATE/DELETE',
    `entity_type` varchar(50) NOT NULL COMMENT '实体类型：METRIC',
    `entity_id` bigint(20) NOT NULL COMMENT '实体 ID',
    `entity_code` varchar(50) NOT NULL COMMENT '实体代码',
    `old_version` int(11) DEFAULT NULL COMMENT '旧版本',
    `new_version` int(11) NOT NULL COMMENT '新版本',
    `changed_by` varchar(64) DEFAULT NULL COMMENT '变更人',
    `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_entity` (`entity_type`, `entity_id`),
    KEY `idx_changed_at` (`changed_at`)
) DEFAULT CHARSET = utf8mb4  COMMENT = '智能问数-元数据变更事件表';

-- 智能问数工具配置表
CREATE TABLE `askdata_tool_config` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tool_name` varchar(100) NOT NULL COMMENT '工具唯一标识（对应HiAgent工具名）',
    `tool_display_name` varchar(200) DEFAULT NULL COMMENT '工具显示名称',
    `description` text DEFAULT NULL COMMENT '工具描述（用于AI理解）',
    `api_url` varchar(500) NOT NULL COMMENT '原接口地址',
    `http_method` varchar(10) DEFAULT 'POST' COMMENT 'HTTP方法',
    `content_type` varchar(50) DEFAULT 'application/json' COMMENT '请求类型',
    `required_role` varchar(50) DEFAULT NULL COMMENT '所需角色',
    `dept_filter` tinyint(1) DEFAULT '0' COMMENT '是否需要部门过滤',
    `is_sensitive` tinyint(1) DEFAULT '0' COMMENT '是否敏感工具',
    `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
    `auth_config_id` bigint(20) DEFAULT NULL COMMENT '关联认证配置ID（askdata_auth_config.id，NULL=无需认证）',
    `response_data_path` varchar(200) DEFAULT '$.data' COMMENT '数据提取路径(JSONPath)',
    `response_chart_type` varchar(50) DEFAULT 'table' COMMENT '默认图表类型',
    `response_total_path` varchar(200) DEFAULT NULL COMMENT '总数提取路径',
    `response_schema` text DEFAULT NULL COMMENT '响应 Schema 定义（JSON）\n定义字段映射、数据路径、展示提示等完整响应结构描述',
    `timeout_seconds` int(11) DEFAULT '5' COMMENT '超时时间(秒)',
    `cache_ttl_seconds` int(11) DEFAULT '0' COMMENT '缓存时长(秒)，0表示不缓存',
    `skip_post_process` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否跳过结果后处理：0=正常走result_agent，1=跳过直接返回前端',
    `page_codes` varchar(500) DEFAULT NULL COMMENT '适用页面编码，需与请求pageCode精确匹配',
    `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` int(11) DEFAULT '1' COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `tool_name` (`tool_name`),
    KEY `idx_tool_name` (`tool_name`),
    KEY `idx_enabled` (`is_enabled`),
    KEY `idx_page_codes` (`page_codes`),
    KEY `idx_sensitive` (`is_sensitive`)
) DEFAULT CHARSET = utf8mb4 COMMENT = '智能问数工具配置表';

-- 工具参数定义表
CREATE TABLE `askdata_tool_param` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tool_id` bigint(20) NOT NULL COMMENT '关联工具ID',
    `param_name` varchar(100) NOT NULL COMMENT '参数名（AI提取时用）',
    `param_display_name` varchar(200) DEFAULT NULL COMMENT '参数显示名',
    `param_type` varchar(20) NOT NULL COMMENT '参数类型: string/integer/boolean/array/object',
    `description` text DEFAULT NULL COMMENT '参数描述（帮助AI理解）',
    `is_required` tinyint(1) DEFAULT '0' COMMENT '是否必填',
    `default_value` text DEFAULT NULL COMMENT '默认值（JSON格式）',
    `enum_values` text DEFAULT NULL COMMENT '枚举值（JSON数组）',
    `min_value` decimal(20,4) DEFAULT NULL COMMENT '最小值（数值类型）',
    `max_value` decimal(20,4) DEFAULT NULL COMMENT '最大值（数值类型）',
    `min_length` int(11) DEFAULT NULL COMMENT '最小长度（字符串类型）',
    `max_length` int(11) DEFAULT NULL COMMENT '最大长度（字符串类型）',
    `pattern` varchar(500) DEFAULT NULL COMMENT '正则表达式',
    `target_param_name` varchar(100) DEFAULT NULL COMMENT '映射到原接口的参数名',
    `transform_rule` varchar(100) DEFAULT NULL COMMENT '转换规则标识',
    `transform_config` text DEFAULT NULL COMMENT '转换配置（JSON格式）',
    `clarify_priority` int(11) DEFAULT '99' COMMENT '澄清优先级（数字越小越优先追问）',
    `clarify_question_template` varchar(500) DEFAULT NULL COMMENT '澄清问题模板',
    `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tool_param` (`tool_id`, `param_name`),
    KEY `idx_tool_id` (`tool_id`)
) DEFAULT CHARSET = utf8mb4  COMMENT = '工具参数定义表';

-- ----------------------------
-- Records of askdata_auth_config
-- ----------------------------
INSERT INTO `askdata_auth_config` VALUES (1, '本系统内部工具', '同系统跨模块工具 API 调用，携带当前用户 Token', 'zqzt_backend', 'SA_TOKEN', '{\"headerName\": \"Authorization\", \"headerPrefix\": \"Bearer \"}', '0', 'system', '2026-06-09 19:14:20', '', '2026-06-09 19:14:20', '0');
INSERT INTO `askdata_auth_config` VALUES (2, 'smart_ai_micro', 'AI微服务平台-政企商机看板', 'crm-askdata', 'API_KEY', '{\"headerName\":\"Authorization\",\"headerPrefix\":\"Bearer \",\"apiKey\":\"eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6IjQ5ZWMzYTg0LTRiNDctNGIxYS05ZTUzLWUxZWY5MDExOWMxOCJ9.OKxSz72wya2ardt5uGH67WI0kIxYwUgTm67XtkbC-Ul_mnvhQ6BCkj-DPp3iav7KPml3sRpENWqppv5WJwbZjw\",\"forwardHeaders\":[\"loginoa\",\"loginname\"]}', '0', 'system', '2026-06-10 20:42:31', '', '2026-07-02 08:50:07', '0');

-- ----------------------------
-- Records of askdata_meta_metric
-- ----------------------------
INSERT INTO `askdata_meta_metric` VALUES (6, 'contract_count', '合同数', 'sum(contract_count)', 'count', 1, '个', NULL, 1, 1, 'admin', NULL, '', NULL, 'dws_contract_daily', 'master', '[{\"name\": \"sign_month\", \"type\": \"string\", \"description\": \"签约月份，格式yyyy-MM\"}, {\"name\": \"region_code\", \"type\": \"string\", \"description\": \"区域编码\"}, {\"name\": \"dept_id\", \"type\": \"bigint\", \"description\": \"部门ID\"}, {\"name\": \"contract_count\", \"type\": \"int\", \"description\": \"合同数量\"}, {\"name\": \"contract_amount\", \"type\": \"decimal\", \"description\": \"合同金额\"}, {\"name\": \"avg_contract_amt\", \"type\": \"decimal\", \"description\": \"平均合同金额\"}]', 'contract');
INSERT INTO `askdata_meta_metric` VALUES (11, 'contract_amount', '合同额', 'SUM(contract_amount)', 'money', 1, '元', NULL, 1, 1, 'admin', NULL, '', NULL, 'dws_contract_daily', 'master', '[{\"name\": \"sign_month\", \"type\": \"string\", \"description\": \"签约月份，格式yyyy-MM\"}, {\"name\": \"region_code\", \"type\": \"string\", \"description\": \"区域编码\"}, {\"name\": \"dept_id\", \"type\": \"bigint\", \"description\": \"部门ID\"}, {\"name\": \"contract_count\", \"type\": \"int\", \"description\": \"合同数量\"}, {\"name\": \"contract_amount\", \"type\": \"decimal\", \"description\": \"合同金额\"}, {\"name\": \"avg_contract_amt\", \"type\": \"decimal\", \"description\": \"平均合同金额\"}]', 'contract');
INSERT INTO `askdata_meta_metric` VALUES (12, 'new_customer', '新增客户', 'count(distinct customer_id)', 'count', 1, '个', NULL, 1, 1, 'admin', NULL, '', NULL, 'dws_customer_daily', 'master', '[{\"name\": \"create_month\", \"type\": \"string\", \"description\": \"创建月份，格式yyyy-MM\"}, {\"name\": \"region_code\", \"type\": \"string\", \"description\": \"查全省或者浙江 浙江省 region_code 为999时，不用带条件 ，查具体地市，带 类似 571 的区域编码\"}, {\"name\": \"dept_id\", \"type\": \"bigint\", \"description\": \"部门ID\"}, {\"name\": \"customer_id\", \"type\": \"bigint\", \"description\": \"客户ID\"}]', 'contract');

-- ----------------------------
-- Records of askdata_tool_config
-- ----------------------------
INSERT INTO `askdata_tool_config` VALUES (4, 'query_opportunity_summary', '商机概览聚合查询', '查询政企商机概览数据，按地市聚合返回。支持按时间范围、业务维度(算网数智/联网通信/国际业务)、行业大类(2G/2B)、主业务类型(多选)、客户行业、金额范围筛选。返回各地市的：商机数量、商机金额(万元)、本年签约数量、本年签约金额(万元)、商机转化数量、转化金额(万元)、亿级商机数量、千万级商机数量、商机金额同比(%)、累计拜访次数、平均拜访次数。', 'https://wisdom.loc.zj186.com:9998/prod-api/asiainfo_micro/dmservice/zq_bussness/oppoSummarize', 'POST', 'application/json', NULL, 0, 0, 1, 2, '$.data', 'table', NULL, '{\"version\":\"1.0\",\"dataPath\":\"$.data\",\"fields\":[{\"field\":\"AREA_NAME\",\"label\":\"地市名称\",\"type\":\"string\"},{\"field\":\"TOTAL_COUNT\",\"label\":\"商机数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"EXPECT_CONTRACT_MONEY\",\"label\":\"预计签约金额(万元)\",\"type\":\"currency\",\"format\":\"#,##0.00\"},{\"field\":\"IS_ZH\",\"label\":\"转化数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"ZH_MONEY\",\"label\":\"转化金额(万元)\",\"type\":\"currency\",\"format\":\"#,##0.00\"},{\"field\":\"CUR_YEAR_SING_COUNT\",\"label\":\"本年签约数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"CUR_YEAR_SING_MONEY\",\"label\":\"本年签约金额(万元)\",\"type\":\"currency\",\"format\":\"#,##0.00\"},{\"field\":\"TB_EXPECT_CONTRACT_MONEY\",\"label\":\"金额同比(%)\",\"type\":\"percentage\"},{\"field\":\"YJ_EXPECT_CONTRACT_MONEY\",\"label\":\"亿级商机数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"QW_EXPECT_CONTRACT_MONEY\",\"label\":\"千万级商机数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"FIVE_FUSION_TYPE\",\"label\":\"五融数\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"VISIT_CNT\",\"label\":\"累计拜访次数\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"VISIT_AVG_CNT\",\"label\":\"平均拜访次数\",\"type\":\"number\",\"format\":\"#,##0.00\"}],\"displayHints\":{\"defaultChartType\":\"table\",\"dimensionField\":\"\",\"metricField\":\"\"}}', 30, 60, 0, 'businessBoard', 'system', '2026-06-10 20:46:37', NULL, '2026-06-22 16:26:05', 1);
INSERT INTO `askdata_tool_config` VALUES (5, 'query_sign_summary', '签约概览聚合查询', '查询政企合同/签约概览数据，按地市或区县聚合返回。支持按时间范围、业务类型(数科集成/联通云/数据服务/数智应用/数据中心/联网通信)、客户行业、金额范围筛选。返回各地市/区县的：签约数量、签约金额(万元)、签约金额同比(%)、签约金额完成率(%)、亿级合同数量(>=10000万)、千万级合同数量(1000~10000万)、百万级合同数量(100~1000万)、签约前置商机比例(%)、年化签约金额(万元)。', 'https://wisdom.loc.zj186.com:9998/prod-api/asiainfo_micro/dmservice/zq_bussness/signSummarize', 'POST', 'application/json', NULL, 0, 0, 1, 2, '$.data', 'table', NULL, '{\"version\":\"1.0\",\"dataPath\":\"$.data\",\"fields\":[{\"field\":\"AREA_NAME\",\"label\":\"地市名称\",\"type\":\"string\"},{\"field\":\"TOTAL_COUNT\",\"label\":\"签约数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"ARRAIR_AMOUNT\",\"label\":\"签约金额(万元)\",\"type\":\"currency\",\"format\":\"#,##0.00\"},{\"field\":\"TB_ARRAIR_AMOUNT\",\"label\":\"签约金额同比(%)\",\"type\":\"percentage\"},{\"field\":\"WCL_ARRAIR_AMOUNT\",\"label\":\"完成率\",\"type\":\"percentage\"},{\"field\":\"YJ_ARRAIR_AMOUNT\",\"label\":\"亿级合同数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"QW_ARRAIR_AMOUNT\",\"label\":\"千万级合同数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"BW_ARRAIR_AMOUNT\",\"label\":\"百万级合同数量\",\"type\":\"number\",\"format\":\"#,##0\"},{\"field\":\"PRE_OPPO\",\"label\":\"签约前置商机比例(%)\",\"type\":\"percentage\"},{\"field\":\"YEAR_ARRAIR_AMOUNT\",\"label\":\"年化签约金额(万元)\",\"type\":\"currency\",\"format\":\"#,##0.00\"},{\"field\":\"YEAR_AMOUNT\",\"label\":\"年化签约金额(万元)\",\"type\":\"currency\",\"format\":\"#,##0.00\"}],\"displayHints\":{\"defaultChartType\":\"table\",\"dimensionField\":\"\",\"metricField\":\"\"}}', 30, 60, 0, 'businessBoard', 'system', '2026-06-10 20:47:14', NULL, '2026-06-22 16:26:05', 1);
INSERT INTO `askdata_tool_config` VALUES (6, 'query_opportunity_detail', '商机明细列表查询', '查询政企商机明细列表(分页)，每条记录是一个商机项目。支持按时间范围、地市、区县、金额范围、业务维度、行业大类、主业务类型、客户行业、客户经理、异常商机筛选，并支持排序。返回字段包括：商机编号、名称、创建时间、客户经理、地市、预估金额(万元)、商机状态、主业务类型、客户行业、是否签约、中标金额、合同金额、项目信息、拜访次数、支撑次数、关注状态等。注意：dataType固定传\"SJ\"(商机)，itrstOa为必填(用户OA，用于查询关注状态，传空则无关注数据)。', 'https://wisdom.loc.zj186.com:9998/prod-api/asiainfo_micro/dmservice/zq_bussness/oppoListPage', 'POST', 'application/json', NULL, 0, 0, 1, 2, '$.rows', 'table', '$.total', '{\"version\": \"1.0\",\"dataPath\": \"$.rows\",\"fields\": [{\"field\": \"OPP_CODE\", \"label\": \"商机编号\", \"type\": \"string\"},{\"field\": \"OPP_NAME\", \"label\": \"商机名称\", \"type\": \"string\"},{\"field\": \"CREATE_TIME\", \"label\": \"创建时间\", \"type\": \"string\"},{\"field\": \"CREATE_NAME\", \"label\": \"客户经理\", \"type\": \"string\"},{\"field\": \"AREA_NAME\", \"label\": \"地市\", \"type\": \"string\"},{\"field\": \"COUNTY_NAME\", \"label\": \"区县\", \"type\": \"string\"},{\"field\": \"EXPECT_CONTRACT_MONEY\", \"label\": \"预估金额(万元)\", \"type\": \"currency\", \"format\": \"#,##0.00\"},{\"field\": \"OPPO_STATE\", \"label\": \"商机状态\", \"type\": \"string\"},{\"field\": \"OPPO_TYPE\", \"label\": \"主业务类型\", \"type\": \"string\"},{\"field\": \"TRADE_CLUSTER_NAME\", \"label\": \"客户/企业行业\", \"type\": \"string\"},{\"field\": \"YN_QY\", \"label\": \"是否签约\", \"type\": \"string\"},{\"field\": \"RESULT_MONEY\", \"label\": \"中标金额(万元)\", \"type\": \"currency\", \"format\": \"#,##0.00\"},{\"field\": \"CTRT_MONEY\", \"label\": \"合同金额(万元)\", \"type\": \"currency\", \"format\": \"#,##0.00\"},{\"field\": \"PROJECT_NUMBER\", \"label\": \"项目编码\", \"type\": \"string\"},{\"field\": \"PROJECT_NAME\", \"label\": \"项目名称\", \"type\": \"string\"},{\"field\": \"VISIT_CNT\", \"label\": \"拜访次数\", \"type\": \"number\", \"format\": \"#,##0\"},{\"field\": \"SUPPORT_COUNT\", \"label\": \"支撑次数\", \"type\": \"number\", \"format\": \"#,##0\"},{\"field\": \"DATA_TYPE\", \"label\": \"关注状态\", \"type\": \"string\"},{\"field\": \"DICT_LABEL\", \"label\": \"名单制客户级别\", \"type\": \"string\"}],\"displayHints\": {\"defaultChartType\": \"table\",\"dimensionField\": \"\",\"metricField\": \"\"}}', 30, 0, 1, 'businessBoard', 'system', '2026-06-10 20:48:13', NULL, '2026-06-22 16:26:05', 1);
INSERT INTO `askdata_tool_config` VALUES (7, 'query_sign_detail', '签约明细列表查询', '查询政企签约项目明细列表(分页)，每条记录是一个合同。支持按时间范围、地市、区县、金额范围、业务类型、客户行业、客户经理筛选，并支持排序。返回字段包括：合同编号、合同名称、合同金额(万元)、合同类型、审批日期、业务类型、客户经理、客户名称、客户行业、名单制客户、项目编码、项目名称、关注状态等。注意：dataType固定传\"HT\"(合同)，itrstOa为必填(用户OA，用于查询关注状态)。', 'https://wisdom.loc.zj186.com:9998/prod-api/asiainfo_micro/dmservice/zq_bussness/listPage', 'POST', 'application/json', NULL, 0, 0, 1, 2, '$.rows', 'table', '$.total', '{\"version\": \"1.0\",\"dataPath\": \"$.rows\",\"fields\": [{\"field\": \"ARRAIR_NO\", \"label\": \"合同编号\", \"type\": \"string\"},{\"field\": \"CONTRACT_NAME\", \"label\": \"合同名称\", \"type\": \"string\"},{\"field\": \"ARRAIR_AMOUNT\", \"label\": \"合同金额(万元)\", \"type\": \"currency\", \"format\": \"#,##0.00\"},{\"field\": \"CONTRACT_TYPE\", \"label\": \"合同类型\", \"type\": \"string\"},{\"field\": \"APPROVAL_DATE\", \"label\": \"审批日期\", \"type\": \"string\"},{\"field\": \"SVC_TYPE\", \"label\": \"业务类型\", \"type\": \"string\"},{\"field\": \"MAIN_PERFORMER_USER_NAME\", \"label\": \"客户经理\", \"type\": \"string\"},{\"field\": \"AREA_NAME\", \"label\": \"地市\", \"type\": \"string\"},{\"field\": \"COUNTY_NAME\", \"label\": \"区县\", \"type\": \"string\"},{\"field\": \"NATURE_CUST_NAME\", \"label\": \"客户名称\", \"type\": \"string\"},{\"field\": \"TRADE_CLUSTER_NAME\", \"label\": \"客户/企业行业\", \"type\": \"string\"},{\"field\": \"ROSTER_CUSTOMER_NAME\", \"label\": \"名单制客户\", \"type\": \"string\"},{\"field\": \"PROJECT_CODE\", \"label\": \"项目编码\", \"type\": \"string\"},{\"field\": \"PROJECT_NAME\", \"label\": \"项目名称\", \"type\": \"string\"},{\"field\": \"DATA_TYPE\", \"label\": \"关注状态\", \"type\": \"string\"},{\"field\": \"DICT_LABEL\", \"label\": \"名单制客户级别\", \"type\": \"string\"}],\"displayHints\": {\"defaultChartType\": \"table\",\"dimensionField\": \"\",\"metricField\": \"\"}}', 30, 0, 1, 'businessBoard', 'system', '2026-06-10 20:48:46', NULL, '2026-06-22 16:26:05', 1);
INSERT INTO `askdata_tool_config` VALUES (8, 'get_trade_cluster_list', '客户行业列表查询', '查询政企客户行业下拉列表。用于辅助其他查询工具的tradeClusterId参数。无入参。返回行业ID(TRADE_CLUSTER_ID)和行业名称(SF_TRADE_CLUSTER_NAME)。', 'https://wisdom.loc.zj186.com:9998/prod-api/asiainfo_micro/dmservice/zq_bussness/getCustomer', 'GET', 'application/json', NULL, 0, 0, 1, 2, '$.data', 'table', NULL, NULL, 10, 3600, 0, 'businessBoard', 'system', '2026-06-10 20:52:24', NULL, '2026-06-22 16:26:05', 1);
INSERT INTO `askdata_tool_config` VALUES (9, 'get_business_type_list', '商机主业务类型列表查询', '查询商机主业务类型下拉列表。用于辅助query_opportunity_summary的oppoTypes参数。可选入参svcType(业务维度过滤，如\"算网数智\"/\"联网通信\"/\"国际业务\")。返回业务类型名称列表(TYPE_NAME)。', 'https://wisdom.loc.zj186.com:9998/prod-api/asiainfo_micro/dmservice/zq_bussness/getBusinessType', 'GET', 'application/json', NULL, 0, 0, 1, 2, '$.data', 'table', NULL, NULL, 10, 3600, 0, 'businessBoard', 'system', '2026-06-10 20:52:29', NULL, '2026-06-22 16:26:05', 1);
INSERT INTO `askdata_tool_config` VALUES (10, 'query_kpi_cards', '政企商机KPI指标卡片', '查询政企商机/项目机会看板顶部门户的4个KPI指标卡片数据，不支持按月查询，仅支持查询当年统计数据，日期固定为当年1月1日至昨天，包括：签约合同数量、签约金额+环比、商机数量、商机金额+环比。注意：仅支持查询当年统计数据（日期固定为当年1月1日至昨天，自动计算无需传入），不支持传入时间参数查询历史年份。入参只有areaCode(地市编码)。返回结构包含QY(签约/合同数据)和SJ(商机/项目机会数据)两部分。', 'https://wisdom.loc.zj186.com:9998/prod-api/asiainfo_micro/dmservice/zq_bussness/titleInfo', 'POST', 'application/json', NULL, 0, 0, 1, 2, '$.data', 'summary', NULL, '{\"version\": \"1.0\",\"dataPath\": \"$.data\",\"fields\": [{\"field\": \"TOTAL_COUNT\", \"label\": \"数量\", \"type\": \"metric\", \"format\": \"#,##0\"},{\"field\": \"ARRAIR_AMOUNT\", \"label\": \"签约金额(亿元)\", \"type\": \"metric\", \"format\": \"#,##0.00\"},{\"field\": \"LAST_TOTAL_COUNT\", \"label\": \"数量环比(%)\", \"type\": \"metric\", \"format\": \"0.00\"},{\"field\": \"LAST_ARRAIR_AMOUNT\", \"label\": \"金额环比(%)\", \"type\": \"metric\", \"format\": \"0.00\"},{\"field\": \"EXPECT_CONTRACT_MONEY\", \"label\": \"预计签约金额(亿元)\", \"type\": \"metric\", \"format\": \"#,##0.00\"},{\"field\": \"LAST_EXPECT_CONTRACT_MONEY\", \"label\": \"预计金额环比(%)\", \"type\": \"metric\", \"format\": \"0.00\"}],\"displayHints\": {\"defaultChartType\": \"card\",\"groupFields\": [\"QY\", \"SJ\"],\"groupLabels\": {\"QY\": \"签约\", \"SJ\": \"商机\"}}}', 30, 60, 0, 'businessBoard', 'system', '2026-06-10 20:45:55', NULL, '2026-06-22 16:26:05', 1);

-- ----------------------------
-- Records of askdata_tool_param
-- ----------------------------
INSERT INTO `askdata_tool_param` VALUES (1, 1, 'region', '地区', 'string', '查询地区，可选值：all(全部)、huadong(华东)、huabei(华北)、huanan(华南)、huaxi(华西)', 1, '\"all\"', '[\"all\", \"huadong\", \"huabei\", \"huanan\", \"huaxi\"]', NULL, NULL, NULL, NULL, NULL, 'regionCode', NULL, NULL, 1, NULL, 1);
INSERT INTO `askdata_tool_param` VALUES (2, 1, 'time_range', '时间范围', 'string', '时间范围，如：last_month(上月)、last_quarter(上季度)、ytd(年初至今)', 1, NULL, '[\"last_month\", \"last_quarter\", \"ytd\", \"custom\"]', NULL, NULL, NULL, NULL, NULL, 'timeRange', 'date_range_mapper', NULL, 2, NULL, 2);
INSERT INTO `askdata_tool_param` VALUES (3, 1, 'metrics', '指标列表', 'array', '指标列表，如：销售额、订单量、客单价', 0, '[\"sales_amount\"]', '[\"sales_amount\", \"order_count\", \"avg_price\"]', NULL, NULL, NULL, NULL, NULL, 'metricCodes', NULL, NULL, 3, NULL, 3);
INSERT INTO `askdata_tool_param` VALUES (4, 10, 'areaCode', '地市编码', 'string', '地市编码，全省传\"999\"，杭州=\"571\"，宁波=\"574\"，温州=\"577\"，嘉兴=\"573\"，湖州=\"572\"，绍兴=\"575\"，金华=\"579\"，衢州=\"570\"，舟山=\"580\"，台州=\"576\"，丽水=\"578\"', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'areaCode', NULL, NULL, 1, NULL, 1);
INSERT INTO `askdata_tool_param` VALUES (5, 4, 'areaCode', '地市编码', 'string', '地市编码，全省传\"999\"，杭州=\"571\"，宁波=\"574\"等', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'areaCode', NULL, NULL, 1, NULL, 1);
INSERT INTO `askdata_tool_param` VALUES (6, 4, 'startTime', '开始日期', 'string', '开始日期，格式YYYYMMDD，如\"20260101\"。注意：不支持跨年查询，开始和结束日期必须在同一年。', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'startTime', NULL, NULL, 2, NULL, 2);
INSERT INTO `askdata_tool_param` VALUES (7, 4, 'endTime', '结束日期', 'string', '结束日期，格式YYYYMMDD，如\"20260610\"。注意：不支持跨年查询。', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'endTime', NULL, NULL, 3, NULL, 3);
INSERT INTO `askdata_tool_param` VALUES (8, 4, 'svcType', '业务维度', 'string', '商机业务维度筛选：空字符串=全部商机、\"算网数智\"=算网数智商机、\"联网通信\"=联网通信商机、\"国际业务\"=国际业务商机', 0, '\"\"', '[\"\", \"算网数智\", \"联网通信\", \"国际业务\"]', NULL, NULL, NULL, NULL, NULL, 'svcType', NULL, NULL, 99, NULL, 4);
INSERT INTO `askdata_tool_param` VALUES (9, 4, 'bgType', '行业大类', 'string', '商机行业大类：空字符串=全部、\"2G\"=政府类、\"2B\"=企业类', 0, '\"\"', '[\"\", \"2G\", \"2B\"]', NULL, NULL, NULL, NULL, NULL, 'bgType', NULL, NULL, 99, NULL, 5);
INSERT INTO `askdata_tool_param` VALUES (10, 4, 'tradeClusterId', '客户行业ID', 'string', '客户行业ID，可通过get_trade_cluster_list工具获取。传空字符串=不筛选', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'tradeClusterId', NULL, NULL, 99, NULL, 6);
INSERT INTO `askdata_tool_param` VALUES (11, 4, 'arrairAmountStart', '金额范围下限(万元)', 'integer', '商机金额范围下限，单位万元。与arrairAmountEnd配合使用。例如查询100万以上的商机传\"100\"', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountStart', NULL, NULL, 99, NULL, 7);
INSERT INTO `askdata_tool_param` VALUES (12, 4, 'arrairAmountEnd', '金额范围上限(万元)', 'integer', '商机金额范围上限，单位万元。与arrairAmountStart配合使用，arrairAmountEnd设置了，必须同时设置arrairAmountStart。例如查询1000万以下的商机传\"1000\",arrairAmountStart传\"0\"', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountEnd', NULL, NULL, 99, NULL, 8);
INSERT INTO `askdata_tool_param` VALUES (13, 4, 'oppoTypes', '主业务类型列表', 'array', '商机主业务类型名称列表(多选)，如[\"云计算\",\"大数据\"]。可通过get_business_type_list工具获取可选值。空数组=不筛选', 0, '[]', NULL, NULL, NULL, NULL, NULL, NULL, 'oppoTypes', NULL, NULL, 99, NULL, 9);
INSERT INTO `askdata_tool_param` VALUES (14, 5, 'areaCode', '地市编码', 'string', '地市编码，全省传\"999\"，杭州=\"571\"，宁波=\"574\"等', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'areaCode', NULL, NULL, 1, NULL, 1);
INSERT INTO `askdata_tool_param` VALUES (15, 5, 'startTime', '开始日期', 'string', '开始日期，格式YYYYMMDD，如\"20260101\"。注意：不支持跨年查询。', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'startTime', NULL, NULL, 2, NULL, 2);
INSERT INTO `askdata_tool_param` VALUES (16, 5, 'endTime', '结束日期', 'string', '结束日期，格式YYYYMMDD，如\"20260610\"。注意：不支持跨年查询。', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'endTime', NULL, NULL, 3, NULL, 3);
INSERT INTO `askdata_tool_param` VALUES (17, 5, 'svcType', '业务类型', 'string', '业务类型筛选：空字符串=全部、\"数科集成\"=\"数科集成\"、\"联通云\"=\"联通云\"、\"数据服务\"=\"数据服务\"、\"数智应用\"=\"数智应用\"、\"数据中心\"=\"数据中心\"、\"联网通信\"=\"联网通信\"', 0, '\"\"', '[\"\", \"数科集成\", \"联通云\", \"数据服务\", \"数智应用\", \"数据中心\", \"联网通信\"]', NULL, NULL, NULL, NULL, NULL, 'svcType', NULL, NULL, 99, NULL, 4);
INSERT INTO `askdata_tool_param` VALUES (18, 5, 'tradeClusterId', '客户行业ID', 'string', '客户行业ID，可通过get_trade_cluster_list工具获取。传空字符串=不筛选', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'tradeClusterId', NULL, NULL, 99, NULL, 5);
INSERT INTO `askdata_tool_param` VALUES (19, 5, 'arrairAmountStart', '金额范围下限(万元)', 'integer', '合同金额范围下限，单位万元。与arrairAmountEnd配合使用。', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountStart', NULL, NULL, 99, NULL, 6);
INSERT INTO `askdata_tool_param` VALUES (20, 5, 'arrairAmountEnd', '金额范围上限(万元)', 'integer', '合同金额范围上限，单位万元。与arrairAmountStart配合使用。', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountEnd', NULL, NULL, 99, NULL, 7);
INSERT INTO `askdata_tool_param` VALUES (21, 6, 'areaCode', '地市编码', 'string', '地市编码，全省传\"999\"，杭州=\"571\"，宁波=\"574\"等', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'areaCode', NULL, NULL, 1, NULL, 1);
INSERT INTO `askdata_tool_param` VALUES (22, 6, 'startTime', '开始日期', 'string', '开始日期(商机创建时间)，格式YYYYMMDD，如\"20260101\"', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'startTime', NULL, NULL, 2, NULL, 2);
INSERT INTO `askdata_tool_param` VALUES (23, 6, 'endTime', '结束日期', 'string', '结束日期(商机创建时间)，格式YYYYMMDD，如\"20260610\"', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'endTime', NULL, NULL, 3, NULL, 3);
INSERT INTO `askdata_tool_param` VALUES (24, 6, 'pageNum', '页码', 'integer', '分页页码，从1开始', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'pageNum', NULL, NULL, 4, NULL, 4);
INSERT INTO `askdata_tool_param` VALUES (25, 6, 'pageSize', '每页条数', 'integer', '每页返回记录数，建议20', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'pageSize', NULL, NULL, 5, NULL, 5);
INSERT INTO `askdata_tool_param` VALUES (26, 6, 'dataType', '数据类型(固定)', 'string', '固定传\"SJ\"表示查询商机数据。系统自动填充，AI无需关心。', 0, '\"SJ\"', NULL, NULL, NULL, NULL, NULL, NULL, 'dataType', NULL, NULL, 99, NULL, 99);
INSERT INTO `askdata_tool_param` VALUES (27, 6, 'itrstOa', '用户OA(固定)', 'string', '用户OA账号，用于查询关注状态。系统自动填充（需改造为从上下文获取），目前固定传\"SYSTEM\"。', 0, '\"SYSTEM\"', NULL, NULL, NULL, NULL, NULL, NULL, 'itrstOa', NULL, NULL, 99, NULL, 99);
INSERT INTO `askdata_tool_param` VALUES (28, 6, 'countyCode', '区县编码', 'string', '区县编码，如\"57101\"。传空=不按区县筛选', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'countyCode', NULL, NULL, 99, NULL, 10);
INSERT INTO `askdata_tool_param` VALUES (29, 6, 'svcType', '业务维度', 'string', '商机业务维度筛选：\"算网数智\"/\"联网通信\"/\"国际业务\"', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'svcType', NULL, NULL, 99, NULL, 11);
INSERT INTO `askdata_tool_param` VALUES (30, 6, 'bgType', '行业大类', 'string', '商机行业大类：\"2G\"=政府类、\"2B\"=企业类', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'bgType', NULL, NULL, 99, NULL, 12);
INSERT INTO `askdata_tool_param` VALUES (31, 6, 'tradeClusterId', '客户行业ID', 'string', '客户行业ID筛选', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'tradeClusterId', NULL, NULL, 99, NULL, 13);
INSERT INTO `askdata_tool_param` VALUES (32, 6, 'customerManager', '客户经理', 'string', '客户经理姓名模糊搜索', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'customerManager', NULL, NULL, 99, NULL, 14);
INSERT INTO `askdata_tool_param` VALUES (33, 6, 'abnmlOppo', '是否异常商机', 'string', '异常商机筛选：空字符串=全部、\"1\"=异常、\"0\"=正常', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'abnmlOppo', NULL, NULL, 99, NULL, 15);
INSERT INTO `askdata_tool_param` VALUES (34, 6, 'arrairAmountStart', '金额范围下限(万元)', 'integer', '商机预估金额范围下限，单位万元', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountStart', NULL, NULL, 99, NULL, 16);
INSERT INTO `askdata_tool_param` VALUES (35, 6, 'arrairAmountEnd', '金额范围上限(万元)', 'integer', '商机预估金额范围上限，单位万元', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountEnd', NULL, NULL, 99, NULL, 17);
INSERT INTO `askdata_tool_param` VALUES (36, 6, 'ctrtStartMoney', '签约金额下限(万元)', 'integer', '签约合同金额范围下限，单位万元', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ctrtStartMoney', NULL, NULL, 99, NULL, 18);
INSERT INTO `askdata_tool_param` VALUES (37, 6, 'ctrtEndMoney', '签约金额上限(万元)', 'integer', '签约合同金额范围上限，单位万元', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ctrtEndMoney', NULL, NULL, 99, NULL, 19);
INSERT INTO `askdata_tool_param` VALUES (38, 6, 'orderFlag', '排序字段', 'string', '排序字段标识：\"1\"=按预估金额排序', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'orderFlag', NULL, NULL, 99, NULL, 20);
INSERT INTO `askdata_tool_param` VALUES (39, 6, 'orderType', '排序方向', 'string', '排序方向：\"ASC\"=升序、\"DESC\"=降序', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'orderType', NULL, NULL, 99, NULL, 21);
INSERT INTO `askdata_tool_param` VALUES (40, 6, 'oppoTypes', '主业务类型列表', 'array', '商机主业务类型名称列表(多选)，如[\"云计算\",\"大数据\"]', 0, '[]', NULL, NULL, NULL, NULL, NULL, NULL, 'oppoTypes', NULL, NULL, 99, NULL, 22);
INSERT INTO `askdata_tool_param` VALUES (41, 7, 'areaCode', '地市编码', 'string', '地市编码，全省传\"999\"，杭州=\"571\"，宁波=\"574\"等', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'areaCode', NULL, NULL, 1, NULL, 1);
INSERT INTO `askdata_tool_param` VALUES (42, 7, 'startTime', '开始日期', 'string', '开始日期(合同审批日期)，格式YYYYMMDD，如\"20260101\"', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'startTime', NULL, NULL, 2, NULL, 2);
INSERT INTO `askdata_tool_param` VALUES (43, 7, 'endTime', '结束日期', 'string', '结束日期(合同审批日期)，格式YYYYMMDD，如\"20260610\"', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'endTime', NULL, NULL, 3, NULL, 3);
INSERT INTO `askdata_tool_param` VALUES (44, 7, 'pageNum', '页码', 'integer', '分页页码，从1开始', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'pageNum', NULL, NULL, 4, NULL, 4);
INSERT INTO `askdata_tool_param` VALUES (45, 7, 'pageSize', '每页条数', 'integer', '每页返回记录数，建议20', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'pageSize', NULL, NULL, 5, NULL, 5);
INSERT INTO `askdata_tool_param` VALUES (46, 7, 'dataType', '数据类型(固定)', 'string', '固定传\"HT\"表示查询合同数据。系统自动填充。', 0, '\"HT\"', NULL, NULL, NULL, NULL, NULL, NULL, 'dataType', NULL, NULL, 99, NULL, 99);
INSERT INTO `askdata_tool_param` VALUES (47, 7, 'itrstOa', '用户OA(固定)', 'string', '用户OA账号，用于查询关注状态。系统自动填充（需改造），目前固定传\"SYSTEM\"。', 0, '\"SYSTEM\"', NULL, NULL, NULL, NULL, NULL, NULL, 'itrstOa', NULL, NULL, 99, NULL, 99);
INSERT INTO `askdata_tool_param` VALUES (48, 7, 'countyCode', '区县编码', 'string', '区县编码筛选。传空=不筛选', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'countyCode', NULL, NULL, 99, NULL, 10);
INSERT INTO `askdata_tool_param` VALUES (49, 7, 'svcType', '业务类型', 'string', '业务类型筛选：\"数科集成\"/\"联通云\"/\"数据服务\"/\"数智应用\"/\"数据中心\"/\"联网通信\"', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'svcType', NULL, NULL, 99, NULL, 11);
INSERT INTO `askdata_tool_param` VALUES (50, 7, 'tradeClusterId', '客户行业ID', 'string', '客户行业ID筛选', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'tradeClusterId', NULL, NULL, 99, NULL, 12);
INSERT INTO `askdata_tool_param` VALUES (51, 7, 'customerManager', '客户经理', 'string', '客户经理OA或姓名模糊搜索', 0, '\"\"', NULL, NULL, NULL, NULL, NULL, NULL, 'customerManager', NULL, NULL, 99, NULL, 13);
INSERT INTO `askdata_tool_param` VALUES (52, 7, 'arrairAmountStart', '金额范围下限(万元)', 'integer', '合同金额范围下限，单位万元', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountStart', NULL, NULL, 99, NULL, 14);
INSERT INTO `askdata_tool_param` VALUES (53, 7, 'arrairAmountEnd', '金额范围上限(万元)', 'integer', '合同金额范围上限，单位万元', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'arrairAmountEnd', NULL, NULL, 99, NULL, 15);
INSERT INTO `askdata_tool_param` VALUES (54, 7, 'orderFlag', '排序字段', 'string', '排序字段标识：\"1\"=按合同金额排序', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'orderFlag', NULL, NULL, 99, NULL, 16);
INSERT INTO `askdata_tool_param` VALUES (55, 7, 'orderType', '排序方向', 'string', '排序方向：\"ASC\"=升序、\"DESC\"=降序', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'orderType', NULL, NULL, 99, NULL, 17);
INSERT INTO `askdata_tool_param` VALUES (56, 9, 'svcType', '业务维度过滤', 'string', '业务维度过滤：空字符串=全部、\"算网数智\"=算网数智类业务类型、\"联网通信\"=联网通信类、\"国际业务\"=国际业务类', 0, '\"\"', '[\"\", \"算网数智\", \"联网通信\", \"国际业务\"]', NULL, NULL, NULL, NULL, NULL, 'svcType', NULL, NULL, 99, NULL, 1);

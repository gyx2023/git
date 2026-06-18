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
    `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` int(11) DEFAULT '1' COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `tool_name` (`tool_name`),
    KEY `idx_tool_name` (`tool_name`),
    KEY `idx_enabled` (`is_enabled`),
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
    CONSTRAINT `askdata_tool_param_OBFK_1778056248782330` FOREIGN KEY (`tool_id`) REFERENCES `askdata_tool_config`(`id`) ON UPDATE RESTRICT ON DELETE CASCADE ,
    UNIQUE KEY `uk_tool_param` (`tool_id`, `param_name`),
    KEY `idx_tool_id` (`tool_id`)
) DEFAULT CHARSET = utf8mb4  COMMENT = '工具参数定义表';

-- 工具响应字段映射表
CREATE TABLE `askdata_tool_response_field` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tool_id` bigint(20) NOT NULL COMMENT '关联工具ID',
    `field_name` varchar(100) NOT NULL COMMENT 'API返回的原始字段名（如 AREA_NAME）',
    `display_name` varchar(200) DEFAULT NULL COMMENT '前端显示的中文名（如\"地市名称\"）',
    `data_type` varchar(20) DEFAULT 'string' COMMENT '数据类型: string/number/currency/percentage/date',
    `format_rule` varchar(100) DEFAULT NULL COMMENT '格式化规则（如 #,##0.00）',
    `sort_order` int(11) DEFAULT '0' COMMENT '排序号（决定列顺序）',
    `is_visible` tinyint(1) DEFAULT '1' COMMENT '是否默认显示',
    `group_name` varchar(100) DEFAULT NULL COMMENT '分组名',
    `description` text DEFAULT NULL COMMENT '字段描述',
    PRIMARY KEY (`id`),
    CONSTRAINT `askdata_tool_response_field_OBFK_1781182325299513` FOREIGN KEY (`tool_id`) REFERENCES `askdata_tool_config`(`id`) ON UPDATE RESTRICT ON DELETE CASCADE ,
    UNIQUE KEY `uk_tool_resp_field` (`tool_id`, `field_name`),
    KEY `idx_resp_tool_id` (`tool_id`)
) DEFAULT CHARSET = utf8mb4 COMMENT = '工具响应字段映射表';

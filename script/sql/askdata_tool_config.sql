-- =============================================
-- 智能问数工具配置表
-- =============================================

-- 1. 工具配置主表
CREATE TABLE IF NOT EXISTS askdata_tool_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tool_name VARCHAR(100) NOT NULL UNIQUE COMMENT '工具唯一标识（对应HiAgent工具名）',
    tool_display_name VARCHAR(200) COMMENT '工具显示名称',
    description TEXT COMMENT '工具描述（用于AI理解）',
    
    -- 接口信息
    api_url VARCHAR(500) NOT NULL COMMENT '原接口地址',
    http_method VARCHAR(10) DEFAULT 'POST' COMMENT 'HTTP方法',
    content_type VARCHAR(50) DEFAULT 'application/json' COMMENT '请求类型',
    
    -- 权限与安全
    required_role VARCHAR(50) COMMENT '所需角色',
    dept_filter BOOLEAN DEFAULT FALSE COMMENT '是否需要部门过滤',
    is_sensitive BOOLEAN DEFAULT FALSE COMMENT '是否敏感工具',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    
    -- 响应配置
    response_data_path VARCHAR(200) DEFAULT '$.data' COMMENT '数据提取路径(JSONPath)',
    response_chart_type VARCHAR(50) DEFAULT 'table' COMMENT '默认图表类型',
    response_total_path VARCHAR(200) COMMENT '总数提取路径',
    
    -- 性能配置
    timeout_seconds INT DEFAULT 5 COMMENT '超时时间(秒)',
    cache_ttl_seconds INT DEFAULT 0 COMMENT '缓存时长(秒)，0表示不缓存',
    
    -- 元数据
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    
    INDEX idx_tool_name (tool_name),
    INDEX idx_enabled (is_enabled),
    INDEX idx_sensitive (is_sensitive)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能问数工具配置表';

-- 2. 工具参数定义表
CREATE TABLE IF NOT EXISTS askdata_tool_param (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tool_id BIGINT NOT NULL COMMENT '关联工具ID',
    
    -- 参数基本信息
    param_name VARCHAR(100) NOT NULL COMMENT '参数名（AI提取时用）',
    param_display_name VARCHAR(200) COMMENT '参数显示名',
    param_type VARCHAR(20) NOT NULL COMMENT '参数类型: string/integer/boolean/array/object',
    description TEXT COMMENT '参数描述（帮助AI理解）',
    
    -- 校验规则
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    default_value TEXT COMMENT '默认值（JSON格式）',
    enum_values TEXT COMMENT '枚举值（JSON数组）',
    min_value DECIMAL(20,4) COMMENT '最小值（数值类型）',
    max_value DECIMAL(20,4) COMMENT '最大值（数值类型）',
    min_length INT COMMENT '最小长度（字符串类型）',
    max_length INT COMMENT '最大长度（字符串类型）',
    pattern VARCHAR(500) COMMENT '正则表达式',
    
    -- 映射与转换
    target_param_name VARCHAR(100) COMMENT '映射到原接口的参数名',
    transform_rule VARCHAR(100) COMMENT '转换规则标识',
    transform_config TEXT COMMENT '转换配置（JSON格式）',
    
    -- AI 澄清配置
    clarify_priority INT DEFAULT 99 COMMENT '澄清优先级（数字越小越优先追问）',
    clarify_question_template VARCHAR(500) COMMENT '澄清问题模板',
    
    -- 排序与显示
    sort_order INT DEFAULT 0 COMMENT '排序号',
    
    FOREIGN KEY (tool_id) REFERENCES askdata_tool_config(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tool_param (tool_id, param_name),
    INDEX idx_tool_id (tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具参数定义表';

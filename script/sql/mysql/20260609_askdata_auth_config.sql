-- =============================================
-- 智能问数系统认证配置表
-- 创建日期: 2026-06-09
-- 说明: 用于外部系统认证配置管理
-- =============================================

-- 1. 认证配置表
CREATE TABLE IF NOT EXISTS askdata_auth_config (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    -- 系统来源标识
    system_name     VARCHAR(100) NOT NULL COMMENT '外部系统名称（如：结算系统、CRM系统）',
    system_desc     VARCHAR(500) COMMENT '系统描述',
    caller_id       VARCHAR(100) COMMENT '调用方标识（服务名/应用ID，被调用方可用来做来源校验）',

    -- 认证方式
    auth_type       VARCHAR(20) NOT NULL DEFAULT 'NONE'
                    COMMENT '认证方式: NONE/SA_TOKEN/API_KEY/BASIC/OAUTH2_CLIENT',

    -- 认证配置（加密存储）
    auth_config_json TEXT COMMENT '认证配置JSON（加密存储，不同auth_type结构不同）',

    -- 状态
    status          CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',

    -- 元数据
    create_by       VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag        CHAR(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',

    UNIQUE KEY uk_system_name (system_name),
    INDEX idx_auth_type (auth_type),
    INDEX idx_status (status),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能问数-外部系统认证配置表';

-- 2. 工具配置表新增关联认证配置ID
ALTER TABLE askdata_tool_config ADD COLUMN
    auth_config_id BIGINT DEFAULT NULL COMMENT '关联认证配置ID（askdata_auth_config.id，NULL=无需认证）'
    AFTER is_enabled;

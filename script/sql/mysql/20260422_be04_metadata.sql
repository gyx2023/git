-- ============================================================
-- BE-04 语义层元数据管理 建表脚本
-- 创建时间: 2026-04-22
-- ============================================================

-- 指标定义表
CREATE TABLE IF NOT EXISTS `meta_metric` (
  `id`             bigint(20)   NOT NULL AUTO_INCREMENT                   COMMENT '主键',
  `metric_code`    varchar(50)  NOT NULL                                  COMMENT '指标代码 (唯一，小写字母+数字+下划线)',
  `metric_name`    varchar(100) NOT NULL                                  COMMENT '指标名称',
  `expression`     varchar(500) NOT NULL                                  COMMENT '计算表达式',
  `category`       varchar(50)  DEFAULT NULL                              COMMENT '业务分类：sales/contract/customer/finance',
  `data_type`      varchar(20)  NOT NULL                                  COMMENT '数据类型：money/percent/count/ratio/date',
  `security_level` int(11)      NOT NULL DEFAULT '1'                      COMMENT '安全等级：1-公开 2-内部 3-保密',
  `unit`           varchar(20)  DEFAULT NULL                              COMMENT '单位',
  `allowed_values` json         DEFAULT NULL                              COMMENT '枚举值列表 (防止 AI 参数幻觉)',
  `version`        int(11)      NOT NULL DEFAULT '1'                      COMMENT '版本号 (乐观锁)',
  `status`         tinyint(4)   NOT NULL DEFAULT '1'                      COMMENT '状态：1-启用 0-禁用',
  `create_by`      varchar(64)  DEFAULT ''                                COMMENT '创建者',
  `create_time`    datetime     DEFAULT NULL                              COMMENT '创建时间',
  `update_by`      varchar(64)  DEFAULT ''                                COMMENT '更新者',
  `update_time`    datetime     DEFAULT NULL                              COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_code` (`metric_code`),
  KEY `idx_status` (`status`),
  KEY `idx_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语义层指标定义表';

-- 元数据变更事件表 (审计日志)
CREATE TABLE IF NOT EXISTS `meta_change_event` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT                      COMMENT '主键',
  `event_type`  varchar(50)  NOT NULL                                     COMMENT '事件类型：CREATE/UPDATE/DELETE',
  `entity_type` varchar(50)  NOT NULL                                     COMMENT '实体类型：METRIC',
  `entity_id`   bigint(20)   NOT NULL                                     COMMENT '实体 ID',
  `entity_code` varchar(50)  NOT NULL                                     COMMENT '实体代码',
  `old_version` int(11)      DEFAULT NULL                                 COMMENT '旧版本',
  `new_version` int(11)      NOT NULL                                     COMMENT '新版本',
  `changed_by`  varchar(64)  DEFAULT NULL                                 COMMENT '变更人',
  `changed_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_entity` (`entity_type`, `entity_id`),
  KEY `idx_changed_at` (`changed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='元数据变更事件表 (审计追溯)';

-- ============================================================
-- 初始化示例指标数据
-- ============================================================
INSERT IGNORE INTO `meta_metric` (`metric_code`, `metric_name`, `expression`, `data_type`, `security_level`, `unit`, `allowed_values`, `version`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
('gmv',           '成交总额',   'SUM(order_amount)',                   'money',   1, '元',  NULL,                                1, 1, 'admin', NOW(), 'admin', NOW()),
('order_count',   '订单数量',   'COUNT(order_id)',                     'count',   1, '笔',  NULL,                                1, 1, 'admin', NOW(), 'admin', NOW()),
('avg_order_amt', '客单价',     'SUM(order_amount)/COUNT(order_id)',   'money',   1, '元',  NULL,                                1, 1, 'admin', NOW(), 'admin', NOW()),
('conversion_rate','转化率',    'COUNT(order_id)/COUNT(visit_id)',     'percent', 1, '%',   NULL,                                1, 1, 'admin', NOW(), 'admin', NOW()),
('customer_count','客户数量',   'COUNT(DISTINCT customer_id)',         'count',   1, '个',  NULL,                                1, 1, 'admin', NOW(), 'admin', NOW());

-- ============================================================
-- SEMANTIC_QUERY 可执行指标改造
-- meta_metric 表增加 source_table / source_datasource / condition_mapping
-- 使语义查询能从描述型升级为真正的数据查询
-- ============================================================

-- 1. meta_metric 表新增字段
ALTER TABLE `meta_metric`
  ADD COLUMN `source_table`      VARCHAR(200) DEFAULT NULL COMMENT '数据来源表名',
  ADD COLUMN `source_datasource` VARCHAR(50)  DEFAULT 'master' COMMENT '数据源名称（对应 spring.datasource.dynamic.datasource key）',
  ADD COLUMN `condition_mapping` JSON         DEFAULT NULL COMMENT '实体字段→物理字段映射，如 {"time_range":"order_month","region":"region_code"}',
  ADD COLUMN `category`          VARCHAR(50)  DEFAULT NULL COMMENT '业务分类：sales/contract/customer/finance';

-- ============================================================
-- 2. 补充真实业务指标初始化数据
-- source_table 配置后，SEMANTIC_QUERY 可实际执行查询
-- condition_mapping 定义了 entities 字段名到表中实际列名的映射
-- ============================================================
INSERT IGNORE INTO `meta_metric` (`metric_code`, `metric_name`, `expression`, `data_type`, `unit`, `category`, `source_table`, `source_datasource`, `condition_mapping`, `version`, `status`)
VALUES
('gmv',             '成交总额', 'SUM(order_amount)',           'money',   '元', 'sales',    'dws_sales_daily',   'master',
 '{"time_range":"order_month","region":"region_code","dept":"dept_id"}', 1, 1),
('order_count',     '订单数量', 'COUNT(order_id)',             'count',   '笔', 'sales',    'dws_sales_daily',   'master',
 '{"time_range":"order_month","region":"region_code","dept":"dept_id"}', 1, 1),
('avg_order_amt',   '客单价',   'SUM(order_amount)/COUNT(DISTINCT order_id)', 'money', '元', 'sales', 'dws_sales_daily', 'master',
 '{"time_range":"order_month","region":"region_code","dept":"dept_id"}', 1, 1),
('contract_count',  '合同数',   'COUNT(contract_id)',          'count',   '个', 'contract', 'dws_contract_daily', 'master',
 '{"time_range":"sign_month","region":"region_code","dept":"dept_id"}', 1, 1),
('contract_amount', '合同额',   'SUM(contract_amount)',         'money',   '元', 'contract', 'dws_contract_daily', 'master',
 '{"time_range":"sign_month","region":"region_code","dept":"dept_id"}', 1, 1),
('new_customer',    '新增客户', 'COUNT(DISTINCT customer_id)',  'count',   '个', 'customer', 'dws_customer_daily', 'master',
 '{"time_range":"create_month","region":"region_code","dept":"dept_id"}', 1, 1)
ON DUPLICATE KEY UPDATE
    source_datasource = VALUES(source_datasource),
    condition_mapping = VALUES(condition_mapping);

-- ============================================================
-- 3. 使用说明
-- 管理员需要按实际业务表结构调整：
--   source_table：对应实际存在的数据库表名
--   source_datasource：多数据源配置中的 key（master / slave_1）
--   condition_mapping 的 value：对应表中实际列名
--   expression：合法的 SQL 聚合表达式
-- ============================================================


CREATE TABLE IF NOT EXISTS `dws_contract_daily` (
`stat_date`     date         NOT NULL COMMENT '统计日期',
`sign_month`    varchar(7)   NOT NULL COMMENT '签约月份（yyyy-MM）',
`region_code`   varchar(20)  DEFAULT NULL COMMENT '区域编码',
`dept_id`       bigint(20)   DEFAULT NULL COMMENT '部门ID',
`contract_count`     int(11)    DEFAULT 0 COMMENT '合同数量',-- 度量指标
`contract_amount`    decimal(20,2) DEFAULT 0 COMMENT '合同金额',
`avg_contract_amt`   decimal(20,2) DEFAULT 0 COMMENT '平均合同金额',
KEY `idx_sign_month` (`sign_month`),
KEY `idx_region` (`region_code`),
KEY `idx_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同每日聚合宽表';

INSERT INTO `dws_contract_daily` (`stat_date`, `sign_month`, `region_code`, `dept_id`, `contract_count`,
                                  `contract_amount`, `avg_contract_amt`) VALUES
('2025-01-15', '2025-01', 'huadong', 101, 120, 3600000.00, 30000.00),
('2025-01-15', '2025-01', 'huabei',  102,  95, 2850000.00, 30000.00),
('2025-01-31', '2025-01', 'huadong', 101,  45, 1350000.00, 30000.00),
('2025-01-31', '2025-01', 'huabei',  102,  30,  900000.00, 30000.00),
('2025-02-15', '2025-02', 'huadong', 101, 110, 3300000.00, 30000.00),
('2025-02-15', '2025-02', 'huabei',  102,  88, 2640000.00, 30000.00),
('2025-02-28', '2025-02', 'huadong', 101,  40, 1200000.00, 30000.00),
('2025-02-28', '2025-02', 'huabei',  102,  25,  750000.00, 30000.00),
('2025-03-15', '2025-03', 'huadong', 101, 135, 4050000.00, 30000.00),
('2025-03-15', '2025-03', 'huabei',  102, 102, 3060000.00, 30000.00),
('2025-03-31', '2025-03', 'huadong', 101,  50, 1500000.00, 30000.00),
('2025-03-31', '2025-03', 'huabei',  102,  35, 1050000.00, 30000.00),
('2025-04-15', '2025-04', 'huadong', 101, 128, 3840000.00, 30000.00),
('2025-04-15', '2025-04', 'huabei',  102,  98, 2940000.00, 30000.00),
('2025-04-30', '2025-04', 'huadong', 101,  42, 1260000.00, 30000.00),
('2025-04-30', '2025-04', 'huabei',  102,  28,  840000.00, 30000.00),
('2025-05-15', '2025-05', 'huadong', 101, 142, 4260000.00, 30000.00),
('2025-05-15', '2025-05', 'huabei',  102, 108, 3240000.00, 30000.00),
('2025-05-31', '2025-05', 'huadong', 101,  48, 1440000.00, 30000.00),
('2025-05-31', '2025-05', 'huabei',  102,  32,  960000.00, 30000.00),
('2025-06-15', '2025-06', 'huadong', 101, 138, 4140000.00, 30000.00),
('2025-06-15', '2025-06', 'huabei',  102, 105, 3150000.00, 30000.00);

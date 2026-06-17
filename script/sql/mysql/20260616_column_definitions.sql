-- meta_metric 表结构调整
-- 1. 删除 condition_mapping（由 column_definitions 替代，agent 不再需要硬编码映射）
-- 2. 增加 column_definitions（目标表字段定义，供 semantic_sql_agent 生成 SQL 时参考）

ALTER TABLE `meta_metric`
  DROP COLUMN `condition_mapping`,
  ADD COLUMN `column_definitions` JSON DEFAULT NULL
  COMMENT '目标表字段定义，如[{"name":"sign_month","type":"string","description":"签约月份，格式yyyy-MM"}]'
  AFTER `source_datasource`;

-- 已有指标补充 column_definitions
UPDATE `meta_metric` SET `column_definitions` = '[{"name":"sign_month","type":"string","description":"签约月份，格式yyyy-MM"},{"name":"region_code","type":"string","description":"区域编码"},{"name":"dept_id","type":"bigint","description":"部门ID"},{"name":"contract_count","type":"int","description":"合同数量"},{"name":"contract_amount","type":"decimal","description":"合同金额"},{"name":"avg_contract_amt","type":"decimal","description":"平均合同金额"}]'
WHERE `metric_code` IN ('contract_count', 'contract_amount', 'avg_order_amt');

UPDATE `meta_metric` SET `column_definitions` = '[{"name":"order_month","type":"string","description":"订单月份，格式yyyy-MM"},{"name":"region_code","type":"string","description":"区域编码"},{"name":"dept_id","type":"bigint","description":"部门ID"},{"name":"order_amount","type":"decimal","description":"订单金额"},{"name":"order_id","type":"bigint","description":"订单ID"}]'
WHERE `metric_code` IN ('gmv', 'order_count');

UPDATE `meta_metric` SET `column_definitions` = '[{"name":"create_month","type":"string","description":"创建月份，格式yyyy-MM"},{"name":"region_code","type":"string","description":"区域编码"},{"name":"dept_id","type":"bigint","description":"部门ID"},{"name":"customer_id","type":"bigint","description":"客户ID"}]'
WHERE `metric_code` = 'new_customer';

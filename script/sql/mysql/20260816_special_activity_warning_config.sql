-- ============================================================================
-- 特殊活动到期预警 sys_code / sys_param 配置
-- 对应需求：特殊活动到期预警迁移至新框架（按用户客户级派单，处理页沿用沃TV预警同一套）
-- 表结构：type_=分类, code_=值, name_=名称, setting_str3=订单类型标识
-- ============================================================================

-- 1. 流程定义 key（走通用 single_approval）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('process_key', 'single_approval', '特殊活动到期预警', 1, '特殊活动到期预警', '1', 'special_activity_warn');

-- 2. 客户经理处理节点 task_def_key
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('task_def_key', 'special_activity_warn_manager_deal', '客户经理处理', 1, '特殊活动到期预警', '1', 'special_activity_warn');


-- 4. 维系预警报表下拉框（type_=maintenance_order），用于报表和未分配清单按订单类型筛选
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('maintenance_order', 'special_activity_warn', '特殊活动到期预警', 1, '特殊活动到期预警', '1', 'maintenance_order');

-- 5. 通用预警任务开关（WarningJob 读取 type_=warn_type, code_=warning_job 逗号分隔的预警类型列表）
--    将 special_activity_warn 追加到该配置。若该配置行尚不存在则先建一行（值为空，仅启用特殊活动），
--    实现幂等：多次执行不会重复追加。
INSERT INTO sys_param (code_, name_, value_, remark_, type_)
VALUES ('warning_job', '通用预警类型开关', '', '通用预警任务：启用特殊活动到期预警', 'warn_type')
ON DUPLICATE KEY UPDATE code_ = VALUES(code_);

INSERT INTO sys_param (code_, name_, value_, remark_, type_)
SELECT 'warning_job', '通用预警类型开关',
       IF(LOCATE(',special_activity_warn', CONCAT(',', value_)) = 0, CONCAT(value_, ',special_activity_warn'), value_),
       '通用预警任务：启用特殊活动到期预警', 'warn_type'
FROM sys_param
WHERE type_ = 'warn_type' AND code_ = 'warning_job'
ON DUPLICATE KEY UPDATE value_ = VALUES(value_);

-- 说明：老框架侧（YYPT_WEB）对 000032 的建单排除(common_warning_build_exclude)与闭环排除(web_cycle_close)配置，
-- 见资源目录脚本：yypt_web/resources/special_activity_warning_config.sql（不属于本目录的 CI 范围）。

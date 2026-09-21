-- ============================================================================
-- 终端合约到期预警（terminal_contract_warn / warning_type=000033）
-- 新框架 sys_code / sys_param 配置
--
-- 对应需求：终端合约到期预警迁移至新框架（按用户客户级派单，处理页沿用沃TV预警同一套）
-- 表结构：type_=分类, code_=值, name_=名称, order_=排序, setting_str3=分组标识
-- 参照：20260920_group_buy_warning_config.sql（团购折扣，同族）
-- ============================================================================

-- 1. 流程定义 key（走通用 single_approval）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('process_key', 'single_approval', '终端合约到期预警', 1, '终端合约到期预警', '1', 'terminal_contract_warn');

-- 2. 客户经理处理节点 task_def_key
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('task_def_key', 'terminal_contract_warn_manager_deal', '客户经理处理', 1, '终端合约到期预警', '1', 'terminal_contract_warn');

-- 3. 维系情况选项
--    读取链路：TerminalContractWarningBusinessContextProvider → SysCodeServiceImpl.getCodeMapGroupBySettingStr3("terminal_contract_warn")
--    即 type_='terminal_contract_warn'，按 setting_str3 分组（1=完成 2=跟踪中 3=转派）。
--    前端提交的是 name_，故 name_ 文案即业务落库值。
--
--    ⚠ 以下选项文案直接沿用团购折扣到期预警（同族业务，旧 dealByTC.jsp 与 dealByGBW.jsp
--      用的是同一套 transList 机制，JSP 里没有硬编码选项）。上线前请与业务确认是否需要差异。
--
--    选项 → 联动字段（前端 views/workflow/terminalContractWarning/deal/index.vue）：
--      完成-2  已通知客户，客户同意续约 → 日期选择器「计划续约日期」
--      完成-3  已通知客户，客户不再续约 → 文本框「具体原因」(≥10字)
--      完成-4  系统自动续约             → 弹出互斥产品/停机风险提示
--      完成-10 / 跟踪中-3 / 转派-2 其他 → 文本框「请填写具体情况」(≥10字)
--      跟踪中-2 客户要求后续再联系      → 日期选择器「下次联系时间」
-- (1) 选"完成"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('terminal_contract_warn', '1',  '已通知客户，客户已续约',   1, '终端合约到期预警-完成', '1', '1'),
('terminal_contract_warn', '2',  '已通知客户，客户同意续约', 2, '终端合约到期预警-完成', '1', '1'),
('terminal_contract_warn', '3',  '已通知客户，客户不再续约', 3, '终端合约到期预警-完成', '1', '1'),
('terminal_contract_warn', '4',  '系统自动续约',             4, '终端合约到期预警-完成', '1', '1'),
('terminal_contract_warn', '6',  '多次尝试，无法联系客户',   5, '终端合约到期预警-完成', '1', '1'),
('terminal_contract_warn', '7',  '散户用户，无法联系',       6, '终端合约到期预警-完成', '1', '1'),
('terminal_contract_warn', '8',  '非本人客户',               7, '终端合约到期预警-完成', '1', '1'),
('terminal_contract_warn', '10', '其他',                     8, '终端合约到期预警-完成', '1', '1');

-- (2) 选"跟踪中"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('terminal_contract_warn', '1', '无法联系客户，继续跟进', 1, '终端合约到期预警-跟踪中', '1', '2'),
('terminal_contract_warn', '2', '客户要求后续再联系',     2, '终端合约到期预警-跟踪中', '1', '2'),
('terminal_contract_warn', '3', '其他',                   3, '终端合约到期预警-跟踪中', '1', '2');

-- (3) 选"转派"（另需选择「下一步处理人」）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('terminal_contract_warn', '1', '非本人客户', 1, '终端合约到期预警-转派', '1', '3'),
('terminal_contract_warn', '2', '其他',       2, '终端合约到期预警-转派', '1', '3');

-- 4. 维系预警报表下拉框（type_=maintenance_order），用于报表和未分配清单按订单类型筛选
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('maintenance_order', 'terminal_contract_warn', '终端合约到期预警', 1, '终端合约到期预警', '1', 'maintenance_order');

-- 5. 通用预警任务开关（WarningJob 读取 type_='warn_type', code_='warning_job' 逗号分隔的预警类型列表）
--    追加 terminal_contract_warn，WarningJob 即会调用 TerminalContractWarningStrategy 建单派发。
--    ⚠ 必须在「老框架侧排除配置」生效之后再打开，否则双跑重复建单。
INSERT INTO sys_param (code_, name_, value_, remark_, type_)
VALUES ('warning_job', '通用预警类型开关', '', '通用预警任务：启用终端合约到期预警', 'warn_type')
ON DUPLICATE KEY UPDATE code_ = VALUES(code_);

INSERT INTO sys_param (code_, name_, value_, remark_, type_)
SELECT 'warning_job', '通用预警类型开关',
       IF(LOCATE(',terminal_contract_warn', CONCAT(',', value_)) = 0, CONCAT(value_, ',terminal_contract_warn'), value_),
       '通用预警任务：启用终端合约到期预警', 'warn_type'
FROM sys_param
WHERE type_ = 'warn_type' AND code_ = 'warning_job'
ON DUPLICATE KEY UPDATE value_ = VALUES(value_);

-- 6. 闭环自动完结（WarningCycleCloseJob 读取 type_='cycle_close', code_='cycle_close' 的
--    warning_type=order_type 映射列表）。000033 闭环数据到达后由新框架立即完结 Flowable 流程。
INSERT INTO sys_param (code_, name_, value_, remark_, type_, is_system)
VALUES ('cycle_close', '用户级闭环流程自动完结配置', '', '', 'cycle_close', '0')
ON DUPLICATE KEY UPDATE code_ = VALUES(code_);

UPDATE sys_param
SET value_ = CONCAT(value_, ',000033=terminal_contract_warn')
WHERE type_ = 'cycle_close' AND code_ = 'cycle_close'
  AND LOCATE('000033=', value_) = 0;

-- 说明：
-- 1) 闭环标识 / 预警等级字典（sys_code 的 cycle_flag / warning_level）与团购折扣共用，
--    已由 20260920_group_buy_warning_config.sql 第 7 节补齐，此处不重复。
-- 2) 老框架侧（YYPT_WEB）必须同批次停派单/停闭环，否则双跑期会产生重复工单。
--    见：yypt_web/resources/20260921_terminal_contract_warning_config.sql
-- 3) 000033 与 000031 一样，在 TcCustCycleWarningJob.getOrder 中有真实关单分支，
--    老框架侧的排除务必确认生效。

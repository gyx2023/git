-- ============================================================================
-- 双线到期预警（C00007）迁移新框架配置
-- 对应需求：双线到期预警迁移到新框架（orderType=double_wide）
-- 参照：20260916_cloud_expire_warning_config.sql
-- 表结构：type_=分类, code_=值, name_=名称, order_=排序, setting_str3=订单类型/分组标识
--
-- 注意：C00008（双线预警-IP网络）经确认不再产生数据，无需迁移，本脚本只处理 C00007。
-- ============================================================================

-- 1. 流程定义 key（走通用 single_approval）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('process_key', 'single_approval', '双线到期预警', 1, '双线到期预警', '1', 'double_wide');

-- 2. 客户经理处理节点 task_def_key（对齐旧系统 ACT_PHASE_DOUBLE_WIDE_MANAGER）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('task_def_key', 'double_wide_manager_deal', '客户经理处理', 1, '双线到期预警', '1', 'double_wide');

-- 3. 维系情况配置（type_='double_wide'，setting_str3 作处理结果分组：1=完成 2=跟踪中 3=转派）
--    选中后值存入 topp_order.wide_result，前端按 name_ 匹配联动（计划续约日期 / 下次联系时间 / 其他文本框）
-- (1) 选"完成"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('double_wide', '1',  '已通知客户，客户已续约',       1, '双线到期预警-完成', '1', '1'),
('double_wide', '2',  '已通知客户，客户同意续约',     2, '双线到期预警-完成', '1', '1'),
('double_wide', '3',  '已通知客户，客户不再续约',     3, '双线到期预警-完成', '1', '1'),
('double_wide', '5',  '多次尝试，无法联系客户',       4, '双线到期预警-完成', '1', '1'),
('double_wide', '6',  '非本人客户',                   5, '双线到期预警-完成', '1', '1'),
('double_wide', '8',  '其他',                         6, '双线到期预警-完成', '1', '1');

-- (2) 选"跟踪中"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('double_wide', '20', '无法联系客户，继续跟进',       1, '双线到期预警-跟踪中', '1', '2'),
('double_wide', '21', '客户要求后续再联系',           2, '双线到期预警-跟踪中', '1', '2'),
('double_wide', '22', '其他',                         3, '双线到期预警-跟踪中', '1', '2');

-- (3) 选"转派"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('double_wide', '30', '非本人客户',                   1, '双线到期预警-转派', '1', '3'),
('double_wide', '31', '其他',                         2, '双线到期预警-转派', '1', '3');

-- 4. 维系预警报表下拉框（type_=maintenance_order），用于报表和未分配清单按订单类型筛选
--    注：double_wide 类型此前已在报表中存在（老框架沿用），此处为幂等补齐，若已存在可忽略
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3)
SELECT 'maintenance_order', 'double_wide', '双线到期预警', 1, '双线到期预警', '1', 'maintenance_order'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_code WHERE type_ = 'maintenance_order' AND code_ = 'double_wide'
);

-- ============================================================================
-- 5. 通用预警任务开关（WarningJob 读取 sys_param: type_=warn_type, code_=warning_job）
--    幂等追加 double_wide
-- ============================================================================
INSERT INTO sys_param (code_, name_, value_, remark_, type_)
VALUES ('warning_job', '通用预警类型开关', '', '通用预警任务：启用双线到期预警', 'warn_type')
ON DUPLICATE KEY UPDATE code_ = VALUES(code_);

INSERT INTO sys_param (code_, name_, value_, remark_, type_)
SELECT 'warning_job', '通用预警类型开关',
       IF(LOCATE(',double_wide', CONCAT(',', value_)) = 0, CONCAT(value_, ',double_wide'), value_),
       '通用预警任务：启用双线到期预警', 'warn_type'
FROM sys_param
WHERE type_ = 'warn_type' AND code_ = 'warning_job'
ON DUPLICATE KEY UPDATE value_ = VALUES(value_);

-- ============================================================================
-- 6. 闭环自动完结配置（WarningCycleCloseJob 读取 sys_param: type_=cycle_close, code_=cycle_close）
--    幂等追加 C00007=double_wide
-- ============================================================================
INSERT INTO sys_param (code_, name_, value_, remark_, type_, is_system)
VALUES ('cycle_close', '用户级闭环流程自动完结配置', '', 'warning_type=order_type 对，逗号分隔', 'cycle_close', '0')
ON DUPLICATE KEY UPDATE code_ = VALUES(code_);

INSERT INTO sys_param (code_, name_, value_, remark_, type_, is_system)
SELECT 'cycle_close', '用户级闭环流程自动完结配置',
       IF(LOCATE('C00007=', value_) = 0, CONCAT(value_, ',C00007=double_wide'), value_),
       'warning_type=order_type 对，逗号分隔', 'cycle_close', '0'
FROM sys_param
WHERE type_ = 'cycle_close' AND code_ = 'cycle_close'
ON DUPLICATE KEY UPDATE value_ = VALUES(value_);

-- ============================================================================
-- 验证 SQL
-- ============================================================================
-- SELECT * FROM sys_code WHERE type_ = 'double_wide' ORDER BY setting_str3, order_;
-- SELECT * FROM sys_param WHERE type_ = 'warn_type' AND code_ = 'warning_job';
-- SELECT * FROM sys_param WHERE type_ = 'cycle_close';

-- ============================================================================
-- 团购折扣到期预警（group_buy_warn / warning_type=000031）sys_code / sys_param 配置
-- 对应需求：团购折扣到期预警详情迁移至新框架（按用户客户级派单，处理页沿用沃TV预警同一套）
-- 表结构：type_=分类, code_=值, name_=名称, order_=排序, setting_str3=分组标识
-- 参照：20260816_special_activity_warning_config.sql
-- ============================================================================

-- 1. 流程定义 key（走通用 single_approval）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('process_key', 'single_approval', '团购折扣到期预警', 1, '团购折扣到期预警', '1', 'group_buy_warn');

-- 2. 客户经理处理节点 task_def_key
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('task_def_key', 'group_buy_warn_manager_deal', '客户经理处理', 1, '团购折扣到期预警', '1', 'group_buy_warn');

-- 3. 维系情况选项
--    读取链路：GroupBuyWarningBusinessContextProvider → SysCodeServiceImpl.getCodeMapGroupBySettingStr3("group_buy_warn")
--    即 type_='group_buy_warn'，按 setting_str3 分组（1=完成 2=跟踪中 3=转派）。
--    code_ 沿用旧流程迁移号（注意跳号：完成组无 5、9），仅作标识；
--    前端提交的是 name_（见 Vue 的 :value="name"），故 name_ 文案即业务落库值。
--
--    选项 → 联动字段（前端 views/workflow/groupBuyWarning/deal/index.vue）：
--      完成-2  已通知客户，客户同意续约 → 日期选择器「计划续约日期」
--      完成-3  已通知客户，客户不再续约 → 文本框「具体原因」(≥10字)
--      完成-4  系统自动续约             → 弹出互斥产品/停机风险提示
--      完成-10 / 跟踪中-3 / 转派-2 其他 → 文本框「请填写具体情况」(≥10字)
--      跟踪中-2 客户要求后续再联系      → 日期选择器「下次联系时间」
-- (1) 选"完成"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('group_buy_warn', '1',  '已通知客户，客户已续约',   1, '团购折扣到期预警-完成', '1', '1'),
('group_buy_warn', '2',  '已通知客户，客户同意续约', 2, '团购折扣到期预警-完成', '1', '1'),
('group_buy_warn', '3',  '已通知客户，客户不再续约', 3, '团购折扣到期预警-完成', '1', '1'),
('group_buy_warn', '4',  '系统自动续约',             4, '团购折扣到期预警-完成', '1', '1'),
('group_buy_warn', '6',  '多次尝试，无法联系客户',   5, '团购折扣到期预警-完成', '1', '1'),
('group_buy_warn', '7',  '散户用户，无法联系',       6, '团购折扣到期预警-完成', '1', '1'),
('group_buy_warn', '8',  '非本人客户',               7, '团购折扣到期预警-完成', '1', '1'),
('group_buy_warn', '10', '其他',                     8, '团购折扣到期预警-完成', '1', '1');

-- (2) 选"跟踪中"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('group_buy_warn', '1', '无法联系客户，继续跟进', 1, '团购折扣到期预警-跟踪中', '1', '2'),
('group_buy_warn', '2', '客户要求后续再联系',     2, '团购折扣到期预警-跟踪中', '1', '2'),
('group_buy_warn', '3', '其他',                   3, '团购折扣到期预警-跟踪中', '1', '2');

-- (3) 选"转派"（另需选择「下一步处理人」）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('group_buy_warn', '1', '非本人客户', 1, '团购折扣到期预警-转派', '1', '3'),
('group_buy_warn', '2', '其他',       2, '团购折扣到期预警-转派', '1', '3');

-- 4. 维系预警报表下拉框（type_=maintenance_order），用于报表和未分配清单按订单类型筛选
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('maintenance_order', 'group_buy_warn', '团购折扣到期预警', 1, '团购折扣到期预警', '1', 'maintenance_order');

-- 5. 通用预警任务开关（WarningJob 读取 type_='warn_type', code_='warning_job' 逗号分隔的预警类型列表）
--    将 group_buy_warn 追加到该配置，WarningJob 即会调用 GroupBuyWarningStrategy 建单派发。
--    若该配置行尚不存在则先建一行（值为空，仅启用团购折扣），
--    实现幂等：多次执行不会重复追加。
--    ⚠ 必须在「老框架侧排除配置」生效之后再打开本开关，否则双跑重复建单。
INSERT INTO sys_param (code_, name_, value_, remark_, type_)
SELECT 'warning_job', '通用预警类型开关',
       IF(LOCATE(',group_buy_warn', CONCAT(',', value_)) = 0, CONCAT(value_, ',group_buy_warn'), value_),
       '通用预警任务：启用团购折扣到期预警', 'warn_type'
FROM sys_param
WHERE type_ = 'warn_type' AND code_ = 'warning_job'
ON DUPLICATE KEY UPDATE value_ = VALUES(value_);

-- 6. 闭环自动完结（WarningCycleCloseJob 读取 type_='cycle_close', code_='cycle_close' 的
--    warning_type=order_type 映射列表）。000031 闭环数据到达后由新框架立即完结 Flowable 流程。
--    幂等：已存在则不加。
INSERT INTO sys_param (code_, name_, value_, remark_, type_, is_system)
VALUES ('cycle_close', '用户级闭环流程自动完结配置', '', '', 'cycle_close', '0')
ON DUPLICATE KEY UPDATE code_ = VALUES(code_);

UPDATE sys_param
SET value_ = CONCAT(value_, ',000031=group_buy_warn')
WHERE type_ = 'cycle_close' AND code_ = 'cycle_close'
  AND LOCATE('000031=', value_) = 0;

-- 7. 闭环标识 / 预警等级字典（客户级续约类预警 000030/000031/000033 共用）
--    读取点：WarningLabelSupport → ISysCodeService.getCodeMapByType(type)
--    与旧系统同表同源（旧列表走 dict:codeLabel，旧导出走 sysCodeService.findNameByCode）。
--    ⚠ 这两个是全局字典，目标库极可能已存在（老框架页面在用），
--      故用 NOT EXISTS 只补缺失项：可重复执行、不产生重复行、不覆盖已有文案。
INSERT INTO sys_code (type_, code_, name_, order_, is_system, remark_)
SELECT t.type_, t.code_, t.name_, t.ord, '1', t.remark_
FROM (
              SELECT 'cycle_flag'    AS type_, '0' AS code_, '未闭环' AS name_, 1 AS ord, '预警闭环标识' AS remark_
    UNION ALL SELECT 'cycle_flag',    '1', '续约', 2, '预警闭环标识'
    UNION ALL SELECT 'cycle_flag',    '2', '离网', 3, '预警闭环标识'
    UNION ALL SELECT 'cycle_flag',    '3', '超时', 4, '预警闭环标识'
    UNION ALL SELECT 'warning_level', '1', 'Ⅰ级', 1, '预警等级'
    UNION ALL SELECT 'warning_level', '2', 'Ⅱ级', 2, '预警等级'
    UNION ALL SELECT 'warning_level', '3', 'Ⅲ级', 3, '预警等级'
) t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_code s WHERE s.type_ = t.type_ AND s.code_ = t.code_
);

-- 校验：确认两个字典的最终取值（以线上已有文案为准，上面的 INSERT 不会覆盖）
SELECT type_, code_, name_, order_ FROM sys_code
WHERE type_ IN ('cycle_flag', 'warning_level')
ORDER BY type_, order_;

-- 说明：
-- 1) 老框架侧（YYPT_WEB）必须同批次停派单/停闭环，否则双跑期会产生重复工单，
--    或由老框架 Activiti 通道去关新框架的 Flowable 工单。
--    见：yypt_web/resources/20260920_group_buy_warning_config.sql（不属于本目录 CI 范围）。
-- 2) 000031 在 TcCustCycleWarningJob.getOrder 中有真实关单分支（与 000032 不同），
--    老框架侧的排除务必确认生效。
-- 3) 第 7 节字典缺行时，WarningLabelSupport 会回退到代码内置兜底文案
--    （同样是 未闭环/续约/离网/超时、Ⅰ级/Ⅱ级/Ⅲ级），页面不会空白。

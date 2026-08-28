-- ============================================================================
-- 沃TV到期预警 sys_code 配置
-- 参考：余额不足预警（id=4486/4487）
-- 表结构：type_=分类, code_=值, name_=名称, setting_str3=订单类型标识
-- ============================================================================

-- 1. 流程定义 key（走通用 single_approval）
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('process_key', 'single_approval', '沃TV到期预警', 1, '沃TV到期预警', '1', 'wotv_expire_warning');

-- 2. 客户经理处理节点 task_def_key
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('task_def_key', 'wotv_expire_warning_manager_deal', '客户经理处理', 1, '沃TV到期预警', '1', 'wotv_expire_warning');

-- 3. 维系情况配置（type_=wide_follow_up，选中后值存入 order.wide_result）
-- (1) 选"完成"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('wide_follow_up', '1',  '客户已续约',                                 1,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '2',  '已通知客户，客户同意续约',                   2,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '3',  '转包月',                                     3,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '4',  '客户倒闭，不再续约',                         4,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '5',  '客户搬迁后资源不到位，不再续约',              5,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '6',  '资费太高转友商',                             6,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '7',  '网络质量太差转友商',                         7,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '8',  '多次尝试，无法联系客户',                     8,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '9',  '散户用户，无法联系',                         9,  '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '11', '非本人客户',                                 11, '沃TV到期预警-完成', '1', 'wotv_expire_warning'),
('wide_follow_up', '12', '其他',                                       12, '沃TV到期预警-完成', '1', 'wotv_expire_warning');

-- (2) 选"跟踪中"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('wide_follow_up', '20', '无法联系客户，继续跟进',                     1,  '沃TV到期预警-跟踪中', '1', 'wotv_expire_warning'),
('wide_follow_up', '21', '客户要求后续再联系',                         2,  '沃TV到期预警-跟踪中', '1', 'wotv_expire_warning'),
('wide_follow_up', '22', '其他',                                       3,  '沃TV到期预警-跟踪中', '1', 'wotv_expire_warning');

-- (3) 选"转派"
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('wide_follow_up', '30', '非本人客户',                                 1,  '沃TV到期预警-转派', '1', 'wotv_expire_warning'),
('wide_follow_up', '31', '其他',                                       2,  '沃TV到期预警-转派', '1', 'wotv_expire_warning');

-- 4. 维系预警报表下拉框（type_=maintenance_order），用于报表和未分配清单按订单类型筛选
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3) VALUES
('maintenance_order', 'wotv_expire_warning', '沃TV到期预警', 1, '沃TV到期预警', '1', 'maintenance_order');

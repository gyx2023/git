-- ============================================================================
-- 通用预警任务配置（写入 sys_param，type='warning_job'）
-- WarningJob 读取此配置驱动所有预警类型，后续新增类型只需 INSERT 一行
-- ============================================================================
INSERT INTO `sys_param` (`code_`, `name_`, `value_`, `type_`, `remark_`) VALUES
('kd_addr_warn', '宽带装机地址疑似聚合或非真实预警',
 '{"dateFormat":"yyyyMMdd","dateOffset":0,"dateOffsetUnit":"DAY","sortOrder":1,"name":"宽带装机地址疑似聚合或非真实预警"}',
 'warning_job', '通用预警任务：查询当日数据，double_approval 双节点流程');

-- ============================================================
-- 闭环自动完结配置（warningCycleCloseJob）
--
-- 用途：配置 warning_type → order_type 映射，闭环数据（cycle_flag=1）到达后
--       立即完结对应 Flowable 流程。
-- 格式：逗号分隔的 warning_type=order_type 对
-- 后续新增类型只需在 value_ 后追加即可
-- ============================================================

INSERT INTO sys_param (code_, name_, value_, remark_, type_, is_system)
VALUES ('cycle_close', '用户级闭环流程自动完结配置',
        'C00023=kd_addr_warn,C00024=wotv_expire_warning',
        'warning_type=order_type 对，逗号分隔；闭环数据(cycle_flag=1)到达后立即完结对应 Flowable 流程',
        'cycle_close', '0');

-- XXL-Job 调度中心注册：
--   JobHandler: warningCycleCloseJob
--   Cron: 0 */10 * * * * （每10分钟）

-- 验证 SQL：
-- 1. 确认配置生效
--    SELECT * FROM sys_param WHERE type_ = 'cycle_close';
--
-- 2. 查看候选工单（模拟 Job 查询逻辑，参照 TcCustCycleWarningJob）
--    SELECT o.id_, o.order_type, o.status_, o.info_id, f.WARNING_LEVEL AS cycle_warning_level
--    FROM ftp_tc_cust_cycle_warning f
--    JOIN ftp_tc_user_cust_warning c ON c.MAIN_ORDER_FLOW_NO = f.MAIN_ORDER_FLOW_NO
--    JOIN topp_order o ON o.info_id = c.id_
--    WHERE f.WARNING_TYPE IN ('C00023','C00024')
--      AND f.CYCLE_FLAG = 1
--      AND o.status_ = 'running'
--      AND o.process_engine = 'FLOWABLE'
--      AND o.order_type IN ('kd_addr_warn','wotv_expire_warning');

-- 3. 测试闭环数据写入后触发
--    旧系统 TcCustCycleWarningJob 下载 FTP 数据并写入 ftp_tc_cust_cycle_warning
--    等待 warningCycleCloseJob 执行（或手动触发）→ 验证 topp_order.status_ 变为 'end'

-- ============================================================================
-- 营销线索预警定时任务（cluesEarlyWarning）迁移——数据库变更脚本
--
-- 需求：将老框架 yypt_web 的定时任务搬迁到 zqzt_backend
--   旧：com.hollycrm.job.CluesEarlyWarning#cluesEarlyWarning
--   新：com.crm.job.job.warning.CluesEarlyWarningJob#cluesEarlyWarning
--
-- 数据库变更清单（本脚本唯一的一处变更，其余均为运行时业务数据写入）
-- ----------------------------------------------------------------------------
-- 序号 | 对象     | 变更类型     | 内容                              | 是否新增  | 幂等
-- 1    | sys_code | DML(INSERT)  | 订单类型识别 + 维系预警报表下拉配置 | 新增 1 行 | 是
--
-- 本次迁移**不涉及任何 DDL**：不建表、不加字段、不加索引。
-- 复用的既有表：sys_code、sys_param、topp_order、groupclue_order、
--   ding_message、message_record（均已存在，且新旧框架共用）。
-- 复用的既有流程：single_approval（Flowable，FLO_ 前缀表，测试/生产均已部署），
--   未新增 BPMN、未新增流程定义。
-- ----------------------------------------------------------------------------
-- 回滚（删除本次新增的配置行）：
--   DELETE FROM sys_code WHERE type_ = 'process_key'
--     AND code_ = 'clue_warnning_order' AND setting_str3 = 'maintenance_order';
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1. 订单类型识别 + 维系预警报表下拉框
--
-- 四处读取同一行配置，约定与已迁移的 kd_addr_warn / special_activity_warn 一致：
--   a) ToppOrderMapper.xml        ：JOIN sys_code ON order_type = sc.code_
--                                   AND sc.type_='process_key'
--                                   AND sc.setting_str3='maintenance_order'
--                                   → 维系预警报表 / 未分配清单 / 导出按订单类型筛选
--   b) WxProcessKeySupport        ：type_='process_key' 且 setting_str3='maintenance_order'
--                                   的 code_ 集合 = 维系订单类型集合
--   c) PendingSyncTodoServiceImpl ：findCode("process_key", orderType)
--                                   → 取 name_ 作为推送到 OA 的待办类型中文名
--   d) MaintenanceWarningMapper.xml：WHERE setting_str3='maintenance_order'
--                                   → 报表下拉框 code/name
--
-- 订单类型沿用旧系统 topp_order.order_type = 'clue_warnning_order'，与老框架数据同源，
-- 迁移期两边报表口径一致。幂等：重复执行不会产生重复行。
-- ---------------------------------------------------------------------------
INSERT INTO sys_code (type_, code_, name_, order_, remark_, is_system, setting_str3)
SELECT 'process_key', 'clue_warnning_order', '营销线索预警', 99, '营销线索预警', '1', 'maintenance_order'
FROM (SELECT 1 AS dummy) t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_code s
    WHERE s.type_ = 'process_key'
      AND s.code_ = 'clue_warnning_order'
      AND s.setting_str3 = 'maintenance_order'
);

-- 校验：查看维系预警订单类型配置全貌（本次新增行应在其中）
SELECT type_, code_, name_, order_, setting_str3 FROM sys_code
WHERE type_ = 'process_key' AND setting_str3 = 'maintenance_order'
ORDER BY order_;

-- ============================================================================
-- 明确不涉及的配置（避免后续重复添加）
-- ----------------------------------------------------------------------------
-- 1) 不注册通用预警框架开关：sys_param(type_='warn_type', code_='warning_job')
--    无需追加本类型——本任务是独立 job，不经 WarningJob 分发。
--    若后续并入通用预警框架，再补该开关并新增 IWarningQueryStrategy 实现。
-- 2) 不需要 cycle_close 闭环配置：本任务沿用旧系统语义（消息提醒 + 领导审核待办），
--    不参与新框架的用户级闭环自动完结。
-- 3) 不需要新增 sys_flow_user 默认处理人：处理人来自业务表 industry_area_user
--    （行业部领导 / 县分分管领导），与旧系统取人逻辑一致。
-- ============================================================================

-- ============================================================================
-- 运行时产生的业务数据（非变更，供核对用）
-- ----------------------------------------------------------------------------
-- 任务执行时会写入：ding_message（消息）、topp_order（工单）、
-- groupclue_order（线索-工单关联），以及 Flowable 的 FLO_ACT_RU_* / FLO_ACT_HI_* 流程表。
-- 去重依据：message_record（同一人 + 同一线索 + 同一类型只提醒一次）。
-- ============================================================================

-- ============================================================================
-- 上线步骤与影响（人工执行/核对，不属于本脚本 CI 范围）
-- ----------------------------------------------------------------------------
-- 1) XXL-Job 调度中心（测试 192.168.124.160:32271/xxl-job-admin）：
--    任务「cluesEarlyWarning」的执行器由老应用切到 zqzt-job（crm-job-server）；
--    cron 与任务参数保持不变（handler 名与旧系统一致，无需改名）。
-- 2) 老框架（yypt_web）必须同批次停用该任务：
--    yypt_web/resources/applicationContext.xml 中 bean id="cluesEarlyWarning" 的调度；
--    否则双跑期会重复产生 ding_message 消息、topp_order 工单与 groupclue_order 关联记录。
-- 3) 待办处理页仍指向老框架 /cluesEarly/toCluesEarlyAudit（线索模块尚未迁移），
--    对应 CluesEarlyWarningService.DEAL_URL 常量；线索模块迁移完成后同步调整。
-- 4) 只读数据依赖（重要）：本任务查询依赖老框架侧的表——
--    dispatch_order / dispatch_task / dispatch_order_business_data / dispatch_visit /
--    view_user / view_clue_order / industry_area_user。
--    这些表在新框架侧尚未迁移，**本任务仍在运行时不得停写或下线**；
--    后续线索模块迁移时需同步评估本任务。
-- 5) 引擎共存：新框架 Flowable 使用 FLO_ 前缀表，老框架 Activiti 使用 ACT_ 前缀表，
--    互不冲突；老框架待办页已适配读取 FLO_ACT_RU_TASK / FLO_ACT_RU_VARIABLE。
-- ============================================================================

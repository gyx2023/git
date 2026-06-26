-- ============================================================================
-- 浙江联通政企运营平台 v0.8.2 → v0.8.3 DDL
-- 生成日期: 2026-06-05
-- ============================================================================

-- 1. topp_order 表增加 process_engine 字段
--    标识流程实例所属引擎：ACTIVITI（旧系统）/ FLOWABLE（新系统）
--    用于双引擎并行期间区分数据来源，后续迁移完成后可清理
ALTER TABLE topp_order
    ADD COLUMN process_engine VARCHAR(20) DEFAULT NULL
    COMMENT '流程引擎来源：ACTIVITI/FLOWABLE';

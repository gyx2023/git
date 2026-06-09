-- ============================================
-- 支撑工时统计菜单配置SQL
-- 创建时间: 2026-05-07
-- 说明: 为支撑工时统计功能添加菜单和权限配置
-- ============================================

-- 1. 插入支撑工时统计菜单（作为流程管理的子菜单）
-- menu_id: 使用131（根据现有菜单ID递增）
-- parent_id: 4（流程管理目录）
-- order_num: 5（排在部署管理之后）
INSERT INTO `zqzt_sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (131, '支撑工时统计', 4, 5, 'supportHours', 'workflow/supportHours', '', 1, 0, 'C', '0', '0', 'workflow:supportHours:list', 'chart', 'admin', sysdate(), '', NULL, '支撑工时统计菜单');

-- 2. 插入查询权限按钮
INSERT INTO `zqzt_sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (1240, '支撑工时查询', 131, 1, '#', '', '', 1, 0, 'F', '0', '0', 'workflow:supportHours:query', '#', 'admin', sysdate(), '', NULL, '');

-- 3. 插入导出权限按钮
INSERT INTO `zqzt_sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (1241, '支撑工时导出', 131, 2, '#', '', '', 1, 0, 'F', '0', '0', 'workflow:supportHours:export', '#', 'admin', sysdate(), '', NULL, '');

COMMIT;

-- ============================================
-- 验证SQL - 执行后可查看是否插入成功
-- ============================================
-- SELECT * FROM zqzt_sys_menu WHERE menu_id IN (131, 1240, 1241);

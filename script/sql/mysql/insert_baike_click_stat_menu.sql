-- ============================================
-- 百科点击量统计菜单配置SQL
-- 说明: 为百科点击量统计功能添加菜单和权限配置
-- 注意: parent_id 请根据实际菜单结构调整
-- ============================================

-- 插入百科点击量统计菜单
INSERT INTO `zqzt_sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (132, '百科点击量统计', 4, 6, 'baikeClickStat', 'statistics/baikeClickStat/index', '', 1, 0, 'C', '0', '0', 'statistics:baikeClickStat:list', 'chart', 'admin', sysdate(), '', NULL, '百科点击量统计菜单');

-- 插入查询权限按钮
INSERT INTO `zqzt_sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (1250, '百科点击量查询', 132, 1, '#', '', '', 1, 0, 'F', '0', '0', 'statistics:baikeClickStat:query', '#', 'admin', sysdate(), '', NULL, '');

-- 插入导出权限按钮
INSERT INTO `zqzt_sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (1251, '百科点击量导出', 132, 2, '#', '', '', 1, 0, 'F', '0', '0', 'statistics:baikeClickStat:export', '#', 'admin', sysdate(), '', NULL, '');

COMMIT;

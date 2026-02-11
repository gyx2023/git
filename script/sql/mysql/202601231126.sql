-- form表增加2个字段，还需要清空历史数据
ALTER TABLE `zj_sj`.`zqzt_wf_form`
    ADD COLUMN `form_type` varchar(10) NULL COMMENT '表单类型：system-系统表单，custom-自定义URL' AFTER `content`,
    ADD COLUMN `form_url` varchar(100) NULL COMMENT '自定义表单URL路径' AFTER `form_type`;
ALTER TABLE `zj_sj`.`zqzt_wf_deploy_form`
    ADD COLUMN `form_type` varchar(10) NULL COMMENT '表单类型：system-系统表单，custom-自定义URL' AFTER `content`,
    ADD COLUMN `form_url` varchar(100) NULL COMMENT '自定义表单URL路径' AFTER `form_type`;

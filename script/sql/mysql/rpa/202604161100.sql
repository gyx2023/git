ALTER TABLE `zqzt_rpa_form_mapping`
    ADD COLUMN `script_code` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '脚本编码',
    ADD COLUMN `script_enabled` varchar(10) COLLATE utf8mb4_bin DEFAULT '0' COMMENT '脚本是否启用，0-否，1-是',
    ADD COLUMN `script_stage` varchar(20) COLLATE utf8mb4_bin DEFAULT 'ROW' COMMENT '脚本执行阶段：ROW/POST_ALL';

CREATE TABLE `zqzt_rpa_script_def` (
    `id` bigint(32) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `script_code` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '脚本编码',
    `script_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '脚本名称',
    `script_content` longtext COLLATE utf8mb4_bin COMMENT '脚本内容',
    `status` varchar(10) COLLATE utf8mb4_bin DEFAULT '1' COMMENT '状态，0-禁用，1-启用',
    `script_version` varchar(32) COLLATE utf8mb4_bin DEFAULT '1' COMMENT '脚本版本',
    `remark` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_script_code` (`script_code`)
) COMMENT = 'rpa行级脚本定义表';

CREATE INDEX `idx_rpa_form_mapping_script` ON `zqzt_rpa_form_mapping` (`biz_code`, `form_key`, `component_key`, `script_code`);

INSERT INTO `zqzt_rpa_script_def`
(`script_code`, `script_name`, `script_content`, `status`, `script_version`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES
('RPA_SUBJECT_NAME_CN_UNICOM', '合同组件-我方主体', 'def wfzt = helpers.getValueByPath(messageMap, mapping.sourceKey)\nif (wfzt == null) {\n  return null\n}\ndef parts = wfzt.toString().split(\"-\")\ndef city = parts.length > 1 ? parts[1] : \"\"\ndef result = city == \"浙江\" ? \"中国联合网络通信有限公司浙江省分公司\" : \"中国联合网络通信有限公司${city}市分公司\"\nhelpers.setValueByPath(targetMap, mapping.formKey, result)\nreturn null', '1', '1', '从地市编码映射我方主体', 'system', NOW(), 'system', NOW()),
('RPA_PAPERLESS_SIGN', '合同组件-盖章模式', 'def code = helpers.getValueByPath(messageMap, mapping.sourceKey)\nif (code == null) {\n  return null\n}\ndef value = sysParamApi.getValue(\"paperlessSign\", code.toString())\nif (value != null) {\n  helpers.setValueByPath(targetMap, mapping.formKey, Integer.valueOf(value))\n}\nreturn null', '1', '1', '根据sys_param映射盖章模式', 'system', NOW(), 'system', NOW()),
('RPA_CONTRACT_END_TIME', '合同组件-合同结束时间', 'def startTime = helpers.getValueByPath(messageMap, \"dateField_BBlT1ons\")\ndef monthsValue = helpers.getValueByPath(messageMap, mapping.sourceKey)\nif (startTime == null || monthsValue == null) {\n  return null\n}\ndef formatter = java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd\")\ndef startDate = java.time.LocalDate.parse(startTime.toString(), formatter)\ndef months = monthsValue instanceof Number ? monthsValue.intValue() : Integer.valueOf(monthsValue.toString())\ndef endDate = startDate.plusMonths(months)\nhelpers.setValueByPath(targetMap, mapping.formKey, endDate.format(formatter))\nreturn null', '1', '1', '根据开始日期+合同期数计算结束日期', 'system', NOW(), 'system', NOW()),
('RPA_CONTRACT_TIME_LIMIT', '合同组件-延展期限', 'def code = helpers.getValueByPath(messageMap, mapping.sourceKey)\nif (code == null) {\n  return null\n}\ndef value = sysParamApi.getValue(\"timeLimit\", code.toString())\nif (value != null) {\n  helpers.setValueByPath(targetMap, mapping.formKey, Integer.valueOf(value))\n}\nreturn null', '1', '1', '根据sys_param映射延展期限', 'system', NOW(), 'system', NOW()),
('RPA_PAY_PERIOD_SPECIAL', 'RPA缴费期特殊处理', 'def code = helpers.getValueByPath(messageMap, mapping.sourceKey)\nif (code == null) {\n  return null\n}\nif (\"4grckj2u1xz\".equals(code.toString())) {\n  helpers.setValueByPath(targetMap, mapping.formKey, \"2\")\n}\nreturn null', '1', '1', '缴费期特定值映射', 'system', NOW(), 'system', NOW()),
('RPA_COMPONENT_CONTRACT_INFO_SYNC', '电子表单组件-合同信息同步', 'def goodsMsg = messageMap.get(\"GOODS_MSG\")\nif (!(goodsMsg instanceof Map)) {\n  return null\n}\ndef contractInfo = goodsMsg.get(\"contractInfo\")\nif (!(contractInfo instanceof Map)) {\n  return null\n}\ntargetMap.putAll(contractInfo)\nreturn null', '1', '1', '将GOODS_MSG.contractInfo同步到合同组件', 'system', NOW(), 'system', NOW()),
('RPA_GOODS_CONTRACT_INFO_BACK_SYNC', '电子表单组件-结果回写GOODS_MSG', 'def component = messageMap.get(\"marketingContract1_Qu7n57Gt\")\ndef goodsMsg = messageMap.get(\"GOODS_MSG\")\nif (!(component instanceof Map) || !(goodsMsg instanceof Map)) {\n  return null\n}\ngoodsMsg.put(\"contractInfo\", component)\nreturn null', '1', '1', '将合同组件结果回写到GOODS_MSG.contractInfo', 'system', NOW(), 'system', NOW());

UPDATE `zqzt_rpa_form_mapping`
SET `script_code` = 'RPA_SUBJECT_NAME_CN_UNICOM', `script_enabled` = '1', `script_stage` = 'ROW'
WHERE `component_key` = 'marketingContract1_Qu7n57Gt' AND `form_key` = 'subjectName';

UPDATE `zqzt_rpa_form_mapping`
SET `script_code` = 'RPA_PAPERLESS_SIGN', `script_enabled` = '1', `script_stage` = 'ROW'
WHERE `component_key` = 'marketingContract1_Qu7n57Gt' AND `form_key` = 'paperlessSign';

UPDATE `zqzt_rpa_form_mapping`
SET `script_code` = 'RPA_CONTRACT_END_TIME', `script_enabled` = '1',`script_stage` = 'ROW'
WHERE `component_key` = 'marketingContract1_Qu7n57Gt' AND `form_key` = 'contract.endTime';

UPDATE `zqzt_rpa_form_mapping`
SET `script_code` = 'RPA_CONTRACT_TIME_LIMIT', `script_enabled` = '1', `script_stage` = 'ROW'
WHERE `component_key` = 'marketingContract1_Qu7n57Gt' AND `form_key` = 'contract.timeLimit';

UPDATE `zqzt_rpa_form_mapping`
SET `script_code` = 'RPA_PAY_PERIOD_SPECIAL', `script_enabled` = '1', `script_stage` = 'ROW'
WHERE `is_component` = '0' AND `form_key` = 'radioField_iYoB2Lrs';

UPDATE `zqzt_rpa_form_mapping`
SET `script_code` = 'RPA_COMPONENT_CONTRACT_INFO_SYNC', `script_enabled` = '1', `script_stage` = 'ROW'
WHERE `is_component` = '1'
  AND `component_key` = 'marketingContract1_Qu7n57Gt'
  AND `form_key` = 'marketingContract1_Qu7n57Gt';

INSERT INTO `zqzt_rpa_form_mapping`
(`biz_code`, `is_component`, `form_key`, `form_name`, `script_code`, `script_enabled`,`script_stage`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT b.`biz_code`,
       '0',
       '__post_all_goods_contract_info_sync__',
       '后置脚本-回写contractInfo',
       'RPA_GOODS_CONTRACT_INFO_BACK_SYNC',
       '1',
       'POST_ALL',
       'system',
       NOW(),
       'system',
       NOW()
FROM (SELECT DISTINCT `biz_code`
      FROM `zqzt_rpa_form_mapping`
      WHERE `component_key` = 'marketingContract1_Qu7n57Gt') b
WHERE NOT EXISTS (
    SELECT 1
    FROM `zqzt_rpa_form_mapping` m
    WHERE m.`biz_code` = b.`biz_code`
      AND m.`form_key` = '__post_all_goods_contract_info_sync__'
);

-- =============================================
-- 智能问数 - 政企商机看板工具配置初始化数据
-- 目标系统: smart-ai-micro / dm-service / zq_bussness
-- 注意:
--   1. api_url 中的 GATEWAY_IP:PORT 需替换为实际地址
--   2. auth_config 中的 API Key / Token 需替换为实际凭证
--   3. 分页接口(listPage/oppoListPage)的 itrstOa(用户OA)
--      建议后端改为可选，否则需通过其他方式注入
-- =============================================

-- =============================================
-- 1. 认证配置: smart-ai-micro 网关
-- =============================================
-- SA_TOKEN 模式: 转发当前用户Token + 用户身份信息
-- 说明:
--   - Authorization: Bearer <token> 由系统从当前请求上下文自动获取并注入
--   - forwardHeaders: 指定需要从当前请求转发到目标 API 的 Header 名称列表
--     loginoa=用户OA工号, loginname=用户姓名 (smart-ai-micro 网关要求)
--   这些 Header 由 RouterService 在请求开始时捕获，存储在 HeaderContextHolder 中，
--   最终由 AuthHeaderInjector 根据此配置自动注入。
INSERT INTO askdata_auth_config (system_name, system_desc, caller_id, auth_type, auth_config_json, status, create_by) VALUES
('smart_ai_micro', 'AI微服务平台-政企商机看板', 'crm-askdata', 'SA_TOKEN',
 '{"headerName": "Authorization", "headerPrefix": "Bearer ", "forwardHeaders": ["loginoa", "loginname"]}',
 '0', 'system');

-- =============================================
-- 2. 工具配置
-- =============================================

-- 注意: (SELECT id FROM askdata_auth_config WHERE system_name = 'smart_ai_micro')
-- 需要替换为实际ID，或先查询确认。

-- -------- 2.1 query_kpi_cards: 政企KPI看板顶部门户指标 --------
INSERT INTO askdata_tool_config (
    tool_name, tool_display_name, description,
    api_url, http_method, content_type,
    is_enabled,
    response_data_path, response_chart_type,
    timeout_seconds, cache_ttl_seconds,
    created_by
) VALUES (
    'query_kpi_cards',
    '政企商机KPI指标卡片',
    '查询政企商机看板顶部门户的4个KPI指标卡片数据，包括：签约合同数量+环比、签约合同金额+环比、商机数量+环比、商机金额+环比。' ||
    '日期范围固定为当年1月1日至昨天(自动计算)，无需传入。' ||
    '入参只有areaCode(地市编码)。返回结构包含QY(签约数据)和SJ(商机数据)两部分。',
    'http://GATEWAY_IP:PORT/asiainfo_micro/dmservice/zq_bussness/titleInfo',
    'POST', 'application/json',
    TRUE,
    '$.data', 'summary',
    30, 60,
    'system'
);

-- -------- 2.2 query_opportunity_summary: 商机概览 --------
INSERT INTO askdata_tool_config (
    tool_name, tool_display_name, description,
    api_url, http_method, content_type,
    is_enabled,
    response_data_path, response_chart_type,
    timeout_seconds, cache_ttl_seconds,
    created_by
) VALUES (
    'query_opportunity_summary',
    '商机概览聚合查询',
    '查询政企商机概览数据，按地市聚合返回。' ||
    '支持按时间范围、业务维度(算网数智/联网通信/国际业务)、行业大类(2G/2B)、主业务类型(多选)、客户行业、金额范围筛选。' ||
    '返回各地市的：商机数量、商机金额(万元)、本年签约数量、本年签约金额(万元)、商机转化数量、转化金额(万元)、' ||
    '亿级商机数量、千万级商机数量、商机金额同比(%)、累计拜访次数、平均拜访次数。',
    'http://GATEWAY_IP:PORT/asiainfo_micro/dmservice/zq_bussness/oppoSummarize',
    'POST', 'application/json',
    TRUE,
    '$.data', 'table',
    30, 60,
    'system'
);

-- -------- 2.3 query_sign_summary: 签约概览 --------
INSERT INTO askdata_tool_config (
    tool_name, tool_display_name, description,
    api_url, http_method, content_type,
    is_enabled,
    response_data_path, response_chart_type,
    timeout_seconds, cache_ttl_seconds,
    created_by
) VALUES (
    'query_sign_summary',
    '签约概览聚合查询',
    '查询政企签约概览数据，按地市或区县聚合返回。' ||
    '支持按时间范围、业务类型(数科集成/联通云/数据服务/数智应用/数据中心/联网通信)、客户行业、金额范围筛选。' ||
    '返回各地市/区县的：签约数量、签约金额(万元)、签约金额同比(%)、签约金额完成率(%)、' ||
    '亿级合同数量(>=10000万)、千万级合同数量(1000~10000万)、百万级合同数量(100~1000万)、' ||
    '签约前置商机比例(%)、年化签约金额(万元)。',
    'http://GATEWAY_IP:PORT/asiainfo_micro/dmservice/zq_bussness/signSummarize',
    'POST', 'application/json',
    TRUE,
    '$.data', 'table',
    30, 60,
    'system'
);

-- -------- 2.4 query_opportunity_detail: 商机明细列表 --------
INSERT INTO askdata_tool_config (
    tool_name, tool_display_name, description,
    api_url, http_method, content_type,
    is_enabled,
    response_data_path, response_total_path, response_chart_type,
    timeout_seconds, cache_ttl_seconds,
    created_by
) VALUES (
    'query_opportunity_detail',
    '商机明细列表查询',
    '查询政企商机明细列表(分页)，每条记录是一个商机项目。' ||
    '支持按时间范围、地市、区县、金额范围、业务维度、行业大类、主业务类型、客户行业、客户经理、异常商机筛选，并支持排序。' ||
    '返回字段包括：商机编号、名称、创建时间、客户经理、地市、预估金额(万元)、商机状态、主业务类型、' ||
    '客户行业、是否签约、中标金额、合同金额、项目信息、拜访次数、支撑次数、关注状态等。' ||
    '注意：dataType固定传"SJ"(商机)，itrstOa为必填(用户OA，用于查询关注状态，传空则无关注数据)。',
    'http://GATEWAY_IP:PORT/asiainfo_micro/dmservice/zq_bussness/oppoListPage',
    'POST', 'application/json',
    TRUE,
    '$.rows', '$.total', 'table',
    30, 0,
    'system'
);

-- -------- 2.5 query_sign_detail: 签约明细列表 --------
INSERT INTO askdata_tool_config (
    tool_name, tool_display_name, description,
    api_url, http_method, content_type,
    is_enabled,
    response_data_path, response_total_path, response_chart_type,
    timeout_seconds, cache_ttl_seconds,
    created_by
) VALUES (
    'query_sign_detail',
    '签约明细列表查询',
    '查询政企签约项目明细列表(分页)，每条记录是一个合同。' ||
    '支持按时间范围、地市、区县、金额范围、业务类型、客户行业、客户经理筛选，并支持排序。' ||
    '返回字段包括：合同编号、合同名称、合同金额(万元)、合同类型、审批日期、业务类型、客户经理、' ||
    '客户名称、客户行业、名单制客户、项目编码、项目名称、关注状态等。' ||
    '注意：dataType固定传"HT"(合同)，itrstOa为必填(用户OA，用于查询关注状态)。',
    'http://GATEWAY_IP:PORT/asiainfo_micro/dmservice/zq_bussness/listPage',
    'POST', 'application/json',
    TRUE,
    '$.rows', '$.total', 'table',
    30, 0,
    'system'
);

-- -------- 2.6 get_trade_cluster_list: 客户行业下拉 --------
INSERT INTO askdata_tool_config (
    tool_name, tool_display_name, description,
    api_url, http_method, content_type,
    is_enabled,
    response_data_path, response_chart_type,
    timeout_seconds, cache_ttl_seconds,
    created_by
) VALUES (
    'get_trade_cluster_list',
    '客户行业列表查询',
    '查询政企客户行业下拉列表。用于辅助其他查询工具的tradeClusterId参数。' ||
    '无入参。返回行业ID(TRADE_CLUSTER_ID)和行业名称(SF_TRADE_CLUSTER_NAME)。',
    'http://GATEWAY_IP:PORT/asiainfo_micro/dmservice/zq_bussness/getCustomer',
    'GET', 'application/json',
    TRUE,
    '$.data', 'table',
    10, 3600,
    'system'
);

-- -------- 2.7 get_business_type_list: 业务类型下拉 --------
INSERT INTO askdata_tool_config (
    tool_name, tool_display_name, description,
    api_url, http_method, content_type,
    is_enabled,
    response_data_path, response_chart_type,
    timeout_seconds, cache_ttl_seconds,
    created_by
) VALUES (
    'get_business_type_list',
    '商机主业务类型列表查询',
    '查询商机主业务类型下拉列表。用于辅助query_opportunity_summary的oppoTypes参数。' ||
    '可选入参svcType(业务维度过滤，如"算网数智"/"联网通信"/"国际业务")。返回业务类型名称列表(TYPE_NAME)。',
    'http://GATEWAY_IP:PORT/asiainfo_micro/dmservice/zq_bussness/getBusinessType',
    'GET', 'application/json',
    TRUE,
    '$.data', 'table',
    10, 3600,
    'system'
);

-- =============================================
-- 3. 参数定义
-- =============================================

-- -------- 3.1 query_kpi_cards 的参数 --------
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, target_param_name,
    clarify_priority, sort_order
) VALUES (
    (SELECT id FROM askdata_tool_config WHERE tool_name = 'query_kpi_cards'),
    'areaCode', '地市编码', 'string',
    '地市编码，全省传"999"，杭州="571"，宁波="574"，温州="577"，嘉兴="573"，湖州="572"，绍兴="575"，金华="579"，衢州="570"，舟山="580"，台州="576"，丽水="578"',
    TRUE, 'areaCode',
    1, 1
);

-- -------- 3.2 query_opportunity_summary 的参数 --------
-- 必填参数
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, target_param_name,
    clarify_priority, sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'areaCode', '地市编码', 'string',
 '地市编码，全省传"999"，杭州="571"，宁波="574"等',
 TRUE, 'areaCode',
 1, 1),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'startTime', '开始日期', 'string',
 '开始日期，格式YYYYMMDD，如"20260101"。注意：不支持跨年查询，开始和结束日期必须在同一年。',
 TRUE, 'startTime',
 2, 2),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'endTime', '结束日期', 'string',
 '结束日期，格式YYYYMMDD，如"20260610"。注意：不支持跨年查询。',
 TRUE, 'endTime',
 3, 3);

-- 可选参数
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, enum_values, target_param_name,
    sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'svcType', '业务维度', 'string',
 '商机业务维度筛选：空字符串=全部商机、"算网数智"=算网数智商机、"联网通信"=联网通信商机、"国际业务"=国际业务商机',
 FALSE, '""', '["", "算网数智", "联网通信", "国际业务"]', 'svcType',
 4),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'bgType', '行业大类', 'string',
 '商机行业大类：空字符串=全部、"2G"=政府类、"2B"=企业类',
 FALSE, '""', '["", "2G", "2B"]', 'bgType',
 5),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'tradeClusterId', '客户行业ID', 'string',
 '客户行业ID，可通过get_trade_cluster_list工具获取。传空字符串=不筛选',
 FALSE, '""', NULL, 'tradeClusterId',
 6),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'arrairAmountStart', '金额范围下限(万元)', 'integer',
 '商机金额范围下限，单位万元。与arrairAmountEnd配合使用。例如查询100万以上的商机传"100"',
 FALSE, NULL, NULL, 'arrairAmountStart',
 7),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
 'arrairAmountEnd', '金额范围上限(万元)', 'integer',
 '商机金额范围上限，单位万元。与arrairAmountStart配合使用。例如查询1000万以下的商机传"1000"',
 FALSE, NULL, NULL, 'arrairAmountEnd',
 8);

-- oppoTypes 是array类型，特殊处理
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, target_param_name,
    sort_order
) VALUES (
    (SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_summary'),
    'oppoTypes', '主业务类型列表', 'array',
    '商机主业务类型名称列表(多选)，如["云计算","大数据"]。可通过get_business_type_list工具获取可选值。空数组=不筛选',
    FALSE, '[]', 'oppoTypes',
    9
);

-- -------- 3.3 query_sign_summary 的参数 --------
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, target_param_name,
    clarify_priority, sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_summary'),
 'areaCode', '地市编码', 'string',
 '地市编码，全省传"999"，杭州="571"，宁波="574"等',
 TRUE, 'areaCode',
 1, 1),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_summary'),
 'startTime', '开始日期', 'string',
 '开始日期，格式YYYYMMDD，如"20260101"。注意：不支持跨年查询。',
 TRUE, 'startTime',
 2, 2),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_summary'),
 'endTime', '结束日期', 'string',
 '结束日期，格式YYYYMMDD，如"20260610"。注意：不支持跨年查询。',
 TRUE, 'endTime',
 3, 3);

INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, enum_values, target_param_name,
    sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_summary'),
 'svcType', '业务类型', 'string',
 '业务类型筛选：空字符串=全部、"数科集成"="数科集成"、"联通云"="联通云"、"数据服务"="数据服务"、"数智应用"="数智应用"、"数据中心"="数据中心"、"联网通信"="联网通信"',
 FALSE, '""', '["", "数科集成", "联通云", "数据服务", "数智应用", "数据中心", "联网通信"]', 'svcType',
 4),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_summary'),
 'tradeClusterId', '客户行业ID', 'string',
 '客户行业ID，可通过get_trade_cluster_list工具获取。传空字符串=不筛选',
 FALSE, '""', NULL, 'tradeClusterId',
 5),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_summary'),
 'arrairAmountStart', '金额范围下限(万元)', 'integer',
 '合同金额范围下限，单位万元。与arrairAmountEnd配合使用。',
 FALSE, NULL, NULL, 'arrairAmountStart',
 6),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_summary'),
 'arrairAmountEnd', '金额范围上限(万元)', 'integer',
 '合同金额范围上限，单位万元。与arrairAmountStart配合使用。',
 FALSE, NULL, NULL, 'arrairAmountEnd',
 7);

-- -------- 3.4 query_opportunity_detail 的参数 --------
-- 必填
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, target_param_name,
    clarify_priority, sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'areaCode', '地市编码', 'string',
 '地市编码，全省传"999"，杭州="571"，宁波="574"等',
 TRUE, 'areaCode',
 1, 1),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'startTime', '开始日期', 'string',
 '开始日期(商机创建时间)，格式YYYYMMDD，如"20260101"',
 TRUE, 'startTime',
 2, 2),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'endTime', '结束日期', 'string',
 '结束日期(商机创建时间)，格式YYYYMMDD，如"20260610"',
 TRUE, 'endTime',
 3, 3),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'pageNum', '页码', 'integer',
 '分页页码，从1开始',
 TRUE, 'pageNum',
 4, 4),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'pageSize', '每页条数', 'integer',
 '每页返回记录数，建议20',
 TRUE, 'pageSize',
 5, 5);

-- 注意: dataType 和 itrstOa 在此用默认值注入，不作为AI需要填的参数
-- dataType='SJ'(商机), itrstOa 需按实际环境配置
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, target_param_name,
    sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'dataType', '数据类型(固定)', 'string',
 '固定传"SJ"表示查询商机数据。系统自动填充，AI无需关心。',
 FALSE, '"SJ"', 'dataType',
 99),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'itrstOa', '用户OA(固定)', 'string',
 '用户OA账号，用于查询关注状态。系统自动填充（需改造为从上下文获取），目前固定传"SYSTEM"。',
 FALSE, '"SYSTEM"', 'itrstOa',
 99);

-- 可选参数
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, target_param_name,
    sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'countyCode', '区县编码', 'string',
 '区县编码，如"57101"。传空=不按区县筛选',
 FALSE, '""', 'countyCode',
 10),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'svcType', '业务维度', 'string',
 '商机业务维度筛选："算网数智"/"联网通信"/"国际业务"',
 FALSE, '""', 'svcType',
 11),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'bgType', '行业大类', 'string',
 '商机行业大类："2G"=政府类、"2B"=企业类',
 FALSE, '""', 'bgType',
 12),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'tradeClusterId', '客户行业ID', 'string',
 '客户行业ID筛选',
 FALSE, '""', 'tradeClusterId',
 13),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'customerManager', '客户经理', 'string',
 '客户经理姓名模糊搜索',
 FALSE, '""', 'customerManager',
 14),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'abnmlOppo', '是否异常商机', 'string',
 '异常商机筛选：空字符串=全部、"1"=异常、"0"=正常',
 FALSE, '""', 'abnmlOppo',
 15),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'arrairAmountStart', '金额范围下限(万元)', 'integer',
 '商机预估金额范围下限，单位万元',
 FALSE, NULL, 'arrairAmountStart',
 16),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'arrairAmountEnd', '金额范围上限(万元)', 'integer',
 '商机预估金额范围上限，单位万元',
 FALSE, NULL, 'arrairAmountEnd',
 17),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'ctrtStartMoney', '签约金额下限(万元)', 'integer',
 '签约合同金额范围下限，单位万元',
 FALSE, NULL, 'ctrtStartMoney',
 18),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'ctrtEndMoney', '签约金额上限(万元)', 'integer',
 '签约合同金额范围上限，单位万元',
 FALSE, NULL, 'ctrtEndMoney',
 19),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'orderFlag', '排序字段', 'string',
 '排序字段标识："1"=按预估金额排序',
 FALSE, NULL, 'orderFlag',
 20),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
 'orderType', '排序方向', 'string',
 '排序方向："ASC"=升序、"DESC"=降序',
 FALSE, NULL, 'orderType',
 21);

-- oppoTypes array参数
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, target_param_name,
    sort_order
) VALUES (
    (SELECT id FROM askdata_tool_config WHERE tool_name = 'query_opportunity_detail'),
    'oppoTypes', '主业务类型列表', 'array',
    '商机主业务类型名称列表(多选)，如["云计算","大数据"]',
    FALSE, '[]', 'oppoTypes',
    22
);

-- -------- 3.5 query_sign_detail 的参数 --------
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, target_param_name,
    clarify_priority, sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'areaCode', '地市编码', 'string',
 '地市编码，全省传"999"，杭州="571"，宁波="574"等',
 TRUE, 'areaCode',
 1, 1),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'startTime', '开始日期', 'string',
 '开始日期(合同审批日期)，格式YYYYMMDD，如"20260101"',
 TRUE, 'startTime',
 2, 2),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'endTime', '结束日期', 'string',
 '结束日期(合同审批日期)，格式YYYYMMDD，如"20260610"',
 TRUE, 'endTime',
 3, 3),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'pageNum', '页码', 'integer',
 '分页页码，从1开始',
 TRUE, 'pageNum',
 4, 4),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'pageSize', '每页条数', 'integer',
 '每页返回记录数，建议20',
 TRUE, 'pageSize',
 5, 5);

INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, target_param_name,
    sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'dataType', '数据类型(固定)', 'string',
 '固定传"HT"表示查询合同数据。系统自动填充。',
 FALSE, '"HT"', 'dataType',
 99),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'itrstOa', '用户OA(固定)', 'string',
 '用户OA账号，用于查询关注状态。系统自动填充（需改造），目前固定传"SYSTEM"。',
 FALSE, '"SYSTEM"', 'itrstOa',
 99);

INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, target_param_name,
    sort_order
) VALUES
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'countyCode', '区县编码', 'string',
 '区县编码筛选。传空=不筛选',
 FALSE, '""', 'countyCode',
 10),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'svcType', '业务类型', 'string',
 '业务类型筛选："数科集成"/"联通云"/"数据服务"/"数智应用"/"数据中心"/"联网通信"',
 FALSE, '""', 'svcType',
 11),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'tradeClusterId', '客户行业ID', 'string',
 '客户行业ID筛选',
 FALSE, '""', 'tradeClusterId',
 12),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'customerManager', '客户经理', 'string',
 '客户经理OA或姓名模糊搜索',
 FALSE, '""', 'customerManager',
 13),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'arrairAmountStart', '金额范围下限(万元)', 'integer',
 '合同金额范围下限，单位万元',
 FALSE, NULL, 'arrairAmountStart',
 14),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'arrairAmountEnd', '金额范围上限(万元)', 'integer',
 '合同金额范围上限，单位万元',
 FALSE, NULL, 'arrairAmountEnd',
 15),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'orderFlag', '排序字段', 'string',
 '排序字段标识："1"=按合同金额排序',
 FALSE, NULL, 'orderFlag',
 16),
((SELECT id FROM askdata_tool_config WHERE tool_name = 'query_sign_detail'),
 'orderType', '排序方向', 'string',
 '排序方向："ASC"=升序、"DESC"=降序',
 FALSE, NULL, 'orderType',
 17);

-- -------- 3.6 get_trade_cluster_list: 无参数 --------

-- -------- 3.7 get_business_type_list: 可选参数 --------
INSERT INTO askdata_tool_param (
    tool_id, param_name, param_display_name, param_type,
    description, is_required, default_value, enum_values, target_param_name,
    sort_order
) VALUES (
    (SELECT id FROM askdata_tool_config WHERE tool_name = 'get_business_type_list'),
    'svcType', '业务维度过滤', 'string',
    '业务维度过滤：空字符串=全部、"算网数智"=算网数智类业务类型、"联网通信"=联网通信类、"国际业务"=国际业务类',
    FALSE, '""', '["", "算网数智", "联网通信", "国际业务"]', 'svcType',
    1
);

-- =============================================
-- 4. 验证查询
-- =============================================
-- 查询所有工具配置:
-- SELECT tc.id, tc.tool_name, tc.tool_display_name, tc.api_url,
--        (SELECT count(1) FROM askdata_tool_param tp WHERE tp.tool_id = tc.id) AS param_count
-- FROM askdata_tool_config tc
-- WHERE tc.created_by = 'system'
-- ORDER BY tc.id;
--
-- 查询某个工具的完整参数:
-- SELECT tp.param_name, tp.param_type, tp.is_required,
--        tp.description, tp.enum_values, tp.default_value,
--        tp.target_param_name, tp.sort_order
-- FROM askdata_tool_param tp
-- JOIN askdata_tool_config tc ON tp.tool_id = tc.id
-- WHERE tc.tool_name = 'query_kpi_cards'
-- ORDER BY tp.sort_order;

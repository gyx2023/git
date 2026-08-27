### TOOL_CALL 工具调用
- 集客发展量是多少 > 本月 > 华东
- 工具配置不存在：get_operation_report
```json lines
{"total_tokens":2103,"event":"message","task_id":"01KT8KH01ZYMCSQDTHY5N7HN4F","id":"01KT8KH01ZYMCSQDTHY5N7HN4F","conversation_id":"01KT8KGZSANFTW66MCH5JA287M","answer":"{\"output\": \"{\\\"intent\\\":\\\"data_query\\\",\\\"confidence\\\":0.85,\\\"tool_name\\\":\\\"get_operation_report\\\",\\\"entities\\\":{\\\"metric\\\":[\\\"发展量\\\"]},\\\"needs_clarification\\\":true,\\\"missing_slots\\\":[\\\"time_range\\\",\\\"region\\\"],\\\"clarify_question\\\":\\\"请问您要查询哪个时间范围和地区？\\\",\\\"clarify_options\\\":[\\\"今天\\\",\\\"本周\\\",\\\"本月\\\",\\\"上月\\\",\\\"本年\\\"]}\"}","created_at":1780552866,"latency":6.354,"input_tokens":2021,"output_tokens":82,"start_time_first_resp":1780552866067,"latency_first_resp":6355}
{"total_tokens":2217,"event":"message","task_id":"01KT8KHGYQFWXHA36Z0ZQ0K9DA","id":"01KT8KHGYQFWXHA36Z0ZQ0K9DA","conversation_id":"01KT8KGZSANFTW66MCH5JA287M","answer":"{\"output\": \"{\\\"intent\\\":\\\"data_query\\\",\\\"confidence\\\":0.85,\\\"tool_name\\\":\\\"get_operation_report\\\",\\\"entities\\\":{\\\"time_range\\\":\\\"this_month\\\",\\\"metric\\\":[\\\"发展量\\\"]},\\\"needs_clarification\\\":true,\\\"missing_slots\\\":[\\\"region\\\"],\\\"clarify_question\\\":\\\"请问您要查询哪个地区？\\\",\\\"clarify_options\\\":[\\\"华东\\\",\\\"杭州\\\",\\\"临安\\\"]}\"}","created_at":1780552892,"latency":15.163,"input_tokens":2137,"output_tokens":80,"start_time_first_resp":1780552892180,"latency_first_resp":15164}
{"total_tokens":2320,"event":"message","task_id":"01KT8KJ76HBTQ8T7JP334DRT2N","id":"01KT8KJ76HBTQ8T7JP334DRT2N","conversation_id":"01KT8KGZSANFTW66MCH5JA287M","answer":"{\"output\": \"{\\\"intent\\\":\\\"data_query\\\",\\\"confidence\\\":0.95,\\\"tool_name\\\":\\\"get_operation_report\\\",\\\"entities\\\":{\\\"time_range\\\":\\\"this_month\\\",\\\"region\\\":\\\"huadong\\\",\\\"metric\\\":[\\\"发展量\\\"]},\\\"needs_clarification\\\":false,\\\"missing_slots\\\":[],\\\"clarify_question\\\":\\\"\\\",\\\"clarify_options\\\":[]}\"}","created_at":1780552908,"latency":8.882,"input_tokens":2250,"output_tokens":70,"start_time_first_resp":1780552908677,"latency_first_resp":8882}
```

### SEMANTIC_QUERY 语义查询 指标
- 今年杭州合同数是多少
- 暂不支持指标「合同数」的查询，请尝试查询其他业务指标
```json lines
{"total_tokens":2087,"event":"message","task_id":"01KT8Q9YFNEGARQGQW04WG7C9Z","id":"01KT8Q9YFNEGARQGQW04WG7C9Z","conversation_id":"01KT8Q9Y6WTWDES28MAK4HSBV1","answer":"{\"output\": \"{\\\"intent\\\":\\\"data_query\\\",\\\"confidence\\\":0.85,\\\"tool_name\\\":null,\\\"entities\\\":{\\\"time_range\\\":\\\"this_year\\\",\\\"region\\\":\\\"hangzhou\\\",\\\"metric\\\":[\\\"合同数\\\"]},\\\"needs_clarification\\\":false,\\\"missing_slots\\\":[],\\\"clarify_question\\\":\\\"\\\",\\\"clarify_options\\\":[]}\"}","created_at":1780556838,"latency":15.53,"input_tokens":2021,"output_tokens":66,"start_time_first_resp":1780556838560,"latency_first_resp":15530}
```

### TEXT_TO_SQL SQL 沙箱
- 合同数是多少
- 已识别 SQL 查询计划： ```sql SELECT * LIMIT 50 ``` ⚠️ 未识别到查询的数据表名称，请在问题中明确指定查询对象。 示例：「查询 sales 表中上个月的销售额」
- 智能体回复如下:
```json lines
{"total_tokens":2230,"event":"message","task_id":"01KT8PBMWTEAV4MT2TWZMP0WFZ","id":"01KT8PBMWTEAV4MT2TWZMP0WFZ","conversation_id":"01KT8NW1R6VGBMD7509Q48AM9P","answer":"{\"output\": \"{\\n  \\\"intent\\\": \\\"data_query\\\",\\n  \\\"confidence\\\": 0.7,\\n  \\\"tool_name\\\": null,\\n  \\\"entities\\\": {\\n    \\\"metric\\\": [\\\"合同数\\\"]\\n  },\\n  \\\"needs_clarification\\\": false,\\n  \\\"missing_slots\\\": [],\\n  \\\"clarify_question\\\": \\\"\\\",\\n  \\\"clarify_options\\\": []\\n}\"}","created_at":1780555839,"latency":9.527,"input_tokens":2151,"output_tokens":79,"start_time_first_resp":1780555839700,"latency_first_resp":9528}
{"total_tokens":2339,"event":"message","task_id":"01KT8PCGW535HKKCSJHW5517EJ","id":"01KT8PCGW535HKKCSJHW5517EJ","conversation_id":"01KT8NW1R6VGBMD7509Q48AM9P","answer":"{\"output\": \"{\\n  \\\"intent\\\": \\\"data_query\\\",\\n  \\\"confidence\\\": 0.7,\\n  \\\"tool_name\\\": null,\\n  \\\"entities\\\": {\\n    \\\"metric\\\": [\\\"合同数\\\"]\\n  },\\n  \\\"needs_clarification\\\": false,\\n  \\\"missing_slots\\\": [],\\n  \\\"clarify_question\\\": \\\"\\\",\\n  \\\"clarify_options\\\": []\\n}\"}","created_at":1780555862,"latency":3.665,"input_tokens":2260,"output_tokens":79,"start_time_first_resp":1780555862488,"latency_first_resp":3665}
```
- 上个月合同数
- 已识别 SQL 查询计划： ```sql SELECT * WHERE time_range = 'last_month' LIMIT 50 ``` ⚠️ 未识别到查询的数据表名称，请在问题中明确指定查询对象。 示例：「查询 sales 表中上个月的销售额」
- 智能体回复如下:
```json lines
{"total_tokens":2081,"event":"message","task_id":"01KTNS7HE1BV2K2CWWGCGSKHC6","id":"01KTNS7HE1BV2K2CWWGCGSKHC6","conversation_id":"01KTNS7H04WRP01S29MJP8X67B","answer":"{\"output\": \"{\\\"intent\\\":\\\"data_query\\\",\\\"confidence\\\":0.7,\\\"tool_name\\\":null,\\\"entities\\\":{\\\"time_range\\\":\\\"last_month\\\",\\\"metric\\\":[\\\"合同数\\\"]},\\\"needs_clarification\\\":false,\\\"missing_slots\\\":[],\\\"clarify_question\\\":\\\"\\\",\\\"clarify_options\\\":[]}\"}","created_at":1780995051,"latency":2.272,"input_tokens":2020,"output_tokens":61,"start_time_first_resp":1780995051171,"latency_first_resp":2272}
```


###  闲聊意图
- 今天天气怎么样
- 正在识别意图...正在分析查询...正在执行 [FALLBACK_KB] 策略...抱歉，未能理解您的问题。请重新描述，
```json lines
{"total_tokens":2067,"event":"message","task_id":"01KT8QR8EY52ZG48AZ06V0PZ0D","id":"01KT8QR8EY52ZG48AZ06V0PZ0D","conversation_id":"01KT8QR85GGX0ZNT3BSE0DPX79","answer":"{\"output\": \"{\\\"intent\\\":\\\"chat\\\",\\\"confidence\\\":0.95,\\\"tool_name\\\":null,\\\"entities\\\":{},\\\"needs_clarification\\\":false,\\\"missing_slots\\\":[],\\\"clarify_question\\\":\\\"\\\",\\\"clarify_options\\\":[]}\"}","created_at":1780557296,"latency":4.688,"input_tokens":2019,"output_tokens":48,"start_time_first_resp":1780557296688,"latency_first_resp":4688}
```


tool_call
上月杭州合同数是多少 -> 数字
今年杭州合同数的发展趋势  -> 表格
那宁波的呢


SemanticQuery
今年杭州新增客户数 -> 数字
今年各月杭州新增用户趋势 -> 图表
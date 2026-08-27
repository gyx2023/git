# 智能问数（AskData）模块技术概要

## 1. 模块概述

AskData（智能问数）是基于 HiAgent AI 平台的智能数据查询模块，提供自然语言 → 业务数据的端到端查询能力。用户以自然语言提问，系统经过意图识别 → 路由决策 → 执行查询 → 结果展示的完整链路，最终返回结构化的图表或文本答案。

整个模块遵循**后端路由决策 + Agent 驱动的二阶段架构**设计：HiAgent 负责意图识别和语义理解，Java 层负责安全控制、路由决策和工具调用执行。

---

## 2. 模块定位

- **所属项目**: `zqzt_backend`（浙江联通政企运营平台）
- **Maven 模块**: `crm-askdata`（独立模块，通过 crm-admin 暴露 REST API）
- **前端项目**: `zqzt_frontend`，路径 `src/views/askdata/`
- **数据库**: 使用 `askdata_*` 前缀表（工具配置、参数定义、指标元数据等）

---

## 3. 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 (Vue 2)                              │
│  ┌─────────────┐  ┌──────────┐  ┌──────────────┐               │
│  │ ChatWindow   │  │MessageBub│  │ ChartRenderer │               │
│  │ (useSSEChat) │  │ ble      │  │ (ECharts)    │               │
│  └──────┬───────┘  └──────────┘  └──────────────┘               │
│         │ SSE (EventSource) / REST API                          │
└─────────┼───────────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────────────────────┐
│                    Controller 层                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              AskDataController                            │   │
│  │  GET  /askdata/chat/stream      (SSE 流式)               │   │
│  │  POST /askdata/query            (同步查询)                │   │
│  │  POST /askdata/feedback         (反馈上报)                │   │
│  │  POST /askdata/conversation/reset(会话重置)               │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────────────────────┐
│                     Service 层                                  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                 RouterService (核心引擎)                   │   │
│  │  1. recognizeIntent → 2. handleClarification →           │   │
│  │  3. makeRoutingDecision → 4. executeQuery →              │   │
│  │  5. streamResultToClient                                 │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌───────────────┐     │
│  │IntentPars│ │Confidence │ │Conversati│ │AuditLogger    │     │
│  │er        │ │Calibrator │ │onContext │ │(审计日志)     │     │
│  └──────────┘ └───────────┘ └──────────┘ └───────────────┘     │
└──────────────────────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────────────────────┐
│                   Executor 层 (策略模式)                          │
│                                                                  │
│  ┌──────────────┐ ┌────────────┐ ┌──────────┐ ┌──────────────┐ │
│  │ToolCallExecut│ │SemanticSql │ │TextToSql │ │FallbackQuery │ │
│  │or (TOOL_CALL)│ │Executor    │ │Executor  │ │Executor      │ │
│  │              │ │(SEMANTIC_Q)│ │(TEXT_TO_)│ │(FALLBACK_KB) │ │
│  └──────┬───────┘ └─────┬──────┘ └──────────┘ └──────────────┘ │
│         │               │                                       │
│  ┌──────▼───────┐ ┌─────▼──────┐                                │
│  │DatabaseDrive │ │SqlSandboxEx│                                │
│  │nApiTool      │ │ecutor      │                                │
│  │(动态工具调用) │ │(SQL 沙箱)  │                                │
│  └──────────────┘ └────────────┘                                │
└──────────────────────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────────────────────┐
│                   外部系统                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│  │   HiAgent     │  │  业务系统API  │  │   数据库          │      │
│  │  (AI 平台)    │  │  (工具调用)   │  │  (指标/SQL 执行)  │      │
│  └──────────────┘  └──────────────┘  └──────────────────┘      │
└──────────────────────────────────────────────────────────────────┘
```

---

## 4. 核心流程详解

### 4.1 完整请求链路（SSE 流式）

```
用户输入 "华东区上月销售额"
        │
        ▼
┌─────────────────────────────────────────────┐
│ 1. 意图识别 (recognizeIntent)                │
│    ├─ 获取/创建 HiAgent 会话 (convId)        │
│    ├─ 调用 HiAgent chatQueryBlocking         │
│    ├─ IntentParser 解析 answer → IntentResult│
│    └─ ConfidenceCalibrator 校准置信度        │
├─────────────────────────────────────────────┤
│ IntentResult 示例:                           │
│ {                                            │
│   "intent": "data_query",                    │
│   "confidence": 0.92,                        │
│   "toolName": "sales_summary",              │
│   "entities": {                              │
│     "region": "华东区",                       │
│     "timeRange": "last_month"               │
│   },                                         │
│   "needClarify": false                       │
│ }                                            │
├─────────────────────────────────────────────┤
│ 2. 澄清处理 (handleClarificationIfNeeded)    │
│    └─ needClarify=false → 跳过澄清           │
├─────────────────────────────────────────────┤
│ 3. 路由决策 (makeRoutingDecision)            │
│    ├─ data_query + 0.92 ≥ 0.85 (high)       │
│    └─ toolName非空 → TOOL_CALL               │
├─────────────────────────────────────────────┤
│ 4. 执行查询 (executeQuery)                   │
│    ├─ ToolCallExecutor.execute()            │
│    │   ├─ 权限校验                           │
│    │   ├─ 限流检查                           │
│    │   ├─ DatabaseDrivenApiTool.execute()   │
│    │   │   ├─ 参数校验 (UniversalParam...)   │
│    │   │   ├─ 参数映射 (UniversalParam...)   │
│    │   │   ├─ 调用业务系统 API               │
│    │   │   └─ 响应格式化 (字段映射+图表配置) │
│    │   └─ ResultPostProcessor 后处理         │
│    └─ 结果 → QueryResult                     │
├─────────────────────────────────────────────┤
│ 5. 流式推送 (streamResultToClient)           │
│    └─ SSE 事件序列:                          │
│       text("正在识别意图...")                 │
│       text("正在分析查询...")                 │
│       text("正在执行 [TOOL_CALL] 策略...")    │
│       text(逐字打字机效果)                   │
│       chart(图表数据)                        │
│       end(suggestions, latencyMs)           │
└─────────────────────────────────────────────┘
```

### 4.2 路由决策矩阵

| 意图类型 | 置信度区间 | 条件 | 路由策略 | 说明 |
|---------|-----------|------|---------|------|
| data_query | ≥ 0.85 (high) | toolName 非空 | **TOOL_CALL** | 调用预注册的报表 API |
| data_query | ≥ 0.85 (high) | toolName 为空 | **SEMANTIC_QUERY** | 语义指标查询 |
| data_query | ≥ 0.85 (high) | 缺必填参数 | **FALLBACK_KB** | 安全兜底 |
| data_query | [0.60, 0.85) | toolName 非空 | **TOOL_CALL** | session 锁定兜底 |
| data_query | [0.60, 0.85) | 有 SQL 权限 | **TEXT_TO_SQL** | AI 生成 SQL |
| data_query | [0.60, 0.85) | 无 SQL 权限 | **SEMANTIC_QUERY** | 语义查询 |
| data_query | < 0.60 | — | **FALLBACK_KB** | 置信度不足降级 |
| knowledge | — | — | **FALLBACK_KB** | 知识类走知识库 |
| chat | — | — | **FALLBACK_KB** | 闲聊走知识库 |
| unknown | — | — | **FALLBACK_KB** | 默认降级 |

---

## 5. 模块结构

### 5.1 包结构

```
com.crm.askdata/
├── client/              HiAgent 平台 HTTP 客户端
│   └── HiAgentClient.java
├── config/              配置属性类
│   ├── HiAgentProperties.java        HiAgent 连接配置
│   ├── HiAgentRestTemplateConfig.java RestTemplate 配置
│   ├── MetadataProperties.java       元数据配置
│   ├── Resilience4jConfig.java       熔断/重试/超时配置
│   ├── RouterProperties.java         路由决策阈值配置
│   ├── SseThreadPoolConfig.java      SSE 异步线程池
│   └── ToolCallProperties.java       工具调用安全配置
├── entity/              MyBatis-Plus 实体
│   ├── AuthConfig.java               认证配置
│   ├── MetaChangeEvent.java          元数据变更记录
│   ├── MetaMetric.java               指标元数据
│   ├── ToolConfig.java               工具配置
│   ├── ToolParam.java                工具参数定义
│   └── ToolResponseField.java        响应字段映射
├── event/               元数据变更事件
│   └── MetadataChangeEvent.java
├── exception/           自定义异常
│   ├── MetadataException.java
│   └── StateConflictException.java
├── executor/            查询执行器（策略模式）
│   ├── QueryExecutor.java                   接口
│   ├── ToolCallExecutor.java                TOOL_CALL
│   ├── DatabaseDrivenApiTool.java           动态 API 工具调用
│   ├── SemanticSqlExecutor.java             语义 SQL 查询
│   ├── TextToSqlExecutor.java               SQL 沙箱
│   ├── SqlSandboxExecutor.java              SQL 安全执行器
│   └── FallbackQueryExecutor.java           知识库降级
├── listener/            事件监听
│   └── MetadataEventListener.java
├── mapper/              MyBatis-Plus Mapper
│   ├── AuthConfigMapper.java
│   ├── MetaChangeEventMapper.java
│   ├── MetaMetricMapper.java
│   ├── ToolConfigMapper.java
│   ├── ToolParamMapper.java
│   └── ToolResponseFieldMapper.java
├── model/               数据模型（不含 ORM 映射）
│   ├── AskDataRequest.java              同步查询请求
│   ├── ConversationSession.java         对话会话对象
│   ├── DialogueTurn.java                对话轮次记录
│   ├── FeedbackRequest.java             反馈请求
│   ├── HiAgentChatResponse.java         HiAgent 响应
│   ├── IntentResult.java                意图识别结果
│   ├── KnowledgeRetrieveRequest.java    知识库请求
│   ├── QueryResult.java                 查询结果
│   ├── ResponseSchema.java              响应 Schema
│   ├── ResultProcessingOutput.java      后处理输出
│   ├── RoutingDecision.java             路由决策
│   ├── SseMessage.java                  SSE 消息协议
│   ├── ToolCallAuditLog.java            工具调用审计日志
│   ├── ToolConfigWithParams.java        工具配置+参数
│   └── ToolContext.java                 工具执行上下文
├── router/              路由策略枚举
│   └── RoutingStrategy.java
├── security/            安全模块
│   ├── AuthHeaderInjector.java          认证 Header 注入
│   ├── HeaderContextHolder.java         请求头上下文
│   ├── RequestContextHelper.java        请求上下文捕获
│   ├── TokenContextHolder.java          Token 上下文
│   └── ToolCallLimiter.java             工具调用限流
├── service/             业务服务
│   ├── AuditLogger.java                审计日志服务
│   ├── AuthConfigService.java          认证配置服务
│   ├── CacheService.java               缓存管理服务
│   ├── ConfidenceCalibrator.java       置信度校准器
│   ├── ConversationContextManager.java  会话上下文管理
│   ├── HiAgentConversationManager.java HiAgent 会话管理
│   ├── HiAgentSchemaService.java       HiAgent Schema
│   ├── IntentParser.java               HiAgent 响应解析
│   ├── MetricService.java              指标元数据服务
│   ├── ResultPostProcessorService.java  结果后处理服务
│   ├── RouterService.java              路由决策引擎
│   ├── SemanticLayerService.java       语义层服务
│   ├── ToolConfigService.java          工具配置服务
│   └── UserPermissionService.java      用户权限服务
├── transformer/         参数转换器
│   ├── DateRangeMapper.java            时间范围映射
│   ├── ParamTransformer.java           参数转换接口
│   └── UniversalParameterMapper.java   通用参数映射器
└── validator/           参数校验器
    ├── MetadataValidator.java          元数据校验
    ├── StateValidator.java             会话状态校验
    └── UniversalParameterValidator.java 通用参数校验
```

### 5.2 前端结构

```
src/views/askdata/
├── ChatWindow.vue                     主聊天窗口
├── components/
│   ├── MessageBubble.vue              消息气泡
│   ├── ChartRenderer.vue              ECharts 图表渲染
│   ├── ClarifyDialog.vue              澄清对话框
│   └── FeedbackButton.vue             点赞/踩反馈按钮
src/api/
├── askdata.js                         API 封装
src/mixins/
├── useSSEChat.js                      SSE 聊天逻辑 Mixin
src/utils/
├── sse-client.js                      SSE 客户端工具类
├── traceId.js                         链路追踪 ID 管理
```

---

## 6. 核心类职责

### 6.1 RouterService（路由决策引擎）

包路径: `com.crm.askdata.service.RouterService`

核心编排类，负责整个查询流程的串联。方法调用链：

```
processStream(query, traceId, sessionId, userId, requestHeaders, emitter)
  └─ processInternal(query, traceId, sessionId, userId, startTime)
       ├─ recognizeIntent()           → 意图识别
       ├─ handleClarificationIfNeeded() → 澄清处理
       ├─ makeRoutingDecision()       → 路由决策
       ├─ transitionToExecuting()     → 状态转换
       ├─ executeQuery()              → 查询执行
       └─ resetSessionAfterCompletion() → 会话清理
```

关键设计点：
- **SSE/同步双模式**：`processStream`（异步 SSE）和 `process`（同步）共享 `processInternal` 核心逻辑
- **异步线程上下文透传**：MDC（日志链路）+ Token + 请求 Header 通过 `RequestContextHelper` 和 `MDC.getCopyOfContextMap()` 从 Tomcat 线程传到 `sseExecutor` 线程
- **会话状态机管理**：INITIAL → CLARIFYING → EXECUTING → COMPLETED/FAILED
- **重试机制**：executeQuery 中 decision.canRetry() → 自动降级 FALLBACK_KB

### 6.2 HiAgentClient（AI 平台客户端）

包路径: `com.crm.askdata.client.HiAgentClient`

HiAgent 平台 HTTP 客户端，通过 REST API 与 HiAgent 交互：
- `createConversation()` — 创建 AI 会话，返回 AppConversationID
- `chatQueryBlocking()` — 阻塞式聊天，一次性拿到完整 answer
- `feedback()` — 提交反馈（点赞/踩）

通过 Resilience4j 配置熔断器和重试，防止 AI 服务故障导致雪崩。

### 6.3 IntentParser（意图解析器）

包路径: `com.crm.askdata.service.IntentParser`

将 HiAgent blocking 模式返回的嵌套 JSON answer 解析为 `IntentResult`。核心挑战是 HiAgent 的 answer 可能包含 1~3 层 output 嵌套：
- 单层: `{"intent":"data_query",...}`
- 双层: `{"output":"{\"intent\":\"data_query\",...}"}`
- 三层: `{"output":"{\"output\":\"{\\\"intent\\\":...}\"}"}`

`normalizeAnswer()` 方法递归提取最多 3 层 output 字段，直至找到包含 `intent` 的 JSON。

### 6.4 DatabaseDrivenApiTool（动态工具执行器）

包路径: `com.crm.askdata.executor.DatabaseDrivenApiTool`

核心创新：将传统硬编码的工具调用改造为**数据库驱动的动态执行**。流程：
1. 从 `askdata_tool_config` 表读取工具配置（API URL、HTTP 方法、认证方式）
2. 从 `askdata_tool_param` 表读取参数定义（参数名、类型、是否必填、映射规则）
3. 通过 `UniversalParameterValidator` 校验参数完整性
4. 通过 `UniversalParameterMapper` 将实体参数映射为 API 请求参数
5. 通过 `AuthHeaderInjector` 按认证类型注入请求 Header（支持 SA_TOKEN/API_KEY/BASIC/OAUTH2_CLIENT）
6. 调用业务系统 API
7. 通过 `ResponseSchema` / `ToolResponseField` 进行字段中文映射和格式化

### 6.5 SqlSandboxExecutor（SQL 沙箱）

包路径: `com.crm.askdata.executor.SqlSandboxExecutor`

安全执行 AI 生成的 SQL，安全机制：
1. **危险关键字过滤**：禁止 INTO OUTFILE、LOAD_FILE、SLEEP 等
2. **JSqlParser AST 解析**：必须是 SELECT 语句
3. **表名白名单**：仅允许 `meta_metric.sourceTable` 已注册的表
4. **行数限制**：自动追加 LIMIT 1000

---

## 7. SSE 消息协议

前后端通过 Server-Sent Events 通信，消息格式：

| type | 含义 | 关键字段 |
|------|------|---------|
| text | 文本片段（流式追加） | content, confidence |
| chart | 图表数据 | chartType, data, chartOptions |
| clarify | 需要澄清 | clarifyQuestion, clarifyOptions |
| error | 错误信息 | message |
| end | 流式结束 | strategy, latencyMs, suggestions, hiagentMessageId |
| keepalive | 心跳（每 15s） | — |

消息序列（正常路径）：
```
text("正在识别意图...") → text("正在分析查询...") → text("正在执行 [策略]...") → text(逐字输出) → chart(数据) → end
```

消息序列（澄清路径）：
```
text("正在识别意图...") → text("...") → clarify(问题+选项) → end(type=CLARIFY)
```

---

## 8. 会话状态管理

### 8.1 会话状态机

```
    ┌──────────┐
    │  INITIAL  │  ← 新建/重置后的初始状态
    └────┬─────┘
         │ 意图识别 → needClarify=true
         ▼
    ┌──────────┐
    │CLARIFYING│  最大 5 轮，超时 300s
    └────┬─────┘
         │ 用户补充后重新请求 → execute
         ▼
    ┌──────────┐
    │ EXECUTING│  超时检测（300s）
    └────┬─────┘
         │
    ┌────▼────┐
    │COMPLETED│  ← 正常结束
    └─────────┘
    ┌───────┐
    │ FAILED │  ← 异常中断，下次请求自动重置
    └───────┘
    ┌────────┐
    │EXPIRED │  ← 元数据过期
    └────────┘
```

### 8.2 会话存储

- **HiAgentConversationManager**: Redis 缓存 `userId + agentId → AppConversationID`，TTL 2h
- **ConversationContextManager**: Redis 缓存 `sessionId → ConversationSession`，TTL 2h

Redis key 示例：
- `hiagent:conv:intent_agent:u001` → "conv_abc123"
- `askdata:session:sess_xyz` → `{status, currentTurnCount, lockedToolName, ...}`

### 8.3 防护机制

- **轮次限制**：CLARIFYING 状态最大 5 轮强制降级
- **超时检测**：CLARIFYING 300s / EXECUTING 300s 自动重置
- **状态机恢复**：COMPLETED/FAILED/EXPIRED 状态的会话在新请求时自动重置
- **意图切换检测**：CLARIFYING 期间 toolName 变化 → 自动清空会话重建

---

## 9. 安全机制

### 9.1 工具调用安全

- **权限校验** (`ToolCallExecutor`): 检查用户是否有所需工具和敏感工具的权限
- **调用限流** (`ToolCallLimiter`): 按工具和会话维度限制调用频率
- **熔断降级** (Resilience4j): 工具调用失败率 > 50% → 熔断 10s
- **审计日志** (`AuditLogger`): 每次工具调用记录完整审计追踪

### 9.2 SQL 安全

- **沙箱执行** (`SqlSandboxExecutor`): 危险关键字过滤 + AST 语法校验 + 表名白名单
- **行数限制**: 自动追加 LIMIT 1000

### 9.3 身份透传

- **AuthHeaderInjector**: 支持 SA_TOKEN / API_KEY / BASIC / OAUTH2_CLIENT 四种认证
- **forwardHeaders**: 透传用户身份（loginoa/loginname）到下游业务系统
- **Token 传递**: 异步线程通过 TokenContextHolder 传递 Sa-Token

---

## 10. 数据库表结构

| 表名 | 前缀 | 用途 |
|------|------|------|
| `askdata_tool_config` | 工具 | 工具配置定义（API URL、HTTP 方法、认证方式、响应映射） |
| `askdata_tool_param` | 工具参数 | 参数名称、类型、是否必填、默认值、映射规则 |
| `askdata_tool_response_field` | 响应字段 | 响应字段中文映射（旧方案，逐步迁移到 response_schema JSON） |
| `askdata_auth_config` | 认证配置 | 目标系统的认证方式与凭证 |
| `askdata_meta_metric` | 指标元数据 | 业务指标定义（表达式、来源表、数据类型、安全级别） |
| `askdata_meta_change_event` | 元数据变更 | 元数据变更审计记录 |

---

## 11. 配置项

### 11.1 application.yml 关键配置

```yaml
hiagent:
  base-url: http://hiagent.internal:8000
  create-conversation-api: /create_conversation
  chat-query-api: /chat_query_v2
  feedback-api: /feedback
  connect-timeout: 5s
  read-timeout: 60s
  intent-agent-id: intent_agent
  semantic-sql-agent-id: semantic_sql_agent
  result-agent-id: result_agent
  agents:
    intent_agent:
      api-key: ${HIAGENT_API_KEY_INTENT}
    semantic_sql_agent:
      api-key: ${HIAGENT_API_KEY_SQL}
    result_agent:
      api-key: ${HIAGENT_API_KEY_RESULT}

askdata:
  router:
    confidence-thresholds:
      high: 0.85
      medium: 0.60
      low: 0.40
    security:
      enable-sql-sandbox: true
      default-fallback: FALLBACK_KB
    budget:
      intent-recognition: 1000
      routing-decision: 200
      query-execution: 1500
      total: 3000
    retry:
      max-retries: 2
      reversible-strategies: TEXT_TO_SQL, SEMANTIC_QUERY
```

### 11.2 Resilience4j 熔断器

| 实例名 | 失败率阈值 | 熔断时长 | 半开请求数 | 滑动窗口 |
|--------|-----------|---------|-----------|---------|
| hiagent | 50% | 30s | 3 | 10 |
| toolCall | 50% | 10s | 3 | 10 |

---

## 12. 关键设计决策

### 12.1 为什么选择同步 blocking + 后端 SSE 而非 HiAgent 原生流式？

HiAgent 的 blocking 模式一次返回完整 answer，后端解析意图后再通过自身 SSE 推送结果。这使得：
- **Java 层掌握路由决策权**：可根据置信度、权限、限流等条件选择策略
- **统一前端协议**：无论 TOOL_CALL 还是 SEMANTIC_QUERY，前端只消费统一的 SSE 消息格式
- **审计与安全控制**：所有工具调用在 Java 层执行，可审计、可限流、可熔断

### 12.2 为什么引入 DatabaseDrivenApiTool？

将工具调用从硬编码改造为数据库驱动后：
- 新增业务指标查询只需在 askdata 表中插入配置，无需改代码
- 参数校验和映射统一由 `UniversalParameterValidator/Mapper` 处理
- 支持多种认证方式（SA_TOKEN/API_KEY/BASIC/OAUTH2_CLIENT）可配置切换

### 12.3 为什么使用三个独立的 HiAgent Agent？

- **intent_agent**: 负责意图识别 + 工具匹配 + 参数提取（多轮对话复用会话）
- **semantic_sql_agent**: 负责根据指标定义生成 SQL（每次调用独立会话）
- **result_agent**: 负责对查询结果进行后处理（展示优化、智能过滤）

职责分离使得每个 Agent 的 prompt 更聚焦，且不同 Agent 可独立调整和优化。

---

## 13. 调用关系与依赖图

```
AskDataController
  ├── HiAgentClient              ──→ HiAgent API
  ├── RouterService (核心)        ──→ 所有 Service + Executor
  │   ├── IntentParser            ──→ 解析 HiAgent 响应
  │   ├── ConfidenceCalibrator   ──→ 置信度校准
  │   ├── ConversationContextManager ──→ Redis 会话管理
  │   ├── HiAgentConversationManager ──→ Redis 会话管理
  │   ├── ToolCallExecutor        ──→ DatabaseDrivenApiTool
  │   │   ├── ToolConfigService   ──→ DB 工具配置
  │   │   ├── UniversalParamValidator
  │   │   ├── UniversalParamMapper
  │   │   ├── AuthHeaderInjector  ──→ 认证 Header
  │   │   └── ResultPostProcessor ──→ HiAgent result_agent
  │   ├── SemanticSqlExecutor     ──→ HiAgent sql_agent + SqlSandboxExecutor
  │   ├── TextToSqlExecutor       ──→ 查询计划描述
  │   ├── FallbackQueryExecutor   ──→ 降级文案
  │   └── AuditLogger             ──→ 审计记录
  └── conversationManager        ──→ 会话重置
```

---

## 14. 错误处理策略

| 错误场景 | 处理方式 | 用户表现 |
|---------|---------|---------|
| HiAgent 超时/不可用 | Resilience4j 熔断 → fallbackChatQuery | "智能服务暂时不可用，请稍后重试" |
| 工具配置不存在 | 抛出 IllegalArgumentException | 前端 error 类型 SSE |
| 工具调用抛异常 | executeQuery 重试 → decision.canRetry() → FALLBACK_KB | 降级提示 |
| 工具调用熔断 | CircuitBreaker fallbackExecute | "数据查询失败，请稍后重试" |
| SS 连接断开 | 前端重连 3 次（间隔 3s） | "正在重连 (n/3)" |
| 意图解析失败 | normalizeAnswer 多层兜底 → 最终抛出异常 | "智能体响应异常" |
| 会话超时 | getOrInitSession 自动重置 | 无感恢复 |

---

## 15. 性能与约束

| 指标 | 值 |
|------|-----|
| SSE 超时 | 5 分钟 |
| SSE 心跳间隔 | 15 秒 |
| HiAgent 读取超时 | 60 秒 |
| HiAgent 会话 TTL | 2 小时 |
| 后端会话 TTL | 2 小时 |
| 最大澄清轮次 | 5 轮 |
| 会话超时 | 300 秒 |
| SQL 最大行数 | 1000 行 |
| 后处理样本行数 | 50 行 |
| 总延迟预算 | 3000 ms |
| 后端 SSE 线程池 | 自定义（MDC 上下文传递） |

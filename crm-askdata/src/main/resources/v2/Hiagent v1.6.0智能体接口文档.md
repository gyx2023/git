# HiAgent v1.6.0 智能体接口文档

> 📄 API 文档 | 版本：v1.6.0

## 📋 调用说明（以对话类聊天为例）

### 三步调用流程

1. **获取凭证**  
   在智能体预览页右侧，获取 API 调用的 `URL` 和鉴权用的 `{{ApiKey}}`

2. **配置请求**  
   - 请求 Header 加上 `Apikey: {{ApiKey}}`
   - Body 参数中包括 `AppKey`，也赋值为 `{{ApiKey}}`（已废弃，可不传）
   - 所有接口需在 Body 中加上 `UserID`（用户标识，1-20字符，应用内唯一）

3. **开始对话**
   ```mermaid
   graph LR
   A[获取AppID] --> B[CreateConversation]
   B --> C[生成AppConversationID]
   C --> D[ChatQuery 流式聊天]
   D --> E[StopMessage/QueryAgain/Feedback]
   ```

---

## 🔌 API 列表概览

### ChatService 服务接口

| 接口名 | 功能说明 |
|--------|----------|
| `ChatQuery` | 对话类聊天（建议用V2） |
| `ChatQueryV2` | 对话类聊天 V2 ✅ 推荐 |
| `QueryAgain` / `QueryAgainV2` | 重新生成回复 |
| `GetAppConfigPreview` | 获取应用配置 |
| `GetConversationList` | 获取对话列表 |
| `GetConversationInputs` | 获取对话变量输入 |
| `CreateConversation` | 创建会话 |
| `UpdateConversation` | 更新会话 |
| `DeleteConversation` | 删除会话 |
| `StopMessage` | 停止响应 |
| `GetConversationMessages` | 获取会话历史消息列表 |
| `GetMessageInfo` | 获取消息详情 |
| `DeleteMessage` | 删除消息 |
| `Feedback` | 回答反馈评价（赞/踩） |
| `SetMessageAnswerUsed` | 设置默认回答（多组回答时） |
| `GetSuggestedQuestions` | 获取提问建议 |
| `RunAppWorkflow` | 测试运行工作流（异步） |
| `QueryRunAppProcess` | 查询工作流运行进展（异步） |
| `SyncRunAppWorkflow` | 测试运行工作流（同步） |
| `CallTriggerWebhook` | 触发事件触发器 |
| `ListOauth2Token` | 查询用户 OAuth2 登录信息 |
| `ClearMessage` | 清除消息 |

---

## 🚀 核心接口详解

### 1️⃣ 创建会话 `CreateConversation`

> 对话类聊天需先生成会话，获取 `AppConversationID`

**请求**  
`POST /create_conversation`

**Headers**
```http
Apikey: {{ApiKey}}
Content-Type: application/json
```

**Body 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `UserID` | string | ✅ | 用户ID（1-20字符，应用内唯一） |
| `Inputs` | map[string,string] | ❌ | 变量输入（根据应用编排设置） |
| `AppKey` | string | ❌ | 应用key（已废弃，可不传） |

**Python 示例**
```python
def create_conversation(user_id: str) -> str:
    """创建会话，返回 conversation_id"""
    url = base_url + "create_conversation"
    headers = {
        'Apikey': api_key,
        'Content-Type': 'application/json'
    }
    data = {
        "UserID": user_id,
        "Inputs": {"var": "variable"}  # 可选
    }
    
    response = requests.post(url, headers=headers, data=json.dumps(data))
    response.raise_for_status()
    result = response.json()
    
    return result["Conversation"]["AppConversationID"]
```

**响应示例**
```json
{
  "Conversation": {
    "AppConversationID": "ct4m7gmoc93a9ns2sngg",
    "ConversationName": "新的会话",
    "CreateTime": "",
    "LastChatTime": "",
    "EmptyConversation": false
  },
  "BaseResp": null
}
```

---

### 2️⃣ 对话类聊天 `ChatQueryV2` ✅ 推荐

> 建议使用 V2 接口，返回结构更规范

**请求**  
`POST /chat_query_v2`

**Headers**
```http
Apikey: {{ApiKey}}
Content-Type: application/json
Accept: text/event-stream  # 流式模式时必需
```

**Body 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `UserID` | string | ✅ | 用户ID |
| `AppConversationID` | string | ✅ | 会话ID（来自 CreateConversation） |
| `Query` | string | ✅ | 用户提问内容 |
| `ResponseMode` | string | ❌ | `streaming`（默认）或 `blocking` |
| `QueryExtends` | object | ❌ | 扩展信息（如文件上传） |
| `AppKey` | string | ❌ | 已废弃 |

**QueryExtends.Files 示例**
```json
{
  "Files": [
    {
      "Name": "图片.jpeg",
      "Path": "upload/full/59/e8/0304b770...",
      "Size": 47525,
      "Url": "{{apiurl}}/api/proxy/down?Action=Download&..."
    }
  ]
}
```
---

## 🔁 流式响应事件类型详解（SSE）

`ChatQueryV2` 等接口支持 `streaming` 模式，返回标准 SSE（Server-Sent Events）格式。客户端需解析 `data` 字段中的 JSON 对象。

### 📡 事件类型枚举（`event` 字段）

| 事件类型 | 说明 |
|----------|------|
| `message_start` | 消息开始，携带全局 `task_id` |
| `message` | 消息内容片段，含 `answer` 字段 |
| `message_end` | 消息结束，含完整 `id`/`answer` |
| `message_output_start` | 开始输出（用于多阶段响应） |
| `message_output_end` | 输出结束 |
| `message_cost` | 消息消耗统计（token/耗时） |
| `suggestion` | 返回提问建议列表 |
| `suggestion_cost` | 建议生成消耗 |
| `agent_thought` / `agent_thought_end` | Agent 思考过程（开启推理时） |
| `message_replace` | 消息替换（用于修正/更新） |
| `message_failed` | 消息生成失败 |
| `knowledge_retrieve` / `_end` | 知识库检索开始/结束 |
| `qa_retrieve` / `_end` | 问答库检索开始/结束 |
| `knowledge_graph_retrieve` / `_end` | 图谱库检索开始/结束 |
| `terminology_retrieve` / `_end` | 术语库检索开始/结束 |
| `tool_message` | 工具调用消息节点 |
| `think_message_output_start` / `message` / `end` | 推理思考内容输出 |
| `agent_jump` | Agent 跳转事件 |
| `agent_take_over` | Agent 接管最终回答 |
| `intention` | 用户意图识别结果 |

### 📦 流式响应数据结构示例

```json
{
  "event": "message",
  "task_id": "6b2f4720-a14e-413d-9783-024442a58e7d",
  "id": "01JDHDFNH1XMJG89SEP2F49HDV",
  "answer": "你",
  "created_at": 1732530134,
  "conversation_id": "01JDHDFNGYRB130PZSEDE8R70B"
}
```

> 🔍 **字段说明**：
> - `task_id`：等效于当前对话的全局消息 ID，用于 `StopMessage` 等接口
> - `id`：消息 ID（全局或子消息），作为 `Feedback`/`DeleteMessage` 等接口的入参
> - `conversation_id`：底层模型会话 ID（与 `AppConversationID` 业务含义不同）

---

## 🔄 重新生成回复接口

### QueryAgain（旧版）

**请求**  
`POST /query_again`

**Body 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `AppConversationID` | string | ✅ | 会话 ID |
| `MessageID` | string | ✅ | 上轮返回的消息 ID |
| `UserID` | string | ✅ | 用户 ID |

> ⚠️ 仅支持流式返回（SSE），结构嵌套 `data: { ... }`

### QueryAgainV2 ✅ 推荐

**请求**  
`POST /query_again_v2`

**优势**：
- 流式返回为标准 SSE，无嵌套 `data` 字段
- 非流式返回为完整 JSON（历史接口为兼容遗留设计）

**Python 示例**
```python
def query_again_v2(user_id: str, app_conversation_id: str, message_id: str):
    url = base_url + "query_again_v2"
    headers = {
        'Apikey': api_key,
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'  # 流式必需
    }
    data = {
        "AppConversationID": app_conversation_id,
        "MessageID": message_id,
        "UserID": user_id
    }
    response = requests.post(url, headers=headers, data=json.dumps(data), stream=True)
    return response
```

---

## 📜 获取历史消息 & 消息详情

### GetConversationMessages

**请求**  
`POST /get_conversation_messages`

**Body 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `AppConversationID` | string | ✅ | 会话 ID |
| `Limit` | int32 | ✅ | 返回消息条数上限 |
| `UserID` | string | ✅ | 用户 ID |

**响应示例**
```json
{
  "Messages": [
    {
      "ConversationID": "01JENGF8YA4NWT8AHP0WD9PTT6",
      "QueryID": "ctbcla1pgge5cea2drig",
      "Query": "北京天气",
      "AnswerInfo": {
        "Answer": "您想知道最近北京的天气情况吗？...",
        "MessageID": "01JENGF8YABMJBFG5YV1RB1J27",
        "CreatedTime": 1733741225,
        "TaskID": "01JENGF8YABMJBFG5YV1RB1J27",
        "Like": 0,
        "TotalTokens": 125,
        "Latency": 9.897,
        "TracingJsonStr": "",
        "RetrieverResource": false
      },
      "OtherAnswers": [],
      "Inputs": {}
    }
  ],
  "BaseResp": null
}
```

### GetMessageInfo（含知识库引用提取）

**请求**  
`POST /get_message_info`

**关键用途**：获取单条消息详情，特别是 `TracingJsonStr` 中的知识库检索溯源信息。

**🔍 引用归属提取代码示例**（特别鸣谢 @黄冬）
```python
def get_message_info(self, message_id):
    """根据消息 ID 获取消息详情，提取知识库引用"""
    url = base_url + "/api/proxy/api/v1/get_message_info"
    headers = {"Apikey": self.api_key, "Content-Type": "application/json"}
    data = {"MessageID": message_id, "UserID": self.user_id}
    
    response = requests.post(url, headers=headers, json=data)
    response.raise_for_status()
    result = response.json()
    
    tracing_str = result['MessageInfo']['AnswerInfo']['TracingJsonStr']
    if not tracing_str:
        return []
    
    tracing_list = json.loads(tracing_str)
    references = []
    for item in tracing_list:
        if item.get("event") == "knowledge_retrieve_end":
            for doc in item.get("docs", {}).get("outputList", []):
                meta = doc.get("metadata", {})
                references.append({
                    "document_name": meta.get("document_name"),
                    "obs_url": f"https://{self.host}/api/proxy/down?Action=Download&Version=2022-01-01&Path={meta.get('document_obs_url')}&IsAnonymous=true"
                })
    return references
```

---

## 👍 反馈 & 建议 & 消息管理

### Feedback（点赞/点踩）

**请求**  
`POST /feedback`

**Body 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `MessageID` | string | ✅ | 消息 ID |
| `LikeType` | enum | ✅ | `-1`=踩，`0`=默认，`1`=赞 |
| `UserID` | string | ✅ | 用户 ID |

### GetSuggestedQuestions（获取提问建议）

**请求**  
`POST /get_suggested_questions`

**响应示例**
```json
{
  "SuggestedQuestions": [
    "明天天气如何？",
    "未来一周天气？",
    "现在温度多少？"
  ],
  "BaseResp": null
}
```

### 消息管理接口速览

| 接口 | 功能 | 关键参数 |
|------|------|----------|
| `DeleteMessage` | 删除单条消息 | `MessageID` 或 `QueryID`（二选一） |
| `SetMessageAnswerUsed` | 多答案时设默认 | `MessageID` |
| `StopMessage` | 停止流式输出 | `TaskID`（来自 `message_start`） |
| `ClearMessage` | 清空会话全部消息 | `AppConversationID` |

---

## ⚙️ 工作流测试接口（流程编排型应用）

### RunAppWorkflow（异步执行）

**请求**  
`POST /run_app_workflow`

**Body 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `InputData` | string | ✅ | JSON 字符串，如 `{"content":"我想上天"}` |
| `UserID` | string | ✅ | 用户 ID |

**响应**
```json
{ "runId": "c4213b25cd614424ade0009ceb445f36" }
```

### QueryRunAppProcess（轮询进展）

**请求**  
`POST /query_run_app_process`

**Body 参数**：`AppID`, `RunID`, `UserID`

**🔁 轮询示例代码**（指数退避策略）
```python
def get_process(run_id, max_retries=10):
    url = base_url + "query_run_app_process"
    headers = {"Content-Type": "application/json", "Apikey": API_KEY}
    data = {"AppID": APP_ID, "UserID": USER_ID, "RunID": run_id}
    
    for retry in range(max_retries):
        result = requests.post(url, headers=headers, json=data).json()
        if result.get('status') == 'success':
            return result
        time.sleep(min(2 ** retry, 30))  # 指数退避，最大30秒
    return None
```

### SyncRunAppWorkflow（同步执行）

> 适合短时任务，直接返回完整执行结果（含各节点 `input`/`output`/`costMs`/`nodeType`）

---

## 📦 核心结构体定义参考

### app.AppConversationBrief
```json
{
  "AppConversationID": "string",   // 会话 ID
  "ConversationName": "string",    // 会话名称（可修改）
  "CreateTime": "string",          // 创建时间
  "LastChatTime": "string",        // 最后聊天时间
  "EmptyConversation": false       // 是否为空会话
}
```

### app.MessageAnswerInfo
| 字段 | 类型 | 说明 |
|------|------|------|
| `Answer` | string | 回复内容 |
| `MessageID` | string | 消息 ID（用于反馈/删除） |
| `CreatedTime` | int32 | Unix 时间戳 |
| `TaskID` | string | 任务 ID（用于停止响应） |
| `Like` | enum | -1/0/1 |
| `TotalTokens` | int32 | 消耗 token 数 |
| `Latency` | double | 耗时（秒） |
| `TracingJsonStr` | string | 调试/溯源 JSON（含知识库检索详情） |
| `RetrieverResource` | bool | 是否启用知识库检索 |

### app.QueryExtendsInfo（文件上传支持）
```json
{
  "Files": [
    {
      "Name": "图片.jpeg",
      "Path": "upload/full/59/e8/0304b770...",
      "Size": 47525,
      "Url": "{{apiurl}}/api/proxy/down?Action=Download&..."
    }
  ]
}
```
> 📌 文件需先上传至 OBS，参考文档：*HiAgent Up 服务接口调用说明*

### common.VariableType（变量类型枚举）
| 值 | 说明 | 附加要求 |
|----|------|----------|
| `Text` | 单行文本 | `TextMaxLength` 必填 |
| `Paragraph` | 段落文本 | - |
| `Enum` | 枚举 | `EnumValues`（string 数组）必填 |

---

## 🔐 鉴权与安全最佳实践

### 1️⃣ API Key 管理

```python
# ✅ 推荐：通过环境变量注入
import os
API_KEY = os.getenv("HIAGENT_API_KEY")

# ❌ 避免：硬编码在代码中
# API_KEY = "co6dd4i1hp0kieia137g"  # 高危！
```

### 2️⃣ 请求签名建议（增强安全）

虽然 HiAgent 目前仅依赖 `Apikey` Header 鉴权，但生产环境建议增加：

| 措施 | 说明 |
|------|------|
| **请求时间戳校验** | 服务端拒绝 `timestamp > 5min` 的请求，防重放 |
| **请求体哈希签名** | `sign = HMAC_SHA256(Apikey, body+timestamp)` |
| **IP 白名单** | 在 HiAgent 控制台配置允许调用的服务器 IP |
| **UserID 权限隔离** | 确保 `UserID` 与业务系统用户绑定，防越权 |

### 3️⃣ 敏感信息脱敏

```python
# 日志脱敏示例
def safe_log(data: dict, sensitive_keys: list = ["Apikey", "UserID"]):
    safe_data = data.copy()
    for key in sensitive_keys:
        if key in safe_data:
            safe_data[key] = "***"
    return safe_data
```

---

## 🧪 错误码大全 & 重试策略

### 常见错误码速查

| HTTP Code | 业务 Code | 说明 | 建议处理 |
|-----------|-----------|------|----------|
| `200` | `0` | 成功 | - |
| `400` | `1001` | 参数校验失败 | 检查必填字段/格式 |
| `401` | `1002` | Apikey 无效/过期 | 刷新凭证/联系管理员 |
| `403` | `1003` | 权限不足 | 检查 UserID 是否绑定应用 |
| `404` | `1004` | 资源不存在 | 确认 AppConversationID/MessageID |
| `429` | `1005` | 请求频率超限 | 指数退避重试 |
| `500` | `2001` | 服务端内部错误 | 记录日志 + 重试（最多3次） |
| `503` | `2002` | 服务不可用 | 等待后重试 + 告警 |

### 🔄 智能重试策略（指数退避 + 抖动）

```python
import time, random
from requests.exceptions import RequestException

def retry_request(func, max_retries=3, base_delay=1.0):
    """通用重试装饰器"""
    for attempt in range(max_retries):
        try:
            return func()
        except RequestException as e:
            if attempt == max_retries - 1:
                raise
            # 指数退避 + 随机抖动（防雪崩）
            delay = base_delay * (2 ** attempt) + random.uniform(0, 0.5)
            time.sleep(delay)
    return None

# 使用示例
response = retry_request(
    lambda: requests.post(url, headers=headers, json=data, timeout=30)
)
```

> 📌 **重试原则**：
> - 仅对 `5xx` / `429` / 网络超时 进行重试
> - `4xx` 客户端错误 **不要重试**（避免无效请求）
> - 单次请求设置 `timeout=30s`，避免长连接阻塞

---

## 🧩 高级用法

### 🔁 多轮对话上下文管理

```python
# 会话状态缓存示例（Redis）
import redis
r = redis.Redis(host='localhost', port=6379, db=0)

def get_conversation_context(user_id: str, app_id: str) -> dict:
    key = f"hiagent:ctx:{app_id}:{user_id}"
    ctx = r.get(key)
    return json.loads(ctx) if ctx else {}

def save_conversation_context(user_id: str, app_id: str, context: dict, ttl=3600):
    key = f"hiagent:ctx:{app_id}:{user_id}"
    r.setex(key, ttl, json.dumps(context))
```

> 💡 **最佳实践**：
> - `AppConversationID` 是业务会话标识，建议与业务系统 `session_id` 映射存储
> - 长时间无交互的会话（>1h）建议自动清理或归档

### 🎯 变量注入（动态参数传递）

应用编排时定义的变量，可通过 `Inputs` 在创建会话或更新会话时注入：

```python
# 创建带变量的会话
def create_session_with_vars(user_id: str, vars: dict):
    return create_conversation(
        user_id=user_id,
        inputs={
            "user_level": vars.get("level", "normal"),  # 用户等级
            "region": vars.get("region", "CN"),          # 区域
            "preferences": json.dumps(vars.get("prefs", []))  # JSON 字符串
        }
    )
```

> ⚠️ 注意：
> - `Inputs` 值为 `string` 类型，复杂结构需 `JSON.stringify`
> - 变量名需与应用编排界面 **完全一致**（区分大小写）

### 🛠️ 工具调用集成（Function Calling）

当智能体配置了工具节点，流式响应中会出现 `tool_message` 事件：

```json
{
  "event": "tool_message",
  "tool_name": "weather_query",
  "tool_input": {"city": "北京"},
  "tool_output": {"temp": 25, "condition": "晴"},
  "status": "success"
}
```

**前端处理建议**：
```javascript
// SSE 事件监听示例
eventSource.addEventListener('tool_message', (e) => {
  const data = JSON.parse(e.data);
  if (data.tool_name === 'weather_query') {
    // 渲染天气卡片
    renderWeatherCard(data.tool_output);
  }
});
```

---

## 📊 监控建议：可观测性建设

### 🔍 关键指标埋点

| 指标 | 采集点 | 用途 |
|------|--------|------|
| `api_request_total` | 每次 HTTP 请求 | QPS 监控/限流 |
| `api_latency_seconds` | 请求开始→响应结束 | 性能瓶颈分析 |
| `token_usage_total` | `TotalTokens` 字段 | 成本核算/配额管理 |
| `conversation_duration` | Create→LastChat | 用户粘性分析 |
| `feedback_ratio` | `LikeType` 统计 | 回答质量评估 |

### 📈 示例：Prometheus 埋点（Python）

```python
from prometheus_client import Counter, Histogram

# 定义指标
REQUEST_COUNT = Counter('hiagent_request_total', 'API 请求总数', ['endpoint', 'status'])
REQUEST_LATENCY = Histogram('hiagent_request_latency_seconds', '请求耗时', ['endpoint'])
TOKEN_USAGE = Counter('hiagent_tokens_total', 'Token 消耗', ['user_type'])

# 埋点示例
@REQUEST_LATENCY.labels(endpoint='chat_query_v2').time()
def chat_with_monitoring(user_id, query):
    resp = chat_query_v2(user_id, conv_id, query)
    tokens = extract_tokens(resp)  # 从响应解析
    TOKEN_USAGE.labels(user_type=get_user_tier(user_id)).inc(tokens)
    REQUEST_COUNT.labels(endpoint='chat_query_v2', status='200').inc()
    return resp
```

### 🚨 告警规则建议（Alertmanager）

```yaml
# 高延迟告警
- alert: HiAgentHighLatency
  expr: histogram_quantile(0.95, rate(hiagent_request_latency_seconds_bucket[5m])) > 10
  for: 2m
  labels: { severity: warning }
  annotations: { summary: "95分位请求耗时 > 10s" }

# Token 消耗突增
- alert: HiAgentTokenSpike
  expr: rate(hiagent_tokens_total[1h]) > 10000
  for: 5m
  labels: { severity: critical }
  annotations: { summary: "小时级 Token 消耗突增" }
```

---

## 📦 附录：完整调用链路示例（Python 类封装）

```python
class HiAgentClient:
    def __init__(self, api_key: str, base_url: str):
        self.api_key = api_key
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({
            'Apikey': api_key,
            'Content-Type': 'application/json'
        })

    def create_conversation(self, user_id: str, inputs: dict = None) -> str:
        """创建会话，返回 AppConversationID"""
        resp = self.session.post(
            f"{self.base_url}/create_conversation",
            json={"UserID": user_id, "Inputs": inputs or {}}
        )
        resp.raise_for_status()
        return resp.json()["Conversation"]["AppConversationID"]

    def chat_stream(self, user_id: str, conv_id: str, query: str, 
                   on_token: callable = None, on_complete: callable = None):
        """流式聊天，支持回调处理"""
        resp = self.session.post(
            f"{self.base_url}/chat_query_v2",
            json={
                "UserID": user_id,
                "AppConversationID": conv_id,
                "Query": query,
                "ResponseMode": "streaming"
            },
            headers={'Accept': 'text/event-stream'},
            stream=True
        )
        
        full_answer = []
        for line in resp.iter_lines():
            if not line or not line.startswith(b'data:'):
                continue
            data = json.loads(line[6:].decode())
            event = data.get('event')
            
            if event == 'message' and 'answer' in data:
                chunk = data['answer']
                full_answer.append(chunk)
                if on_token:
                    on_token(chunk)  # 实时回调
            elif event == 'message_end' and on_complete:
                on_complete(''.join(full_answer), data)  # 完成回调
                break
        return ''.join(full_answer)

    def feedback(self, message_id: str, like: bool, user_id: str):
        """提交反馈"""
        self.session.post(
            f"{self.base_url}/feedback",
            json={
                "MessageID": message_id,
                "LikeType": 1 if like else -1,
                "UserID": user_id
            }
        ).raise_for_status()
```

---

## ✅ 文档使用清单（开发前自查）

- [ ] 已从智能体预览页获取 `Apikey` 和 `URL`
- [ ] `UserID` 生成规则已确定（应用内唯一，1-20字符）
- [ ] 流式接口已设置 `Accept: text/event-stream`
- [ ] 文件上传已对接 OBS（参考 *HiAgent Up 服务接口文档*）
- [ ] 错误处理已覆盖 `429`/`5xx` 重试逻辑
- [ ] 敏感字段（Apikey/UserID）已脱敏日志
- [ ] 关键指标（延迟/Token/反馈）已埋点

---

# Agent 目录 AGENTS.md

本目录只实现 AI 编排、提示词、记忆、检索和受控 Tool；根目录及上级业务 AGENTS.md 的规则优先。当前 Agent 包只有目录预留，尚未形成稳定 API；新增类前必须检查实际 Spring AI Alibaba 版本和已有实现。Agent 不拥有数据库事实、权限或交易最终决定权。

## Agent 目录约定

`agent` 内按职责拆分为 `memory`（Redis 脱敏摘要）、`model`（模型适配与工厂）、`prompts`（版本化提示词）、`skills/<skill-name>`（专用 Agent 及其必需的 `SKILL.md`）、`tools`（受控 Tool）、`rag`（Modular RAG）、`service`（应用编排/审计）、`dto`（HTTP Request 与内部上下文分离）、`vo`（API View）和 `common/utils`（无状态工具）。Tool 输入输出对象只属于 Agent 内部契约，不得复用 HTTP 层的 `dto/request` 或 `vo`；跨层交互通过明确的 Application Service 接口完成。配置属性和客户端 Bean 放在当前包 `config`，Redis Key/TTL 放在 `common/constants`，通用脱敏、哈希工具放在 `agent/common/utils` 并统一命名 `*Util.java`，禁止在 Agent 中复制常量或创建万能工具类。

### 目录与命名约定

```text
agent/
  common/utils/*Util.java       # 纯函数/无状态脱敏、哈希等工具
  config/*Properties.java       # @ConfigurationProperties
  controller/*Controller.java   # HTTP 适配
  dto/request/*Request.java     # HTTP 输入
  dto/internal/*                # AgentContext、AgentResult 等内部契约
  memory/*Memory*.java          # Redis 摘要记忆及其策略
  model/*Model*.java            # Chat/Embedding 客户端与 AgentModelFactory
  prompts/AgentPrompts.java     # 版本化 Prompt 常量
  rag/*                         # 检索、过滤、融合、上下文组装；不放入 service
  skills/<name>/*Agent.java     # 一个 Skill 一个子目录
  skills/<name>/SKILL.md        # Skill 必需发现文档
  tools/*Tool.java              # 单一职责、受控、可审计 Tool
  service/*Service.java         # 仅总控应用编排和 AI 审计
  vo/*View.java                 # API 输出 View
```

任何名为 Tool 的类必须使用 `*Tool.java` 后缀，不能把 Tool 实现混在 Agent 类中；任何通用工具必须使用 `*Util.java` 后缀。`SKILL.md` 至少说明 name、description、目标、上下文边界、允许 Tool、输出契约、确认规则和禁止事项。新 Skill 未提供 `SKILL.md` 时视为目录不完整，不得合并。

当前构建基线为 Spring Boot `3.5.16`、Spring AI `1.1.2` 与 Spring AI Alibaba `1.1.2.2`。Agent 模型调用通过本项目 `agent/model` 的 OpenAI 兼容 REST 客户端实现，避免业务代码直接耦合供应商 SDK；升级 BOM 或 Spring Boot 前必须重新验证自动配置兼容性，禁止伪造类或吞掉测试失败。

当前 `spring.ai.vectorstore.type=none` 同样是显式兼容配置：知识检索使用项目自己的 `MilvusVectorSearchClient` 调用 REST v2，不创建 Spring AI 1.0.0 的 `VectorStore` Bean。未来切换回官方 Milvus 自动配置前，必须同时验证 Boot 4 兼容版本、认证为空时的行为以及可用的 `EmbeddingModel` Bean。

## 当前范围与明确暂不考虑

当前只实现检索推荐、对话式办理/表单补全、知识交谈三类能力。评价、投诉、售后、优惠券/活动、工作人员排班与线上交流、自动派单、复杂限流/分布式锁、RAG 缓存、系统/文件日志平台、多租户计费、分账和退款自动化暂不考虑，不得为其创建 Tool、Prompt 或持久化结构。

## 总体架构：单向总控与专用智能体

Agent 采用简单单向流程，不实现多 Agent 互相循环或自由规划：

```text
用户消息 → 总控 Agent（意图识别与路由） → 一个专用 Agent
  → 固定业务模块 / Modular RAG / 受控 Tool → 固定页面组件标识
```

总控只允许一次分发，专用 Agent 完成任务后直接返回；禁止 Agent A 再分发 Agent B、无限 Thought→Tool 循环或把模型输出当作权限。路由结果必须是受限枚举，无法判断时转人工或普通页面。组件由后端返回稳定的 `componentType`、`schemaVersion` 和已定义字段，前端只渲染已有白名单组件，模型不得生成 HTML、JavaScript 或组件代码。

### 检索推荐

用户可以用模糊自然语言描述服务类型、区域、时间、预算和偏好，不强制先完成表单。总控识别为推荐后只分发给检索推荐 Agent，该 Agent 执行查询重写、结构化服务查询和 Modular RAG 检索。价格、服务状态、商家和预约事实优先查结构化业务数据；RAG 用于补充服务说明和排序解释。默认最多返回 5 条候选，每条带来源、时间或数据版本；没有足够结果要说明缺失条件，禁止编造。

### 对话办理与表单补全

对话页用于帮助用户找到入口并完成预约、地址、联系人、入驻资料和订单确认。Agent 可从会话历史提出非核心字段草稿，但核心身份、地址、联系方式、金额、支付、取消和其他敏感字段必须由用户明确填写或确认。交互顺序固定为：

```text
意图识别 → 调用可用 Tool → 生成页面内组件表单 → 用户填写并提交
→ 使用与普通表单相同的校验/异常处理 → 查询结果核验 → 展示完成信息
```

Tool 执行成功不等于业务完成；必须再次查询真实结果，例如预约创建后返回预约单详情，订单确认后返回订单号、金额和状态。失败时展示后端错误，不得由模型猜测成功。

### 知识交谈

面向服务说明、流程、政策、标准和家政知识。总控识别为知识问题后只分发给知识问答 Agent，该 Agent 只查询 RAG，不调用订单、用户或写入 Tool。先做租户、类别、版本、生效时间过滤，再进行查询重写、向量/关键词混合检索、去重和重排。回答引用来源；没有可靠上下文时明确回答不知道并引导人工渠道。

## Tool 设计

Tool 按单一职责拆分，例如 `searchServices`、`queryAppointment`、`createAppointmentDraft`、`submitAppointment`、`queryOrder`、`confirmOrder`、`queryKnowledge`。禁止万能 Tool、任意 SQL/HTTP/文件系统/Redis/系统命令。每个 Tool 需要版本、输入/输出 Schema、权限等级、超时、幂等策略、审计字段和失败码。

权限分为 `READ`、`WRITE`、`SENSITIVE_WRITE`。取消、支付、账户修改、入驻审批等敏感操作先返回待确认摘要，获得用户确认后再次校验用户、资源归属、状态、金额和幂等键。模型传入的 userId、role、金额、状态和资源 ID 都是不可信参数，必须由后端上下文覆盖或重新查询。

## Modular RAG、提示词、记忆和上下文

Prompt 集中放在 `agent/prompts`，必须版本化并记录修改原因。RAG 模块固定为：解析 → 清洗 → 分块 → 元数据 → Embedding → Milvus → 过滤 → 查询重写 → 向量/关键词检索 → 融合/重排 → 上下文组装 → 生成引用；各模块独立测试，不把流程塞进一个方法。开发环境推荐免费本地 Ollama `bge-m3`，默认名称仅是可替换配置，不是生产模型锁定；Chat 与 Embedding 必须允许使用不同端点和 Key。

会话记忆只采用短期摘要缓存：按固定规则生成脱敏摘要，写入 Redis，TTL 暂定 1 天；不建立长期记忆数据库表，不把完整消息无限累积到 Prompt。摘要 Key 必须包含用户/会话边界和版本，过期后允许重新询问，不能把缓存当业务事实。MySQL 不保存会话正文，只写入最小化 `ai_request_log` 元数据；Tool 日志只为写操作和敏感操作提供审计，payload 使用哈希/大小而非原文；每日用量写入 `ai_usage_daily` 聚合表。明细日志必须设置过期时间并由分批清理任务删除。不得把密码、Token、Cookie、证件号、银行卡、COS 密钥或完整隐私写入记忆或日志。每次模型调用设置最大迭代、Tool 次数、Token、响应大小和 timeout，检测循环和重复提交。

## RAG 与安全

知识文档是不可信输入，文档内容不能改变 System Prompt、权限、状态机或 Tool Schema。Chunk 必须携带 document_id、chunk_id、source、title、tenant_id、category、version 和生效时间；新版本生效后旧版本默认不可检索。对 Prompt Injection、越权查询、RAG Poisoning、Tool 参数伪造和敏感信息回显进行测试。

### RAG 数据导入、状态与重建

知识库管理属于受保护的后台能力，不由普通知识 Agent 自主写入。`POST /api/admin/rag/documents` 接收管理员提供的标题、来源、分类和正文，服务完成校验、清洗、分块、Embedding 和 Milvus 写入；`POST /api/admin/rag/documents/seed` 导入 `resources/knowledge/home_service_faq.md`；`GET` 接口查看目录及索引状态；`DELETE /api/admin/rag/documents/{id}` 软删除并清理向量；索引失败或 Milvus 数据丢失时使用 `POST /api/admin/rag/documents/{id}/reindex` 从 MySQL 当前版本重建。写库事务不得包住 Embedding、Milvus 或其他外部 HTTP，失败必须记录 `FAILED` 状态并允许有限重试。

MySQL 的 `rag_document`、`rag_document_version`、`rag_document_chunk` 是唯一事实来源，分别记录文档目录、版本/有效期和分块正文/索引状态。状态含义固定为：文档 `ACTIVE/DELETED`，版本 `INDEXING/ACTIVE/RETIRED/FAILED`，分块 `PENDING/INDEXED/FAILED/DELETED`。Milvus 只保存 FloatVector 和可过滤元数据，不承载业务状态；collection 必须包含 `document_id`、`chunk_id`、`source`、`title`、`content`、`tenant_id`、`category`、`version`、`status`、`effective_from`、`effective_to`、`vector`，其中 vector 维度必须与 Embedding 模型一致。检索前按 ACTIVE、租户、类别和生效时间过滤，回答引用来源；删除先提交 MySQL，再按 document_id 删除 Milvus，向量删除失败也不能让已删除文档重新出现在检索结果中。

## AI 日志与测试

当前只建设 AI 相关日志，不新增系统日志/文件日志平台。日志分为三类：`ai_request_log` 保存失败、敏感业务和按比例采样请求的路由、模型、Token、延迟、检索数量和状态；`ai_tool_call_log` 对写入/敏感 Tool 全量审计，普通 READ 只记录失败或采样，payload 只保存哈希/大小；`ai_usage_daily` 保存按日聚合的用量。三者均不保存完整 Prompt、用户原文、会话正文或密钥。请求明细默认保留 30 天，Tool 审计默认保留 90 天；到期由分批、限速清理任务删除，保留周期可按合规要求调整。

大量会话不应逐条写入 MySQL：会话正文是交互临时数据，不是业务事实，逐消息持久化会带来高写入、备份膨胀和隐私风险。前端负责展示当前会话，Redis 只提供一天内的脱敏摘要；需要审计时使用请求元数据、Tool 审计和业务表自身的订单/预约状态历史。若未来有合规要求必须保存原文，应先单独设计加密、访问审批、分区/归档和生命周期，不能直接恢复逐消息落库。

高频成功的 READ 请求不必全部写明细：每次请求只累加进程内低基数指标，再按周期/阈值批量把增量 UPSERT 到 `ai_usage_daily`；`ai_request_log` 通过 `detail_level=ERROR/FULL/SAMPLED` 控制留存量。错误、超时、越权、敏感写入使用 FULL；普通成功 READ 默认按采样率记录；Tool 审计使用独立 `tool_call_id`，不能用一次 AI 请求的 `request_id` 作为唯一键，因为一个请求可能调用多个 Tool。

### 记忆与日志落库决策（必须遵守）

“短期记忆”仅表示 Redis 中一天有效的脱敏摘要，不表示把消息保存一天后再写入 MySQL。前端保留当前页面的消息展示，后端每轮只更新同一摘要 Key；摘要过期后允许重新询问。MySQL 明细按以下规则控制规模：

| 场景 | 明细写入 | 聚合 | 默认 TTL |
| --- | --- | --- | --- |
| 成功 READ | 按 `AI_READ_SAMPLE_RATE` 采样（默认 1%） | 内存累加、定时批量增量 UPSERT | 30 天（采样明细） |
| 失败、超时、越权 | `ai_request_log` 全量 | 内存累加、定时批量增量 UPSERT | 30 天 |
| WRITE/SENSITIVE_WRITE | `ai_request_log` 与 `ai_tool_call_log` 全量 | 内存累加、定时批量增量 UPSERT | Tool 90 天 |

只允许保存路由、模型、Token、延迟、状态、错误码、组件类型及 payload 哈希/大小；禁止 Prompt、响应、用户原文和凭据。清理任务使用 `expire_time` 分批删除。数据库初始化脚本会移除历史 `ai_conversation`/`ai_message_log` 表；生产迁移必须先备份并审批，具体 SQL 以根目录 `AGENTS.md` 的“数据库变更 SQL”章节和 `src/main/resources/db/home_service.sql` 为准。

对应配置中 `app.ai.persistence.store-conversation-content`、`store-tool-payload` 固定为 `false`，不得通过环境变量开启；采样比例使用 `app.ai.persistence.read-sample-rate`（默认 1%）。

`ai_usage_daily` 的批量刷写默认每 5 分钟执行；刷写语句必须以本批 delta 做 `field = field + delta`，成功提交后再清除本地 delta，失败保留到有限次数重试。指标不是业务事实，进程崩溃时允许丢失未刷新的少量统计，禁止因此把每个请求同步写库或把指标刷写放进订单事务。

测试关注行为而非固定文案：模糊推荐最多 5 条、查询重写、结构化事实优先、字段缺失追问、核心字段确认、Tool 权限拒绝、重复请求、订单结果核验、无知识拒答、过期文档过滤、Prompt Injection 和模型超时降级。

## 当前实现类与配置

当前实现由 `agent/service/AgentApplicationService` 统一入口、`agent/controller/AgentController` 暴露 `POST /api/agent/chat`。总控 `skills/router/RouterAgent` 只返回受限路由，随后单向调用 `skills/knowledge/KnowledgeAgent`、`skills/recommendation/RecommendationAgent` 或 `skills/business/BusinessAgent`；末端 `skills/summary/SummaryAgent` 在不修改事实和组件契约的前提下整理最终答复，并将另一份脱敏摘要写入 Redis 一天。每个智能体在自身类中声明 `MODEL_NAME`，直接调用 `AgentModelFactory.create("模型名", 可选温度, 可选 Token 上限)`；模型选择清晰可见，工厂集中维护常用模型默认参数、端点适配和客户端缓存，密钥仍由配置注入。

模型调用遵循 Spring AI Tool/Prompt 约定：受控工具方法使用 `org.springframework.ai.tool.annotation.Tool`，参数使用 `@ToolParam` 描述；接入官方 `ChatClient` 时通过 `ToolCallbacks`/`ToolCallbackProvider` 注册白名单工具，并在模型调用前后执行权限、状态和审计校验。当前兼容客户端仍负责 OpenAI 兼容 HTTP 调用，注解作为稳定 Tool 元数据，禁止绕过应用服务直接执行数据库或外部命令。提示词集中位于 `agent/prompts/AgentPrompts`，按角色、任务、上下文边界、输出格式、安全规则和降级策略分段并版本化。

知识 Agent 使用 `ModularRagService`：`QueryRewriteModule` 改写查询，`MilvusVectorSearchClient` 执行原查询/改写查询向量检索，`MetadataFilterModule` 校验分类、租户、状态、版本和生效时间，`RetrievalFusionModule` 做倒数排名融合与轻量关键词补分，`RagContextAssembler` 组装带来源的不可信上下文。Embedding 通过 OpenAI 兼容 `/embeddings`，Milvus 使用 REST v2 搜索接口；推荐免费本地 Ollama `bge-m3`（先执行 `ollama pull bge-m3`）。未配置模型或 Milvus 时必须返回明确降级信息，不得生成无依据的事实。

# AGENTS.md

## 项目定位

这是面向家政服务的生产级 Spring Boot 后端，同时提供 AI Agent 能力。优先级依次为：正确性、数据安全、架构边界、可观测性、向后兼容、可维护性。当前只规划三类 AI 能力：检索推荐、对话式业务办理/表单补全、知识交谈（RAG QA）。不要扩展成无边界的“万能 Agent”。

## 当前阶段范围（明确标记）

下表是当前迭代的范围约束。标记为“暂不考虑”的能力不得为了实现普通需求提前创建接口、表、依赖、Redis Key 或 Tool；如果需求确实需要，必须先更新本节并说明迁移影响。

| 能力                                 | 当前状态     | 约束                                         |
| ------------------------------------ | ------------ | -------------------------------------------- |
| 用户、入驻、商家资料                 | 当前建设     | 以真实业务数据和权限为准；商家审批通过后成为平台用户 |
| 服务项目、价格、区域、可用时间       | 当前建设     | 支撑检索推荐和订单校验                       |
| 工作人员主数据、排班、线上交流       | **暂不考虑** | 平台线下自行找人；仅允许订单保存外部人员引用   |
| 订单、预约、改期、取消、支付         | 当前建设     | 必须有状态机、幂等、并发和审计               |
| AI 检索推荐                          | 当前建设     | 只读为主，事实来自结构化查询                 |
| AI 对话办理、表单补全                | 当前建设     | 写操作后端校验，敏感操作必须确认             |
| AI 知识交谈 / RAG QA                 | 当前建设     | 必须保留来源、版本和生效时间                 |
| 评价、投诉、售后                     | **暂不考虑** | 不新增相关业务流程或 Agent Tool              |
| 优惠券、营销活动                     | **暂不考虑** | 不设计优惠计算、活动规则或缓存               |
| 分布式锁、限流、复杂幂等平台         | **暂不考虑** | 当前只做数据库约束和状态校验；确需引入先评审 |
| 高级缓存、RAG 缓存                   | **暂不考虑** | Redis 只保存 Agent 摘要短期记忆，TTL 暂定 1 天 |
| 高级派单、自动调度优化               | **暂不考虑** | 先完成基本可用性查询和人工/规则流程          |
| 平台估价操作                         | **暂不考虑** | 工作人员现场线下估价；平台仅录入最终结果      |
| 多租户计费、分账、退款自动化         | **暂不考虑** | 涉及资金的需求必须单独设计和确认             |

“暂不考虑”不等于允许使用临时代码绕过边界；应返回明确的未支持错误或保留扩展点。

## 技术栈基线（以构建文件为准）

| 组件                                                     | 版本/状态                                                 |
| -------------------------------------------------------- | --------------------------------------------------------- |
| Java                                                     | 21（Gradle toolchain）                                    |
| Spring Boot                                              | 3.5.16                                                    |
| Gradle Wrapper                                           | 9.7.1                                                     |
| JPA / Security / Web MVC / Validation / Actuator / Redis | Spring Boot 3.5.16 管理版本                               |
| MySQL                                                    | 8.4 LTS（Compose 开发环境）                               |
| Redis                                                    | 7.4 Alpine（Compose 开发环境）                            |
| 测试                                                     | JUnit 5、Spring Boot Test，`useJUnitPlatform()`           |
| Spring AI Alibaba                                        | 1.1.2.2 BOM + `spring-ai-alibaba-starter-dashscope`       |
| LLM / Embedding                                          | DashScope Chat + Ollama `bge-m3` Embedding；关键配置直接填写 |
| Vector Store / Embedding                                 | Milvus；本地开发推荐 Ollama `bge-m3`，模型可通过配置替换    |

依赖依据为 `build.gradle`、`settings.gradle`、Gradle Wrapper 和 `compose.yaml`。版本冲突时以构建文件及实际依赖解析结果为准；新增依赖前检查 Spring Boot 兼容性，优先使用官方 BOM，禁止浮动版本。当前 AI 依赖统一由 Spring AI Alibaba 1.1.2.2 BOM、Spring AI 1.1.2 BOM 管理，DashScope 提供模型接入；Milvus 作为 Vector Store，Embedding 模型配置留空。Compose 中的默认密码仅限本地开发，生产必须使用 Secret Manager。

## 当前代码布局

源码根包为 `src/main/java/com/example/home_service_backend`。目录职责必须保持单向依赖，新增代码先搜索并复用现有抽象，再创建最小必要文件：

```text
config      Spring 配置类（Security、Session、Jackson、外部客户端等）
controller  HTTP 参数接收、@Valid 校验、身份上下文和统一响应映射
service     Application/Domain 用例、事务边界和状态迁移编排
repository  Spring Data/JPA 持久化访问、投影查询及全部 SQL/JPQL/Criteria/Specification
entity      JPA 持久化模型；不得直接作为 API 请求或响应
dto/request 按领域存放 HTTP 请求对象（只表达输入，不放 View/Response）
vo          按领域存放 API 输出 View/Response（只读展示模型，不放 Request）
security    认证主体、UserDetailsService、认证过滤器及 401/403 处理器
common/
  result    Result、ResultData、ResultPage 等统一响应契约
  exception 业务异常、错误码和全局异常处理
  enums     稳定的状态/角色枚举（序列化值不得擅自修改）
  constants Redis Key、认证、API 等常量；禁止散落魔法字符串
  utils     无状态、通用工具（不得承载业务规则）
agent       按 skills/<name>（每个含 SKILL.md）、tools/*Tool、rag、memory、model、prompts、service、dto、vo 分层
```

Request 与 View 必须物理分离：例如 `dto/request/auth/LoginRequest`、`dto/request/user/ChangePasswordRequest`，对应响应放在 `vo/auth`、`vo/user`。禁止在 `dto` 中混放 `*View`/`*Response`，也禁止把 Entity、密码哈希、Session 内容直接暴露给 API。配置类统一放 `config`，工具类统一放 `common/utils`，常量统一放 `common/constants`；若某工具只服务单一领域，应优先放在该领域 service 下而不是扩充万能工具类。Controller 不得直接访问 Repository/Redis/LLM，Service 不得依赖 Controller 或拼接 HTTP 响应。

持久化分层是强约束：所有 SQL、JPQL、原生查询、Criteria/Specification 条件和投影定义只能位于 `repository` 包（或 Repository 的专用实现）。Controller、Service 与 `security/UserDetailsServiceImpl` 不得注入 `JdbcTemplate`/`EntityManager`、拼接 SQL 或直接执行数据库查询，只能调用 Repository；Service 可以根据用例创建 `Pageable`/`Sort`，并负责业务编排、权限、资源归属、状态机和事务边界。认证固定遵循 `LoginFilter → AuthenticationManager → AuthenticationProvider → UserDetailsService → Repository`，`UserDetailsServiceImpl` 只能组装认证主体。

认证 Bean（`PasswordEncoder`、`DaoAuthenticationProvider`、`AuthenticationManager`）集中定义在 `config/SecurityBeanConfig`，`SecurityConfig` 只负责过滤器链和授权规则，避免在认证服务中互相注入造成循环依赖。当前 Spring Security 7.1 已移除 `AntPathRequestMatcher`，URL 匹配统一使用官方 `PathPatternRequestMatcher`；不要为了兼容旧示例自行伪造该类。

包名必须与目录一致，公共类使用清晰后缀（`*Request`、`*View`/`*Response`、`*Service`、`*Repository`、`*Config`）。移动类时同步更新全部 import、测试和文档，不保留旧包的重复实现或兼容空壳；删除/重命名公共包前需提供迁移说明。

不得修改已经存在枚举，统一返回，异常处理等等。可进行添加

## Controller 规范

Controller 只负责 HTTP 层适配：参数接收、`@Valid` 校验、身份上下文获取、调用 Application Service、映射统一响应。禁止在 Controller 中计算价格、修改多个 Entity、操作 Repository/Redis、调用 LLM 或第三方 API。

```java
@PostMapping
public Result<OrderView> create(@Valid @RequestBody CreateOrderRequest request) {
    return Result.success(orderService.create(request));
}
```

接口必须明确 HTTP method、路径、状态码、错误码、分页、排序、幂等键和权限。新增字段优先可选，避免破坏旧客户端；敏感接口必须校验当前用户和租户边界。

## Service 规范

Service 按业务用例组织，不制造无意义的 `Manager`、`FactoryFactory` 或万能 `Processor`。应用服务编排流程，领域服务承载可复用规则，基础设施服务封装外部依赖。每个写用例应说明前置状态、状态迁移、事务范围、并发策略、审计事件和失败补偿。

事务应尽可能短：事务内只做必要的数据库读写和状态变更；LLM、RAG、HTTP、上传、消息发送等放在事务外或通过可靠事件异步执行。不要捕获异常后返回 `null`，应抛出明确业务异常并保留 cause。

## Entity / JPA 规范

- Entity 是持久化模型，不直接作为 API 请求或响应。
- 关联默认 `LAZY`，谨慎使用双向关联；禁止在 `toString`、`equals`、`hashCode` 中遍历关联。
- 状态迁移集中在领域规则/服务中，禁止 Controller 直接 set 状态绕过状态机。
- 金额使用定点类型（如 `BigDecimal`），明确精度和舍入；不要使用 `double` 表示金额。
- 必须声明长度、非空、唯一约束和索引意图；大表查询必须配合分页和投影。
- 修改字段时考虑旧数据、序列化、迁移脚本和回滚；删除字段采用兼容迁移而非直接删除。

## 事务、幂等与并发

预约创建、订单发布/确认、支付回调以及 Agent 写 Tool 都必须能够安全重试。优先使用请求幂等键、数据库唯一约束、状态条件更新和版本号；Redis 仅作辅助，不能成为唯一成功依据。状态检查必须在真正写入前再次执行，外部调用成功但本地提交失败时要保留可重试、对账和审计信息。禁止在同一事务内执行 LLM、RAG、HTTP、COS 上传等长耗时操作。

## Repository 规范

Repository 只负责持久化访问，不承载权限、价格计算或状态机。简单查询使用 Spring Data 方法；复杂查询使用 JPQL、Specification 或项目已有 QueryDSL。查询评审必须检查 WHERE/JOIN/ORDER BY、索引命中、分页、锁和 N+1。

## MySQL 与迁移

MySQL 开发环境为 8.4 LTS、字符集 `utf8mb4`。正式数据库版本、时区、连接池、备份和恢复策略以部署配置为准。表设计必须考虑主键、租户、created_time/updated_time。

新增索引前先根据真实查询确认选择性和顺序，避免重复或低选择性索引。大型表变更要评估线上 DDL 成本。任何批量 UPDATE/DELETE 先确认 WHERE、数据范围、影响行数和回滚方案；禁止空 WHERE 的危险 SQL。

## Redis 规范

Redis 版本为 Compose 的 7.4 Alpine。只存短期 Agent 摘要记忆（TTL 暂定 1 天）及必要的缓存/Session 辅助；MySQL 才是业务事实。每个 Key 文档化命名空间、版本、TTL、最大值、序列化、脱敏、淘汰和失败降级。当前不建设长期记忆数据库表，不把 Redis 操作写进核心事务的唯一成功路径，也不要无限续锁或无限重试。

## 认证、Session 与鉴权

登录使用 Spring Security + Spring Session Data Redis，禁止在浏览器或 Redis 中保存 JWT、明文密码或密码哈希。`POST /api/auth/login` 不经过 Controller 或框架默认 `UsernamePasswordAuthenticationFilter`，由自定义 `LoginFilter extends AbstractAuthenticationProcessingFilter` 通过 `PathPatternRequestMatcher`（Spring Security 7 已移除 `AntPathRequestMatcher`）精确匹配。过滤器按 `AuthenticationManager → AuthenticationProvider → UserDetailsService → Repository` 链路认证，成功后自行执行 Session Fixation 防护、设置 3 天超时并保存 `SecurityContext` 到 Redis Session；会话无操作超时时间固定为 3 天。Session Cookie 使用 `HttpOnly`、`SameSite=Lax`，生产必须开启 `Secure` 并使用 HTTPS。

认证接口约定：`POST /api/auth/register` 仅创建普通用户、初始化 `username_normal_user` 角色并返回用户编号；`POST /api/auth/login` 建立 Session；`GET /api/auth/me` 返回当前认证主体；`POST /api/auth/logout` 使 Session 失效并清理 Cookie；`GET /api/auth/csrf` 返回 CSRF Token。登录、注册、注销失败不得泄露账号是否存在、密码校验细节或数据库异常。

用户接口约定：`GET /api/users` 分页查询用户、`GET /api/users/{id}` 查询后台详情、`POST /api/users` 添加普通用户、`DELETE /api/users/{id}` 逻辑删除（状态置为 `logout`）、`PUT /api/users/{id}/lock` 锁定、`PUT /api/users/{id}/unlock` 解锁；上述后台接口仅开放给已有管理员角色并再次校验资源边界。普通用户通过 `GET /api/users/me` 查询自身资料、`PUT /api/users/me/profile` 修改昵称/头像/性别、`PUT /api/users/me/password` 修改密码。删除和锁定会尽力通过 Spring Session 索引撤销目标用户的 Redis 会话，数据库状态仍是最终事实。

用户状态只能使用 `normal`、`locked`、`logout`；后两者认证失败。角色和权限从 `user_role`、`role_permission`、`permission` 查询，不能相信请求体中的 userId/role。Controller 不直接判断角色字符串；接口使用 Security 的认证上下文和方法级 `@PreAuthorize`/权限表达式。所有非公开接口默认要求认证，资源归属和平台管理员边界仍由应用服务二次校验。

登录成功后必须显式保存 SecurityContext，并采用 Session Fixation 防护；注销应清理 SecurityContext、销毁 Redis Session 并删除 Cookie。Session Redis 故障时不得静默降级为无状态“已登录”；返回认证基础设施错误并记录脱敏指标。浏览器写请求启用 CSRF，先访问 `/api/auth/csrf` 获取 Token，再通过 `X-XSRF-TOKEN` 提交登录后的写操作。

## 外部服务与异步

第三方 API、模型服务和文件存储必须设置连接/读取超时、有限重试、退避、熔断、错误映射和指标。非幂等请求谨慎重试，记录供应商 requestId 但不得记录密钥和完整隐私。

文档解析、清洗、分块、Embedding、RAG 索引、通知和非关键日志适合异步。关键交易仍需同步完成并返回确定状态；异步任务应支持重试上限、死信/失败记录、幂等消费和可观测性。

## Spring AI Alibaba 依赖与使用

当前 `build.gradle` 使用 `com.alibaba.cloud.ai:spring-ai-alibaba-bom:1.1.2.2`、`org.springframework.ai:spring-ai-bom:1.1.2`，并引入 `spring-ai-alibaba-starter-dashscope` 及 Milvus Vector Store starter。不得再为 Spring AI 模块单独写漂移版本。模型名、Endpoint、API Key、Embedding 模型和超时全部由配置注入。开发环境默认推荐免费本地 Ollama `bge-m3`，生产选型仍需评测维度、效果和资源消耗；密钥禁止写入源码、YAML 或 Git。

DashScope starter 提供 Chat/Embedding 模型接入，Milvus 负责向量存储；MySQL 仍是业务事实库。RAG 采用 Modular RAG：解析/清洗/分块、元数据过滤、查询重写、向量检索、关键词检索、融合/重排、上下文组装和生成均为可替换模块。若官方版本与本项目不兼容，应暂停引入并记录决策，不要通过强制降级 Spring Boot 或排除传递依赖解决。

总控 Agent 只负责意图识别和一次路由，随后单向调用一个专用 Agent（检索推荐、业务办理或知识问答）；不允许多 Agent 互相循环。页面组件不是动态生成：后端返回固定的组件类型和 Schema 版本，前端从白名单渲染已有组件。Agent 短期记忆只保存脱敏摘要到 Redis，TTL 暂定 1 天，不建设长期记忆持久化表。

AI 调用必须经过项目自己的 Ai/Agent Service，统一处理超时、重试、Token、审计、脱敏、错误和成本。普通业务 Service 不得散落 `ChatClient` 调用。

当前构建使用 Spring Boot `3.5.16` 与 Spring AI Alibaba `1.1.2.2`，依赖可完成解析和 Java 编译。Agent 运行时仍通过项目 `agent/model` 的 OpenAI 兼容客户端统一封装模型调用，避免业务代码直接耦合供应商 SDK；升级 Spring Boot 或 Alibaba BOM 前必须重新执行完整测试并核对自动配置兼容性，禁止伪造类或吞掉测试失败。

## Tool Calling 与权限

Tool 是公共契约，单一职责、参数明确、输出结构化、可审计、可幂等，Java 类统一使用 `*Tool.java` 命名并放在 `agent/tools`。Skill 是可复用的指令与上下文包，每个 `agent/skills/<name>/` 必须包含 `SKILL.md`，说明触发场景、允许 Tool、输出 Schema、确认规则和禁止事项。权限分为 `READ`、`WRITE`、`SENSITIVE_WRITE`，在 Tool 执行前后都校验当前用户、租户、资源归属和业务状态。禁止提供任意 SQL、Redis、HTTP、文件系统或系统命令 Tool。

LLM 输出必须经过 Parse → Schema Validation → Business Validation → Permission Validation → Execution。Agent 设置最大迭代、Tool 次数、Token、响应大小和超时；检测循环、提示注入、越权、Tool 参数伪造和 RAG Poisoning。文档内容永远不能覆盖 System Prompt、权限和后端规则。

## RAG 数据与版本

Document、Version、Chunk、Embedding Version、Status、Effective From/To 必须可追溯。文档更新后旧版本不得继续参与默认检索；索引任务需要幂等、可重建和失败重试。Chunk 至少包含 document_id、chunk_id、source、title、content、tenant_id、category、version、created_at。

数据来源：包括后台管理员输入的知识信息和数据库种部分数据（服务信息，入驻商家），数据必须走标准流程，为预处理、清洗、切块、向量化、构建索引、入库。

### RAG 知识库管理接口与事实来源

管理员通过 `POST /api/admin/rag/documents` 提交或更新知识文档，`GET /api/admin/rag/documents`（及 `/{id}`）查看文档、当前版本、分块数量和索引状态；`POST /api/admin/rag/documents/seed` 导入 `src/main/resources/knowledge/home_service_faq.md` 内置家政 FAQ；`DELETE /api/admin/rag/documents/{id}` 软删除文档并清理 Milvus 向量；`POST /api/admin/rag/documents/{id}/reindex` 从 MySQL 当前版本重新向量化和写入。接口仅允许审计、系统和超级管理员，正文必须经过校验、分块和脱敏，不能把模型输出直接写入知识库。

判断 RAG 中“有什么数据”以 MySQL 为准：`rag_document` 是文档目录和 `ACTIVE/DELETED` 状态，`rag_document_version` 是版本、有效期和 `INDEXING/ACTIVE/RETIRED/FAILED` 状态，`rag_document_chunk` 是正文分块及 `PENDING/INDEXED/FAILED/DELETED` 状态。Milvus collection 只保存可重建的 FloatVector 与 `document_id/chunk_id/source/title/content/tenant_id/category/version/status/effective_from/effective_to` 元数据；默认检索必须过滤 ACTIVE 版本、租户、分类和生效期。删除时先提交 MySQL 状态，再按 `document_id` 调用 Milvus v2 delete；向量删除失败不得恢复业务可见性，后续可用 reindex/运维重试。Milvus 误删不影响业务事实，可依据 MySQL 中 INDEXED 分块完整重建。

## 敏感数据与配置

密码、Token、Cookie、身份证号、银行卡、支付信息、数据库凭证、内部密钥和完整个人隐私不得进入日志、Prompt、RAG 或 Tool 参数。发送模型前执行最小化、过滤、脱敏和权限检查。Secret 仅通过环境变量、Secret Manager 或部署平台注入；`.env` 必须被 `.gitignore` 忽略。

## 日志、指标与审批

日志用于后台系统的管理，暂时只做AI相关日志（系统、文件等日志暂不考虑），AI 指标记录 model、latency、input/output/total tokens、tool_calls、retrieval_count、retrieval_latency、failure_reason 和成本估算。商家入驻需要管理员审批

## 兼容性、提交与报告

公共 API、DTO、Enum、数据库字段、Redis Key、消息格式、Tool Schema 和 RAG Metadata 都需向后兼容。Breaking Change 使用 Add → Migrate → Switch → Remove，必要时提供 V2。提交保持小而明确，使用 `feat:`、`fix:`、`test:`、`refactor:`、`docs:`、`chore:` 前缀。

完成修改后至少执行 `./gradlew test`、重要变更执行 `./gradlew build`，并检查 `git diff --check`、`git status`。不能运行时必须如实说明环境原因；不能把失败或未执行伪称为通过。

## 推荐的分层调用方向

```text
Controller
  → Application Service
    → Domain Service / Policy
      → Repository / Infrastructure

AI Controller
  → Agent Application Service
    → Intent / Slot / Guardrail
      → Read Tool or Write Tool
        → 普通业务 Application Service
```

反向依赖、Controller 直接访问 Repository、Entity 依赖 AI Client、普通业务依赖 Prompt 文本均需要评审。AI Tool 可以调用受控的业务应用服务，但业务应用服务不能信任模型传入的权限、金额、状态或资源 ID。

## 统一错误与状态原则

错误码应稳定、可搜索、可监控，至少区分参数错误、未认证、无权限、资源不存在、状态冲突、幂等重复、外部依赖失败和系统错误。对用户可见的消息不泄露 SQL、堆栈、密钥或内部路径；日志保留完整 cause 但脱敏。

状态迁移采用显式表格或策略方法，禁止通过字符串比较散落实现。非法迁移返回明确冲突错误，不自动“修正”数据库状态。任何取消、退款、支付完成、派单和服务完成操作都必须保留操作者、时间、原状态、新状态和原因。

## 可观测性约定

每个跨服务请求传播 requestId/traceId；异步任务携带 jobId、父请求 ID 和业务主键。日志采用结构化字段，不记录完整 Prompt、用户原文隐私、Authorization、Cookie 或 Secret。指标名称和标签保持低基数，避免将 userId、orderId 直接作为高基数标签。

关键指标包括模型延迟/Token/失败率、Tool 调用次数和 RAG 检索耗时。

## 禁止事项汇总

1. 不删除用户代码、配置、数据或测试而不说明原因。
2. 不提交密码、Token、Cookie、API Key、身份证号或支付数据。
3. 不绕过权限、状态机、幂等和数据库约束。
4. 不在事务中执行长时间 AI/HTTP/文件操作。
5. 不使用无限 Agent Loop、无限 Retry、无限 Redis Lock 或无限分页。
6. 不用 RAG/LLM 猜测订单、价格、人员、时间和退款事实。
7. 不吞异常、不返回伪造成功、不隐藏测试失败。
8. 不为小需求进行大规模重构或引入未经验证的依赖。
9. 不把本文件当成依赖版本的唯一来源；始终核对实际构建配置。

## AI三大功能能力边界

### 1. 检索推荐（READ）

检索服务、价格、区域、人员和排班并解释推荐。价格、库存、排班、订单状态等事实必须来自后端结构化查询，不能由模型生成。

### 2. 对话式业务办理与表单补全（READ/WRITE）

对话可完成查询、预约、改期、取消、地址和联系人等基本操作。要修改数据的流程固定为：

根据可能要调用的接口工具给出在ai对话中特有的组件表单格式 -> 让用户完成填写并提交（提交方式以及出现的异常处理方式与正常表单提交无异） ->  完成提交后，猜想可能需要证明提交完成的内容进行查询并发给用户（比如完成预约用户需要去其他界面查看刚才预约的信息，智能体会直接发给用户让用户查看）

Agent 只能填充当前用户有权访问的字段；缺失、冲突或低置信度信息必须追问。支付、退款、取消、账户和隐私相关写操作不得静默执行。

### 3. 知识交谈（RAG/READ）

用于服务说明、流程、标准、政策和家政知识 QA。每个 Chunk 至少保留 `document_id`、`chunk_id`、来源、标题、内容、租户、类别、版本和生效时间。检索应优先元数据过滤，再进行向量/关键词混合检索和重排；无可靠依据时明确回答未知。

## Agent、Tool 与安全

AI 层与业务层解耦，通过 `agent/service` 的应用编排、`agent/rag` 的模块化检索、`agent/memory` 的短期记忆、`agent/model` 的模型工厂和 `agent/tools` 的受控 Tool 协作。RAG 与 memory 不得再次复制到 service；普通业务 Service 不得散落 `chatClient.prompt(...)`。无状态通用工具统一放 `agent/common/utils` 并命名 `*Util.java`。

每个 Agent 必须声明目标、上下文、允许的 Tool、权限、记忆、检索、输出 Schema、确认规则和禁止事项。Tool 单一职责、参数明确、结构化输出、可测试、可审计、具备权限检查；禁止任意 SQL、Redis Command、HTTP、文件系统或系统命令 Tool。

权限至少分为 `READ`、`WRITE`、`SENSITIVE_WRITE`。LLM 输出不等于事实、权限或指令，必须经过 Parse → Schema Validation → Business Validation → Permission Validation → Execution。Agent Loop 设置 `max_iterations`、`max_tool_calls`、`max_tokens` 和 timeout，禁止无限调用。

不得向模型发送密码、Token、Cookie、身份证号、银行卡、支付信息、密钥、数据库凭证或完整隐私数据；调用前执行过滤、脱敏和最小化上下文。RAG 文档是不可信输入，不能覆盖 System Prompt、权限或后端规则。

## 业务流程边界补充

用户找家政的流程是“预约申请 → 平台查看并线下自行找人 → 工作人员现场线下估价 → 形成并发布订单 → 用户确认/完成订单”。本系统不实现估价操作，平台当前不建设 worker 主数据、工作人员排班、工作人员线上交流或自动派单；`worker_id` 仅可作为外部引用或人工录入信息，不能据此扩展出工作人员管理模块。预约申请与正式订单必须分表、分别记录状态和审计，不得在预约提交时虚构已支付订单。

入驻流程是“提交申请（地图选点、图片上传）→ 高德解析/校验位置 → 腾讯云 COS 保存对象 → 审计管理员审批 → 审批通过后成为平台用户/建立商家资料”。高德和 COS 调用放在事务外，数据库保存必要的坐标、供应商和 Object Key 元数据，不保存密钥或临时签名 URL。审批必须记录审计管理员、时间、结果、备注，并处理重复审批。

当前日志只建设 AI 相关日志；系统日志、文件日志管理平台暂不考虑。AI 不持久化完整会话和消息正文：Redis 只保存脱敏摘要，TTL 暂定 1 天；MySQL 只保存有限期请求元数据、敏感 Tool 审计和每日聚合统计。评价、投诉、售后、优惠券、营销活动、复杂分布式锁、限流、高级缓存、RAG 缓存、高级派单/自动调度、工作人员线上能力、多租户计费、分账和退款自动化、平台估价功能均明确“暂不考虑”，不得提前创建接口、表、依赖、Redis Key 或 Tool。

## AI 数据存储与生命周期

完整对话正文不是业务事实，也不是长期记忆。前端负责展示当前会话；后端只在 Redis 中缓存脱敏摘要，默认 TTL 1 天。MySQL 使用三类轻量数据：`ai_request_log`（失败、敏感业务和按比例采样请求的路由/模型/Token/延迟/状态，默认 30 天）、`ai_tool_call_log`（写入或敏感 Tool 必须审计，普通 READ 只记录失败或采样，payload 只保留哈希和大小，默认 90 天）和 `ai_usage_daily`（按日、路由、Agent、模型聚合的用量，可长期保留）。明细表按 `expire_time` 分批、限速清理；禁止逐消息写库、保存完整 Prompt/响应或无期限保留用户原文。若未来确需合规留存原文，必须另行设计加密、访问审批、分区归档和生命周期，不能直接恢复旧表。

### 写入规则与保留矩阵

| 数据 | 写入策略 | 默认保留 | 允许内容 |
| --- | --- | --- | --- |
| Redis 摘要 | 每次会话覆盖同一版本 Key，超时自动过期 | 1 天 | 脱敏后的意图、已确认槽位摘要、待办；不得存密码/证件/原文 |
| `ai_request_log` | 错误、超时、越权、敏感业务全量；普通成功 READ 按 `AI_READ_SAMPLE_RATE`（默认 1%）采样 | 30 天 | 路由、模型、Token、延迟、状态、错误码、组件类型 |
| `ai_tool_call_log` | WRITE/SENSITIVE_WRITE 全量；READ 仅失败或采样 | 90 天 | Tool 名、权限、确认结果、payload 哈希/大小、延迟 |
| `ai_usage_daily` | 请求先累加进程内低基数计数器，每 5 分钟或达到阈值后批量 UPSERT 增量 | 可长期 | 次数、Token、Tool/检索次数、总延迟 |

因此“每天大量谈话”不会等量产生 MySQL 消息行或逐请求聚合写入：绝大多数成功 READ 只更新内存指标，少量采样产生请求元数据；只有写操作和敏感操作产生 Tool 审计。聚合指标属于可观测数据而非业务事实，进程异常时允许丢失尚未刷新的少量增量；多实例各自按增量 UPSERT，禁止用旧总值覆盖。明细达到期限后由定时任务按主键小批量删除，避免锁表和无界增长。任何实现若重新写入 `ai_conversation`、`ai_message_log` 或保存 Prompt/响应正文，均视为违反本约定。

## 认证与数据库事实来源

角色和用户状态必须以 `AuthRoleEnum`、`AuthUserStateEnum` 源码为唯一事实来源，禁止自行使用 `MERCHANT`、`WORKER`、`ADMIN`、`ACTIVE` 等替代枚举值。当前角色编码为：

```text
username_super_admin = 1  超级管理员
username_sec_admin   = 2  安全管理员
username_aud_admin   = 3  审计管理员
username_sys_admin   = 4  系统管理员
username_normal_user = 5  普通用户
username_pla_user    = 6  平台用户（商家入驻审批通过后使用）
```

用户状态仅使用 `normal`、`locked`、`logout`。新增角色/状态必须同步修改枚举、权限矩阵、DTO、数据库种子、迁移和测试，并说明兼容影响。

`src/main/resources/db/home_service.sql` 以当前已有表名和字段为基线，尊重 `created_time`/`created_at` 等历史差异，不擅自重命名。新增表默认不建立外键，跨表 ID 的存在性、资源归属、用户可见范围和权限由应用层校验；如需补外键必须单独评审历史数据、删除策略和迁移成本。包含 DROP 的初始化脚本仅用于明确环境，生产必须改成可回滚迁移。

## 配置与外部供应商

数据库、Redis、DashScope、高德、腾讯云 COS 的地址、超时、模型名、Key 和桶信息统一在 `application.properties` 使用明确属性配置；当前项目不使用环境变量占位符。非敏感参数使用代码默认值或推荐值，密码和密钥留空后由部署者直接填写，禁止提交真实生产密钥。所有外部调用需要超时、有限重试、错误映射和降级；上传要校验 MIME、扩展名、大小、哈希和恶意内容，地图响应只保留必要脱敏字段。

AI 日志采样比例由 `app.ai.persistence.read-sample-rate`（默认 1%）控制；`store-conversation-content` 与 `store-tool-payload` 固定为 `false`，防止通过环境变量误开启正文落库。

## 对话式表单交互约定

当 Agent 需要办理新增/修改业务时，应根据 Tool Schema 返回页面内可渲染的组件表单，让用户填写并提交；提交使用与普通表单相同的校验、权限、幂等和错误处理。敏感字段必须用户明确填写或确认，非核心字段可根据会话历史生成草稿但必须可编辑。提交成功后必须调用 READ Tool 查询真实结果，并在当前对话展示单号、状态、金额或预约详情，不能只回复“已完成”。

## 预约、订单与支付接口

- `POST /api/appointments`：认证用户创建预约，必须校验商铺营业状态、在线服务项目和地址归属；写入 `PENDING_PLATFORM`，请求幂等键和状态历史由后端处理。
- `GET /api/appointments`、`GET /api/appointments/{id}`：用户只能看自己的预约；平台管理员可看全部；商铺平台用户可看自己商铺相关预约。
- `PUT /api/appointments/{id}/status`：平台管理员按 `PENDING_PLATFORM → CONTACTING → WORKER_ARRANGED` 推进，工作人员仍是线下外部人员，不建立 worker 主数据。
- `POST /api/orders/from-appointment`：平台管理员根据线下估价生成 `PENDING_PUBLISH` 订单，保存地址/服务/外部工作人员展示快照；同一预约只能生成一个订单。
- `PUT /api/orders/{id}/publish`：平台管理员发布为 `PENDING_PAYMENT` 并设置支付过期时间。
- `POST /api/orders/{id}/pay`：订单用户使用 Stripe Test/Sandbox 模式支付；当前实现为可替换的沙盒适配器，必须启用 `payment.stripe.enabled` 与 `payment.stripe.sandbox`，生产接入真实 Checkout/Webhook 前不得宣称已完成真实扣款。
- `PUT /api/orders/{id}/confirm`：用户在支付成功后确认订单，`PAID → CONFIRMED`；`GET /api/orders`、`GET /api/orders/{id}` 按用户、商铺和平台管理员边界返回。

订单状态使用 `PENDING_PUBLISH`、`PENDING_PAYMENT`、`PAID`、`CONFIRMED`、`IN_SERVICE`、`COMPLETED`、`CANCELLED`、`CLOSED`。订单和预约的状态迁移必须写入对应 history 表；金额使用 `BigDecimal`，支付记录通过幂等键和供应商支付号去重。`home_service.sql` 已补充预约目标商铺、订单状态历史及 Stripe 支付元数据字段；生产环境需使用可回滚迁移替代初始化脚本。

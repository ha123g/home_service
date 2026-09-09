# 业务域 AGENTS.md

本目录是家政平台传统业务边界。根目录 AGENTS.md 的安全、事务、兼容和验证规则在本目录全部生效。本文件比根目录更具体；若未来新增更深层 AGENTS.md，以更深层为准。

## 代码现状与修改权限

当前已实现认证/Session 基础设施以及用户管理用例：`controller/AuthController`、`controller/UserController`，应用服务位于 `service`，请求对象位于 `dto/request`，响应模型位于 `vo`。预约、订单、入驻审核等仍按本文边界逐步建设，不要为了“完善准备工作”虚构调用方或大规模生成业务代码。已有 `AuthRoleEnum`、`AuthUserStateEnum`、Result 和异常类视为稳定公共契约：禁止直接修改、重命名或改变序列化值；只能在有明确需求、迁移方案和测试时添加兼容能力。

角色只能使用源码已有的 `username_super_admin`、`username_sec_admin`、`username_aud_admin`、`username_sys_admin`、`username_normal_user`、`username_pla_user`；用户状态只能使用 `normal`、`locked`、`logout`。商家入驻通过后使用平台用户角色，不创建 `MERCHANT`/`WORKER` 等新角色。

## 当前业务流程

### 用户预约找人

1. 用户提交预约申请，填写服务、地址（可通过地址选点填充）、联系方式和期望时间。
2. 平台后台可查看申请，平台管理员负责线下联系合适工作人员。
3. 工作人员、平台与用户之间的排班、线下交流、人员档案当前不实现；
4. 工作人员到现场后完成线下估价；本系统不实现估价操作。外部估价完成后，平台只负责形成/发布正式订单。
5. 用户在当前页面确认/支付/完成订单；系统应返回订单详情供用户核验，不要求用户自行寻找其它入口。

预约申请和订单是两个不同聚合：预约申请表示“请平台找人”的意向，订单表示“平台已根据现场估价发布、等待用户处理”的交易事实。不能在用户刚提交预约时直接创建已支付订单，也不能用订单状态代替预约跟进状态。

## 当前明确不实现

- 不建设 worker/服务人员主数据、排班、考勤、评价、平台内聊天或派单算法。
- 不实现优惠券、活动、秒杀、分账、自动退款、投诉和售后。
- 不改变已有 `AuthRoleEnum`、`AuthUserStateEnum`、Result、异常体系；有特殊需要可在对应模块添加。
- 平台管理员使用现有角色模型；服务商入驻后成为“平台用户”，不要自行创建 `MERCHANT` 或 `WORKER` 角色。

## 入驻业务

申请人提交入驻资料、地图选点和图片。高德 API 只负责地址解析/坐标校验，腾讯云 COS 只负责对象存储；数据库保存申请信息、坐标结果和 COS Object Key，不保存永久公网 URL 或临时签名。申请状态由申请人可查，审核由具备相应权限的审计管理员完成；审批通过后再创建/激活平台用户或商家资料，重复审批必须幂等。

当前商铺接口集中在 `ShopController`，由 `service/ShopService` 编排：用户可提交、查询和取消自己的待审入驻申请；审计管理员可查看与审批申请；审批通过后创建唯一商铺并将普通用户角色转换为 `username_pla_user`。商铺支持详情、分页、多条件、省市区、服务分类及其全部子分类、经纬度半径查询；平台用户只能修改自己的商铺，系统/超级管理员可以修改与停业/暂停。非管理者默认只能看到 `OPEN` 商铺。

地图地址解析通过 `MapController` → `AmapGeocodingService` 调用高德 Web 服务地理编码 API。浏览器地图选点所需的安全密钥与服务端 REST Key 均只能通过配置注入；入驻提交必须带完整地址和一对合法经纬度。第三方 HTTP 调用不放入数据库事务，高德 GET 只允许有限重试，异常映射为稳定业务错误且日志不得包含 Key。

## 代码约束

- 目录职责与根目录一致：`config` 仅放 Spring 配置；`controller` 仅做 HTTP 适配；`service` 负责应用/领域用例；`repository` 只做持久化；`entity` 只做 JPA 模型；`security` 负责认证主体与过滤器。
- 所有 SQL、JPQL、原生查询、`Specification`/Criteria 条件和投影定义必须位于 `repository` 包（或 Repository 接口调用的专用查询实现）。`service`、`security/UserDetailsServiceImpl` 和 Controller 禁止注入 `JdbcTemplate`/`EntityManager`、拼接 SQL 或直接执行查询；Service 可以创建 `Pageable`/`Sort`，但只能调用 Repository 获取数据并负责业务规则、权限和状态校验。认证服务也必须遵循 `UserDetailsService → Repository` 的调用方向。
- HTTP 输入统一放在 `dto/request/<domain>`（如 `auth`、`user`），HTTP 输出统一放在 `vo/<domain>`；不得把 Request、View/Response 混放在同一目录，也不得让 Controller 返回 Entity。新增字段优先采用独立 Request/VO，避免复用导致敏感字段泄露。
- 统一响应、异常、枚举、常量和无状态工具分别放在 `common/result`、`common/exception`、`common/enums`、`common/constants`、`common/utils`。常量不得散落在 Controller/Service，工具类不得承载业务状态。移动类时必须同步 import、测试和文档，不保留旧包重复类。
- Controller 只做参数、身份和响应适配；业务流程放 Application Service。
- 登录态使用 Spring Session Redis，空闲过期时间 3 天；`POST /api/auth/login` 由 `security/LoginFilter`（`AbstractAuthenticationProcessingFilter` + `PathPatternRequestMatcher`）处理，Controller 不实现登录。过滤器按 `AuthenticationManager → AuthenticationProvider → UserDetailsService → Repository` 认证并自行完成 Session Fixation 防护和 SecurityContext 持久化；除登录、CSRF Token 和健康检查外，接口默认必须认证。鉴权依据当前 `SecurityContext`、源码角色枚举和数据库权限，禁止相信客户端传入角色/userId；资源归属仍由应用服务校验。
- `PasswordEncoder`、`DaoAuthenticationProvider` 与 `AuthenticationManager` 统一由 `config/SecurityBeanConfig` 提供；`config/SecurityConfig` 只装配安全过滤器链，禁止在 `AuthService` 或 Controller 中重复声明认证 Bean。
- 认证接口固定为注册、登录、当前用户、注销和 CSRF Token；注册只创建普通用户并分配 `username_normal_user`，禁止客户端选择管理员/平台角色。
- 密码只使用 BCrypt 校验，Cookie 必须 HttpOnly/SameSite，生产开启 Secure；浏览器写请求保持 CSRF 防护，注销销毁 Redis Session。
- 预约创建、状态迁移、订单形成/确认和支付回调必须在短事务内完成，并使用状态检查、幂等键和版本控制；估价属于线下外部过程，不在本系统建模为平台操作。
- 跨表 ID 不假设数据库外键存在；Repository 查询后必须在应用层校验资源存在、用户归属、平台可见性和权限。
- 金额使用 `BigDecimal`，订单必须保留服务、地址、估价和工作人员展示信息快照。
- 用户和平台可见的数据要区分查询范围；任何后台接口不能因为“管理员”字符串而绕过权限。
- 对外返回统一 Result/VO；不得暴露密码、证件号、COS 密钥、内部备注或未脱敏 AI 日志。

## 用户功能边界

后台用户管理由 `UserController` 调用 `service/UserService` 提供：分页列表和详情仅限管理员角色；新增账号只能初始化为 `username_normal_user`，不能由请求选择管理员角色；删除采用状态置为 `logout` 的逻辑删除以保留业务历史；锁定/解锁只允许已有管理员角色并记录状态时间，锁定或删除后撤销目标用户的 Redis Session。普通用户只能通过 `/api/users/me` 查询和修改自己的昵称、头像、性别及密码，用户名不可直接修改。所有查询输出使用 `vo` 中的 View，不返回密码或 Entity。

## 推荐状态

预约申请：`PENDING_PLATFORM` → `CONTACTING` → `WORKER_ARRANGED` → `ORDER_PENDING` → `COMPLETED`，异常可转 `CANCELLED`/`CLOSED`。

订单：`PENDING_PAYMENT` → `PAID` → `CONFIRMED` → `IN_SERVICE` → `COMPLETED`，超时/拒绝可转 `CANCELLED`/`CLOSED`。具体状态以数据库字段和实现为准，新增状态必须更新 DTO、状态机、测试和迁移说明。

## 验证重点

新增业务先覆盖：用户/平台可见性、重复提交、非法状态迁移、外部估价金额的格式/范围校验、订单发布后用户确认、支付回调重复、地址归属、COS 文件类型/大小和高德超时降级。测试失败或外部服务不可用必须如实报告。

## 预约订单实现现状

预约由 `controller/AppointmentController` → `service/AppointmentService` 处理，用户创建后状态为 `PENDING_PLATFORM`；平台管理员通过状态接口推进联系和线下找人，系统不保存 worker 主数据。订单由 `controller/OrderController` → `service/OrderService` 处理：平台依据线下估价从预约生成 `PENDING_PUBLISH`，发布后为 `PENDING_PAYMENT`，用户使用 Stripe Test/Sandbox 支付并确认。双方查询必须经过 Service 的用户/商铺/平台边界校验，所有状态迁移写入 history 表，Repository 承担全部持久化查询。

支付当前仅提供 Stripe 沙盒适配：`payment.stripe.enabled=true`、`payment.stripe.sandbox=true` 后才能调用支付接口；密钥仍通过环境变量注入。没有配置 Stripe 时返回明确的 503，不得伪造生产支付成功。

## 认证查询实现约定

`security/UserDetailsServiceImpl` 只负责把认证用户名交给 `UserRepository`，再组合角色与权限为 `LoginUser`；用户、用户角色、角色权限和权限编码分别通过 `UserRepository`、`UserRoleRepository`、`RolePermissionRepository`、`PermissionRepository` 获取。不得在该类恢复旧版 `JdbcTemplate` 或内嵌 SQL。新增认证字段时先扩展 Entity/Repository，再由此类组装，不得反向让 Entity 依赖 Spring Security。

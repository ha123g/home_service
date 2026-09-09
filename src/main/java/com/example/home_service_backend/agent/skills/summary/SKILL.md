---
name: summary
description: 在不改变事实和组件契约的前提下整理最终答复，并生成一天有效的脱敏摘要。
---

# Summary Skill

## 目标

接收一个专用 Skill 的结果，压缩为清晰答复；随后只把意图、已确认的非敏感槽位和待办写入 Redis 摘要 Key，TTL 为 1 天。

## 安全边界

不得保留用户原文、完整地址、联系方式、密码、Token、证件、银行卡、支付信息或模型凭据。Summary 不改变订单金额、状态、编号、引用和组件类型。

## 允许 Tool 与输出契约

Summary 不调用业务 Tool；只接收下游 Skill 的结构化结果，输出最终文本以及与原结果一致的 `componentType`、`schemaVersion`、数据和引用。摘要只能写入 `AgentMemoryService` 管理的 Redis Key，不得写 MySQL 会话或消息表。

## 确认规则

Summary 不代表用户确认，也不能把“已生成组件”改写成“已提交成功”。支付、取消、账户修改、入驻审批等敏感动作必须由用户在固定组件中明确确认，并由普通业务接口返回真实结果后再展示。

## 失败降级

模型不可用时返回确定性原答复，并使用 `AgentSummarySanitizerUtil` 生成最小摘要；Redis 不可用不得阻塞本轮业务响应。

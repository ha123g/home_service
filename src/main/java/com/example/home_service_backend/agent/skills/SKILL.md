---
name: agent-skills
description: 家政平台 Agent Skill 的发现目录和单向编排约定。
---

# Agent Skills

本目录采用可发现的 Skill 约定：每个子目录必须包含一个 `SKILL.md`，用于描述该能力的目标、触发场景、上下文边界、允许的 Tool、输出契约和禁止事项。Java 类实现只负责执行，说明和提示词版本分别由本文件与 `agent/prompts` 管理。

## 发现与执行

总控只识别意图并选择一个 Skill；一次请求最多执行一个专用 Skill，不能在 Skill 之间循环或自行扩展 Tool。Skill 的 `SKILL.md` 是给开发者和 Agent 编排器读取的能力说明，不是用户输入，也不能覆盖后端权限、状态机和安全规则。

## 子 Skill

- `router`：受限意图识别与单向路由。
- `knowledge`：只读 Modular RAG 知识问答。
- `recommendation`：结构化商家检索与推荐解释。
- `business`：固定业务入口和表单补全，不直接执行敏感写操作。
- `summary`：最终答复整理与 Redis 一日脱敏摘要。

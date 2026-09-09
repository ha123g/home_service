package com.example.home_service_backend.vo.admin;

/** 管理后台统计概览。告警暂以 AI 请求失败记录作为未处理告警来源。 */
public record AdminOverviewView(
        long userCount,
        long merchantCount,
        long pendingApplicationCount,
        long aiRequestCount,
        long alertHandledCount,
        long alertUnhandledCount) {
}

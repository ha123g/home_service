package com.example.home_service_backend.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** AI 用量只接受批量增量 UPSERT，禁止用旧总值覆盖。 */
public interface AiUsageDailyRepository extends Repository<com.example.home_service_backend.entity.AiRequestLog, Long> {
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = """
            INSERT INTO ai_usage_daily
              (usage_date, route_type, agent_name, model_name, request_count, success_count,
               failure_count, input_tokens, output_tokens, tool_call_count, retrieval_count,
               total_latency_ms, updated_time)
            VALUES
              (:usageDate, :routeType, :agentName, :modelName, :requestCount, :successCount,
               :failureCount, :inputTokens, :outputTokens, :toolCallCount, :retrievalCount,
               :totalLatencyMs, CURRENT_TIMESTAMP(3))
            ON DUPLICATE KEY UPDATE
              request_count = request_count + VALUES(request_count),
              success_count = success_count + VALUES(success_count),
              failure_count = failure_count + VALUES(failure_count),
              input_tokens = input_tokens + VALUES(input_tokens),
              output_tokens = output_tokens + VALUES(output_tokens),
              tool_call_count = tool_call_count + VALUES(tool_call_count),
              retrieval_count = retrieval_count + VALUES(retrieval_count),
              total_latency_ms = total_latency_ms + VALUES(total_latency_ms),
              updated_time = CURRENT_TIMESTAMP(3)
            """, nativeQuery = true)
    int addDelta(
            @Param("usageDate") LocalDate usageDate,
            @Param("routeType") String routeType,
            @Param("agentName") String agentName,
            @Param("modelName") String modelName,
            @Param("requestCount") long requestCount,
            @Param("successCount") long successCount,
            @Param("failureCount") long failureCount,
            @Param("inputTokens") long inputTokens,
            @Param("outputTokens") long outputTokens,
            @Param("toolCallCount") long toolCallCount,
            @Param("retrievalCount") long retrievalCount,
            @Param("totalLatencyMs") long totalLatencyMs);
}

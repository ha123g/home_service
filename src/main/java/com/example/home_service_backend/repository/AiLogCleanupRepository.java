package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.AiRequestLog;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 到期 AI 明细按小批量清理，避免无界增长和长事务。 */
public interface AiLogCleanupRepository extends Repository<AiRequestLog, Long> {
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = "DELETE FROM ai_request_log WHERE expire_time < :now ORDER BY id LIMIT :batchSize",
            nativeQuery = true)
    int deleteExpiredRequestLogs(
            @Param("now") LocalDateTime now,
            @Param("batchSize") int batchSize);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = "DELETE FROM ai_tool_call_log WHERE expire_time < :now ORDER BY id LIMIT :batchSize",
            nativeQuery = true)
    int deleteExpiredToolLogs(
            @Param("now") LocalDateTime now,
            @Param("batchSize") int batchSize);
}

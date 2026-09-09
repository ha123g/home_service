package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.entity.AiRequestLog;
import com.example.home_service_backend.repository.AiRequestLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/admin/ops")
@PreAuthorize("hasAnyRole('username_super_admin','username_sec_admin','username_sys_admin')")
public class AdminOpsController {
    private final AiRequestLogRepository logs;
    public AdminOpsController(AiRequestLogRepository logs) { this.logs = logs; }
    @GetMapping("/logs") public ResponseEntity<ResultData<Page<AiRequestLog>>> logs(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) { return ResponseEntity.ok(ResultFactory.buildSuccessData(logs.findAllByOrderByCreatedTimeDesc(PageRequest.of(Math.max(0,page), Math.min(size,100))))); }
    @GetMapping("/alerts") public ResponseEntity<ResultData<List<Map<String,Object>>>> alerts() { return ResponseEntity.ok(ResultFactory.buildSuccessData(List.of())); }
    @PutMapping("/alerts/{id}/handle") public ResponseEntity<ResultData<Boolean>> handle(@PathVariable Long id) { return ResponseEntity.ok(ResultFactory.buildSuccessData(Boolean.TRUE)); }
    @GetMapping("/logs/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder csv = new StringBuilder("request_id,route_type,agent_name,model_name,status,latency_ms,created_time\n");
        for (AiRequestLog log : logs.findAllByOrderByCreatedTimeDesc(PageRequest.of(0, 5000))) {
            csv.append(String.join(",", safe(log.getRequestId()), safe(log.getRouteType()), safe(log.getAgentName()), safe(log.getModelName()), safe(log.getStatus()), String.valueOf(log.getLatencyMs()), safe(String.valueOf(log.getCreatedTime())))).append('\n');
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ai-logs.csv").contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
    private String safe(String value) { return value == null ? "" : value.replace(",", " ").replace("\n", " "); }
}

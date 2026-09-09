package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.result.ResultBase;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.dto.request.rag.CreateKnowledgeDocumentRequest;
import com.example.home_service_backend.service.KnowledgeDocumentService;
import com.example.home_service_backend.vo.rag.KnowledgeDocumentPageView;
import com.example.home_service_backend.vo.rag.KnowledgeDocumentView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 管理员知识库管理：MySQL 保存版本事实，Milvus 保存可重建向量索引。 */
@RestController
@RequestMapping("/api/admin/rag/documents")
@Validated
@PreAuthorize("hasAnyRole('username_aud_admin','username_sys_admin','username_super_admin')")
public class KnowledgeAdminController {
    private final KnowledgeDocumentService service;

    public KnowledgeAdminController(KnowledgeDocumentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResultData<KnowledgeDocumentView>> create(
            @Valid @RequestBody CreateKnowledgeDocumentRequest request) {
        return ResponseEntity.status(201)
                .body(ResultFactory.buildSuccessData(service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ResultData<KnowledgeDocumentPageView>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(service.list(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultData<KnowledgeDocumentView>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(service.get(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResultBase> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResultFactory.buildSuccessBase());
    }

    @PostMapping("/{id}/reindex")
    public ResponseEntity<ResultData<KnowledgeDocumentView>> reindex(@PathVariable Long id) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(service.reindex(id)));
    }

    @PostMapping("/seed")
    public ResponseEntity<ResultData<KnowledgeDocumentView>> seed() {
        return ResponseEntity.status(201)
                .body(ResultFactory.buildSuccessData(service.seed()));
    }
}

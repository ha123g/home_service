package com.example.home_service_backend.agent.rag;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/** 以边界标记组装不可信知识上下文，便于模型引用且不把文档当系统指令。 */
@Component
public class RagContextAssembler {
    public String assemble(List<RagDocument> documents) {
        return IntStream.range(0, documents.size())
                .mapToObj(index -> {
                    RagDocument document = documents.get(index);
                    return "[资料" + (index + 1) + "] 标题=" + safe(document.title())
                            + "；来源=" + safe(document.source())
                            + "；版本=" + safe(document.version())
                            + "\n<untrusted-document>\n" + document.content()
                            + "\n</untrusted-document>";
                })
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "未标明" : value;
    }
}

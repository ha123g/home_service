package com.example.home_service_backend.agent.rag;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/** 按字符上限切分知识正文，优先在段落/句号边界断开。 */
@Component
public class RagTextChunker {
    private static final int MAX_CHUNK_LENGTH = 1200;
    private static final int OVERLAP = 120;
    public List<String> split(String content) {
        String normalized = content == null ? "" : content.replace("\r\n", "\n").trim();
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(normalized.length(), start + MAX_CHUNK_LENGTH);
            if (end < normalized.length()) {
                int boundary = Math.max(normalized.lastIndexOf('\n', end), normalized.lastIndexOf('。', end));
                if (boundary > start + MAX_CHUNK_LENGTH / 2) end = boundary + 1;
            }
            chunks.add(normalized.substring(start, end).trim());
            if (end == normalized.length()) break;
            start = Math.max(start + 1, end - OVERLAP);
        }
        return chunks;
    }
}

package com.example.home_service_backend.agent.model;

import java.util.List;

public interface EmbeddingClient {
    List<Double> embed(String text);
}

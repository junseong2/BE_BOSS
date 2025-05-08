
package com.onshop.shop.domain.vector.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIEmbeddingService {

    private final WebClient webClient = WebClient.create("https://api.openai.com/v1");

    @Value("${openai.api.key}")
    private String apiKey;

    public float[] getEmbedding(String inputText) {
    	log.info("🔐 주입된 API 키: {}", apiKey);
        try {
            String requestBody = """
                {
                  "input": "%s",
                  "model": "text-embedding-ada-002"
                }
                """.formatted(inputText.replace("\"", "\\\""));

            String response = webClient.post()
                    .uri("/embeddings")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode embedding = root.path("data").get(0).path("embedding");

            int dimension = embedding.size();
            log.info("✅ OpenAI 임베딩 차원 수: {}차원", dimension);

            float[] result = new float[dimension];
            for (int i = 0; i < dimension; i++) {
                result[i] = (float) embedding.get(i).asDouble();
            }

            return result;

        } catch (Exception e) {
            log.error("🧨 OpenAI 임베딩 오류: {}", e.getMessage());
            throw new RuntimeException("OpenAI 임베딩 실패", e);
        }
    }
}



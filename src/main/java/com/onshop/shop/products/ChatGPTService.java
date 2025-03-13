package com.onshop.shop.products;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class ChatGPTService {

    @Value("${openai.api.key}") // API key from application.properties
    private String apiKey;

    private final ProductsService productsService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser

    private List<String> previousRecommendations = new ArrayList<>();

    @Autowired // ✅ Ensures proper dependency injection
    public ChatGPTService(ProductsService productsService) {
        this.productsService = productsService;
    }

    public List<Map<String, Object>> processUserQuery(String userMessage) {
        try {
            // 1️⃣ Fetch all products from DB
            List<ProductsDTO> allProducts = productsService.getAllProducts();
            List<Map<String, Object>> productList = new ArrayList<>();

            for (ProductsDTO product : allProducts) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("name", product.getName());
                productMap.put("price", product.getPrice());
                productMap.put("category", product.getCategoryId());
                productMap.put("description", product.getDescription());
                productMap.put("image", product.getGImage() != null ? product.getGImage() : ""); // ✅ Avoids empty/null images
                productList.add(productMap);
            }

            // 2️⃣ Apply price filtering (if requested)
            int priceLimit = extractPriceLimit(userMessage);
            if (priceLimit > 0) {
                productList = productList.stream()
                        .filter(product -> (int) product.get("price") <= priceLimit)
                        .collect(Collectors.toList());
            }

            // 3️⃣ Build the GPT prompt
            String prompt = "아래는 현재 판매 중인 상품 목록입니다. \n" +
                    "이 목록을 사용하여 사용자 질문에 맞는 상품을 추천하세요. \n" +
                    "반드시 목록에 포함된 상품만 추천해야 합니다.\n\n" +
                    "**상품 목록 (JSON 형식):**\n" +
                    objectMapper.writeValueAsString(productList) + "\n\n";

            // ✅ Ensure previous recommendations are excluded
            if (!previousRecommendations.isEmpty()) {
                prompt += "⚠ **다음 상품들은 이미 추천된 상품입니다.**\n" +
                          previousRecommendations + "\n" +
                          "**이전 추천을 제외하고 새로운 상품을 추천해주세요.**\n\n";
            }

            prompt += "사용자 요청: \"" + userMessage + "\"\n" +
                    "해당 요청에 맞는 상품을 3개 추천하세요. " +
                    "만약 특정 상품을 묻는 질문이라면, 해당 상품의 상세 정보를 제공하세요. " +
                    "만약 가격 조건(예: 1900원 이하)이 있다면, 반드시 그 가격 이하의 상품만 추천하세요.\n" +
                    "다른 설명 없이 JSON 형식으로만 응답해야 합니다.\n" +
                    "예제:\n" +
                    "[\n" +
                    "  {\"name\": \"콜라\", \"price\": 1500, \"description\": \"탄산이 톡 쏘는 시원한 콜라\", \"image\": \"URL\"},\n" +
                    "  {\"name\": \"비타500\", \"price\": 1200, \"description\": \"비타민 C가 풍부한 피로회복 음료\", \"image\": \"URL\"}\n" +
                    "]\n" +
                    "설명하지 말고 반드시 JSON 형식으로만 응답하세요.";

            // 4️⃣ Call GPT API
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "gpt-3.5-turbo");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "당신은 쇼핑 도우미입니다. 반드시 JSON 형식으로만 응답하세요."));
            messages.add(Map.of("role", "user", "content", prompt));

            requestMap.put("messages", messages);
            requestMap.put("max_tokens", 300);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions", 
                HttpMethod.POST, 
                entity, 
                Map.class
            );

            // 5️⃣ Process GPT response
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");
            String aiResponse = (String) messageMap.get("content");

            // Convert GPT JSON response to List
            List<Map<String, Object>> recommendedProducts = objectMapper.readValue(aiResponse, new TypeReference<List<Map<String, Object>>>() {});

            // ✅ Save recommendations to prevent duplicates
            previousRecommendations.clear();
            for (Map<String, Object> product : recommendedProducts) {
                previousRecommendations.add((String) product.get("name"));
            }

            return recommendedProducts;

        } catch (Exception e) {
            System.out.println("AI 추천 처리 오류: " + e.getMessage());
            e.printStackTrace();
            return List.of(Map.of("name", "추천 상품 없음", "description", "상품 추천에 실패했습니다."));
        }
    }

    /**
     * 🔥 Extracts price limit from user input (e.g., "1900원 이하로 추천해줘")
     */
    private int extractPriceLimit(String userMessage) {
        try {
            if (userMessage.matches(".*?(\\d{3,4})원.*")) {
                return Integer.parseInt(userMessage.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            System.out.println("가격 제한 추출 오류: " + e.getMessage());
        }
        return -1; // No price limit
    }
}

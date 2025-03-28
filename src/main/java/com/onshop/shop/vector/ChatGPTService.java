package com.onshop.shop.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductRepository;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String openAiApiKey;
    private final ProductRepository productRepository;

    
    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini"; //"gpt-3.5-turbo"; // 또는 "gpt-4-turbo"

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 자연어 → 구조화된 쿼리 문자열 변환
     */
    public String rewriteToStructuredQuery(String userPrompt) {
    	String systemPrompt = """
    			사용자의 자연어 요청을 다음과 같은 형식으로 바꿔줘:
    			[키워드묶음 추천개수 평균가격 표준편차]

    			📌 규칙:
    			- 추천개수, 평균가격, 표준편차가 없으면 각각 3, 0, 0으로 채워줘
    			- 추천 개수의 최대치는 5개야. 만약 사용자가 100개 추천해 달라고 요청해도 5만 적어줘
    			- 반드시 총 4개의 항목이 공백으로 구분된 한 줄 문자열로 출력해
    			- 출력은 오직 변환된 문장만. 여는 말이나 설명은 절대 하지 마
    			- 너의 판단 하에 사용자의 요청에서 중요할 핵심이 될 키워드를 3~8회 단순 반복해 주고
    			사용자의 요청의 핵심 키워드와 유사한 연관성이 큰 키워드(쇼핑몰 사용자가 검색하는 맥락에서 유의어)의 나열을 통해 강화해줘
    			

    			예시입력1 : '건강한 단백질 5개 추천해줘 대충 50000원대로..?'  
    			예시출력1 : '건강 건강 건강 건강 단백질 단백질 단백질 단백질 헬스 프로틴 쉐이크 5 50000 3000'

    			예시입력2 : '단백질 쉐이크 100개 추천해줘'  
    			예시출력2 : '단백질 단백질 단백질 쉐이크 쉐이크 건강 헬스 프로틴 5 0 0'

    			예시입력3 : '노트북 고성능 추천해줘'  
    			예시출력3 : '노트북 노트북 노트북 고성능 고성능 게이밍 빠른 최신형 3 0 0'

    			예시입력4 : '가성비좋고 고성능인데 튼튼한 노트북 추천해줘'  
    			예시출력4 : '가성비 가성비 고성능 고성능 내구성 내구성 튼튼한 노트북 3 0 0'

    			예시입력5 : '면역력 올려주는 거 추천'  
    			예시출력5 : '면역력 면역력 건강 건강 이뮨 유산균 비타민 3 0 0'

    			예시입력6 : '운동용 보충제 5개 추천해줘'  
    			예시출력6 : '운동 운동 보충제 보충제 보충제 프로틴 단백질 헬스 체력 5 0 0'

    			예시입력7 : '최대 7만원대 가격대로 가성비 좋은 제품 추천해줘'  
    			예시출력7 : '가성비 가성비 저렴한 효율 가격 제품 3 70000 0'

    			예시입력8 : '다이어트에 도움되는 간식 추천해줘!'  
    			예시출력8 : '다이어트 다이어트 다이어트 간식 간식 건강 식이섬유 저칼로리 3 0 0'

    			예시입력9 : '최신형이고 빠른 노트북 추천해줘 1000000원 안 넘게'  
    			예시출력9 : '노트북 노트북 최신형 최신형 빠른 빠른 성능 가성비 3 1000000 0'
    			
    			- 절대 문장을 만들지 말고 키워드만 공백으로 나열해
    			
    			""";

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", 0.2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GPT_API_URL, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            log.info("💬 GPT 응답: {}", content);
            return content.trim();
        } catch (Exception e) {
            log.error("❌ GPT API 요청 실패: {}", e.getMessage());
            return "안댔서..ㅠ"; // fallback
        }
    }
    
    
    public List<Map<String, Object>> rerankWithGpt(List<Long> productIds, String originalQuery) {
        List<Product> products = productRepository.findAllById(productIds);

        StringBuilder sb = new StringBuilder();
        sb.append("아래는 사용자의 요청과 추천 가능한 상품들입니다."
        		+ "사용자 요청에 가장 적합한 순서로 3~5개를 골라 추천하고"
        		+ "각 추천의 이유도 간단히 설명해줘.\n");
        sb.append("사용자 요청: ").append(originalQuery).append("\n\n");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append("[상품 ").append(i + 1).append("]\n");
            sb.append("상품명: ").append(p.getName()).append("\n");
            sb.append("설명: ").append(p.getDescription()).append("\n");
            sb.append("가격: ").append(p.getPrice()).append("원\n\n");
        }
        sb.append("단, 사용자의 요청에서 핵심이 되는 제품분류군 하나를 골라서 그 분류와 적합한것을 우선적으로 추천해"
        		+ "예시 : 운동 후 먹을 단백질 제품 2개 추천해달라는 요청 [단백질 볶음밥, 단백질 보충제, 프로틴 쉐이크]"
        		+ "중에서 볶음밥은 보충제 종류가 아니므로 추천에서 제외해"
        		+ "추천할 만한 상품이 없다면 아무것도 추천하지마"
        		+ "또한 추천이유는 한줄 이내의 간결한 코멘트로 표현해."
        		
        		);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", List.of(
                Map.of("role", "user", "content", sb.toString())
        ));
        body.put("temperature", 0.3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GPT_API_URL, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            log.info("\n🧠 GPT 재정렬 응답: {}", content);

            List<Map<String, Object>> result = new ArrayList<>();
            for (String line : content.split("\\n")) {
                if (!line.isBlank()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("result", line.trim());
                    result.add(map);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("❌ GPT 재정렬 실패: {}", e.getMessage());
            return List.of(Map.of("error", "GPT 응답 실패"));
        }
    }
}
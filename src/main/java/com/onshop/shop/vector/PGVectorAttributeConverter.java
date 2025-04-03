package com.onshop.shop.vector;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// 일단 돌아가게 만들어둔 얼레벌레 코드

@Converter
public class PGVectorAttributeConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] vector) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i != vector.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString(); // ✅ PostgreSQL vector 타입과 호환되는 형식 [0.1, 0.2, ...]
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.length() < 2) {
            return new float[0];
        }

        // 양쪽 대괄호 제거
        String content = dbData.substring(1, dbData.length() - 1);
        String[] parts = content.split(",");

        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                vector[i] = Float.parseFloat(parts[i].trim());
            } catch (NumberFormatException e) {
                vector[i] = 0f; // 파싱 실패 시 0으로 처리
            }
        }

        return vector;
    }

    // ✅ 임베딩을 벡터 문자열로 바꾸는 유틸 메서드 (직접 호출용)
    public static String toPgVectorString(float[] vector) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i != vector.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    public static float[] fromPgVectorString(String pgVector) {
        // 입력 형식이 '(0.1, 0.2, 0.3, ...)' 형태라고 가정
        if (pgVector == null || pgVector.isEmpty()) {
            return new float[0];
        }

        String trimmed = pgVector.replaceAll("[()]", ""); // 괄호 제거
        String[] parts = trimmed.split(",");
        float[] vector = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }

        return vector;
    }
}

package com.onshop.shop.user;

public class FaceData {
    private String faceToken;
    private String userId;

    // 기본 생성자
    public FaceData() {}

    // 매개변수 있는 생성자
    public FaceData(String faceToken, String userId) {
        this.faceToken = faceToken;
        this.userId = userId;
    }

    // Getter 및 Setter
    public String getFaceToken() {
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "FaceData{" +
                "faceToken='" + faceToken + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}

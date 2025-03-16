package com.onshop.shop.user;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/face")
@CrossOrigin(origins = "http://localhost:5173") // React와 연결
public class FaceRecognitionController {

    private final FaceRecognitionService faceRecognitionService;

    public FaceRecognitionController(FaceRecognitionService faceRecognitionService) {
        this.faceRecognitionService = faceRecognitionService;
    }

    @PostMapping("/add-face")
    public String addFace(@RequestBody Map<String, String> requestData) {
        String imageData = requestData.get("base64Image");
        String name = requestData.get("name");

        if (imageData == null || name == null) {
            return "Error: Missing parameters";
        }

        return faceRecognitionService.addFace(imageData, name);
    }
}

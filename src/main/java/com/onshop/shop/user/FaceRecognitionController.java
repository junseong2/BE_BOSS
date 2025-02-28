package com.onshop.shop.user;

import com.onshop.shop.user.FaceRecognitionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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

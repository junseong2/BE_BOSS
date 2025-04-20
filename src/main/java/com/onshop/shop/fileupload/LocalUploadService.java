package com.onshop.shop.fileupload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Profile("dev") // 개발 환경에서만 사용
@Slf4j
public class LocalUploadService implements FileUploadService{
	
    @Value("${file.upload.url}")
    private String uploadDir;
	
	
	@Override
	public String upload(MultipartFile file) {
            String name = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 랜덤 파일명 생성
            String imageUrl = uploadDir + name;
            
                    
            // 파일을 서버에 저장하는 로직
            File fileDir = new File(imageUrl);
            try {
            	
                Files.createDirectories(Paths.get(uploadDir)); // 디렉토리 자동 생성
                file.transferTo(fileDir); // 이미지 저장
                
                
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            return name;
	}

}

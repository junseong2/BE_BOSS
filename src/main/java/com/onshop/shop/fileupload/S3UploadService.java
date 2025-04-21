package com.onshop.shop.fileupload;


import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class S3UploadService implements FileUploadService {

    private final S3Client s3Client;

    @Value("${upload.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file) {
    	log.info("현재 업로드 버킷:{}", bucket);
        try {
            String key = UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return  key; // 버켓 주소 반환(이걸 데이터베이스에 저장하면 됨)
        } catch (IOException e) {
            throw new RuntimeException("S3 upload failed", e);
        }
    }
}
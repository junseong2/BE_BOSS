package com.onshop.shop.fileupload;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Profile("prd") // 배포 환경에서만 사용
public class S3UploadService implements FileUploadService {

		@Override
		public String upload(MultipartFile image) {
			// TODO Auto-generated method stub
			return null;
		}

}

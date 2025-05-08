package com.onshop.shop.global.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
	
	
	String upload(MultipartFile image); 

}

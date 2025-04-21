package com.onshop.shop.fileupload;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
	
	
	String upload(MultipartFile image); 

}

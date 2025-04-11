package com.onshop.shop.crawl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.product.Product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CrawlerService {

    private static final String UPLOAD_DIR = "C:/uploads/";

    public void handleCrawledImages(List<MultipartFile> images, Product product, String uuid) throws IOException {
        if (images == null || images.isEmpty()) return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) uploadFolder.mkdirs();

        List<String> savedFilenames = new ArrayList<>();

        for (MultipartFile file : images) {
            String originalName = file.getOriginalFilename();
            if (originalName == null) continue;

            String newName = timestamp + "_" + originalName;
            File dest = new File(UPLOAD_DIR + newName);
            file.transferTo(dest);
            savedFilenames.add(newName);
        }

        product.setGImage(String.join(",", savedFilenames));
        product.setCreatedRegister(LocalDateTime.now());

        // ✅ 마지막에 임시 폴더 삭제
        File crawlFolder = new File("C:/Crawl/" + uuid);
        deleteDirectory(crawlFolder);
    }

    private void deleteDirectory(File dir) {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file); // 재귀
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}

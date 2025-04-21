package com.onshop.shop.crawl;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

@Service
public class AliCrawlerService {
	
	
	@Value("${ali.crawl.driver-path}")
	private String driver;
	
	@Value("${ali.crawl.base-path}")
	private String tempImgPath;

    public CrawledProductDto crawl(String url) {
        System.setProperty("webdriver.chrome.driver", driver);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--start-maximized");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        String uuid = UUID.randomUUID().toString();
        Path basePath = Paths.get(tempImgPath +"/" + uuid);
        Path imagePath = basePath.resolve("image");
        Path descPath = basePath.resolve("desc");
        Path dataFile = basePath.resolve("data.txt");

        try {
            Files.createDirectories(imagePath);
            Files.createDirectories(descPath);

            driver.get(url);

            // 상품 정보 버튼 클릭
            try {
                WebElement infoBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[title='상품 정보']")));
                infoBtn.click();
            } catch (Exception e) {
                System.out.println("상품 정보 버튼 없음 or 클릭 실패");
            }

            // 더보기 버튼 클릭
            try {
                WebElement moreBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".extend--btn--TWsP5SV")));
                moreBtn.click();
            } catch (Exception e) {
                System.out.println("더보기 버튼 없음 or 클릭 실패");
            }

            // 상품명
            String title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1[data-pl='product-title']"))).getText();

            // 가격 정보
            String discountRate = "";
            String discountedPrice = "";
            String originalPriceText = "";
            int price = 0;

            try {
                discountedPrice = driver.findElement(By.cssSelector(".price--currentPriceText--V8_y_b5")).getText(); // ₩1,511
            } catch (Exception ignored) {}

            try {
                discountRate = driver.findElement(By.cssSelector(".price--discount--Y9uG2LK")).getText(); // 20%
            } catch (Exception ignored) {}

            try {
                originalPriceText = driver.findElement(By.cssSelector(".price--originalText--gxVO5_d")).getText(); // ₩1,906
                price = parsePrice(originalPriceText);
            } catch (Exception ignored) {}

            // 설명 텍스트 구성
            StringBuilder descriptionBuilder = new StringBuilder();
            if (!discountRate.isEmpty()) {
                descriptionBuilder.append("할인율: ").append(discountRate).append("\n");
            }
            if (!discountedPrice.isEmpty()) {
                descriptionBuilder.append("할인가격: ").append(discountedPrice).append("\n");
            }
            descriptionBuilder.append("\n");
            try {
                WebElement descElement = driver.findElement(By.cssSelector("#product-description"));
                descriptionBuilder.append(descElement.getText());
            } catch (Exception ignored) {}

            // 이미지 저장 리스트
            List<String> imageNames = new ArrayList<>();

            // 캐러셀 썸네일 이미지들 → 고해상도로 변환 저장
            List<WebElement> thumbImgs = driver.findElements(By.cssSelector(".slider--item--FefNjlj img"));
            int mainIndex = 0;
            for (WebElement img : thumbImgs) {
                String thumbUrl = img.getAttribute("src");
                if (thumbUrl != null && !thumbUrl.isEmpty()) {
                    String highResUrl = thumbUrl.replaceAll("(?<=_)220x220[^.]*", "960x960q75"); // 220x220 → 960x960q75
                    String filename = "main_" + mainIndex++ + ".jpg";
                    saveImage(highResUrl, imagePath.resolve(filename));
                    imageNames.add(filename);
                }
            }

            // 대표 확대 이미지 저장
            try {
                WebElement zoomed = driver.findElement(By.cssSelector(".magnifier--image--EYYoSlr"));
                String zoomUrl = zoomed.getAttribute("src");
                if (zoomUrl != null && !zoomUrl.isEmpty()) {
                    saveImage(zoomUrl, imagePath.resolve("main_cover.jpg"));
                    imageNames.add("main_cover.jpg");
                }
            } catch (Exception e) {
                System.out.println("대표 이미지(확대용) 추출 실패: " + e.getMessage());
            }

            // 설명 이미지 저장
            List<WebElement> descImgs = driver.findElements(By.cssSelector("#product-description img"));
            int index = 0;
            for (WebElement img : descImgs) {
                String imgUrl = img.getAttribute("src");
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    String filename = "desc_" + index++ + ".jpg";
                    saveImage(imgUrl, descPath.resolve(filename));
                    imageNames.add(filename);
                }
            }

            // 텍스트 파일 저장
            try (BufferedWriter writer = Files.newBufferedWriter(dataFile)) {
                writer.write("상품명: " + title + "\n");
                writer.write("원가: " + originalPriceText + "\n");
                if (!discountRate.isEmpty()) writer.write("할인율: " + discountRate + "\n");
                if (!discountedPrice.isEmpty()) writer.write("할인가격: " + discountedPrice + "\n");
                writer.write("\n설명:\n" + descriptionBuilder);
            }

            return new CrawledProductDto(
                    title,
                    descriptionBuilder.toString(),
                    price,
                    imageNames,
                    uuid
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new CrawledProductDto("알리 상품명 파싱 실패", "", 0, new ArrayList<>(), "error");
        } finally {
            driver.quit();
        }
    }

    private void saveImage(String url, Path outputPath) {
        try (InputStream in = new java.net.URL(url).openStream()) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("이미지 저장 실패: " + url);
        }
    }

    private int parsePrice(String priceText) {
        try {
            return Integer.parseInt(priceText.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}


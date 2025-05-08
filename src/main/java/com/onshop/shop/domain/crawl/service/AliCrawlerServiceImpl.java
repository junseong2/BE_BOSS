package com.onshop.shop.domain.crawl.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.onshop.shop.domain.crawl.dto.CrawledProductDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link AliCrawlerService}의 구현체로,
 * Selenium WebDriver를 사용하여 알리익스프레스 상품 상세 페이지에서
 * 제목, 가격, 설명 및 이미지 정보를 크롤링합니다.
 */
@Slf4j
@Service
public class AliCrawlerServiceImpl implements AliCrawlerService {

    @Value("${ali.crawl.driver-path}")
    private String driverPath;

    @Value("${ali.crawl.base-path}")
    private String tempImgPath;

    /**
     * 주어진 알리익스프레스 URL로부터 상품 정보를 크롤링합니다.
     *
     * @param url 알리익스프레스 상품 URL
     * @return 크롤링된 {@link CrawledProductDTO}
     */
    @Override
    public CrawledProductDTO crawl(String url) {
        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--start-maximized");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        String uuid = UUID.randomUUID().toString();
        Path basePath = Paths.get(tempImgPath, uuid);
        Path imagePath = basePath.resolve("image");
        Path descPath = basePath.resolve("desc");
        Path dataFile = basePath.resolve("data.txt");

        try {
            Files.createDirectories(imagePath);
            Files.createDirectories(descPath);

            driver.get(url);

            // 상품 정보 탭 클릭
            try {
                WebElement infoBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[title='상품 정보']")));
                infoBtn.click();
            } catch (Exception e) {
                log.info("상품 정보 버튼 없음 or 클릭 실패");
            }

            // 더보기 클릭
            try {
                WebElement moreBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".extend--btn--TWsP5SV")));
                moreBtn.click();
            } catch (Exception e) {
                log.info("더보기 버튼 없음 or 클릭 실패");
            }

            String title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1[data-pl='product-title']"))).getText();

            String discountRate = "";
            String discountedPrice = "";
            String originalPriceText = "";
            int price = 0;

            try {
                discountedPrice = driver.findElement(By.cssSelector(".price--currentPriceText--V8_y_b5")).getText();
            } catch (Exception ignored) {}

            try {
                discountRate = driver.findElement(By.cssSelector(".price--discount--Y9uG2LK")).getText();
            } catch (Exception ignored) {}

            try {
                originalPriceText = driver.findElement(By.cssSelector(".price--originalText--gxVO5_d")).getText();
                price = parsePrice(originalPriceText);
            } catch (Exception ignored) {}

            StringBuilder descriptionBuilder = new StringBuilder();
            if (!discountRate.isEmpty()) descriptionBuilder.append("할인율: ").append(discountRate).append("\n");
            if (!discountedPrice.isEmpty()) descriptionBuilder.append("할인가격: ").append(discountedPrice).append("\n").append("\n");

            try {
                WebElement descElement = driver.findElement(By.cssSelector("#product-description"));
                descriptionBuilder.append(descElement.getText());
            } catch (Exception ignored) {}

            List<String> imageNames = new ArrayList<>();

            // 썸네일 이미지 고해상도로 저장
            List<WebElement> thumbImgs = driver.findElements(By.cssSelector(".slider--item--FefNjlj img"));
            int mainIndex = 0;
            for (WebElement img : thumbImgs) {
                String thumbUrl = img.getAttribute("src");
                if (thumbUrl != null && !thumbUrl.isEmpty()) {
                    String highResUrl = thumbUrl.replaceAll("(?<=_)220x220[^.]*", "960x960q75");
                    String filename = "main_" + mainIndex++ + ".jpg";
                    saveImage(highResUrl, imagePath.resolve(filename));
                    imageNames.add(filename);
                }
            }

            // 확대 대표 이미지 저장
            try {
                WebElement zoomed = driver.findElement(By.cssSelector(".magnifier--image--EYYoSlr"));
                String zoomUrl = zoomed.getAttribute("src");
                if (zoomUrl != null && !zoomUrl.isEmpty()) {
                    saveImage(zoomUrl, imagePath.resolve("main_cover.jpg"));
                    imageNames.add("main_cover.jpg");
                }
            } catch (Exception e) {
                log.info("대표 이미지(확대용) 추출 실패: {}", e.getMessage());
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

            // 텍스트 정보 저장
            try (BufferedWriter writer = Files.newBufferedWriter(dataFile)) {
                writer.write("상품명: " + title + "\n");
                writer.write("원가: " + originalPriceText + "\n");
                if (!discountRate.isEmpty()) writer.write("할인율: " + discountRate + "\n");
                if (!discountedPrice.isEmpty()) writer.write("할인가격: " + discountedPrice + "\n");
                writer.write("\n설명:\n" + descriptionBuilder);
            }

            return new CrawledProductDTO(title, descriptionBuilder.toString(), price, imageNames, uuid);

        } catch (Exception e) {
            log.error("크롤링 실패", e);
            return new CrawledProductDTO("알리 상품명 파싱 실패", "", 0, new ArrayList<>(), "error");
        } finally {
            driver.quit();
        }
    }

    /**
     * 이미지 URL을 받아 해당 이미지를 지정된 경로에 저장합니다.
     *
     * @param url        이미지 URL
     * @param outputPath 저장 경로
     */
    private void saveImage(String url, Path outputPath) {
        try (InputStream in = new java.net.URL(url).openStream()) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("이미지 저장 실패: {}", url);
        }
    }

    /**
     * 문자열에서 숫자만 추출하여 가격(int)으로 반환합니다.
     *
     * @param priceText ₩1,234 형식 문자열
     * @return 숫자만 추출한 정수 가격
     */
    private int parsePrice(String priceText) {
        try {
            return Integer.parseInt(priceText.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}

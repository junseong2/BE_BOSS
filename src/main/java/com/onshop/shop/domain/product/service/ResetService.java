package com.onshop.shop.domain.product.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


// TODO: 쓰는거 아니라면 제거(YoungWan Kim)
@Service
public class ResetService {

    // 매일 자정에 실행 (일간 초기화)
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyData() {
        // 일간 데이터 초기화 로직
    }

    // 매주 월요일 자정에 실행 (주간 초기화)
    @Scheduled(cron = "0 0 0 * * MON")
    public void resetWeeklyData() {
        // 주간 데이터 초기화 로직
    }

    // 매월 1일 자정에 실행 (월간 초기화)
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyData() {
        // 월간 데이터 초기화 로직
    }
}

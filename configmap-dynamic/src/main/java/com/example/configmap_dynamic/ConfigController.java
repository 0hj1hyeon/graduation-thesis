package com.example.configmap_dynamic;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RefreshScope
@RestController
public class ConfigController {

    @Value("${myValue}")
    private String myValue;

    @Value("${jobCron}")
    private String jobCron;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        myValue = "before";
        jobCron = "*/5 * * * *";
        return Map.of(
                "myValue", myValue,
                "jobCron", jobCron
        );
    }
    @Scheduled(fixedDelay = 3000) // 5초마다 출력
    public void printConfigValue() {
        System.out.println("현재 설정 값: " + myValue);
    }
}


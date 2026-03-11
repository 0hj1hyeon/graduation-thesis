package com.example.configmap_dynamic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RefreshScope
@RestController
public class ConfigController {

    // 1. 동적 값 (ConfigMap에서 관리)
    @Value("${myValue:default-dynamic}")
    private String myValue;

    // 2. 정적 값 (application.properties에 고정)
    @Value("${static.message:This-Is-Static}")
    private String staticMessage;

    @Value("${jobCron}")
    private String jobCron;

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return Map.of(
                "dynamic_myValue", myValue,     // 실험 대상 (실시간 변경)
                "static_message", staticMessage, // 대조군 (재시작 전까지 고정)
                "jobCron", jobCron
        );
    }

    @Scheduled(fixedDelay = 3000)
    public void printConfigValue() {
        // 로그를 통해 실시간으로 변하는지 관찰합니다.
        System.out.println("동적 설정 값: " + myValue + " | 정적 설정 값: " + staticMessage);
    }
}
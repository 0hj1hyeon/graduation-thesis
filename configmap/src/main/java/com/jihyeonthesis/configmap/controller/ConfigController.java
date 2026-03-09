package com.jihyeonthesis.configmap.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RefreshScope
@RestController
public class ConfigController {

    @Value("${my.value:defaultValue}")
    private String myValue;

    @Value("${job.cron:defaultCron}")
    private String jobCron;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of(
                "my.value", myValue,
                "job.cron", jobCron
        );
    }
}
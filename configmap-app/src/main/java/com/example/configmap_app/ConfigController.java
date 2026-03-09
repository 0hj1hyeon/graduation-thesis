package com.example.configmap_app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigController {

    @Value("${my.value}")
    private String myValue;

    @Value("${job.cron}")
    private String jobCron;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("myValue", myValue);
        config.put("jobCron", jobCron);
        return config;
    }
}
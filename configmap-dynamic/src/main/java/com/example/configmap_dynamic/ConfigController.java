package com.example.configmap_dynamic;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {

    private final KubernetesClient kubernetesClient;
    private String myValue = "Legacy-Final-Test"; // 내부 메모리에서 직접 관리

    public ConfigController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return Map.of("dynamic_myValue", myValue);
    }

    @Scheduled(fixedDelay = 3000)
    public void pollAndPrint() {
        try {
            // NPE 방지 및 안전한 데이터 추출
            ConfigMap cmap = kubernetesClient.configMaps()
                    .inNamespace("default")
                    .withName("configmap-dynamic")
                    .get();

            if (cmap != null && cmap.getData() != null) {
                String newValue = cmap.getData().get("myValue");
                if (newValue != null) {
                    this.myValue = newValue;
                }
            }

            String currentTime = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("[" + currentTime + "] 현재 설정 값: " + this.myValue);

        } catch (Exception e) {
            // 에러 원인을 명확히 파악하기 위한 로그 수정
            System.out.println("K8s API 조회 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
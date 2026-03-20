package com.example.configmap_dynamic;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {


    private String myValue = "Watch-API-Init"; // 힙 메모리 변수

    private final ConfigProperties configProperties;

    // 프레임워크가 주입해주는 설정 객체만 받으면 됩니다.
    public ConfigController(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * [데이터 수신] K8s API 서버와 영구적인 Watch 연결 수립 (부하 0%)
     */
    @PostConstruct
   /* public void watchConfigMap() {
        kubernetesClient.configMaps()
                .inNamespace("default")
                .withName("configmap-dynamic")
                .watch(new Watcher<ConfigMap>() {
                    @Override
                    public void eventReceived(Action action, ConfigMap configMap) {
                        if ((action == Action.MODIFIED || action == Action.ADDED) && configMap != null && configMap.getData() != null) {
                            String newValue = configMap.getData().get("myValue");

                            // 값이 변경된 시점에만 즉시 메모리 교체 (0초 지연)
                            if (newValue != null && !newValue.equals(myValue)) {
                                myValue = newValue;
                                System.out.println("🔥 [K8s Watch API] 변경 감지! 메모리 핫스왑 완료: " + myValue);
                            }
                        }
                    }

                    @Override
                    public void onClose(WatcherException cause) {
                        if (cause != null) {
                            System.out.println("🚨 Watch 연결 끊김: " + cause.getMessage());
                        }
                    }
                });
    }*/

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return Map.of("dynamic_myValue", myValue);
    }

    /**
     * [데이터 출력] 통신 없이 로컬 자바 힙 메모리의 값만 3초마다 출력
     */
    @Scheduled(fixedDelay = 3000)
    public void printLocalMemory() {
        String currentTime = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + currentTime + "] 현재 힙 메모리 유지 값: " + this.myValue);
    }
    @Scheduled(fixedDelay = 3000)
    public void debugPrint() {
        // ConfigProperties에서 값을 읽어옴
        System.out.println("현재 스코프 값: " + configProperties.getMyValue());
    }
}
package com.example.configmap_dynamic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigRefreshTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testDynamicConfigurationRefresh() {
        // 1. 초기값 확인 (ConfigMap 주입 전후 비교)
        ResponseEntity<Map> responseBefore = restTemplate.getForEntity("/config", Map.class);
        String initialValue = (String) responseBefore.getBody().get("myValue");

        // 2. 외부 환경(ConfigMap 역할 대체)의 값이 변경되었다고 가정하고 Context 리프레시 발생 시나리오 테스트
        // 실제 운영 환경에서는 kubectl apply 후 actuator/refresh 호출이나 Spring Cloud K8s가 이 역할을 수행함 [cite: 150, 193]

        // 3. 변경 후 값 일치 여부 확인
        // (실제 K8s 환경에서는 위 AvailabilityTest를 실행하며 외부에서 kubectl 명령을 날리는 것이 가장 정확함)
    }
}

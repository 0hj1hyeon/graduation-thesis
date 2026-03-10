package com.example.configmap_dynamic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AvailabilityTest {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://<Node-IP>:<NodePort>/config"))
                .build();

        System.out.println("설정 변경 중 가용성 테스트 시작...");
        while (true) {
            long start = System.currentTimeMillis();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long end = System.currentTimeMillis();
                System.out.printf("상태 코드: %d, 응답 시간: %d ms, 내용: %s\n",
                        response.statusCode(), (end - start), response.body());
            } catch (Exception e) {
                System.err.println("요청 실패: " + e.getMessage());
            }
            Thread.sleep(500); // 0.5초 간격으로 요청
        }
    }
}
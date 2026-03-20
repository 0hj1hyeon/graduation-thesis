# 스프링 클라우드와 쿠버네티스 기반 설정 관리 최적화
> **Configuration Management Optimization with Spring Cloud and Kubernetes**

클라우드 네이티브 환경에서 애플리케이션 재시작 없이 설정값을 실시간으로 반영하는 무중단 동적 설정 관리(Dynamic Configuration) 아키텍처를 제안하고, 기존 정적 방식 및 프레임워크 기본 기능의 한계를 분석하여 최적화된 우회 구조를 구현한 프로젝트

---

## 🛠 기술 스택 (Tech Stack)
* **Framework**: Spring Boot 3.x, Spring Cloud Kubernetes
* **Infrastructure**: Kubernetes (Minikube), Docker
* **Library**: Fabric8 Kubernetes Client (Watch API 통신용)
* **Language**: Java 17
* **Build Tool**: Gradle

---

## 💡 프로젝트 개요 (Abstract)
* **연구 배경**: 기존 정적 설정 방식은 ConfigMap 값 변경 시 애플리케이션 파드(Pod)의 재시작이 필수적이며, 이는 평균 30초 이상의 서비스 중단과 세션 유실을 초래함.
* **1차 개선 (대중적 프레임워크 도입)**: 파드 재시작을 막기 위해 `Spring Cloud Kubernetes`의 기본 기능인 폴링(Polling) 엔진과 `@RefreshScope`를 적용하여 1차 무중단 갱신을 구현함. 그러나 이 방식은 K8s API 서버에 지속적인 HTTP 요청 부하를 일으키고, 객체 재생성에 따른 애플리케이션 내부 오버헤드가 발생함을 확인.
* **2차 최종 개선 (독자적 Watch API 아키텍처 구현)**: 프레임워크의 불안정한 폴링 엔진을 걷어내고, K8s Native 통신 방식인 **Watch API(Event-Driven Push)** 파이프라인을 직접 구축함. 이를 통해 네트워크 부하를 0%로 줄이고 타겟 변수만 즉시 교체하는 핫스왑(Hot-Swap)을 달성한 무중단 아키텍처를 완성함

---

## 🏗 시스템 아키텍처 진화 (Architecture Evolution)

### 1. AS-IS (동적 설정 미적용)
* **방식**: ConfigMap 수정 후 파드를 강제 재시작 (`kubectl rollout restart`).
* **한계**: 프로세스 종료 및 재기동 기간 동안 트래픽 처리가 불가능하며, 서비스 가용성이 훼손됨.

### 2. TO-BE : 개선안 A (Polling + RefreshScope)
* **방식**: 백그라운드 스레드가 5초 주기로 K8s API 서버를 조회(Polling)하여 변경을 감지하고, 스프링 빈(Bean) 객체를 통째로 파괴 후 재생성함.
* **한계**: 서비스 무중단은 달성했으나, 무의미한 K8s API 서버 네트워크 트래픽 부하가 지속적으로 발생함.

### 3. TO-BE : 개선안 B (Watch API + Hot-Swap) [최종 아키텍처]
* **방식**: K8s 마스터 노드와 영구적인 단일 연결을 유지하며 변경 이벤트를 즉시 밀어받는(Push) 구조. 이벤트를 수신한 자바 애플리케이션은 무거운 객체 재생성 없이 타겟 메모리 변수만 0초 지연으로 핫스왑(Hot-Swap) 처리함.
* **성과**: 완벽한 무중단 달성 및 인프라 통신 부하 전면 제거.

---

## 📊 실험 데이터 (Experimental Results)
기존 정적 방식과 2가지 무중단 개선안의 정량적 비교 결과입니다.

| 비교 항목 | [AS-IS] 미적용 (정적 설정) | [1차 개선] 무중단 Polling | [2차 최종] 무중단 Watch API |
| :--- | :--- | :--- | :--- |
| **서비스 가용성** | **중단 발생** (파드 재시작) | **무중단** | **무중단** |
| **설정 반영 시간** | 평균 32.3초 소요 | 0 ~ 5초 지연 (주기 의존) | **0초 (이벤트 즉시 반영)** |
| **K8s API 트래픽 부하** | 없음 | 5초당 1회 무조건 호출 | **0회 (Push 수신 대기)** |
| **애플리케이션 비용** | 프로세스 전체 재기동 | 무거운 Bean 객체 재생성 | **객체 보존, 타겟 메모리 핫스왑** |
| **운영자 개입** | 롤아웃 재배포 파이프라인 가동 | 스케줄러 자동 감지 반영 | **Push 이벤트 즉시 자동 반영** |

---

## 🔍 트러블슈팅 및 아키텍처 전환 과정 (Troubleshooting & Resolution)
단순한 프레임워크 설정(1차 개선안)에서 K8s Native Watch API(최종 개선안)로 시스템을 전면 재설계하게 된 핵심 디버깅 및 트러블슈팅 과정

### 1. Spring 감지 엔진(Detector) 정지 및 스레드 데드락 현상
* **Issue**: `Spring Cloud Kubernetes`의 자동 리로드 기능을 켰으나, K8s ConfigMap 값을 변경해도 애플리케이션이 이를 감지하지 못하고 리로드 로직 자체가 멈추는 현상 발생.
* **Root Cause**: 프레임워크 내부의 `ConfigurationChangeDetector`가 K8s API 서버를 지속적으로 조회(Polling)하는 과정에서, 기본 할당된 백그라운드 스레드 풀이 고갈되어 데드락(Deadlock)에 빠짐.
* **Resolution**: `application.properties`에 `spring.task.scheduling.pool.size=5`를 명시하여 스케줄러 스레드 풀을 확장, 감지 엔진의 안정성을 1차적으로 확보함.

### 2. 설정 우선순위 충돌(Override) 및 파이프라인 단절 추적
* **Issue**: 스레드 풀 확장 후에도 최종 단계인 객체 갱신(`@RefreshScope`)이 이루어지지 않는 무음 실패(Silent Failure) 발생.
* **Root Cause**: 상위 패키지의 로그 레벨이 `ERROR`로 고정되어 내부 디버그 단계를 확인할 수 없었으며, `application.properties`의 `reload.enabled=false` 옵션이 상위 설정을 무효화함.
* **Resolution**: 충돌 프로퍼티를 제거하고 `ConfigReloadUtil`의 로그 레벨을 `DEBUG`로 격상. 감지(Modified) -> 이벤트 발행(Refresh) -> 빈 재생성으로 이어지는 4단계 파이프라인을 가시화하여 정상 작동을 검증함.

### 3. 'Push 이벤트'에 대한 논리적 모순 발견 (최종 아키텍처 도입 계기)
* **Issue**: 1차 개선안 적용 후 내부 DEBUG 로그 분석 결과, `[TaskScheduler-1]` 스레드가 5초마다 K8s API 서버를 찌르고 `no changes found`를 반복 출력하는 것을 직접 확인.
* **Root Cause**: 애플리케이션 내부의 이벤트 처리는 Push 방식이나, 인프라 간 통신(K8s -> App)은 100% Polling 방식으로 되어 있어 심각한 네트워크 부하를 유발하고 있었음.
* **Resolution (Watch API 도입)**: 무거운 프레임워크 계층을 걷어내고, K8s 마스터 노드와 영구적 단일 연결을 맺어 실제 이벤트를 Push 받는 Native Watch API 기반의 2차 개선안을 독자적으로 구현하여 통신 부하를 원천 차단함.

---

## 실행결과 
기존 방식

<img width="945" height="92" alt="image" src="https://github.com/user-attachments/assets/8956ea45-0a0d-46a8-845d-c4d3f5edb3dd" />
<img width="581" height="117" alt="image" src="https://github.com/user-attachments/assets/837d94b7-b620-4fee-9bb2-2aeea611a62d" />

동적 방식

<img width="940" height="387" alt="image" src="https://github.com/user-attachments/assets/7464d936-d0cf-4bdc-9f1b-e885aacdc27d" />

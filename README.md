# 스프링 클라우드와 쿠버네티스 기반 설정 관리 최적화
> **Configuration Management Optimization with Spring Cloud and Kubernetes**

클라우드 네이티브 환경에서 애플리케이션 재시작 없이 설정값을 실시간으로 반영하는 무중단 동적 설정 관리(Dynamic Configuration) 아키텍처를 제안하고, 기존 정적 방식 및 프레임워크 기본 기능의 한계를 분석하여 최적화된 우회 구조를 구현한 프로젝트

---

## 프로젝트 개요 (Abstract)
* **연구 배경**: 기존 정적 설정 방식은 값 변경 시 애플리케이션 재시작이 필수적이며, 이는 서비스 중단 및 운영 복잡성을 초래함
* 초기 접근 및 한계: K8s ConfigMap과 Spring Cloud Kubernetes의 @RefreshScope를 결합하여 동적 반영을 시도했으나, 프레임워크 내부의 캐시 고정(Cache Freeze) 버그와 백그라운드 스레드 데드락 현상으로 인해 실시간 동기화에 실패
* 핵심 솔루션: 프레임워크의 불안정한 추상화 계층을 배제하고, KubernetesClient를 이용한 API 직접 폴링(Direct Polling) 아키텍처를 독자적으로 설계 및 구현
* 주요 성과: 설정 반영 시간을 기존 파드 재시작 방식(평균 32.4초) 대비 **최대 3초 이내로 단축(약 90% 이상 개선)**시켰으며, 변경 과정에서 파드(Pod) 재시작이 전혀 발생하지 않는 완벽한 무중단(Zero-Downtime) 상태를 입증

---

## 시스템 아키텍처 (Architecture)
단순히 외부의 갱신 이벤트를 수동적으로 기다리는 것이 아니라, 애플리케이션 내부 스케줄러가 능동적으로 K8s 인프라와 통신하여 메모리를 핫스왑(Hot-swap)하는 구조

* **In-bound**: 관리자가 K8s 클러스터의 ConfigMap 데이터를 수정 (kubectl edit).

* **Internal Polling**: 자바 애플리케이션 내 독립된 백그라운드 스케줄러가 3초 주기로 K8s API 서버를 직접 조회.

* **Hot-Swapping**: 변경된 데이터 감지 시, 애플리케이션 프로세스 종료 없이 내부 자바 힙 메모리의 변수만 즉시 업데이트.

* **Security**: 파드 내부에서 K8s API 서버 자원에 접근하기 위해 ServiceAccount 기반의 RBAC 권한 통제 프로토콜 적용.
---

## 기술 스택 (Tech Stack)
* **Framework**: Spring Boot 3.x, Spring Cloud Kubernetes
* **Infrastructure**: Kubernetes (Minikube), Docker
* **Language**: Java 17
* **Build Tool**: Gradle

---

## 실험 데이터 (Experimental Results)
기존 Spring 방식과 본 프로젝트의 ConfigMap 기반 방식의 정량적 비교 결과

| 비교 항목 | 기존 Spring 방식 (Pod 재시작) | 본 프로젝트 (API Direct Polling) | 성능 개선 요약 |
| :--- | :--- | :--- | :--- |
| **설정 반영 시간** | 평균 32,357.9ms | **최대 3,000ms 이내** | 약 90% 이상 시간 단축 |
| **서비스 가용성** | 파드 재시작에 따른 일시 중단 | **무중단 (Zero-Downtime)** | 세션 및 트래픽 유실 0% |
| **운영자 개입** | 재배포 파이프라인 가동 필수 | **스케줄러(3초 주기) 자동 반영** | 운영 오버헤드 원천 제거 |
| **메모리 상태** | 재시작 시 기존 힙 메모리 초기화 | **기존 메모리 상태 완벽 보존** | 프로세스 유지 및 Hot-Swap 증명 |

---
## 실행결과 
기존 방식

<img width="945" height="92" alt="image" src="https://github.com/user-attachments/assets/8956ea45-0a0d-46a8-845d-c4d3f5edb3dd" />
<img width="581" height="117" alt="image" src="https://github.com/user-attachments/assets/837d94b7-b620-4fee-9bb2-2aeea611a62d" />

동적 방식

<img width="940" height="387" alt="image" src="https://github.com/user-attachments/assets/7464d936-d0cf-4bdc-9f1b-e885aacdc27d" />

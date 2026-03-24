# 스프링 클라우드와 쿠버네티스 기반 설정 관리 최적화
> **Configuration Management Optimization with Spring Cloud and Kubernetes**

## 1. 개요

클라우드 네이티브 환경에서 ConfigMap 변경 시 애플리케이션 재시작 없이 설정값을 반영하는 무중단 동적 설정 관리(Dynamic Configuration) 구조를 구현하고,  
Spring Cloud Kubernetes 기반 방식과 Kubernetes Watch API 기반 방식을 각각 적용하여 두 접근 방식의 동작 방식과 한계를 비교 분석한 프로젝트입니다.

---

## 2. 브랜치 구조

본 프로젝트는 실험 단계별 구현을 브랜치 단위로 분리하여 관리합니다.

- `main`  
  전체 아키텍처 및 실험 결과를 정리한 브랜치

- `feature/polling-refresh`  
  Spring Cloud Kubernetes + `@RefreshScope` 기반 Polling 방식 구현

- `refactor/watch-api-optimization`  
  Fabric8 Kubernetes Client 기반 Watch API 방식 구현

---
## 3. 문제 정의

Kubernetes 환경에서 ConfigMap 변경 시 일반적으로 Pod 재시작이 필요합니다.

이 방식은 다음과 같은 문제를 발생시킵니다.

- 서비스 중단 발생
- 불필요한 Pod 재생성
- 운영 비용 증가

이 문제를 해결하기 위해 애플리케이션 재시작 없이 설정을 반영하는 구조를 설계하였습니다.

---

## 4. 아키텍처 진화 과정

### 4.1 기존 방식

- ConfigMap 변경 후 `kubectl rollout restart` 수행
- Pod 재생성 후 설정 반영

문제점
- 서비스 중단 발생
- 리소스 낭비

---

### 4.2 개선안 A - Polling 기반 방식

Spring Cloud Kubernetes + `@RefreshScope`

구현 내용
- 일정 주기로 ConfigMap 변경 여부 확인
- 변경 감지 시 Context refresh
- Bean 재생성을 통해 설정값 반영

특징
- 프레임워크 기반으로 구현이 간단함
- 안정적으로 동작

한계
- Polling 주기에 의존하여 즉시 반영이 어려움
- 주기적인 Kubernetes API 호출 발생
- Bean 재생성 비용 존재

---

### 4.3 개선안 B - Watch API 기반 방식

Fabric8 Kubernetes Client 기반 Watch API

구현 내용
- Kubernetes API 서버와 Watch 연결 유지
- ConfigMap 변경 이벤트를 실시간으로 수신
- 변경된 값을 애플리케이션 메모리에서 직접 교체

특징
- 이벤트 기반 구조로 Polling 제거
- 설정 변경 시 즉시 반영
- 객체 재생성 없이 값만 변경

---

## 5. 실험 및 비교

두 방식 모두 애플리케이션 재시작 없이 설정 반영에 성공하였으며, 각 방식은 다음과 같은 차이를 보였습니다.

| 항목 | Polling 방식 | Watch 방식 |
|------|-------------|------------|
| 반영 방식 | 주기적 조회 | 이벤트 기반 |
| 반영 지연 | Polling 주기 의존 | 즉시 반영 |
| API 호출 | 지속 발생 | 이벤트 발생 시 |
| Bean 재생성 | 있음 | 없음 |

---


## 트러블슈팅 및 아키텍처 전환 과정 (Troubleshooting & Resolution)
단순한 프레임워크 설정(1차 개선안)에서 K8s Native Watch API(최종 개선안)로 시스템을 전면 재설계하게 된 핵심 디버깅 및 트러블슈팅 과정

## 6. 트러블슈팅 및 아키텍처 전환 과정

단순한 프레임워크 기반 방식에서 K8s Native Watch API 방식으로 전환하게 된 핵심 문제 해결 과정

### 6.1 Spring 감지 엔진 정지 문제

문제  
- ConfigMap 변경 시 자동 리로드가 동작하지 않고 감지 로직이 중단됨  

원인  
- Spring Cloud Kubernetes 내부 Polling 과정에 스케줄러 스레드 풀이 부족하여 감지 지연 및 중단 발생  

해결  
- `spring.task.scheduling.pool.size` 설정을 통해 스레드 풀 확장  
- 감지 안정성 확보  

---

### 6.2 설정 반영 실패 (Silent Failure)

문제  
- 변경 감지는 되었으나 실제 설정값이 반영되지 않음  

원인  
- `reload.enabled=false` 설정으로 리로드 비활성화  
- 로그 레벨 제한으로 내부 동작 확인 불가  

해결  
- 충돌 설정 제거  
- DEBUG 로그 활성화를 통해 감지 → 이벤트 → 빈 재생성 흐름 검증  

---

### 6.3 Polling 구조의 비효율 문제 (아키텍처 전환 계기)

문제  
- 설정 변경이 없어도 K8s API 호출이 지속적으로 발생  

원인  
- 내부 이벤트 처리와 달리,  
  K8s → 애플리케이션 통신이 Polling 방식으로 동작  

해결  
- Kubernetes Watch API 기반 구조로 전환  
- 이벤트 발생 시에만 처리하도록 개선  
- 불필요한 API 호출 제거 및 반영 속도 개선
---
## 7. 결론

본 프로젝트에서는 Kubernetes 환경에서의 설정 관리 방식을 두 가지 접근으로 직접 구현하고 비교하였습니다.

- Polling 방식은 구현이 단순하고 안정적이지만 반영 속도와 API 호출 측면에서 한계가 존재합니다.

- Watch 방식은 이벤트 기반 구조를 통해 더 빠른 반영과 효율적인 운영이 가능함을 확인하였습니다.

---

## 8. 사용 기술

- Spring Boot 3.x  
- Spring Cloud Kubernetes  
- Fabric8 Kubernetes Client  
- Docker  
- Kubernetes (Minikube)  
- Java 17  
- Gradle  

---

## 실행결과 
기존 방식

<img width="945" height="92" alt="image" src="https://github.com/user-attachments/assets/8956ea45-0a0d-46a8-845d-c4d3f5edb3dd" />
<img width="581" height="117" alt="image" src="https://github.com/user-attachments/assets/837d94b7-b620-4fee-9bb2-2aeea611a62d" />

동적 방식

<img width="940" height="387" alt="image" src="https://github.com/user-attachments/assets/7464d936-d0cf-4bdc-9f1b-e885aacdc27d" />

# 스프링 클라우드와 쿠버네티스 기반 설정 관리 최적화
> **Configuration Management Optimization with Spring Cloud and Kubernetes**

 클라우드 네이티브 환경에서 애플리케이션 재시작 없이 설정값을 실시간으로 반영하는 **무중단 동적 설정 관리 구조**를 제안하고, 기존 방식과의 효율성을 비교 분석

---

## 프로젝트 개요 (Abstract)
* **연구 배경**: 기존 정적 설정 방식은 값 변경 시 애플리케이션 재시작이 필수적이며, 이는 서비스 중단 및 운영 복잡성을 초래함
* **핵심 솔루션**: Kubernetes ConfigMap과 Spring Cloud Kubernetes의 `@RefreshScope`를 결합하여 실시간 동적 설정 반영 구현
* **주요 성과**: 설정 반영 시간을 기존 대비 약 **72.7% 단축** (32.4초 → 8.8초) 및 서비스 가용성 확보

---

## 시스템 아키텍처 (Architecture)
사용자가 `ConfigMap`을 수정하면 `Spring Cloud Kubernetes`가 변경 이벤트를 감지하여, 재시작 없이 특정 빈(Bean)의 설정을 갱신하는 구조입니다.

1.  **Local/Build**: Docker를 통한 애플리케이션 이미지화
2.  **Kubernetes**: Minikube 클러스터 내 Pod 배포 및 Service 노출
3.  **Config Management**: ConfigMap 및 Secret을 통한 설정 외부화

---

## 기술 스택 (Tech Stack)
* **Framework**: Spring Boot 3.x, Spring Cloud Kubernetes
* **Infrastructure**: Kubernetes (Minikube), Docker
* **Language**: Java 17
* **Build Tool**: Gradle

---

## 실험 데이터 (Experimental Results)
기존 Spring 방식과 본 프로젝트의 ConfigMap 기반 방식의 정량적 비교 결과입니다.

| 비교 항목 | 기존 Spring 방식 (재시작) | ConfigMap 기반 (동적 반영) |
| :--- | :---: | :---: |
| **설정 반영 평균 시간** | 32,357.9ms | **8,826.5ms** |
| **서비스 가용성** | 일시 중단 발생 | **무중단 유지** |
| **운영자 개입** | 재배포 및 재기동 필수 | 자동 감지 및 반영 |

---

# newscurator

AI 및 IT 관련 뉴스를 자동으로 수집하고 카테고리별로 제공하는 웹 서비스입니다.

## Project Specifications

* **Framework**: Spring Boot 3.4.4
* **Build Tool**: Maven
* **Group ID**: `kr.ac.dankook.cs`
* **Artifact ID**: `curation`
* **Packaging**: JAR
* **Java Version**: 17
* **Database**: MySQL

---

## Core Features

### 1. News Aggregation

* **NewsAPI Integration**: NewsAPI를 호출하여 AI/IT 관련 최신 뉴스 수집
* **Deduplication**: 중복된 기사를 필터링하여 데이터베이스 최적화
* **Storage**: 수집된 데이터를 카테고리별 테이블에 저장

### 2. REST API Endpoints

* `GET /api/recommended`: 추천 뉴스 목록 조회
* `GET /api/news/{category}`: 특정 카테고리(AI, 빅데이터, 보안 등) 뉴스 조회

---

## Database Schema

카테고리별 뉴스 데이터는 아래의 쿼리를 통해 조회할 수 있습니다.

```sql
-- AI 관련 기사 조회
SELECT * FROM ai_articles;

-- 빅데이터 관련 기사 조회
SELECT * FROM bigdata_articles;

-- 보안 관련 기사 조회
SELECT * FROM security_articles;

-- 하드웨어 관련 기사 조회
SELECT * FROM hardware_articles;

```

---

## Installation & Setup

```bash
# 리포지토리 클론
git clone https://github.com/Drranny/newscurator.git

# 프로젝트 경로로 이동
cd newscurator

# Maven 빌드 및 실행 (상황에 따라 ./mvnw 사용)
mvn clean install
mvn spring-boot:run

```


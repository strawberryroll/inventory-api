# 재고 관리 REST API 로드맵

> Claude Code는 작업을 시작하기 전에 이 문서를 읽고, **현재 단계**의 다음 할 일만 진행한다.
> 한 단계가 끝나면 체크박스를 `[x]`로 바꾸고 "현재 단계"를 갱신한다.

## 프로젝트 개요

- **목적**: 생산 현장의 상품(자재·제품) 정보와 입고·출고 이력을 관리하는 REST API
- **용도**: ERP/QMS·생산 시스템 백엔드 직무 지원용 포트폴리오
- **개발자 배경**: 프론트엔드 경험, PHP+MySQL CRUD 경험, SQLD·정보처리기사 보유. **Spring/백엔드는 처음**

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 언어 | Java 21 (Eclipse Temurin) |
| 프레임워크 | Spring Boot 4.x (Spring Web, Spring Data JPA, Validation) |
| ORM | JPA / Hibernate 7 |
| DB | MySQL 8.4 LTS (DB명 `inventory`) |
| 빌드 | Gradle (Groovy) |
| 기타 | Lombok, IntelliJ IDEA, DBeaver, Postman, Git/GitHub |

## 설정 메모

- 설정 파일: `src/main/resources/application.yaml`
- DB 비밀번호는 환경변수 `DB_PASSWORD`로 주입 (IntelliJ 실행 구성에 설정). 코드·Git에 비밀번호를 절대 넣지 않는다
- `ddl-auto: update`, `show-sql: true` (개발 단계용)
- 서버 포트: 기본 8080

## 패키지 구조

```text
com.example.inventory
├── controller   // HTTP 요청을 받는 입구
├── service      // 비즈니스 규칙 (중복 검사, 재고 부족 검사 등)
├── repository   // DB 접근 (JpaRepository)
├── domain       // Entity (DB 테이블과 매핑)
├── dto          // 요청/응답 데이터 형태
└── exception    // 예외 클래스, 전역 예외 처리
```

## 현재 단계

**6단계. 입고·출고 (`StockHistory` 엔티티부터)**

---

## 단계별 진행

### 1~4단계. 환경 준비 ✅
- [x] JDK 21, IntelliJ, MySQL 8.4, DBeaver, Postman, Claude Code 설치
- [x] start.spring.io로 프로젝트 생성 (Gradle-Groovy, Java 21)
- [x] `inventory` DB 생성
- [x] `application.yaml` DB 연결 설정 + 환경변수
- [x] `/hello` 확인용 컨트롤러로 실행 확인

### 5단계. 상품 CRUD
- [x] 5-1. `Product` 엔티티 + `ProductRepository` (`products` 테이블 자동 생성 확인)
- [x] 5-2. 상품 등록 `POST /api/products`
    - 요청 DTO / 응답 DTO / Service / Controller 분리
    - `existsByProductCode`로 상품코드 중복 검사
    - 성공 시 `201 Created`
- [x] 5-3. 단건 조회 `GET /api/products/{id}`, 목록 조회 `GET /api/products`
    - 없는 id는 예외 발생
- [x] 5-4. 수정 `PUT /api/products/{id}` (이름·가격만), 삭제 `DELETE /api/products/{id}`
    - 수정은 엔티티 메서드 + 변경 감지(dirty checking) 사용
- [x] 5-5. Git 초기화 및 GitHub 첫 업로드 (`.gitignore` 확인)

**완료 기준**: Postman으로 5개 API 정상 동작, DBeaver에서 데이터 반영 확인

### 6단계. 입고·출고
- [ ] `StockHistory` 엔티티 (`@ManyToOne`으로 Product 연결, type: IN/OUT, quantity, createdAt)
- [ ] 입고 `POST /api/products/{id}/stock-in`
- [ ] 출고 `POST /api/products/{id}/stock-out` (재고보다 많이 출고하면 예외)
- [ ] 상품별 입출고 이력 조회 `GET /api/products/{id}/stock-histories`
- [ ] 수량 변경 로직은 엔티티 메서드(`increaseStock`, `decreaseStock`)에 둔다
- [ ] Service에 `@Transactional` 적용

**완료 기준**: 재고 10개에 20개 출고 시 에러, 이력 테이블에 기록 확인

### 7단계. 예외 처리 · 유효성 검사
- [ ] 요청 DTO에 `@NotBlank`, `@Positive`, `@PositiveOrZero` 등 적용 + `@Valid`
- [ ] 커스텀 예외 클래스 (상품 없음, 상품코드 중복, 재고 부족)
- [ ] `@RestControllerAdvice` 전역 예외 처리
    - 상품 없음 → 404, 중복 → 409, 재고 부족·입력 오류 → 400
    - 에러 응답 JSON 형식 통일 (예: `status`, `code`, `message`)

### 8단계. 추가 기능
- [ ] 재고 부족 상품 조회 `GET /api/products/low-stock?threshold=10`
- [ ] 상품명·코드 검색
- [ ] 목록 페이징
- [ ] Service 단위 테스트 (JUnit)
- [ ] Swagger/OpenAPI 문서

### 9단계. 포트폴리오 마무리
- [ ] README.md: 목적, 기술 스택, 실행 방법, API 목록·예시, 테이블 구조(ERD), 고민한 점, 개선 예정
- [ ] (선택) Docker로 MySQL + 앱 실행
- [ ] (선택) 지원 회사 DB(Oracle/MSSQL)로 전환 테스트

---

## API 목록 (목표)

| 기능 | 메서드 | URL | 상태 |
|---|---|---|---|
| 상품 등록 | POST | `/api/products` | ✅ |
| 상품 목록 | GET | `/api/products` | ✅ |
| 상품 단건 | GET | `/api/products/{id}` | ✅ |
| 상품 수정 | PUT | `/api/products/{id}` | ✅ |
| 상품 삭제 | DELETE | `/api/products/{id}` | ✅ |
| 입고 | POST | `/api/products/{id}/stock-in` | ⬜ |
| 출고 | POST | `/api/products/{id}/stock-out` | ⬜ |
| 입출고 이력 | GET | `/api/products/{id}/stock-histories` | ⬜ |
| 재고 부족 조회 | GET | `/api/products/low-stock?threshold=10` | ⬜ |

## Claude Code 작업 규칙

1. **한 번에 한 하위 단계만** 구현한다. 다음 단계는 요청받기 전에 미리 만들지 않는다
2. 코드에는 초보자가 이해할 수 있는 **핵심 주석**을 단다
3. 구현 후에는 **요청 → Controller → Service → Repository → DB** 흐름을 파일 순서대로 쉽게 설명한다. PHP나 프론트엔드에 비유하면 좋다
4. 새로운 개념(어노테이션, JPA 기능 등)이 처음 나오면 한두 줄로 설명한다
5. 구현 후 **Postman 테스트 방법**(메서드, URL, Body 예시, 기대 결과)을 알려준다
6. Entity를 API 응답으로 직접 반환하지 않고 DTO로 변환한다
7. 비밀번호·키 등 민감 정보는 코드에 쓰지 않는다
8. 한 단계가 끝나면 이 문서의 체크박스와 "현재 단계"를 갱신하고, 커밋 메시지를 제안한다

## 이해 체크 (면접 대비)

단계를 마칠 때마다 아래 질문에 직접 답할 수 있는지 확인한다.

- [x] `application.yaml`은 어떤 역할을 하나요?
- [ ] JPA, Hibernate, Spring Data JPA의 차이는?
- [x] Controller, Service, Repository의 역할은?
- [x] Entity와 DTO를 왜 나누나요?
- [ ] `@Transactional`은 왜 필요한가요?
- [x] 변경 감지(dirty checking)란?
- [ ] 출고 시 재고가 음수가 되지 않게 어떻게 막았나요?
- [ ] 전역 예외 처리를 왜 했나요?
# 재고 관리 REST API

생산 현장의 **상품(자재·제품) 정보와 입고·출고 이력을 관리하는 REST API**입니다.
Spring Boot와 JPA로 만들었고, 재고가 음수가 되지 않도록 막는 규칙과 재고·이력의 일관성(트랜잭션), 통일된 에러 응답, 테스트에 신경 썼습니다.

- 상품 등록·조회·수정·삭제, 상품명·코드 검색, 페이징
- 입고·출고로 재고를 변경하고 이력을 남김 (재고보다 많이 출고 불가)
- 재고 부족 상품 조회
- Swagger UI로 API 문서 제공 (`/swagger-ui.html`)

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Validation) |
| ORM | JPA / Hibernate 7 |
| DB | MySQL 8.4 |
| 빌드 | Gradle (Wrapper 포함) |
| API 문서 | springdoc-openapi 3.2.0 (Swagger UI) |
| 테스트 | JUnit, Mockito, AssertJ, H2(테스트 전용 인메모리 DB), Postman |
| 기타 | Lombok |

## 주요 기능

| 기능 | 설명 |
|---|---|
| 상품 CRUD | 상품코드는 중복 불가. 수정은 이름·가격만 가능하고, 재고는 입고·출고로만 변경 |
| 검색·페이징 | `keyword`로 상품명·상품코드 부분 일치 검색(대소문자 무시), `page`·`size`로 페이징 |
| 재고 부족 조회 | 재고가 기준값 이하인 상품을 재고가 적은 순으로 조회 |
| 입고·출고 | 재고 변경과 이력 저장을 **하나의 트랜잭션**으로 처리. 재고보다 많이 출고하면 거절하고 아무것도 바뀌지 않음 |
| 입출고 이력 | 상품별 이력을 최신순으로 조회 |
| 통일된 에러 응답 | 모든 오류를 `status`, `code`, `message` 형식의 JSON으로 응답 |

## 실행 방법

### 준비물
- JDK 21
- MySQL 8.x

### 1. 저장소 받기
```bash
git clone https://github.com/strawberryroll/inventory-api.git
cd inventory-api
```

### 2. 데이터베이스 만들기
MySQL에 접속해서 `inventory` 데이터베이스를 만듭니다. 테이블은 앱을 처음 실행할 때 자동으로 생성됩니다(`ddl-auto: update`, 개발용 설정).
```sql
CREATE DATABASE inventory CHARACTER SET utf8mb4;
```
접속 주소는 `src/main/resources/application.yaml`의 `jdbc:mysql://localhost:3306/inventory`입니다. 다른 주소를 쓴다면 이 파일을 수정하세요.

### 3. 환경변수 설정 후 실행
DB 비밀번호는 코드에 적지 않고 **환경변수**로 받습니다.

| 환경변수 | 설명 | 기본값 |
|---|---|---|
| `DB_PASSWORD` | MySQL 비밀번호 (필수) | 없음 |
| `DB_USERNAME` | MySQL 사용자명 | `root` |

```bash
DB_PASSWORD='내MySQL비밀번호' ./gradlew bootRun
```
IntelliJ에서 실행한다면 실행 구성(Run Configuration)의 Environment variables에 `DB_PASSWORD=...`를 넣으면 됩니다.

### 4. 확인
| 주소 | 설명 |
|---|---|
| http://localhost:8080/swagger-ui.html | Swagger UI (API 문서, 직접 호출해 볼 수 있음) |
| http://localhost:8080/v3/api-docs | OpenAPI 명세 (JSON) |
| http://localhost:8080/hello | 서버 동작 확인용 |

## 테스트

### 자동 테스트 (JUnit)
```bash
./gradlew test
```
**MySQL이나 환경변수 없이** 실행됩니다. 통합 테스트는 테스트 전용 인메모리 DB(H2)를 사용합니다. 총 42개입니다.

| 종류 | 개수 | 내용 |
|---|---|---|
| 단위 테스트 | 27 | 재고 규칙(엔티티), Service 로직을 Mockito로 검증. DB를 사용하지 않음 |
| 통합 테스트 | 14 | 실제 서버와 H2 DB로 입출고 동작, 생성된 API 문서의 내용 검증 |
| 시동 테스트 | 1 | 앱이 정상적으로 시작되는지 확인 |

### API 테스트 (Postman)
`docs/postman/`에 Postman Collection이 있습니다. 앱을 실행한 상태에서 사용합니다.

| 파일 | 내용 |
|---|---|
| `inventory-api.postman_collection.json` | 전체 API 테스트 (요청 74개, 검증 187개). 정상 흐름, 입력 검증, 오류 응답, 페이징·검색, API 문서까지 확인 |
| `inventory-seed.postman_collection.json` | 테스트용 상품 12개(`S-001`~`S-012`) 등록 |

Postman에서 **Import**한 뒤 Collection의 **Run**을 누르거나, Postman CLI로 실행합니다.
```bash
postman collection run docs/postman/inventory-api.postman_collection.json
```
> 개발용 DB에서만 실행하세요. 실행할 때마다 입출고 이력이 있는 테스트 상품이 하나씩 남습니다.

## API 목록

| 기능 | 메서드 | URL | 성공 | 주요 오류 |
|---|---|---|---|---|
| 상품 등록 | POST | `/api/products` | 201 | 400, 409 |
| 상품 목록·검색 | GET | `/api/products?keyword=&page=&size=` | 200 | - |
| 상품 단건 조회 | GET | `/api/products/{id}` | 200 | 400, 404 |
| 상품 수정 | PUT | `/api/products/{id}` | 200 | 400, 404 |
| 상품 삭제 | DELETE | `/api/products/{id}` | 204 | 404, 409 |
| 재고 부족 조회 | GET | `/api/products/low-stock?threshold=10&page=&size=` | 200 | 400 |
| 입고 | POST | `/api/products/{id}/stock-in` | 200 | 400, 404 |
| 출고 | POST | `/api/products/{id}/stock-out` | 200 | 400, 404 |
| 입출고 이력 조회 | GET | `/api/products/{id}/stock-histories` | 200 | 404 |

- **페이징**: `page`는 0부터 시작하고, `size`는 기본 10, 최대 100입니다. 정렬은 서버에서 고정합니다(목록·검색은 최신 등록순, 재고 부족 조회는 재고가 적은 순). `sort` 파라미터는 무시됩니다.
- **삭제**: 입출고 이력이 있는 상품은 삭제할 수 없습니다(`409`).
- 상세한 요청·응답 형식은 Swagger UI에서 확인할 수 있습니다.

## 요청·응답 예시

아래 응답은 실제로 앱을 실행해 받은 결과입니다.

### 상품 등록 `POST /api/products`
```json
{
  "productCode": "P-001",
  "name": "볼트 M8",
  "price": 500,
  "stockQuantity": 100
}
```
성공하면 `201 Created`와 함께 아래 단건 조회와 같은 형식의 상품 정보를 돌려줍니다.

### 상품 단건 조회 `GET /api/products/6`
```json
{
  "id": 6,
  "productCode": "S-001",
  "name": "볼트 M6",
  "price": 100,
  "stockQuantity": 5,
  "createdAt": "2026-10-28T14:52:21.985745"
}
```

### 재고 부족 조회 `GET /api/products/low-stock?threshold=10&size=3`
재고가 적은 순서로 정렬되고, 목록·검색 API도 같은 페이징 응답 형식을 사용합니다.
```json
{
  "content": [
    { "id": 14, "productCode": "S-009", "name": "와셔 L", "price": 900, "stockQuantity": 0, "createdAt": "2026-10-28T14:52:22.1023" },
    { "id": 10, "productCode": "S-005", "name": "너트 M8", "price": 500, "stockQuantity": 3, "createdAt": "2026-10-28T14:52:22.048023" },
    { "id": 6, "productCode": "S-001", "name": "볼트 M6", "price": 100, "stockQuantity": 5, "createdAt": "2026-10-28T14:52:21.985745" }
  ],
  "page": 0,
  "size": 3,
  "totalElements": 6,
  "totalPages": 2
}
```

### 입고·출고 `POST /api/products/{id}/stock-in`, `/stock-out`
요청은 수량 하나입니다. 성공하면 변경된 재고가 담긴 상품 정보를 돌려주고, 재고와 입출고 이력이 함께 저장됩니다.
```json
{ "quantity": 10 }
```

### 입출고 이력 조회 `GET /api/products/{id}/stock-histories`
최신순으로 돌려주고, 이력이 없으면 빈 배열(`[]`)입니다.
```json
[
  { "id": 10, "productId": 25, "type": "OUT", "quantity": 15, "createdAt": "2026-10-29T00:30:41.685092" },
  { "id": 9, "productId": 25, "type": "IN", "quantity": 30, "createdAt": "2026-10-29T00:30:41.669995" }
]
```

## 에러 응답

모든 오류는 같은 형식으로 응답합니다. `message`는 사람이 읽는 설명이고, 클라이언트는 안정적인 `code`로 분기하면 됩니다.

| code | HTTP | 발생 상황 |
|---|---|---|
| `PRODUCT_NOT_FOUND` | 404 | 없는 상품 |
| `DUPLICATE_PRODUCT_CODE` | 409 | 이미 있는 상품코드로 등록 |
| `INSUFFICIENT_STOCK` | 400 | 재고보다 많이 출고 |
| `PRODUCT_HAS_HISTORY` | 409 | 입출고 이력이 있는 상품 삭제 |
| `INVALID_INPUT` | 400 | 입력값 오류(검증 실패, 숫자 자리에 문자 등) |
| `INVALID_REQUEST_BODY` | 400 | 요청 본문을 읽을 수 없음(JSON 형식 오류 등) |
| `DATA_INTEGRITY_VIOLATION` | 409 | 그 밖의 DB 제약 위반 |

**재고 부족** `POST /api/products/6/stock-out` (재고 5, 수량 1000000)
```json
{
  "status": 400,
  "code": "INSUFFICIENT_STOCK",
  "message": "재고가 부족합니다. 현재 재고=5, 요청 수량=1000000"
}
```

**입력 검증 실패** `POST /api/products` — 어느 필드가 왜 틀렸는지 `errors`로 알려 줍니다.
```json
{
  "status": 400,
  "code": "INVALID_INPUT",
  "message": "입력값이 올바르지 않습니다",
  "errors": [
    { "field": "name", "message": "상품명은 필수입니다" },
    { "field": "productCode", "message": "상품코드는 필수입니다" },
    { "field": "price", "message": "가격은 0 이상이어야 합니다" }
  ]
}
```

package com.example.inventory.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 실제로 서버를 띄운(RANDOM_PORT) 상태에서 Swagger가 만든 API 명세(/v3/api-docs)를 직접 받아 확인한다.
// H2 인메모리 DB(test 프로파일)를 쓰므로 MySQL 없이 실행된다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiDocsTest {

	// 서버가 실제로 뜬 임의의 포트 번호
	@Value("${local.server.port}")
	private int port;

	private final HttpClient client = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private JsonNode docs;

	@BeforeEach
	void loadDocs() throws Exception {
		HttpResponse<String> response = get("/v3/api-docs");
		assertThat(response.statusCode()).isEqualTo(200);
		docs = objectMapper.readTree(response.body());

		// 생성된 명세를 눈으로 확인할 수 있게 build 폴더(Git에 올라가지 않음)에 저장해 둔다.
		Files.writeString(Path.of("build", "api-docs.json"), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(docs));
	}

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	@DisplayName("명세에 제목과 OpenAPI 버전이 들어 있다")
	void docs_hasInfo() {
		assertThat(docs.path("openapi").asString()).startsWith("3.");
		assertThat(docs.path("info").path("title").asString()).isEqualTo("재고 관리 REST API");
	}

	@Test
	@DisplayName("모든 API 경로가 문서에 포함된다")
	void docs_containsAllPaths() {
		List<String> paths = new ArrayList<>();
		docs.path("paths").propertyNames().forEach(paths::add);

		assertThat(paths).contains(
				"/api/products",
				"/api/products/{id}",
				"/api/products/low-stock",
				"/api/products/{id}/stock-in",
				"/api/products/{id}/stock-out",
				"/api/products/{id}/stock-histories"
		);
	}

	@Test
	@DisplayName("모든 API에 성공 응답(2xx)이 정의되어 있다")
	void docs_everyOperationHasSuccessResponse() {
		List<String> withoutSuccess = new ArrayList<>();
		docs.path("paths").properties().forEach(pathEntry ->
				pathEntry.getValue().properties().forEach(methodEntry -> {
					List<String> codes = new ArrayList<>();
					methodEntry.getValue().path("responses").propertyNames().forEach(codes::add);
					if (codes.stream().noneMatch(code -> code.startsWith("2"))) {
						withoutSuccess.add(methodEntry.getKey().toUpperCase() + " " + pathEntry.getKey());
					}
				}));

		assertThat(withoutSuccess).as("성공 응답이 없는 API").isEmpty();
	}

	@Test
	@DisplayName("서버 확인용 /hello는 문서에 노출되지 않는다")
	void docs_hidesHelloEndpoint() {
		assertThat(docs.path("paths").has("/hello")).isFalse();
	}

	@Test
	@DisplayName("성공 응답에 응답 스키마가 연결되어 있다 (단건·이력·재고 부족 조회)")
	void docs_successResponsesHaveSchemas() {
		JsonNode paths = docs.path("paths");

		assertThat(paths.path("/api/products/{id}").path("get").path("responses").path("200")
				.path("content").path("*/*").path("schema").path("$ref").asString()).endsWith("/ProductResponse");
		assertThat(paths.path("/api/products/{id}/stock-histories").path("get").path("responses").path("200")
				.path("content").path("*/*").path("schema").path("items").path("$ref").asString()).endsWith("/StockHistoryResponse");
		assertThat(paths.path("/api/products/low-stock").path("get").path("responses").path("200")
				.path("content").path("*/*").path("schema").path("$ref").asString()).endsWith("/PageResponseProductResponse");
	}

	@Test
	@DisplayName("목록 API 문서에는 page, size, keyword만 보이고 무시되는 sort는 보이지 않는다")
	void docs_listParameters_excludeSort() {
		List<String> names = new ArrayList<>();
		docs.path("paths").path("/api/products").path("get").path("parameters")
				.forEach(parameter -> names.add(parameter.path("name").asString()));

		assertThat(names).containsExactlyInAnyOrder("keyword", "page", "size");
	}

	@Test
	@DisplayName("재고 부족 조회 문서에도 sort는 없고 threshold가 있다")
	void docs_lowStockParameters() {
		List<String> names = new ArrayList<>();
		docs.path("paths").path("/api/products/low-stock").path("get").path("parameters")
				.forEach(parameter -> names.add(parameter.path("name").asString()));

		assertThat(names).containsExactlyInAnyOrder("threshold", "page", "size");
	}

	@Test
	@DisplayName("등록 API 문서에 201 성공과 400, 409 오류 응답이 정의되어 있다")
	void docs_createResponses() {
		JsonNode responses = docs.path("paths").path("/api/products").path("post").path("responses");

		assertThat(responses.has("201")).isTrue();
		assertThat(responses.has("400")).isTrue();
		assertThat(responses.has("409")).isTrue();
	}

	@Test
	@DisplayName("오류 응답 형식(ErrorResponse)이 스키마로 문서화된다")
	void docs_errorResponseSchema() {
		JsonNode errorSchema = docs.path("components").path("schemas").path("ErrorResponse");

		assertThat(errorSchema.isMissingNode()).isFalse();
		List<String> fields = new ArrayList<>();
		errorSchema.path("properties").propertyNames().forEach(fields::add);
		assertThat(fields).contains("status", "code", "message", "errors");
	}

	@Test
	@DisplayName("요청 DTO의 검증 규칙과 예시값이 문서에 반영된다")
	void docs_requestSchemaReflectsValidation() {
		JsonNode create = docs.path("components").path("schemas").path("ProductCreateRequest");

		assertThat(create.path("properties").path("productCode").path("maxLength").asInt()).isEqualTo(50);
		assertThat(create.path("properties").path("productCode").path("example").asString()).isEqualTo("P-001");
		List<String> required = new ArrayList<>();
		create.path("required").forEach(node -> required.add(node.asString()));
		assertThat(required).contains("productCode", "name", "price", "stockQuantity");
	}

	@Test
	@DisplayName("Swagger UI 화면에 접속할 수 있다")
	void swaggerUi_isAccessible() throws Exception {
		assertThat(get("/swagger-ui/index.html").statusCode()).isEqualTo(200);
	}

}
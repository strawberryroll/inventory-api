package com.example.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger UI(/swagger-ui.html) 맨 위에 보이는 API 문서의 제목·설명·버전을 정한다.
// 각 API의 경로와 요청·응답 구조는 springdoc이 코드(Controller, DTO)를 읽어 자동으로 만든다.
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI().info(new Info()
				.title("재고 관리 REST API")
				.description("상품 등록·조회·수정·삭제, 상품명·코드 검색, 재고 부족 조회, 입고·출고와 입출고 이력 조회를 제공하는 API")
				.version("v1"));
	}

}

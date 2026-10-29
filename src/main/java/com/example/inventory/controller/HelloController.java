package com.example.inventory.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

// @RestController: 이 클래스가 HTTP 요청을 받는 컨트롤러이고, 반환값을 그대로 응답 본문(body)에 쓴다는 표시
// @Hidden: 서버 확인·안내용 API라서 Swagger 문서에는 보여 주지 않는다.
@Hidden
@RestController
public class HelloController {

	// GET / (배포 주소 맨 처음 화면) -> API 문서(Swagger UI)로 이동시킨다.
	// 이 서버는 화면(HTML)이 없는 API 서버라서, 이 코드가 없으면 주소로 들어온 사람이 404 오류 화면을 보게 된다.
	// 302(Found): "임시로 다른 주소로 가라"는 의미. Location 헤더의 주소로 브라우저가 이동한다.
	@GetMapping("/")
	public ResponseEntity<Void> root() {
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/swagger-ui.html")).build();
	}

	// GET /hello 요청이 오면 이 메서드가 실행된다 (PHP로 치면 hello.php 한 파일)
	@GetMapping("/hello")
	public String hello() {
		return "Hello, Inventory!";
	}

}

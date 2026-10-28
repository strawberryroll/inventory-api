package com.example.inventory.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController: 이 클래스가 HTTP 요청을 받는 컨트롤러이고, 반환값을 그대로 응답 본문(body)에 쓴다는 표시
// @Hidden: 서버가 떠 있는지 확인하는 용도의 API라서 Swagger 문서에는 보여 주지 않는다.
@Hidden
@RestController
public class HelloController {

	// GET /hello 요청이 오면 이 메서드가 실행된다 (PHP로 치면 hello.php 한 파일)
	@GetMapping("/hello")
	public String hello() {
		return "Hello, Inventory!";
	}

}

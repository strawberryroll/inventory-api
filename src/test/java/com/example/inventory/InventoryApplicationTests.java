package com.example.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// 앱 전체(Spring 컨텍스트)가 정상적으로 켜지는지 확인하는 "시동 테스트".
// @ActiveProfiles("test"): application-test.yaml을 적용해 MySQL 대신 H2 인메모리 DB로 켠다.
// 그래서 MySQL이나 DB_PASSWORD 환경변수 없이도 통과한다.
@SpringBootTest
@ActiveProfiles("test")
class InventoryApplicationTests {

	@Test
	void contextLoads() {
	}

}

package com.example.inventory.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// 페이징 응답 상자. Spring의 Page를 그대로 내보내면 불필요한 필드가 많고 버전에 따라 모양이 바뀔 수 있어서,
// 필요한 5개만 담은 우리만의 형식을 따로 정의한다.
// <T>: 어떤 DTO든 담을 수 있는 제네릭. (예: PageResponse<ProductResponse>)
public record PageResponse<T>(
		List<T> content,      // 이번 페이지의 데이터
		int page,             // 현재 페이지 번호 (0부터 시작)
		int size,             // 한 페이지 크기
		long totalElements,   // 전체 데이터 개수
		int totalPages        // 전체 페이지 수
) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages()
		);
	}

}

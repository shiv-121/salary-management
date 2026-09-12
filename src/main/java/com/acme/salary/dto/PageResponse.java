package com.acme.salary.dto;

import java.util.List;

/**
 * Generic paginated response DTO.
 * Wraps Spring Data's Page information in a clean JSON structure.
 *
 * @param <T> Type of content items
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}

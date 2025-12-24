package com.daryaftmanazam.daryaftcore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper for paginated API responses.
 *
 * @param <T> the type of content in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    @JsonProperty("content")
    private List<T> content;

    @JsonProperty("page_number")
    private Integer pageNumber;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("total_elements")
    private Long totalElements;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("is_first")
    private Boolean isFirst;

    @JsonProperty("is_last")
    private Boolean isLast;

    @JsonProperty("has_next")
    private Boolean hasNext;

    @JsonProperty("has_previous")
    private Boolean hasPrevious;

    /**
     * Create a PageResponse from Spring Data Page.
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Create an empty PageResponse.
     */
    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(0)
                .totalElements(0L)
                .totalPages(0)
                .isFirst(true)
                .isLast(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}

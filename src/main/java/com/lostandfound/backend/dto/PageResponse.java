package com.lostandfound.backend.dto;

import java.util.List;

/** Generic page of results plus the paging numbers a client needs for page buttons / "load more". */
public record PageResponse<T>(List<T> content, int page, int size,
                              long totalElements, int totalPages) {}
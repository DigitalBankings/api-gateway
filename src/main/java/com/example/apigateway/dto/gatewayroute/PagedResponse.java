package com.example.apigateway.dto.gatewayroute;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private Pagination pagination;

    @Data
    @AllArgsConstructor
    public static class Pagination {
        private int itemsPerPage;
        private int totalPages;
        private long totalItems;
        private int currentPage;
    }
}

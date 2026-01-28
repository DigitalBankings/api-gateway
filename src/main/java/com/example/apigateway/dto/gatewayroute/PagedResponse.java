package com.example.apigateway.dto.gatewayroute;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

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

package com.example.apigateway.dto.gatewayroutemapdto;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRouteMapResponse {
  private Long totalElements;
  private Integer totalPages;
  private Integer currentPage;
  private List<GatewayRouteConfigResponse> items;
}

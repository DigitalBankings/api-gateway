package com.example.apigateway.dto.gatewayroutemapdto;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetRouteMapResponse {
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private List<GatewayRouteConfigResponse> items;
}

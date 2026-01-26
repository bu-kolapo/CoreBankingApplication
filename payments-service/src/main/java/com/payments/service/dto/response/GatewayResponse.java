package com.payments.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public  class GatewayResponse {
    private String transactionId;
    private String status;
    private String clientSecret;
}
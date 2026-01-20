package com.payments.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayChargeResponse {
    private  String gatewayRef;
    private boolean isConfirmed;
    private boolean isAuthorized;
}

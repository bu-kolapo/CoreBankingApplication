package com.orders.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusRequest {

    @NotBlank(message = "Status is required")
    private String orderStatus;

    private String fulfillmentStatus;
    private String trackingNumber;
    private String reason;
}

package com.orders.service.dto.request;

import com.orders.service.dto.AddressDto;
import com.orders.service.dto.OrderItemDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "Order type is required")
    private String orderType;               // PURCHASE, TRANSFER, SUBSCRIPTION, TOP_UP

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String description;

    private List<OrderItemDto> items;

    private AddressDto shippingAddress;
    private AddressDto billingAddress;

    private String idempotencyKey;          // For payment idempotency

}

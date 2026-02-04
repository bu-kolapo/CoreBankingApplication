package com.notification.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEventDto {
    private String accountId;
    private String customerId;
    private String email;
    private String eventType;               // FROZEN, UNFROZEN, CLOSED
    private String accountNumber;
}
package com.notification.service.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycStatusChangedEvent {
    private String customerId;
    private String email;
    private String kycStatus;               // VERIFIED, REJECTED
    private String kycLevel;
    private String previousStatus;
}
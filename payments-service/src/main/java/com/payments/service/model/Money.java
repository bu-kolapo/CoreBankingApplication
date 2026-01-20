package com.payments.service.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Money {

    private String currency;
    private  long minorUnits;
    private PaymentStatus status;






}

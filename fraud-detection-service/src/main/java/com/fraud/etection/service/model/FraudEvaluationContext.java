package com.fraud.etection.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudEvaluationContext {

    private BigDecimal riskScore = BigDecimal.ZERO;
    private List<String> flaggedReasons = new ArrayList<>();

    private boolean velocityFailed;
    private boolean amountExceeded;
    private boolean unusualPattern;

    public void addRisk(BigDecimal value) {
        riskScore = riskScore.add(value);
    }

    public void flag(String reason) {
        flaggedReasons.add(reason);
    }
}


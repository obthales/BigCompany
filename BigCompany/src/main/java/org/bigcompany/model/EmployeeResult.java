package org.bigcompany.model;

import java.math.BigDecimal;

public record EmployeeResult(
        String id,
        String firstName,
        String lastName,
        BigDecimal salary,
        String managerId,
        BigDecimal differenceFromExpectedPay,
        int totalManagersToCeo
    ) {
}

package org.bigcompany.model;

import java.util.List;

public record Metrics(
        List<EmployeeResult> overpaidManagers,
        List<EmployeeResult> underpaidManagers,
        List<EmployeeResult> employeesFarFromCeo
        ) {
}

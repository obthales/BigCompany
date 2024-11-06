package org.bigcompany.service;

import org.bigcompany.model.Employee;
import org.bigcompany.model.EmployeeResult;
import org.bigcompany.model.Metrics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CompanyService {
    private List<EmployeeResult> overpaidManagers;
    private List<EmployeeResult> underpaidManagers;
    private List<EmployeeResult> employeesFarFromCeo;

    public Metrics navigateStructureAndProcessMetrics(Employee ceo) {
        if (ceo == null) {
            return null;
        }

        overpaidManagers = new ArrayList<>();
        underpaidManagers = new ArrayList<>();
        employeesFarFromCeo = new ArrayList<>();

        Queue<Employee> queue = new LinkedList<>(ceo.getSubordinates());
        while (!queue.isEmpty()) {
            Employee employee = queue.poll();
            processMetrics(employee);

            if (employee.getSubordinates() != null) {
                queue.addAll(employee.getSubordinates());
            }
        }

        return new Metrics(
                overpaidManagers,
                underpaidManagers,
                employeesFarFromCeo);
    }

    private void processMetrics(Employee employee) {
        var differenceFromExpectedPay = BigDecimal.ZERO;
        if (employee.isOverPaid()) {
            differenceFromExpectedPay = employee.getOverpaidAmount();
        } else if (employee.isUnderPaid()) {
            differenceFromExpectedPay = employee.getUnderpaidAmount();
        }

        EmployeeResult employeeResult = cretateEmployeeResult(employee, differenceFromExpectedPay);

        if (employee.isOverPaid()) {
            overpaidManagers.add(employeeResult);
        } else if (employee.isUnderPaid()) {
            underpaidManagers.add(employeeResult);
        }

        if (employee.isFarFromCeo()) {
            employeesFarFromCeo.add(employeeResult);
        }
    }

    private EmployeeResult cretateEmployeeResult(Employee employee, BigDecimal differenceFromExpectedPay) {
        return new EmployeeResult(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getSalary(),
                employee.getManagerId(),
                differenceFromExpectedPay,
                employee.getDistanceToCeo()
        );
    }
}

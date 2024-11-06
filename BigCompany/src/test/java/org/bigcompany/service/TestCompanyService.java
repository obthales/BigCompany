package org.bigcompany.service;

import org.bigcompany.io.CsvParser;
import org.bigcompany.model.Employee;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCompanyService {

    @Test
    void testNavigateStructureAndProcessMetrics() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,1000000,
                124,Martin,Chekov,450000,123
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                306,Andre,Silva,30000,305
                307,Andrey,Kokonenko,33000,305
                308,Brandy,Silver,25000,306
                309,Carlos,Garcia,20000,308
                310,Jose,Perez,10000,309
                """;

        Path tempFile = Files.createTempFile("test-navigate-structure", ".csv");
        Files.writeString(tempFile, csvContent);
        var parser = new CsvParser();

        Map<String, Employee> employeesById = parser.csvToEmployeeList(tempFile.toString());
        Employee ceo = parser.buildCompanyStructure(employeesById);

        var service = new CompanyService();

        var metrics = service.navigateStructureAndProcessMetrics(ceo);

        var overpaidManagers = metrics.overpaidManagers();
        var underpaidManagers = metrics.underpaidManagers();
        var employeesFarFromCeo = metrics.employeesFarFromCeo();

        assertEquals(2, overpaidManagers.size());
        assertEquals(1, underpaidManagers.size());
        assertEquals(2, employeesFarFromCeo.size());

        var martin = overpaidManagers.stream().filter(employee -> "124".equals(employee.id())).findFirst().orElse(null);
        var carlosOverpaid = overpaidManagers.stream().filter(employee -> "309".equals(employee.id())).findFirst().orElse(null);

        var brett = underpaidManagers.stream().filter(employee -> "305".equals(employee.id())).findFirst().orElse(null);

        var carlosFarFromCeo = employeesFarFromCeo.stream().filter(employee -> "309".equals(employee.id())).findFirst().orElse(null);
        var jose = employeesFarFromCeo.stream().filter(employee -> "310".equals(employee.id())).findFirst().orElse(null);

        // Ensure records are not being duplicated
        assert carlosOverpaid == carlosFarFromCeo;

        assertEquals(new BigDecimal("375000.000"), martin.differenceFromExpectedPay());
        assertEquals(new BigDecimal("5000.000"), carlosOverpaid.differenceFromExpectedPay());
        assertEquals(new BigDecimal("-3800.000"), brett.differenceFromExpectedPay());
        assertEquals(5, carlosFarFromCeo.totalManagersToCeo());
        assertEquals(6, jose.totalManagersToCeo());
    }
}

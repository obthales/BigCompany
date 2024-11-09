package org.bigcompany.io;

import org.bigcompany.exceptions.CeoAlreadyExistsException;
import org.bigcompany.exceptions.CeoNotFoundException;
import org.bigcompany.exceptions.EmployeeAlreadyExistsException;
import org.bigcompany.exceptions.ManagerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.bigcompany.model.Employee;

import static org.junit.jupiter.api.Assertions.*;

class TestCsvParser {
    private CsvParser parser;
    private Path defaultTestFile;

    @BeforeEach
    void setUp() throws IOException {
        parser = new CsvParser();

        // Create a temporary CSV file for testing
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                """;

        defaultTestFile = Files.createTempFile("default_test", ".csv");
        Files.writeString(defaultTestFile, csvContent);
    }

    @Test
    void testCsvToEmployeeList() throws IOException {
        Map<String, Employee> employeesById = parser.csvToEmployeeList(defaultTestFile.toString());
        List<Employee> employees = employeesById.values().stream().toList();

        // Check the number of nodes
        assertEquals(5, employeesById.size());

        // Check specific node properties
        Employee joe = employees.stream().filter(employee -> "123".equals(employee.getId())).findFirst().orElse(null);
        assertNotNull(joe);
        assertEquals("Joe", joe.getFirstName());
        assertEquals("Doe", joe.getLastName());
        assertEquals(new BigDecimal("60000"), joe.getSalary());
        assertTrue(joe.getManagerId() == null || joe.getManagerId().isEmpty());

        Employee martin = employees.stream().filter(employee -> "124".equals(employee.getId())).findFirst().orElse(null);
        assertNotNull(martin);
        assertEquals("Martin", martin.getFirstName());
        assertEquals("Chekov", martin.getLastName());
        assertEquals(new BigDecimal("45000"), martin.getSalary());
        assertEquals("123", martin.getManagerId());

        Employee brett = employees.stream().filter(employee -> "305".equals(employee.getId())).findFirst().orElse(null);
        assertNotNull(brett);
        assertEquals("Brett", brett.getFirstName());
        assertEquals("Hardleaf", brett.getLastName());
        assertEquals(new BigDecimal("34000"), brett.getSalary());
        assertEquals("300", brett.getManagerId());
    }

    @Test
    void testCsvToEmployeeListWithDifferentHeaderOrder() throws IOException {
        String csvContent = """
                firstName,lastName,salary,managerId,Id
                Joe,Doe,60000,,123
                Martin,Chekov,45000,123,124
                Bob,Ronstad,47000,123,125
                Alice,Hasacat,50000,124,300
                Brett,Hardleaf,34000,300,305
                """;

        Path localTestFile = Files.createTempFile("local_test", ".csv");
        Files.writeString(localTestFile, csvContent);

        Map<String, Employee> employeesById = parser.csvToEmployeeList(localTestFile.toString());
        List<Employee> employees = employeesById.values().stream().toList();

        // Check the number of nodes
        assertEquals(5, employeesById.size());

        // Check specific node properties
        Employee joe = employees.stream().filter(employee -> "123".equals(employee.getId())).findFirst().orElse(null);
        assertNotNull(joe);
        assertEquals("Joe", joe.getFirstName());
        assertEquals("Doe", joe.getLastName());
        assertEquals(new BigDecimal("60000"), joe.getSalary());
        assertTrue(joe.getManagerId() == null || joe.getManagerId().isEmpty());

        Employee martin = employees.stream().filter(employee -> "124".equals(employee.getId())).findFirst().orElse(null);
        assertNotNull(martin);
        assertEquals("Martin", martin.getFirstName());
        assertEquals("Chekov", martin.getLastName());
        assertEquals(new BigDecimal("45000"), martin.getSalary());
        assertEquals("123", martin.getManagerId());

        Employee brett = employees.stream().filter(employee -> "305".equals(employee.getId())).findFirst().orElse(null);
        assertNotNull(brett);
        assertEquals("Brett", brett.getFirstName());
        assertEquals("Hardleaf", brett.getLastName());
        assertEquals(new BigDecimal("34000"), brett.getSalary());
        assertEquals("300", brett.getManagerId());
    }

    @Test
    void testBuildCompanyStructure() throws IOException {
        Map<String, Employee> employeesById = parser.csvToEmployeeList(defaultTestFile.toString());
        Employee ceo = parser.buildCompanyStructure(employeesById);

        assertNotNull(ceo);
        assertEquals(2, ceo.getSubordinates().size());
    }

    @Test
    void testBuildCompanyStructureWithBlankLines() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,305
                124,Martin,Chekov,45000,123
                
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                """;
        Path localTestFile = Files.createTempFile("local_test", ".csv");
        Files.writeString(localTestFile, csvContent);

        assertThrows(IllegalArgumentException.class, () -> {
            Map<String, Employee> employeesById = parser.csvToEmployeeList(localTestFile.toString());
        });
    }

    @Test
    void testBuildCompanyStructureWithoutCeo() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,305
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                """;
        Path localTestFile = Files.createTempFile("local_test", ".csv");
        Files.writeString(localTestFile, csvContent);

        Map<String, Employee> employeesById = parser.csvToEmployeeList(localTestFile.toString());

        assertThrows(CeoNotFoundException.class, () -> {
            parser.buildCompanyStructure(employeesById);
        });
    }

    @Test
    void testBuildCompanyStructureWithMultipleCeos() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,
                305,Brett,Hardleaf,34000,300
                """;
        Path localTestFile = Files.createTempFile("local_test", ".csv");
        Files.writeString(localTestFile, csvContent);

        Map<String, Employee> employeesById = parser.csvToEmployeeList(localTestFile.toString());

        assertThrows(CeoAlreadyExistsException.class, () -> {
            parser.buildCompanyStructure(employeesById);
        });
    }

    @Test
    void testBuildCompanyStructureWithMissingManager() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,100
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                """;
        Path localTestFile = Files.createTempFile("local_test", ".csv");
        Files.writeString(localTestFile, csvContent);

        Map<String, Employee> employeesById = parser.csvToEmployeeList(localTestFile.toString());

        assertThrows(ManagerNotFoundException.class, () -> {
            parser.buildCompanyStructure(employeesById);
        });
    }

    @Test
    void testBuildCompanyStructureWithDuplicatedEmployee() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                124,Dylan,Culprit,40000,123
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                """;
        Path localTestFile = Files.createTempFile("local_test", ".csv");
        Files.writeString(localTestFile, csvContent);

        assertThrows(EmployeeAlreadyExistsException.class, () -> {
            parser.csvToEmployeeList(localTestFile.toString());
        });
    }

    @Test
    void testGetPeopleAbove() throws IOException {
        Map<String, Employee> employeesById = parser.csvToEmployeeList(defaultTestFile.toString());
        Employee ceo = parser.buildCompanyStructure(employeesById);

        assertNotNull(ceo);

        Employee martin = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("124")).findFirst().orElse(null);
        Employee bob = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("125")).findFirst().orElse(null);
        Employee alice = martin.getSubordinates().getFirst();
        Employee brett = alice.getSubordinates().getFirst();

        assertEquals(0, ceo.getTotalPeopleAbove());
        assertEquals(1, martin.getTotalPeopleAbove());
        assertEquals(1, bob.getTotalPeopleAbove());
        assertEquals(2, alice.getTotalPeopleAbove());
        assertEquals(3, brett.getTotalPeopleAbove());
    }

    @Test
    void testGetPeopleBelow() throws IOException {
        Map<String, Employee> employeesById = parser.csvToEmployeeList(defaultTestFile.toString());
        Employee ceo = parser.buildCompanyStructure(employeesById);

        assertNotNull(ceo);

        Employee martin = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("124")).findFirst().orElse(null);
        Employee bob = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("125")).findFirst().orElse(null);
        Employee alice = martin.getSubordinates().getFirst();
        Employee brett = alice.getSubordinates().getFirst();

        assertEquals(5, ceo.getTotalPeopleBelowPlusSelf());
        assertEquals(3, martin.getTotalPeopleBelowPlusSelf());
        assertEquals(1, bob.getTotalPeopleBelowPlusSelf());
        assertEquals(2, alice.getTotalPeopleBelowPlusSelf());
        assertEquals(1, brett.getTotalPeopleBelowPlusSelf());
    }

    @Test
    void testGetSalaryOfEmployeeAndSubordinates() throws IOException {
        Map<String, Employee> employeesById = parser.csvToEmployeeList(defaultTestFile.toString());
        Employee ceo = parser.buildCompanyStructure(employeesById);

        assertNotNull(ceo);

        Employee martin = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("124")).findFirst().orElse(null);
        Employee bob = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("125")).findFirst().orElse(null);
        Employee alice = martin.getSubordinates().getFirst();
        Employee brett = alice.getSubordinates().getFirst();

        assertEquals(new BigDecimal("236000"), ceo.getTotalSalariesBelowPlusSelf());
        assertEquals(new BigDecimal("129000"), martin.getTotalSalariesBelowPlusSelf());
        assertEquals(new BigDecimal("47000"), bob.getTotalSalariesBelowPlusSelf());
        assertEquals(new BigDecimal("84000"), alice.getTotalSalariesBelowPlusSelf());
        assertEquals(new BigDecimal("34000"), brett.getTotalSalariesBelowPlusSelf());
    }

    @Test
    void testGetTotalSubordinatesAverageSalary() throws IOException {
        Map<String, Employee> employeesById = parser.csvToEmployeeList(defaultTestFile.toString());
        Employee ceo = parser.buildCompanyStructure(employeesById);

        assertNotNull(ceo);

        Employee martin = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("124")).findFirst().orElse(null);
        Employee bob = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("125")).findFirst().orElse(null);
        Employee alice = martin.getSubordinates().getFirst();
        Employee brett = alice.getSubordinates().getFirst();

        assertEquals(new BigDecimal("44000.00"), ceo.getTotalSubortinatesAverageSalary());
        assertEquals(new BigDecimal("42000.00"), martin.getTotalSubortinatesAverageSalary());
        assertNull(bob.getTotalSubortinatesAverageSalary());
        assertEquals(new BigDecimal("34000.00"), alice.getTotalSubortinatesAverageSalary());
        assertNull(brett.getTotalSubortinatesAverageSalary());
    }

    @Test
    void testGetDirectSubordinatesAverageSalary() throws IOException {
        Map<String, Employee> employeesById = parser.csvToEmployeeList(defaultTestFile.toString());
        Employee ceo = parser.buildCompanyStructure(employeesById);

        assertNotNull(ceo);

        Employee martin = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("124")).findFirst().orElse(null);
        Employee bob = ceo.getSubordinates().stream().filter(employee -> employee.getId().equals("125")).findFirst().orElse(null);
        Employee alice = martin.getSubordinates().getFirst();
        Employee brett = alice.getSubordinates().getFirst();

        assertEquals(new BigDecimal("46000.00"), ceo.getDirectSubortinatesAverageSalary());
        assertEquals(new BigDecimal("50000.00"), martin.getDirectSubortinatesAverageSalary());
        assertNull(bob.getDirectSubortinatesAverageSalary());
        assertEquals(new BigDecimal("34000.00"), alice.getDirectSubortinatesAverageSalary());
        assertNull(brett.getDirectSubortinatesAverageSalary());
    }

}

package org.bigcompany.io;

import org.bigcompany.model.Employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class CsvParser {

    public Employee parseCsv(String filePath) throws IOException {
        Map<String, Employee> employeesById = csvToEmployeeList(filePath);

        return buildCompanyStructure(employeesById);
    }

    public Map<String, Employee> csvToEmployeeList(String filePath) throws IOException {
        Map<String, Employee> employeesById = new HashMap<>();

        try (var br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;

            if ((line = br.readLine()) != null) {
                headers = line.split(",");
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> record = new HashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < values.length ? values[i].trim() : "";
                    record.put(key, value);
                }

                Employee node = createEmployeeNode(record);
                employeesById.put(node.getId(), node);
            }
        }

        return employeesById;
    }

    private Employee createEmployeeNode(Map<String, String> record) {
        Employee employee = new Employee();
        employee.setId(record.get("Id"));
        employee.setFirstName(record.get("firstName"));
        employee.setLastName(record.get("lastName"));
        employee.setSalary(new BigDecimal(record.get("salary")));
        employee.setManagerId(record.get("managerId"));
        return employee;
    }

    public Employee buildCompanyStructure(Map<String, Employee> employeesById) {
        AtomicReference<Employee> ceo = new AtomicReference<>(null);
        Map<String, Employee> concurrentEmployeesById = new ConcurrentHashMap<>(employeesById);

        concurrentEmployeesById.entrySet().parallelStream().forEach(e -> {
            Employee employee = e.getValue();
            String managerId = employee.getManagerId();
            if (managerId == null || managerId.isEmpty()) {
                ceo.set(employee);
            } else {
                Employee manager = concurrentEmployeesById.get(managerId);
                if (manager != null) {
                    synchronized (manager) {
                        employee.setManager(manager);
                        manager.addSubordinate(employee);
                    }
                }
            }
        });

        // Raise exception if there are employees but no CEO
        if (ceo.get() == null && !employeesById.isEmpty()) {
            throw new IllegalStateException("No CEO found in the company structure");
        }

        return ceo.get();
    }

}

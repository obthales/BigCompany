package org.bigcompany.io;

import org.bigcompany.exceptions.CeoNotFoundException;
import org.bigcompany.exceptions.EmployeeAlreadyExistsException;
import org.bigcompany.exceptions.ManagerNotFoundException;
import org.bigcompany.exceptions.CeoAlreadyExistsException;
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
                Map<String, String> fields = new HashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < values.length ? values[i].trim() : "";
                    fields.put(key, value);
                }

                if (employeesById.containsKey(fields.get("Id"))) {
                    throw new EmployeeAlreadyExistsException();
                }

                Employee node = createEmployeeNode(fields);
                employeesById.put(node.getId(), node);
            }
        }

        return employeesById;
    }

    private Employee createEmployeeNode(Map<String, String> fields) {
        if (fields.get("Id") == null || fields.get("Id").isEmpty()
                || fields.get("firstName") == null || fields.get("firstName").isEmpty()
                || fields.get("lastName") == null || fields.get("lastName").isEmpty()
                || fields.get("salary") == null || fields.get("salary").isEmpty()
        ) {
            throw new IllegalArgumentException("Unable to create employee. Invalid value.");
        }

        Employee employee = new Employee();
        try {
            employee.setId(fields.get("Id"));
            employee.setFirstName(fields.get("firstName"));
            employee.setLastName(fields.get("lastName"));
            employee.setSalary(new BigDecimal(fields.get("salary")));
            employee.setManagerId(fields.get("managerId"));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to create employee. Invalid value.");
        }
        return employee;
    }

    public Employee buildCompanyStructure(Map<String, Employee> employeesById) {
        AtomicReference<Employee> ceo = new AtomicReference<>(null);
        Map<String, Employee> concurrentEmployeesById = new ConcurrentHashMap<>(employeesById);

        concurrentEmployeesById.entrySet().parallelStream().forEach(e -> {
            Employee employee = e.getValue();
            String managerId = employee.getManagerId();
            if (managerId == null || managerId.isEmpty()) {
                if (ceo.get() != null) {
                    throw new CeoAlreadyExistsException();
                }
                ceo.set(employee);
            } else {
                if (!employeesById.containsKey(managerId)) {
                    throw new ManagerNotFoundException();
                }
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
            throw new CeoNotFoundException();
        }

        return ceo.get();
    }

}

package org.bigcompany.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Employee {
    static final String SALARY_PERCENTAGE_UPPER_LIMIT = "1.5";
    static final String SALARY_PERCENTAGE_LOWER_LIMIT = "1.2";
    static final int MAXIMUM_MANAGERS_ALLOWED = 4;


    private Employee manager;
    private List<Employee> subordinates;

    private String id;
    private String firstName;
    private String lastName;
    private BigDecimal salary;
    private String managerId;

    private Integer totalPeopleBelowPlusSelf;
    private BigDecimal totalSalariesBelowPlusSelf;
    private Integer totalPeopleAbove;

    private BigDecimal averageDirectSubordinateSalary;

    // Getters and setters

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public List<Employee> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(List<Employee> subordinates) {
        this.subordinates = subordinates;
    }

    public void addSubordinate(Employee child) {
        if (this.subordinates == null) {
            this.subordinates = new ArrayList<>();
        }
        this.subordinates.add(child);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    // Compute expected salaries and distance from CEO

    public BigDecimal getDirectSubortinatesAverageSalary() {
        if (averageDirectSubordinateSalary != null) {
            return averageDirectSubordinateSalary;
        }

        if (subordinates == null || subordinates.size() == 0) {
            return null;
        }

        BigDecimal directSalariesBelow = subordinates.stream()
                .map(x -> x.getSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal directPeopleBelow = new BigDecimal(subordinates.size());

        // Specify scale and rounding mode
        averageDirectSubordinateSalary = directSalariesBelow.divide(directPeopleBelow, 2, RoundingMode.HALF_UP); // 2 decimal places

        return averageDirectSubordinateSalary;
    }

    public boolean isOverPaid() {
        BigDecimal maximumAcceptedSalary = getMaximumAcceptedSalary();

        if (maximumAcceptedSalary == null) {
            return false;
        }

        return (salary.compareTo(maximumAcceptedSalary) > 0);
    }

    public BigDecimal getOverpaidAmount() {
        BigDecimal maximumAcceptedSalary = getMaximumAcceptedSalary();

        if (maximumAcceptedSalary == null) {
            return BigDecimal.ZERO;
        }

        return salary.subtract(maximumAcceptedSalary);
    }

    private BigDecimal getMaximumAcceptedSalary() {
        BigDecimal directSubortinatesAverageSalary = getDirectSubortinatesAverageSalary();
        if (directSubortinatesAverageSalary == null) {
            return null;
        }

        return directSubortinatesAverageSalary.multiply(new BigDecimal(SALARY_PERCENTAGE_UPPER_LIMIT));
    }

    public boolean isUnderPaid() {
        BigDecimal minimumAcceptedSalary = getMinimumAcceptedSalary();

        if (minimumAcceptedSalary == null) {
            return false;
        }

        return (salary.compareTo(minimumAcceptedSalary) < 0);
    }

    public BigDecimal getUnderpaidAmount() {
        BigDecimal minimumAcceptedSalary = getMinimumAcceptedSalary();

        if (minimumAcceptedSalary == null) {
            return BigDecimal.ZERO;
        }

        return salary.subtract(minimumAcceptedSalary);
    }

    private BigDecimal getMinimumAcceptedSalary() {
        BigDecimal directSubortinatesAverageSalary = getDirectSubortinatesAverageSalary();
        if (directSubortinatesAverageSalary == null) {
            return null;
        }

        return directSubortinatesAverageSalary.multiply(new BigDecimal(SALARY_PERCENTAGE_LOWER_LIMIT));
    }

    public Integer getTotalPeopleAbove() {
        if (totalPeopleAbove == null) {
            totalPeopleAbove = 0;

            if (manager != null) {
                totalPeopleAbove = 1 + manager.getTotalPeopleAbove();
            }
        }

        return totalPeopleAbove;
    }

    public boolean isFarFromCeo() {
        return getTotalPeopleAbove() > MAXIMUM_MANAGERS_ALLOWED + 1; // Managers + CEO
    }

    public int getDistanceToCeo() {
        return getTotalPeopleAbove() - 1; // Managers - CEO
    }

    // Methods below are NOT used for final result. Kept here for didactic purposes.

    public Integer getTotalPeopleBelowPlusSelf() {
        if (totalPeopleBelowPlusSelf == null) {
            int totalPeopleBelow = 0;

            if (subordinates != null) {
                totalPeopleBelow = subordinates.parallelStream()
                        .mapToInt(Employee::getTotalPeopleBelowPlusSelf)
                        .sum();
            }

            totalPeopleBelowPlusSelf = totalPeopleBelow + 1;
        }

        return totalPeopleBelowPlusSelf;
    }

    private Integer getTotalPeopleBelow(){
        return getTotalPeopleBelowPlusSelf() - 1;
    }

    public BigDecimal getTotalSalariesBelowPlusSelf() {
        if (totalSalariesBelowPlusSelf == null) {
            BigDecimal totalSalariesBelow = BigDecimal.ZERO;

            if (subordinates != null) {
                totalSalariesBelow = subordinates.parallelStream()
                        .map(Employee::getTotalSalariesBelowPlusSelf)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            totalSalariesBelowPlusSelf = totalSalariesBelow.add(salary);
        }

        return totalSalariesBelowPlusSelf;
    }

    private BigDecimal getTotalSalariesBelow() {
        return getTotalSalariesBelowPlusSelf().subtract(salary);
    }

    public BigDecimal getTotalSubortinatesAverageSalary() {
        if (getTotalPeopleBelow() == 0) {
            return null;
        }

        BigDecimal totalSalaries = getTotalSalariesBelow();
        BigDecimal totalPeople = new BigDecimal(getTotalPeopleBelow());

        // Specify scale and rounding mode
        return totalSalaries.divide(totalPeople, 2, RoundingMode.HALF_UP); // 2 decimal places
    }

}

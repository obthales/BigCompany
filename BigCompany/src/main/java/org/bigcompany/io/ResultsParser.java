package org.bigcompany.io;

import org.bigcompany.model.EmployeeResult;
import org.bigcompany.model.Metrics;

import java.text.MessageFormat;
import java.util.List;

public class ResultsParser {

    enum ListType {
        OVERPAID,
        UNDERPAID
    }

    public static void DisplayOnConsole(Metrics metrics) {
        printEmployeesFarFromCeo(metrics);
        printOverpaidAndUnderpaidManagers(metrics, ListType.OVERPAID);
        printOverpaidAndUnderpaidManagers(metrics, ListType.UNDERPAID);
    }

    private static void printEmployeesFarFromCeo(Metrics metrics) {
        var items = metrics.employeesFarFromCeo();
        System.out.println(MessageFormat.format(
                "Total employees too far from CEO: {0}",
                items.size()));
        if (!items.isEmpty()) {
            System.out.println("List of people far from CEO:");
            System.out.println("Id, First name, Last name, Managers to CEO");
            for (var employee : items) {
                System.out.println(
                        MessageFormat.format("{0}, {1}, {2}, {3}",
                                employee.id(),
                                employee.firstName(),
                                employee.lastName(),
                                employee.totalManagersToCeo()
                        )
                );
            }
        }
        System.out.println();
    }

    private static void printOverpaidAndUnderpaidManagers(Metrics metrics, ListType listType) {
        List<EmployeeResult> items = switch (listType) {
            case OVERPAID -> metrics.overpaidManagers();
            case UNDERPAID -> metrics.underpaidManagers();
            default -> throw new IllegalArgumentException("Unsupported ListType");
        };

        String managerSituation = switch (listType) {
            case OVERPAID -> "overpaid";
            case UNDERPAID -> "underpaid";
            default -> throw new IllegalArgumentException("Unsupported ListType");
        };

        System.out.println(MessageFormat.format(
                "Total {0} managers: {1}",
                managerSituation,
                items.size()));
        if (!items.isEmpty()) {
            System.out.println(MessageFormat.format("List of {0} managers:", managerSituation));
            System.out.println("Id, First name, Last name, Difference from expected pay");
            for (var employee : items) {
                System.out.println(
                        MessageFormat.format("{0}, {1}, {2}, {3}",
                                employee.id(),
                                employee.firstName(),
                                employee.lastName(),
                                employee.differenceFromExpectedPay()
                        )
                );
            }
        }
        System.out.println();
    }

}

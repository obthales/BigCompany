package org.bigcompany;

import org.bigcompany.io.CsvParser;
import org.bigcompany.io.ResultsParser;
import org.bigcompany.service.CompanyService;

import java.io.IOException;
import java.text.MessageFormat;

public class Main {
    public static void main(String[] args) {
        String filePath = "";

        try {
            filePath = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No file path specified.");
            return;
        }

        var csvParser = new CsvParser();
        var service = new CompanyService();

        try {
            var ceo = csvParser.parseCsv(filePath);
            var metrics = service.navigateStructureAndProcessMetrics(ceo);
            ResultsParser.DisplayOnConsole(metrics);
        } catch (IOException e) {
            System.out.println(MessageFormat.format("Error reading file {0}", filePath));
        }
    }
}
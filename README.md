# BigCompany

## About

This sample program reads a CSV with a list of employees from a company and shows some metrics, like overpaid and underpaid managers, and people who are too far down the company hierarchy.

Here's an example of a CSV file:

```csv
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
125,Bob,Ronstad,47000,123
300,Alice,Hasacat,50000,124
305,Brett,Hardleaf,34000,300
```

The program supports CSVs with columns in any given order.

## How to run

Just call the main method and pass the CSV file as argument.

Here is an example of how it looks like:
```bash
C:\Users\MyUser\.jdks\openjdk-23.0.1\bin\java.exe -classpath BigCompany/target/classes org.bigcompany.Main BigCompany/src/test/resources/sample.csv
```

## Disclaimer

The requirement to calculate average salaries for managers was only for their DIRECT subordinates.
However, I initially got it wrong and thought I needed to consider ALL subordinates.

This approach was tested but ended up not being used, since it was not the one required.
However, I decided to not remove the code and leave it there for didatic purposes, since they used a more sophisticated approach to compute average and work with parallel streams.

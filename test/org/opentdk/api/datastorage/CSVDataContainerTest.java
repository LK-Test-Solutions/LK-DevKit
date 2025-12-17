package org.opentdk.api.datastorage;

import org.opentdk.api.filter.EOperator;
import org.opentdk.api.filter.Filter;
import org.opentdk.api.helper.CSVFileGenerator;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CSVDataContainerTest {

    @BeforeTest
    public void prepare() {
        // Prepare
        try {
            Files.createDirectory(Paths.get("tmp"));
        } catch(IOException ignored) {}

        Path filePath = Paths.get("tmp/CSVDataContainerTest.csv");
        if(!Files.exists(filePath)) {
            CSVFileGenerator.newCSVFile("tmp/CSVDataContainerTest.csv");
        }
    }

    @Test
    public void getHeaders() throws IOException {
        DataContainer dc = prepareFile();
        List<String> actual = Arrays.stream(dc.tabInstance().getHeaders()).toList();
        List<String> expected = List.of("ID", "Name", "Alter", "Land");
        Assert.assertEquals(actual, expected);
        System.out.println("Success: Headers are " + actual);
    }

    @Test
    public void getRow() throws IOException {
        DataContainer dc = prepareFile();
        List<String> actual = Arrays.asList(dc.tabInstance().getRow(2));
        List<String> expected = List.of("3", "Hannah" , "35", "Österreich");
        Assert.assertEquals(actual, expected);
        System.out.println("Success: Row is " + actual);
        // Filter
        Filter filter = new Filter();
        filter.addFilterRule("Name", "anna", EOperator.CONTAINS);
        actual = Arrays.asList(dc.tabInstance().getRow(filter));
        Assert.assertEquals(actual, expected);
        System.out.println("Success: Row is " + actual);

    }

    @Test
    public void getRows() throws IOException {
        DataContainer dc = prepareFile();
        Filter filter = new Filter();
        filter.addFilterRule("Name", "Dave", EOperator.EQUALS);

        System.out.println("getRows start ==> " + LocalTime.now());
        int actual = dc.tabInstance().getRows(filter).size();
        System.out.println("getRows end ==> " + LocalTime.now());

        System.out.println("Row size is " + actual); // Has to be checked manually
    }

    @Test
    public void getValue() throws IOException {
        DataContainer dc = prepareFile();
        String actual = dc.tabInstance().getValue(1, "Land");
        String expected = "Schweiz";
        Assert.assertEquals(actual, expected);
        System.out.println("Success: Country is " + actual);
    }

    @Test
    public void getColumn() throws IOException {
        DataContainer dc = prepareFile();
        List<String> actual = dc.tabInstance().getColumn("Alter");
        List<String> expected = List.of("42", "29" , "35", "19", "51", "27", "null", "41", "60", "31");
        int index = 0;
        for(String exp : expected) {
            Assert.assertEquals(actual.get(index), exp);
            System.out.println("Success: Age is " + actual.get(index));
            index++;
        }
        // Filter
        Filter filter = new Filter();
        filter.addFilterRule("Alter", "50", EOperator.GREATER_OR_EQUAL_THAN);
        actual = dc.tabInstance().getColumn("Name", filter);
        Assert.assertEquals(actual.size(), 238054);
        System.out.println("Success: Names size is " + actual.size());
    }

    @Test
    public void addRow() throws IOException {
        DataContainer dc = prepareFile();
        dc.tabInstance().addRow(List.of("x", "Fabian", "29", "Bayern"));
        writeFile(dc);
        String actual = dc.tabInstance().getValue(dc.tabInstance().getRows().size() - 1, "Land");
        String expected = "Bayern";
        Assert.assertEquals(actual, expected);
        System.out.println("Success: Country is " + actual);
    }

    @Test
    public void addColumn() throws IOException {
        DataContainer dc = prepareFile();
        dc.tabInstance().addColumn("Geschlecht");
        dc.tabInstance().addColumn("Name");
        writeFile(dc);
        List<String> actual = dc.tabInstance().getColumn("Name_2");
        List<String> expected = Collections.nCopies(475831, "");
        Assert.assertEquals(actual, expected);
        System.out.println("Success: column created");
    }

    @Test
    public void setValue() throws IOException {
        DataContainer dc = prepareFile();
        dc.tabInstance().setValue("ID", "8", "20");
        writeFile(dc);
        Filter filter = new Filter();
        filter.addFilterRule("ID", "20", EOperator.EQUALS);
        List<String> actual = List.of(dc.tabInstance().getRow(filter));
        List<String> expected = List.of("20", "David", "41", "Österreich");
        Assert.assertEquals(actual, expected);
        System.out.println("Success: Value changed " + actual);
    }

    @Test
    public void setRow() throws IOException {
        DataContainer dc = prepareFile();
        Filter filter = new Filter();
        filter.addFilterRule("Name", "Jul", EOperator.STARTS_WITH);
        dc.tabInstance().setRow(List.of("7", "Julia", "31", "Deutschland").toArray(String[]::new), filter);
        writeFile(dc);
        filter.clear();
        filter.addFilterRule("ID", "7", EOperator.EQUALS);
        List<String> actual = List.of(dc.tabInstance().getRow(filter));
        List<String> expected = List.of("7", "Julia", "31", "Deutschland");
        Assert.assertEquals(actual, expected);
        System.out.println("Success: Value changed " + actual);
    }

    @Test
    public void deleteRow() throws IOException {
        DataContainer dc = prepareFile();
        Filter filter = new Filter();
        filter.addFilterRule("Name", "Ben", EOperator.EQUALS);
        dc.tabInstance().deleteRows(filter);
        writeFile(dc);

        filter.clear();
        filter.addFilterRule("Name", "Ben", EOperator.EQUALS);
        List<String[]> actual = dc.tabInstance().getRows(filter);
        Assert.assertEquals(actual.isEmpty(), true);
        System.out.println("Success: Row for 'Ben' is deleted");
    }

    private DataContainer prepareFile() throws IOException {
        DataContainer dc = DataContainer.newContainer(EContainerFormat.CSV);
        dc.tabInstance().setDelimiter(",");
        dc.readData(Paths.get("tmp/CSVDataContainerTest.csv"));
        return dc;
    }

    private void writeFile(DataContainer dc) throws IOException {
        String filePath = "tmp/writeCSVDataContainerTest.csv";
        Path tempFile = Paths.get(filePath);
        dc.writeData(tempFile);
    }

}

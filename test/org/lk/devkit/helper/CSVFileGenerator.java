package org.lk.devkit.helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class CSVFileGenerator {

    private static final String[] HEADERS = {"ID", "Name", "Alter", "Land"};
    private static final String[] FIXED_LINES = {
            "1,Emma,42,Deutschland",
            "2,Chris,29,Schweiz",
            "3,Hannah,35,Österreich",
            "4,Ben,19,Frankreich",
            "5,Greta,51,Spanien",
            "6,Felix,27,Italien",
            "7,Julia,null,Deutschland",
            "8,David,41,Österreich",
            "9,'Ivan',60,Schweiz",
            "10,Anna,31,Frankreich"
    };
    private static final String[] NAMES = {"John", "Jane", "Alex", "Alice", "Bob", "Carol", "Dave", "Eve"};
    private static final String[] COUNTRIES = {"USA", "Canada", "UK", "Germany", "France", "Italy", "Spain", "Australia"};

    public static void newCSVFile(String name) {
        generateCSVFile(name, 10 * 1024 * 1024); // 10 MB
    }

    private static void generateCSVFile(String filePath, int targetSizeInBytes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write headers
            writer.write(String.join(",", HEADERS));
            writer.newLine();

            // Write fixed lines
            for (String line : FIXED_LINES) {
                writer.write(line);
                writer.newLine();
            }

            // Write random data until the file reaches the target size
            Random random = new Random();
            int id = 11;
            while (Files.size(Paths.get(filePath)) < targetSizeInBytes) {
                String name = NAMES[random.nextInt(NAMES.length)];
                int age = random.nextInt(100);
                String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
                String line = String.format("%d,%s,%d,%s", id++, name, age, country);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
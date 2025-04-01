package main.java.com.hallbooking.common.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FileHandler {
    private static final String DATA_DIRECTORY = "data/";

    static {
        // Ensure the data directory exists
        new File(DATA_DIRECTORY).mkdirs();
    }

    public static <T> List<T> readFromFile(String fileName, Function<String, T> parser) {
        List<T> items = new ArrayList<>();
        File file = new File(DATA_DIRECTORY + fileName);
        System.out.println(fileName);
        System.out.println(file);
        System.out.println("current working directory:" + new File(".").getAbsolutePath());

        if (!file.exists()) {
            System.out.println("File not found: " + file.getPath());
            return items; // Return empty list if file doesn't exist
        } else {
            System.out.println("Reading file: " + file.getPath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                items.add(parser.apply(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + fileName);
            e.printStackTrace();
        }
        return items;
    }

    // New method to read scheduler IDs from a file
    public static List<String> readSchedulerIdsFromFile(String fileName) {
        return readFromFile(fileName, line -> line.split(",")[0].trim()); // Get the first field (ID)
    }


    public static <T> void writeToFile(String fileName, List<T> items, Function<T, String> formatter) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_DIRECTORY + fileName))) {
            for (T item : items) {
                writer.write(formatter.apply(item));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + fileName);
            e.printStackTrace();
        }
    }

    public static void appendToFile(String fileName, String content) {
        try {
            // Create the file if it does not exist
            new File(DATA_DIRECTORY + fileName).createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_DIRECTORY + fileName, true))) {
                writer.write(content);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error appending to file: " + fileName);
            e.printStackTrace();
        }
    }

    public static boolean deleteFromFile(String fileName, String lineToDelete) {
        File inputFile = new File(DATA_DIRECTORY + fileName);
        File tempFile = new File(DATA_DIRECTORY + "temp.txt");

        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().equals(lineToDelete.trim())) {
                    found = true;
                    continue;
                }
                writer.write(currentLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error modifying file: " + fileName);
            e.printStackTrace();
            return false;
        }

        if (found) {
            return inputFile.delete() && tempFile.renameTo(inputFile);
        } else {
            tempFile.delete();
            return false;
        }
    }
}

package main.java.com.hallbooking.part5_manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {
    private static final String BOOKINGS_FILE = "data/bookings.txt";
    private static final String HISTORY_FILE = "data/history.txt";

    public ReportGenerator() {
    }

    public String generateWeeklyReport() {
        return generateReport("Weekly", null, null);
    }

    public String generateMonthlyReport() {
        return generateReport("Monthly", null, null);
    }

    public String generateYearlyReport() {
        return generateReport("Yearly", null, null);
    }

    // New method to generate custom reports
    public String generateCustomReport(LocalDateTime startDate, LocalDateTime endDate) {
        return generateReport("Custom", startDate, endDate);
    }

    private String generateReport(String period, LocalDateTime customStartDate, LocalDateTime customEndDate) {
        List<Double> revenues = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int cancellations = 0;
        double totalCancelledAmount = 0.0;

        // Reading sales data from bookings.txt
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                LocalDateTime bookingStartDate = LocalDateTime.parse(parts[3], formatter); // Booking start date
                double amount = Double.parseDouble(parts[5]); // Amount

                // Check if the booking falls within the custom date range
                if (bookingStartDate.isAfter(customStartDate) && bookingStartDate.isBefore(customEndDate)) {
                    revenues.add(amount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Checking cancellations from history.txt
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                LocalDateTime bookingCancelDate;
                try {
                    bookingCancelDate = LocalDateTime.parse(parts[3], formatter); // Date of cancellation
                } catch (Exception e) {
                    System.err.println("Error parsing date from line: " + line + " - " + e.getMessage());
                    continue; // Skip this iteration if parsing fails
                }

                // Check if the cancellation falls within the custom date range
                if (bookingCancelDate.isAfter(customStartDate) && bookingCancelDate.isBefore(customEndDate)) {
                    cancellations++;
                    totalCancelledAmount += getCancellationAmount(parts[0]); // Get cancellation amount
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        double totalRevenue = revenues.stream().mapToDouble(Double::doubleValue).sum();
        return "Sales Report for Custom Period:\nTotal Bookings: " + revenues.size() + "\nTotal Revenue: $" + totalRevenue
                + "\n\nTotal Cancellations: " + cancellations + "\nTotal Cancelled Amount: $" + totalCancelledAmount;
    }

    public List<Double> getSalesDataForPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime, String period) {
        List<Double> salesData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try (BufferedReader br = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                LocalDateTime bookingDateTime = LocalDateTime.parse(parts[3], formatter); // Start date of booking
                double amount = Double.parseDouble(parts[5]); // Amount

                // Check if the booking is within the selected date range
                if (bookingDateTime.isAfter(startDateTime) && bookingDateTime.isBefore(endDateTime)) {
                    salesData.add(amount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return salesData;
    }

    // Method to get the cancellation amount based on bookingId
    private double getCancellationAmount(String bookingId) {
        double amount = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) { // Update the path as needed
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(bookingId)) { // Match booking ID
                    // Amount is in the last part (index 5) based on your provided format
                    amount = Double.parseDouble(parts[5]); // Get the amount
                    break; // Exit once the booking is found
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return amount;
    }
}








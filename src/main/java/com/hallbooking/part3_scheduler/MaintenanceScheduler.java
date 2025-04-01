package main.java.com.hallbooking.part3_scheduler;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class MaintenanceScheduler {
    private static final String ISSUES_FILE = "data/issues.txt";
    private static final String BOOKINGS_FILE = "data/bookings.txt";
    private static final String MAINTENANCE_FILE = "data/maintenance.txt";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<String[]> getAssignedIssues(String schedulerId) throws IOException {
        List<String[]> assignedIssues = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ISSUES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    if (parts[6].equals(schedulerId) && parts[5].equalsIgnoreCase("ASSIGNED")) {
                        assignedIssues.add(new String[]{
                                parts[0],
                                parts[1],
                                parts[2],
                                parts[3],
                                parts[4],
                                parts.length > 5 ? parts[5] : "Not scheduled", // Start Date/Time
                                parts.length > 6 ? parts[6] : "Not scheduled"  // End Date/Time
                        });
                    }
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        }
        return assignedIssues;
    }

    public void scheduleMaintenance(String schedulerId, String issueId, String hallId,
                                    LocalDateTime startTime, LocalDateTime endTime)
            throws IOException, IllegalArgumentException {
        try {
            validateIssue(schedulerId, issueId);
            checkBookingConflicts(hallId, startTime, endTime);
            recordMaintenance(hallId, startTime, endTime, schedulerId, issueId);
            updateIssueStatus(issueId);
        } catch (IllegalArgumentException e) {
            // Log the error message to help with debugging
            System.err.println("Error scheduling maintenance: " + e.getMessage());
            throw e; // Rethrow if you want the calling method to handle it
        }
    }

    private void validateIssue(String schedulerId, String issueId) throws IOException, IllegalArgumentException {
        try (BufferedReader reader = new BufferedReader(new FileReader(ISSUES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Remove trailing comma if present
                if (line.endsWith(",")) {
                    line = line.substring(0, line.length() - 1);
                }

                String[] parts = line.split(",");
                if (parts.length != 6 && parts.length != 7) {
                    System.err.println("Skipping invalid line format: " + line);
                    continue;
                }

                if (parts[0].equals(issueId)) {
                    if (!parts[5].equalsIgnoreCase("ASSIGNED")) {
                        throw new IllegalArgumentException("Issue is not ASSIGNED");
                    }

                    if (parts.length == 7 && !parts[6].equals(schedulerId)) {
                        throw new IllegalArgumentException("Issue is not assigned to this scheduler");
                    }
                    return; // Valid issue found
                }
            }
        }
        throw new IllegalArgumentException("Issue not found or not assigned to this scheduler");
    }

    private void checkBookingConflicts(String hallId, LocalDateTime startTime, LocalDateTime endTime) throws IOException, IllegalArgumentException {
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Trim leading and trailing whitespace
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    parts = Arrays.stream(parts).map(String::trim).toArray(String[]::new); // Trim each part
                    if (parts[2].equals(hallId)) {
                        try {
                            LocalDateTime bookingStart = LocalDateTime.parse(parts[3], DATE_TIME_FORMATTER);
                            LocalDateTime bookingEnd = LocalDateTime.parse(parts[4], DATE_TIME_FORMATTER);
                            if (startTime.isBefore(bookingEnd) && endTime.isAfter(bookingStart)) {
                                throw new IllegalArgumentException("Proposed maintenance time conflicts with existing bookings");
                            }
                        } catch (DateTimeParseException e) {
                            System.err.println("Invalid date/time format in booking file: " + line);
                            // Continue to next line instead of throwing an exception
                        }
                    }
                } else {
                    System.err.println("Invalid line format in booking file: " + line);
                    // Continue to next line instead of throwing an exception
                }
            }
        }
    }

    private void recordMaintenance(String hallId, LocalDateTime startTime, LocalDateTime endTime,
                                   String schedulerId, String issueId) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAINTENANCE_FILE, true))) {
            writer.write(String.format("%s,%s,%s,%s,%s\n",
                    hallId,
                    startTime.format(DATE_TIME_FORMATTER),
                    endTime.format(DATE_TIME_FORMATTER),
                    schedulerId,
                    issueId));
        }
    }

    private void updateIssueStatus(String issueId) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ISSUES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(issueId)) {
                    parts[5] = "IN_PROGRESS"; // Change status to IN PROGRESS
                    line = String.join(",", parts);
                }
                lines.add(line);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ISSUES_FILE))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}

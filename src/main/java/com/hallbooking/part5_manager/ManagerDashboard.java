package main.java.com.hallbooking.part5_manager;

import main.java.com.hallbooking.common.models.Issue;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.IssueNotFoundException;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.UserNotFoundException;
import main.java.com.hallbooking.common.utils.FileHandler;
import main.java.com.hallbooking.common.utils.DateTimeUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

public class ManagerDashboard {
    private static final String ISSUES_FILE = "issues.txt";
    private static final String SCHEDULERS_FILE = "schedulers.txt";
    private static final String MAINTENANCE_FILE = "maintenance.txt";
    private ReportGenerator reportGenerator;

    public ManagerDashboard() {
        this.reportGenerator = new ReportGenerator();  // Uses the updated ReportGenerator
    }

    public List<Issue> getIssuesByStatus(String status) {
        List<Issue> allIssues = FileHandler.readFromFile(ISSUES_FILE, this::parseIssue);
        return allIssues.stream()
                .filter(issue -> issue.getStatus().toString().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public void assignIssueToScheduler(String issueId, String schedulerId) throws IssueNotFoundException, UserNotFoundException {
        List<Issue> issues = FileHandler.readFromFile(ISSUES_FILE, this::parseIssue);
        List<String[]> schedulersData = FileHandler.readFromFile(SCHEDULERS_FILE, line -> line.split(","));

        // Check if the schedulerId exists and get its status
        String schedulerStatus = null;
        for (String[] scheduler : schedulersData) {
            if (scheduler[0].equals(schedulerId)) {
                schedulerStatus = scheduler[7]; // Assuming the status is the 8th column (index 7)
                break;
            }
        }

        if (schedulerStatus == null) {
            throw new UserNotFoundException(schedulerId);
        }

        // Check if the scheduler is blocked
        if ("Blocked".equals(schedulerStatus)) {
            throw new IllegalStateException("Scheduler cannot be assigned. Currently Suspended.");
        }

        boolean issueFound = false;
        for (int i = 0; i < issues.size(); i++) {
            if (issues.get(i).getId().equals(issueId)) {
                Issue updatedIssue = issues.get(i);
                updatedIssue.assignToScheduler(schedulerId);  // Change status to ASSIGNED
                issues.set(i, updatedIssue);
                issueFound = true;
                break;
            }
        }

        if (!issueFound) {
            throw new IssueNotFoundException(issueId);
        }

        FileHandler.writeToFile(ISSUES_FILE, issues, this::formatIssue);
    }

    public void closeIssues() throws IOException {
        List<String[]> maintenanceRecords = FileHandler.readFromFile(MAINTENANCE_FILE, line -> line.split(","));
        List<String[]> issues = FileHandler.readFromFile(ISSUES_FILE, line -> line.split(","));
        LocalDateTime now = LocalDateTime.now();  // Get the current system time

        for (String[] maintenance : maintenanceRecords) {
            if (maintenance.length >= 5) {
                String issueId = maintenance[4];  // Issue ID is in the 5th position (index 4)
                LocalDateTime endTime = DateTimeUtils.parseDateTime(maintenance[2]);  // End time is in 3rd position

                if (now.isAfter(endTime)) {
                    // Now check the issue status in issues.txt
                    for (int i = 0; i < issues.size(); i++) {
                        String[] issue = issues.get(i);

                        // Issue ID matches and status is IN_PROGRESS
                        if (issue[0].equals(issueId) && issue[5].equals("IN_PROGRESS")) {
                            issue[5] = "CLOSED";  // Update status to CLOSED
                            issues.set(i, issue);  // Update the issues list
                            break;  // No need to check further, go to next maintenance record
                        }
                    }
                }
            }
        }

        // Write the updated issues back to issues.txt
        FileHandler.writeToFile(ISSUES_FILE, issues, line -> String.join(",", line));
    }

    // Updated to work with the modified ReportGenerator
    public String generateSalesReport(String period) {
        switch (period) {
            case "Weekly":
                return reportGenerator.generateWeeklyReport();
            case "Monthly":
                return reportGenerator.generateMonthlyReport();
            case "Yearly":
                return reportGenerator.generateYearlyReport();
            default:
                return "Invalid report period.";
        }
    }

    private Issue parseIssue(String line) {
        String[] parts = line.split(",");

        // Check the length of parts
        if (parts.length < 6 || parts.length > 7) {
            throw new IllegalArgumentException("Invalid issue data: " + line);
        }

        // Set the assignedSchedulerId to empty if not provided
        String assignedSchedulerId = parts.length == 7 ? parts[6] : ""; // Use empty string if not present
        return new Issue(parts[0], parts[1], parts[2], parts[3], parts[4],
                Issue.IssueStatus.valueOf(parts[5]), assignedSchedulerId);
    }

    private String formatIssue(Issue issue) {
        return String.join(",", issue.getId(), issue.getCustomerId(), issue.getBookingId(),
                issue.getHallId(), issue.getDescription(), issue.getStatus().toString(),
                issue.getAssignedSchedulerId() != null ? issue.getAssignedSchedulerId() : "");
    }
}


package main.java.com.hallbooking.common.models;

import java.time.LocalDateTime;

public class Issue {
    private final String id;
    private final String customerId;
    private final String bookingId;
    private final String hallId;
    private String description;
    private LocalDateTime reportedDateTime;
    private IssueStatus status;
    private String assignedSchedulerId = ""; // Initialize as an empty string
    private String resolution;

    public enum IssueStatus {
        OPEN,
        ASSIGNED,      // "ASSIGNED" status
        IN_PROGRESS,
        CLOSED         // Replaced "RESOLVED" with "CLOSED"
    }

    // Constructor accepting parameters
    public Issue(String id, String customerId, String bookingId, String hallId, String description,
                 IssueStatus status, String assignedSchedulerId) {
        this.id = id;
        this.customerId = customerId;
        this.bookingId = bookingId;
        this.hallId = hallId;
        this.description = description;
        this.status = status;
        this.assignedSchedulerId = assignedSchedulerId != null ? assignedSchedulerId : ""; // Default to empty string
        this.reportedDateTime = LocalDateTime.now(); // Set current time
    }

    // Getters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getBookingId() { return bookingId; }
    public String getHallId() { return hallId; }
    public String getDescription() { return description; }
    public LocalDateTime getReportedDateTime() { return reportedDateTime; }
    public IssueStatus getStatus() { return status; }
    public String getAssignedSchedulerId() { return assignedSchedulerId; }
    public String getResolution() { return resolution; }

    // Setters
    public void setDescription(String description) { this.description = description; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public void setAssignedSchedulerId(String assignedSchedulerId) { this.assignedSchedulerId = assignedSchedulerId != null ? assignedSchedulerId : ""; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    // Custom methods
    public boolean isAssigned() {
        return !assignedSchedulerId.isEmpty(); // Checks for non-empty string
    }

    public boolean isResolved() {
        return status == IssueStatus.CLOSED;
    }

    // Assigning issue to scheduler and setting the status to ASSIGNED
    public void assignToScheduler(String schedulerId) {
        this.assignedSchedulerId = schedulerId;
        this.status = IssueStatus.ASSIGNED; // Change status to ASSIGNED
    }

    // Moving an assigned issue to "In Progress"
    public void startProgress() {
        if (this.status == IssueStatus.ASSIGNED) {
            this.status = IssueStatus.IN_PROGRESS;
        } else {
            throw new IllegalStateException("Cannot start progress unless issue is assigned.");
        }
    }

    // Closing an issue
    public void closeIssue() {
        this.status = IssueStatus.CLOSED;
    }

    // Method to format the issue as a CSV string
    public String toCsvString() {
        return String.join(",",
                this.id,
                this.customerId,
                this.bookingId,
                this.hallId,
                this.description,
                this.status.name(),
                this.assignedSchedulerId); // No need for null check, always a string
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", hallId='" + hallId + '\'' +
                ", description='" + description + '\'' +
                ", reportedDateTime=" + reportedDateTime +
                ", status=" + status +
                ", assignedSchedulerId='" + assignedSchedulerId + '\'' +
                ", resolution='" + resolution + '\'' +
                '}';
    }
}

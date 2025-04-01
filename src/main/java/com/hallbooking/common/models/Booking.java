package main.java.com.hallbooking.common.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Booking {
    private final String id;
    private final String customerId;
    private final String hallId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BookingStatus status;

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELED,
        COMPLETED
    }

    public Booking(String customerId, String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.hallId = hallId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = BookingStatus.PENDING;
    }

    // Getters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getHallId() { return hallId; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public BookingStatus getStatus() { return status; }

    // Setters
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    public void setStatus(BookingStatus status) { this.status = status; }

    // Custom methods
    public boolean isOverlapping(Booking other) {
        return this.startDateTime.isBefore(other.endDateTime) && other.startDateTime.isBefore(this.endDateTime);
    }

    public long getDurationHours() {
        return java.time.Duration.between(startDateTime, endDateTime).toHours();
    }

    public boolean canBeCanceled() {
        LocalDateTime cancellationDeadline = startDateTime.minusDays(3);
        return LocalDateTime.now().isBefore(cancellationDeadline) && status == BookingStatus.CONFIRMED;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", hallId='" + hallId + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", status=" + status +
                '}';
    }
}
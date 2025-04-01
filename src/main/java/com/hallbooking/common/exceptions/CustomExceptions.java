package main.java.com.hallbooking.common.exceptions;

public class CustomExceptions {

    public static class UserNotFoundException extends Exception {
        public UserNotFoundException(String userId) {
            super("User not found with ID: " + userId);
        }
    }

    public static class InvalidCredentialsException extends Exception {
        public InvalidCredentialsException(String message) {
            super(message);  // Use the provided message
        }
    }

    public static class HallNotFoundException extends Exception {
        public HallNotFoundException(String hallId) {
            super("Hall not found with ID: " + hallId);
        }
    }

    public static class BookingNotFoundException extends Exception {
        public BookingNotFoundException(String bookingId) {
            super("Booking not found with ID: " + bookingId);
        }
    }

    public static class InvalidBookingException extends Exception {
        public InvalidBookingException(String message) {
            super(message);
        }
    }

    public static class OverlappingBookingException extends Exception {
        public OverlappingBookingException(String hallId, String startTime, String endTime) {
            super("Booking overlaps with existing booking for Hall ID: " + hallId +
                    " from " + startTime + " to " + endTime);
        }
    }

    public static class UnauthorizedAccessException extends Exception {
        public UnauthorizedAccessException(String userId, String action) {
            super("User " + userId + " is not authorized to perform action: " + action);
        }
    }

    public static class InvalidInputException extends Exception {
        public InvalidInputException(String message) {
            super("Invalid input: " + message);
        }
    }

    public static class FileOperationException extends Exception {
        public FileOperationException(String operation, String fileName, String reason) {
            super("File operation '" + operation + "' failed for file '" + fileName + "': " + reason);
        }
    }

    public static class MaintenanceScheduleConflictException extends Exception {
        public MaintenanceScheduleConflictException(String hallId, String startTime, String endTime) {
            super("Maintenance schedule conflicts with existing booking or maintenance for Hall ID: " + hallId +
                    " from " + startTime + " to " + endTime);
        }
    }

    public static class PaymentProcessingException extends Exception {
        public PaymentProcessingException(String message) {
            super("Payment processing failed: " + message);
        }
    }
    public static class IssueNotFoundException extends Exception {
        public IssueNotFoundException(String issueId) {
            super("Issue not found with ID: " + issueId);
        }
    }

}
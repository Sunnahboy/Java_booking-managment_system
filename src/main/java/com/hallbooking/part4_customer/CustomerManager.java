package main.java.com.hallbooking.part4_customer;

import main.java.com.hallbooking.common.models.User;
import main.java.com.hallbooking.common.models.Issue;
import main.java.com.hallbooking.common.utils.FileHandler;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerManager {
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String ISSUES_FILE = "issues.txt";
    private BookingManager bookingManager;

    public CustomerManager() {
        this.bookingManager = new BookingManager();
    }

    // Method to update a customer's profile
    public void updateCustomerProfile(User customer) throws UserNotFoundException {
        List<User> customers = FileHandler.readFromFile(CUSTOMERS_FILE, this::parseCustomer);
        Optional<User> existingCustomer = customers.stream()
                .filter(c -> c.getId().equals(customer.getId()))
                .findFirst();
        if (existingCustomer.isPresent()) {
            customers.set(customers.indexOf(existingCustomer.get()), customer);
            FileHandler.writeToFile(CUSTOMERS_FILE, customers, this::formatCustomer);
        } else {
            throw new UserNotFoundException(customer.getId());
        }
    }

    // Method to raise an issue for a booking
    public void raiseIssue(String customerId, String bookingId, String hallId, String description) {
        Issue newIssue = new Issue(
                generateIssueId(),
                customerId,
                bookingId,
                hallId,
                description,
                Issue.IssueStatus.OPEN,
                null
        );
        String issueCsv = newIssue.toCsvString();
        FileHandler.appendToFile(ISSUES_FILE, issueCsv);
    }

    // Method to fetch all bookings for the customer from BookingManager
    public List<String> getCustomerBookings(String customerId) {
        return bookingManager.getBookingsForCustomer(customerId);
    }

    public List<String> getPastBookings(String customerId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return bookingManager.getBookingsForCustomer(customerId).stream()
                .filter(booking -> {
                    String[] parts = booking.split(",");
                    // Ensure there are enough parts before parsing
                    if (parts.length >= 5) {
                        LocalDateTime bookingEnd;
                        try {
                            // Parse the end time using the custom formatter
                            bookingEnd = LocalDateTime.parse(parts[4], formatter);
                        } catch (DateTimeParseException e) {
                            System.err.println("Invalid date format in booking: " + booking);
                            return false; // Skip this booking if the date format is invalid
                        }
                        return bookingEnd.isBefore(LocalDateTime.now());
                    }
                    return false; // Return false if there are not enough parts
                })
                .collect(Collectors.toList());
    }

    // Method to fetch future bookings for the customer
    public List<String> getFutureBookings(String customerId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return bookingManager.getBookingsForCustomer(customerId).stream()
                .filter(booking -> {
                    String[] parts = booking.split(",");
                    LocalDateTime bookingStart;

                    try {
                        // Parse the start time using the custom formatter
                        bookingStart = LocalDateTime.parse(parts[3], formatter);
                    } catch (DateTimeParseException e) {
                        // Handle invalid date format
                        System.err.println("Invalid date format in booking: " + booking);
                        return false; // Exclude invalid entries from the result
                    }

                    // Return true for bookings that are in the future
                    return bookingStart.isAfter(LocalDateTime.now());
                })
                .collect(Collectors.toList());
    }

    // Method to cancel a customer's booking via BookingManager
    public void cancelBooking(String bookingId) throws BookingNotFoundException, InvalidBookingException {
        bookingManager.cancelBooking(bookingId);
    }

    // Method to handle booking operations through BookingManager
    public void bookHall(String customerId, String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws InvalidBookingException, HallNotFoundException {
        bookingManager.bookHall(customerId, hallId, startDateTime, endDateTime);
    }

    // Helper methods for customer profile management
    private User parseCustomer(String line) {
        String[] parts = line.split(",");
        return new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]) {
            @Override
            public String getUserType() {
                return "Customer";
            }
        };
    }

    public User getCustomerById(String customerId) throws UserNotFoundException {
        List<User> customers = FileHandler.readFromFile(CUSTOMERS_FILE, this::parseCustomer);
        return customers.stream()
                .filter(c -> c.getId().equals(customerId))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(customerId));
    }

    private String formatCustomer(User customer) {
        return String.join(",", customer.getId(), customer.getPassword(),
            customer.getEmail(), customer.getPhoneNumber(), customer.getName(),
            customer.getAddress(), customer.getNationality(), "Unblocked", "Customer");
    }

    // Method to generate a unique issue ID
    private String generateIssueId() {
        return "I" + System.currentTimeMillis();
    }

    // Getter for BookingManager
    public BookingManager getBookingManager() {
        return this.bookingManager;
    }
}

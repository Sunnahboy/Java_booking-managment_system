package main.java.com.hallbooking.part4_customer;

import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.common.utils.DateTimeUtils;
import main.java.com.hallbooking.common.utils.FileHandler;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class BookingManager {
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String HALLS_FILE = "hall.txt";
    private static final String HISTORY_FILE = "history.txt";
    private static final String MAINTENANCE_FILE = "data/maintenance.txt";

    public void bookHall(String customerId, String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws InvalidBookingException, HallNotFoundException {
        if (!isHallAvailable(hallId, startDateTime, endDateTime)) {
            throw new InvalidBookingException("Hall is not available for the selected time slot.");
        }

        if (isHallUnderMaintenance(hallId, startDateTime, endDateTime)) {
            throw new InvalidBookingException("The selected hall is under maintenance during the requested time.");
        }

        double totalRate = calculateBookingPrice(hallId, startDateTime, endDateTime);

        String bookingId = generateBookingId();
        String booking = String.join(",", bookingId, customerId, hallId,
                DateTimeUtils.formatDateTime(startDateTime),
                DateTimeUtils.formatDateTime(endDateTime), String.valueOf(totalRate));
        FileHandler.appendToFile(BOOKINGS_FILE, booking);

        finalizeBooking(customerId, hallId, totalRate, startDateTime, endDateTime);
    }

    public boolean isHallUnderMaintenance(String hallId, LocalDateTime bookingStart, LocalDateTime bookingEnd) {
        List<MaintenanceRecord> maintenanceRecords = readMaintenanceRecords();

        for (MaintenanceRecord record : maintenanceRecords) {
            if (record.hallId.equals(hallId)) {
                if (isTimeOverlap(bookingStart, bookingEnd, record.startTime, record.endTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private List<MaintenanceRecord> readMaintenanceRecords() {
        List<MaintenanceRecord> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try {
            List<String> lines = Files.readAllLines(Paths.get(MAINTENANCE_FILE));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String hallId = parts[0];
                    LocalDateTime startTime = LocalDateTime.parse(parts[1], formatter);
                    LocalDateTime endTime = LocalDateTime.parse(parts[2], formatter);
                    records.add(new MaintenanceRecord(hallId, startTime, endTime));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading maintenance file: " + e.getMessage());
        }

        return records;
    }

    private static class MaintenanceRecord {
        String hallId;
        LocalDateTime startTime;
        LocalDateTime endTime;

        MaintenanceRecord(String hallId, LocalDateTime startTime, LocalDateTime endTime) {
            this.hallId = hallId;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public void cancelBooking(String bookingId) throws BookingNotFoundException, InvalidBookingException {
        // Read the list of bookings from the file
        List<String> bookings = FileHandler.readFromFile(BOOKINGS_FILE, line -> line);

        // Find the booking to cancel
        String bookingToCancel = bookings.stream()
                .filter(b -> b.startsWith(bookingId))
                .findFirst()
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Extract and parse the start time (4th field in the comma-separated booking string)
        String bookingStartTime = bookingToCancel.split(",")[3]; // Start time is at index 3

        LocalDateTime bookingStart;
        try {
            // Assuming DateTimeUtils expects format 'yyyy-MM-dd HH:mm'
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            bookingStart = LocalDateTime.parse(bookingStartTime, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidBookingException("Invalid booking start time format.");
        }

        // Check if the booking can be canceled (at least 3 days before start time)
        if (LocalDateTime.now().plusDays(3).isAfter(bookingStart)) {
            throw new InvalidBookingException("Bookings can only be canceled at least 3 days before the start time.");
        }

        // Remove the booking from the list
        bookings.remove(bookingToCancel);

        // Write the updated bookings list back to the file
        FileHandler.writeToFile(BOOKINGS_FILE, bookings, booking -> booking);

        // Append the canceled booking to the history file
        FileHandler.appendToFile(HISTORY_FILE, bookingToCancel);
    }

    public List<String> getBookingsForCustomer(String customerId) {
        List<String> bookings = FileHandler.readFromFile(BOOKINGS_FILE, line -> line);
        return bookings.stream()
                .filter(b -> b.split(",")[1].equals(customerId))
                .collect(Collectors.toList());
    }

    public boolean isHallAvailable(String hallId, LocalDateTime start, LocalDateTime end) {
        List<String> bookings = FileHandler.readFromFile(BOOKINGS_FILE, line -> line);
        return bookings.stream().noneMatch(b -> {
            String[] parts = b.split(",");

            // Ensure the parts array has exactly 6 elements (Booking ID, Customer ID, Hall ID, Start, End, Price)
            if (parts.length != 6) {
                System.err.println("Invalid booking data: " + b);
                return false;  // Skip this invalid entry
            }

            String bookedHallId = parts[2];
            LocalDateTime bookedStart = DateTimeUtils.parseDateTime(parts[3]);
            LocalDateTime bookedEnd = DateTimeUtils.parseDateTime(parts[4]);

            // Check for time overlap
            return bookedHallId.equals(hallId) && (start.isBefore(bookedEnd) && end.isAfter(bookedStart));
        });
    }

    public Hall getHallById(String hallId) throws HallNotFoundException {
        List<Hall> halls = FileHandler.readFromFile(HALLS_FILE, this::parseHall);
        return halls.stream()
                .filter(h -> h.getId().equals(hallId))
                .findFirst()
                .orElseThrow(() -> new HallNotFoundException(hallId));
    }

    private Hall parseHall(String line) {
        String[] parts = line.split(",");
        return new Hall(parts[0], Hall.HallType.valueOf(parts[1]), Integer.parseInt(parts[2]),
                new java.math.BigDecimal(parts[3]), parts[4]);
    }

    private String generateBookingId() {
        return "B" + System.currentTimeMillis();
    }

    public List<Hall> filterHalls(String type, int capacity, String location, double maxRate) {
        List<Hall> halls = FileHandler.readFromFile(HALLS_FILE, this::parseHall);
        return halls.stream()
                .filter(hall -> (type.equals("Any") || hall.getType().name().equalsIgnoreCase(type)) &&
                        (capacity == 0 || hall.getCapacity() >= capacity) &&
                        (location.isEmpty() || hall.getLocation().equalsIgnoreCase(location)) &&
                        (maxRate == 0 || hall.getRate().doubleValue() <= maxRate))
                .collect(Collectors.toList());
    }

    public List<Hall> getAllAvailableHalls() {
        return FileHandler.readFromFile(HALLS_FILE, this::parseHall);
    }

    public double calculateBookingPrice(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) throws HallNotFoundException {
        System.out.println("calculateBookingPrice called with hallId: " + hallId);

        Hall hall = getHallById(hallId);
        if (hall == null) {
            System.out.println("Hall not found for ID: " + hallId);
            throw new HallNotFoundException("Hall with ID " + hallId + " not found.");
        }
        System.out.println("Hall found: " + hall.toString());

        long hours = DateTimeUtils.calculateHours(startDateTime, endDateTime);
        System.out.println("Hours calculated: " + hours);

        double rate = hall.getRate().doubleValue();
        System.out.println("Rate per hour: " + rate);

        double totalPrice = rate * hours;
        System.out.println("Total booking price calculated: " + totalPrice);

        return totalPrice;
    }

    public void finalizeBooking(String customerId, String hallId, double totalPrice, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Removed receipt generation and file writing logic related to receipts
        // If necessary, you may need to log or handle the finalized booking differently
    }

    private String generateReceiptId() {
        return "R" + System.currentTimeMillis();
    }
}

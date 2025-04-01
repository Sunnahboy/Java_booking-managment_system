package main.java.com.hallbooking.part3_scheduler;

import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;
import main.java.com.hallbooking.common.utils.FileHandler;
import main.java.com.hallbooking.common.utils.DateTimeUtils;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SchedulerManager {
    private static final String HALL_FILE = "hall.txt";
    private static final String HALL_AVAILABILITY_FILE = "hallAvailability.txt";
    private static final String Availability = "data/hallAvailability.txt";
    private static final String MAINTENANCE_FILE = "maintenance.txt";
    private static final String BOOKING_FILE = "data/bookings.txt";

    private static final String MAINTENANCE = "data/maintenance.txt";
    private static boolean isInitialized = false;  // Add this line

    private static List<Hall> halls;

    public SchedulerManager() {
        if (!isInitialized) {
            initializeDefaultHalls();
            isInitialized = true;
        }
        loadHalls();

    }

    private void initializeDefaultHalls() {
        File file = new File(HALL_FILE);
        System.out.println("Checking halls file: " + HALL_FILE);

        if (!file.exists() || file.length() == 0) {
            // File doesn't exist or is empty
            List<Hall> existingHalls = FileHandler.readFromFile(HALL_FILE, this::parseHall);

            if (existingHalls.isEmpty()) {
                // Only create default halls if there are no existing halls
                List<Hall> defaultHalls = createDefaultHalls();
                FileHandler.writeToFile(HALL_FILE, defaultHalls, this::formatHall);
                System.out.println("Created and wrote default halls to " + HALL_FILE);
            } else {
                System.out.println("Existing halls found. Not creating default halls.");
            }
        } else {
            System.out.println("Hall file exists and is not empty. Using existing data.");
        }
    }
    private List<Hall> createDefaultHalls() {
        List<Hall> defaultHalls = new ArrayList<>();
        defaultHalls.add(new Hall("H17354", Hall.HallType.AUDITORIUM, 1000, new BigDecimal("300.00"), "Main Building"));
        defaultHalls.add(new Hall("H27477", Hall.HallType.BANQUET_HALL, 300, new BigDecimal("100.00"), "Event Center"));
        defaultHalls.add(new Hall("H84843", Hall.HallType.MEETING_ROOM, 30, new BigDecimal("50.00"), "Business Wing"));
        return defaultHalls;
    }

    private void loadHalls() {
        halls = FileHandler.readFromFile(HALL_FILE, this::parseHall);
    }

    public void addHall(Hall hall) throws InvalidInputException {
        validateHallUniqueness(hall);
        setDefaultHallProperties(hall);
        halls.add(hall);
        saveHalls();
    }

    private void validateHallUniqueness(Hall hall) throws InvalidInputException {
        if (halls.stream().anyMatch(h -> h.getId().equals(hall.getId()))) {
            throw new InvalidInputException("Hall ID already exists");
        }
    }

    private void setDefaultHallProperties(Hall hall) throws InvalidInputException {
        switch (hall.getType()) {
            case AUDITORIUM:
                hall.setCapacity(1000);
                hall.setRate(new BigDecimal("300.00"));
                break;
            case BANQUET_HALL:
                hall.setCapacity(300);
                hall.setRate(new BigDecimal("100.00"));
                break;
            case MEETING_ROOM:
                hall.setCapacity(30);
                hall.setRate(new BigDecimal("50.00"));
                break;
            default:
                throw new InvalidInputException("Invalid hall type");
        }
        if (hall.getLocation().isEmpty()) {
            hall.setLocation("Default Location");
        }
    }

    public void updateHall(Hall updatedHall) throws HallNotFoundException {
        Hall hallToUpdate = getHallById(updatedHall.getId());
        updateHallDetails(hallToUpdate, updatedHall);
        saveHalls();
        System.out.println("Hall updated successfully. New location: " + hallToUpdate.getLocation());
    }

    private void updateHallDetails(Hall existingHall, Hall newHall) {
        existingHall.setType(newHall.getType());
        existingHall.setLocation(newHall.getLocation().isEmpty() ? existingHall.getLocation() : newHall.getLocation());
    }

    public void deleteHall(String hallId) throws HallNotFoundException, InvalidInputException {
        // First, check if the hall exists
        if (!halls.stream().anyMatch(h -> h.getId().equals(hallId))) {
            throw new HallNotFoundException(hallId);
        }

        // Check if the hall has any bookings
        if (isHallBooked(hallId)) {
            throw new InvalidInputException("Cannot delete hall " + hallId + " as it has active bookings.");
        }

        // If we've reached here, the hall exists and has no bookings
        boolean removed = halls.removeIf(h -> h.getId().equals(hallId));
        if (removed) {
            saveHalls(); // Save updated hall list

            // Remove the hall from HallAvailability.txt
            try {
                File availabilityFile = new File(Availability);
                List<String> lines = new ArrayList<>();

                // Read all lines from the file
                BufferedReader reader = new BufferedReader(new FileReader(availabilityFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Split the line by commas to extract the hallId (first part)
                    String[] parts = line.split(",");
                    String availableHallId = parts[0].trim();

                    // If the hallId in the line does not match the hallId to delete, keep it
                    if (!availableHallId.equals(hallId)) {
                        lines.add(line);
                    }
                }
                reader.close();

                // Write the updated lines back to the file
                BufferedWriter writer = new BufferedWriter(new FileWriter(availabilityFile));
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
                writer.close();

                System.out.println("Hall " + hallId + " has been successfully deleted from both records.");
            } catch (IOException e) {
                System.err.println("An error occurred while updating HallAvailability.txt: " + e.getMessage());
            }

        } else {
            // This should never happen, but just in case
            throw new HallNotFoundException(hallId);
        }
    }

    private boolean isHallBooked(String hallId) {
        File bookingFile = new File(BOOKING_FILE);

        if (!bookingFile.exists()) {
            return false; // If the file doesn't exist, assume no bookings
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try (BufferedReader reader = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",\\s*");
                if (parts.length >= 5 && parts[2].trim().equals(hallId)) {
                    try {
                        LocalDateTime bookingEndTime = LocalDateTime.parse(parts[4].trim(), formatter);
                        if (bookingEndTime.isAfter(now)) {
                            return true; // Hall has a future booking
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing date: " + e.getMessage());
                        // If there's an error parsing the date, assume the booking is valid
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading booking file: " + e.getMessage());
            // If there's an error reading the file, err on the side of caution
            return true;
        }

        return false; // Hall is not booked for any future dates
    }


    private void validateHallDeletion(String hallId) throws InvalidInputException {
        if (hasActiveBookings(hallId) || hasScheduledMaintenance(hallId)) {
            throw new InvalidInputException("Cannot delete hall with active bookings or scheduled maintenance");
        }
    }

    public List<Hall> getHalls() {
        return new ArrayList<>(halls);
    }

    public static Hall getHallById(String hallId) throws HallNotFoundException {
        return halls.stream()
                .filter(h -> h.getId().equals(hallId))
                .findFirst()
                .orElseThrow(() -> new HallNotFoundException(hallId));
    }

    public void setHallAvailability(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws HallNotFoundException, InvalidBookingException {
        validateAvailabilityInput(hallId, startDateTime, endDateTime);
        String availability = createAvailabilityRecord(hallId, startDateTime, endDateTime);
        FileHandler.appendToFile(HALL_AVAILABILITY_FILE, availability);
    }

    private void validateAvailabilityInput(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws HallNotFoundException, InvalidBookingException {

        // Check if the hall exists
        if (!hallExists(hallId)) {
            throw new HallNotFoundException(hallId);
        }

        // Ensure that the end date/time is after the start date/time
        if (endDateTime.isBefore(startDateTime)) {
            throw new InvalidBookingException("End date/time must be after start date/time.");
        }

        // Check if the booking is only on weekdays
        if (!DateTimeUtils.isWeekday(startDateTime.toLocalDate()) || !DateTimeUtils.isWeekday(endDateTime.toLocalDate())) {
            throw new InvalidBookingException("Availability can only be set for weekdays.");
        }

        // Ensure that the booking is within business hours (8 AM to 6 PM)
        if (!DateTimeUtils.isWithinBusinessHours(startDateTime.toLocalTime()) ||
                !DateTimeUtils.isWithinBusinessHours(endDateTime.toLocalTime())) {
            throw new InvalidBookingException("Availability can only be set within business hours (8 AM - 6 PM).");
        }

        // Check if the availability is already set for the hall and date range (ignore time)
        if (isAvailabilityAlreadySet(hallId, startDateTime.toLocalDate(), endDateTime.toLocalDate())) {
            throw new InvalidBookingException("Hall with this ID is already set for the given date range.");
        }
    }

    private String createAvailabilityRecord(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return String.join(",", hallId,
                DateTimeUtils.formatDateTime(startDateTime),
                DateTimeUtils.formatDateTime(endDateTime));
    }

    public void scheduleMaintenance(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime, String remark)
            throws HallNotFoundException, InvalidBookingException, MaintenanceScheduleConflictException {
        if (!hallExists(hallId)) {
            throw new HallNotFoundException(hallId);
        }
        validateMaintenanceSchedule(startDateTime, endDateTime, hallId);
        String maintenanceId = generateMaintenanceId();
        String maintenanceRecord = createMaintenanceRecord(maintenanceId, hallId, startDateTime, endDateTime, remark);
        FileHandler.appendToFile(MAINTENANCE_FILE, maintenanceRecord);
    }

    public class MaintenanceScheduleConflictException extends Exception {
        public MaintenanceScheduleConflictException(String hallId, String startDateTime, String endDateTime) {
            super(String.format("Maintenance schedule conflict for hall %s from %s to %s", hallId, startDateTime, endDateTime));
        }

        public MaintenanceScheduleConflictException(String message) {
            super(message);
        }
    }

    private void validateMaintenanceSchedule(LocalDateTime startDateTime, LocalDateTime endDateTime, String hallId)
            throws InvalidBookingException, MaintenanceScheduleConflictException {
        if (startDateTime.isBefore(LocalDateTime.now()) || endDateTime.isBefore(startDateTime)) {
            throw new InvalidBookingException("Invalid date range");
        }
        if (isOverlappingWithBooking(hallId, startDateTime, endDateTime)) {
            throw new MaintenanceScheduleConflictException(hallId,
                    DateTimeUtils.formatDateTime(startDateTime),
                    DateTimeUtils.formatDateTime(endDateTime));
        }
        if (hasScheduledMaintenance(hallId, startDateTime.toLocalDate(), endDateTime.toLocalDate())) {
            throw new MaintenanceScheduleConflictException("Hall already has scheduled maintenance during this period");
        }
        if (isAvailabilityAlreadySet(hallId, startDateTime.toLocalDate(), endDateTime.toLocalDate())) {
            throw new MaintenanceScheduleConflictException("Hall is marked as available during this period");
        }
    }

    private String createMaintenanceRecord(String maintenanceId, String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime, String remark) {
        return String.join(",", maintenanceId, hallId,
                DateTimeUtils.formatDateTime(startDateTime),
                DateTimeUtils.formatDateTime(endDateTime),
                remark);
    }

    private void saveHalls() {
        FileHandler.writeToFile(HALL_FILE, halls, this::formatHall);
    }

    private Hall parseHall(String line) {
        String[] parts = line.split(",");
        return new Hall(
                parts[0].trim(),
                Hall.HallType.valueOf(parts[1].trim().toUpperCase().replace(' ', '_')),
                Integer.parseInt(parts[2].trim()),
                new BigDecimal(parts[3].trim()),
                parts[4].trim()
        );
    }

    private String formatHall(Hall hall) {
        return String.format("%s,%s,%d,%.2f,%s",
                hall.getId(),
                hall.getType().toString(),  // Write the enum value as is
                hall.getCapacity(),
                hall.getRate(),
                hall.getLocation());
    }

    private boolean hallExists(String hallId) {
        return halls.stream().anyMatch(h -> h.getId().equals(hallId));
    }

    private boolean isOverlappingWithBooking(String hallId, LocalDateTime start, LocalDateTime end) {
        File bookingFile = new File(BOOKING_FILE);
        if (!bookingFile.exists()) {
            System.out.println("Booking file not found. Assuming no overlapping bookings.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] bookingDetails = line.split(",");
                if (bookingDetails.length < 4) continue;

                String bookingHallId = bookingDetails[1].trim();
                LocalDateTime bookingStart = LocalDateTime.parse(bookingDetails[2].trim());
                LocalDateTime bookingEnd = LocalDateTime.parse(bookingDetails[3].trim());

                if (hallId.equals(bookingHallId) &&
                        !(end.isBefore(bookingStart) || start.isAfter(bookingEnd))) {
                    return true;
                }
            }
        } catch (IOException | DateTimeParseException e) {
            System.err.println("Error checking for overlapping bookings: " + e.getMessage());
        }
        return false;
    }

    private boolean hasActiveBookings(String hallId) {
        File bookingFile = new File(BOOKING_FILE);
        if (!bookingFile.exists()) {
            System.out.println("Booking file not found. Assuming no active bookings.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] bookingDetails = line.split(",");
                if (bookingDetails.length > 1 && bookingDetails[1].trim().equals(hallId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking for active bookings: " + e.getMessage());
        }
        return false;
    }

    private boolean hasScheduledMaintenance(String hallId) {
        LocalDate now = LocalDate.now();
        LocalDate futureDate = now.plusYears(1);
        return hasScheduledMaintenance(hallId, now, futureDate);
    }

    private boolean hasScheduledMaintenance(String hallId, LocalDate startDate, LocalDate endDate) {
        File maintenanceFile = new File(MAINTENANCE);
        if (!maintenanceFile.exists()) {
            System.out.println("Maintenance file not found. Assuming no scheduled maintenance.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(maintenanceFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] maintenanceDetails = line.split(",");
                String existingHallId = maintenanceDetails[1].trim();
                LocalDate maintenanceStart = LocalDate.parse(maintenanceDetails[2].substring(0, 10));
                LocalDate maintenanceEnd = LocalDate.parse(maintenanceDetails[3].substring(0, 10));

                if (existingHallId.equals(hallId) && !(endDate.isBefore(maintenanceStart) || startDate.isAfter(maintenanceEnd))) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking for scheduled maintenance: " + e.getMessage());
        }
        return false;
    }

    private boolean isAvailabilityAlreadySet(String hallId, LocalDate startDate, LocalDate endDate) {
        File availabilityFile = new File(Availability);
        if (!availabilityFile.exists()) {
            System.out.println("Availability file not found. Assuming no availabilities set.");
            return false; // Assume available if the file does not exist
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(availabilityFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue; // Skip invalid lines

                String existingHallId = parts[0].trim();
                // Parse only the date part (first 10 characters) from the date-time string
                LocalDate existingStartDate = LocalDate.parse(parts[1].trim().substring(0, 10)); // "2024-09-23"
                LocalDate existingEndDate = LocalDate.parse(parts[2].trim().substring(0, 10));   // "2024-09-23"

                if (existingHallId.equals(hallId)) {
                    // Check for date overlap
                    if (!(endDate.isBefore(existingStartDate) || startDate.isAfter(existingEndDate))) {
                        System.out.println("Hall ID " + hallId + " is already booked for these dates.");
                        return true; // Overlap detected
                    }
                }
            }
        } catch (IOException | DateTimeParseException e) {
            System.err.println("Error checking for existing availability: " + e.getMessage());
        }
        return false; // No overlap found
    }

    private String generateMaintenanceId() {
        return "M" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}













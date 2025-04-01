package main.java.com.hallbooking.part2_admin;

import main.java.com.hallbooking.common.models.User;
import main.java.com.hallbooking.common.utils.FileHandler;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;

import java.util.List;
import java.util.stream.Collectors;

public class AdminManager {
    private static final String USERS_FILE = "users.txt";
    private static final String SCHEDULERS_FILE = "schedulers.txt";
    private static final String CUSTOMERS_FILE = "customers.txt";

    // Load users from file
    public List<User> loadUsersFromFile(String fileName) {
        return FileHandler.readFromFile(fileName, this::parseUser);
    }

    // Create user with full name for userType
    public void createUser(User user) throws InvalidInputException {
        String idPrefix = user.getId().substring(0, 1).toUpperCase();
        String fileName;
        String userType;

        // Determine the file and full user type based on the ID prefix
        switch (idPrefix) {
            case "M":
                fileName = USERS_FILE;
                userType = "Manager";
                break;
            case "S":
                fileName = SCHEDULERS_FILE;
                userType = "Scheduler";
                break;
            case "C":
                fileName = CUSTOMERS_FILE;
                userType = "Customer";
                break;
            case "A":
                fileName = USERS_FILE;
                userType = "Admin";
                break;
            default:
                throw new InvalidInputException("Invalid user ID prefix. Must start with M (Manager), S (Scheduler), or C (Customer) or A (Admin).");
        }

        // Ensure ID uniqueness
        List<User> users = loadUsersFromFile(fileName);
        if (users.stream().anyMatch(u -> u.getId().equals(user.getId()))) {
            throw new InvalidInputException("User ID already exists");
        }

        // Validate fields (ensure no empty fields)
        validateUserFields(user);

        // Append the user to the corresponding file with full user type
        FileHandler.appendToFile(fileName, formatUser(user, userType));
    }

    private String determineUserFile(String userId) {
        if (getUserById(USERS_FILE, userId) != null) {
            return USERS_FILE;
        } else if (getUserById(SCHEDULERS_FILE, userId) != null) {
            return SCHEDULERS_FILE;
        } else if (getUserById(CUSTOMERS_FILE, userId) != null) {
            return CUSTOMERS_FILE;
        }
        return null; // User not found in any file
    }

    public void editUserById(User updatedUser) throws UserNotFoundException {
        String fileName = determineUserFile(updatedUser.getId());
        if (fileName == null) {
            throw new UserNotFoundException(updatedUser.getId());
        }

        List<User> users = loadUsersFromFile(fileName);

        User existingUser = users.stream()
                .filter(u -> u.getId().equals(updatedUser.getId()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(updatedUser.getId()));

        // Preserve the userType and update other fields
        String originalUserType = existingUser.getUserType();
        String originalStatus = existingUser.getStatus();

        // Update the fields as necessary
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setName(updatedUser.getName());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setNationality(updatedUser.getNationality());
        existingUser.setStatus(updatedUser.getStatus());

        // Ensure the userType remains unchanged
        existingUser.setStatus(originalStatus);
        existingUser.setUserType(originalUserType);

        // Write updated users back to the file
        FileHandler.writeToFile(fileName, users, this::formatUser);
    }


    // This method converts a line from the file into a User object
    private User mapLineToUser(String line) {
        String[] parts = line.split(",");

        // Create a User object with all necessary fields
        User user = new User(
                parts[0],  // id
                parts[1],  // password
                parts[2],  // email
                parts[3],  // phoneNumber
                parts[4],  // name
                parts[5],  // address
                parts[6]   // nationality
        ) {
            @Override
            public String getUserType() {
                return "";
            }
        };

        // Set status and userType directly, as there are no subclasses
        user.setStatus(parts[7]);
        user.setUserType(parts[8]);

        return user;
    }

    // Delete user
    public void deleteUser(String userId) throws UserNotFoundException, InvalidInputException {
        String fileName = getUserFileFromIdPrefix(userId);
        if (fileName == null) {
            throw new InvalidInputException("Invalid user ID. Cannot delete user.");
        }

        // Load users from the correct file and remove the user
        List<User> users = loadUsersFromFile(fileName);
        User userToDelete = users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(userId));

        users.remove(userToDelete);
        FileHandler.writeToFile(fileName, users, u -> formatUser(u, userToDelete.getUserType()));
    }

    // Block a user
    public void blockUser(String userId) throws UserNotFoundException {
        updateUserStatus(userId, "Blocked");
    }

    // Unblock a user
    public void unblockUser(String userId) throws UserNotFoundException {
        updateUserStatus(userId, "Unblocked");
    }

    // Filter users based on criteria
    public List<User> filterUsers(String fileName, String criteria, String value) {
        List<User> users = loadUsersFromFile(fileName);
        return users.stream()
                .filter(u -> matchesCriteria(u, criteria, value))
                .collect(Collectors.toList());
    }

    // Find user by ID across all files
    public User getUserByIdAcrossFiles(String userId) {
        User user = getUserById(USERS_FILE, userId);
        if (user != null) {
            return user;
        }

        user = getUserById(SCHEDULERS_FILE, userId);
        if (user != null) {
            return user;
        }

        return getUserById(CUSTOMERS_FILE, userId);
    }

    private void updateUserStatus(String userId, String newStatus) throws UserNotFoundException {
        String fileName = getUserFileFromIdPrefix(userId);

        if (fileName == null) {
            throw new UserNotFoundException("Invalid ID prefix.");
        }

        List<User> users = loadUsersFromFile(fileName);

        User userToUpdate = users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Update only the status, keep the rest of the user's info unchanged
        userToUpdate.setStatus(newStatus);

        // Write the updated list of users back to the file
        FileHandler.writeToFile(fileName, users, this::formatUser);
    }

    // Determine file based on user ID prefix
    private String getUserFileFromIdPrefix(String id) {
        if (id.startsWith("M")) {
            return USERS_FILE;
        } else if (id.startsWith("S")) {
            return SCHEDULERS_FILE;
        } else if (id.startsWith("C")) {
            return CUSTOMERS_FILE;
        } else if (id.startsWith("A")) {
            return USERS_FILE;
        }
        return null; // Invalid prefix
    }


    public class RegularUser extends User {
        private String userType;

        public RegularUser(String id, String password, String email, String phoneNumber,
                           String name, String address, String nationality, String userType) {
            super(id, password, email, phoneNumber, name, address, nationality);
            this.userType = userType; // Set userType based on the provided argument
        }

        @Override
        public String getUserType() {
            return userType; // Return the preserved userType
        }
    }

    private User parseUser(String line) {
        String[] parts = line.split(",");
        if (parts.length < 9) {
            throw new IllegalArgumentException("Invalid user data: " + line);
        }
        RegularUser user = new RegularUser(parts[0], parts[1], parts[2], parts[3],
                parts[4], parts[5], parts[6], parts[8]); // Pass userType

        user.setStatus(parts[7].equals("Blocked") ? "Blocked" : "Unblocked");

        // Debugging output
        System.out.println("Parsed user: " + user.getId() + ", Status: " + user.getStatus() + ", UserType: " + user.getUserType());

        return user;
    }

    // Format a user for saving to file
    private String formatUser(User user, String userType) {
        return String.join(",", user.getId(),
                user.getPassword(), user.getEmail(), user.getPhoneNumber(), user.getName(), user.getAddress(),
                user.getNationality(), user.getStatus(), user.getUserType());
    }

    // Format a user with preserved user type
    private String formatUser(User user) {
        return String.join(",", user.getId(),
                user.getPassword(), user.getEmail(), user.getPhoneNumber(), user.getName(), user.getAddress(),
                user.getNationality(), user.getStatus(), user.getUserType());
    }

    // Find user by ID in a specific file
    private User getUserById(String fileName, String userId) {
        List<User> users = loadUsersFromFile(fileName);
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    // Match users based on criteria for filtering
    private boolean matchesCriteria(User user, String criteria, String value) {
        if (value.isEmpty()) {
            return true; // Include all users if no value is provided
        }
        switch (criteria.toLowerCase()) {
            case "nationality":
                return user.getNationality().toLowerCase().contains(value.toLowerCase());
            case "id":
                return user.getId().toLowerCase().contains(value.toLowerCase());
            case "name":
                return user.getName().toLowerCase().contains(value.toLowerCase());
            case "email":
                return user.getEmail().toLowerCase().contains(value.toLowerCase());
            case "phone":
                return user.getPhoneNumber().toLowerCase().contains(value.toLowerCase());
            case "address":
                return user.getAddress().toLowerCase().contains(value.toLowerCase());
            default:
                return false;
        }
    }

    // Validate user fields during creation
    private void validateUserFields(User user) throws InvalidInputException {
        if (user.getId().isEmpty() || user.getPassword().isEmpty() || user.getEmail().isEmpty() ||
                user.getPhoneNumber().isEmpty() || user.getName().isEmpty() || user.getAddress().isEmpty() ||
                user.getNationality().isEmpty()) {
            throw new InvalidInputException("All fields must be filled.");
        }

        if (!user.getEmail().contains("@")) {
            throw new InvalidInputException("Email must contain '@'.");
        }

        try {
            Long.parseLong(user.getPhoneNumber());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Phone number must be numeric.");
        }
    }
}

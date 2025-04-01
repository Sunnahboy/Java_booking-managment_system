package main.java.com.hallbooking.part1_login;

import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;
import main.java.com.hallbooking.common.models.User;
import main.java.com.hallbooking.common.utils.FileHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class LoginManager {
    private static final String USERS_FILE = "users.txt";
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String SCHEDULERS_FILE = "schedulers.txt";

    private static String loggedInSchedulerId;
    private static String loggedInCustomerId;

    public User login(String userId, String password) throws InvalidCredentialsException, UserNotFoundException {
        System.out.println("Debug: Attempting login for userId: " + userId);

        // Check for super admin
        if (userId.equals("admin") && password.equals("123")) {
            System.out.println("Debug: Super admin login successful");
            return new SuperAdmin();
        }

        // Check regular users
        Optional<User> user = findUser(userId);
        if (user.isPresent()) {
            User foundUser = user.get();
            System.out.println("Debug: User found. ID: " + foundUser.getId() + ", Type: " + foundUser.getUserType() + ", Status: " + foundUser.getStatus());
            System.out.println("Debug: Stored password: " + foundUser.getPassword() + ", Provided password: " + password);

            if ("Blocked".equalsIgnoreCase(foundUser.getStatus())) {
                System.out.println("Debug: User is blocked");
                throw new InvalidCredentialsException("Your account is blocked. Please contact admin.");  // Change the message
            }

            if (password.equals(foundUser.getPassword())) {
                System.out.println("Debug: Password match successful");
                setLoggedInUserId(foundUser);
                return foundUser;
            } else {
                System.out.println("Debug: Password mismatch");
                throw new InvalidCredentialsException("Invalid username or password");  // Keep this message for invalid credentials
            }
        } else {
            System.out.println("Debug: User not found for ID: " + userId);
            throw new UserNotFoundException(userId);
        }
    }


    private void setLoggedInUserId(User user) {
        if ("Scheduler".equals(user.getUserType())) {
            loggedInSchedulerId = user.getId();
            System.out.println("Debug: Logged in Scheduler ID: " + loggedInSchedulerId);
        } else if ("Customer".equals(user.getUserType())) {
            loggedInCustomerId = user.getId();
            System.out.println("Debug: Logged in Customer ID: " + loggedInCustomerId);
        }
    }

    private Optional<User> findUser(String userId) {
        System.out.println("Debug: Searching for user with ID: " + userId);
        return Stream.of(
                        FileHandler.readFromFile(USERS_FILE, this::parseUser),
                        FileHandler.readFromFile(CUSTOMERS_FILE, this::parseCustomer),
                        FileHandler.readFromFile(SCHEDULERS_FILE, this::parseScheduler)
                )
                .flatMap(List::stream)
                .peek(u -> System.out.println("Debug: Checking user: " + u.getId()))
                .filter(u -> u.getId().equals(userId))
                .findFirst();
    }

    private User parseUser(String line) {
        String[] parts = line.split(",");
        if (parts.length < 9) throw new IllegalArgumentException("Invalid user data format.");
        System.out.println("Debug: Parsing user line: " + line);
        return new AdminOrManager(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
    }

    private User parseCustomer(String line) {
        String[] parts = line.split(",");
        if (parts.length < 9) throw new IllegalArgumentException("Invalid customer data format.");
        System.out.println("Debug: Parsing customer line: " + line);
        return new Customer(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
    }

    private User parseScheduler(String line) {
        String[] parts = line.split(",");
        if (parts.length < 9) throw new IllegalArgumentException("Invalid scheduler data format.");
        System.out.println("Debug: Parsing scheduler line: " + line);
        return new Scheduler(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
    }

    private static class AdminOrManager extends User {
        private final String status;
        private final String userType;

        public AdminOrManager(String id, String password, String email, String phone, String name, String address, String description, String status, String userType) {
            super(id, password, email, phone, name, address, description);
            this.status = status;
            this.userType = userType;
        }

        @Override
        public String getUserType() {
            return userType;
        }

        @Override
        public String getStatus() {
            return status;
        }
    }

    private static class Customer extends User {
        private final String status;

        public Customer(String id, String password, String email, String phone, String name, String address, String description, String status, String userType) {
            super(id, password, email, phone, name, address, description);
            this.status = status;
        }

        @Override
        public String getUserType() {
            return "Customer";
        }

        @Override
        public String getStatus() {
            return status;
        }
    }

    private static class Scheduler extends User {
        private final String status;

        public Scheduler(String id, String password, String email, String phone, String name, String address, String description, String status, String userType) {
            super(id, password, email, phone, name, address, description);
            this.status = status;
        }

        @Override
        public String getUserType() {
            return "Scheduler";
        }

        @Override
        public String getStatus() {
            return status;
        }
    }

    private static class SuperAdmin extends User {
        public SuperAdmin() {
            super("admin", "123", "admin@example.com", "1234567890", "Super Admin", "Admin Office", "N/A");
        }

        @Override
        public String getUserType() {
            return "SuperAdmin";
        }

        @Override
        public String getStatus() {
            return "Unblocked";
        }
    }

    public static String getLoggedInSchedulerId() {
        return loggedInSchedulerId;
    }

    public static String getLoggedInCustomerId() {
        return loggedInCustomerId;
    }
}
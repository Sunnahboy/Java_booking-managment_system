package main.java.com.hallbooking.common.models;

import java.util.UUID;

public abstract class User {
    private final String id;
    private String password;
    private String email;
    private String phoneNumber;
    private String name;
    private String address;
    private String nationality;
    private String status; // Changed to String to represent blocked/active status
    private String userType;

    protected User(String id, String password, String email, String phoneNumber,
                   String name, String address, String nationality) {
        this.id = (id != null) ? id : UUID.randomUUID().toString();
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.address = address;
        this.nationality = nationality;
        this.status = "Unblocked"; // Default status is active
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getNationality() { return nationality; }
    public String getStatus() { return status; }
    public String getPassword() { return password; }
    public String getUserType() { return userType; } // Added getter for userType

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public void setStatus(String status) { this.status = status; }
    public void setUserType(String userType) { this.userType = userType; } // Added setter for userType

    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", nationality='" + nationality + '\'' +
                ", status='" + status + '\'' +
                ", userType='" + userType + '\'' +  // Print userType directly
                '}';
    }
}






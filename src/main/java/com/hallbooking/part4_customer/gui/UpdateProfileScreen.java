package main.java.com.hallbooking.part4_customer.gui;

import main.java.com.hallbooking.common.models.User;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;
import main.java.com.hallbooking.part4_customer.CustomerManager;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class UpdateProfileScreen extends JFrame {
    private String customerId;
    private CustomerManager customerManager;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField nationalityField;
    private JPasswordField passwordField;
    private JFrame customerDashboard;  // Parent frame to reference the dashboard

    // Custom colors and styles
    private static final Color BACKGROUND_COLOR = new Color(230, 240, 250);  // Light blue background
    private static final Color HEADER_COLOR = new Color(70, 130, 180);       // Steel blue for header
    private static final Color BUTTON_COLOR = new Color(65, 105, 225);       // Royal blue for button
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private String currentStatus;  // To store the current status (e.g., Unblocked, Blocked)
    private String currentUserType;  // To store the current user type (e.g., Customer)

    public UpdateProfileScreen(String customerId, JFrame customerDashboard) {
        this.customerId = customerId;
        this.customerDashboard = customerDashboard;  // Store reference to the dashboard
        this.customerManager = new CustomerManager();
        initializeUI();
        loadCustomerProfile();  // Load the current user data, including status and user type

        // Hide the customer dashboard when this screen opens
        customerDashboard.setVisible(false);
    }

    private void initializeUI() {
        setTitle("Update Profile");
        setSize(450, 500); // Adjusted height for better spacing
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(BACKGROUND_COLOR);
        setContentPane(mainPanel);

        JLabel headerLabel = new JLabel("Update Profile", JLabel.CENTER);
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(HEADER_COLOR);
        headerLabel.setBounds(0, 20, 450, 30);
        mainPanel.add(headerLabel);

        addComponent("Name:", nameField = new JTextField(), 70, mainPanel);
        addComponent("Email:", emailField = new JTextField(), 120, mainPanel);
        addComponent("Phone:", phoneField = new JTextField(), 170, mainPanel);
        addComponent("Address:", addressField = new JTextField(), 220, mainPanel);
        addComponent("Nationality:", nationalityField = new JTextField(), 270, mainPanel);
        addComponent("Password:", passwordField = new JPasswordField(), 320, mainPanel);

        // Update button
        JButton updateButton = createStyledButton("Update Profile");
        updateButton.setBounds(125, 380, 200, 40); // Centered and larger
        updateButton.addActionListener(e -> updateProfile());
        mainPanel.add(updateButton);

        // Cancel button
        JButton cancelButton = createStyledButton("Cancel");
        cancelButton.setBounds(125, 430, 200, 40); // Centered and larger
        cancelButton.addActionListener(e -> {
            customerDashboard.setVisible(true); // Reopen the customer dashboard
            dispose(); // Close the UpdateProfileScreen
        });
        mainPanel.add(cancelButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_COLOR);  // Set text color to white
        button.setBackground(BUTTON_COLOR);  // Set background to blue
        button.setOpaque(true);  // Ensure button is opaque to show background color
        button.setBorderPainted(false);  // No border
        button.setFocusPainted(false);  // No focus border
        return button;
    }

    private void addComponent(String labelText, JTextField field, int y, JPanel panel) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(Color.BLACK);
        label.setBounds(50, y, 300, 20);
        panel.add(label);

        field.setFont(FIELD_FONT);
        field.setBounds(50, y + 20, 350, 25);  // Increased width for better spacing
        field.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(field);
    }

    // Load customer profile and store status and userType
    private void loadCustomerProfile() {
        try {
            User customer = customerManager.getCustomerById(customerId);
            nameField.setText(customer.getName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhoneNumber());
            addressField.setText(customer.getAddress());
            nationalityField.setText(customer.getNationality());
            passwordField.setText(customer.getPassword());  // Display the current password
            currentStatus = customer.getStatus();  // Save current status
            currentUserType = customer.getUserType();  // Save current userType
        } catch (UserNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading customer profile: " + e.getMessage(), "Profile Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void updateProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String nationality = nationalityField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Validation checks
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || nationality.isEmpty()) {
            showMessage("All fields are required", true);
            return;
        }

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address.", true);
            return;
        }

        if (!phone.startsWith("+60")) {
            showMessage("Phone number must start with +60 for Malaysia.", true);
            return;
        }

        if (!isValidPhoneNumber(phone)) {
            showMessage("Phone number must be 10 digits after the +60 prefix.", true);
            return;
        }

        try {
            // When creating the updated customer, include the existing status and userType
            User updatedCustomer = new User(customerId, password, email, phone, name, address, nationality) {
                @Override
                public String getUserType() {
                    return currentUserType;  // Use the saved userType
                }
            };
            updatedCustomer.setStatus(currentStatus);  // Set the saved status

            customerManager.updateCustomerProfile(updatedCustomer);  // Update customer with correct status and userType
            showMessage("Profile updated successfully", false);

            // Reopen the customer dashboard and close the UpdateProfileScreen
            customerDashboard.setVisible(true);
            dispose();
        } catch (UserNotFoundException e) {
            showMessage("Error updating profile: " + e.getMessage(), true);
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String digits = phoneNumber.substring(3).replaceAll("\\D", "");
        return digits.length() == 10;
    }

    private void showMessage(String message, boolean isError) {
        JOptionPane.showMessageDialog(this, message, isError ? "Error" : "Success",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UpdateProfileScreen updateScreen = new UpdateProfileScreen("C12345", new JFrame());
            updateScreen.setVisible(true);
        });
    }
}

package main.java.com.hallbooking.part4_customer.gui;
import main.java.com.hallbooking.part1_login.gui.LoginScreen;
import main.java.com.hallbooking.part2_admin.gui.ExtraAdminDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

public class CompleteRegistrationGUI extends JFrame {

    private JTextField idField, emailField, phoneField, nameField, addressField, nationalityField;
    private JPasswordField passwordField, confirmPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JPanel mainPanel;

    private static final Color BACKGROUND_COLOR = new Color(230, 240, 250);  // Light blue background
    private static final Color HEADER_COLOR = new Color(70, 130, 180);       // Steel blue for header
    private static final Color BUTTON_COLOR = new Color(65, 105, 225);       // Royal blue for button
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public CompleteRegistrationGUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        setTitle("User Registration");
        setSize(400, 700);  // Increased height to accommodate new button
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        mainPanel.setBackground(BACKGROUND_COLOR);
        setContentPane(mainPanel);

        JLabel headerLabel = new JLabel("Customer Registration", JLabel.CENTER);
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(HEADER_COLOR);
        headerLabel.setBounds(0, 20, 400, 30);
        mainPanel.add(headerLabel);

        addComponent("User ID:", idField = new JTextField(), 70);
        addComponent("Password:", passwordField = new JPasswordField(), 120);
        addComponent("Confirm Password:", confirmPasswordField = new JPasswordField(), 170);

        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setBounds(50, 220, 150, 20);
        showPasswordCheckBox.setBackground(BACKGROUND_COLOR);
        showPasswordCheckBox.addActionListener(e -> togglePasswordVisibility());
        mainPanel.add(showPasswordCheckBox);

        addComponent("Email:", emailField = new JTextField(), 250);
        addComponent("Phone Number:", phoneField = new JTextField(), 300);
        addComponent("Name:", nameField = new JTextField(), 350);
        addComponent("Address:", addressField = new JTextField(), 400);
        addComponent("Nationality:", nationalityField = new JTextField(), 450);

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(BUTTON_FONT);
        submitButton.setForeground(Color.BLUE);
        submitButton.setBackground(BUTTON_COLOR);
        submitButton.setBounds(150, 510, 100, 30);
        submitButton.setBorder(BorderFactory.createEmptyBorder());
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(e -> saveToFile());
        mainPanel.add(submitButton);

        // New "Back to Login" button
        JButton backToLoginButton = new JButton("Back to Login");
        backToLoginButton.setFont(BUTTON_FONT);
        backToLoginButton.setForeground(Color.BLUE);
        backToLoginButton.setBackground(new Color(100, 149, 237));  // Cornflower blue
        backToLoginButton.setBounds(125, 560, 150, 30);
        backToLoginButton.setBorder(BorderFactory.createEmptyBorder());
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.addActionListener(e -> backToLogin());
        mainPanel.add(backToLoginButton);
    }

    private void addComponent(String labelText, JTextField field, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);
        label.setBounds(50, y, 300, 20);
        mainPanel.add(label);

        field.setFont(FIELD_FONT);
        field.setBounds(50, y + 20, 300, 25);
        field.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        mainPanel.add(field);
    }

    private void togglePasswordVisibility() {
        if (showPasswordCheckBox.isSelected()) {
            passwordField.setEchoChar((char) 0);
            confirmPasswordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('•');
            confirmPasswordField.setEchoChar('•');
        }
    }

    private void saveToFile() {
        String id = idField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText();
        String phoneNumber = phoneField.getText();
        String name = nameField.getText();
        String address = addressField.getText();
        String nationality = nationalityField.getText();

        if (!isValidInput(id, email, phoneNumber, password, confirmPassword)) {
            return;
        }

        if (userIdExists(id) || emailExists(email) || phoneNumberExists(phoneNumber)) {
            return;
        }

        String userDetails = String.format("%s,%s,%s,%s,%s,%s,%s,Unblocked,Customer",
                id, password, email, phoneNumber, name, address, nationality);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/customers.txt", true))) {
            writer.write(userDetails);
            writer.newLine();
            showMessage("User Registered Successfully!", false);
            clearFields();
        } catch (IOException ex) {
            showMessage("Error saving user data: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private boolean isValidInput(String id, String email, String phoneNumber, String password, String confirmPassword) {
        if (!id.startsWith("C")) {
            showMessage("User ID must start with 'C'.", true);
            return false;
        }

        if (password.length() < 8) {
            showMessage("Password must be at least 8 characters long.", true);
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", true);
            return false;
        }

        if (!phoneNumber.startsWith("+60")) {
            showMessage("Phone number must start with +60 for Malaysia.", true);
            return false;
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            showMessage("Phone number must be 10 digits after the +60 prefix.", true);
            return false;
        }

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address.", true);
            return false;
        }

        return true;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String digits = phoneNumber.substring(3).replaceAll("\\D", "");
        return digits.length() == 10;
    }

    private boolean userIdExists(String id) {
        return checkExistingField(id, 0);
    }

    private boolean emailExists(String email) {
        return checkExistingField(email, 2);
    }

    private boolean phoneNumberExists(String phoneNumber) {
        return checkExistingField(phoneNumber, 3);
    }

    private boolean checkExistingField(String value, int index) {
        try (BufferedReader reader = new BufferedReader(new FileReader("data/customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > index && parts[index].equals(value)) {
                    String fieldName = index == 0 ? "User ID" : (index == 2 ? "Email" : "Phone number");
                    showMessage(fieldName + " already exists! Please enter a different " + fieldName.toLowerCase() + ".", true);
                    return true;
                }
            }
        } catch (IOException e) {
            showMessage("Error checking existing data: " + e.getMessage(), true);
            e.printStackTrace();
        }
        return false;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }

    private void showMessage(String message, boolean isError) {
        JOptionPane.showMessageDialog(this, message, isError ? "Error" : "Success",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearFields() {
        idField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        emailField.setText("");
        phoneField.setText("");
        nameField.setText("");
        addressField.setText("");
        nationalityField.setText("");
        showPasswordCheckBox.setSelected(false);
        togglePasswordVisibility();
    }

    private LogoutCallback logoutCallback;

    // Set the logout callback method, similar to AdminDashboard
    public void setLogoutCallback(LogoutCallback callback) {
        this.logoutCallback = callback;
    }

    @FunctionalInterface
    public interface LogoutCallback {
        void onLogout();
    }

    private void backToLogin() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to return to the login screen?",
                "Back to Login Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Hide the current registration window instead of disposing it
            this.setVisible(false);  // Just hide the window

            if (logoutCallback != null) {
                logoutCallback.onLogout();  // Show the login screen
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CompleteRegistrationGUI registrationGUI = new CompleteRegistrationGUI();

            // Set the logout callback to redirect to the login screen
            registrationGUI.setLogoutCallback(() -> {
                LoginScreen loginScreen = new LoginScreen();
                loginScreen.setVisible(true); // Show the login screen
            });

            registrationGUI.setVisible(true);
        });
    }
}
package main.java.com.hallbooking.part1_login.gui;

import main.java.com.hallbooking.common.exceptions.CustomExceptions;
import main.java.com.hallbooking.common.models.User;
import main.java.com.hallbooking.part1_login.LoginManager;
import main.java.com.hallbooking.part4_customer.gui.CompleteRegistrationGUI;
import main.java.com.hallbooking.part5_manager.gui.ManagerDashboardScreen;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginScreen extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton resetButton;
    private JButton registerButton;
    private JLabel errorMessageLabel;
    private JCheckBox showPasswordCheckBox;  // Add a JCheckBox for "Show Password"
    private LoginManager loginManager;
    private LoginCallback loginCallback;



    private void initComponents() {
        setTitle("Hall Booking System - Login");
        setMinimumSize(new Dimension(500, 400));
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(224, 224, 224));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JLabel headerLabel = new JLabel("Welcome to Hall Booking System");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(new Color(51, 51, 51));
        headerPanel.add(headerLabel);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.white);
        loginPanel.setBorder(new LineBorder(new Color(204, 204, 204), 2));

        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.weightx = 1.0;
        centerGbc.weighty = 1.0;
        centerGbc.fill = GridBagConstraints.BOTH;
        centerGbc.insets = new Insets(20, 20, 20, 20);
        centerPanel.add(loginPanel, centerGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userIdLabel = new JLabel("User ID:");
        userIdLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        userIdLabel.setForeground(new Color(51, 51, 51));
        loginPanel.add(userIdLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        userIdField = new JTextField(15);
        userIdField.setFont(new Font("Arial", Font.PLAIN, 20));
        userIdField.setBorder(new LineBorder(new Color(153, 153, 153), 2));
        loginPanel.add(userIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordLabel.setForeground(new Color(51, 51, 51));
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setBorder(new LineBorder(new Color(153, 153, 153), 2));
        loginPanel.add(passwordField, gbc);

        // Add "Show Password" checkbox
        gbc.gridx = 1;
        gbc.gridy = 2;
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setFont(new Font("Arial", Font.PLAIN, 16));
        showPasswordCheckBox.setOpaque(false); // Transparent background
        loginPanel.add(showPasswordCheckBox, gbc);

        // Add ActionListener to toggle password visibility
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);  // Show password as plain text
            } else {
                passwordField.setEchoChar('\u2022');  // Default bullet for hidden password
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        errorMessageLabel = new JLabel("");
        errorMessageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        errorMessageLabel.setForeground(Color.RED);
        loginPanel.add(errorMessageLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(150, 50));
        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setBorder(null);
        buttonPanel.add(loginButton);

        resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(150, 50));
        resetButton.setBackground(new Color(244, 67, 54));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Arial", Font.BOLD, 20));
        resetButton.setBorder(null);
        buttonPanel.add(resetButton);

        registerButton = new JButton("Customer Registration");
        registerButton.setPreferredSize(new Dimension(150, 50));
        registerButton.setBackground(new Color(33, 150, 243));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 20));
        registerButton.setBorder(null);
        buttonPanel.add(registerButton);

        loginButton.addActionListener(e -> performLogin());
        resetButton.addActionListener(e -> resetFields());
        registerButton.addActionListener(e -> openRegistrationScreen());

        pack();
    }

    private void performLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        errorMessageLabel.setText("");

        if (userId.isEmpty() || password.isEmpty()) {
            showError("Please enter both User ID and Password.");
            return;
        }

        try {
            User user = loginManager.login(userId, password);
            if (loginCallback != null) {
                loginCallback.onLoginSuccess(user);
            }
            JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.setVisible(false); // Hide the login screen

        } catch (CustomExceptions.UserNotFoundException ex) {
            showError("User not found. Please check your User ID.");
        } catch (CustomExceptions.InvalidCredentialsException ex) {
            if (ex.getMessage().equals("Your account is blocked. Please contact admin.")) {
                showError("Your account is blocked. Please contact admin.");
            } else {
                showError("Invalid credentials. Please try again.");
            }
        }
    }

    private void resetFields() {
        userIdField.setText("");
        passwordField.setText("");
        errorMessageLabel.setText("");
        userIdField.requestFocus();
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
    }

    public LoginScreen() {
        loginManager = new LoginManager();
        initComponents();
    }


    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }

    private void openRegistrationScreen() {
        // Close the login window by hiding it
        this.setVisible(false);

        SwingUtilities.invokeLater(() -> {
            CompleteRegistrationGUI registrationGUI = new CompleteRegistrationGUI();

            // Set the LogoutCallback to reopen the login screen when "Back to Login" is clicked
            registrationGUI.setLogoutCallback(() -> {
                registrationGUI.setVisible(false); // Hide registration window
                this.setVisible(true); // Reopen the login screen
            });

            registrationGUI.setVisible(true); // Show the registration screen
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginScreen().setVisible(true);
        });
    }

    public interface LoginCallback {
        void onLoginSuccess(User user);
        void onLoginFailure(String errorMessage);
    }
}

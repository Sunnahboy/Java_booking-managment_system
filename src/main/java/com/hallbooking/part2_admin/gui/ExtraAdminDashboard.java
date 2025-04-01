package main.java.com.hallbooking.part2_admin.gui;

import main.java.com.hallbooking.part1_login.gui.LoginScreen;
import main.java.com.hallbooking.common.models.User;
import main.java.com.hallbooking.part2_admin.ExtraAdminManager;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;

public class ExtraAdminDashboard extends JFrame {
    private ExtraAdminManager extraAdminManager;
    private JTextField userIdField, passwordField, nameField, emailField, phoneField, addressField, nationalityField;
    private JComboBox<String> userTypeDropdown;
    private JButton addUserButton, cancelButton, logoutButton;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTabbedPane tabbedPane;
    private String selectedFileType;

    private final Color BACKGROUND_COLOR = new Color(230, 240, 250); // Light blue background
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color TEXT_COLOR = Color.WHITE;

    public ExtraAdminDashboard() {
        extraAdminManager = new ExtraAdminManager();
        initComponents();
    }

    private void initComponents() {
        setTitle("Admin Management System");
        setSize(900, 650);  // Adjusted size to fit all fields
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.setPreferredSize(new Dimension(900, 50));
        JLabel titleLabel = new JLabel("Admin Management System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Add User", createAddUserPanel());
        tabbedPane.addTab("Update User", createUpdateUserPanel());

        add(titlePanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        gbc.weightx = 0.0;
    }

    private JPanel createAddUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // User Type Dropdown and ID prefix auto-fill
        String[] userTypes = {"Manager", "Customer", "Scheduler"};
        userTypeDropdown = new JComboBox<>(userTypes);
        userTypeDropdown.addActionListener(e -> autoFillIdPrefix());

        // Add form fields
        addFormField(panel, gbc, "User Type:", userTypeDropdown, 0, 0);
        addFormField(panel, gbc, "User ID:", userIdField = createStyledTextField(), 0, 1);
        addFormField(panel, gbc, "Password:", passwordField = createStyledTextField(), 0, 2);
        addFormField(panel, gbc, "Name:", nameField = createStyledTextField(), 0, 3);
        addFormField(panel, gbc, "Email:", emailField = createStyledTextField(), 0, 4);
        addFormField(panel, gbc, "Phone:", phoneField = createStyledTextField(), 2, 0);
        addFormField(panel, gbc, "Address:", addressField = createStyledTextField(), 2, 1);
        addFormField(panel, gbc, "Nationality:", nationalityField = createStyledTextField(), 2, 2);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        addUserButton = createStyledButton("Add User", new Color(0, 128, 0)); // Green
        cancelButton = createStyledButton("Cancel", Color.RED);
        logoutButton = createStyledButton("Logout", Color.ORANGE);

        buttonPanel.add(addUserButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(logoutButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);

        // Add action listeners for buttons
        addUserButton.addActionListener(e -> addUser());
        cancelButton.addActionListener(e -> clearFields());
        logoutButton.addActionListener(e -> logout());

        return panel;
    }

    private void toggleUserStatus(boolean block) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No user selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);  // Get the selected user's ID

        try {
            if (block) {
                extraAdminManager.blockUser(userId);  // Call to block user
                JOptionPane.showMessageDialog(this, "User blocked successfully!");
            } else {
                extraAdminManager.unblockUser(userId);  // Call to unblock user
                JOptionPane.showMessageDialog(this, "User unblocked successfully!");
            }
            loadUsersIntoTable(userTypeDropdown.getSelectedItem().toString());  // Refresh the table
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to change user status", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createUpdateUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);  // Adding more space around components

        // Buttons to choose file type
        JButton customerButton = createStyledButton("Customers", new Color(52, 152, 219));
        JButton schedulerButton = createStyledButton("Schedulers", new Color(52, 152, 219));
        JButton managerButton = createStyledButton("Managers", new Color(52, 152, 219));

        // Adding buttons to panel with better spacing
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(customerButton, gbc);

        gbc.gridx = 1;
        panel.add(schedulerButton, gbc);

        gbc.gridx = 2;
        panel.add(managerButton, gbc);

        // Filter label and filter fields
        JLabel filterLabel = new JLabel("Filter By:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;  // Resetting grid width
        panel.add(filterLabel, gbc);

        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{"ID", "Name", "Email", "Phone", "Address", "Nationality"});
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        panel.add(filterDropdown, gbc);

        JTextField filterField = createStyledTextField();
        gbc.gridx = 2;
        gbc.gridwidth = 2;  // Spanning the filter field across 2 columns for better alignment
        panel.add(filterField, gbc);

        JButton filterButton = createStyledButton("Filter", new Color(41, 128, 185));
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        panel.add(filterButton, gbc);

        // Table to show users
        setupUserTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;  // Span the table across the full width
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(scrollPane, gbc);

        // Action buttons
        JButton editButton = createStyledButton("Edit", new Color(41, 128, 185));
        JButton blockButton = createStyledButton("Block", Color.RED);
        JButton unblockButton = createStyledButton("Unblock", new Color(39, 174, 96));
        JButton deleteButton = createStyledButton("Delete", new Color(192, 57, 43));

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionsPanel.add(editButton);
        actionsPanel.add(blockButton);
        actionsPanel.add(unblockButton);
        actionsPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 5;  // Span the action buttons across the full width
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(actionsPanel, gbc);

        // Action listeners
        // Example listener for filter button
        filterButton.addActionListener(e -> {
            String selectedCriteria = (String) filterDropdown.getSelectedItem();  // Criteria like ID, Name, etc.
            String filterValue = filterField.getText();  // Value entered in the text field

            if (selectedFileType == null || selectedCriteria == null || filterValue.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a user type, a filter criteria, and provide a filter value.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                filterUsers(selectedCriteria, filterValue);  // Call to the filterUsers method
            }
        });


        customerButton.addActionListener(e -> {
            selectedFileType = "Customer";
            loadUsersIntoTable(selectedFileType);
        });

        schedulerButton.addActionListener(e -> {
            selectedFileType = "Scheduler";
            loadUsersIntoTable(selectedFileType);
        });


        managerButton.addActionListener(e -> {
            selectedFileType = "Manager";
            loadUsersIntoTable(selectedFileType);
        });


        editButton.addActionListener(e -> editUser());
        blockButton.addActionListener(e -> toggleUserStatus(true));
        unblockButton.addActionListener(e -> toggleUserStatus(false));
        deleteButton.addActionListener(e -> deleteUser());

        return panel;
    }


    private void setupUserTable() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Address", "Nationality", "Status"}, 0);
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void autoFillIdPrefix() {
        String selectedUserType = (String) userTypeDropdown.getSelectedItem();
        if (selectedUserType.equalsIgnoreCase("Manager")) {
            userIdField.setText("M");
        } else if (selectedUserType.equalsIgnoreCase("Customer")) {
            userIdField.setText("C");
        } else if (selectedUserType.equalsIgnoreCase("Scheduler")) {
            userIdField.setText("S");
        }
    }

    private void addUser() {
        try {
            String id = userIdField.getText();
            String password = passwordField.getText();
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            String nationality = nationalityField.getText();
            String userType = (String) userTypeDropdown.getSelectedItem();

            if (!(id.startsWith("M") || id.startsWith("C") || id.startsWith("S"))) {
                JOptionPane.showMessageDialog(this, "User ID must start with 'M', 'C', or 'S'", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newUser = new User(id, password, email, phone, name, address, nationality) {
                @Override
                public String getUserType() {
                    return userType;
                }
            };
            extraAdminManager.createUser(newUser);
            JOptionPane.showMessageDialog(this, "User added successfully!");
            clearFields();
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterUsers(String criteria, String value) {
        try {
            // Use the currently selected file type, which is determined by the button clicked (not a dropdown)
            String selectedUserType = selectedFileType;  // `selectedFileType` is updated by button clicks

            List<User> filteredUsers;

            // Check which type of file (user type) is selected by the button
            switch (selectedUserType) {
                case "Customer":
                    filteredUsers = extraAdminManager.filterUsers("customers.txt", criteria, value);
                    break;
                case "Scheduler":
                    filteredUsers = extraAdminManager.filterUsers("schedulers.txt", criteria, value);
                    break;
                case "Manager":
                    filteredUsers = extraAdminManager.filterUsers("users.txt", criteria, value).stream()
                            .filter(user -> user.getId().startsWith("M") && !user.getUserType().equalsIgnoreCase("Admin"))
                            .collect(Collectors.toList());
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Unknown user type selected.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            tableModel.setRowCount(0);  // Clear the table

            // Add filtered users to the table
            for (User user : filteredUsers) {
                tableModel.addRow(new Object[]{user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getAddress(), user.getNationality(), user.getStatus()});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to filter users", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUsersIntoTable(String userType) {
        try {
            List<User> users;
            switch (userType) {
                case "Customer":
                    users = extraAdminManager.loadUsersFromFile("customers.txt");
                    break;
                case "Scheduler":
                    users = extraAdminManager.loadUsersFromFile("schedulers.txt");
                    break;
                case "Manager":
                    users = extraAdminManager.loadUsersFromFile("users.txt").stream()
                            .filter(user -> user.getId().startsWith("M"))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown user type");
            }

            tableModel.setRowCount(0);  // Clear the table
            for (User user : users) {
                tableModel.addRow(new Object[]{user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getAddress(), user.getNationality(), user.getStatus()});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load users", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No user selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);  // Get the selected user's ID

        // Find the user by ID across all user files
        User user = extraAdminManager.getUserByIdAcrossFiles(userId);

        if (user != null) {
            JLabel idLabel = new JLabel(user.getId());  // ID is non-editable
            JTextField emailField = new JTextField(user.getEmail());
            JTextField phoneField = new JTextField(user.getPhoneNumber());
            JTextField nameField = new JTextField(user.getName());
            JTextField addressField = new JTextField(user.getAddress());
            JTextField nationalityField = new JTextField(user.getNationality());

            Object[] fields = {
                    "ID:", idLabel,  // ID is non-editable
                    "Email:", emailField,
                    "Phone:", phoneField,
                    "Name:", nameField,
                    "Address:", addressField,
                    "Nationality:", nationalityField,
            };

            int result = JOptionPane.showConfirmDialog(null, fields, "Edit User", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    // Update fields for the user object
                    user.setEmail(emailField.getText());
                    user.setPhoneNumber(phoneField.getText());
                    user.setName(nameField.getText());
                    user.setAddress(addressField.getText());
                    user.setNationality(nationalityField.getText());

                    // Update the user based on ID (not user type)
                    extraAdminManager.editUserById(user);

                    JOptionPane.showMessageDialog(this, "User updated successfully!");
                    loadUsersIntoTable(userTypeDropdown.getSelectedItem().toString());  // Reload table after updating
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Failed to update user", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No user selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            extraAdminManager.deleteUser(userId);
            JOptionPane.showMessageDialog(this, "User deleted successfully!");
            loadUsersIntoTable(userTypeDropdown.getSelectedItem().toString());  // Reload the table to reflect the changes
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to delete user", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        userIdField.setText("");
        passwordField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        nationalityField.setText("");
    }
    public interface LogoutCallback {
        void onLogout();
    }
    private LogoutCallback logoutCallback; // Callback field

    // Method to set the callback from the Main class
    public void setLogoutCallback(LogoutCallback callback) {
        this.logoutCallback = callback;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); // Close the dashboard window
            if (logoutCallback != null) { // Trigger the callback to handle logout
                logoutCallback.onLogout();
            }
        }
    }

    private String getFileName(String userType) {
        switch (userType) {
            case "Customer":
                return "customers.txt";
            case "Scheduler":
                return "schedulers.txt";
            case "Manager":
                return "users.txt";
            default:
                throw new IllegalArgumentException("Unknown user type");
        }
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExtraAdminDashboard dashboard = new ExtraAdminDashboard();
            dashboard.setVisible(true);
        });
    }
}


package main.java.com.hallbooking.part3_scheduler.gui;

import main.java.com.hallbooking.part3_scheduler.MaintenanceScheduler;
import main.java.com.hallbooking.part3_scheduler.HallManager;
import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.common.utils.DateTimeUtils;
import main.java.com.hallbooking.common.models.madeCalender;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MaintenanceScreen extends JFrame {
    private MaintenanceScheduler maintenanceScheduler;
    private HallManager hallManager;
    private JComboBox<String> hallIdComboBox;
    private madeCalender startDatePicker, endDatePicker;
    private JTextField remarkField;
    private JComboBox<String> startTimeComboBox, endTimeComboBox;
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private JButton scheduleButton, backButton;
    private JFrame parentFrame; // Reference to the parent frame

    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MaintenanceScreen(String schedulerId, JFrame parentFrame) {
        this.maintenanceScheduler = new MaintenanceScheduler();
        this.hallManager = new HallManager();
        this.parentFrame = parentFrame; // Store reference to the parent frame

        // Check for assigned issues
        if (checkAssignedIssues(schedulerId)) {
            initComponents(schedulerId);
            loadMaintenanceSchedule(schedulerId);

            // Hide the parent frame when this screen opens
            parentFrame.setVisible(false);
        } else {
            showNoIssuesMessage();
        }
    }

    private boolean checkAssignedIssues(String schedulerId) {
        try {
            List<String[]> assignedIssues = maintenanceScheduler.getAssignedIssues(schedulerId);
            return !assignedIssues.isEmpty();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error checking assigned issues: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showNoIssuesMessage() {
        JOptionPane.showMessageDialog(this,
                "No issues are currently assigned to you.",
                "No Issues",
                JOptionPane.INFORMATION_MESSAGE);
        goBack();
    }

    private void initComponents(String schedulerId) {
        setTitle("Maintenance Scheduling");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel inputPanel = createInputPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        scheduleButton.addActionListener(e -> scheduleMaintenance(schedulerId));
        backButton.addActionListener(e -> goBack());
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        List<Hall> halls = hallManager.getHalls();
        String[] hallIds = halls.stream().map(Hall::getId).toArray(String[]::new);
        hallIdComboBox = new JComboBox<>(hallIds);
        startDatePicker = new madeCalender();
        endDatePicker = new madeCalender();
        remarkField = createStyledTextField();

        String[] timeOptions = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        startTimeComboBox = new JComboBox<>(timeOptions);
        endTimeComboBox = new JComboBox<>(timeOptions);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(createStyledLabel("Hall ID:"), gbc);
        gbc.gridx = 1; panel.add(hallIdComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(createStyledLabel("Start Date:"), gbc);
        gbc.gridx = 1; panel.add(startDatePicker, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(createStyledLabel("Start Time:"), gbc);
        gbc.gridx = 1; panel.add(startTimeComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(createStyledLabel("End Date:"), gbc);
        gbc.gridx = 1; panel.add(endDatePicker, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(createStyledLabel("End Time:"), gbc);
        gbc.gridx = 1; panel.add(endTimeComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(createStyledLabel("Remark:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; panel.add(remarkField, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        String[] columnNames = {"Issue ID", "Hall ID", "Description", "Start Date/Time", "End Date/Time"};
        tableModel = new DefaultTableModel(columnNames, 0);
        maintenanceTable = new JTable(tableModel);
        styleTable(maintenanceTable);

        JScrollPane scrollPane = new JScrollPane(maintenanceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);

        scheduleButton = createStyledButton("Schedule Maintenance");
        backButton = createStyledButton("Back to Dashboard");

        panel.add(scheduleButton);
        panel.add(backButton);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(LABEL_COLOR);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(200, 40));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        return button;
    }

    private void styleTable(JTable table) {
        Color headerBackground = new Color(70, 130, 180);
        Color headerForeground = Color.WHITE;
        Color evenRowBackground = Color.WHITE;
        Color oddRowBackground = new Color(240, 248, 255);
        Color selectionBackground = new Color(135, 206, 250);
        Color selectionForeground = Color.BLACK;

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(headerBackground);
                label.setForeground(headerForeground);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    label.setBackground(selectionBackground);
                    label.setForeground(selectionForeground);
                } else {
                    label.setBackground(row % 2 == 0 ? evenRowBackground : oddRowBackground);
                    label.setForeground(Color.BLACK);
                }
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });

        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(selectionBackground);
        table.setSelectionForeground(selectionForeground);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setAutoCreateRowSorter(true);

        table.setColumnModel(table.getColumnModel());
    }

    private boolean loadMaintenanceSchedule(String schedulerId) {
        tableModel.setRowCount(0);
        try {
            List<String[]> assignedIssues = maintenanceScheduler.getAssignedIssues(schedulerId);
            if (assignedIssues.isEmpty()) {
                return false;
            }
            for (String[] issue : assignedIssues) {
                tableModel.addRow(new Object[]{
                        issue[0],
                        issue[3],
                        issue[4],
                        "Not scheduled",
                        "Not scheduled"
                });
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error loading assigned issues: " + e.getMessage());
            return false;
        }
    }

    private void scheduleMaintenance(String schedulerId) {
        try {
            String hallId = (String) hallIdComboBox.getSelectedItem();
            LocalDate startDate = startDatePicker.getSelectedDate();
            String startTimeStr = (String) startTimeComboBox.getSelectedItem();
            LocalDate endDate = endDatePicker.getSelectedDate();
            String endTimeStr = (String) endTimeComboBox.getSelectedItem();

            if (startDate == null || endDate == null) {
                showErrorMessage("Please select both start and end dates.");
                return;
            }

            LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.parse(startTimeStr));
            LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.parse(endTimeStr));

            // Check if start date/time is after end date/time
            if (startDateTime.isAfter(endDateTime)) {
                showErrorMessage("The start date/time cannot be after the end date/time. Please adjust your selection.");
                return;
            }

            int selectedRow = maintenanceTable.getSelectedRow();
            if (selectedRow == -1) {
                showErrorMessage("Please select an issue from the table before scheduling maintenance.");
                return;
            }
            String issueId = (String) tableModel.getValueAt(selectedRow, 0);

            maintenanceScheduler.scheduleMaintenance(schedulerId, issueId, hallId, startDateTime, endDateTime);

            tableModel.setValueAt(startDateTime.format(DATE_TIME_FORMATTER), selectedRow, 3);
            tableModel.setValueAt(endDateTime.format(DATE_TIME_FORMATTER), selectedRow, 4);
            showSuccessMessage("Maintenance has been successfully scheduled.");
        } catch (DateTimeParseException e) {
            showErrorMessage("The date or time you entered is not valid. Please check and try again.");
        } catch (IllegalArgumentException e) {
            showErrorMessage(getUserFriendlyMessage(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("There was a problem saving the maintenance schedule. Please try again or contact support if the issue persists.");
        } catch (Exception e) {
            showErrorMessage("An unexpected error occurred. Please try again or contact support if the issue persists.");
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Unable to Schedule Maintenance", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Maintenance Scheduled", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getUserFriendlyMessage(String errorMessage) {
        if (errorMessage.contains("Issue is not assigned to this scheduler")) {
            return "You don't have permission to schedule maintenance for this issue. Please check with your supervisor.";
        } else if (errorMessage.contains("not open")) {
            return "This issue is no longer available for scheduling maintenance. It may have been closed or is already in progress.";
        } else if (errorMessage.contains("conflicts with existing bookings")) {
            return "The selected time slot conflicts with an existing booking. Please choose a different time.";
        } else {
            return "There was a problem scheduling the maintenance. Please try again or contact support if the issue persists.";
        }
    }

    private void clearInputFields() {
        hallIdComboBox.setSelectedIndex(0);
        startDatePicker.setSelectedDate(null);
        endDatePicker.setSelectedDate(null);
        startTimeComboBox.setSelectedIndex(0);
        endTimeComboBox.setSelectedIndex(0);
        remarkField.setText("");
    }

    private void goBack() {
        this.dispose();
        parentFrame.setVisible(true); // Make the parent frame visible again
    }

    @Override
    public void dispose() {
        super.dispose();
        parentFrame.setVisible(true); // Ensure the parent frame is visible when this window is closed
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame parentFrame = new JFrame(); // Dummy parent frame for testing
            parentFrame.setVisible(false); // Hide the dummy parent frame
            new MaintenanceScreen("scheduler001", parentFrame).setVisible(true);
        });
    }
}

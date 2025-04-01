package main.java.com.hallbooking.part4_customer.gui;

import main.java.com.hallbooking.common.exceptions.CustomExceptions;
import main.java.com.hallbooking.common.models.madeCalender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.geom.RoundRectangle2D;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.*;
import java.util.Vector;
import java.util.List;


import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.HallNotFoundException;
import main.java.com.hallbooking.part4_customer.BookingManager;

public class BookingScreen extends JFrame {
    private String customerId;
    private BookingManager bookingManager;
    private JTable hallTable;  // Interactive table for hall selection
    private DefaultTableModel tableModel;
    private madeCalender startDatePicker;
    private madeCalender endDatePicker;
    private JComboBox<String> startTimeComboBox;
    private JComboBox<String> endTimeComboBox;
    private JLabel priceLabel;
    private JFrame customerDashboard;

    // Custom colors and styles
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    public BookingScreen(String customerId, JFrame customerDashboard) {
        this.customerId = customerId;
        this.customerDashboard = customerDashboard;
        this.bookingManager = new BookingManager();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Book a Hall");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Hide the customer dashboard when this screen opens
        customerDashboard.setVisible(false);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel tablePanel = createTablePanel();       // Hall selection table
        JPanel inputPanel = createInputPanel();       // Booking details input
        JPanel buttonPanel = createButtonPanel();     // Buttons panel

        mainPanel.add(tablePanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        String[] columnNames = {"Hall ID", "Type", "Capacity", "Rate", "Location"};
        tableModel = new DefaultTableModel(columnNames, 0);
        hallTable = new JTable(tableModel);
        styleTable(hallTable);
        loadHalls();

        hallTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                try {
                    updateHallDetails();
                } catch (HallNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(hallTable);
        scrollPane.setPreferredSize(new Dimension(500, 180));  // Reduce table size
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

private void styleTable(JTable table) {
        table.setRowHeight(25);  // Reduce row height for a compact look
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(BUTTON_HOVER_COLOR);
        table.setSelectionForeground(TEXT_COLOR);
        table.setFont(new Font("Arial", Font.PLAIN, 12));  // Slightly smaller font

        // Style the table header
        JTableHeader header = table.getTableHeader();
        header.setBackground(BUTTON_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 13));  // Reduce header font size
        header.setPreferredSize(new Dimension(header.getWidth(), 30));  // Reduce header height

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setBackground(BUTTON_COLOR);
                label.setForeground(TEXT_COLOR);
                label.setFont(new Font("Arial", Font.BOLD, 13));
                label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });
    }

    private void loadHalls() {
        tableModel.setRowCount(0);
        for (Hall hall : bookingManager.getAllAvailableHalls()) {
            tableModel.addRow(new Object[]{
                    hall.getId(),
                    hall.getType(),
                    hall.getCapacity(),
                    String.format("$%.2f", hall.getRate()),
                    hall.getLocation()
            });
        }
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createStyledLabel("Start Date:"), gbc);

        startDatePicker = new madeCalender();
        startDatePicker.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(startDatePicker, gbc);

        gbc.gridx = 2;
        panel.add(createStyledLabel("Start Time:"), gbc);

        startTimeComboBox = new JComboBox<>(getTimeOptions());
        startTimeComboBox.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 3;
        panel.add(startTimeComboBox, gbc);

        // End Date and Time
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(createStyledLabel("End Date:"), gbc);

        endDatePicker = new madeCalender();
        endDatePicker.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(endDatePicker, gbc);

        gbc.gridx = 2;
        panel.add(createStyledLabel("End Time:"), gbc);

        endTimeComboBox = new JComboBox<>(getTimeOptions());
        endTimeComboBox.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 3;
        panel.add(endTimeComboBox, gbc);

        // Price Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(createStyledLabel("Estimated Price:"), gbc);

        priceLabel = new JLabel("$0.00");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 1;
        panel.add(priceLabel, gbc);

        startDatePicker.addPropertyChangeListener("selectedDate", e -> updatePrice());
        endDatePicker.addPropertyChangeListener("selectedDate", e -> updatePrice());
        startTimeComboBox.addActionListener(e -> updatePrice());
        endTimeComboBox.addActionListener(e -> updatePrice());

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);

        JButton bookButton = createStyledButton("Book");
        bookButton.setPreferredSize(new Dimension(170, 50));
        bookButton.addActionListener(e -> bookHall());  // Trigger the booking process with conflict check
        panel.add(bookButton);

        JButton cancelButton = createStyledButton("Back to Dashboard");
        cancelButton.setPreferredSize(new Dimension(170, 50));
        cancelButton.addActionListener(e -> goBack());
        panel.add(cancelButton);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(LABEL_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(160, 50));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        return button;
    }

    private void bookHall() {
        // Get selected hall and booking details
        int selectedRow = hallTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a hall to book.", "Booking Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hallId = (String) hallTable.getValueAt(selectedRow, 0);
        LocalDateTime startDateTime = LocalDateTime.of(startDatePicker.getSelectedDate(),
                LocalTime.parse((String) startTimeComboBox.getSelectedItem()));
        LocalDateTime endDateTime = LocalDateTime.of(endDatePicker.getSelectedDate(),
                LocalTime.parse((String) endTimeComboBox.getSelectedItem()));

        if (startDateTime.isAfter(endDateTime)) {
            JOptionPane.showMessageDialog(this, "Invalid date/time selection. Start must be before end.", "Booking Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Step 1: Check for booking conflicts
        if (!bookingManager.isHallAvailable(hallId, startDateTime, endDateTime)) {
            JOptionPane.showMessageDialog(this, "The selected hall is not available for the selected time slot.",
                    "Booking Conflict", JOptionPane.ERROR_MESSAGE);
            return;  // Stop the booking process if there is a conflict
        }

        // Step 2: Proceed to payment
        try {
            double price = bookingManager.calculateBookingPrice(hallId, startDateTime, endDateTime);
            PaymentScreen paymentScreen = new PaymentScreen(customerId, hallId, startDateTime, endDateTime, price, this);
            paymentScreen.setVisible(true);
        } catch (HallNotFoundException e) {
            JOptionPane.showMessageDialog(this, "The selected hall could not be found. Please try another hall.",
                    "Hall Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePrice() {

        int selectedRow = hallTable.getSelectedRow();
        if (selectedRow != -1) {
            String hallId = (String) tableModel.getValueAt(selectedRow, 0);
            Hall selectedHall = null;
            try {
                selectedHall = bookingManager.getHallById(hallId);
            } catch (HallNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (selectedHall != null) {
                try {
                    LocalDateTime startDateTime = LocalDateTime.of(startDatePicker.getSelectedDate(),
                            LocalTime.parse((String) startTimeComboBox.getSelectedItem()));
                    LocalDateTime endDateTime = LocalDateTime.of(endDatePicker.getSelectedDate(),
                            LocalTime.parse((String) endTimeComboBox.getSelectedItem()));

                    if (startDateTime.isAfter(endDateTime)) {
                        priceLabel.setText("Invalid date/time selection");
                        return;
                    }

                    double price = bookingManager.calculateBookingPrice(
                            selectedHall.getId(), startDateTime, endDateTime);
                    priceLabel.setText(String.format("$%.2f", price));
                } catch (Exception e) {
                    priceLabel.setText("Error calculating price");
                }
            }
        } else {
            priceLabel.setText("$0.00");
        }
    }

    public void finalizeBooking(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Actually book the hall after payment confirmation
        try {
            bookingManager.bookHall(customerId, hallId, startDateTime, endDateTime);
            JOptionPane.showMessageDialog(this, "Booking successful!", "Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);
            goBack(); // Return to dashboard
        } catch (CustomExceptions.InvalidBookingException | HallNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + e.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Vector<String> getTimeOptions() {
        Vector<String> times = new Vector<>();
        for (int hour = 8; hour <= 18; hour++) {
            times.add(String.format("%02d:00", hour));
        }
        return times;
    }

    private void updateHallDetails() throws HallNotFoundException {
        int selectedRow = hallTable.getSelectedRow();
        if (selectedRow != -1) {
            String hallId = (String) tableModel.getValueAt(selectedRow, 0);
            Hall selectedHall = bookingManager.getHallById(hallId);
            if (selectedHall != null) {
                updatePrice();
            }
        }
    }

    private void goBack() {
        this.dispose();
        customerDashboard.setVisible(true);  // Reopen the customer dashboard
    }

    @Override
    public void dispose() {
        super.dispose();
        customerDashboard.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame customerDashboard = new JFrame();  // Dummy parent frame for testing
            customerDashboard.setVisible(false);  // Hide the dummy parent frame
            new BookingScreen("C12345", customerDashboard).setVisible(true);
        });
    }
}
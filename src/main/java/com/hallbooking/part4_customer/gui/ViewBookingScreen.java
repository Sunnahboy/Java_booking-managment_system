package main.java.com.hallbooking.part4_customer.gui;

import main.java.com.hallbooking.part4_customer.CustomerManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ViewBookingScreen extends JFrame {
    private String customerId;
    private CustomerManager customerManager;
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel resultLabel;
    private JFrame customerDashboard;  // Reference to the customer dashboard

    public ViewBookingScreen(String customerId, JFrame customerDashboard) {
        this.customerId = customerId;
        this.customerDashboard = customerDashboard;  // Store reference to the dashboard
        this.customerManager = new CustomerManager();
        initializeUI();
        loadAllBookings();

        // Hide the customer dashboard when this screen opens
        customerDashboard.setVisible(false);
    }

    private void initializeUI() {
        setTitle("View Bookings");
        setSize(1000, 800); // Adjusted to a larger size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(customerDashboard);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE); // Keep white background
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(8, 45, 118), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Create top panel with title and back button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("View Bookings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createBackButton();
        topPanel.add(backButton, BorderLayout.EAST);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);

        // Updated search button with custom styling
        JButton searchButton = createCustomStyledButton("Search");

        searchButton.addActionListener(e -> performSearch());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        resultLabel = new JLabel("Showing all bookings");
        searchPanel.add(resultLabel);

        contentPanel.add(searchPanel, BorderLayout.CENTER);

        // Create table
        String[] columnNames = {"Booking ID", "Hall ID", "Start Time", "End Time", "Price", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable = new JTable(tableModel);
        styleTable(bookingsTable);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        contentPanel.add(scrollPane, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    // Custom-styled back button
    private JButton createBackButton() {
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            customerDashboard.setVisible(true); // Reopen the customer dashboard
            dispose(); // Close the ViewBookingScreen
        });
        return backButton;
    }

    // Custom-styled search button
    private JButton createCustomStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(70, 130, 180));  // Steel Blue fill
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Rounded corners
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(100, 30));
        button.setForeground(Color.WHITE); // White text
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false); // No fill to allow custom paint
        return button;
    }

    // Styling the JTable
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

    // Load booking data
    private void loadAllBookings() {
        List<String> allBookings = customerManager.getCustomerBookings(customerId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();

        for (String booking : allBookings) {
            String[] bookingData = booking.split(",");
            if (bookingData.length >= 6) {
                String bookingId = bookingData[0];
                String hallId = bookingData[2];
                LocalDateTime startTime;
                LocalDateTime endTime;

                try {
                    // Parse start and end times using the defined formatter
                    startTime = LocalDateTime.parse(bookingData[3], formatter);
                    endTime = LocalDateTime.parse(bookingData[4], formatter);
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid date format in booking: " + booking);
                    continue; // Skip this booking if the date format is invalid
                }

                String price = bookingData[5];
                String status = startTime.isAfter(now) ? "Upcoming" : "Past";

                Object[] rowData = {
                        bookingId,
                        hallId,
                        startTime.format(formatter),
                        endTime.format(formatter),
                        price,
                        status
                };
                tableModel.addRow(rowData);
            }
        }
    }

    // Search functionality
    private void performSearch() {
        String searchText = searchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        bookingsTable.setRowSorter(sorter);

        if (searchText.length() == 0) {
            sorter.setRowFilter(null);
            resultLabel.setText("Showing all bookings");
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
            resultLabel.setText("Filtered results for: " + searchText);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        customerDashboard.setVisible(true); // Ensure the dashboard is visible when this window is closed
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewBookingScreen viewBookingScreen = new ViewBookingScreen("C12345", new JFrame());
            viewBookingScreen.setVisible(true);
        });
    }
}

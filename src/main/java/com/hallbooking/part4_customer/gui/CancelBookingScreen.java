package main.java.com.hallbooking.part4_customer.gui;

import java.awt.geom.RoundRectangle2D;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import main.java.com.hallbooking.part4_customer.CustomerManager;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.BookingNotFoundException;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.InvalidBookingException;

public class CancelBookingScreen extends JFrame {
    private String customerId;
    private CustomerManager customerManager;
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JButton cancelButton, backButton;
    private JFrame customerDashboard; // Reference to customer dashboard

    // Custom colors and styles
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    public CancelBookingScreen(String customerId, JFrame customerDashboard) {
        this.customerId = customerId;
        this.customerDashboard = customerDashboard; // Store reference to the dashboard
        this.customerManager = new CustomerManager();
        initializeUI();
        loadFutureBookings();
    }

    private void initializeUI() {
        setTitle("Cancel Booking");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Hide the customer dashboard when this screen opens
        customerDashboard.setVisible(false);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Input Panel for selecting booking ID
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        String[] columnNames = {"Booking ID", "Hall ID", "Start Time", "End Time", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0);
        bookingsTable = new JTable(tableModel);
        styleTable(bookingsTable);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);

        cancelButton = createStyledButton("Cancel Booking");
        cancelButton.addActionListener(e -> cancelSelectedBooking());
        panel.add(cancelButton);

        backButton = createStyledButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            customerDashboard.setVisible(true); // Reopen the customer dashboard
            dispose(); // Close the CancelBookingScreen
        });
        panel.add(backButton);

        return panel;
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
        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(BUTTON_HOVER_COLOR);
        table.setSelectionForeground(TEXT_COLOR);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        JTableHeader header = table.getTableHeader();
        header.setBackground(BUTTON_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(BUTTON_COLOR);
                label.setForeground(TEXT_COLOR);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 250));
                }
                return c;
            }
        });
    }

    private void loadFutureBookings() {
        // Fetch the future bookings for the customer
        List<String> futureBookings = customerManager.getFutureBookings(customerId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (String booking : futureBookings) {
            String[] bookingData = booking.split(",");
            if (bookingData.length >= 6) {
                String bookingId = bookingData[0];
                String hallId = bookingData[2];

                LocalDateTime startTime;
                LocalDateTime endTime;
                try {
                    startTime = LocalDateTime.parse(bookingData[3], formatter);
                    endTime = LocalDateTime.parse(bookingData[4], formatter);
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid date format in booking: " + booking);
                    continue;
                }

                String price = bookingData[5];

                if (startTime.isAfter(LocalDateTime.now().plusDays(3))) {
                    Object[] rowData = {
                            bookingId,
                            hallId,
                            startTime.format(formatter),
                            endTime.format(formatter),
                            price
                    };
                    tableModel.addRow(rowData);
                }
            }
        }
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this booking?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                customerManager.cancelBooking(bookingId);
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully", "Cancellation Confirmed", JOptionPane.INFORMATION_MESSAGE);
            } catch (BookingNotFoundException | InvalidBookingException e) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + e.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CancelBookingScreen cancelScreen = new CancelBookingScreen("C12345", new JFrame()); // Placeholder frame
            cancelScreen.setVisible(true);
        });
    }
}

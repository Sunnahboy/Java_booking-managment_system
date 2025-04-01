package main.java.com.hallbooking.part4_customer.gui;

import main.java.com.hallbooking.part4_customer.CustomerManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class IssueReportingScreen extends JFrame {
    private String customerId;
    private CustomerManager customerManager;
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JTextArea issueDescriptionArea;
    private JButton reportIssueButton, closeButton;
    private JFrame customerDashboard;  // Reference to the customer dashboard

    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    public IssueReportingScreen(String customerId, JFrame customerDashboard) {
        this.customerId = customerId;
        this.customerDashboard = customerDashboard;  // Store reference to the dashboard
        this.customerManager = new CustomerManager();
        initializeUI();
        loadPastBookings();

        // Hide the customer dashboard when this screen opens
        customerDashboard.setVisible(false);
    }

    private void initializeUI() {
        setTitle("Report an Issue");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel tablePanel = createTablePanel();
        JPanel issuePanel = createIssuePanel();
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(tablePanel, BorderLayout.NORTH);
        mainPanel.add(issuePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        reportIssueButton.addActionListener(e -> reportIssue());
        closeButton.addActionListener(e -> {
            customerDashboard.setVisible(true); // Reopen the customer dashboard
            dispose(); // Close the IssueReportingScreen
        });
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        String[] columnNames = {"Booking ID", "Hall ID", "Start Time", "End Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable = new JTable(tableModel);
        styleTable(bookingsTable);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createIssuePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel issueLabel = createStyledLabel("Describe the issue:");
        panel.add(issueLabel, BorderLayout.NORTH);

        issueDescriptionArea = new JTextArea(5, 30);
        issueDescriptionArea.setLineWrap(true);
        issueDescriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(issueDescriptionArea);
        panel.add(descriptionScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);

        reportIssueButton = createStyledButton("Report Issue");
        closeButton = createStyledButton("Close");

        panel.add(reportIssueButton);
        panel.add(closeButton);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(LABEL_COLOR);
        return label;
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

    private void loadPastBookings() {
        List<String> pastBookings = customerManager.getPastBookings(customerId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (String booking : pastBookings) {
            String[] bookingData = booking.split(",");
            if (bookingData.length >= 5) {
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

                Object[] rowData = {
                        bookingId,
                        hallId,
                        startTime.format(formatter),
                        endTime.format(formatter)
                };
                tableModel.addRow(rowData);
            }
        }
    }

    private void reportIssue() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to report an issue", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = (String) tableModel.getValueAt(selectedRow, 0);
        String hallId = (String) tableModel.getValueAt(selectedRow, 1);
        String description = issueDescriptionArea.getText().trim();

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a description of the issue", "Empty Description", JOptionPane.WARNING_MESSAGE);
            return;
        }

        customerManager.raiseIssue(customerId, bookingId, hallId, description);
        JOptionPane.showMessageDialog(this, "Issue reported successfully", "Issue Reported", JOptionPane.INFORMATION_MESSAGE);
        issueDescriptionArea.setText("");
    }

    @Override
    public void dispose() {
        super.dispose();
        customerDashboard.setVisible(true); // Ensure the dashboard is visible when this window is closed
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IssueReportingScreen issueScreen = new IssueReportingScreen("C12345", new JFrame());
            issueScreen.setVisible(true);
        });
    }
}

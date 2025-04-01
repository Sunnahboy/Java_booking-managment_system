package main.java.com.hallbooking.part4_customer.gui;

import main.java.com.hallbooking.common.utils.FileHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentScreen extends JFrame {
    private JTextField cardNumberField;
    private JTextField cardHolderField;
    private JComboBox<String> expiryMonthComboBox;
    private JComboBox<String> expiryYearComboBox;
    private JPasswordField cvvField;
    private JLabel errorMessageLabel;
    private JLabel priceLabel;

    private String customerId;
    private String hallId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private double price;
    private BookingScreen bookingScreen;

    // Custom colors and styles
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    public PaymentScreen(String customerId, String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime, double price, BookingScreen bookingScreen) {
        this.customerId = customerId;
        this.hallId = hallId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.price = price;
        this.bookingScreen = bookingScreen;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Payment");
        setSize(500, 400);
        setLocationRelativeTo(null);  // Center the PaymentScreen on the screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = createInputPanel();   // Payment input fields
        JPanel buttonPanel = createButtonPanel(); // Buttons panel

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Card Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createStyledLabel("Card Number:"), gbc);

        cardNumberField = new JTextField(16);
        gbc.gridx = 1;
        panel.add(cardNumberField, gbc);

        // Cardholder Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(createStyledLabel("Cardholder Name:"), gbc);

        cardHolderField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(cardHolderField, gbc);

        // Expiry Date (Month and Year)
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(createStyledLabel("Expiry Date:"), gbc);

        JPanel expiryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expiryPanel.setBackground(BACKGROUND_COLOR);

        expiryMonthComboBox = new JComboBox<>(getMonths());
        expiryMonthComboBox.setPreferredSize(new Dimension(70, 25)); // Wider dropdown
        expiryPanel.add(expiryMonthComboBox);

        expiryYearComboBox = new JComboBox<>(getYears());
        expiryYearComboBox.setPreferredSize(new Dimension(100, 25)); // Wider dropdown
        expiryPanel.add(expiryYearComboBox);

        gbc.gridx = 1;
        panel.add(expiryPanel, gbc);

        // CVV
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(createStyledLabel("CVV:"), gbc);

        cvvField = new JPasswordField(3);
        gbc.gridx = 1;
        panel.add(cvvField, gbc);

        // Error Message Label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        errorMessageLabel = new JLabel("");
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(errorMessageLabel, gbc);

        // Price Display
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(createStyledLabel("Total Price:"), gbc);

        priceLabel = new JLabel(String.format("$%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 1;
        panel.add(priceLabel, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);

        JButton payButton = createStyledButton("Pay");
        payButton.addActionListener(e -> processPayment());
        panel.add(payButton);

        JButton cancelButton = createStyledButton("Cancel");
        cancelButton.addActionListener(e -> goBack());
        panel.add(cancelButton);

        return panel;
    }

    private void processPayment() {
        String cardNumber = cardNumberField.getText().trim();
        String cardHolder = cardHolderField.getText().trim();
        String expiryMonth = (String) expiryMonthComboBox.getSelectedItem();
        String expiryYear = (String) expiryYearComboBox.getSelectedItem();
        String cvv = new String(cvvField.getPassword());

        if (cardNumber.isEmpty() || cardHolder.isEmpty() || cvv.isEmpty()) {
            showError("Please fill all the fields.");
            return;
        }

        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            showError("Invalid card number.");
            return;
        }

        if (cvv.length() != 3 || !cvv.matches("\\d+")) {
            showError("Invalid CVV.");
            return;
        }

        // Simulate payment processing
        JOptionPane.showMessageDialog(this, "Payment successful! Generating receipt...",
                "Payment Confirmation", JOptionPane.INFORMATION_MESSAGE);

        // Call finalizeBooking() from BookingScreen after successful payment
        bookingScreen.finalizeBooking(hallId, startDateTime, endDateTime);

        // Generate a receipt
        generateReceipt();

        this.dispose();
    }

    private void generateReceipt() {
        // Get the latest booking ID for the customer
        String bookingId = getLatestBookingIdForCustomer(customerId);

        if (bookingId == null) {
            JOptionPane.showMessageDialog(this, "Error: Could not retrieve booking details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate receipt content
        String receiptId = "R" + System.currentTimeMillis();  // Generate a unique receipt ID
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startDateTimeFormatted = startDateTime.format(formatter);
        String endDateTimeFormatted = endDateTime.format(formatter);

        // Generate receipt content to be displayed
        String receiptContent = String.format(
                "Receipt ID: %s\nBooking ID: %s\nCustomer ID: %s\nHall ID: %s\nStart Time: %s\nEnd Time: %s\nTotal Price: $%.2f",
                receiptId, bookingId, customerId, hallId, startDateTimeFormatted, endDateTimeFormatted, price);

        // Show the receipt in a dialog with improved GUI
        ReceiptDialog receiptDialog = new ReceiptDialog(this, receiptId, bookingId, customerId, hallId,
                startDateTimeFormatted, endDateTimeFormatted, price);
        receiptDialog.setVisible(true);  // Show the dialog and wait for user to close it

        // Write the receipt to a single line in the file
        String receiptLine = String.format("%s,%s,%s,%s,%s,%s,%.2f",
                receiptId, bookingId, customerId, hallId,
                startDateTimeFormatted, endDateTimeFormatted, price);
        FileHandler.appendToFile("receipts.txt", receiptLine);
    }

    private String getLatestBookingIdForCustomer(String customerId) {
        List<String> bookings = FileHandler.readFromFile("bookings.txt", line -> line);

        // Get the latest booking for this customer
        for (int i = bookings.size() - 1; i >= 0; i--) {
            String[] bookingDetails = bookings.get(i).split(",");
            if (bookingDetails.length >= 2 && bookingDetails[1].equals(customerId)) {
                return bookingDetails[0];  // Return the Booking ID (which is the first value)
            }
        }

        return null;  // Return null if no booking found for the customer
    }

    private void goBack() {
        this.dispose();
        bookingScreen.setVisible(true);  // Reopen the booking screen
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(150, 40));
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

    private String[] getMonths() {
        return new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    }

    private String[] getYears() {
        int currentYear = LocalDateTime.now().getYear();
        String[] years = new String[10];
        for (int i = 0; i < 10; i++) {
            years[i] = String.valueOf(currentYear + i);
        }
        return years;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame dummyBookingScreen = new JFrame();  // Dummy booking screen for testing
            dummyBookingScreen.setVisible(false);  // Hide the dummy screen
            new PaymentScreen("C12345", "H123", LocalDateTime.now(), LocalDateTime.now().plusHours(2), 299.99, new BookingScreen("C12345", dummyBookingScreen)).setVisible(true);
        });
    }
}

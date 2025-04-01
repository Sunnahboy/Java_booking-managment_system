package main.java.com.hallbooking.part4_customer.gui;

import javax.swing.*;
import java.awt.*;

public class ReceiptDialog extends JDialog {
    public ReceiptDialog(JFrame parent, String receiptId, String bookingId, String customerId,
                         String hallId, String startTime, String endTime, double totalPrice) {
        super(parent, "Payment Receipt", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);  // Center relative to the PaymentScreen

        JPanel receiptPanel = new JPanel();
        receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));
        receiptPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create styled labels for each receipt field
        JLabel titleLabel = new JLabel("Receipt");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel receiptIdLabel = createStyledLabel("Receipt ID: " + receiptId);
        JLabel bookingIdLabel = createStyledLabel("Booking ID: " + bookingId);
        JLabel customerIdLabel = createStyledLabel("Customer ID: " + customerId);
        JLabel hallIdLabel = createStyledLabel("Hall ID: " + hallId);
        JLabel startTimeLabel = createStyledLabel("Start Time: " + startTime);
        JLabel endTimeLabel = createStyledLabel("End Time: " + endTime);
        JLabel totalPriceLabel = createStyledLabel("Total Price: $" + String.format("%.2f", totalPrice));

        // Add components to the panel
        receiptPanel.add(titleLabel);
        receiptPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Add some spacing
        receiptPanel.add(receiptIdLabel);
        receiptPanel.add(bookingIdLabel);
        receiptPanel.add(customerIdLabel);
        receiptPanel.add(hallIdLabel);
        receiptPanel.add(startTimeLabel);
        receiptPanel.add(endTimeLabel);
        receiptPanel.add(totalPriceLabel);
        receiptPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dispose());  // Close dialog on button press
        receiptPanel.add(closeButton);

        add(receiptPanel);
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}

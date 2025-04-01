package main.java.com.hallbooking.part4_customer.gui;

import main.java.com.hallbooking.part1_login.gui.LoginScreen;
import main.java.com.hallbooking.part2_admin.gui.ExtraAdminDashboard;
import main.java.com.hallbooking.part4_customer.CustomerManager;

import main.java.com.hallbooking.part4_customer.gui.CustomerDashboard;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private String customerId;
    private List<JButton> buttons;
    private JPanel contentPanel;
    private Timer animationTimer;
    private int animationStep;

    public CustomerDashboard(String customerId) {
        this.customerId = customerId;
        this.buttons = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Customer Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 240, 240)); // Match background color

        JPanel titlePanel = createTitlePanel();
        contentPanel = createContentPanel();
        JPanel footerPanel = createFooterPanel();

        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(240, 240, 240));
        JLabel titleLabel = new JLabel("Customer Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        return titlePanel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        String[] buttonLabels = {"Update Profile", "Book a Hall", "Cancel Booking", "Report Issue", "View Bookings", "Logout"};

        for (String label : buttonLabels) {
            JButton button = createStyledButton(label);
            panel.add(button, gbc);
            buttons.add(button);
        }

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 240, 240));
        JLabel footerLabel = new JLabel("© 2024 Hall Booking System");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerPanel.add(footerLabel);
        return footerPanel;
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
        button.setPreferredSize(new Dimension(250, 60));
        button.setBackground(new Color(70, 130, 180)); // Match button color
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(100, 149, 237)); // Hover effect
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
                button.repaint();
            }
        });

        button.addActionListener(e -> animateButtonClick(button));
        return button;
    }

    private void animateButtonClick(JButton clickedButton) {
        animationStep = 0;
        animationTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                if (animationStep <= 10) {
                    float scale = 1 - (animationStep * 0.05f);
                    clickedButton.setFont(clickedButton.getFont().deriveFont(16f * scale));
                    clickedButton.setPreferredSize(new Dimension((int) (250 * scale), (int) (60 * scale)));
                } else if (animationStep > 10 && animationStep <= 20) {
                    float alpha = 1 - ((animationStep - 10) * 0.1f);
                    clickedButton.setForeground(new Color(1f, 1f, 1f, alpha));
                } else {
                    animationTimer.stop();
                    clickedButton.setFont(clickedButton.getFont().deriveFont(16f));
                    clickedButton.setPreferredSize(new Dimension(250, 60));
                    clickedButton.setForeground(Color.WHITE);
                    openCorrespondingScreen(clickedButton.getText());
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        animationTimer.start();
    }

    private void openCorrespondingScreen(String buttonText) {
        switch (buttonText) {
            case "Update Profile":
                new UpdateProfileScreen(customerId, this).setVisible(true);
                this.setVisible(false);
                break;
            case "Book a Hall":
                new BookingScreen(customerId, this).setVisible(true);
                this.setVisible(false);
                break;
            case "Cancel Booking":
                new CancelBookingScreen(customerId, this).setVisible(true);
                this.setVisible(false);
                break;
            case "Report Issue":
                new IssueReportingScreen(customerId, this).setVisible(true);
                this.setVisible(false);
                break;
            case "View Bookings":
                new ViewBookingScreen(customerId, this).setVisible(true);
                this.setVisible(false);
                break;
            case "Logout":
                logout();
                break;
        }
    }

    public interface LogoutCallback {
        void onLogout();
    }
    private ExtraAdminDashboard.LogoutCallback logoutCallback; // Callback field

    // Method to set the callback from the Main class
    public void setLogoutCallback(ExtraAdminDashboard.LogoutCallback callback) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CustomerDashboard dashboard = new CustomerDashboard("C12345");
            dashboard.setVisible(true);
        });
    }
}

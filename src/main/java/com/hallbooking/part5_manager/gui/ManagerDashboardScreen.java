package main.java.com.hallbooking.part5_manager.gui;

import main.java.com.hallbooking.common.models.Issue;
import main.java.com.hallbooking.common.utils.FileHandler;
import main.java.com.hallbooking.part2_admin.gui.ExtraAdminDashboard;
import main.java.com.hallbooking.part5_manager.IssueManager;
import main.java.com.hallbooking.part5_manager.ReportGenerator;
import main.java.com.hallbooking.part1_login.gui.LoginScreen;
import main.java.com.hallbooking.part5_manager.ManagerDashboard;
import main.java.com.hallbooking.part5_manager.gui.SalesAnalysisGUI; // Added SalesAnalysisGUI import

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardScreen extends JFrame {
    private static ManagerDashboardScreen instance;
    private IssueManager issueManager;
    private ReportGenerator reportGenerator;
    private JButton logoutButton;
    private List<JButton> buttons;
    private JPanel contentPanel;
    private Timer animationTimer;
    private int animationStep;

    public ManagerDashboardScreen() {
        this.issueManager = new IssueManager();
        this.reportGenerator = new ReportGenerator();
        this.buttons = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Manager Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 240, 240)); // Background color matching CustomerDashboard

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
        JLabel titleLabel = new JLabel("Manager Dashboard");
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

        String[] buttonLabels = {"Manage Issues", "Generate Report", "Sales Analysis", "Logout"};

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
        button.setBackground(new Color(70, 130, 180)); // Button color
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
            case "Manage Issues":
                closeIssuesAndOpenManagementScreen();
                break;
            case "Generate Report":
                openReportScreen();
                break;
            case "Sales Analysis": // Handle Sales Analysis button
                openSalesAnalysisScreen();
                break;
            case "Logout":
                logout();
                break;
        }
    }

    private void closeIssuesAndOpenManagementScreen() {
        try {
            issueManager.closeIssues(); // Call the closeIssues method
            openIssueManagementScreen(); // Then open the Issue Management Screen
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error closing issues: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openIssueManagementScreen() {
        SwingUtilities.invokeLater(() -> new IssueManagementScreen(issueManager, this).setVisible(true));
        this.setVisible(false);
    }

    private void openReportScreen() {
        SwingUtilities.invokeLater(() -> new ReportScreen(reportGenerator, this).setVisible(true));
        this.setVisible(false);
    }

    private void openSalesAnalysisScreen() {
        SwingUtilities.invokeLater(() -> new SalesAnalysisGUI().setVisible(true));
        this.setVisible(false);
    }

    // Logout logic, with callback
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
        SwingUtilities.invokeLater(() -> new ManagerDashboardScreen().setVisible(true));
    }
}


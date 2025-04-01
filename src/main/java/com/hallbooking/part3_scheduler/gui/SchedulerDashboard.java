package main.java.com.hallbooking.part3_scheduler.gui;

import main.java.com.hallbooking.part1_login.LoginManager;
import main.java.com.hallbooking.part2_admin.gui.ExtraAdminDashboard;
import main.java.com.hallbooking.part3_scheduler.HallManager;
import main.java.com.hallbooking.part3_scheduler.FilterHall;
import main.java.com.hallbooking.part3_scheduler.gui.HallAvailabilityScreen;
import main.java.com.hallbooking.part3_scheduler.gui.FilterHallGUI;
import main.java.com.hallbooking.part3_scheduler.gui.HallManagementScreen;
import main.java.com.hallbooking.part3_scheduler.gui.MaintenanceScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class SchedulerDashboard extends JFrame {

    private HallManager hallManager;
    private MaintenanceScreen maintenanceScreen;
    private JPanel contentPanel;
    private List<JButton> buttons;
    private Timer animationTimer;
    private int animationStep;
    private LoginManager loginManagerInstance; // Declare as an instance variable

    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;

    public SchedulerDashboard() {
        this.hallManager = new HallManager();
        this.buttons = new ArrayList<>();
        initComponents();
    }

    private void initComponents() {
        setTitle("Scheduler Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

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
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("Scheduler Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        return titlePanel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        String[] buttonLabels = {"Hall Management", "Schedule Maintenance", "Set Availability", "Filter Halls", "Logout"}; // Added Logout button here

        for (String label : buttonLabels) {
            JButton button = createStyledButton(label);
            panel.add(button, gbc);
            buttons.add(button);
        }

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(BACKGROUND_COLOR);
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
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_COLOR);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_COLOR);
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
                    clickedButton.setPreferredSize(new Dimension((int)(250 * scale), (int)(60 * scale)));
                } else if (animationStep > 10 && animationStep <= 20) {
                    float alpha = 1 - ((animationStep - 10) * 0.1f);
                    clickedButton.setForeground(new Color(1f, 1f, 1f, alpha));
                } else {
                    animationTimer.stop();
                    // Reset button appearance
                    clickedButton.setFont(clickedButton.getFont().deriveFont(16f));
                    clickedButton.setPreferredSize(new Dimension(250, 60));
                    clickedButton.setForeground(TEXT_COLOR);
                    openCorrespondingScreen(clickedButton.getText());
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        animationTimer.start();
    }

    private void openCorrespondingScreen(String buttonText) {
        SwingUtilities.invokeLater(() -> {
            switch (buttonText) {
                case "Hall Management":
                    new HallManagementScreen(hallManager, this).setVisible(true);
                    break;
                case "Schedule Maintenance":
                    String schedulerId = getCurrentSchedulerId();
                    new MaintenanceScreen(schedulerId, this).setVisible(true);
                    break;
                case "Set Availability":
                    new HallAvailabilityScreen(hallManager, this).setVisible(true);
                    break;
                case "Filter Halls":
                    FilterHallGUI filterHallGUI = new FilterHallGUI(this);
                    filterHallGUI.setVisible(true);
                    this.setVisible(false); // Hide the dashboard while FilterHallGUI is open
                    break;
                case "Logout":
                    logout(); // Call logout method when Logout is clicked
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Invalid option selected", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
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

    private String getCurrentSchedulerId() {
        String schedulerId = LoginManager.getLoggedInSchedulerId(); // Access the static variable

        if (schedulerId == null) {
            JOptionPane.showMessageDialog(this, "No scheduler ID found. Using default ID.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        System.out.println("Scheduler ID: " + schedulerId);

        return schedulerId;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new SchedulerDashboard().setVisible(true));
    }
}

package main.java.com.hallbooking.part5_manager.gui;

import main.java.com.hallbooking.part5_manager.ReportGenerator;
import main.java.com.hallbooking.common.models.madeCalender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;

public class ReportScreen extends JFrame {
    private ReportGenerator reportGenerator;
    private JComboBox<String> reportTypeComboBox;
    private madeCalender startDateCalendar;
    private JTextField endDateField;
    private JTextArea reportTextArea;
    private JButton generateButton;
    private JButton backButton;

    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;

    public ReportScreen(ReportGenerator reportGenerator, JFrame managerDashboard) {
        this.reportGenerator = reportGenerator;
        initComponents(managerDashboard);
    }

    private void initComponents(JFrame managerDashboard) {
        setTitle("Sales and Cancellations Report");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel controlPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(BACKGROUND_COLOR);

        reportTypeComboBox = new JComboBox<>(new String[]{"Weekly", "Monthly", "Yearly"});
        startDateCalendar = new madeCalender();
        endDateField = new JTextField();
        endDateField.setEditable(false);
        endDateField.setBackground(Color.LIGHT_GRAY);
        generateButton = createStyledButton("Generate Report");
        backButton = createStyledButton("Back to Dashboard");

        controlPanel.add(new JLabel("Report Type:"));
        controlPanel.add(reportTypeComboBox);
        controlPanel.add(new JLabel("Start Date:"));
        controlPanel.add(startDateCalendar);
        controlPanel.add(new JLabel("End Date:"));
        controlPanel.add(endDateField);
        controlPanel.add(new JLabel());
        controlPanel.add(generateButton);

        reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportTextArea);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);  // Add the back button at the bottom

        reportTypeComboBox.addActionListener(e -> updateEndDateField());
        generateButton.addActionListener(e -> generateReport());
        backButton.addActionListener(e -> goBack(managerDashboard)); // Back button event
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

    private void updateEndDateField() {
        LocalDate startDate = startDateCalendar.getSelectedDate();
        LocalDate endDate = null;

        switch (reportTypeComboBox.getSelectedItem().toString()) {
            case "Weekly":
                endDate = startDate.plusWeeks(1);
                break;
            case "Monthly":
                endDate = startDate.plusMonths(1);
                break;
            case "Yearly":
                endDate = startDate.plusYears(1);
                break;
        }

        if (endDate != null) {
            endDateField.setText(endDate.toString());
        }
    }

private void generateReport() {
    String reportType = reportTypeComboBox.getSelectedItem().toString();
    LocalDate startDate = startDateCalendar.getSelectedDate();
    LocalDate endDate = LocalDate.parse(endDateField.getText());

    if (endDate.isBefore(startDate)) {
        JOptionPane.showMessageDialog(this, "End date cannot be before the start date.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Use the custom report method, passing the start and end dates
    String report = reportGenerator.generateCustomReport(startDate.atStartOfDay(), endDate.atTime(23, 59));
    reportTextArea.setText(report);
}

    private void goBack(JFrame managerDashboard) {
        this.dispose();
        managerDashboard.setVisible(true);  // Return to the manager dashboard
    }
}

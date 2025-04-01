package main.java.com.hallbooking.part5_manager.gui;

import main.java.com.hallbooking.part5_manager.IssueManager;
import main.java.com.hallbooking.common.models.Issue;
import main.java.com.hallbooking.common.utils.FileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class IssueManagementScreen extends JFrame {
    private IssueManager issueManager;
    private JTable issueTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterComboBox;
    private JComboBox<String> schedulerIdComboBox;
    private JButton assignButton;
    private JButton backButton;

    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;

    public IssueManagementScreen(IssueManager issueManager, JFrame managerDashboard) {
        this.issueManager = issueManager;
        initComponents(managerDashboard);
        loadSchedulerIds();  // Load scheduler IDs on initialization
        loadIssues();
    }

    private void initComponents(JFrame managerDashboard) {
        setTitle("Issue Management");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10)); // Adjusted to align left
        filterPanel.setBackground(BACKGROUND_COLOR);

        statusFilterComboBox = new JComboBox<>(new String[]{"All", "Open", "Assigned", "In Progress", "Closed"});
        statusFilterComboBox.setPreferredSize(new Dimension(180, 30)); // Wider dropdown
        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(statusFilterComboBox);

        schedulerIdComboBox = new JComboBox<>(); // Added next to filter
        schedulerIdComboBox.setPreferredSize(new Dimension(180, 30)); // Wider dropdown
        filterPanel.add(new JLabel("Scheduler ID:"));
        filterPanel.add(schedulerIdComboBox);

        // Table
        String[] columnNames = {"ID", "Customer ID", "Booking ID", "Hall ID", "Description", "Status", "Assigned To"};
        tableModel = new DefaultTableModel(columnNames, 0);
        issueTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(issueTable);
        styleTable(issueTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Center align buttons
        buttonPanel.setBackground(BACKGROUND_COLOR);
        assignButton = createStyledButton("Assign to Scheduler");
        assignButton.setPreferredSize(new Dimension(200, 50)); // Wider button

        backButton = createStyledButton("Back to Dashboard");
        backButton.setPreferredSize(new Dimension(200, 50)); // Wider button

        buttonPanel.add(assignButton);
        buttonPanel.add(backButton);

        // Layout Configuration
        setLayout(new BorderLayout());
        add(filterPanel, BorderLayout.NORTH); // Add filter panel to the top
        add(scrollPane, BorderLayout.CENTER); // Table at the center
        add(buttonPanel, BorderLayout.SOUTH); // Button panel at the bottom

        // Event Listeners
        statusFilterComboBox.addActionListener(e -> loadIssues());
        assignButton.addActionListener(e -> assignIssue());
        backButton.addActionListener(e -> goBack(managerDashboard)); // Back button event
    }

    // New Method: Load Scheduler IDs
    private void loadSchedulerIds() {
        try {
            List<String> schedulerIds = FileHandler.readSchedulerIdsFromFile("schedulers.txt");
            for (String schedulerId : schedulerIds) {
                schedulerIdComboBox.addItem(schedulerId);
            }
            schedulerIdComboBox.setPreferredSize(new Dimension(180, 30));  // Ensure correct width
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load scheduler IDs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        button.setPreferredSize(new Dimension(180, 40)); // Increase width
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
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(BUTTON_HOVER_COLOR);
        table.setSelectionForeground(TEXT_COLOR);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        // Style the table header
        JTableHeader header = table.getTableHeader();
        header.setBackground(BUTTON_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 250));
                }
                return c;
            }
        });
    }

    private void loadIssues() {
        tableModel.setRowCount(0);
        List<Issue> issues;
        String status = (String) statusFilterComboBox.getSelectedItem();
        switch (status) {
            case "Open":
                issues = issueManager.getOpenIssues();
                break;
            case "Assigned":
                issues = issueManager.getAssignedIssues();
                break;
            case "In Progress":
                issues = issueManager.getInProgressIssues();
                break;
            case "Closed":
                issues = issueManager.getClosedIssues();
                break;
            default:
                issues = issueManager.getOpenIssues();
                issues.addAll(issueManager.getAssignedIssues());
                issues.addAll(issueManager.getInProgressIssues());
                issues.addAll(issueManager.getClosedIssues());
        }

        for (Issue issue : issues) {
            tableModel.addRow(new Object[]{
                    issue.getId(),
                    issue.getCustomerId(),
                    issue.getBookingId(),
                    issue.getHallId(),
                    issue.getDescription(),
                    issue.getStatus(),
                    issue.getAssignedSchedulerId() != null ? issue.getAssignedSchedulerId() : ""
            });
        }
    }

    private void assignIssue() {
        int selectedRow = issueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an issue to assign");
            return;
        }

        String issueId = (String) tableModel.getValueAt(selectedRow, 0);
        String schedulerId = (String) schedulerIdComboBox.getSelectedItem();

        try {
            issueManager.assignIssue(issueId, schedulerId);  // Assign without changing the status
            JOptionPane.showMessageDialog(this, "Issue assigned successfully");
            loadIssues();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Assignment failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack(JFrame managerDashboard) {
        this.dispose();
        managerDashboard.setVisible(true);  // Return to the manager dashboard
    }
}

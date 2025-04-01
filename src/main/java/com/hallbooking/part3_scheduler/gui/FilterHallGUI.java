package main.java.com.hallbooking.part3_scheduler.gui;

import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.part3_scheduler.FilterHall;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class FilterHallGUI extends JFrame {
    private FilterHall system;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCriteria;
    private JTextField filterValue;
    private JLabel resultLabel;
    private JFrame parentFrame;

    public FilterHallGUI(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        system = new FilterHall();
        system.loadHallsFromFile("data/hall.txt");

        setTitle("Filter Halls");
        setSize(1000, 800); // Adjusted to a larger size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parentFrame);

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

        JLabel titleLabel = new JLabel("Filter Halls");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = createBackButton();
        topPanel.add(backButton, BorderLayout.EAST);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        filterCriteria = new JComboBox<>(new String[]{"All", "ID", "Type", "Capacity", "Rate", "Location"});
        filterValue = new JTextField(20);

        // Updated filter button with custom styling
        JButton filterButton = createCustomStyledButton("Filter");

        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String criteria = (String) filterCriteria.getSelectedItem();
                String value = filterValue.getText();
                List<Hall> filteredHalls = system.filterHalls(criteria, value);
                updateTable(filteredHalls);
                updateResultLabel(criteria, value, filteredHalls.size());
            }
        });

        searchPanel.add(filterCriteria);
        searchPanel.add(filterValue);
        searchPanel.add(filterButton);
        resultLabel = new JLabel("Showing all halls");
        searchPanel.add(resultLabel);

        contentPanel.add(searchPanel, BorderLayout.CENTER);

        // Create table
        String[] columnNames = {"ID", "Type", "Capacity", "Rate", "Location"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        updateTable(system.getAllHalls());
    }

    // Custom-styled back button
    private JButton createBackButton() {
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true); // Reopen the parent frame (Scheduler Dashboard)
            dispose(); // Close the FilterHallGUI
        });
        return backButton;
    }

    // Custom-styled filter button
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

    private void updateResultLabel(String criteria, String value, int resultCount) {
        String message;
        if (criteria.equals("All")) {
            message = "Showing all halls";
        } else if (resultCount == 0) {
            message = "No matching results found";
        } else {
            message = "Found " + resultCount + " matching or similar results";
        }
        resultLabel.setText(message);
    }

    private void updateTable(List<Hall> halls) {
        tableModel.setRowCount(0); // Clear previous rows
        for (Hall hall : halls) {
            tableModel.addRow(new Object[]{
                    hall.getId(),
                    hall.getType(),
                    hall.getCapacity(),
                    hall.getRate(),
                    hall.getLocation()
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        parentFrame.setVisible(true); // Ensure the dashboard is visible when this window is closed
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame parentFrame = new JFrame(); // Dummy parent frame for testing purposes
            parentFrame.setVisible(false); // Hide the parent frame
            new FilterHallGUI(parentFrame).setVisible(true);
        });
    }
}

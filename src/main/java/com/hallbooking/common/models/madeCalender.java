package main.java.com.hallbooking.common.models;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;

public class madeCalender extends JPanel {
    private JTextField dateField;
    private JButton calendarButton;
    private JPopupMenu popupMenu;
    private JPanel calendarPanel;
    private LocalDate selectedDate;

    // Custom colors
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.blue;
    private static final Color NAV_BUTTON_COLOR = new Color(30, 144, 255);
    private static final Color GRID_COLOR = new Color(220, 220, 220); // Grid color

    public madeCalender() {
        setLayout(new BorderLayout());
        initializeComponents();
        configureCalendarButton();
        selectedDate = LocalDate.now();
        updateDateField();
    }

    private void initializeComponents() {
        dateField = createStyledTextField();
        calendarButton = new JButton("📅");
        calendarButton.setPreferredSize(new Dimension(30, 30));
        calendarButton.setBackground(BUTTON_COLOR);
        calendarButton.setForeground(TEXT_COLOR);
        calendarButton.setBorderPainted(false);
        calendarButton.setFocusPainted(false);

        add(dateField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(100, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private void configureCalendarButton() {
        calendarButton.addActionListener(e -> showCalendarPopup());
    }

    private void showCalendarPopup() {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            calendarPanel = new JPanel(new BorderLayout());
            popupMenu.add(calendarPanel);
        }
        updateCalendarPanel();
        popupMenu.show(calendarButton, 0, calendarButton.getHeight());
    }

    private void updateCalendarPanel() {
        calendarPanel.removeAll();
        YearMonth yearMonth = YearMonth.from(selectedDate);

        JPanel navigationPanel = createNavigationPanel(yearMonth);
        JPanel daysPanel = createDaysPanel(yearMonth);

        calendarPanel.add(navigationPanel, BorderLayout.NORTH);
        calendarPanel.add(daysPanel, BorderLayout.CENTER);
        calendarPanel.revalidate();
        calendarPanel.repaint();
        popupMenu.revalidate();
        popupMenu.repaint();
    }

    private JPanel createNavigationPanel(YearMonth yearMonth) {
        JPanel navigationPanel = new JPanel(new FlowLayout());
        JButton prevButton = createNavigationButton("<", e -> {
            selectedDate = selectedDate.minusMonths(1);
            updateCalendarPanel();
        });
        JButton nextButton = createNavigationButton(">", e -> {
            selectedDate = selectedDate.plusMonths(1);
            updateCalendarPanel();
        });

        JLabel monthLabel = new JLabel(yearMonth.getMonth() + " " + yearMonth.getYear(), JLabel.CENTER);
        monthLabel.setForeground(TEXT_COLOR);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 16));
        navigationPanel.add(prevButton);
        navigationPanel.add(monthLabel);
        navigationPanel.add(nextButton);

        return navigationPanel;
    }

    private JButton createNavigationButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setBackground(NAV_BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.addActionListener(actionListener);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createDaysPanel(YearMonth yearMonth) {
        JPanel daysPanel = new JPanel(new GridLayout(0, 7));
        String[] dayNames = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setForeground(BUTTON_COLOR);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            daysPanel.add(dayLabel);
        }

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < dayOfWeek; i++) {
            daysPanel.add(new JLabel("")); // Empty labels for alignment
        }

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            daysPanel.add(createDayButton(yearMonth, day));
        }

        // Set grid lines
        // Set grid lines
        for (Component comp : daysPanel.getComponents()) {
            if (comp instanceof JButton) {
                ((JComponent) comp).setBorder(BorderFactory.createLineBorder(GRID_COLOR));
            }
        }

        return daysPanel;
    }

    private JButton createDayButton(YearMonth yearMonth, int day) {
        JButton dayButton = new JButton(String.valueOf(day));
        dayButton.setPreferredSize(new Dimension(40, 40));
        LocalDate fullDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day);
        dayButton.setBackground(fullDate.equals(selectedDate) ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
        dayButton.setForeground(TEXT_COLOR);
        dayButton.setBorderPainted(false);
        dayButton.setFocusPainted(false);

        dayButton.addActionListener(e -> {
            selectedDate = fullDate;
            updateDateField();
            popupMenu.setVisible(false);
        });

        // Mouse listener for hover effect
        dayButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dayButton.setBackground(BUTTON_HOVER_COLOR);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dayButton.setBackground(fullDate.equals(selectedDate) ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            }
        });

        return dayButton;
    }

    private void updateDateField() {
        dateField.setText(selectedDate.toString());
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate = date;
        updateDateField();
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Date Picker Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new FlowLayout());
            frame.setSize(300, 100);

            madeCalender datePicker = new madeCalender();
            frame.add(datePicker);

            frame.setVisible(true);
        });
    }
}

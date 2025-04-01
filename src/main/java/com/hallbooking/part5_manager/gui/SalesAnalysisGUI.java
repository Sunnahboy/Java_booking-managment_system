package main.java.com.hallbooking.part5_manager.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class SalesAnalysisGUI extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JComboBox<String> periodDropdown;
    private JPanel chartsPanel;
    private JPanel salesChartPanel;
    private JPanel comparisonChartPanel;
    private JPanel distributionChartPanel;
    private List<SalesEntry> confirmedSales;
    private List<SalesEntry> canceledSales;

    public SalesAnalysisGUI() {
        setTitle("Sales Analysis Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        chartsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(chartsPanel, BorderLayout.CENTER);

        initializeChartPanels();
        updateCharts();

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel periodLabel = new JLabel("Select Analysis Period:");
        periodDropdown = new JComboBox<>(new String[]{"Weekly", "Monthly", "Yearly"});
        periodDropdown.addActionListener(e -> updateCharts());

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> updateCharts());

        topPanel.add(periodLabel);
        topPanel.add(periodDropdown);
        topPanel.add(refreshButton);

        return topPanel;
    }

    private void initializeChartPanels() {
        salesChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSalesChart(g);
            }
        };
        comparisonChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawComparisonChart(g);
            }
        };
        distributionChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDistributionChart(g);
            }
        };

        chartsPanel.add(salesChartPanel);
        chartsPanel.add(comparisonChartPanel);
        chartsPanel.add(distributionChartPanel);
    }

    private void updateCharts() {
        String selectedPeriod = (String) periodDropdown.getSelectedItem();
        System.out.println("Updating charts for period: " + selectedPeriod);

        try {
            confirmedSales = readSalesData("data/bookings.txt");
            canceledSales = readSalesData("data/history.txt");

            System.out.println("Confirmed sales: " + confirmedSales.size());
            System.out.println("Canceled sales: " + canceledSales.size());

            chartsPanel.revalidate();
            chartsPanel.repaint();

            printSummaryStatistics(confirmedSales, canceledSales);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading sales data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void drawSalesChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = salesChartPanel.getWidth();
        int height = salesChartPanel.getHeight();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Draw title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Sales Over Time", width / 2 - 50, 20);

        // Define chart area
        int chartX = 60;
        int chartY = 40;
        int chartWidth = width - 80;
        int chartHeight = height - 80;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight); // X-axis
        g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight); // Y-axis

        if (confirmedSales != null && !confirmedSales.isEmpty()) {
            Map<LocalDateTime, Double> salesByPeriod = getSalesByPeriod(confirmedSales, getPeriodField((String) periodDropdown.getSelectedItem()));
            List<Map.Entry<LocalDateTime, Double>> sortedSales = new ArrayList<>(salesByPeriod.entrySet());
            sortedSales.sort(Map.Entry.comparingByKey());

            double maxSale = sortedSales.stream().mapToDouble(Map.Entry::getValue).max().orElse(0);
            double xScale = (double) chartWidth / (sortedSales.size() - 1);
            double yScale = (double) chartHeight / maxSale;

            // Draw Y-axis labels
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 5; i++) {
                int y = chartY + chartHeight - (i * chartHeight / 5);
                g2d.drawString(String.format("$%.0f", maxSale * i / 5), 5, y);
                g2d.drawLine(chartX - 5, y, chartX, y); // Tick marks
            }

            // Draw X-axis labels
            for (int i = 0; i < sortedSales.size(); i += Math.max(1, sortedSales.size() / 5)) {
                int x = chartX + (int) (i * xScale);
                String label = sortedSales.get(i).getKey().toLocalDate().toString();
                g2d.drawString(label, x - 20, chartY + chartHeight + 20);
                g2d.drawLine(x, chartY + chartHeight, x, chartY + chartHeight + 5); // Tick marks
            }

            // Draw the line graph
            g2d.setColor(new Color(0, 120, 255));
            g2d.setStroke(new BasicStroke(2));
            for (int i = 0; i < sortedSales.size() - 1; i++) {
                int x1 = chartX + (int) (i * xScale);
                int y1 = chartY + chartHeight - (int) (sortedSales.get(i).getValue() * yScale);
                int x2 = chartX + (int) ((i + 1) * xScale);
                int y2 = chartY + chartHeight - (int) (sortedSales.get(i + 1).getValue() * yScale);
                g2d.drawLine(x1, y1, x2, y2);
            }

            // Draw points
            g2d.setColor(new Color(0, 80, 200));
            for (int i = 0; i < sortedSales.size(); i++) {
                int x = chartX + (int) (i * xScale);
                int y = chartY + chartHeight - (int) (sortedSales.get(i).getValue() * yScale);
                g2d.fillOval(x - 4, y - 4, 8, 8);
            }
        } else {
            g2d.drawString("No data available", width / 2 - 40, height / 2);
        }

        // Label axes
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Date", width / 2 - 15, height - 10);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Sales Amount ($)", -height / 2 - 50, 20);
        g2d.rotate(Math.PI / 2);
    }

    private void drawComparisonChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = comparisonChartPanel.getWidth();
        int height = comparisonChartPanel.getHeight();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Draw title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Confirmed vs Canceled Sales", width / 2 - 100, 20);

        // Define chart area
        int chartX = 60;
        int chartY = 40;
        int chartWidth = width - 80;
        int chartHeight = height - 80;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight); // X-axis
        g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight); // Y-axis

        if (confirmedSales != null && canceledSales != null) {
            Map<LocalDateTime, Double> confirmedByPeriod = getSalesByPeriod(confirmedSales, getPeriodField((String) periodDropdown.getSelectedItem()));
            Map<LocalDateTime, Double> canceledByPeriod = getSalesByPeriod(canceledSales, getPeriodField((String) periodDropdown.getSelectedItem()));

            Set<LocalDateTime> allDates = new TreeSet<>(confirmedByPeriod.keySet());
            allDates.addAll(canceledByPeriod.keySet());

            if (!allDates.isEmpty()) {
                double maxSale = Math.max(
                        confirmedByPeriod.values().stream().mapToDouble(v -> v).max().orElse(0),
                        canceledByPeriod.values().stream().mapToDouble(v -> v).max().orElse(0)
                );
                double xScale = (double) chartWidth / (allDates.size() - 1);
                double yScale = (double) chartHeight / maxSale;

                // Draw Y-axis labels
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                for (int i = 0; i <= 5; i++) {
                    int y = chartY + chartHeight - (i * chartHeight / 5);
                    g2d.drawString(String.format("$%.0f", maxSale * i / 5), 5, y);
                    g2d.drawLine(chartX - 5, y, chartX, y); // Tick marks
                }

                // Draw X-axis labels
                List<LocalDateTime> datesList = new ArrayList<>(allDates);
                for (int i = 0; i < datesList.size(); i += Math.max(1, datesList.size() / 5)) {
                    int x = chartX + (int) (i * xScale);
                    String label = datesList.get(i).toLocalDate().toString();
                    g2d.drawString(label, x - 20, chartY + chartHeight + 20);
                    g2d.drawLine(x, chartY + chartHeight, x, chartY + chartHeight + 5); // Tick marks
                }

                // Draw confirmed sales line
                drawSalesLine(g2d, confirmedByPeriod, allDates, chartX, chartY, xScale, yScale, chartHeight, new Color(0, 120, 255));

                // Draw canceled sales line
                drawSalesLine(g2d, canceledByPeriod, allDates, chartX, chartY, xScale, yScale, chartHeight, new Color(255, 0, 0));

                // Draw legend
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.setColor(new Color(0, 120, 255));
                g2d.fillRect(width - 140, 30, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Confirmed", width - 120, 43);
                g2d.setColor(new Color(255, 0, 0));
                g2d.fillRect(width - 140, 50, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Canceled", width - 120, 63);
            } else {
                g2d.drawString("No data available", width / 2 - 40, height / 2);
            }
        } else {
            g2d.drawString("No data available", width / 2 - 40, height / 2);
        }

        // Label axes
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Date", width / 2 - 15, height - 10);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Sales Amount ($)", -height / 2 - 50, 20);
        g2d.rotate(Math.PI / 2);
    }

    private void drawSalesLine(Graphics2D g2d, Map<LocalDateTime, Double> salesData, Set<LocalDateTime> allDates,
                               int chartX, int chartY, double xScale, double yScale, int chartHeight, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));

        List<LocalDateTime> datesList = new ArrayList<>(allDates);
        for (int i = 0; i < datesList.size() - 1; i++) {
            int x1 = chartX + (int) (i * xScale);
            int y1 = chartY + chartHeight - (int) (salesData.getOrDefault(datesList.get(i), 0.0) * yScale);
            int x2 = chartX + (int) ((i + 1) * xScale);
            int y2 = chartY + chartHeight - (int) (salesData.getOrDefault(datesList.get(i + 1), 0.0) * yScale);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw points
        for (int i = 0; i < datesList.size(); i++) {
            int x = chartX + (int) (i * xScale);
            int y = chartY + chartHeight - (int) (salesData.getOrDefault(datesList.get(i), 0.0) * yScale);
            g2d.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    private void drawDistributionChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = distributionChartPanel.getWidth();
        int height = distributionChartPanel.getHeight();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Draw title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Sales Distribution", width / 2 - 60, 20);

        // Define chart area
        int chartX = 60;
        int chartY = 40;
        int chartWidth = width - 80;
        int chartHeight = height - 80;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight); // X-axis
        g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight); // Y-axis

        if (confirmedSales != null && !confirmedSales.isEmpty()) {
            int numBins = 10;
            double maxAmount = confirmedSales.stream().mapToDouble(s -> s.amount).max().orElse(0);
            double binSize = maxAmount / numBins;

            int[] bins = new int[numBins];
            for (SalesEntry sale : confirmedSales) {
                int binIndex = (int) (sale.amount / binSize);
                if (binIndex == numBins) binIndex--;
                bins[binIndex]++;
            }

            int maxBinCount = Arrays.stream(bins).max().orElse(0);
            double xScale = (double) chartWidth / numBins;
            double yScale = (double) chartHeight / maxBinCount;

            // Draw Y-axis labels
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 5; i++) {
                int y = chartY + chartHeight - (i * chartHeight / 5);
                g2d.drawString(String.format("%d", maxBinCount * i / 5), 5, y);
                g2d.drawLine(chartX - 5, y, chartX, y); // Tick marks
            }

            // Draw X-axis labels
            for (int i = 0; i <= numBins; i++) {
                int x = chartX + (int) (i * xScale);
                String label = String.format("$%.0f", i * binSize);
                g2d.drawString(label, x - 15, chartY + chartHeight + 20);
                g2d.drawLine(x, chartY + chartHeight, x, chartY + chartHeight + 5); // Tick marks
            }

            // Draw bars
            g2d.setColor(new Color(0, 150, 136));
            for (int i = 0; i < numBins; i++) {
                int x = chartX + (int) (i * xScale);
                int y = chartY + chartHeight - (int) (bins[i] * yScale);
                int barWidth = (int) xScale - 2;
                int barHeight = (int) (bins[i] * yScale);
                g2d.fillRect(x, y, barWidth, barHeight);
            }
        } else {
            g2d.drawString("No data available", width / 2 - 40, height / 2);
        }

        // Label axes
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Sale Amount Range", width / 2 - 50, height - 10);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Frequency", -height / 2 - 30, 20);
        g2d.rotate(Math.PI / 2);
    }

    private List<SalesEntry> readSalesData(String filename) throws IOException {
        List<SalesEntry> salesData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String bookingId = parts[0];
                    String customerId = parts[1];
                    String hallId = parts[2];
                    LocalDateTime startTime = LocalDateTime.parse(parts[3], DATE_FORMAT);
                    LocalDateTime endTime = LocalDateTime.parse(parts[4], DATE_FORMAT);
                    double amount = Double.parseDouble(parts[5]);
                    salesData.add(new SalesEntry(bookingId, customerId, hallId, startTime, endTime, amount));
                }
            }
        }
        return salesData;
    }

    private Map<LocalDateTime, Double> getSalesByPeriod(List<SalesEntry> sales, int calendarField) {
        Map<LocalDateTime, Double> salesByPeriod = new TreeMap<>();

        for (SalesEntry sale : sales) {
            LocalDateTime periodStart = getPeriodStart(sale.startTime, calendarField);
            salesByPeriod.put(periodStart, salesByPeriod.getOrDefault(periodStart, 0.0) + sale.amount);
        }
        return salesByPeriod;
    }

    private LocalDateTime getPeriodStart(LocalDateTime date, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(java.sql.Timestamp.valueOf(date));
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendarField == Calendar.MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (calendarField == Calendar.YEAR) {
            calendar.set(Calendar.DAY_OF_YEAR, 1);
        }

        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    private int getPeriodField(String period) {
        switch (period) {
            case "Weekly": return Calendar.WEEK_OF_YEAR;
            case "Monthly": return Calendar.MONTH;
            case "Yearly": return Calendar.YEAR;
            default: return Calendar.DAY_OF_YEAR;
        }
    }

    private void printSummaryStatistics(List<SalesEntry> confirmedSales, List<SalesEntry> canceledSales) {
        double totalConfirmed = confirmedSales.stream().mapToDouble(sale -> sale.amount).sum();
        double totalCanceled = canceledSales.stream().mapToDouble(sale -> sale.amount).sum();

        System.out.printf("Total confirmed sales: $%.2f%n", totalConfirmed);
        System.out.printf("Total canceled sales: $%.2f%n", totalCanceled);
        System.out.printf("Number of confirmed bookings: %d%n", confirmedSales.size());
        System.out.printf("Number of canceled bookings: %d%n", canceledSales.size());
    }

    private static class SalesEntry {
        String bookingId;
        String customerId;
        String hallId;
        LocalDateTime startTime;
        LocalDateTime endTime;
        double amount;

        SalesEntry(String bookingId, String customerId, String hallId, LocalDateTime startTime, LocalDateTime endTime, double amount) {
            this.bookingId = bookingId;
            this.customerId = customerId;
            this.hallId = hallId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.amount = amount;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SalesAnalysisGUI();
        });
    }
}
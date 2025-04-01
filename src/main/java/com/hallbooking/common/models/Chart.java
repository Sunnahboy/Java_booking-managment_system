package main.java.com.hallbooking.common.models;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Chart extends JPanel {
    private List<LocalDateTime> dates;
    private List<Double> amounts;
    private String title;
    private String xLabel;
    private String yLabel;

    public Chart(List<LocalDateTime> dates, List<Double> amounts, String title, String xLabel, String yLabel) {
        this.dates = dates;
        this.amounts = amounts;
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGraph(g);
    }

    private void drawGraph(Graphics g) {
        // Set up drawing parameters
        g.setColor(Color.BLUE);
        int padding = 50;
        int width = getWidth() - 2 * padding;
        int height = getHeight() - 2 * padding;

        // Calculate max amount for scaling
        double maxAmount = amounts.stream().max(Double::compare).orElse(0.0);
        double minAmount = amounts.stream().min(Double::compare).orElse(0.0);

        // Draw axes
        g.drawLine(padding, height + padding, width + padding, height + padding); // X-axis
        g.drawLine(padding, padding, padding, height + padding); // Y-axis

        // Draw labels
        g.drawString(xLabel, width / 2 + padding, height + padding + 20);
        g.drawString(yLabel, padding - 40, height / 2 + padding);

        // Draw title
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(title, width / 2 + padding, padding - 10);

        // Draw the line graph
        if (dates.size() > 1 && amounts.size() > 1) {
            for (int i = 1; i < dates.size(); i++) {
                int x1 = padding + (i - 1) * (width / (dates.size() - 1));
                int y1 = height + padding - (int) ((amounts.get(i - 1) - minAmount) / (maxAmount - minAmount) * height);
                int x2 = padding + i * (width / (dates.size() - 1));
                int y2 = height + padding - (int) ((amounts.get(i) - minAmount) / (maxAmount - minAmount) * height);
                g.drawLine(x1, y1, x2, y2);
            }
        }

        // Optionally draw points
        g.setColor(Color.RED);
        for (int i = 0; i < dates.size(); i++) {
            int x = padding + i * (width / (dates.size() - 1));
            int y = height + padding - (int) ((amounts.get(i) - minAmount) / (maxAmount - minAmount) * height);
            g.fillOval(x - 5, y - 5, 10, 10); // Draw points as circles
        }
    }

    // Example method to generate the chart
    public void generateChart() {
        // Sample data for demonstration
        List<LocalDateTime> salesDates = new ArrayList<>();
        List<Double> salesAmounts = new ArrayList<>();

        List<LocalDateTime> cancellationDates = new ArrayList<>();
        List<Double> cancellationAmounts = new ArrayList<>();

        // Populate the sales data (example)
        salesDates.add(LocalDateTime.now().minusDays(2)); // Two days ago
        salesAmounts.add(150.0);

        salesDates.add(LocalDateTime.now().minusDays(1)); // One day ago
        salesAmounts.add(200.0);

        // Populate the cancellation data (example)
        cancellationDates.add(LocalDateTime.now().minusDays(1)); // One day ago
        cancellationAmounts.add(50.0);

        // Create the chart
        Chart chart = new Chart(salesDates, salesAmounts, "Sales and Cancellations Report", "Time", "Amount");
        chart.setPreferredSize(new Dimension(800, 600));
        JFrame chartFrame = new JFrame("Chart");
        chartFrame.add(chart);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chartFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Chart chart = new Chart(new ArrayList<>(), new ArrayList<>(), "Sample Chart", "X-Axis", "Y-Axis");
            chart.generateChart();
        });
    }
}

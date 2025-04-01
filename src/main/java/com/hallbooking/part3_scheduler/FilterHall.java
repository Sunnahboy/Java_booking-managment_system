package main.java.com.hallbooking.part3_scheduler;

import main.java.com.hallbooking.common.models.Hall;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterHall {
    private List<Hall> halls;

    public FilterHall() {
        halls = new ArrayList<>();
    }

    public void loadHallsFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    halls.add(new Hall(
                            parts[0],
                            Hall.HallType.valueOf(parts[1].replace(" ", "_")),
                            Integer.parseInt(parts[2]),
                            new BigDecimal(parts[3]),
                            parts[4]
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Hall> filterHalls(String criteria, String value) {
        switch (criteria.toLowerCase()) {
            case "all":
                return new ArrayList<>(halls);
            case "id":
                return halls.stream()
                        .filter(h -> fuzzyMatchString(h.getId(), value))
                        .collect(Collectors.toList());
            case "type":
                return halls.stream()
                        .filter(h -> fuzzyMatchString(h.getType().name(), value))
                        .collect(Collectors.toList());
            case "capacity":
                return halls.stream()
                        .filter(h -> fuzzyMatchNumber(h.getCapacity(), value, 0.2))  // 20% tolerance
                        .collect(Collectors.toList());
            case "rate":
                return halls.stream()
                        .filter(h -> fuzzyMatchNumber(h.getRate().doubleValue(), value, 0.2))  // 20% tolerance
                        .collect(Collectors.toList());
            case "location":
                return halls.stream()
                        .filter(h -> fuzzyMatchString(h.getLocation(), value))
                        .collect(Collectors.toList());
            default:
                System.out.println("Invalid filter criteria: " + criteria);
                return new ArrayList<>();
        }
    }

    private boolean fuzzyMatchString(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        return s1.contains(s2) || s2.contains(s1) || levenshteinDistance(s1, s2) <= 2;
    }

    private boolean fuzzyMatchNumber(double value, String input, double tolerance) {
        try {
            double inputValue = Double.parseDouble(input);
            double lowerBound = inputValue * (1 - tolerance);
            double upperBound = inputValue * (1 + tolerance);
            return value >= lowerBound && value <= upperBound;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public List<Hall> getAllHalls() {
        return new ArrayList<>(halls);
    }
}
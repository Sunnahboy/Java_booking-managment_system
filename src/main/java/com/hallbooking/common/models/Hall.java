package main.java.com.hallbooking.common.models;

import java.math.BigDecimal;

public class Hall {
    private final String id;
    private HallType type;
    private int capacity;
    private BigDecimal rate;
    private String location;

    public enum HallType {
        AUDITORIUM,
        MEETING_ROOM,
        BANQUET_HALL
    }

    public Hall(String id, HallType type, int capacity, BigDecimal rate, String location) {
        this.id = id;
        this.type = type; // Directly assign the HallType without conversion
        this.capacity = capacity;
        this.rate = rate;
        this.location = location;
    }

    // Getters
    public String getId() { return id; }
    public HallType getType() { return type; }
    public int getCapacity() { return capacity; }
    public BigDecimal getRate() { return rate; }
    public String getLocation() { return location; }

    // Setters
    public void setType(HallType type) { this.type = type; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return "Hall{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", capacity=" + capacity +
                ", rate=" + rate +
                ", location='" + location + '\'' +
                '}';
    }

    // Custom method to calculate cost for a given number of hours
    public BigDecimal calculateCost(int hours) {
        return this.rate.multiply(BigDecimal.valueOf(hours));
    }
}

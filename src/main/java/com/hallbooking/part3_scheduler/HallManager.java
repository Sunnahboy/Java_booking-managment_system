package main.java.com.hallbooking.part3_scheduler;

import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;

import java.time.LocalDateTime;
import java.util.List;

public class HallManager {
    private SchedulerManager schedulerManager;

    public HallManager() {
        this.schedulerManager = new SchedulerManager();
    }

    public void addHall(Hall hall) throws InvalidInputException {
        schedulerManager.addHall(hall);
    }

    public List<Hall> getHalls() {
        return schedulerManager.getHalls();
    }

    public void updateHall(Hall hall) throws HallNotFoundException {
        schedulerManager.updateHall(hall);
    }

    public void deleteHall(String hallId) throws HallNotFoundException, InvalidInputException {
        schedulerManager.deleteHall(hallId);
    }

    public void setHallAvailability(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) throws InvalidBookingException, HallNotFoundException {
        schedulerManager.setHallAvailability(hallId, startDateTime, endDateTime);
    }


}

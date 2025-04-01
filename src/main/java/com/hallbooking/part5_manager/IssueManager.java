package main.java.com.hallbooking.part5_manager;

import main.java.com.hallbooking.common.exceptions.CustomExceptions;
import main.java.com.hallbooking.common.models.Issue;

import java.io.IOException;
import java.util.List;

public class IssueManager {
    private ManagerDashboard managerDashboard;

    public IssueManager() {
        this.managerDashboard = new ManagerDashboard();
    }

    public List<Issue> getOpenIssues() {
        return managerDashboard.getIssuesByStatus("OPEN");
    }

    public List<Issue> getAssignedIssues() {
        return managerDashboard.getIssuesByStatus("ASSIGNED");
    }

    public List<Issue> getInProgressIssues() {
        return managerDashboard.getIssuesByStatus("IN_PROGRESS");
    }

    public List<Issue> getClosedIssues() {
        return managerDashboard.getIssuesByStatus("CLOSED");
    }

    //Assigning an issue to a scheduler without changing its status
    public void assignIssue(String issueId, String schedulerId) throws CustomExceptions.IssueNotFoundException, CustomExceptions.UserNotFoundException {
        managerDashboard.assignIssueToScheduler(issueId, schedulerId); // Assign without modifying status
    }

    // Method to close issues with IN_PROGRESS status if the maintenance end time has passed
    public void closeIssues() throws IOException {
        managerDashboard.closeIssues();
    }
}

package com.hallbooking;

import main.java.com.hallbooking.part1_login.LoginManager;
import main.java.com.hallbooking.part1_login.gui.LoginScreen;
import main.java.com.hallbooking.part2_admin.gui.AdminDashboard;
import main.java.com.hallbooking.part2_admin.gui.ExtraAdminDashboard;
import main.java.com.hallbooking.part3_scheduler.gui.SchedulerDashboard;
import main.java.com.hallbooking.part4_customer.gui.CompleteRegistrationGUI;
import main.java.com.hallbooking.part4_customer.gui.CustomerDashboard;
import main.java.com.hallbooking.part5_manager.gui.ManagerDashboardScreen;
import main.java.com.hallbooking.common.models.User;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;
import javax.swing.*;

public class Main {
    private static LoginManager loginManager = new LoginManager();
    private static LoginScreen loginScreen;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            loginScreen = new LoginScreen();
            loginScreen.setLoginCallback(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess(User user) {
                    if ("block".equals(user.getStatus())) {
                        JOptionPane.showMessageDialog(null, "Your account is blocked. Please contact support.", "Account Blocked", JOptionPane.WARNING_MESSAGE);
                        loginScreen.setVisible(true); // Reset the login screen
                    } else {
                        openDashboard(user); // Proceed to open the dashboard
                    }
                }

                @Override
                public void onLoginFailure(String errorMessage) {
                    JOptionPane.showMessageDialog(null, errorMessage, "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            });
            loginScreen.setVisible(true);
        });
    }

    private static void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            JFrame dashboard = null;

            // Create the correct dashboard based on the user type
            switch (user.getUserType()) {
                case "SuperAdmin":
                    dashboard = new AdminDashboard();
                    break;
                case "Admin":
                    dashboard = new ExtraAdminDashboard();
                    break;
                case "Scheduler":
                    dashboard = new SchedulerDashboard();
                    break;
                case "Customer":
                    dashboard = new CustomerDashboard(user.getId());
                    break;
                case "Manager":
                    dashboard = new ManagerDashboardScreen();
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Unknown user type", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            if (dashboard != null) {
                loginScreen.setVisible(false); // Hide the login screen on successful login
                dashboard.setVisible(true); // Show the dashboard

                // Set the logout callback for each dashboard
                if (dashboard instanceof AdminDashboard) {
                    JFrame finalDashboard = dashboard;
                    ((AdminDashboard) dashboard).setLogoutCallback(() -> {
                        finalDashboard.dispose(); // Close the dashboard window
                        loginScreen.setVisible(true); // Show the login screen again
                    });
                } else if (dashboard instanceof ExtraAdminDashboard) {
                    JFrame finalDashboard1 = dashboard;
                    ((ExtraAdminDashboard) dashboard).setLogoutCallback(() -> {
                        finalDashboard1.dispose();
                        loginScreen.setVisible(true);
                    });
                } else if (dashboard instanceof SchedulerDashboard) {
                    JFrame finalDashboard2 = dashboard;
                    ((SchedulerDashboard) dashboard).setLogoutCallback(() -> {
                        finalDashboard2.dispose();
                        loginScreen.setVisible(true);
                    });
                } else if (dashboard instanceof CustomerDashboard) {
                    JFrame finalDashboard3 = dashboard;
                    ((CustomerDashboard) dashboard).setLogoutCallback(() -> {
                        finalDashboard3.dispose();
                        loginScreen.setVisible(true);
                    });
                } else if (dashboard instanceof ManagerDashboardScreen) {
                    JFrame finalDashboard4 = dashboard;
                    ((ManagerDashboardScreen) dashboard).setLogoutCallback(() -> {
                        finalDashboard4.dispose();
                        loginScreen.setVisible(true);
                    });
                }
            }
        });
    }
}

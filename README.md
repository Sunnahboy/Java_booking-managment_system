# Hall  Booking Management System

## Project Overview
A Java-based GUI application for Hall Symphony Inc. that manages hall bookings for events such as conferences, weddings, and meetings. The system serves four user roles (Scheduler, Customer, Administrator, and Manager) with specific features tailored to each role's responsibilities.

## Features

### Scheduler (Staff) Features
- Login functionality
- Hall management (add, view, edit, delete hall information)
- Schedule hall availability
- Schedule hall maintenance
- Logout functionality

### Customer Features
- Registration and login
- Profile management
- Hall booking process
- View and filter booking history
- Booking cancellation (at least 3 days before booking date)
- Issue reporting
- Logout functionality

### Administrator Features
- Login functionality
- Scheduler staff management
- User management
- Booking oversight
- Logout functionality

### Manager Features
- Login functionality
- Sales dashboard with filtering
- Maintenance operation management
- Customer issue tracking and resolution
- Logout functionality

## System Architecture
- Built using Java and Object-Oriented Programming principles
- Data storage using text files (.txt)
- GUI implemented with Java Swing

## Installation and Setup
1. Ensure you have Java Development Kit (JDK) installed
2. Clone the repository or download the source code
3. Compile the Java files
4. Run the main application file

## Usage Guide
```
java -jar HallBookingSystem.jar
```

Select your user role and log in with appropriate credentials:
- **Scheduler**: username - scheduler, password - scheduler123
- **Administrator**: username - admin, password - admin123
- **Manager**: username - manager, password - manager123
- **Customer**: Register through the registration page


## Object-Oriented Concepts Implemented
- Inheritance and polymorphism in user classes
- Encapsulation of data within classes
- Interface implementation
- Design patterns (MVC architecture)

## Development Team
- [Team Member 1]
- [Team Member 2]
- [Team Member 3]
- [Team Member 4]

## Limitations
- The system operates only during business hours (8:00 AM to 6:00 PM)
- Text file-based storage has limited scalability for large datasets
- No integration with external payment systems

## For documentation check out here
### https://docs.google.com/document/d/1U469yI3aRiU1KUXMU38YQckey6k7rSf-R0K8sIDsGDI/edit?tab=t.0

---

*Developed as part of the Object-Oriented Programming coursework at Asia Pacific University*

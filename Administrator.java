import java.io.File;//For handling files and directories
import java.io.FileInputStream;//For reading data from a file
import java.io.FileOutputStream;//For writing data to a file
import java.io.IOException;//For handling input/output exceptions
import java.io.InputStream;//Base class for reading byte streams
import java.io.OutputStream;//Base class for writing byte streams

public class Administrator extends User {
    private int securityLevel;

    public Administrator(int userId, String username, String password, String name, 
                        String email, String contactInfo, int securityLevel) {
        super(userId, username, password, name, email, contactInfo);
        this.securityLevel = securityLevel;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public User createUser(String userType, String username, String password, String name, String email, String contactInfo, String additionalInfo) {
        // Input validation
        if (userType == null || username == null || password == null || name == null || email == null || contactInfo == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        BookingSystem system = BookingSystem.getInstance();
        
        // Check if username already exists
        for (User u : system.getUsers()) {
            if (u.getUsername().equals(username)) {
                throw new IllegalArgumentException("Username already exists");
            }
        }

        int newUserId = system.getUsers().size() + 1;
        
        try {
            switch (userType.toLowerCase()) {
                case "customer":
                    Customer customer = new Customer(newUserId, username, password, name, 
                                                  email, contactInfo, additionalInfo);
                    system.addUser(customer);
                    System.out.println("Customer created successfully: " + username);
                    return customer;
                    
                case "agent":
                    Agent agent = new Agent(newUserId, username, password, name, 
                                          email, contactInfo, additionalInfo);
                    system.addUser(agent);
                    System.out.println("Agent created successfully: " + username);
                    return agent;
                    
                case "admin":
                    if (this.securityLevel < 5) { // Example: Only high-level admins can create admins
                        throw new SecurityException("Insufficient security level to create admin");
                    }
                    int newSecurityLevel = Integer.parseInt(additionalInfo);
                    if (newSecurityLevel >= this.securityLevel) {
                        throw new SecurityException("Cannot create admin with equal or higher security level");
                    }
                    Administrator admin = new Administrator(newUserId, username, password, name, email, contactInfo, newSecurityLevel);
                    system.addUser(admin);
                    System.out.println("Administrator created successfully: " + username);
                    return admin;
                    
                default:
                    throw new IllegalArgumentException("Invalid user type: " + userType);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid security level format");
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteUser(int userId) {
  
    if (userId <= 0) {
        System.err.println("Invalid user ID: " + userId);
        return false;
    }

    BookingSystem system = BookingSystem.getInstance();
    
   
    if (userId == this.getUserId()) {
        System.err.println("Cannot delete own account");
        return false;
    }

    try {
        User userToDelete = null;
        
       
        for (User user : system.getUsers()) {
            if (user.getUserId() == userId) {
                userToDelete = user;
                break;
            }
        }

        if (userToDelete == null) {
            System.err.println("User not found: " + userId);
            return false;
        }

        
        if (userToDelete instanceof Administrator) {
            Administrator admin = (Administrator) userToDelete;
            if (admin.getSecurityLevel() >= this.securityLevel) {
                System.err.println("Cannot delete admin with equal or higher security level");
                return false;
            }
        }

        
        if (hasUserBookings(userId)) {
            System.err.println("Cannot delete user with active bookings");
            return false;
        }

        
        synchronized (system.getUsers()) {
            boolean removed = system.getUsers().remove(userToDelete);
            if (removed) {
                System.out.println("User deleted successfully: " + userToDelete.getUsername());
                system.deleteUser(userToDelete);
                FileManager.saveAllData(system); 
            } else {
                System.err.println("Failed to delete user: " + userToDelete.getUsername());
            }
            return removed;
        }
    } catch (Exception e) {
        System.err.println("Error deleting user: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

    private boolean hasUserBookings(int userId) {
    for (Booking booking : BookingSystem.getInstance().getBookings()) {
        if (booking.getUser().getUserId() == userId) {
            return true;
        }
    }
    return false;
}

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    public String generateSystemReport() {
    BookingSystem system = BookingSystem.getInstance();
    StringBuilder report = new StringBuilder();
    
    report.append("=== System Report ===\n");
    report.append("Total Users: ").append(system.getUsers().size()).append("\n");
    report.append("Total Flights: ").append(system.getFlights().size()).append("\n");
    report.append("Total Bookings: ").append(system.getBookings().size()).append("\n");
    report.append("Total Payments: ").append(system.getPayments().size()).append("\n");
    
    double totalRevenue = 0;
    for (Payment payment : system.getPayments()) {
        if (payment.getStatus().equals("Completed")) {
            totalRevenue += payment.getAmount();
        }
    }
    report.append("Total Revenue: $").append(totalRevenue).append("\n");
    
    return report.toString();
}

    public void backupSystemData() {
    try {
       
        File backupDir = new File("backup");
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        
        copyFile("users.txt", "backup/users_backup.txt");
        copyFile("flights.txt", "backup/flights_backup.txt");
        copyFile("bookings.txt", "backup/bookings_backup.txt");
        copyFile("passengers.txt", "backup/passengers_backup.txt");
        copyFile("payments.txt", "backup/payments_backup.txt");

        System.out.println("System data backed up successfully.");
    } catch (IOException e) {
        System.err.println("Backup failed: " + e.getMessage());
    }
}

private void copyFile(String sourcePath, String destPath) throws IOException {
    File source = new File(sourcePath);
    File dest = new File(destPath);
    
    try (InputStream in = new FileInputStream(source);
         OutputStream out = new FileOutputStream(dest)) {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }
}

    

    @Override
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    @Override
    public void logout() {
        System.out.println("Administrator " + name + " logged out successfully");
    }

    @Override
    public void updateProfile(String name, String email, String contactInfo) {
        if (name != null && !name.isEmpty()) this.name = name;
        if (email != null && !email.isEmpty()) this.email = email;
        if (contactInfo != null && !contactInfo.isEmpty()) this.contactInfo = contactInfo;
    }
    public String generateTicket(Booking booking) {
    return String.format(
        """
        === ADMIN TICKET (Security Level: %d) ===
        Booking Ref: %s
        Created By: %s
        Flight: %s
        Status: %s
        Security Check: APPROVED
        """,
        this.securityLevel,
        booking.getBookingReference(),
        this.getName(),
        booking.getFlight().getFlightNumber(),
        booking.getStatus()
    );
}
}
import java.util.ArrayList;
import java.time.LocalDateTime;

class Agent extends User {
    private String department;
    private double commission;
    private ArrayList<Booking> agentBookings; 

    public Agent(int userId, String username, String password, String name, String email, String contactInfo, String department) {
        super(userId, username, password, name, email, contactInfo);
        this.department = department;
        this.commission = 0.05;
        this.agentBookings = new ArrayList<>();
    }

    @Override
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    @Override
    public void logout() {
        System.out.println("Agent " + name + " logged out successfully");
    }

    @Override
    public void updateProfile(String name, String email, String contactInfo) {
        if (name != null && !name.isEmpty()) this.name = name;
        if (email != null && !email.isEmpty()) this.email = email;
        if (contactInfo != null && !contactInfo.isEmpty()) this.contactInfo = contactInfo;
    }

    public void manageFlight(Flight flight, LocalDateTime newDepartureTime, LocalDateTime newArrivalTime) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        flight.updateSchedule(newDepartureTime, newArrivalTime);
    }

    public Booking createBookingForCustomer(Customer customer, Flight flight, ArrayList<Passenger> passengers, String seatClass) {
        if (customer == null || flight == null || passengers == null || passengers.isEmpty()) {
            throw new IllegalArgumentException("Invalid booking parameters");
        }

        if (!flight.checkAvailability(seatClass, passengers.size())) {
            throw new IllegalStateException("Not enough seats available in " + seatClass + " class");
        }

        Booking booking = new Booking(generateBookingReference(),customer,flight,passengers,seatClass,"Pending");

        // Process commission (5% of total price)
        double bookingTotal = booking.calculateTotalPrice();
        this.commission += bookingTotal * 0.05;

        // Reserve seats and add to booking lists
        flight.reserveSeat(seatClass, passengers.size());
        customer.viewBookings().add(booking);
        this.agentBookings.add(booking);
        BookingSystem.getInstance().addBooking(booking);

        return booking;
    }

    public void modifyBooking(Booking booking, Flight newFlight, ArrayList<Passenger> newPassengers) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        // Validate new passengers if provided
        if (newPassengers != null && newPassengers.isEmpty()) {
            throw new IllegalArgumentException("Passenger list cannot be empty");
        }

        // Calculate seat difference
        int currentSeats = booking.getPassengers().size();
        int newSeats = (newPassengers != null) ? newPassengers.size() : currentSeats;
        int seatDifference = newSeats - currentSeats;

        // Check availability if increasing seats
        if (seatDifference > 0) {
            Flight targetFlight = (newFlight != null) ? newFlight : booking.getFlight();
            if (!targetFlight.checkAvailability(booking.getSeatClass(), seatDifference)) {
                throw new IllegalStateException("Not enough seats available for modification");
            }
        }

        // Release seats from old flight if changing flight
        if (newFlight != null && !newFlight.equals(booking.getFlight())) {
            booking.getFlight().reserveSeat(booking.getSeatClass(), -currentSeats);
        }

        // Reserve seats (net difference)
        if (seatDifference != 0) {
            Flight targetFlight = (newFlight != null) ? newFlight : booking.getFlight();
            targetFlight.reserveSeat(booking.getSeatClass(), seatDifference);
        }

        // Apply changes to booking
        booking.modifyBooking(
            (newFlight != null) ? newFlight : booking.getFlight(),
            (newPassengers != null) ? newPassengers : booking.getPassengers()
        );
    }

    public void generateReport() {
        System.out.println("\n=== AGENT BOOKING REPORT ===");
        System.out.println("Agent: " + this.getName() + " | Department: " + department);
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-15s %-20s %-15s %-10s %-10s%n", 
                        "Booking Ref", "Customer", "Flight", "Status", "Amount");
        
        double totalSales = 0;
        for (Booking booking : agentBookings) {
            System.out.printf("%-15s %-20s %-15s %-10s $%-9.2f%n", 
                            booking.getBookingReference(),
                            booking.getUser().getName(),
                            booking.getFlight().getFlightNumber(),
                            booking.getStatus(),
                            booking.calculateTotalPrice());
            totalSales += booking.calculateTotalPrice();
        }
        
        System.out.println("------------------------------------------------------------");
        System.out.printf("Total Bookings: %d | Total Sales: $%.2f | Your Commission: $%.2f%n",
                        agentBookings.size(), totalSales, this.commission);
    }

    private String generateBookingReference() {
        return "AGT-" + userId + "-" + System.currentTimeMillis();
    }

    // Getters
    public String getDepartment() { return department; }
    public double getCommission() { return commission; }
    public ArrayList<Booking> getAgentBookings() { return new ArrayList<>(agentBookings); }

    // Setters
    public void setDepartment(String department) { 
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty");
        }
        this.department = department; 
    }

    public void addCommission(double amount) { 
        if (amount < 0) {
            throw new IllegalArgumentException("Commission cannot be negative");
        }
        this.commission += amount; 
    }
    public String generateTicket(Booking booking) {
    double commission = booking.calculateTotalPrice() * 0.05; 
    return String.format(
        """
        === AGENT E-TICKET (Commission: $%.2f) ===
        Booking Ref: %s
        Customer: %s
        Flight: %s
        Departure: %s
        Seat Class: %s
        Commission Earned: $%.2f
        """,
        commission,
        booking.getBookingReference(),
        booking.getUser().getName(),
        booking.getFlight().getFlightNumber(),
        booking.getFlight().getDepartureTime(),
        booking.getSeatClass(),
        commission
    );
}
}
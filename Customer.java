import java.util.ArrayList;
import java.time.LocalDateTime;

class Customer extends User {
    private String address;
    private ArrayList<Booking> bookingHistory;
    private ArrayList<String> preferences;

    public Customer(int userId, String username, String password, String name, 
                   String email, String contactInfo, String address) {
        super(userId, username, password, name, email, contactInfo);
        this.address = address;
        this.bookingHistory = new ArrayList<>();
        this.preferences = new ArrayList<>();
    }

    @Override
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    @Override
    public void logout() {
        System.out.println("Customer " + name + " logged out successfully");
    }

    @Override
    public void updateProfile(String name, String email, String contactInfo) {
        if (name != null && !name.isEmpty()) this.name = name;
        if (email != null && !email.isEmpty()) this.email = email;
        if (contactInfo != null && !contactInfo.isEmpty()) this.contactInfo = contactInfo;
    }

    public ArrayList<Flight> searchFlights(String origin, String destination, LocalDateTime date) {
        return BookingSystem.getInstance().searchFlights(origin, destination, date);
    }

    public Booking createBooking(Flight flight, ArrayList<Passenger> passengers, String seatClass) {
        if (!flight.checkAvailability(seatClass, passengers.size())) {
            throw new IllegalStateException("Not enough seats available");
        }
        
        Booking booking = new Booking(
            generateBookingReference(),
            this,
            flight,
            passengers,
            seatClass,
            "Pending"
        );
        
        flight.reserveSeat(seatClass, passengers.size());
        bookingHistory.add(booking);
        BookingSystem.getInstance().addBooking(booking);
        return booking;
    }

    public ArrayList<Booking> viewBookings() {
        return new ArrayList<>(bookingHistory);
    }

    public void cancelBooking(Booking booking) {
        if (bookingHistory.contains(booking)) {
            booking.cancelBooking();
            bookingHistory.remove(booking);
        }
    }

    private String generateBookingReference() {
        return "CUST-" + userId + "-" + System.currentTimeMillis();
    }

    public Payment makePayment(Booking booking, String paymentMethod) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        
        if (!bookingHistory.contains(booking)) {
            throw new IllegalStateException("This booking doesn't belong to the customer");
        }
        
        Payment payment = BookingSystem.getInstance().processPayment(booking, paymentMethod);
        if (payment != null && payment.getStatus().equals("Completed")) {
            booking.confirmBooking();
        }
        return payment;
    }

    public void viewBookingDetails(String bookingRef) {
        for (Booking booking : bookingHistory) {
            if (booking.getBookingReference().equals(bookingRef)) {
                booking.generateItinerary();
                return;
            }
        }
        System.out.println("Booking not found");
    }

    public boolean modifyBooking(String bookingRef, Flight newFlight, ArrayList<Passenger> newPassengers) {
        for (Booking booking : bookingHistory) {
            if (booking.getBookingReference().equals(bookingRef)) {
                try {
                    booking.modifyBooking(newFlight, newPassengers);
                    return true;
                } catch (Exception e) {
                    System.out.println("Error modifying booking: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    // Getters and setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public void addPreference(String preference) { preferences.add(preference); }
    public ArrayList<String> getPreferences() { return new ArrayList<>(preferences); }
    public String generateTicket(Booking booking) {
    return String.format(
        """
        === CUSTOMER E-TICKET ===
        Booking Ref: %s
        Passenger: %s
        Flight: %s (%s → %s)
        Departure: %s
        Seat Class: %s
        Total Paid: $%.2f
        Payment Method: %s
        """,
        booking.getBookingReference(),
        this.getName(),
        booking.getFlight().getFlightNumber(),
        booking.getFlight().getOrigin(),
        booking.getFlight().getDestination(),
        booking.getFlight().getDepartureTime(),
        booking.getSeatClass(),
        booking.calculateTotalPrice(),
        booking.getPaymentStatus()
    );
}
}
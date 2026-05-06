import java.util.ArrayList;
import java.time.LocalDateTime;

class BookingSystem {
    private static BookingSystem instance;
    private ArrayList<User> users;
    private ArrayList<Flight> flights;
    private ArrayList<Booking> bookings;
    private ArrayList<Payment> payments;

    private BookingSystem() {
        this.users = new ArrayList<>();
        this.flights = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.payments = new ArrayList<>();
    }

    public static BookingSystem getInstance() {
        if (instance == null) {
            instance = new BookingSystem();
        }
        return instance;
    }

    public ArrayList<Flight> searchFlights(String origin, String destination, LocalDateTime date) {
        ArrayList<Flight> results = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getOrigin().equalsIgnoreCase(origin) && 
                flight.getDestination().equalsIgnoreCase(destination) &&
                flight.getDepartureTime().toLocalDate().equals(date.toLocalDate())) {
                results.add(flight);
            }
        }
        return results;
    }

    public Booking createBooking(User user, Flight flight, String seatClass, int numberOfSeats) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }
        
        if (!flight.checkAvailability(seatClass, numberOfSeats)) {
            throw new IllegalStateException("Not enough seats available");
        }
        
        Booking booking = new Booking(
            generateBookingReference(),
            user,
            flight,
            seatClass,
            numberOfSeats
        );
        
        flight.reserveSeat(seatClass, numberOfSeats);
        bookings.add(booking);
        return booking;
    }

    public Payment processPayment(Booking booking, String paymentMethod) {
        double amount = booking.getFlight().calculatePrice(booking.getSeatClass(), booking.getNumberOfSeats());
        Payment payment = new Payment(
            generatePaymentId(),
            booking.getBookingReference(),
            amount,
            paymentMethod
        );
        
        if (payment.processPayment()) {
            payments.add(payment);
            booking.confirmBooking();
            return payment;
        }
        return null;
    }

    public String generateTicket(Booking booking) {
        StringBuilder ticket = new StringBuilder();
        ticket.append("=== E-TICKET ===\n");
        ticket.append("Booking Ref: ").append(booking.getBookingReference()).append("\n");
        ticket.append("Passenger: ").append(booking.getUser().getName()).append("\n");
        ticket.append("Flight: ").append(booking.getFlight().getFlightNumber()).append("\n");
        ticket.append("From: ").append(booking.getFlight().getOrigin()).append("\n");
        ticket.append("To: ").append(booking.getFlight().getDestination()).append("\n");
        ticket.append("Departure: ").append(booking.getFlight().getDepartureTime()).append("\n");
        ticket.append("Arrival: ").append(booking.getFlight().getArrivalTime()).append("\n");
        ticket.append("Seat Class: ").append(booking.getSeatClass()).append("\n");
        ticket.append("Seats: ").append(booking.getNumberOfSeats()).append("\n");
        ticket.append("Status: CONFIRMED\n");
        return booking.getUser().generateTicket(booking);
    }

    // Methods to add data to system
    public void addUser(User user) { users.add(user); }
    public void deleteUser(User user){
        users.remove(user);
    }
    public boolean addFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        
        // Check for duplicate flight number
        for (Flight f : flights) {
            if (f.getFlightNumber().equals(flight.getFlightNumber())) {
                return false;
            }
        }
        
        flights.add(flight);
        return true;
    }

    public boolean removeFlight(String flightNumber) {
        Flight toRemove = null;
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equals(flightNumber)) {
                toRemove = flight;
                break;
            }
        }
        
        if (toRemove != null) {
            // Check if flight has any bookings
            for (Booking booking : bookings) {
                if (booking.getFlight().equals(toRemove)) {
                    return false; // Can't remove flight with existing bookings
                }
            }
            flights.remove(toRemove);
            return true;
        }
        return false;
    }

    public ArrayList<Booking> searchBookings(String customerName, String flightNumber) {
        ArrayList<Booking> results = new ArrayList<>();
        for (Booking booking : bookings) {
            boolean nameMatch = customerName == null || 
                               booking.getUser().getName().equalsIgnoreCase(customerName);
            boolean flightMatch = flightNumber == null || 
                                booking.getFlight().getFlightNumber().equalsIgnoreCase(flightNumber);
            
            if (nameMatch && flightMatch) {
                results.add(booking);
            }
        }
        return results;
    }

    public boolean updateBookingStatus(String bookingRef, String newStatus) {
        for (Booking booking : bookings) {
            if (booking.getBookingReference().equals(bookingRef)) {
                if (newStatus.equals("Cancelled")) {
                    booking.cancelBooking();
                } else if (newStatus.equals("Confirmed")) {
                    booking.confirmBooking();
                } else {
                    throw new IllegalArgumentException("Invalid status");
                }
                return true;
            }
        }
        return false;
    }

    // Getters
    public ArrayList<User> getUsers() { return new ArrayList<>(users); }
    public ArrayList<Flight> getFlights() { return new ArrayList<>(flights); }
    public ArrayList<Booking> getBookings() { return new ArrayList<>(bookings); }
    public ArrayList<Payment> getPayments() { return new ArrayList<>(payments); }

    private String generateBookingReference() {
        return "BK-" + System.currentTimeMillis();
    }

    private String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis();
    }
    
    public void addBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        bookings.add(booking);
    }
    
    public void setUsers(ArrayList<User> users) {
        if (users == null) {
            throw new IllegalArgumentException("Users list cannot be null");
        }
        this.users = new ArrayList<>(users);
    }

    public void setFlights(ArrayList<Flight> flights) {
        if (flights == null) {
            throw new IllegalArgumentException("Flights list cannot be null");
        }
        this.flights = new ArrayList<>(flights);
    }

    public void setBookings(ArrayList<Booking> bookings) {
        if (bookings == null) {
            throw new IllegalArgumentException("Bookings list cannot be null");
        }
        this.bookings = new ArrayList<>(bookings);
    }  
    
    public void setPayments(ArrayList<Payment> payments) {
        this.payments = payments != null ? new ArrayList<>(payments) : new ArrayList<>();
    }
}
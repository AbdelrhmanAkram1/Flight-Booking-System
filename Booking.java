import java.util.ArrayList;

class Booking {
    private String bookingReference;
    private User user;
    private Flight flight;
    private ArrayList<Passenger> passengers;//Booking has Passenger(composition)
    private String seatClass;
    private int numberOfSeats;
    private String status;
    private String paymentStatus;

    public Booking(String bookingReference, User user, Flight flight, String seatClass, int numberOfSeats) {
        this.bookingReference = bookingReference;
        this.user = user;
        this.flight = flight;
        this.seatClass = seatClass;
        this.numberOfSeats = numberOfSeats;
        this.status = "Pending";
        this.paymentStatus = "Pending";
        this.passengers = new ArrayList<>();
    }

    public Booking(String bookingReference, User user, Flight flight, ArrayList<Passenger> passengers, String seatClass, String status) {
        this(bookingReference, user, flight, seatClass, passengers.size());
        this.passengers = new ArrayList<>(passengers);
        this.status = status;
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
    }

    public double calculateTotalPrice() {
        return flight.calculatePrice(seatClass, numberOfSeats);
    }

    public void confirmBooking() {
        if ("Confirmed".equals(status)) {
            throw new IllegalStateException("Booking is already confirmed");
        }
        status = "Confirmed";
        paymentStatus = "Paid";
    }

    public boolean cancelBooking() {
    if ("Cancelled".equals(status)) {
        return false; // Booking was already cancelled
    }
    
    try {
        status = "Cancelled";
        paymentStatus = "Refunded";
        flight.reserveSeat(seatClass, -numberOfSeats); // Release seats
        return true; // Cancellation successful
    } catch (Exception e) {
        return false; // Cancellation failed
    }
}

    public void generateItinerary() {
        System.out.println("\n=== Flight Itinerary ===");
        System.out.println("Booking Reference: " + bookingReference);
        System.out.println("User: " + user.getName());
        System.out.println("Flight: " + flight.getFlightNumber() + " (" + flight.getAirline() + ")");
        System.out.println("Route: " + flight.getOrigin() + " to " + flight.getDestination());
        System.out.println("Departure: " + flight.getDepartureTime());
        System.out.println("Arrival: " + flight.getArrivalTime());
        System.out.println("Passengers (" + passengers.size() + "):");
        for (Passenger p : passengers) {
            System.out.println(" - " + p.getName() + " (" + p.getPassportNumber() + ")");
        }
        System.out.println("Seat Class: " + seatClass);
        System.out.println("Number of Seats: " + numberOfSeats);
        System.out.printf("Total Price: $%.2f%n", calculateTotalPrice());
        System.out.println("Status: " + status);
        System.out.println("Payment Status: " + paymentStatus);
    }

    public void modifyBooking(Flight newFlight, ArrayList<Passenger> newPassengers) {
        if (newPassengers == null || newPassengers.isEmpty()) {
            throw new IllegalArgumentException("Passenger list cannot be empty");
        }

        // Release seats from old flight
        this.flight.reserveSeat(this.seatClass, -this.numberOfSeats);

        // Update flight if provided
        if (newFlight != null) {
            this.flight = newFlight;
        }

        // Update passengers and number of seats
        this.passengers = new ArrayList<>(newPassengers);
        this.numberOfSeats = newPassengers.size();

        // Reserve seats in new flight
        this.flight.reserveSeat(this.seatClass, this.numberOfSeats);
    }

    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || !(paymentStatus.equals("Pending") || 
            paymentStatus.equals("Paid") || paymentStatus.equals("Refunded"))) {
            throw new IllegalArgumentException("Invalid payment status");
        }
        this.paymentStatus = paymentStatus;
    }

    // Getters
    public String getBookingReference() { return bookingReference; }
    public User getUser() { return user; }
    public Flight getFlight() { return flight; }
    public ArrayList<Passenger> getPassengers() { return new ArrayList<>(passengers); }
    public String getSeatClass() { return seatClass; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
}
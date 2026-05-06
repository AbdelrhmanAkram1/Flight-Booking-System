import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    // File paths
    private static final String USERS_FILE = "users.txt";
    private static final String FLIGHTS_FILE = "flights.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String PASSENGERS_FILE = "passengers.txt";
    private static final String PAYMENTS_FILE = "payments.txt";
    
    // Date formatter
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Save all system data
    public static void saveAllData(BookingSystem system) {
        saveUsers(system.getUsers());
        saveFlights(system.getFlights());
        saveBookings(system.getBookings());
        savePayments(system.getPayments());
    }

    // Load all system data
    public static void loadAllData(BookingSystem system) {
    try {
        
        List<User> originalUsers = new ArrayList<>(system.getUsers());
        List<Flight> originalFlights = new ArrayList<>(system.getFlights());
        List<Booking> originalBookings = new ArrayList<>(system.getBookings());
        List<Payment> originalPayments = new ArrayList<>(system.getPayments());

        
        system.setUsers(loadUsers());
        system.setFlights(loadFlights());
        system.setBookings(loadBookings());
        system.setPayments(loadPayments());
        
        
        if (system.getUsers().isEmpty() && !originalUsers.isEmpty()) {
            system.setUsers((ArrayList<User>) originalUsers);
            throw new IOException("Failed to load users - keeping existing data");
        }
        
    } catch (Exception e) {
        System.err.println("Error loading data: " + e.getMessage());
       
    }
}

    // Save users to file
    public static void saveUsers(ArrayList<User> users) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
        for (User user : users) {
            StringBuilder sb = new StringBuilder();
            sb.append(user.getUserId()).append("||");
            sb.append(user.getUsername()).append("||");
            sb.append(user.getPassword()).append("||");
            sb.append(user.getName()).append("||");
            sb.append(user.getEmail()).append("||");
            sb.append(user.getContactInfo()).append("||");
            
            if (user instanceof Customer) {
                sb.append("Customer||").append(((Customer)user).getAddress());
            } 
            else if (user instanceof Agent) {
                sb.append("Agent||").append(((Agent)user).getDepartment());
            }
            else if (user instanceof Administrator) {
                sb.append("Admin||").append(((Administrator)user).getSecurityLevel());
            }
            
            writer.println(sb.toString());
        }
    } catch (IOException e) {
        System.err.println("Error saving users: " + e.getMessage());
    }
}

    // Load users from file
    public static ArrayList<User> loadUsers() {
    ArrayList<User> users = new ArrayList<>();
    
    try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split("\\|\\|", -1); 
                
                if (parts.length >= 8) {
                    int userId = Integer.parseInt(parts[0]);
                    String username = parts[1];
                    String password = parts[2];
                    String name = parts[3];
                    String email = parts[4];
                    String contactInfo = parts[5];
                    String type = parts[6];
                    String additionalInfo = parts[7];
                    
                    switch (type) {
                        case "Customer":
                            users.add(new Customer(userId, username, password, name, 
                                               email, contactInfo, additionalInfo));
                            break;
                        case "Agent":
                            users.add(new Agent(userId, username, password, name, 
                                            email, contactInfo, additionalInfo));
                            break;
                        case "Admin":
                            int securityLevel = Integer.parseInt(additionalInfo);
                            users.add(new Administrator(userId, username, password, 
                                                    name, email, contactInfo, securityLevel));
                            break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing user line: " + line);
                e.printStackTrace();
            }
        }
    } catch (IOException e) {
        System.err.println("Error loading users: " + e.getMessage());
    }
    
    return users;
}

    // Save flights to file
    public static void saveFlights(ArrayList<Flight> flights) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FLIGHTS_FILE, false))) {
            for (Flight flight : flights) {
                String flightType = flight instanceof InternationalFlight ? "International" : "Domestic";
                
                writer.println(String.join(",",
                    flight.getFlightNumber(),
                    flight.getAirline(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    flight.getDepartureTime().format(formatter),
                    flight.getArrivalTime().format(formatter),
                    String.valueOf(flight.getEconomySeats()),
                    String.valueOf(flight.getBusinessSeats()),
                    String.valueOf(flight.getFirstSeats()),
                    String.valueOf(flight.getEconomyPrice()),
                    String.valueOf(flight.getBusinessPrice()),
                    String.valueOf(flight.getFirstPrice()),
                    flightType
                ));
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving flights: " + e.getMessage());
        }
    }

    // Load flights from file
    public static ArrayList<Flight> loadFlights() {
        ArrayList<Flight> flights = new ArrayList<>();
        
        File file = new File(FLIGHTS_FILE);
        if (!file.exists()) {
            return flights;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(FLIGHTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 13) {
                    String flightNumber = parts[0];
                    String airline = parts[1];
                    String origin = parts[2];
                    String destination = parts[3];
                    LocalDateTime departureTime = LocalDateTime.parse(parts[4], formatter);
                    LocalDateTime arrivalTime = LocalDateTime.parse(parts[5], formatter);
                    int economySeats = Integer.parseInt(parts[6]);
                    int businessSeats = Integer.parseInt(parts[7]);
                    int firstSeats = Integer.parseInt(parts[8]);
                    double economyPrice = Double.parseDouble(parts[9]);
                    double businessPrice = Double.parseDouble(parts[10]);
                    double firstPrice = Double.parseDouble(parts[11]);
                    String flightType = parts[12];
                    
                    Flight flight;
                    if (flightType.equals("International")) {
                        flight = new InternationalFlight(flightNumber, airline, origin, destination,
                                                       departureTime, arrivalTime,
                                                       economyPrice, businessPrice, firstPrice);
                    } else {
                        flight = new DomesticFlight(flightNumber, airline, origin, destination,
                                                  departureTime, arrivalTime,
                                                  economyPrice, businessPrice, firstPrice);
                    }
                    
                    // Set actual seat availability
                    for (int i = 0; i < 50 - economySeats; i++) flight.reserveSeat("economy", 1);
                    for (int i = 0; i < 20 - businessSeats; i++) flight.reserveSeat("business", 1);
                    for (int i = 0; i < 10 - firstSeats; i++) flight.reserveSeat("first", 1);
                    
                    flights.add(flight);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading flights: " + e.getMessage());
        }
        
        return flights;
    }

    // Save bookings to file
    public static void saveBookings(ArrayList<Booking> bookings) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, false))) {
            for (Booking booking : bookings) {
                writer.println(String.join(",",
                    booking.getBookingReference(),
                    String.valueOf(booking.getUser().getUserId()),
                    booking.getFlight().getFlightNumber(),
                    booking.getSeatClass(),
                    String.valueOf(booking.getNumberOfSeats()),
                    booking.getStatus(),
                    booking.getPaymentStatus()
                ));
            }
            writer.flush();
            
            // Save passengers separately (clear old data first)
            savePassengers(bookings);
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    // Load bookings from file
    public static ArrayList<Booking> loadBookings() {
        ArrayList<Booking> bookings = new ArrayList<>();
        BookingSystem system = BookingSystem.getInstance();
        
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) {
            return bookings;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String bookingRef = parts[0];
                    int userId = Integer.parseInt(parts[1]);
                    String flightNumber = parts[2];
                    String seatClass = parts[3];
                    int numberOfSeats = Integer.parseInt(parts[4]);
                    String status = parts[5];
                    String paymentStatus = parts[6];
                    
                    // Find user and flight
                    User user = findUserById(userId, system.getUsers());
                    Flight flight = findFlightByNumber(flightNumber, system.getFlights());
                    
                    if (user != null && flight != null) {
                        // Load passengers for this booking
                        ArrayList<Passenger> passengers = loadPassengersForBooking(bookingRef);
                        
                        Booking booking = new Booking(bookingRef, user, flight, passengers, seatClass, status);
                        booking.setPaymentStatus(paymentStatus);
                        
                        bookings.add(booking);
                        
                        // Add to user's booking history if user is a customer
                        if (user instanceof Customer) {
                            ((Customer) user).viewBookings().add(booking);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
        
        return bookings;
    }

    // Save passengers to file
    private static void savePassengers(ArrayList<Booking> bookings) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PASSENGERS_FILE, false))) {
            for (Booking booking : bookings) {
                for (Passenger passenger : booking.getPassengers()) {
                    writer.println(String.join(",",
                        String.valueOf(passenger.getPassengerId()),
                        passenger.getName(),
                        passenger.getPassportNumber(),
                        passenger.getDateOfBirth(),
                        passenger.getSpecialRequests(),
                        booking.getBookingReference()
                    ));
                }
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving passengers: " + e.getMessage());
        }
    }

    // Load passengers for a specific booking
    private static ArrayList<Passenger> loadPassengersForBooking(String bookingRef) {
        ArrayList<Passenger> passengers = new ArrayList<>();
        
        File file = new File(PASSENGERS_FILE);
        if (!file.exists()) {
            return passengers;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(PASSENGERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[5].equals(bookingRef)) {
                    int passengerId = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String passportNumber = parts[2];
                    String dateOfBirth = parts[3];
                    String specialRequests = parts.length > 4 ? parts[4] : "";
                    
                    Passenger passenger = new Passenger(passengerId, name, passportNumber, dateOfBirth);
                    if (!specialRequests.isEmpty()) {
                        passenger.addSpecialRequest(specialRequests);
                    }
                    
                    passengers.add(passenger);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading passengers: " + e.getMessage());
        }
        
        return passengers;
    }

    // Save payments to file
    public static void savePayments(ArrayList<Payment> payments) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PAYMENTS_FILE, false))) {
            for (Payment payment : payments) {
                writer.println(String.join(",",
                    payment.getPaymentId(),
                    payment.getBookingReference(),
                    String.valueOf(payment.getAmount()),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getTransactionDate().format(formatter)
                ));
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving payments: " + e.getMessage());
        }
    }

    // Load payments from file
    public static ArrayList<Payment> loadPayments() {
        ArrayList<Payment> payments = new ArrayList<>();
        
        File file = new File(PAYMENTS_FILE);
        if (!file.exists()) {
            return payments;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    Payment payment = new Payment(
                        parts[0], // paymentId
                        parts[1], // bookingReference
                        Double.parseDouble(parts[2]), // amount
                        parts[3]  // paymentMethod
                    );
                    payment.setStatus(parts[4]); // status
                    payment.setTransactionDate(LocalDateTime.parse(parts[5], formatter));
                    payments.add(payment);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading payments: " + e.getMessage());
        }
        
        return payments;
    }

    // Helper methods
    private static User findUserById(int userId, ArrayList<User> users) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    private static Flight findFlightByNumber(String flightNumber, ArrayList<Flight> flights) {
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equals(flightNumber)) {
                return flight;
            }
        }
        return null;
    }
    
}
import java.time.LocalDateTime;

public abstract class Flight {
    // Attributes for flight details
    private String flightNumber; 
    private String airline;      
    private String origin;       
    private String destination;  
    private LocalDateTime departureTime; 
    private LocalDateTime arrivalTime;   
    // Seats available for each class
    private int economySeats;
    private int businessSeats;
    private int firstSeats;
    // Prices for each class
    private double economyPrice;
    private double businessPrice;
    private double firstPrice;

    public Flight(String flightNumber, String airline, String origin, String destination,
                 LocalDateTime departureTime, LocalDateTime arrivalTime,
                 double economyPrice, double businessPrice, double firstPrice) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.economySeats = 50;
        this.businessSeats = 20;
        this.firstSeats = 10;
        this.economyPrice = economyPrice;
        this.businessPrice = businessPrice;
        this.firstPrice = firstPrice;
    }

    // Abstract method for calculating price (to be implemented by subclasses)
    public abstract double calculatePrice(String seatClass, int numberOfSeats);

    // Check seat availability
    public boolean checkAvailability(String seatClass, int requestedSeats) {
        switch (seatClass.toLowerCase()) {
            case "economy":
                return economySeats >= requestedSeats;
            case "business":
                return businessSeats >= requestedSeats;
            case "first":
                return firstSeats >= requestedSeats;
            default:
                throw new IllegalArgumentException("Invalid seat class: " + seatClass);
        }
    }

    // Reserve seats
    public boolean reserveSeat(String seatClass, int numberOfSeats) {
        if (!checkAvailability(seatClass, numberOfSeats)) {
            return false;
        }
        
        switch (seatClass.toLowerCase()) {
            case "economy":
                economySeats -= numberOfSeats;
                break;
            case "business":
                businessSeats -= numberOfSeats;
                break;
            case "first":
                firstSeats -= numberOfSeats;
                break;
        }
        return true;
    }

    // Update flight schedule
    public void updateSchedule(LocalDateTime newDepartureTime, LocalDateTime newArrivalTime) {
        this.departureTime = newDepartureTime;
        this.arrivalTime = newArrivalTime;
    }

    // Getters
    public String getFlightNumber() { return flightNumber; }
    public String getAirline() { return airline; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getEconomySeats() { return economySeats; }
    public int getBusinessSeats() { return businessSeats; }
    public int getFirstSeats() { return firstSeats; }
    public double getEconomyPrice() { return economyPrice; }
    public double getBusinessPrice() { return businessPrice; }
    public double getFirstPrice() { return firstPrice; }
}

class DomesticFlight extends Flight {
    private static final double DOMESTIC_TAX = 0.05; // 5% tax for domestic flights

    public DomesticFlight(String flightNumber, String airline, String origin, String destination,
                         LocalDateTime departureTime, LocalDateTime arrivalTime,
                         double economyPrice, double businessPrice, double firstPrice) {
        super(flightNumber, airline, origin, destination, departureTime, arrivalTime,
              economyPrice, businessPrice, firstPrice);
    }

    @Override
    public double calculatePrice(String seatClass, int numberOfSeats) {
        double basePrice;
        switch (seatClass.toLowerCase()) {
            case "economy":
                basePrice = getEconomyPrice() * numberOfSeats;
                break;
            case "business":
                basePrice = getBusinessPrice() * numberOfSeats;
                break;
            case "first":
                basePrice = getFirstPrice() * numberOfSeats;
                break;
            default:
                throw new IllegalArgumentException("Invalid seat class: " + seatClass);
        }
        return basePrice * (1 + DOMESTIC_TAX);
    }
}

class InternationalFlight extends Flight {
    private static final double INTERNATIONAL_TAX = 0.10; // 10% tax for international flights
    private static final double CUSTOMS_FEE = 50.0; // Customs fee per passenger

    public InternationalFlight(String flightNumber, String airline, String origin, String destination,
                              LocalDateTime departureTime, LocalDateTime arrivalTime,
                              double economyPrice, double businessPrice, double firstPrice) {
        super(flightNumber, airline, origin, destination, departureTime, arrivalTime,
              economyPrice, businessPrice, firstPrice);
    }

    @Override
    public double calculatePrice(String seatClass, int numberOfSeats) {
        double basePrice;
        switch (seatClass.toLowerCase()) {
            case "economy":
                basePrice = getEconomyPrice() * numberOfSeats;
                break;
            case "business":
                basePrice = getBusinessPrice() * numberOfSeats;
                break;
            case "first":
                basePrice = getFirstPrice() * numberOfSeats;
                break;
            default:
                throw new IllegalArgumentException("Invalid seat class: " + seatClass);
        }
        return (basePrice * (1 + INTERNATIONAL_TAX)) + (CUSTOMS_FEE * numberOfSeats);
    }
}
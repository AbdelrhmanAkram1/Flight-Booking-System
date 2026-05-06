import java.util.ArrayList;
import java.time.LocalDateTime;
public class App1 {
    public static void main(String[] args) {
       
        User customer = new Customer(1, "cust1", "pass123", "Ali", "ali@example.com", "0123456789", "Cairo");
        User agent = new Agent(2, "agent1", "pass456", "Mona", "mona@example.com", "0109876543", "Sales");
        User admin = new Administrator(3, "admin1", "pass789", "Ahmed", "ahmed@example.com", "0112233445", 5);

        // polymerphism
        Flight flight = new DomesticFlight("F100", "EgyptAir", "CAI", "JFK", 
                                         LocalDateTime.now(), LocalDateTime.now().plusHours(10),
                                         300.0, 600.0, 1000.0);
        ArrayList<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger(1, "Ali", "P123456", "1990-01-01"));
        Booking booking = new Booking("BK-123", customer, flight, passengers, "Economy", "Confirmed");

        // polymerphism
        System.out.println("===== Customer Ticket =====");
        System.out.println(customer.generateTicket(booking));

        System.out.println("\n===== Agent Ticket =====");
        System.out.println(agent.generateTicket(booking));

        System.out.println("\n===== Admin Ticket =====");
        System.out.println(admin.generateTicket(booking));

       
        System.out.println("\n===== Tickets from User Array =====");
        User[] users = {customer, agent, admin};
        for (User user : users) {
            System.out.println(user.generateTicket(booking));
            System.out.println("------------------------");
        }
     Flight domesticFlight = new DomesticFlight(
            "F100", "EgyptAir", "CAI", "LXR",
            LocalDateTime.now(), LocalDateTime.now().plusHours(3),
            100.0, 200.0, 300.0
        );

        Flight internationalFlight = new InternationalFlight(
            "F200", "EgyptAir", "CAI", "JFK",
            LocalDateTime.now(), LocalDateTime.now().plusHours(10),
            300.0, 600.0, 1000.0
        );

        //polymerphism
        System.out.println("===== Domestic Flight Price =====");
        printFlightPrice(domesticFlight, "Economy", 2);

        System.out.println("\n===== International Flight Price =====");
        printFlightPrice(internationalFlight, "Business", 1); 
    }

    
    public static void printFlightPrice(Flight flight, String seatClass, int seats) {
        double price = flight.calculatePrice(seatClass, seats);
        System.out.printf("Flight: %s (%s → %s)\n", 
            flight.getFlightNumber(), flight.getOrigin(), flight.getDestination());
        System.out.printf("Seat Class: %s\n", seatClass);
        System.out.printf("Number of Seats: %d\n", seats);
        System.out.printf("Total Price: $%.2f\n", price);
    }
}
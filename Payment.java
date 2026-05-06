import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {
    private String paymentId;
    private String bookingReference;
    private double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime transactionDate;

    public Payment(String paymentId, String bookingReference, double amount, 
                 String paymentMethod) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (bookingReference == null || bookingReference.isEmpty()) {
            throw new IllegalArgumentException("Booking reference cannot be empty");
        }
        
        this.paymentId = paymentId;
        this.bookingReference = bookingReference;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = "Pending";
        this.transactionDate = LocalDateTime.now();
    }

    public boolean processPayment() {
        if (!status.equals("Pending")) {
            throw new IllegalStateException("Payment can only be processed from Pending status");
        }
        
        // Simulate payment processing
        boolean success = Math.random() > 0.1; // 90% success rate for simulation
        
        if (success) {
            this.status = "Completed";
            return true;
        } else {
            this.status = "Failed";
            return false;
        }
    }

    public void refundPayment() {
        if (!status.equals("Completed")) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }
        this.status = "Refunded";
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public String getBookingReference() { return bookingReference; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public LocalDateTime getTransactionDate() { return transactionDate; }

    // Setters with validation
    public void setStatus(String status) {
        if (!status.equals("Pending") && !status.equals("Completed") && 
            !status.equals("Failed") && !status.equals("Refunded")) {
            throw new IllegalArgumentException("Invalid payment status");
        }
        this.status = status;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        if (transactionDate == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return String.format("Payment[ID: %s, Booking: %s, Amount: %.2f, Method: %s, Status: %s, Date: %s]",
                           paymentId, bookingReference, amount, paymentMethod, status, 
                           transactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
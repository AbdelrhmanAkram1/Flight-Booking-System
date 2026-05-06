import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        BookingSystem system = BookingSystem.getInstance();
        FileManager.loadAllData(system);
        SwingUtilities.invokeLater(() -> new LoginWindow(system).setVisible(true));
    }
}

class LoginWindow extends JFrame {
    private BookingSystem system;

    public LoginWindow(BookingSystem system) {
        this.system = system;
        initializeUI();

    }

    private void initializeUI() {
    setTitle("Flight Booking System - Login");
    setSize(400, 300);  // Increased height for better spacing
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());
    getContentPane().setBackground(new Color(240, 240, 240));  // Light gray background

    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    panel.setBackground(new Color(240, 240, 240));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Username components
    JLabel userLabel = new JLabel("Username:");
    userLabel.setFont(new Font("Arial", Font.BOLD, 12));
    JTextField userText = new JTextField(15);
    userText.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    // Password components
    JLabel passLabel = new JLabel("Password:");
    passLabel.setFont(new Font("Arial", Font.BOLD, 12));
    JPasswordField passText = new JPasswordField(15);
    passText.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    // Show password checkbox
    JCheckBox showPassword = new JCheckBox("Show password");
    showPassword.setFont(new Font("Arial", Font.PLAIN, 11));
    showPassword.setBackground(new Color(240, 240, 240));
    showPassword.addActionListener(e -> {
        passText.setEchoChar(showPassword.isSelected() ? (char)0 : '•');
    });

    // Login button
    JButton loginButton = new JButton("Login");
    loginButton.setFont(new Font("Arial", Font.BOLD, 12));
    loginButton.setBackground(new Color(70, 130, 180));  // Steel blue
    loginButton.setForeground(Color.WHITE);
    loginButton.setFocusPainted(false);
    loginButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

    // Message label
    JLabel messageLabel = new JLabel("", JLabel.CENTER);
    messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    messageLabel.setForeground(Color.RED);
    messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    // Layout components
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(userLabel, gbc);

    gbc.gridx = 1;
    panel.add(userText, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(passLabel, gbc);

    gbc.gridx = 1;
    panel.add(passText, gbc);

    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.LINE_START;
    panel.add(showPassword, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(loginButton, gbc);

    gbc.gridy = 4;
    panel.add(messageLabel, gbc);

    // Login action
    loginButton.addActionListener(e -> {
        String username = userText.getText().trim();
        String password = new String(passText.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password!");
            return;
        }

        try {
            User user = authenticate(username, password);
            if (user != null) {
                messageLabel.setForeground(new Color(0, 150, 0));  // Green for success
                messageLabel.setText("Login successful! Loading...");
                
                // Add slight delay before opening dashboard
                Timer timer = new Timer(800, ev -> {
                    dispose();
                    openDashboard(user);
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Invalid username or password!");
                passText.setText("");
                passText.requestFocus();
            }
        } catch (Exception ex) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("System error. Please try again.");
            System.err.println("Login error: " + ex.getMessage());
        }
    });

    // Add enter key listener
    passText.addActionListener(e -> loginButton.doClick());

    add(panel, BorderLayout.CENTER);
}

    private User authenticate(String username, String password) {
        for (User user : system.getUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private void openDashboard(User user) {
        if (user instanceof Customer) {
            new CustomerDashboard((Customer) user, system).setVisible(true);
        } else if (user instanceof Agent) {
            new AgentDashboard((Agent) user, system).setVisible(true);
        } else if (user instanceof Administrator) {
            new AdminDashboard((Administrator) user, system).setVisible(true);
        }
    }
}

class CustomerDashboard extends JFrame {
    private Customer customer;
    private BookingSystem system;

    public CustomerDashboard(Customer customer, BookingSystem system) {
        this.customer = customer;
        this.system = system;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Customer Dashboard - " + customer.getName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Book Flights", createBookPanel());
        tabbedPane.addTab("My Bookings", createBookingsPanel());
        tabbedPane.addTab("Payments", createPaymentsPanel());
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            customer.logout();
            dispose();
            new LoginWindow(system).setVisible(true);
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createBookPanel() {
        JPanel bookPanel = new JPanel(new BorderLayout(10, 10));
        bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fromLabel = new JLabel("From :");
        JTextField fromField = new JTextField("", 10);
        JLabel toLabel = new JLabel("To :");
        JTextField toField = new JTextField("", 10);
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE), 10);
        JButton searchButton = new JButton("Search Flights");

        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(fromLabel, gbc);
        gbc.gridx = 1;
        searchPanel.add(fromField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        searchPanel.add(toLabel, gbc);
        gbc.gridx = 1;
        searchPanel.add(toField, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        searchPanel.add(dateLabel, gbc);
        gbc.gridx = 1;
        searchPanel.add(dateField, gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        searchPanel.add(searchButton, gbc);

        // Flight Results
        DefaultTableModel flightModel = new DefaultTableModel(
            new Object[]{"Flight #", "Airline", "Departure", "Arrival", "Economy", "Business", "First"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable flightTable = new JTable(flightModel);
        JScrollPane flightScrollPane = new JScrollPane(flightTable);

        // Booking Panel
        JPanel bookingPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JComboBox<String> seatClassCombo = new JComboBox<>(new String[]{"Economy", "Business", "First"});
        JButton bookButton = new JButton("Book Selected Flight");

        bookingPanel.add(seatClassCombo);
        bookingPanel.add(bookButton);

        searchButton.addActionListener(e -> {
            flightModel.setRowCount(0);
            try {
                if (fromField.getText().trim().isEmpty() || toField.getText().trim().isEmpty() || 
                    dateField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all search fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                ArrayList<Flight> flights = system.searchFlights(
                    fromField.getText().trim(),
                    toField.getText().trim(),
                    LocalDateTime.parse(dateField.getText().trim() + "T00:00:00")
                );
                
                if (flights.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No flights found", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
                
                for (Flight flight : flights) {
                    flightModel.addRow(new Object[]{
                        flight.getFlightNumber(),
                        flight.getAirline(),
                        flight.getDepartureTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        flight.getArrivalTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        flight.getEconomySeats() + " seats ($" + flight.getEconomyPrice() + ")",
                        flight.getBusinessSeats() + " seats ($" + flight.getBusinessPrice() + ")",
                        flight.getFirstSeats() + " seats ($" + flight.getFirstPrice() + ")"
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bookButton.addActionListener(e -> {
            int selectedRow = flightTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a flight first", "No Flight Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String flightNumber = (String) flightModel.getValueAt(selectedRow, 0);
            Flight flight = findFlightByNumber(flightNumber);
            if (flight == null) {
                JOptionPane.showMessageDialog(this, "Flight not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String seatClass = (String) seatClassCombo.getSelectedItem();
            ArrayList<Passenger> passengers = new ArrayList<>();
            passengers.add(new Passenger(1, customer.getName(), "P" + customer.getUserId(), "1980-01-01"));

            try {
                Booking booking = customer.createBooking(flight, passengers, seatClass);
                
                // Show payment dialog
                PaymentDialog paymentDialog = new PaymentDialog(this, booking);
                paymentDialog.setVisible(true);
                
                if (paymentDialog.isPaymentSuccessful()) {
                    FileManager.saveAllData(system); // Save changes immediately
                    JOptionPane.showMessageDialog(this, 
                        "Booking and payment successful!\nReference: " + booking.getBookingReference(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bookPanel.add(searchPanel, BorderLayout.NORTH);
        bookPanel.add(flightScrollPane, BorderLayout.CENTER);
        bookPanel.add(bookingPanel, BorderLayout.SOUTH);

        return bookPanel;
    }

    private JPanel createBookingsPanel() {
        JPanel bookingsPanel = new JPanel(new BorderLayout(10, 10));
        bookingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel bookingsModel = new DefaultTableModel(
            new Object[]{"Reference", "Flight", "Status", "Seat Class", "Price", "Payment Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookingsTable = new JTable(bookingsModel);
        JScrollPane bookingsScrollPane = new JScrollPane(bookingsTable);

        JButton cancelButton = new JButton("Cancel Booking");
        JButton refreshButton = new JButton("Refresh List");
        

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelButton);

        refreshButton.addActionListener(e -> refreshBookings(bookingsModel));
        cancelButton.addActionListener(e -> {
            int selectedRow = bookingsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking first", "No Booking Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String bookingRef = (String) bookingsModel.getValueAt(selectedRow, 0);
            Booking booking = findBookingByReference(bookingRef);
            if (booking != null) {
                if (booking.cancelBooking()) {
                    FileManager.saveAllData(system); // Save changes immediately
                    JOptionPane.showMessageDialog(this, "Booking cancelled", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshBookings(bookingsModel);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel booking", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshBookings(bookingsModel);
        bookingsPanel.add(bookingsScrollPane, BorderLayout.CENTER);
        bookingsPanel.add(buttonPanel, BorderLayout.SOUTH);
        JButton generateTicketButton = new JButton("Generate Ticket");
    buttonPanel.add(generateTicketButton);
    
    generateTicketButton.addActionListener(e -> {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String bookingRef = (String) bookingsModel.getValueAt(selectedRow, 0);
        Booking booking = findBookingByReference(bookingRef);
        if (booking != null) {
            
            String ticket = booking.getUser().generateTicket(booking);
            
            
            JTextArea ticketArea = new JTextArea(ticket);
            ticketArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(ticketArea);
            JOptionPane.showMessageDialog(this, scrollPane, "E-Ticket", JOptionPane.INFORMATION_MESSAGE);
        }
    });
        return bookingsPanel;
        
    }

    private JPanel createPaymentsPanel() {
        JPanel paymentsPanel = new JPanel(new BorderLayout(10, 10));
        paymentsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel paymentsModel = new DefaultTableModel(
            new Object[]{"Payment ID", "Booking Ref", "Amount", "Method", "Status", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable paymentsTable = new JTable(paymentsModel);
        JScrollPane paymentsScrollPane = new JScrollPane(paymentsTable);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshPayments(paymentsModel));

        paymentsPanel.add(paymentsScrollPane, BorderLayout.CENTER);
        paymentsPanel.add(refreshButton, BorderLayout.SOUTH);

        refreshPayments(paymentsModel);
        return paymentsPanel;
    }

    private void refreshBookings(DefaultTableModel model) {
        model.setRowCount(0);
        for (Booking booking : customer.viewBookings()) {
            model.addRow(new Object[]{
                booking.getBookingReference(),
                booking.getFlight().getFlightNumber(),
                booking.getStatus(),
                booking.getSeatClass(),
                String.format("$%.2f", booking.calculateTotalPrice()),
                booking.getPaymentStatus()
            });
        }
    }

    private void refreshPayments(DefaultTableModel model) {
        model.setRowCount(0);
        for (Payment payment : system.getPayments()) {
            // Only show payments for this customer's bookings
            for (Booking booking : customer.viewBookings()) {
                if (booking.getBookingReference().equals(payment.getBookingReference())) {
                    model.addRow(new Object[]{
                        payment.getPaymentId(),
                        payment.getBookingReference(),
                        String.format("$%.2f", payment.getAmount()),
                        payment.getPaymentMethod(),
                        payment.getStatus(),
                        payment.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    });
                    break;
                }
            }
        }
    }

    private Flight findFlightByNumber(String flightNumber) {
        for (Flight flight : system.getFlights()) {
            if (flight.getFlightNumber().equals(flightNumber)) {
                return flight;
            }
        }
        return null;
    }

    private Booking findBookingByReference(String bookingRef) {
        for (Booking booking : system.getBookings()) {
            if (booking.getBookingReference().equals(bookingRef)) {
                return booking;
            }
        }
        return null;
    }
}

class PaymentDialog extends JDialog {
    private boolean paymentSuccessful = false;
    private Booking booking;
    private JLabel statusLabel;

    public PaymentDialog(JFrame parent, Booking booking) {
        super(parent, "Payment Processing", true);
        this.booking = booking;
        initializeUI();
    }

    private void initializeUI() {
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // Status panel at top
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setForeground(new Color(0, 100, 0));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);

        // Payment form in center
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel amountLabel = new JLabel("Amount to Pay:");
        JLabel amountValue = new JLabel(String.format("$%.2f", booking.calculateTotalPrice()));
        
        JLabel methodLabel = new JLabel("Payment Method:");
        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"Credit Card", "Debit Card", "PayPal"});
        
        JLabel cardLabel = new JLabel("Card Number:");
        JTextField cardField = new JTextField();
        
        JLabel expiryLabel = new JLabel("Expiry Date:");
        JTextField expiryField = new JTextField();
        
        JLabel cvvLabel = new JLabel("CVV:");
        JPasswordField cvvField = new JPasswordField();

        formPanel.add(amountLabel);
        formPanel.add(amountValue);
        formPanel.add(methodLabel);
        formPanel.add(methodCombo);
        formPanel.add(cardLabel);
        formPanel.add(cardField);
        formPanel.add(expiryLabel);
        formPanel.add(expiryField);
        formPanel.add(cvvLabel);
        formPanel.add(cvvField);

        add(formPanel, BorderLayout.CENTER);

        // Buttons at bottom
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton payButton = new JButton("Make Payment");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(payButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        payButton.addActionListener(e -> {
            if (cardField.getText().isEmpty() || expiryField.getText().isEmpty() || cvvField.getPassword().length == 0) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please fill all payment details");
                return;
            }

            // Simulate payment processing delay
            payButton.setEnabled(false);
            statusLabel.setForeground(new Color(0, 100, 0));
            statusLabel.setText("Processing payment...");

            // Use a SwingWorker to prevent UI freezing during "processing"
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Simulate network delay
                    Thread.sleep(1500);
                    
                    Payment payment = BookingSystem.getInstance().processPayment(
                        booking,
                        (String) methodCombo.getSelectedItem()
                    );

                    return payment != null && payment.getStatus().equals("Completed");
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            paymentSuccessful = true;
                            booking.confirmBooking();
                            statusLabel.setText("Payment successful! ✔️");
                            
                            // Close dialog after 1 second
                            Timer timer = new Timer(1000, ev -> dispose());
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            statusLabel.setForeground(Color.RED);
                            statusLabel.setText("Payment failed. Please try again.");
                            payButton.setEnabled(true);
                        }
                    } catch (Exception ex) {
                        statusLabel.setForeground(Color.GREEN);
                        statusLabel.setText("payment successful");
                        payButton.setEnabled(true);
                    }
                }
            }.execute();
        });

        cancelButton.addActionListener(e -> dispose());
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }
}
class AgentDashboard extends JFrame {
    private Agent agent;
    private BookingSystem system;

    public AgentDashboard(Agent agent, BookingSystem system) {
        this.agent = agent;
        this.system = system;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Agent Dashboard - " + agent.getName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Flights", createFlightPanel());
        tabbedPane.addTab("Manage Bookings", createBookingPanel());
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            agent.logout();
            dispose();
            new LoginWindow(system).setVisible(true);
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createFlightPanel() {
        JPanel flightPanel = new JPanel(new BorderLayout(10, 10));
        flightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel flightModel = new DefaultTableModel(
            new Object[]{"Flight #", "Airline", "From", "To", "Departure", "Arrival", "Economy Seats", "Business Seats", "First Seats"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable flightTable = new JTable(flightModel);
        JScrollPane flightScrollPane = new JScrollPane(flightTable);

        JPanel flightControlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton addFlightButton = new JButton("Add Flight");
        JButton removeFlightButton = new JButton("Remove Flight");
        JButton refreshFlightsButton = new JButton("Refresh");

        flightControlPanel.add(addFlightButton);
        flightControlPanel.add(removeFlightButton);
        flightControlPanel.add(refreshFlightsButton);

        refreshFlightsButton.addActionListener(e -> refreshFlights(flightModel));
        addFlightButton.addActionListener(e -> showAddFlightDialog());
        removeFlightButton.addActionListener(e -> {
            int selectedRow = flightTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a flight first", "No Flight Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String flightNumber = (String) flightModel.getValueAt(selectedRow, 0);
            if (system.removeFlight(flightNumber)) {
                FileManager.saveAllData(system); // Save changes immediately
                JOptionPane.showMessageDialog(this, "Flight removed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshFlights(flightModel);
            } else {
                JOptionPane.showMessageDialog(this, "Could not remove flight (may have existing bookings)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshFlights(flightModel);
        flightPanel.add(flightScrollPane, BorderLayout.CENTER);
        flightPanel.add(flightControlPanel, BorderLayout.SOUTH);

        return flightPanel;
    }

    private JPanel createBookingPanel() {
        JPanel bookingPanel = new JPanel(new BorderLayout(10, 10));
        bookingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel bookingModel = new DefaultTableModel(
            new Object[]{"Reference", "Customer", "Flight", "Status", "Seat Class"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookingTable = new JTable(bookingModel);
        JScrollPane bookingScrollPane = new JScrollPane(bookingTable);

        JPanel bookingControlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton createBookingButton = new JButton("Create Booking");
        JButton cancelBookingButton = new JButton("Cancel Booking");
        JButton refreshBookingsButton = new JButton("Refresh");

        bookingControlPanel.add(createBookingButton);
        bookingControlPanel.add(cancelBookingButton);
        bookingControlPanel.add(refreshBookingsButton);

        refreshBookingsButton.addActionListener(e -> refreshBookings(bookingModel));
        createBookingButton.addActionListener(e -> showCreateBookingDialog());
        cancelBookingButton.addActionListener(e -> {
            int selectedRow = bookingTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking first", "No Booking Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String bookingRef = (String) bookingModel.getValueAt(selectedRow, 0);
            Booking booking = findBookingByReference(bookingRef);
            if (booking != null) {
                if (booking.cancelBooking()) {
                    FileManager.saveAllData(system); // Save changes immediately
                    JOptionPane.showMessageDialog(this, "Booking cancelled", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshBookings(bookingModel);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel booking", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshBookings(bookingModel);
        bookingPanel.add(bookingScrollPane, BorderLayout.CENTER);
        bookingPanel.add(bookingControlPanel, BorderLayout.SOUTH);

        return bookingPanel;
    }

    private void refreshFlights(DefaultTableModel model) {
        model.setRowCount(0);
        for (Flight flight : system.getFlights()) {
            model.addRow(new Object[]{
                flight.getFlightNumber(),
                flight.getAirline(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getDepartureTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                flight.getArrivalTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                flight.getEconomySeats(),
                flight.getBusinessSeats(),
                flight.getFirstSeats()
            });
        }
    }

    private void refreshBookings(DefaultTableModel model) {
        model.setRowCount(0);
        for (Booking booking : system.getBookings()) {
            model.addRow(new Object[]{
                booking.getBookingReference(),
                booking.getUser().getName(),
                booking.getFlight().getFlightNumber(),
                booking.getStatus(),
                booking.getSeatClass()
            });
        }
    }

    private void showAddFlightDialog() {
        JDialog dialog = new JDialog(this, "Add New Flight", true);
        dialog.setSize(500, 500);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField flightNumberField = new JTextField();
        JTextField airlineField = new JTextField();
        JTextField originField = new JTextField();
        JTextField destinationField = new JTextField();
        JTextField departureField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        JTextField arrivalField = new JTextField(LocalDateTime.now().plusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        JTextField economyPriceField = new JTextField("100");
        JTextField businessPriceField = new JTextField("200");
        JTextField firstPriceField = new JTextField("300");
        JComboBox<String> flightTypeCombo = new JComboBox<>(new String[]{"Domestic", "International"});
        JButton addButton = new JButton("Add Flight");

        dialog.add(new JLabel("Flight Number:"));
        dialog.add(flightNumberField);
        dialog.add(new JLabel("Airline:"));
        dialog.add(airlineField);
        dialog.add(new JLabel("Origin:"));
        dialog.add(originField);
        dialog.add(new JLabel("Destination:"));
        dialog.add(destinationField);
        dialog.add(new JLabel("Departure (yyyy-MM-ddTHH:mm):"));
        dialog.add(departureField);
        dialog.add(new JLabel("Arrival (yyyy-MM-ddTHH:mm):"));
        dialog.add(arrivalField);
        dialog.add(new JLabel("Economy Price:"));
        dialog.add(economyPriceField);
        dialog.add(new JLabel("Business Price:"));
        dialog.add(businessPriceField);
        dialog.add(new JLabel("First Class Price:"));
        dialog.add(firstPriceField);
        dialog.add(new JLabel("Flight Type:"));
        dialog.add(flightTypeCombo);
        dialog.add(new JLabel(""));
        dialog.add(addButton);

        addButton.addActionListener(e -> {
            try {
                // Validate all fields are filled
                if (flightNumberField.getText().isEmpty() || airlineField.getText().isEmpty() ||
                    originField.getText().isEmpty() || destinationField.getText().isEmpty() ||
                    departureField.getText().isEmpty() || arrivalField.getText().isEmpty() ||
                    economyPriceField.getText().isEmpty() || businessPriceField.getText().isEmpty() ||
                    firstPriceField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Flight flight;
                if (flightTypeCombo.getSelectedItem().equals("Domestic")) {
                    flight = new DomesticFlight(
                        flightNumberField.getText(),
                        airlineField.getText(),
                        originField.getText(),
                        destinationField.getText(),
                        LocalDateTime.parse(departureField.getText()),
                        LocalDateTime.parse(arrivalField.getText()),
                        Double.parseDouble(economyPriceField.getText()),
                        Double.parseDouble(businessPriceField.getText()),
                        Double.parseDouble(firstPriceField.getText())
                    );
                } else {
                    flight = new InternationalFlight(
                        flightNumberField.getText(),
                        airlineField.getText(),
                        originField.getText(),
                        destinationField.getText(),
                        LocalDateTime.parse(departureField.getText()),
                        LocalDateTime.parse(arrivalField.getText()),
                        Double.parseDouble(economyPriceField.getText()),
                        Double.parseDouble(businessPriceField.getText()),
                        Double.parseDouble(firstPriceField.getText())
                    );
                }

                if (system.addFlight(flight)) {
                    FileManager.saveAllData(system); // Save changes immediately
                    JOptionPane.showMessageDialog(dialog, "Flight added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Flight number already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void showCreateBookingDialog() {
        JDialog dialog = new JDialog(this, "Create New Booking", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JComboBox<Customer> customerCombo = new JComboBox<>();
        JComboBox<Flight> flightCombo = new JComboBox<>();
        JComboBox<String> seatClassCombo = new JComboBox<>(new String[]{"Economy", "Business", "First"});
        JTextField passengerNameField = new JTextField();
        JTextField passportField = new JTextField();
        
        // Populate customer combo
        for (User user : system.getUsers()) {
            if (user instanceof Customer) {
                customerCombo.addItem((Customer) user);
            }
        }
        
        // Populate flight combo
        for (Flight flight : system.getFlights()) {
            flightCombo.addItem(flight);
        }
        
        formPanel.add(new JLabel("Customer:"));
        formPanel.add(customerCombo);
        formPanel.add(new JLabel("Flight:"));
        formPanel.add(flightCombo);
        formPanel.add(new JLabel("Seat Class:"));
        formPanel.add(seatClassCombo);
        formPanel.add(new JLabel("Passenger Name:"));
        formPanel.add(passengerNameField);
        formPanel.add(new JLabel("Passport Number:"));
        formPanel.add(passportField);
        
        JButton createButton = new JButton("Create Booking");
        createButton.addActionListener(e -> {
            try {
                if (passengerNameField.getText().isEmpty() || passportField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all passenger fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Customer customer = (Customer) customerCombo.getSelectedItem();
                Flight flight = (Flight) flightCombo.getSelectedItem();
                String seatClass = (String) seatClassCombo.getSelectedItem();
                
                ArrayList<Passenger> passengers = new ArrayList<>();
                passengers.add(new Passenger(1, passengerNameField.getText(), passportField.getText(), "2000-01-01"));
                
                Booking booking = system.createBooking(
                    customer,
                    flight,
                    seatClass,
                    passengers.size()
                );
                
                if (booking != null) {
                    FileManager.saveAllData(system); // Save changes immediately
                    JOptionPane.showMessageDialog(dialog, 
                        "Booking created!\nReference: " + booking.getBookingReference(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create booking", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(createButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private Booking findBookingByReference(String bookingRef) {
        for (Booking booking : system.getBookings()) {
            if (booking.getBookingReference().equals(bookingRef)) {
                return booking;
            }
        }
        return null;
    }
}

class AdminDashboard extends JFrame {
    private Administrator admin;
    private BookingSystem system;
    private JTextArea reportArea;

    public AdminDashboard(Administrator admin, BookingSystem system) {
        this.admin = admin;
        this.system = system;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard - " + admin.getName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("User Management", createUserPanel());
        tabbedPane.addTab("System Settings", createSystemPanel());
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            admin.logout();
            dispose();
            new LoginWindow(system).setVisible(true);
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createUserPanel() {
        JPanel userPanel = new JPanel(new BorderLayout(10, 10));
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel userModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Name", "Email", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable userTable = new JTable(userModel);
        JScrollPane userScrollPane = new JScrollPane(userTable);

        JPanel userControlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton addUserButton = new JButton("Add User");
        JButton deleteUserButton = new JButton("Delete User");
        JButton refreshUsersButton = new JButton("Refresh");

        userControlPanel.add(addUserButton);
        userControlPanel.add(deleteUserButton);
        userControlPanel.add(refreshUsersButton);

        refreshUsersButton.addActionListener(e -> refreshUsers(userModel));
        addUserButton.addActionListener(e -> showAddUserDialog());
        deleteUserButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a user first", "No User Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int userId = (int) userModel.getValueAt(selectedRow, 0);
            if (admin.deleteUser(userId)) {
                FileManager.saveAllData(system); // This line ensures the deletion is saved to the .txt file
                JOptionPane.showMessageDialog(this, "User deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUsers(userModel);
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete user", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshUsers(userModel);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        userPanel.add(userControlPanel, BorderLayout.SOUTH);

        return userPanel;
    }

    private JPanel createSystemPanel() {
        JPanel systemPanel = new JPanel(new BorderLayout(10, 10));
        systemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        JScrollPane reportScrollPane = new JScrollPane(reportArea);

        JButton saveDataButton = new JButton("Save All Data");
        JButton loadDataButton = new JButton("Load All Data");
        JButton backupButton = new JButton("Create Backup");
        JButton reportButton = new JButton("Generate Report");

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.add(saveDataButton);
        buttonPanel.add(loadDataButton);
        buttonPanel.add(backupButton);
        buttonPanel.add(reportButton);

        saveDataButton.addActionListener(e -> {
            FileManager.saveAllData(system);
            JOptionPane.showMessageDialog(this, "All data saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        loadDataButton.addActionListener(e -> {
            FileManager.loadAllData(system);
            JOptionPane.showMessageDialog(this, "All data loaded successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        backupButton.addActionListener(e -> {
            admin.backupSystemData();
            JOptionPane.showMessageDialog(this, "Backup created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        reportButton.addActionListener(e -> {
            String report = admin.generateSystemReport();
            reportArea.setText(report);
            JOptionPane.showMessageDialog(this, "System report generated", "Report", JOptionPane.INFORMATION_MESSAGE);
        });

        systemPanel.add(new JLabel("System Administration Tools", JLabel.CENTER), BorderLayout.NORTH);
        systemPanel.add(reportScrollPane, BorderLayout.CENTER);
        systemPanel.add(buttonPanel, BorderLayout.SOUTH);

        return systemPanel;
    }
    private boolean isValidPassword(String password) {
        // Password must be at least 6 characters with letters and numbers
        return password.matches("(?=.*[a-zA-Z])(?=.*\\d).{6,}");
    }
    private void refreshUsers(DefaultTableModel model) {
        model.setRowCount(0);
        for (User user : system.getUsers()) {
            String role = user instanceof Administrator ? "Admin" : 
                         user instanceof Agent ? "Agent" : "Customer";
            model.addRow(new Object[]{
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                role
            });
        }
    }

    private void showAddUserDialog() {
    JDialog dialog = new JDialog(this, "Add New User", true);
    dialog.setSize(450, 500);  // Increased size for better layout
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.getContentPane().setBackground(new Color(240, 240, 240));

    // Main form panel
    JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
    formPanel.setBackground(new Color(240, 240, 240));

    // Form components
    JTextField usernameField = createFormTextField();
    JPasswordField passwordField = createFormPasswordField();
    JPasswordField confirmPasswordField = createFormPasswordField();
    JTextField nameField = createFormTextField();
    JTextField emailField = createFormTextField();
    JTextField contactField = createFormTextField();
    JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Customer", "Agent", "Admin"});
    JTextField additionalField = createFormTextField();
    
    // Password visibility toggle
    JCheckBox showPasswordCheck = new JCheckBox("Show passwords");
    showPasswordCheck.addActionListener(e -> {
        char echoChar = showPasswordCheck.isSelected() ? (char)0 : '•';
        passwordField.setEchoChar(echoChar);
        confirmPasswordField.setEchoChar(echoChar);
    });

    // Add components to form
    formPanel.add(new JLabel("Username*:"));
    formPanel.add(usernameField);
    formPanel.add(new JLabel("Password*:"));
    formPanel.add(passwordField);
    formPanel.add(new JLabel("Confirm Password*:"));
    formPanel.add(confirmPasswordField);
    formPanel.add(new JLabel(""));
    formPanel.add(showPasswordCheck);
    formPanel.add(new JLabel("Full Name*:"));
    formPanel.add(nameField);
    formPanel.add(new JLabel("Email*:"));
    formPanel.add(emailField);
    formPanel.add(new JLabel("Contact Info*:"));
    formPanel.add(contactField);
    formPanel.add(new JLabel("Role:"));
    formPanel.add(roleCombo);
    formPanel.add(new JLabel("Additional Info:"));
    formPanel.add(additionalField);

    // Status label
    JLabel statusLabel = new JLabel("", JLabel.CENTER);
    statusLabel.setForeground(Color.RED);
    statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    buttonPanel.setBackground(new Color(240, 240, 240));
    
    JButton addButton = new JButton("Add User");
    addButton.setBackground(new Color(70, 130, 180));
    addButton.setForeground(Color.WHITE);
    addButton.setFocusPainted(false);
    addButton.setPreferredSize(new Dimension(120, 30));
    
    addButton.addActionListener(e -> {
        try {
            // Validate required fields
            if (usernameField.getText().trim().isEmpty() || 
                nameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                contactField.getText().trim().isEmpty()) {
                statusLabel.setText("Please fill all required fields (*)");
                return;
            }

            // Validate password
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (password.isEmpty()) {
                statusLabel.setText("Password cannot be empty");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                statusLabel.setText("Passwords do not match");
                return;
            }
            
            if (!isValidPassword(password)) {
                statusLabel.setText("Password must be at least 6 characters contains numbers and letters");
                return;
            }
            
            // Validate email format
            if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                statusLabel.setText("Please enter a valid email address");
                return;
            }

            // Create user
            User newUser = admin.createUser(
                (String) roleCombo.getSelectedItem(),
                usernameField.getText().trim(),
                password,
                nameField.getText().trim(),
                emailField.getText().trim(),
                contactField.getText().trim(),
                additionalField.getText().trim()
            );

            if (newUser != null) {
                FileManager.saveAllData(system);
                statusLabel.setForeground(new Color(0, 150, 0));
                statusLabel.setText("User added successfully!");
                
                // Close dialog after 1 second
                Timer timer = new Timer(1000, ev -> dialog.dispose());
                timer.setRepeats(false);
                timer.start();
            }
        } catch (Exception ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: " + ex.getMessage());
        }
    });

    buttonPanel.add(addButton);
    
    // Add components to dialog
    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(statusLabel, BorderLayout.NORTH);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    
    dialog.setVisible(true);
}

private JTextField createFormTextField() {
    JTextField field = new JTextField();
    field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    return field;
}

private JPasswordField createFormPasswordField() {
    JPasswordField field = new JPasswordField();
    field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    return field;
}
}
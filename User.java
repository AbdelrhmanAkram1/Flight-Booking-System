abstract class User {
    protected int userId;
    protected String username;
    protected String password;
    protected String name;
    protected String email;
    protected String contactInfo;

    public User(int userId, String username, String password, String name, String email, String contactInfo) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters with letters and numbers");
        }
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.contactInfo = contactInfo;
    }

    private boolean isValidPassword(String password) {
        // Password must be at least 6 characters with letters and numbers
        return password.matches("(?=.*[a-zA-Z])(?=.*\\d).{6,}");
    }

    public abstract boolean login(String username, String password);
    public abstract void logout();
    public abstract void updateProfile(String name, String email, String contactInfo);

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getContactInfo() { return contactInfo; }
    public String getPassword(){return password;}
    public abstract String generateTicket(Booking booking);
}
class Passenger {
    private int passengerId;
    private String name;
    private String passportNumber;
    private String dateOfBirth;
    private String specialRequests;

    public Passenger(int passengerId, String name, String passportNumber, String dateOfBirth) {
        this.passengerId = passengerId;
        this.name = name;
        this.passportNumber = passportNumber;
        this.dateOfBirth = dateOfBirth;
        this.specialRequests = "";
    }

    public void updateInfo(String name, String passportNumber, String dateOfBirth) {
        if (name != null && !name.isEmpty()) this.name = name;
        if (passportNumber != null && !passportNumber.isEmpty()) this.passportNumber = passportNumber;
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) this.dateOfBirth = dateOfBirth;
    }

    public String getPassengerDetails() {
        return "Name: " + name + "\nPassport: " + passportNumber + "\nDOB: " + dateOfBirth + 
               "\nSpecial Requests: " + (specialRequests.isEmpty() ? "None" : specialRequests);
    }

    public void addSpecialRequest(String request) {
        if (!specialRequests.isEmpty()) {
            specialRequests += ", ";
        }
        specialRequests += request;
    }

    // Getters
    public int getPassengerId() { return passengerId; }
    public String getName() { return name; }
    public String getPassportNumber() { return passportNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getSpecialRequests() { return specialRequests; }
}
package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.service.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
// import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;
        try{
            userBookingService = new UserBookingService();
        }catch(IOException ex){
            // System.out.println("There is something wrong: " + ex.getMessage());
            // ex.printStackTrace();
            System.out.println("There is something wrong");
            return;
        }
        Train trainSelectedForBooking = null; // extra 
        String selectedSource = null; // extra 
        String selectedDestination = null; // extra 
        while(option!=7){ 
            System.out.println("Choose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");
            option = scanner.nextInt();

            switch (option)
            {
                case 1:
                    System.out.println(" ");
                    System.out.println("Enter the username to signup");
                    String nameToSignUp = scanner.next(); System.out.println(" ");
                    System.out.println("Enter the password to signup");
                    String passwordToSignUp = scanner.next();

                    User userToSignup = new User(nameToSignUp, passwordToSignUp, 
                    UserServiceUtil.hashPassword(passwordToSignUp), 
                    new ArrayList<>(), UUID.randomUUID().toString());

                    userBookingService.signUp(userToSignup);
                    break;
                case 2:
                    System.out.println(" ");
                    System.out.println("Enter the username to Login");
                    String nameToLogin = scanner.next(); System.out.println(" ");
                    System.out.println("Enter the password to Login");
                    String passwordToLogin = scanner.next();
                    
                    // Create a temporary user object for login validation
                    // User userToLogin = new User(nameToLogin, passwordToLogin, "", new ArrayList<>(), "");
                    User userToLogin = new User(nameToLogin, passwordToLogin, 
                    UserServiceUtil.hashPassword(passwordToLogin), 
                    new ArrayList<>(), UUID.randomUUID().toString());
                    
                    try{
                        // Create UserBookingService with the login user for validation
                        UserBookingService loginService = new UserBookingService(userToLogin);
                        
                        // // Validate login credentials
                        Boolean loginSuccessful = loginService.loginUser();
                        
                        if(loginSuccessful) {
                            System.out.println(" ");
                            System.out.println("Login successful! Welcome " + nameToLogin);
                            userBookingService = loginService; // Use this service for the session
                        } else {
                            System.out.println(" ");
                            System.out.println("Login failed! Invalid username or password.");
                        }
                        
                    }catch (IOException ex){
                        System.out.println(" ");
                        System.out.println("Error during login: " + ex.getMessage());
                        return;
                    }
                    break;
                case 3:
                    System.out.println(" ");
                    System.out.println("Fetching your bookings...");
                    userBookingService.fetchBookings();
                    break;
                case 4:
                    System.out.println(" ");
                    System.out.println("Type your source station");
                    String source = scanner.next(); System.out.println(" ");
                    System.out.println("Type your destination station");
                    String dest = scanner.next();
                    
                    // Store the selected source and destination
                    selectedSource = source;
                    selectedDestination = dest;
                    
                    List<Train> trains = userBookingService.getTrains(source, dest);
                    
                    int index = 1;
                    for (Train t: trains)
                    {
                        System.out.println(" ");
                        System.out.println(index+" Train id : "+t.getTrainId());
                        for (Map.Entry<String, String> entry: t.getStationTimes().entrySet())
                        {
                            System.out.println("station "+entry.getKey()+" time: "+entry.getValue());
                        }
                        index++;
                    } System.out.println(" ");
                    System.out.println("Select a train by typing 1,2...");

                    int trainChoice = scanner.nextInt() - 1; // Convert to 0-based index
                    if (trainChoice >= 0 && trainChoice < trains.size()) 
                    {
                        trainSelectedForBooking = trains.get(trainChoice); trainChoice++; System.out.println(" ");
                        System.out.println("You have selected Train "+ trainChoice + ". You may now book a seat on this train.");
                        trainChoice--;
                    } 
                    else 
                    {
                        System.out.println(" ");
                        System.out.println("Invalid train selection. Please try again.");
                    }
                    break;
                case 5:
                    if (trainSelectedForBooking == null) 
                    {
                        System.out.println(" ");
                        System.out.println("Please search and select a train first (option 4)");
                        break;
                    }
                    if (selectedSource == null || selectedDestination == null) 
                    {
                        System.out.println(" ");
                        System.out.println("Please search for trains first to specify source and destination (option 4)");
                        break;
                    }
                    System.out.println(" ");
                    System.out.println("Select a seat out of these seats");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    
                    for (List<Integer> row: seats){
                        for (Integer val: row){
                            System.out.print(val+" ");
                        }
                        System.out.println();
                    }
                    System.out.println(" ");
                    System.out.println("Select the seat by typing the row and column");
                    System.out.println(" ");
                    System.out.println("Enter the row");
                    int row = scanner.nextInt()-1; System.out.println(" ");
                    System.out.println("Enter the column");
                    int col = scanner.nextInt()-1;
                    System.out.println("Booking your seat....");

                    Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col, selectedSource, selectedDestination);
                    if(booked.equals(Boolean.TRUE)){
                        System.out.println("Booked! Enjoy your journey");
                    }else{
                        System.out.println("Sorry, this seat has already been taken.");
                    }
                    break;
                case 6:
                    System.out.println("Enter the ticket id to cancel");
                    String ticketId = scanner.next();
                    Boolean cancelled = userBookingService.cancelBooking(ticketId);
                    if(cancelled.equals(Boolean.TRUE)){
                        System.out.println("Ticket cancelled successfully");
                    }else{
                        System.out.println("Failed to cancel ticket");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

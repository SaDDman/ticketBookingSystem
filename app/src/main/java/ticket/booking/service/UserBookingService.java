package ticket.booking.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserBookingService{
    
    private ObjectMapper objectMapper = new ObjectMapper(); 

    private List<User> userList; // fetched LocalDb file heres 

    private User user;

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        String userFilePath = "src/main/java/ticket/booking/localDB/users.json";
        userList = objectMapper.readValue(new File(userFilePath), new TypeReference<List<User>>() {});
    }

    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 -> { // seq onujayi // filer krtese e/O
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1)
    {
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        String userFilePath = "src/main/java/ticket/booking/localDB/users.json";
        File usersFile = new File(userFilePath);
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBookings()
    {
        if (user == null) {
            System.out.println("Please login first to fetch bookings.");
            return;
        }
        // user.printTickets();
        try {
            // Reload user data from file to get latest bookings
            loadUserListFromFile();
            Optional<User> userFromFile = userList.stream()
                .filter(u -> u.getName().equals(user.getName()))
                .findFirst();
            
            if (userFromFile.isPresent()) {
                User updatedUser = userFromFile.get();
                List<Ticket> tickets = updatedUser.getTicketsBooked();
                
                if (tickets == null || tickets.isEmpty()) {
                    System.out.println("No bookings found.");
                } else {
                    System.out.println("Your bookings:");
                    for (Ticket ticket : tickets) {
                        System.out.println("Ticket ID: " + ticket.getTicketId());
                        System.out.println("From: " + ticket.getSource() + " To: " + ticket.getDestination());
                        System.out.println("Date: " + ticket.getDateOfTravel());
                        System.out.println("Train No: " + ticket.getTrain().getTrainNo());
                        System.out.println("Train ID: " + ticket.getTrain().getTrainId());
                        System.out.println("Seat: Row " + (ticket.getSeatRow() + 1) + ", Column " + (ticket.getSeatColumn() + 1));
                        System.out.println("---");
                    }
                }
                // Update the current user object with the loaded data
                this.user = updatedUser;
            } else {
                System.out.println("User not found in database.");
            }
        } catch (IOException e) {
            System.out.println("Error loading booking data: " + e.getMessage());
        }
    }

    public Boolean cancelBooking(String ticketId)
    {
        if (user == null) {
            System.out.println("Please login first to cancel bookings.");
            return Boolean.FALSE;
        }

        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        try {
            // Reload user data from file to get latest bookings
            loadUserListFromFile();
            Optional<User> userFromFile = userList.stream()
                .filter(u -> u.getName().equals(user.getName()))
                .findFirst();
            
            if (userFromFile.isPresent()) {
                this.user = userFromFile.get(); // Update current user with file data
            } else {
                System.out.println("User not found in database.");
                return Boolean.FALSE;
            }
        } catch (IOException e) {
            System.out.println("Error loading user data: " + e.getMessage());
            return Boolean.FALSE;
        }

        // Find the ticket to get seat information
        Ticket ticketToCancel = null;
        for (Ticket ticket : user.getTicketsBooked()) {
            if (ticket.getTicketId().equals(ticketId)) {
                ticketToCancel = ticket;
                break;
            }
        }
        
        if (ticketToCancel == null) {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }
        
        
        try {
            // Free up the seat in the train
            TrainService trainService = new TrainService();
            Train train = ticketToCancel.getTrain();
            List<List<Integer>> seats = train.getSeats();

            int row = ticketToCancel.getSeatRow();
            int col = ticketToCancel.getSeatColumn();
            
            // Set seat back to available (0)
            seats.get(row).set(col, 0);
            train.setSeats(seats);
            
            // Update train data
            trainService.addTrain(train);
            
            // Remove ticket from user's booking list
            user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(ticketId));
            
            // Update the user in the userList
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getName().equals(user.getName())) {
                    userList.set(i, user);
                    break;
                }
            }
            
            // Save updated user data
            saveUserListToFile();
            row++; col++;
            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            System.out.println("Seat Row " + row + ", Column " + col + " is now available."); 
            row--; col--;
            return Boolean.TRUE;
            
        } catch (IOException ex) {
            System.out.println("Error canceling booking: " + ex.getMessage());
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination)
    {
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
            return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat, String source, String destination) 
    {
        try{
            if (user == null) {
                System.out.println("Please login first to book seats.");
                return Boolean.FALSE;
            }
            
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) 
            {
                if (seats.get(row).get(seat) == 0) 
                {
                    // Mark seat as booked
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    
                    // Create a ticket
                    String ticketId = "TICKET_" + System.currentTimeMillis(); // Generate unique ticket ID
                    // String ticketId = "TICKET_" + UUID.randomUUID().toString(); // Generate unique ticket ID
                    Ticket ticket = new Ticket(ticketId, user.getUserId(), 
                                             source, // actual source selected by user
                                             destination, // actual destination selected by user
                                             new java.util.Date().toString(), // date of travel
                                             train, row, seat);
                    
                    // Add ticket to user's booked tickets
                    user.getTicketsBooked().add(ticket);
                    
                    // Update the user in the userList
                    for (int i = 0; i < userList.size(); i++) 
                    {
                        if (userList.get(i).getName().equals(user.getName())) 
                        {
                            userList.set(i, user);
                            break;
                        }
                    }
                    
                    // Update user data in the file
                    saveUserListToFile();
                    
                    System.out.println("Ticket ID: " + ticketId + " (Save this for cancellation)");
                    return true; // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        }catch (IOException ex){
            ex.printStackTrace();
            return Boolean.FALSE;
        }
    }
}

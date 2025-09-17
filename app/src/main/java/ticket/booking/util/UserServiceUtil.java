package ticket.booking.util;

import org.mindrot.jbcrypt.BCrypt;

public class UserServiceUtil
{
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        // return BCrypt.checkpw(plainPassword, hashedPassword);
        // Check for null or empty values
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Return false if BCrypt fails for any reason
            return false;
        }
    }
}

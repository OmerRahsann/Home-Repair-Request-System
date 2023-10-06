package homerep.springy.util;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class ValidationUtil {

    public static boolean validateEmail(String email) {
        if (email == null) {
            return false;
        }
        try {
            // Try parsing the Email address
            InternetAddress address = new InternetAddress(email, true);
            // Should only contain a single email address without any additional info
            return !address.isGroup() && address.getPersonal() == null;
        } catch (AddressException e) {
            return false;
        }
    }

    public static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}

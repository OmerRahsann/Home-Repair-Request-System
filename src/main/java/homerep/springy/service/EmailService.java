package homerep.springy.service;

import java.util.Map;

public interface EmailService {
    void sendEmail(String emailAddress, String templateName, Map<String, String> templateVariables);
}

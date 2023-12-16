package homerep.springy.service.impl;

import homerep.springy.service.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Map<String, String> emailTemplates;

    @Autowired
    private ResourceLoader resourceLoader;

    private final Session session = Session.getInstance(new Properties());

    @Override
    public void sendEmail(String emailAddress, String templateName, Map<String, String> templateVariables) {
        try {
            String templateLocation = emailTemplates.get(templateName);
            Resource templateResource = resourceLoader.getResource(templateLocation);

            MimeMessage email = new MimeMessage(session, templateResource.getInputStream());
            templateVariables = sanitize(templateVariables);
            fillEmail(email, templateVariables);
            email.setRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
            email.setRecipient(Message.RecipientType.CC, null);
            email.setRecipient(Message.RecipientType.BCC, null);
            mailSender.send(email);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        } catch (MailException e) {
            LOGGER.warn("Failed to send email to " + emailAddress + " with template " + templateName, e);
        }
    }

    private Map<String, String> sanitize(Map<String, String> templateVariables) {
        Map<String, String> result = new HashMap<>();
        templateVariables.forEach((key, value) -> result.put(key, Jsoup.clean(value, Safelist.none())));
        return result;
    }

    private void fillEmail(MimeMessage template, Map<String, String> templateVariables) throws MessagingException, IOException {
        template.setSubject(fillTemplateContent(template.getSubject(), templateVariables));
        if (template.getContent() instanceof MimeMultipart multipartContent) {
            for (int i = 0; i < multipartContent.getCount(); i++) {
                BodyPart part = multipartContent.getBodyPart(i);
                fillPart(part, templateVariables);
            }
        } else if (template.getContent() instanceof String) {
            fillPart(template, templateVariables);
        }
    }

    private void fillPart(Part part, Map<String, String> templateVariables) throws MessagingException, IOException {
        if (!(part.getContent() instanceof String content)) {
            return;
        }
        if (part.getContentType() == null) {
            return;
        }
        ContentType contentType = new ContentType(part.getContentType());
        String baseType = contentType.getBaseType();
        if ("text/html".equals(baseType) || "text/plain".equals(baseType)) {
            part.setContent(fillTemplateContent(content, templateVariables), part.getContentType());
        }
    }

    private String fillTemplateContent(String template, Map<String, String> templateVariables) {
        if (template == null) {
            return null;
        }
        // Simple, but probably not very efficient?
        for (Map.Entry<String, String> entry : templateVariables.entrySet()) {
            String token = "{{" + entry.getKey() + "}}";
            template = template.replace(token, entry.getValue());
        }
        return template;
    }
}

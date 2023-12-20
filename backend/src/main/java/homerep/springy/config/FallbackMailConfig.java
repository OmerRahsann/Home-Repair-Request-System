package homerep.springy.config;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;

@Configuration
public class FallbackMailConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackMailConfig.class);

    @Bean
    @ConditionalOnProperty(prefix = "spring.mail", name = "host", havingValue="\0", matchIfMissing = true)
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender noopMailSender() {
        LOGGER.warn("Missing mail sender configuration. Noop Java mail sender will be used.");
        return new NoopJavaMailSender();
    }

    static class NoopJavaMailSender implements JavaMailSender {

        @Override
        public MimeMessage createMimeMessage() {
            return null;
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            return null;
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {}

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {}

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {}

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {}

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {}

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {}
    }
}

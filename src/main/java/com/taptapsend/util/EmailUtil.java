package com.taptapsend.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailUtil {
    private static final Properties emailProperties = new Properties();

    static {
        try (InputStream input = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find email.properties");
            }
            emailProperties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load email properties", ex);
        }
    }

    public static void sendEmail(String to, String subject, String body) {
        String from = emailProperties.getProperty("mail.from");
        String host = emailProperties.getProperty("mail.host");
        String port = emailProperties.getProperty("mail.port");
        String username = emailProperties.getProperty("mail.username");
        String password = emailProperties.getProperty("mail.password");

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2"); // Ajout recommandé pour la sécurité

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (MessagingException mex) {
            System.err.println("Failed to send email: " + mex.getMessage());
            mex.printStackTrace();
            throw new RuntimeException("Failed to send email", mex);
        }
    }
}
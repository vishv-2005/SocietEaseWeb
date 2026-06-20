package service;

import storage.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Email notification service using JavaMail with SMTP.
 * 
 * Environment variables:
 *   SMTP_HOST     - SMTP server (default: smtp.gmail.com)
 *   SMTP_PORT     - SMTP port (default: 587)
 *   SMTP_USER     - Email address to send from
 *   SMTP_PASSWORD  - App password (for Gmail: generate at myaccount.google.com/apppasswords)
 * 
 * All emails are sent asynchronously in a background thread.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private static final String SMTP_HOST = getEnv("SMTP_HOST", "smtp.gmail.com");
    private static final String SMTP_PORT = getEnv("SMTP_PORT", "587");
    private static final String SMTP_USER = getEnv("SMTP_USER", "");
    private static final String SMTP_PASSWORD = getEnv("SMTP_PASSWORD", "");

    private static boolean isConfigured() {
        return !SMTP_USER.isEmpty() && !SMTP_PASSWORD.isEmpty();
    }

    /**
     * Send an email asynchronously.
     */
    public static void sendAsync(int societyId, String toEmail, String subject, String htmlBody) {
        if (!isConfigured()) {
            LOGGER.warning("Email not configured (SMTP_USER/SMTP_PASSWORD not set). Skipping email to: " + toEmail);
            return;
        }

        new Thread(() -> {
            try {
                send(toEmail, subject, htmlBody);
                logEmail(societyId, toEmail, subject, "SENT");
                LOGGER.info("Email sent to: " + toEmail + " | Subject: " + subject);
            } catch (Exception e) {
                logEmail(societyId, toEmail, subject, "FAILED");
                LOGGER.log(Level.WARNING, "Failed to send email to: " + toEmail, e);
            }
        }).start();
    }

    /**
     * Send an email synchronously.
     */
    private static void send(String toEmail, String subject, String htmlBody) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER, "SocietEase"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    /**
     * Log email to database for tracking.
     */
    private static void logEmail(int societyId, String recipient, String subject, String status) {
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "INSERT INTO email_log (society_id, recipient, subject, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                stmt.setString(2, recipient);
                stmt.setString(3, subject);
                stmt.setString(4, status);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log email", e);
        }
    }

    // ===== Email Templates =====

    public static void sendWelcomeEmail(int societyId, String toEmail, String residentName, String societyName) {
        String subject = "Welcome to " + societyName + " on SocietEase!";
        String body = "<div style='font-family:Segoe UI,sans-serif;max-width:600px;margin:0 auto;'>" +
            "<h2 style='color:#2d3eaf;'>Welcome to SocietEase!</h2>" +
            "<p>Dear " + residentName + ",</p>" +
            "<p>You have been registered as a resident of <strong>" + societyName + "</strong>.</p>" +
            "<p>You can now log in to SocietEase to:</p>" +
            "<ul><li>View society notices</li><li>File complaints</li><li>Pay maintenance fees online</li></ul>" +
            "<p>Best regards,<br>SocietEase Team</p></div>";
        sendAsync(societyId, toEmail, subject, body);
    }

    public static void sendNoticeEmail(int societyId, String toEmail, String title, String content, String societyName) {
        String subject = "New Notice: " + title + " — " + societyName;
        String body = "<div style='font-family:Segoe UI,sans-serif;max-width:600px;margin:0 auto;'>" +
            "<h2 style='color:#2d3eaf;'>New Notice</h2>" +
            "<h3>" + title + "</h3>" +
            "<p>" + content + "</p>" +
            "<p style='color:#666;font-size:12px;'>This notice was issued by " + societyName + " via SocietEase.</p></div>";
        sendAsync(societyId, toEmail, subject, body);
    }

    public static void sendPaymentReceipt(int societyId, String toEmail, String residentName,
                                           String amount, String month, String paymentId) {
        String subject = "Payment Receipt — ₹" + amount + " for " + month;
        String body = "<div style='font-family:Segoe UI,sans-serif;max-width:600px;margin:0 auto;'>" +
            "<h2 style='color:#2d3eaf;'>Payment Received</h2>" +
            "<p>Dear " + residentName + ",</p>" +
            "<p>Your maintenance payment has been received:</p>" +
            "<table style='border-collapse:collapse;width:100%;'>" +
            "<tr><td style='padding:8px;border:1px solid #ddd;'><strong>Amount</strong></td><td style='padding:8px;border:1px solid #ddd;'>₹" + amount + "</td></tr>" +
            "<tr><td style='padding:8px;border:1px solid #ddd;'><strong>Month</strong></td><td style='padding:8px;border:1px solid #ddd;'>" + month + "</td></tr>" +
            "<tr><td style='padding:8px;border:1px solid #ddd;'><strong>Payment ID</strong></td><td style='padding:8px;border:1px solid #ddd;'>" + paymentId + "</td></tr>" +
            "</table><p>Thank you for your timely payment!</p></div>";
        sendAsync(societyId, toEmail, subject, body);
    }

    public static void sendComplaintNotification(int societyId, String rpEmail, String apartmentLabel,
                                                  String description, String societyName) {
        String subject = "New Complaint from Apt " + apartmentLabel + " — " + societyName;
        String body = "<div style='font-family:Segoe UI,sans-serif;max-width:600px;margin:0 auto;'>" +
            "<h2 style='color:#2d3eaf;'>New Complaint Filed</h2>" +
            "<p><strong>Apartment:</strong> " + apartmentLabel + "</p>" +
            "<p><strong>Description:</strong> " + description + "</p>" +
            "<p>Please log in to SocietEase to review and manage this complaint.</p></div>";
        sendAsync(societyId, rpEmail, subject, body);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}

package com.scutelnic.rutex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendPasswordResetEmail(String to, String resetLink) {
        System.out.println("=== SENDING PASSWORD RESET EMAIL ===");
        System.out.println("To: " + to);
        System.out.println("Reset link: " + resetLink);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@rutex.md");
            message.setTo(to);
            message.setSubject("Resetare parolă - Rutex");
            message.setText(
                "Bună!\n\n" +
                "Ați solicitat resetarea parolei pentru contul dvs. Rutex.\n\n" +
                "Pentru a reseta parola, faceți clic pe link-ul de mai jos:\n" +
                resetLink + "\n\n" +
                "Acest link este valabil timp de 1 oră.\n\n" +
                "Dacă nu ați solicitat această resetare, puteți ignora acest email.\n\n" +
                "Cu stimă,\n" +
                "Echipa Rutex"
            );
            
            System.out.println("Sending email...");
            mailSender.send(message);
            System.out.println("Email sent successfully!");
            
        } catch (Exception e) {
            System.err.println("=== EMAIL SENDING ERROR ===");
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw pentru a vedea eroarea în serviciul principal
        }
    }
    
    public void sendEmail(String to, String subject, String content) {
        System.out.println("=== SENDING EMAIL ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Content length: " + content.length());

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("contact@rutex.md");
                helper.setTo(to);
                helper.setSubject(subject);

                String htmlContent = convertToHtml(content);
                helper.setText(content, htmlContent);

                System.out.println("Message created, attempting to send...");
                System.out.println("MailSender: " + mailSender);
                System.out.println("MailSender class: " + mailSender.getClass().getName());
                System.out.println("SMTP send attempt " + attempt + "/" + maxAttempts);

                mailSender.send(message);
                System.out.println("✅ Email sent successfully to " + to);
                return;
            } catch (Exception e) {
                boolean transientNetworkError = isTransientNetworkError(e);
                boolean shouldRetry = transientNetworkError && attempt < maxAttempts;

                System.err.println("❌ EMAIL SENDING ERROR ===");
                System.err.println("Error sending email to " + to + ": " + e.getMessage());
                System.err.println("Error type: " + e.getClass().getSimpleName());
                if (e.getCause() != null) {
                    System.err.println("Root cause: " + e.getCause().getMessage());
                    System.err.println("Root cause type: " + e.getCause().getClass().getSimpleName());
                }

                if (shouldRetry) {
                    System.err.println("Transient SMTP error detected. Retrying...");
                    try {
                        Thread.sleep(1200L * attempt);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Email send retry interrupted", interruptedException);
                    }
                    continue;
                }

                e.printStackTrace();
                throw new RuntimeException("Failed to send email", e);
            }
        }
    }

    @Async
    public void sendEmailAsync(String to, String subject, String content) {
        try {
            sendEmail(to, subject, content);
        } catch (Exception e) {
            System.err.println("❌ Async email failed for " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String convertToHtml(String content) {
        String escaped = HtmlUtils.htmlEscape(content == null ? "" : content, "UTF-8");
        String html = escaped
            .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
            .replace("---", "<hr>")
            .replace("\r\n", "\n")
            .replace("\n", "<br>");
        
        // Wrap in HTML structure
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        line-height: 1.4; 
                        color: #2c3e50; 
                        max-width: 600px; 
                        margin: 0 auto; 
                        padding: 15px;
                        background-color: #f8f9fa;
                    }
                    .email-container {
                        background-color: #ffffff;
                        padding: 25px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 20px;
                        padding-bottom: 15px;
                        border-bottom: 2px solid #3498db;
                    }
                    .header h1 {
                        color: #2c3e50;
                        margin: 0;
                        font-size: 22px;
                        font-weight: 600;
                    }
                    p {
                        margin: 8px 0;
                    }
                    strong { 
                        font-weight: 600; 
                        color: #2c3e50;
                    }
                    .highlight {
                        background-color: #ecf0f1;
                        padding: 12px;
                        border-left: 4px solid #3498db;
                        margin: 15px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        margin-top: 20px;
                        padding-top: 15px;
                        border-top: 1px solid #ecf0f1;
                        text-align: center;
                        color: #7f8c8d;
                        font-size: 13px;
                    }
                    hr { 
                        border: none; 
                        border-top: 1px solid #ecf0f1; 
                        margin: 15px 0; 
                    }
                    .contact-info {
                        background-color: #f8f9fa;
                        padding: 12px;
                        border-radius: 6px;
                        margin: 12px 0;
                    }
                    .contact-info strong {
                        color: #3498db;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    %s
                </div>
            </body>
            </html>
            """, html);
    }

    private boolean isTransientNetworkError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof UnknownHostException
                || current instanceof SocketTimeoutException
                || current instanceof ConnectException) {
                return true;
            }

            String className = current.getClass().getName();
            if (className.contains("MailConnectException")) {
                return true;
            }

            current = current.getCause();
        }
        return false;
    }
}

package com.safereport.service.impl;

import com.safereport.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name:SafeReport}")
    private String fromName;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    // ─── Public Methods ──────────────────────────────────────────────────────

    /**
     * Send a notification email (called for every in-app notification).
     */
    @Async
    public void sendNotificationEmail(User user, String title, String message) {
        if (user == null || user.getEmail() == null) return;
        String html = buildNotificationHtml(title, message, user.getFullName());
        sendEmail(user.getEmail(), "SafeReport: " + title, html);
    }

    /**
     * Welcome email sent after successful registration.
     */
    @Async
    public void sendWelcomeEmail(User user) {
        if (user == null || user.getEmail() == null) return;
        String html = buildWelcomeHtml(user.getFullName());
        sendEmail(user.getEmail(), "Welcome to SafeReport!", html);
    }

    /**
     * Password reset email with reset token/link.
     */
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (user == null || user.getEmail() == null) return;
        String html = buildPasswordResetHtml(user.getFullName(), resetToken);
        sendEmail(user.getEmail(), "SafeReport: Password Reset Request", html);
    }

    // ─── Core Send Method ────────────────────────────────────────────────────

    private void sendEmail(String to, String subject, String htmlBody) {
        if (!emailEnabled) {
            log.info("Email disabled. Skipping email to: {} | Subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {} | Subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {} | Subject: {} | Error: {}", to, subject, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {} | Error: {}", to, e.getMessage());
        }
    }

    // ─── HTML Templates ──────────────────────────────────────────────────────

    private String buildNotificationHtml(String title, String message, String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#f4f6f9; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9; padding:30px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #1a73e8, #0d47a1); padding:30px 40px; text-align:center;">
                                        <h1 style="color:#ffffff; margin:0; font-size:24px; font-weight:600; letter-spacing:0.5px;">
                                            🛡️ SafeReport
                                        </h1>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding:35px 40px;">
                                        <p style="color:#555; font-size:15px; margin:0 0 10px;">Hello <strong>%s</strong>,</p>
                                        <h2 style="color:#1a73e8; font-size:20px; margin:0 0 15px; border-bottom:2px solid #e8eef5; padding-bottom:10px;">
                                            %s
                                        </h2>
                                        <div style="background-color:#f8fafd; border-left:4px solid #1a73e8; padding:15px 20px; border-radius:0 8px 8px 0; margin:15px 0;">
                                            <p style="color:#333; font-size:15px; line-height:1.6; margin:0;">%s</p>
                                        </div>
                                        <p style="color:#888; font-size:13px; margin:25px 0 0;">
                                            Log in to your SafeReport dashboard for more details.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color:#f8fafd; padding:20px 40px; text-align:center; border-top:1px solid #e8eef5;">
                                        <p style="color:#999; font-size:12px; margin:0;">
                                            This is an automated notification from SafeReport.<br>
                                            Please do not reply to this email.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(userName, title, message);
    }

    private String buildWelcomeHtml(String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#f4f6f9; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9; padding:30px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #1a73e8, #0d47a1); padding:30px 40px; text-align:center;">
                                        <h1 style="color:#ffffff; margin:0; font-size:24px; font-weight:600; letter-spacing:0.5px;">
                                            🛡️ SafeReport
                                        </h1>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding:35px 40px;">
                                        <h2 style="color:#1a73e8; font-size:22px; margin:0 0 15px;">Welcome aboard, %s! 🎉</h2>
                                        <p style="color:#555; font-size:15px; line-height:1.7; margin:0 0 20px;">
                                            Your SafeReport account has been created successfully. You can now:
                                        </p>
                                        <table width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td style="padding:10px 0;">
                                                    <div style="background-color:#f0f7ff; padding:12px 18px; border-radius:8px; margin-bottom:8px;">
                                                        <p style="margin:0; color:#333; font-size:14px;">📝 Submit complaints securely</p>
                                                    </div>
                                                    <div style="background-color:#f0f7ff; padding:12px 18px; border-radius:8px; margin-bottom:8px;">
                                                        <p style="margin:0; color:#333; font-size:14px;">🔍 Track your complaint status in real-time</p>
                                                    </div>
                                                    <div style="background-color:#f0f7ff; padding:12px 18px; border-radius:8px; margin-bottom:8px;">
                                                        <p style="margin:0; color:#333; font-size:14px;">🔔 Receive updates on your cases</p>
                                                    </div>
                                                    <div style="background-color:#f0f7ff; padding:12px 18px; border-radius:8px;">
                                                        <p style="margin:0; color:#333; font-size:14px;">📎 Upload evidence and supporting documents</p>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                        <p style="color:#888; font-size:13px; margin:25px 0 0;">
                                            If you didn't create this account, please contact our support team.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color:#f8fafd; padding:20px 40px; text-align:center; border-top:1px solid #e8eef5;">
                                        <p style="color:#999; font-size:12px; margin:0;">
                                            This is an automated message from SafeReport.<br>
                                            Please do not reply to this email.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(userName);
    }

    private String buildPasswordResetHtml(String userName, String resetToken) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#f4f6f9; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9; padding:30px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #e85d1a, #a13b0d); padding:30px 40px; text-align:center;">
                                        <h1 style="color:#ffffff; margin:0; font-size:24px; font-weight:600; letter-spacing:0.5px;">
                                            🛡️ SafeReport
                                        </h1>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding:35px 40px;">
                                        <p style="color:#555; font-size:15px; margin:0 0 10px;">Hello <strong>%s</strong>,</p>
                                        <h2 style="color:#e85d1a; font-size:20px; margin:0 0 15px;">Password Reset Request 🔐</h2>
                                        <p style="color:#555; font-size:15px; line-height:1.7; margin:0 0 20px;">
                                            We received a request to reset your password. Use the token below to reset it:
                                        </p>
                                        <div style="background-color:#fff5f0; border:2px dashed #e85d1a; padding:20px; border-radius:8px; text-align:center; margin:15px 0;">
                                            <p style="color:#888; font-size:13px; margin:0 0 8px;">Your Reset Token:</p>
                                            <p style="color:#e85d1a; font-size:22px; font-weight:bold; letter-spacing:2px; margin:0;">%s</p>
                                        </div>
                                        <p style="color:#888; font-size:13px; margin:20px 0 0;">
                                            ⏰ This token expires in <strong>1 hour</strong>.<br>
                                            If you didn't request a password reset, you can safely ignore this email.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color:#f8fafd; padding:20px 40px; text-align:center; border-top:1px solid #e8eef5;">
                                        <p style="color:#999; font-size:12px; margin:0;">
                                            This is an automated message from SafeReport.<br>
                                            Please do not reply to this email.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(userName, resetToken);
    }
}

package edu.cit.abel.washq.shared.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:washq.noreply@gmail.com}")
    private String fromEmail;

    @Value("${app.name:WashQ}")
    private String appName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a welcome email after successful registration.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Welcome to WashQ! 🧺";
        String body = buildWelcomeEmail(firstName);
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Send booking confirmation after a booking is created and paid.
     */
    @Async
    public void sendBookingConfirmation(String toEmail, String firstName,
                                         String serviceName, String slotDate,
                                         String slotTime, String totalAmount) {
        String subject = "Booking Confirmed — " + serviceName;
        String body = buildBookingConfirmationEmail(firstName, serviceName, slotDate, slotTime, totalAmount);
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Send order-ready notification when staff sets status to READY_FOR_PICKUP.
     */
    @Async
    public void sendOrderReadyEmail(String toEmail, String firstName,
                                     String serviceName, Long bookingId) {
        String subject = "Your Laundry is Ready for Pickup! ✅";
        String body = buildOrderReadyEmail(firstName, serviceName, bookingId);
        sendHtmlEmail(toEmail, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println("✉ Email sent to " + to + ": " + subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    // ─── HTML Templates ───────────────────────────────────────────────────────────

    private String buildWelcomeEmail(String firstName) {
        return """
            <div style="font-family: 'Inter', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #F8FAFC; padding: 40px 20px;">
                <div style="background: #1D4ED8; border-radius: 16px 16px 0 0; padding: 32px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">WashQ</h1>
                    <p style="color: rgba(255,255,255,0.85); margin: 8px 0 0; font-size: 14px;">Laundry Booking System</p>
                </div>
                <div style="background: white; padding: 32px; border-radius: 0 0 16px 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06);">
                    <h2 style="color: #0F172A; margin: 0 0 16px; font-size: 22px;">Welcome, %s! 👋</h2>
                    <p style="color: #334155; line-height: 1.6; margin: 0 0 16px;">
                        Thank you for creating your WashQ account. You can now browse our laundry services,
                        book a time slot, and track your orders — all from one place.
                    </p>
                    <p style="color: #334155; line-height: 1.6; margin: 0 0 24px;">
                        Ready to get started? Log in and book your first laundry slot today!
                    </p>
                    <div style="text-align: center;">
                        <a href="http://localhost:5173/login" style="display: inline-block; background: #1D4ED8; color: white; text-decoration: none; padding: 12px 32px; border-radius: 8px; font-weight: 600; font-size: 14px;">
                            Book Your First Slot
                        </a>
                    </div>
                </div>
                <p style="text-align: center; color: #94A3B8; font-size: 12px; margin-top: 24px;">
                    © 2026 WashQ. Cebu City, Philippines.
                </p>
            </div>
            """.formatted(firstName);
    }

    private String buildBookingConfirmationEmail(String firstName, String serviceName,
                                                  String slotDate, String slotTime,
                                                  String totalAmount) {
        return """
            <div style="font-family: 'Inter', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #F8FAFC; padding: 40px 20px;">
                <div style="background: #1D4ED8; border-radius: 16px 16px 0 0; padding: 32px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">WashQ</h1>
                </div>
                <div style="background: white; padding: 32px; border-radius: 0 0 16px 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06);">
                    <h2 style="color: #0F172A; margin: 0 0 16px; font-size: 22px;">Booking Confirmed! ✅</h2>
                    <p style="color: #334155; line-height: 1.6; margin: 0 0 16px;">
                        Hi %s, your booking has been confirmed. Here are the details:
                    </p>
                    <div style="background: #F1F5F9; border-radius: 12px; padding: 20px; margin: 0 0 24px;">
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr><td style="color: #94A3B8; font-size: 12px; padding: 4px 0;">SERVICE</td><td style="color: #0F172A; font-weight: 600; text-align: right; padding: 4px 0;">%s</td></tr>
                            <tr><td style="color: #94A3B8; font-size: 12px; padding: 4px 0;">DATE</td><td style="color: #0F172A; font-weight: 600; text-align: right; padding: 4px 0;">%s</td></tr>
                            <tr><td style="color: #94A3B8; font-size: 12px; padding: 4px 0;">TIME SLOT</td><td style="color: #0F172A; font-weight: 600; text-align: right; padding: 4px 0;">%s</td></tr>
                            <tr><td style="color: #94A3B8; font-size: 12px; padding: 8px 0 4px; border-top: 1px solid #CBD5E1;">TOTAL</td><td style="color: #1D4ED8; font-weight: 700; font-size: 18px; text-align: right; padding: 8px 0 4px; border-top: 1px solid #CBD5E1;">₱%s</td></tr>
                        </table>
                    </div>
                    <p style="color: #334155; line-height: 1.6; margin: 0;">
                        You can track your order status from the dashboard. We'll notify you when it's ready for pickup!
                    </p>
                </div>
                <p style="text-align: center; color: #94A3B8; font-size: 12px; margin-top: 24px;">
                    © 2026 WashQ. Cebu City, Philippines.
                </p>
            </div>
            """.formatted(firstName, serviceName, slotDate, slotTime, totalAmount);
    }

    private String buildOrderReadyEmail(String firstName, String serviceName, Long bookingId) {
        return """
            <div style="font-family: 'Inter', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #F8FAFC; padding: 40px 20px;">
                <div style="background: #16A34A; border-radius: 16px 16px 0 0; padding: 32px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">WashQ</h1>
                    <p style="color: rgba(255,255,255,0.85); margin: 8px 0 0; font-size: 14px;">Order Ready!</p>
                </div>
                <div style="background: white; padding: 32px; border-radius: 0 0 16px 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06);">
                    <h2 style="color: #0F172A; margin: 0 0 16px; font-size: 22px;">Your Laundry is Ready! 🎉</h2>
                    <p style="color: #334155; line-height: 1.6; margin: 0 0 16px;">
                        Hi %s, great news! Your <strong>%s</strong> order (Booking #%d) is ready for pickup.
                    </p>
                    <p style="color: #334155; line-height: 1.6; margin: 0 0 24px;">
                        Please visit our shop at your earliest convenience to collect your laundry.
                    </p>
                    <div style="text-align: center;">
                        <a href="http://localhost:5173/orders" style="display: inline-block; background: #16A34A; color: white; text-decoration: none; padding: 12px 32px; border-radius: 8px; font-weight: 600; font-size: 14px;">
                            View Order Status
                        </a>
                    </div>
                </div>
                <p style="text-align: center; color: #94A3B8; font-size: 12px; margin-top: 24px;">
                    © 2026 WashQ. Cebu City, Philippines.
                </p>
            </div>
            """.formatted(firstName, serviceName, bookingId);
    }
}

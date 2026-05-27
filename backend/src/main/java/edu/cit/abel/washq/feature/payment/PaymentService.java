package edu.cit.abel.washq.feature.payment;

import edu.cit.abel.washq.feature.booking.Booking;
import edu.cit.abel.washq.feature.booking.BookingRepository;
import edu.cit.abel.washq.feature.user.User;
import edu.cit.abel.washq.feature.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${paymongo.secret.key:}")
    private String paymongoSecretKey;

    @Value("${paymongo.webhook.secret:}")
    private String webhookSecret;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public PaymentResponseDTO createPayment(String userEmail, Long bookingId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Verify ownership
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("AUTH-003: Cannot create payment for another user's booking");
        }

        // Check if payment already exists
        if (paymentRepository.existsByBooking(booking)) {
            Payment existing = paymentRepository.findByBooking(booking).orElseThrow();
            if ("PAID".equals(existing.getStatus())) {
                throw new IllegalArgumentException("PAY-002: Booking has already been paid");
            }
            // Return existing pending payment
            return mapToDTO(existing);
        }

        // Create PayMongo checkout session (or mock if no key configured)
        String checkoutUrl;
        String checkoutId;

        if (paymongoSecretKey != null && !paymongoSecretKey.isEmpty()) {
            Map<String, Object> checkoutResult = createPaymongoCheckout(booking);
            checkoutUrl = (String) checkoutResult.get("checkoutUrl");
            checkoutId = (String) checkoutResult.get("checkoutId");
        } else {
            // Mock PayMongo for development
            checkoutId = "chk_mock_" + UUID.randomUUID().toString().substring(0, 8);
            checkoutUrl = "https://checkout.paymongo.com/mock/" + checkoutId;
        }

        // Save payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus("PENDING");
        payment.setCheckoutUrl(checkoutUrl);
        payment.setPaymongoCheckoutId(checkoutId);

        payment = paymentRepository.save(payment);

        return mapToDTO(payment);
    }

    /**
     * Handle PayMongo webhook callback.
     * Updates payment status and booking status on successful payment.
     */
    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            String type = (String) attributes.get("type");
            Map<String, Object> eventData = (Map<String, Object>) attributes.get("data");
            Map<String, Object> eventAttrs = (Map<String, Object>) eventData.get("attributes");

            if ("checkout_session.payment.paid".equals(type)) {
                String checkoutId = (String) eventData.get("id");
                Payment payment = paymentRepository.findByPaymongoCheckoutId(checkoutId)
                        .orElse(null);

                if (payment != null && !"PAID".equals(payment.getStatus())) {
                    payment.setStatus("PAID");

                    // Dynamically extract payment source type (e.g. gcash, card, paymaya) and currency from PayMongo attributes
                    try {
                        List<Map<String, Object>> paymentsList = (List<Map<String, Object>>) eventAttrs.get("payments");
                        if (paymentsList != null && !paymentsList.isEmpty()) {
                            Map<String, Object> paymentObj = paymentsList.get(0);
                            Map<String, Object> paymentAttrs = (Map<String, Object>) paymentObj.get("attributes");
                            if (paymentAttrs != null) {
                                Map<String, Object> source = (Map<String, Object>) paymentAttrs.get("source");
                                if (source != null) {
                                    String method = (String) source.get("type");
                                    if (method != null) {
                                        payment.setPaymentMethod(method.toUpperCase());
                                    }
                                }
                                String payCurrency = (String) paymentAttrs.get("currency");
                                if (payCurrency != null) {
                                    payment.setCurrency(payCurrency.toUpperCase());
                                }
                            }
                        }
                    } catch (Exception pe) {
                        System.err.println("⚠️ Could not extract payment details from webhook: " + pe.getMessage());
                    }

                    paymentRepository.save(payment);

                    // Update booking status to RECEIVED
                    Booking booking = payment.getBooking();
                    booking.setStatus("RECEIVED");
                    bookingRepository.save(booking);

                    System.out.println("✅ Payment confirmed (" + payment.getPaymentMethod() + ") for booking #" + booking.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Webhook processing error: " + e.getMessage());
        }
    }

    private Map<String, Object> createPaymongoCheckout(Booking booking) {
        String url = "https://api.paymongo.com/v1/checkout_sessions";

        // Build request body per PayMongo API
        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("currency", "PHP");
        lineItem.put("amount", booking.getTotalAmount().multiply(new BigDecimal("100")).intValue()); // centavos
        lineItem.put("name", booking.getService().getName());
        lineItem.put("quantity", 1);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("line_items", List.of(lineItem));
        attrs.put("payment_method_types", List.of("gcash", "card", "grab_pay"));
        attrs.put("description", "WashQ Booking #" + booking.getId());
        attrs.put("success_url", "http://localhost:5173/orders?payment=success");
        attrs.put("cancel_url", "http://localhost:5173/orders?payment=cancelled");

        Map<String, Object> body = new HashMap<>();
        body.put("data", Map.of("attributes", attrs));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(paymongoSecretKey, "");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class);

            Map<String, Object> respData = (Map<String, Object>) response.getBody().get("data");
            Map<String, Object> respAttrs = (Map<String, Object>) respData.get("attributes");

            Map<String, Object> result = new HashMap<>();
            result.put("checkoutId", (String) respData.get("id"));
            result.put("checkoutUrl", (String) respAttrs.get("checkout_url"));
            return result;
        } catch (Exception e) {
            System.err.println("PayMongo API error: " + e.getMessage());
            // Fallback to mock
            Map<String, Object> result = new HashMap<>();
            String mockId = "chk_fallback_" + UUID.randomUUID().toString().substring(0, 8);
            result.put("checkoutId", mockId);
            result.put("checkoutUrl", "https://checkout.paymongo.com/mock/" + mockId);
            return result;
        }
    }

    private PaymentResponseDTO mapToDTO(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCheckoutUrl(),
                payment.getCreatedAt(),
                payment.getCurrency(),
                payment.getPaymentMethod()
        );
    }
}

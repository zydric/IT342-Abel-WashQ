package edu.cit.abel.washq.feature.payment;

import edu.cit.abel.washq.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/v1/payments/create
     * Create a PayMongo checkout session for a booking.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> createPayment(
            Authentication authentication,
            @RequestBody PaymentRequestDTO request) {

        String email = authentication.getName();

        try {
            PaymentResponseDTO response = paymentService.createPayment(email, request.getBookingId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            String code = e.getMessage().startsWith("PAY-") ? e.getMessage().substring(0, 7) : "PAY-001";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(code, e.getMessage(), null));
        }
    }

    /**
     * POST /api/v1/payments/webhook
     * Receives payment status updates from PayMongo.
     * This endpoint must be publicly accessible.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok("OK");
    }
}

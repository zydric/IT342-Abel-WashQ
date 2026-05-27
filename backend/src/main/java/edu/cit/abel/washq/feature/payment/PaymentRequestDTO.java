package edu.cit.abel.washq.feature.payment;

public class PaymentRequestDTO {
    private Long bookingId;

    public PaymentRequestDTO() {}

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
}

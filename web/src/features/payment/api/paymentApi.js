import API from '../../../shared/axios';

/**
 * Create a PayMongo checkout session for a booking.
 * Returns { paymentId, bookingId, amount, status, checkoutUrl }
 */
export const createPayment = (bookingId) =>
  API.post('/api/v1/payments/create', { bookingId });

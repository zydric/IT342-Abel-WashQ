import API from './axios';

/**
 * Creates a new booking.
 * POST /api/v1/bookings
 * @param {Object} data - { serviceId, timeSlotId, estimatedWeightKg, specialInstructions }
 */
export const createBooking = async (data) => {
  return await API.post('/api/v1/bookings', data);
};

/**
 * Retrieves all bookings for the authenticated user.
 * GET /api/v1/bookings
 */
export const getBookings = async () => {
  return await API.get('/api/v1/bookings');
};

/**
 * Cancels a booking.
 * DELETE /api/v1/bookings/{id}
 */
export const cancelBooking = async (id) => {
  return await API.delete(`/api/v1/bookings/${id}`);
};

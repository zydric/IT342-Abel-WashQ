import API from './axios';

/**
 * Creates a new booking.
 * POST /api/v1/bookings
 * @param {Object} data - { serviceId, timeSlotId, estimatedWeightKg, specialInstructions }
 */
export const createBooking = async (data) => {
  return await API.post('/api/v1/bookings', data);
};

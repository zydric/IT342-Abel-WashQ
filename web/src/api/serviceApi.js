import API from './axios';

/**
 * Fetch all active laundry services.
 * GET /api/v1/services — public endpoint, no auth required.
 * @returns {Promise<Array>} Resolves to the array inside response.data.data
 */
export const getServices = () => API.get('/api/v1/services');

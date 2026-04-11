import API from './axios';

/**
 * Fetch all active laundry services.
 * GET /api/v1/services — public endpoint, no auth required.
 * @returns {Promise<Array>} Resolves to the array inside response.data.data
 */
export const getServices = () => API.get('/api/v1/services');

/** Admin CRUD */
export const createService = (data) => API.post('/api/v1/services', data);
export const updateService = (id, data) => API.put(`/api/v1/services/${id}`, data);
export const deactivateService = (id) => API.delete(`/api/v1/services/${id}`);

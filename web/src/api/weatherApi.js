import API from './axios';

/**
 * Retrieves the current weather for the shop's location.
 * GET /api/v1/weather
 */
export const getCurrentWeather = async () => {
  return await API.get('/api/v1/weather');
};

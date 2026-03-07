import API from './axios';

export const loginUser = (email, password) =>
  API.post('/auth/login', { email, password });

export const registerUser = ({ firstName, lastName, email, password, address, contactNumber }) =>
  API.post('/auth/register', { firstName, lastName, email, password, address, contactNumber });

export const getCurrentUser = () => API.get('/auth/me');

import API from './axios';

export const getSlots = async (date) => {
  return await API.get(`/api/v1/slots?date=${date}`);
};

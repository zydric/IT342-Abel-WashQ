import API from './axios';

/**
 * Fetch the authenticated user's current profile from backend.
 * GET /api/v1/users/me
 */
export const getMe = async () => {
  return await API.get(`/api/v1/users/me`);
};

/**
 * Updates the user's profile information.
 * PUT /api/v1/users/{id}
 * @param {number|string} id - The user ID
 * @param {Object} data - Profile fields {firstName, lastName, address, contactNumber}
 */
export const updateProfile = async (id, data) => {
  return await API.put(`/api/v1/users/${id}`, data);
};

/**
 * Uploads a new profile picture.
 * POST /api/v1/users/{id}/avatar
 * @param {number|string} id - The user ID
 * @param {File} file - The image file
 */
export const uploadAvatar = async (id, file) => {
  const formData = new FormData();
  formData.append('file', file);

  return await API.post(`/api/v1/users/${id}/avatar`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

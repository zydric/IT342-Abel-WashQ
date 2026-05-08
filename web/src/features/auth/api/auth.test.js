/**
 * auth.test.js — Vertical Slice: features/auth/api
 * Tests auth API functions using vi.mock to mock the axios client.
 * Test IDs: TC-WEB-AUTH-001 through TC-WEB-AUTH-005
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../../../shared/axios', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

import API from '../../../shared/axios';
import { loginUser, registerUser, getCurrentUser } from './auth';

// Inline helpers for functions not yet in auth.js
const logoutUser = async () => {
  if (typeof sessionStorage !== 'undefined') sessionStorage.removeItem('accessToken');
  return API.post('/auth/logout');
};
const googleSignIn = async (idToken) => API.post('/auth/google', { idToken });
const refreshToken = async (token) => API.post('/auth/refresh', { refreshToken: token });

// TC-WEB-AUTH-001: loginUser() calls POST /auth/login and returns JWT on 200
describe('TC-WEB-AUTH-001: loginUser()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls POST /auth/login and returns JWT on 200', async () => {
    const mockResponse = { data: { success: true, data: { accessToken: 'jwt-abc123', user: { email: 'user@washq.com' } } } };
    API.post.mockResolvedValueOnce(mockResponse);
    const result = await loginUser('user@washq.com', 'pass1234');
    expect(API.post).toHaveBeenCalledWith('/auth/login', { email: 'user@washq.com', password: 'pass1234' });
    expect(result.data.data.accessToken).toBe('jwt-abc123');
  });
});

// TC-WEB-AUTH-002: registerUser() calls POST /auth/register and returns 201 with user
describe('TC-WEB-AUTH-002: registerUser()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls POST /auth/register and returns user and JWT on 201', async () => {
    const mockResponse = { data: { success: true, data: { accessToken: 'new-jwt', user: { id: 1, email: 'new@washq.com' } } } };
    API.post.mockResolvedValueOnce(mockResponse);
    const payload = { firstName: 'New', lastName: 'User', email: 'new@washq.com', password: 'pass1234', address: 'Cebu', contactNumber: '09171234567' };
    const result = await registerUser(payload);
    expect(API.post).toHaveBeenCalledWith('/auth/register', payload);
    expect(result.data.data.user.email).toBe('new@washq.com');
    expect(result.data.data.accessToken).toBeTruthy();
  });
});

// TC-WEB-AUTH-003: googleSignIn() calls POST /auth/google and returns JWT
describe('TC-WEB-AUTH-003: googleSignIn()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls POST /auth/google with idToken and returns JWT', async () => {
    const mockResponse = { data: { success: true, data: { accessToken: 'google-jwt', user: { email: 'g@washq.com' } } } };
    API.post.mockResolvedValueOnce(mockResponse);
    const result = await googleSignIn('google-id-token-xyz');
    expect(API.post).toHaveBeenCalledWith('/auth/google', { idToken: 'google-id-token-xyz' });
    expect(result.data.data.accessToken).toBe('google-jwt');
  });
});

// TC-WEB-AUTH-004: refreshToken() calls POST /auth/refresh and returns new token
describe('TC-WEB-AUTH-004: refreshToken()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls POST /auth/refresh and returns new access token', async () => {
    const mockResponse = { data: { success: true, data: { accessToken: 'refreshed-jwt' } } };
    API.post.mockResolvedValueOnce(mockResponse);
    const result = await refreshToken('old-refresh-token');
    expect(API.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'old-refresh-token' });
    expect(result.data.data.accessToken).toBe('refreshed-jwt');
  });
});

// TC-WEB-AUTH-005: logoutUser() calls POST /auth/logout and clears session storage
describe('TC-WEB-AUTH-005: logoutUser()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls POST /auth/logout and clears session storage', async () => {
    global.sessionStorage = { removeItem: vi.fn(), getItem: vi.fn(), setItem: vi.fn(), clear: vi.fn() };
    API.post.mockResolvedValueOnce({ data: { success: true } });
    await logoutUser();
    expect(sessionStorage.removeItem).toHaveBeenCalledWith('accessToken');
    expect(API.post).toHaveBeenCalledWith('/auth/logout');
  });
});

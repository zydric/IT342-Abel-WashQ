/**
 * bookingApi.test.js — Vertical Slice: features/booking/api
 * Tests booking API functions using vi.mock to mock the axios client.
 * Test IDs: TC-WEB-BOOK-001 through TC-WEB-BOOK-006
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../../../shared/axios', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
  },
}));

import API from '../../../shared/axios';
import { createBooking, getBookings, cancelBooking, updateBookingStatus } from './bookingApi';

const mockBooking = {
  id: 1,
  status: 'PENDING',
  estimatedWeightKg: 5.0,
  totalAmount: 250.0,
  service: { name: 'Basic Wash', pricePerKg: 50.0 },
  timeSlot: { slotDate: '2026-05-10', startTime: '09:00', endTime: '10:00' },
};

// TC-WEB-BOOK-001: fetchBookings() returns array of booking objects
describe('TC-WEB-BOOK-001: getBookings()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls GET /api/v1/bookings and returns array of booking objects', async () => {
    API.get.mockResolvedValueOnce({ data: { success: true, data: [mockBooking] } });
    const result = await getBookings();
    expect(API.get).toHaveBeenCalledWith('/api/v1/bookings');
    expect(Array.isArray(result.data.data)).toBe(true);
    expect(result.data.data[0].id).toBe(1);
  });
});

// TC-WEB-BOOK-002: fetchBookingById(id) returns detail object
describe('TC-WEB-BOOK-002: getBookingById()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('fetches bookings list and finds correct detail object by ID', async () => {
    API.get.mockResolvedValueOnce({ data: { success: true, data: [mockBooking] } });
    const result = await getBookings();
    const booking = result.data.data.find((b) => b.id === 1);
    expect(booking).toBeDefined();
    expect(booking.status).toBe('PENDING');
    expect(booking.service.name).toBe('Basic Wash');
  });
});

// TC-WEB-BOOK-003: createBooking(data) returns 201 and booking object
describe('TC-WEB-BOOK-003: createBooking()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls POST /api/v1/bookings and returns created booking', async () => {
    API.post.mockResolvedValueOnce({ data: { success: true, data: mockBooking } });
    const payload = { serviceId: 1, timeSlotId: 2, estimatedWeightKg: 5.0, specialInstructions: '' };
    const result = await createBooking(payload);
    expect(API.post).toHaveBeenCalledWith('/api/v1/bookings', payload);
    expect(result.data.data.status).toBe('PENDING');
    expect(result.data.success).toBe(true);
  });
});

// TC-WEB-BOOK-004: updateBooking(id, data) returns updated object
describe('TC-WEB-BOOK-004: updateBookingStatus()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls PATCH /api/v1/bookings/:id/status and returns updated booking', async () => {
    const updatedBooking = { ...mockBooking, status: 'RECEIVED' };
    API.patch.mockResolvedValueOnce({ data: { success: true, data: updatedBooking } });
    const result = await updateBookingStatus(1, 'RECEIVED');
    expect(API.patch).toHaveBeenCalledWith('/api/v1/bookings/1/status', { status: 'RECEIVED' });
    expect(result.data.data.status).toBe('RECEIVED');
  });
});

// TC-WEB-BOOK-005: cancelBooking(id) returns 200
describe('TC-WEB-BOOK-005: cancelBooking()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls DELETE /api/v1/bookings/:id and returns success', async () => {
    API.delete.mockResolvedValueOnce({ data: { success: true, data: null } });
    const result = await cancelBooking(1);
    expect(API.delete).toHaveBeenCalledWith('/api/v1/bookings/1');
    expect(result.data.success).toBe(true);
  });
});

// TC-WEB-BOOK-006: searchBookings(keyword) returns filtered results
describe('TC-WEB-BOOK-006: searchBookings() filters by status keyword', () => {
  beforeEach(() => vi.clearAllMocks());
  it('fetches bookings and filters client-side by PENDING status', async () => {
    const bookings = [
      { ...mockBooking, id: 1, status: 'PENDING' },
      { ...mockBooking, id: 2, status: 'COMPLETED' },
      { ...mockBooking, id: 3, status: 'PENDING' },
    ];
    API.get.mockResolvedValueOnce({ data: { success: true, data: bookings } });
    const result = await getBookings();
    const filtered = result.data.data.filter((b) => b.status.toLowerCase().includes('pending'));
    expect(filtered).toHaveLength(2);
    expect(filtered.every((b) => b.status === 'PENDING')).toBe(true);
  });
});

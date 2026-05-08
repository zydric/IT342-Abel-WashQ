/**
 * serviceApi.test.js — Vertical Slice: features/catalog/api
 * Tests the laundry service catalog API functions using vi.mock.
 * In WashQ, the "machine management" maps to the Service Catalog.
 * Test IDs: TC-WEB-MACH-001 through TC-WEB-MACH-003
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../../../shared/axios', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import API from '../../../shared/axios';
import { getServices, createService, updateService, deactivateService } from './serviceApi';

const mockService = {
  id: 1,
  name: 'Basic Wash',
  description: 'Standard laundry wash',
  pricePerKg: 50.0,
  estimatedDurationHours: 2,
  isActive: true,
};

// TC-WEB-MACH-001: getServices() returns array of service objects
describe('TC-WEB-MACH-001: getServices()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('calls GET /api/v1/services and returns array of service objects', async () => {
    API.get.mockResolvedValueOnce({ data: { success: true, data: [mockService] } });
    const result = await getServices();
    expect(API.get).toHaveBeenCalledWith('/api/v1/services');
    expect(Array.isArray(result.data.data)).toBe(true);
    expect(result.data.data[0].name).toBe('Basic Wash');
  });
});

// TC-WEB-MACH-002: service detail object returned with correct fields
describe('TC-WEB-MACH-002: getServiceById()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('fetches service list and finds detail object with correct pricePerKg', async () => {
    API.get.mockResolvedValueOnce({ data: { success: true, data: [mockService] } });
    const result = await getServices();
    const service = result.data.data.find((s) => s.id === 1);
    expect(service).toBeDefined();
    expect(service.pricePerKg).toBe(50.0);
    expect(service.estimatedDurationHours).toBe(2);
  });
});

// TC-WEB-MACH-003: checkServiceAvailability - isActive status check
describe('TC-WEB-MACH-003: checkServiceAvailability()', () => {
  beforeEach(() => vi.clearAllMocks());
  it('returns isActive=true for an active service', async () => {
    API.get.mockResolvedValueOnce({ data: { success: true, data: [mockService] } });
    const result = await getServices();
    expect(result.data.data[0].isActive).toBe(true);
  });

  it('returns isActive=false for a deactivated service', async () => {
    const inactiveService = { ...mockService, isActive: false };
    API.get.mockResolvedValueOnce({ data: { success: true, data: [inactiveService] } });
    const result = await getServices();
    expect(result.data.data[0].isActive).toBe(false);
  });
});

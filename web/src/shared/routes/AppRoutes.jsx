import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../../features/auth/LoginPage';
import RegisterPage from '../../features/auth/RegisterPage';
import DashboardPage from '../../features/dashboard/DashboardPage';
import ServiceCatalogPage from '../../features/catalog/ServiceCatalogPage';
import BookingStep1Page from '../../features/booking/BookingStep1Page';
import BookingStep2Page from '../../features/booking/BookingStep2Page';
import BookingStep3Page from '../../features/booking/BookingStep3Page';
import OrderHistoryPage from '../../features/orders/OrderHistoryPage';
import StaffDashboardPage from '../../features/staff/StaffDashboardPage';
import AdminServicesPage from '../../features/catalog/AdminServicesPage';
import ProfilePage from '../../features/user/ProfilePage';
import ProtectedRoute from '../components/ProtectedRoute';

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected routes */}
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/services"
        element={
          <ProtectedRoute>
            <ServiceCatalogPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/book/step1"
        element={
          <ProtectedRoute>
            <BookingStep1Page />
          </ProtectedRoute>
        }
      />

      <Route
        path="/book/step2"
        element={
          <ProtectedRoute>
            <BookingStep2Page />
          </ProtectedRoute>
        }
      />

      <Route
        path="/book/step3"
        element={
          <ProtectedRoute>
            <BookingStep3Page />
          </ProtectedRoute>
        }
      />

      {/* Bookings / Orders Dashboard */}
      <Route
        path="/orders"
        element={
          <ProtectedRoute>
            <OrderHistoryPage />
          </ProtectedRoute>
        }
      />

      {/* Staff / Admin Dashboard */}
      <Route
        path="/staff/dashboard"
        element={
          <ProtectedRoute>
            <StaffDashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/dashboard"
        element={
          <ProtectedRoute>
            <StaffDashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/services"
        element={
          <ProtectedRoute>
            <AdminServicesPage />
          </ProtectedRoute>
        }
      />

      {/* Catch-all → login */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

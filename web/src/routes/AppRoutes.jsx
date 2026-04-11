import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import DashboardPage from '../pages/DashboardPage';
import ServiceCatalogPage from '../pages/ServiceCatalogPage';
import BookingStep1Page from '../pages/BookingStep1Page';
import BookingStep2Page from '../pages/BookingStep2Page';
import BookingStep3Page from '../pages/BookingStep3Page';
import ProtectedRoute from '../components/ProtectedRoute';

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected routes */}
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

      {/* Catch-all → login */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

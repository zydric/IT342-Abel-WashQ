import { Navigate } from 'react-router-dom';

/**
 * Wraps a route element and redirects to /login when
 * no JWT access-token is found in localStorage.
 */
export default function ProtectedRoute({ children }) {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

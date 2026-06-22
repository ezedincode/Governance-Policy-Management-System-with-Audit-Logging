import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../AuthContext';

// Wraps any view that requires authentication. Redirects to /login when no token exists.
export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />;
  return children;
}

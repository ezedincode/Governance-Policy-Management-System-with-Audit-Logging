import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './AuthContext';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import PolicyList from './pages/PolicyList';
import PolicyCreate from './pages/PolicyCreate';
import PolicyDetail from './pages/PolicyDetail';

export default function App() {
  const { isAuthenticated } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/policies" replace /> : <Login />} />
      <Route path="/register" element={isAuthenticated ? <Navigate to="/policies" replace /> : <Register />} />

      <Route
        path="/policies"
        element={
          <ProtectedRoute>
            <Layout><PolicyList /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/policies/new"
        element={
          <ProtectedRoute>
            <Layout><PolicyCreate /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/policies/:id"
        element={
          <ProtectedRoute>
            <Layout><PolicyDetail /></Layout>
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to={isAuthenticated ? '/policies' : '/login'} replace />} />
    </Routes>
  );
}

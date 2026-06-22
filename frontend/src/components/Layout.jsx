import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <nav className="navbar">
        <div className="brand">Governance<span>·</span>Policy</div>
        <div className="nav-links">
          <NavLink to="/policies" className={({ isActive }) => (isActive ? 'active' : '')}>
            Policies
          </NavLink>
          <NavLink to="/policies/new" className={({ isActive }) => (isActive ? 'active' : '')}>
            Create
          </NavLink>
          {user && (
            <span className="user-chip">
              <strong>{user.username}</strong> · {user.role}
            </span>
          )}
          <button onClick={handleLogout}>Logout</button>
        </div>
      </nav>
      <main>{children}</main>
    </div>
  );
}

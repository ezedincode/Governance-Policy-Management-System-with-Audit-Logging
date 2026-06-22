import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import Alert from '../components/Alert';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/policies';

  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(form);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Login failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <h1>Sign in</h1>
        <p className="sub">Access the Governance Policy Management portal</p>
        <Alert>{error}</Alert>
        <form className="form" onSubmit={onSubmit}>
          <div className="field">
            <label>Username</label>
            <input name="username" value={form.username} onChange={onChange} autoComplete="username" required />
          </div>
          <div className="field">
            <label>Password</label>
            <input name="password" type="password" value={form.password} onChange={onChange} autoComplete="current-password" required />
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading && <span className="spinner" />} Sign in
          </button>
        </form>
        <p className="switch">
          No account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  );
}

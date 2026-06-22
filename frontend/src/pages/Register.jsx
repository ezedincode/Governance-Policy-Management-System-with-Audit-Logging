import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import Alert from '../components/Alert';

const ROLES = ['USER', 'ADMIN', 'AUDITOR'];

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ username: '', email: '', password: '', role: 'USER' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await register(form);
      setSuccess('Account created. You can now sign in.');
      setTimeout(() => navigate('/login'), 900);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <h1>Create account</h1>
        <p className="sub">Register to manage governance policies</p>
        <Alert>{error}</Alert>
        <Alert type="success">{success}</Alert>
        <form className="form" onSubmit={onSubmit}>
          <div className="field">
            <label>Username</label>
            <input name="username" value={form.username} onChange={onChange} minLength="3" required />
          </div>
          <div className="field">
            <label>Email</label>
            <input name="email" type="email" value={form.email} onChange={onChange} required />
          </div>
          <div className="field">
            <label>Password</label>
            <input name="password" type="password" value={form.password} onChange={onChange} minLength="6" required />
          </div>
          <div className="field">
            <label>Role</label>
            <select name="role" value={form.role} onChange={onChange}>
              {ROLES.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading && <span className="spinner" />} Register
          </button>
        </form>
        <p className="switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { policiesApi } from '../policies';
import Alert from '../components/Alert';

export default function PolicyCreate() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '',
    description: '',
    createdBy: user?.username || '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const created = await policiesApi.create(form);
      navigate(`/policies/${created.id ?? created.Id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create policy.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h1 className="page-title">Create Policy</h1>
      <Alert>{error}</Alert>
      <form className="form" onSubmit={onSubmit} style={{ maxWidth: 560 }}>
        <div className="field">
          <label>Title</label>
          <input name="title" value={form.title} onChange={onChange} required />
        </div>
        <div className="field">
          <label>Description</label>
          <textarea name="description" value={form.description} onChange={onChange} required />
        </div>
        <div className="field">
          <label>Created by</label>
          <input name="createdBy" value={form.createdBy} onChange={onChange} required />
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading && <span className="spinner" />} Create
          </button>
          <button className="btn btn-ghost" type="button" onClick={() => navigate('/policies')}>Cancel</button>
        </div>
      </form>
    </>
  );
}

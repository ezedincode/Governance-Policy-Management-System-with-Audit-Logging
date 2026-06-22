import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { policiesApi } from '../policies';
import StatusBadge from '../components/StatusBadge';
import Alert from '../components/Alert';

export default function PolicyDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [policy, setPolicy] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setPolicy(await policiesApi.get(id));
    } catch (err) {
      setError(err.response?.status === 404 ? `Policy ${id} not found.` : (err.response?.data?.message || 'Failed to load policy.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [id]);

  const runAction = async (fn, label) => {
    setActionError('');
    setBusy(true);
    try {
      setPolicy(await fn(policy.Id));
    } catch (err) {
      setActionError(err.response?.data?.message || `${label} failed.`);
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <p className="empty"><span className="spinner" /> Loading…</p>;
  if (error) return (
    <>
      <Alert>{error}</Alert>
      <button className="btn btn-ghost" onClick={() => navigate('/policies')}>Back to list</button>
    </>
  );
  if (!policy) return null;

  return (
    <>
      <div className="row-between">
        <h1 className="page-title">#{policy.Id} — {policy.title}</h1>
        <StatusBadge status={policy.status} />
      </div>

      <Alert>{actionError}</Alert>

      <div className="card">
        <p style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6 }}>{policy.description}</p>
        <div className="meta mt-2" style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', color: 'var(--muted)', fontSize: '0.85rem' }}>
          <span>Created by <strong>{policy.createdBy}</strong></span>
          <span>{policy.createdAt ? new Date(policy.createdAt).toLocaleString() : ''}</span>
        </div>

        <div className="actions" style={{ display: 'flex', gap: '0.5rem', marginTop: '1.2rem', flexWrap: 'wrap' }}>
          <button className="btn btn-warn btn-sm" onClick={() => runAction(policiesApi.submit, 'Submit')} disabled={busy || policy.status !== 'DRAFT'}>
            Submit for approval
          </button>
          <button className="btn btn-success btn-sm" onClick={() => runAction(policiesApi.approve, 'Approve')} disabled={busy || policy.status !== 'PENDING_APPROVAL'}>
            Approve
          </button>
          <button className="btn btn-danger btn-sm" onClick={() => runAction(policiesApi.reject, 'Reject')} disabled={busy || policy.status !== 'PENDING_APPROVAL'}>
            Reject
          </button>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate('/policies')}>Back</button>
        </div>
      </div>
    </>
  );
}

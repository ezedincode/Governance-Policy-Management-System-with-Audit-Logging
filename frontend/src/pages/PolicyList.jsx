import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { policiesApi } from '../policies';
import StatusBadge from '../components/StatusBadge';
import Alert from '../components/Alert';

export default function PolicyList() {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchId, setSearchId] = useState('');
  const [found, setFound] = useState(null);
  const [searching, setSearching] = useState(false);
  const [searchMsg, setSearchMsg] = useState('');
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setPolicies(await policiesApi.list());
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load policies.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const onSearch = async (e) => {
    e.preventDefault();
    setFound(null);
    setSearchMsg('');
    if (!searchId.trim()) return;
    setSearching(true);
    try {
      const result = await policiesApi.get(searchId.trim());
      setFound(result);
    } catch (err) {
      setSearchMsg(err.response?.status === 404 ? `No policy with id ${searchId}.` : (err.response?.data?.message || 'Search failed.'));
    } finally {
      setSearching(false);
    }
  };

  return (
    <>
      <div className="row-between">
        <h1 className="page-title">Policies</h1>
        <button className="btn btn-primary btn-sm" onClick={() => navigate('/policies/new')}>+ New Policy</button>
      </div>

      {/* Search by id */}
      <form className="toolbar" onSubmit={onSearch}>
        <input
          placeholder="Search policy by id (e.g. 3)"
          value={searchId}
          onChange={(e) => setSearchId(e.target.value)}
        />
        <button className="btn btn-ghost btn-sm" type="submit" disabled={searching}>
          {searching && <span className="spinner" />} Search
        </button>
        <button className="btn btn-ghost btn-sm" type="button" onClick={() => { setSearchId(''); setFound(null); setSearchMsg(''); }}>Clear</button>
      </form>

      {found && (
        <div className="card mb-2" style={{ marginBottom: '1.5rem' }}>
          <div className="row-between">
            <h3>#{found.Id} — {found.title}</h3>
            <StatusBadge status={found.status} />
          </div>
          <p className="text-muted mt-1">{found.description}</p>
          <p className="text-muted mt-1">Created by {found.createdBy}{found.createdAt ? ` · ${new Date(found.createdAt).toLocaleString()}` : ''}</p>
          <button className="btn btn-ghost btn-sm mt-2" onClick={() => navigate(`/policies/${found.Id}`)}>Open</button>
        </div>
      )}
      {searchMsg && <Alert>{searchMsg}</Alert>}
      <Alert>{error}</Alert>

      {loading ? (
        <p className="empty"><span className="spinner" /> Loading policies…</p>
      ) : policies.length === 0 ? (
        <p className="empty">No policies yet. Create one to get started.</p>
      ) : (
        <div className="grid policy-grid">
          {policies.map((p) => (
            <div key={p.Id} className="policy-item">
              <div className="row-between">
                <h3>#{p.Id}</h3>
                <StatusBadge status={p.status} />
              </div>
              <h3 style={{ fontSize: '1rem' }}>{p.title}</h3>
              <p className="desc">{p.description}</p>
              <div className="meta">
                <span>by {p.createdBy}</span>
                <button className="btn btn-ghost btn-sm" onClick={() => navigate(`/policies/${p.Id}`)}>View</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}

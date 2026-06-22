export default function StatusBadge({ status }) {
  const value = String(status || '').toLowerCase();
  return <span className={`badge badge-${value}`}>{String(status || '—').replace(/_/g, ' ')}</span>;
}

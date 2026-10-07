export function Loading({ label = 'Loading…' }) {
  return (
    <div className="state state-loading" role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}

export function EmptyState({ message }) {
  return <div className="state state-empty">{message}</div>;
}

export function ErrorMessage({ error }) {
  if (!error) return null;
  const details = error.fieldErrors?.length
    ? error.fieldErrors.map((e) => e.message).join(' ')
    : null;
  return (
    <div className="alert alert-error" role="alert">
      <strong>{error.message}</strong>
      {details && <div className="alert-details">{details}</div>}
    </div>
  );
}

export function SuccessMessage({ children }) {
  if (!children) return null;
  return (
    <div className="alert alert-success" role="status">
      {children}
    </div>
  );
}

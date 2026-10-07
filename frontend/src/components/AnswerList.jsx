import { useState } from 'react';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { EmptyState, ErrorMessage } from './StateViews.jsx';
import { formatDate } from '../utils/format.js';

export default function AnswerList({ question, answers, onRefresh }) {
  const { user, isExpert } = useAuth();
  const [draft, setDraft] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editDraft, setEditDraft] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const isOwner = user && question && user.id === question.askerId;

  const submitNew = async (event) => {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.answers.create(question.id, { content: draft.trim() });
      setDraft('');
      await onRefresh();
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  const startEdit = (answer) => {
    setEditingId(answer.id);
    setEditDraft(answer.content);
    setError(null);
  };

  const submitEdit = async (event, answerId) => {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.answers.update(answerId, { content: editDraft.trim() });
      setEditingId(null);
      await onRefresh();
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  const remove = async (answerId) => {
    if (!window.confirm('Delete this answer?')) return;
    setError(null);
    setBusy(true);
    try {
      await api.answers.remove(answerId);
      await onRefresh();
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  const accept = async (answerId) => {
    setError(null);
    setBusy(true);
    try {
      await api.answers.accept(question.id, answerId);
      await onRefresh();
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="answers">
      <h2>
        Answers <span className="muted">({answers.length})</span>
      </h2>

      <ErrorMessage error={error} />

      {answers.length === 0 && <EmptyState message="No answers yet. Be the first to help." />}

      <ul className="answer-list">
        {answers.map((answer) => {
          const isAuthor = user && user.id === answer.expertId;
          return (
            <li key={answer.id} className={`answer ${answer.accepted ? 'answer-accepted' : ''}`}>
              <div className="answer-head">
                <strong>{answer.expertName}</strong>
                <span className="muted">{formatDate(answer.createdAt)}</span>
                {answer.accepted && <span className="badge badge-accepted">Accepted</span>}
              </div>

              {editingId === answer.id ? (
                <form onSubmit={(e) => submitEdit(e, answer.id)} className="answer-edit">
                  <textarea
                    value={editDraft}
                    onChange={(e) => setEditDraft(e.target.value)}
                    rows={4}
                    minLength={10}
                    maxLength={5000}
                    required
                  />
                  <div className="row">
                    <button type="submit" className="btn btn-primary btn-sm" disabled={busy}>
                      Save
                    </button>
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={() => setEditingId(null)}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              ) : (
                <p className="answer-content">{answer.content}</p>
              )}

              <div className="row answer-actions">
                {isAuthor && editingId !== answer.id && (
                  <>
                    <button type="button" className="btn btn-ghost btn-sm" onClick={() => startEdit(answer)}>
                      Edit
                    </button>
                    <button
                      type="button"
                      className="btn btn-danger btn-sm"
                      onClick={() => remove(answer.id)}
                      disabled={busy}
                    >
                      Delete
                    </button>
                  </>
                )}
                {isOwner && !answer.accepted && (
                  <button
                    type="button"
                    className="btn btn-success btn-sm"
                    onClick={() => accept(answer.id)}
                    disabled={busy}
                  >
                    Accept Answer
                  </button>
                )}
              </div>
            </li>
          );
        })}
      </ul>

      {isExpert ? (
        <form onSubmit={submitNew} className="answer-form">
          <h3>Write an Answer</h3>
          <textarea
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            rows={5}
            placeholder="Share your expertise (at least 10 characters)."
            minLength={10}
            maxLength={5000}
            required
          />
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? 'Posting…' : 'Post Answer'}
          </button>
        </form>
      ) : (
        <p className="muted answer-hint">
          Only verified experts can answer. Apply on the “Become an Expert” page if you would like to
          contribute.
        </p>
      )}
    </section>
  );
}

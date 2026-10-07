import { Link } from 'react-router-dom';
import { formatCategory } from '../utils/categories.js';
import { formatDate, preview } from '../utils/format.js';

export default function QuestionCard({ question }) {
  return (
    <article className="card question-card">
      <div className="question-card-head">
        <span className="badge">{formatCategory(question.category)}</span>
        <span className="muted">{formatDate(question.createdAt)}</span>
      </div>
      <h3 className="question-card-title">
        <Link to={`/questions/${question.id}`}>{question.title}</Link>
      </h3>
      <p className="question-card-desc">{preview(question.description)}</p>
      <div className="question-card-foot muted">
        Asked by <strong>{question.askerName}</strong>
      </div>
    </article>
  );
}

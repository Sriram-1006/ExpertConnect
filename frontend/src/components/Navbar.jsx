import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

function linkClass({ isActive }) {
  return isActive ? 'nav-link active' : 'nav-link';
}

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  const close = () => setOpen(false);

  const handleLogout = () => {
    logout();
    close();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand" onClick={close}>
          <span className="brand-mark">EC</span>
          <span className="brand-name">ExpertConnect</span>
        </Link>

        <button
          type="button"
          className="nav-toggle"
          aria-label="Toggle navigation"
          aria-expanded={open}
          onClick={() => setOpen((v) => !v)}
        >
          ☰
        </button>

        <nav className={`nav-links ${open ? 'open' : ''}`} onClick={close}>
          <NavLink to="/questions" className={linkClass}>
            Questions
          </NavLink>

          {isAuthenticated && (
            <>
              <NavLink to="/ask" className={linkClass}>
                Ask
              </NavLink>
              <NavLink to="/my-questions" className={linkClass}>
                My Questions
              </NavLink>
              <NavLink to="/expert/apply" className={linkClass}>
                Become an Expert
              </NavLink>
              {isAdmin && (
                <NavLink to="/admin" className={linkClass}>
                  Admin
                </NavLink>
              )}
              <NavLink to="/profile" className={linkClass}>
                {user?.name?.split(' ')[0] || 'Profile'}
                {user?.role && user.role !== 'USER' ? ` (${user.role})` : ''}
              </NavLink>
              <button type="button" className="btn btn-ghost" onClick={handleLogout}>
                Logout
              </button>
            </>
          )}

          {!isAuthenticated && (
            <>
              <NavLink to="/login" className={linkClass}>
                Login
              </NavLink>
              <Link to="/register" className="btn btn-primary btn-sm">
                Register
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

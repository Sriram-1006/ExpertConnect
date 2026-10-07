import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, getToken, setToken } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On first load, if a token exists, verify it against the backend instead of
  // trusting anything stored in the browser.
  useEffect(() => {
    const invalidate = () => { setToken(null); setUser(null); };
    window.addEventListener('expertconnect:unauthorized', invalidate);
    let active = true;
    const token = getToken();
    if (!token) {
      setLoading(false);
      return () => window.removeEventListener('expertconnect:unauthorized', invalidate);
    }
    api.users
      .me()
      .then((me) => {
        if (active) setUser(me);
      })
      .catch(() => {
        if (active) {
          setToken(null);
          setUser(null);
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
      window.removeEventListener('expertconnect:unauthorized', invalidate);
    };
  }, []);

  const login = useCallback(async (email, password) => {
    const result = await api.auth.login({ email, password });
    setToken(result.token);
    const account = {
      id: result.userId,
      name: result.name,
      email: result.email,
      role: result.role,
    };
    setUser(account);
    // Re-fetch from the backend so the role/state is authoritative.
    const me = await api.users.me();
    setUser(me);
    return me;
  }, []);

  const register = useCallback((payload) => api.auth.register(payload), []);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  const refresh = useCallback(async () => {
    try {
      const me = await api.users.me();
      setUser(me);
      return me;
    } catch {
      setToken(null);
      setUser(null);
      return null;
    }
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading,
      isAuthenticated: Boolean(user),
      isAdmin: user?.role === 'ADMIN',
      isExpert: user?.role === 'EXPERT',
      login,
      register,
      logout,
      refresh,
    }),
    [user, loading, login, register, logout, refresh]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}

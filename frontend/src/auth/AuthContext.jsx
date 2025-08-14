import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react';
import axios from 'axios';

const AuthContext = createContext(null);
const STORAGE_KEY = 'jwt';

// Base64URL → JSON 파싱
function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(atob(base64).split('').map(c =>
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export default function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(STORAGE_KEY));
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    const payload = saved ? parseJwt(saved) : null;
    return payload ? { id: payload.userId, email: payload.email, username: payload.username } : null;
  });
  const [ready, setReady] = useState(false); // 초기 복원 완료 여부

  // 앱 부팅 시 localStorage → 상태 복원
  useEffect(() => {
    if (token) {
      localStorage.setItem(STORAGE_KEY, token);
      axios.defaults.headers.common['Authorization'] = token;
      const payload = parseJwt(token);
      setUser(payload
        ? { id: payload.userId, email: payload.email, userRole: payload.userRole, username: payload.username ?? payload.name }
        : null
      );
    } else {
      localStorage.removeItem(STORAGE_KEY);
      delete axios.defaults.headers.common['Authorization'];
      setUser(null);
    }
    setReady(true);
  }, [token]);

  const login = useCallback((newToken) => setToken(newToken), []);
  const logout = useCallback(() => setToken(null), []);

  const value = useMemo(() => ({
    token,
    user,
    isAuthenticated: !!token,
    ready,
    login,
    logout,
  }), [token, user, ready, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);


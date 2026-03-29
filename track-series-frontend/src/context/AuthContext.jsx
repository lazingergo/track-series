import { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext();

function parseTokenPayload(token) {
  if (!token) {
    return null;
  }

  try {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
  } catch {
    return null;
  }
}

function parseUsernameFromToken(token) {
  const payload = parseTokenPayload(token);
  return payload?.sub || '';
}

function isTokenExpired(token) {
  const payload = parseTokenPayload(token);
  if (!payload?.exp) {
    return false;
  }

  const nowInSeconds = Math.floor(Date.now() / 1000);
  return payload.exp <= nowInSeconds;
}

function getInitialToken() {
  const savedToken = localStorage.getItem('token');
  if (!savedToken) {
    return '';
  }

  if (isTokenExpired(savedToken)) {
    localStorage.removeItem('token');
    return '';
  }

  return savedToken;
}

export function AuthProvider({ children }) {
  // check saved tokens
  const initialToken = getInitialToken();
  const [token, setToken] = useState(initialToken);
  const [username, setUsername] = useState(parseUsernameFromToken(initialToken));
  const navigate = useNavigate();

  // login: save the token
  const login = (newToken) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
    setUsername(parseUsernameFromToken(newToken));
    navigate('/');
  };
  // logout: delte the token
  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUsername('');
    navigate('/login');
  };

  return (
    <AuthContext.Provider value={{ token, username, isAuthenticated: !!token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);

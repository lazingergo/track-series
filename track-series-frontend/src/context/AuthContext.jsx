import { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext();

function parseUsernameFromToken(token) {
    if (!token) {
        return '';
    }

    try {
        const payload = token.split('.')[1];
        const decoded = JSON.parse(atob(payload));
        return decoded?.sub || '';
    } catch {
        return '';
    }
}

export function AuthProvider({ children }) {
    // check saved tokens
    const initialToken = localStorage.getItem('token');
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
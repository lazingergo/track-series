import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Tv } from 'lucide-react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [isRegistering, setIsRegistering] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [error, setError] = useState('');

  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      if (isRegistering) {
        const response = await api.post('/auth/register', { username, email, password });
        login(response.data.token);
      } else {
        const response = await api.post('/auth/login', { username, password });
        login(response.data.token);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed. Please check your details.');
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="bg-tvcard p-8 rounded-2xl shadow-xl max-w-md w-full border border-gray-800">
        <div className="flex flex-col items-center mb-8">
          <Tv className="w-12 h-12 text-tvprimary mb-2" />
          <h2 className="text-2xl font-bold">{isRegistering ? 'Create an Account' : 'Welcome Back'}</h2>
        </div>

        {error && <div className="bg-red-500/20 text-red-400 p-3 rounded mb-4 text-sm">{error}</div>}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="bg-[#1e1e1e] border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-tvprimary"
            required
          />

          {}
          {isRegistering && (
            <input
              type="email"
              placeholder="Email Address"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="bg-[#1e1e1e] border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-tvprimary"
              required
            />
          )}

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="bg-[#1e1e1e] border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-tvprimary"
            required
          />

          <button
            type="submit"
            className="bg-tvprimary text-black font-bold py-3 rounded-lg hover:bg-yellow-400 transition mt-2"
          >
            {isRegistering ? 'Sign Up' : 'Log In'}
          </button>
        </form>

        <p className="text-gray-400 text-center mt-6 text-sm">
          {isRegistering ? 'Already have an account?' : "Don't have an account?"}{' '}
          <button
            type="button"
            onClick={() => setIsRegistering(!isRegistering)}
            className="text-tvprimary hover:underline"
          >
            {isRegistering ? 'Log in here' : 'Sign up here'}
          </button>
        </p>
      </div>
    </div>
  );
}

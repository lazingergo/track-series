import { Link, useLocation } from 'react-router-dom';
import { Tv, Search, BarChart2, Home, UserCircle } from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '../context/useAuth';

export default function Navbar() {
  const location = useLocation();
  const { username, logout } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const isActive = (path) => (location.pathname === path ? 'text-tvprimary' : 'text-gray-400 hover:text-white');

  return (
    <nav className="bg-tvcard border-b border-gray-800 p-4 sticky top-0 z-50">
      <div className="max-w-6xl mx-auto flex items-center justify-between">
        {/* Logo and main page */}
        <Link to="/" className="flex items-center gap-2 text-xl font-bold text-white hover:text-tvprimary transition">
          <Tv className="text-tvprimary" />
          TrackSeries
        </Link>

        {/* Menu items */}
        <div className="flex items-center gap-6">
          <Link to="/" className={`flex items-center gap-1 font-medium transition ${isActive('/')}`}>
            <Home size={20} />
            <span className="hidden sm:inline">My Shows</span>
          </Link>

          <Link to="/search" className={`flex items-center gap-1 font-medium transition ${isActive('/search')}`}>
            <Search size={20} />
            <span className="hidden sm:inline">Search</span>
          </Link>

          <Link to="/stats" className={`flex items-center gap-1 font-medium transition ${isActive('/stats')}`}>
            <BarChart2 size={20} />
            <span className="hidden sm:inline">Stats</span>
          </Link>

          <div className="relative">
            <button
              type="button"
              onClick={() => setIsProfileOpen((prev) => !prev)}
              className="flex items-center gap-2 text-gray-300 hover:text-white transition"
            >
              <UserCircle size={20} />
              <span className="hidden sm:inline">{username || 'Profile'}</span>
            </button>

            {isProfileOpen && (
              <div className="absolute right-0 mt-2 w-44 bg-tvcard border border-gray-800 rounded-lg shadow-lg overflow-hidden">
                <Link
                  to="/profile"
                  onClick={() => setIsProfileOpen(false)}
                  className="block px-4 py-2 text-sm text-gray-200 hover:bg-gray-800"
                >
                  Profile
                </Link>
                <button
                  type="button"
                  onClick={logout}
                  className="w-full text-left px-4 py-2 text-sm text-red-300 hover:bg-gray-800"
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

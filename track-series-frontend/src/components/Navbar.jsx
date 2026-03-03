import { Link, useLocation } from 'react-router-dom';
import { Tv, Search, BarChart2, Home } from 'lucide-react';

export default function Navbar() {
  const location = useLocation();

  const isActive = (path) => location.pathname === path ? "text-tvprimary" : "text-gray-400 hover:text-white";

  return (
    <nav className="bg-tvcard border-b border-gray-800 p-4 sticky top-0 z-50">
      <div className="max-w-6xl mx-auto flex items-center justify-between">
        
        {/* Logo and main page */}
        <Link to="/" className="flex items-center gap-2 text-xl font-bold text-white hover:text-tvprimary transition">
          <Tv className="text-tvprimary" />
          TrackSeries
        </Link>

        {/* Menu items */}
        <div className="flex gap-6">
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
        </div>

      </div>
    </nav>
  );
}
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Search from './pages/Search';
import Profile from './pages/Profile';
import SeriesDetails from './pages/SeriesDetails';
import { useAuth } from './context/AuthContext';

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen flex flex-col bg-tvdark">
      {isAuthenticated && <Navbar />}

      <main className="flex-grow max-w-6xl w-full mx-auto p-4 sm:p-6">
        <Routes>
          <Route path="/login" element={!isAuthenticated ? <Login /> : <Navigate to="/" />} />
          <Route path="/" element={isAuthenticated ? <Home /> : <Navigate to="/login" />} />
          <Route path="/search" element={isAuthenticated ? <Search /> : <Navigate to="/login" />} />
          <Route path="/profile" element={isAuthenticated ? <Profile /> : <Navigate to="/login" />} />
          <Route path="/series/:seriesId" element={isAuthenticated ? <SeriesDetails /> : <Navigate to="/login" />} />
          <Route path="/stats" element={isAuthenticated ? <div>Stats Page</div> : <Navigate to="/login" />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import { useAuth } from './context/AuthContext';

function App() {
  const { isAuthenticated } = useAuth(); // Megkérdezzük, van-e token

  return (
    <div className="min-h-screen flex flex-col">
      {/* Csak akkor mutatjuk a menüt, ha be van jelentkezve */}
      {isAuthenticated && <Navbar />}

      <main className="flex-grow max-w-6xl w-full mx-auto p-4 sm:p-6">
        <Routes>
          {/* Publikus útvonal: Login */}
          <Route path="/login" element={!isAuthenticated ? <Login /> : <Navigate to="/" />} />

          {/* Védett útvonalak: Ha nincs bejelentkezve, kidobjuk a loginra */}
          <Route path="/" element={isAuthenticated ? <Home /> : <Navigate to="/login" />} />
          <Route path="/search" element={isAuthenticated ? <div>Search Page</div> : <Navigate to="/login" />} />
          <Route path="/stats" element={isAuthenticated ? <div>Stats Page</div> : <Navigate to="/login" />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
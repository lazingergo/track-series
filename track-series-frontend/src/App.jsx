import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home'; 

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-grow max-w-6xl w-full mx-auto p-4 sm:p-6">
        <Routes>
          <Route path="/" element={<Home />} />
          
          <Route path="/search" element={<div>Search Page</div>} />
          <Route path="/stats" element={<div>Stats Page</div>} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
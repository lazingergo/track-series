import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      {/* A menüsáv mindig legfelül lesz */}
      <Navbar />

      {/*main content*/}
      <main className="flex-grow max-w-6xl w-full mx-auto p-4 sm:p-6">
        <Routes>
          <Route path="/" element={
            <div>
              <h1 className="text-2xl font-bold mb-4">My Collection</h1>
              <p className="text-gray-400">Here we will show your tracked series...</p>
            </div>
          } />
          
          <Route path="/search" element={
            <div>
              <h1 className="text-2xl font-bold mb-4">Discover Shows</h1>
              <p className="text-gray-400">Search input will go here...</p>
            </div>
          } />

          <Route path="/stats" element={
            <div>
              <h1 className="text-2xl font-bold mb-4">Your Statistics</h1>
              <p className="text-gray-400">Charts and graphs will be displayed here...</p>
            </div>
          } />
        </Routes>
      </main>
    </div>
  );
}

export default App;
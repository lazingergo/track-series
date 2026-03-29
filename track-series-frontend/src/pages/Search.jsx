import { useState } from 'react';
import { Search as SearchIcon, Plus, Loader2, Check } from 'lucide-react';
import api from '../api/client';

export default function Search() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [addingId, setAddingId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) {return;}

    setLoading(true);
    setError('');
    setNotice(null);
    try {
      const response = await api.get(`/series/search?q=${encodeURIComponent(query)}`);
      setResults(response.data);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch search results.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddSeries = async (tvMazeId) => {
    setAddingId(tvMazeId);
    setNotice(null);
    try {
      await api.post(`/series/add-to-collection/${tvMazeId}`);
      setResults((prev) => prev.map((show) => (show.tvMazeId === tvMazeId ? { ...show, alreadyAdded: true } : show)));
      setNotice({ type: 'success', message: 'Series successfully added to your collection.' });
    } catch (err) {
      console.error('Error adding series:', err);
      setNotice({ type: 'error', message: err.response?.data?.message || 'Failed to add series.' });
    } finally {
      setAddingId(null);
    }
  };

  return (
    <div className="max-w-5xl mx-auto flex flex-col gap-6">
      <div className="bg-tvcard p-6 rounded-2xl border border-gray-800 shadow-lg">
        <h1 className="text-2xl font-bold mb-4 text-white">Find New Shows</h1>
        <form onSubmit={handleSearch} className="flex relative">
          <input
            type="text"
            placeholder="Search for a TV show..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="w-full bg-[#1e1e1e] border border-gray-700 rounded-lg p-4 pl-12 text-white focus:outline-none focus:border-tvprimary text-lg"
          />
          <SearchIcon className="absolute left-4 top-4 text-gray-400" size={24} />
          <button
            type="submit"
            disabled={loading}
            className="absolute right-2 top-2 bottom-2 bg-tvprimary text-black font-bold px-6 rounded-md hover:bg-yellow-400 transition disabled:opacity-50 min-w-[100px] flex items-center justify-center"
          >
            {loading ? <Loader2 className="animate-spin" size={20} /> : 'Search'}
          </button>
        </form>
      </div>

      {error && <div className="text-red-400 text-center">{error}</div>}
      {notice && (
        <div
          className={`text-center rounded-lg p-3 border ${
            notice.type === 'success'
              ? 'text-green-300 border-green-700 bg-green-900/30'
              : 'text-red-300 border-red-700 bg-red-900/30'
          }`}
        >
          {notice.message}
        </div>
      )}

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {results.map((show) => (
          <div
            key={show.tvMazeId}
            className="bg-tvcard rounded-xl overflow-hidden shadow-lg border border-gray-800 flex flex-col transition hover:scale-[1.02]"
          >
            <div className="aspect-[2/3] w-full bg-black relative">
              <img
                src={show.imageUrl || 'https://via.placeholder.com/210x295?text=No+Poster'}
                alt={show.title}
                className="w-full h-full object-cover"
              />
            </div>

            <div className="p-3 flex flex-col flex-grow justify-between gap-3">
              <div>
                <h3 className="font-bold text-white text-sm line-clamp-2" title={show.title}>
                  {show.title}
                </h3>
                <p className="text-xs text-gray-400 mt-1">
                  {show.releaseDate ? show.releaseDate.substring(0, 4) : 'N/A'}
                </p>
              </div>

              <button
                onClick={() => handleAddSeries(show.tvMazeId)}
                disabled={addingId === show.tvMazeId || show.alreadyAdded}
                className="w-full bg-gray-700 hover:bg-tvprimary hover:text-black text-white py-2 rounded-lg flex items-center justify-center gap-1 transition text-sm font-medium mt-auto disabled:opacity-50"
              >
                {addingId === show.tvMazeId ? (
                  <Loader2 className="animate-spin" size={16} />
                ) : show.alreadyAdded ? (
                  <>
                    <Check size={16} /> Added
                  </>
                ) : (
                  <>
                    <Plus size={16} /> Add Show
                  </>
                )}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

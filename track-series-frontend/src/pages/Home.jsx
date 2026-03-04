import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import UpNextCard from '../components/UpNextCard';

export default function Home() {
  const navigate = useNavigate();
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchUpNext = async () => {
    try {
      const response = await api.get('/collection/up-next');
      const watching = Array.isArray(response.data?.watching) ? response.data.watching : [];
      const planToWatch = Array.isArray(response.data?.planToWatch) ? response.data.planToWatch : [];
      const normalized = [...watching, ...planToWatch].map((item) => ({
        ...item,
        episodeId: item.nextEpisodeId,
      }));

      setShows(normalized);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load your shows.');
    } finally {
      setLoading(false);
    }
  };


  useEffect(() => {
    fetchUpNext();
  }, []);

  // Ez fut le, ha rányomsz a Pipára a kártyán
  const handleMarkWatched = async (episodeId) => {
    try {
      // Meghívjuk a már korábban megírt biztonságos végpontot!
      await api.post(`/episodes/${episodeId}/watch`);
      
      // Sikeres mentés után frissítjük a listát a képernyőn!
      fetchUpNext();
    } catch (err) {
      console.error("Failed to mark as watched", err);
      alert("Failed to mark episode as watched. Please try again.");
    }
  };

  if (loading) {
    return <div className="text-center text-gray-500 mt-10 animate-pulse">Loading your shows...</div>;
  }

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold mb-6 text-white">Up Next to Watch</h1>
      
      {error && <div className="text-red-400 mb-4">{error}</div>}

      {}
      {(!shows || shows.length === 0) ? (
        <div className="text-gray-400 text-center py-12 bg-tvcard rounded-xl border border-gray-800">
          <p className="text-lg">You are all caught up!</p>
          <p className="text-sm mt-2">Search for new shows to add to your collection.</p>
        </div>
      ) : (
        /* list cards*/
        <div className="flex flex-col gap-4">
          {shows.map((item) => (
            <UpNextCard 
              key={item.episodeId} 
              item={item} 
              onMarkWatched={handleMarkWatched}
              onOpenSeries={(seriesId) => navigate(`/series/${seriesId}`)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
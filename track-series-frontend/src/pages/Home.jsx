import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import UpNextCard from '../components/UpNextCard';

export default function Home() {
  const navigate = useNavigate();
  const [groups, setGroups] = useState({
    planToWatch: [],
    watching: [],
    notWatchedForAWhile: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchUpNext = async () => {
    try {
      const response = await api.get('/collection/up-next');
      const watching = Array.isArray(response.data?.watching) ? response.data.watching : [];
      const planToWatch = Array.isArray(response.data?.planToWatch) ? response.data.planToWatch : [];
      const notWatchedForAWhile = Array.isArray(response.data?.notWatchedForAWhile) ? response.data.notWatchedForAWhile : [];

      const normalize = (items) => items.map((item) => ({
        ...item,
        episodeId: item.nextEpisodeId,
      }));

      setGroups({
        planToWatch: normalize(planToWatch),
        watching: normalize(watching),
        notWatchedForAWhile: normalize(notWatchedForAWhile),
      });
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

  const sectionConfig = [
    { key: 'planToWatch', title: "Haven’t Started" },
    { key: 'watching', title: 'Watching' },
    { key: 'notWatchedForAWhile', title: "Haven’t Watched for a While" },
  ];

  const hasAnyShows = sectionConfig.some((section) => (groups[section.key] || []).length > 0);

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold mb-6 text-white">Up Next by Progress</h1>
      
      {error && <div className="text-red-400 mb-4">{error}</div>}

      {!hasAnyShows ? (
        <div className="text-gray-400 text-center py-12 bg-tvcard rounded-xl border border-gray-800">
          <p className="text-lg">You are all caught up!</p>
          <p className="text-sm mt-2">Search for new shows to add to your collection.</p>
        </div>
      ) : (
        <div className="flex flex-col gap-8">
          {sectionConfig.map((section) => {
            const items = groups[section.key] || [];

            if (items.length === 0) {
              return null;
            }

            return (
              <section key={section.key} className="flex flex-col gap-4">
                <h2 className="text-xl font-semibold text-white">{section.title}</h2>
                <div className="flex flex-col gap-4">
                  {items.map((item) => (
                    <UpNextCard
                      key={`${section.key}-${item.episodeId}`}
                      item={item}
                      onMarkWatched={handleMarkWatched}
                      onOpenSeries={(seriesId) => navigate(`/series/${seriesId}`)}
                    />
                  ))}
                </div>
              </section>
            );
          })}
        </div>
      )}
    </div>
  );
}
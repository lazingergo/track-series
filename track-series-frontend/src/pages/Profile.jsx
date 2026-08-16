import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';

export default function Profile() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState({ username: '', series: [] });
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');

  const sectionConfig = [
    { key: 'WATCHING', title: 'Watching' },
    { key: 'PLAN_TO_WATCH', title: 'Plan to Watch' },
    { key: 'COMPLETED', title: 'Completed' },
    { key: 'DROPPED', title: 'Dropped' },
  ];

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/profile/me');
      setProfile({
        username: response.data?.username || '',
        series: Array.isArray(response.data?.series) ? response.data.series : [],
      });
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load profile.');
    } finally {
      setLoading(false);
    }
  };

  const handleRefreshOngoingSeries = async () => {
    try {
      setRefreshing(true);
      setError('');
      await api.post('/collection/refresh-ongoing');
      await fetchProfile();
    } catch (err) {
      console.error(err);
      setError('Failed to refresh ongoing series.');
    } finally {
      setRefreshing(false);
    }
  };

  if (loading) {
    return <div className="text-center text-gray-500 mt-10 animate-pulse">Loading profile...</div>;
  }

  const groupedSeries = sectionConfig.reduce((acc, section) => {
    acc[section.key] = profile.series.filter((show) => show.status === section.key);
    return acc;
  }, {});

  const hasAnySeries = sectionConfig.some((section) => (groupedSeries[section.key] || []).length > 0);

  return (
    <div className="max-w-5xl mx-auto flex flex-col gap-6">
      <div className="bg-tvcard p-6 rounded-2xl border border-gray-800 shadow-lg">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-white">Profile</h1>
            <p className="text-gray-300 mt-2">
              Username: <span className="text-tvprimary font-semibold">{profile.username}</span>
            </p>
          </div>
          <button
            type="button"
            onClick={handleRefreshOngoingSeries}
            disabled={refreshing}
            className="px-3 py-2 rounded-lg bg-tvprimary text-black text-sm font-semibold hover:bg-yellow-400 transition disabled:opacity-50"
          >
            {refreshing ? 'Refreshing...' : 'Refresh Ongoing Series'}
          </button>
        </div>
      </div>

      {error && <div className="text-red-400">{error}</div>}

      {!hasAnySeries ? (
        <div className="text-gray-400 text-center py-12 bg-tvcard rounded-xl border border-gray-800">
          No series in your profile yet.
        </div>
      ) : (
        <div className="flex flex-col gap-8">
          {sectionConfig.map((section) => {
            const items = groupedSeries[section.key] || [];

            if (items.length === 0) {
              return null;
            }

            return (
              <section key={section.key} className="flex flex-col gap-4">
                <h2 className="text-xl font-semibold text-white">{section.title}</h2>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                  {items.map((show) => (
                    <button
                      key={show.seriesId}
                      type="button"
                      onClick={() => navigate(`/series/${show.seriesId}`)}
                      className="bg-tvcard rounded-xl overflow-hidden shadow-lg border border-gray-800 flex flex-col transition hover:scale-[1.02] text-left"
                    >
                      <div className="aspect-[2/3] w-full bg-black">
                        <img
                          src={show.imageUrl || 'https://via.placeholder.com/210x295?text=No+Poster'}
                          alt={show.title}
                          className="w-full h-full object-cover"
                        />
                      </div>

                      <div className="p-3">
                        <h3 className="font-bold text-white text-sm line-clamp-2" title={show.title}>
                          {show.title}
                        </h3>
                        <p className="text-xs text-gray-400 mt-1">{show.status}</p>
                      </div>
                    </button>
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

import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/client';

function formatEpisode(season, episode) {
  return `S${String(season).padStart(2, '0')}E${String(episode).padStart(2, '0')}`;
}

export default function SeriesDetails() {
  const { seriesId } = useParams();
  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [deletingSeries, setDeletingSeries] = useState(false);
  const [refreshingSeries, setRefreshingSeries] = useState(false);
  const [error, setError] = useState('');
  const [watchDialogOpen, setWatchDialogOpen] = useState(false);
  const [selectedEpisode, setSelectedEpisode] = useState(null);
  const [includePrevious, setIncludePrevious] = useState(false);
  const [watchedAt, setWatchedAt] = useState('');

  const fetchDetails = useCallback(async () => {
    try {
      const response = await api.get(`/series/${seriesId}/details`);
      setDetails(response.data);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load series details.');
    } finally {
      setLoading(false);
    }
  }, [seriesId]);

  useEffect(() => {
    fetchDetails();
  }, [fetchDetails]);

  const handleToggleWatched = async (episode) => {
    try {
      if (episode.watched) {
        await api.delete(`/episodes/${episode.id}/watch`);
      } else {
        setSelectedEpisode(episode);
        setIncludePrevious(false);
        setWatchedAt('');
        setWatchDialogOpen(true);
        return;
      }

      fetchDetails();
    } catch (err) {
      console.error(err);
      setError('Failed to update watched status.');
    }
  };

  const submitWatchDialog = async () => {
    if (!selectedEpisode) {
      return;
    }

    try {
      await api.post(`/episodes/${selectedEpisode.id}/watch`, null, {
        params: {
          includePrevious,
          ...(watchedAt ? { watchedAt } : {}),
        },
      });

      setWatchDialogOpen(false);
      setSelectedEpisode(null);
      setIncludePrevious(false);
      setWatchedAt('');
      fetchDetails();
    } catch (err) {
      console.error(err);
      setError('Failed to update watched status.');
    }
  };

  const handleToggleSeriesStatus = async () => {
    if (!details?.userStatus || statusUpdating) {
      return;
    }

    const nextStatus = details.userStatus === 'WATCHING' ? 'DROPPED' : 'WATCHING';

    try {
      setStatusUpdating(true);
      setError('');
      await api.post(`/collection/set-status/${seriesId}/${nextStatus}`);
      fetchDetails();
    } catch (err) {
      console.error(err);
      setError('Failed to update series status.');
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleDeleteSeries = async () => {
    if (!details?.userStatus || deletingSeries) {
      return;
    }

    try {
      setDeletingSeries(true);
      setError('');
      await api.delete(`/collection/${seriesId}`);
      fetchDetails();
    } catch (err) {
      console.error(err);
      setError('Failed to remove series from collection.');
    } finally {
      setDeletingSeries(false);
    }
  };

  const handleRefreshSeries = async () => {
    if (!details?.userStatus || refreshingSeries) {
      return;
    }

    try {
      setRefreshingSeries(true);
      setError('');
      await api.post(`/collection/${seriesId}/refresh`);
      fetchDetails();
    } catch (err) {
      console.error(err);
      setError('Failed to refresh series episodes.');
    } finally {
      setRefreshingSeries(false);
    }
  };

  if (loading) {
    return <div className="text-center text-gray-500 mt-10 animate-pulse">Loading series...</div>;
  }

  if (!details) {
    return <div className="text-red-400">Series not found.</div>;
  }

  const showStatusToggle = details.userStatus === 'WATCHING' || details.userStatus === 'DROPPED';
  const showDeleteButton = Boolean(details.userStatus);

  return (
    <div className="max-w-5xl mx-auto flex flex-col gap-6">
      <div className="bg-tvcard p-6 rounded-2xl border border-gray-800 shadow-lg flex gap-4">
        <img
          src={details.imageUrl || 'https://via.placeholder.com/210x295?text=No+Poster'}
          alt={details.title}
          className="w-32 h-44 object-cover rounded-lg bg-black"
        />
        <div className="flex-1">
          <div className="flex items-start justify-between gap-3">
            <h1 className="text-2xl font-bold text-white">{details.title}</h1>
            <div className="flex items-center gap-2">
              {showStatusToggle && (
                <button
                  type="button"
                  onClick={handleToggleSeriesStatus}
                  disabled={statusUpdating || deletingSeries}
                  className="shrink-0 px-3 py-2 rounded-lg bg-gray-700 hover:bg-tvprimary hover:text-black text-white text-sm font-semibold transition disabled:opacity-50"
                >
                  {statusUpdating
                    ? 'Updating...'
                    : details.userStatus === 'WATCHING'
                      ? 'Stop Watching'
                      : 'Continue Watching'}
                </button>
              )}
              {showDeleteButton && (
                <button
                  type="button"
                  onClick={handleRefreshSeries}
                  disabled={refreshingSeries || deletingSeries || statusUpdating}
                  className="shrink-0 px-3 py-2 rounded-lg bg-tvprimary hover:bg-yellow-400 text-black text-sm font-semibold transition disabled:opacity-50"
                >
                  {refreshingSeries ? 'Refreshing...' : 'Refresh Episodes'}
                </button>
              )}
              {showDeleteButton && (
                <button
                  type="button"
                  onClick={handleDeleteSeries}
                  disabled={deletingSeries || statusUpdating}
                  className="shrink-0 px-3 py-2 rounded-lg bg-red-600 hover:bg-red-500 text-white text-sm font-semibold transition disabled:opacity-50"
                >
                  {deletingSeries ? 'Deleting...' : 'Delete Series'}
                </button>
              )}
            </div>
          </div>
          <p className="text-sm text-gray-400 mt-1">Status: {details.userStatus || 'N/A'}</p>
          <p className="text-gray-300 mt-3 text-sm" dangerouslySetInnerHTML={{ __html: details.summary || '' }} />
        </div>
      </div>

      {error && <div className="text-red-400">{error}</div>}

      <div className="bg-tvcard rounded-2xl border border-gray-800 overflow-hidden">
        <div className="px-5 py-4 border-b border-gray-800">
          <h2 className="text-lg font-semibold text-white">Episodes</h2>
        </div>

        <div className="divide-y divide-gray-800">
          {(details.episodes || []).map((episode) => (
            <button
              type="button"
              key={episode.id}
              onClick={() => handleToggleWatched(episode)}
              className="w-full flex items-center justify-between px-5 py-3 hover:bg-gray-900 transition"
            >
              <div className="text-left">
                <p className="text-sm text-tvprimary font-medium">
                  {formatEpisode(episode.seasonNumber, episode.episodeNumber)}
                </p>
                <p className="text-white text-sm">{episode.title}</p>
              </div>
              <span className={`text-xs font-semibold ${episode.watched ? 'text-green-400' : 'text-gray-400'}`}>
                {episode.watched ? 'WATCHED ✓' : 'NOT WATCHED'}
              </span>
            </button>
          ))}
        </div>
      </div>

      {watchDialogOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-tvcard border border-gray-800 rounded-xl shadow-xl p-5 flex flex-col gap-4">
            <h3 className="text-lg font-semibold text-white">Mark Episode as Watched</h3>

            <p className="text-sm text-gray-300">
              Episode:{' '}
              {selectedEpisode ? formatEpisode(selectedEpisode.seasonNumber, selectedEpisode.episodeNumber) : ''}
            </p>

            <label className="flex items-center gap-2 text-sm text-gray-200">
              <input
                type="checkbox"
                checked={includePrevious}
                onChange={(event) => setIncludePrevious(event.target.checked)}
              />
              I also watched previous episodes
            </label>

            <div>
              <label className="block text-sm text-gray-200 mb-1">Watched date and time (optional)</label>
              <input
                type="datetime-local"
                value={watchedAt}
                onChange={(event) => setWatchedAt(event.target.value)}
                className="w-full bg-[#1e1e1e] border border-gray-700 rounded-lg p-2 text-white focus:outline-none focus:border-tvprimary"
              />
            </div>

            <div className="flex justify-end gap-2 mt-2">
              <button
                type="button"
                onClick={() => {
                  setWatchDialogOpen(false);
                  setSelectedEpisode(null);
                }}
                className="px-4 py-2 rounded-lg bg-gray-700 text-white hover:bg-gray-600 transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={submitWatchDialog}
                className="px-4 py-2 rounded-lg bg-tvprimary text-black font-semibold hover:bg-yellow-400 transition"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

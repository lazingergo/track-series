import { CheckCircle } from 'lucide-react';

export default function UpNextCard({ item, onMarkWatched }) {
  // Segédfüggvény, ami az 1-es évadból és 2-es részből ilyet csinál: S01E02
  const formatEpisode = (season, episode) => {
    return `S${String(season).padStart(2, '0')}E${String(episode).padStart(2, '0')}`;
  };

  return (
    <div className="flex flex-row items-center bg-tvcard rounded-xl overflow-hidden shadow-lg border border-gray-800 transition-transform hover:scale-[1.01]">
      <img 
        src={item.imageUrl || 'https://via.placeholder.com/210x295?text=No+Image'} 
        alt={item.seriesTitle} 
        className="w-24 sm:w-28 h-36 object-cover bg-black"
      />
}
      <div className="flex flex-col flex-grow p-4 min-w-0">
        <h3 className="text-lg font-bold text-white truncate" title={item.seriesTitle}>
          {item.seriesTitle}
        </h3>
        <p className="text-sm text-tvprimary font-medium mt-1 truncate">
          {formatEpisode(item.seasonNumber, item.episodeNumber)} <span className="text-gray-400 font-normal">- {item.episodeTitle}</span>
        </p>
      </div>

      <button 
        onClick={() => onMarkWatched(item.episodeId)}
        className="p-5 text-gray-500 hover:text-green-500 transition-colors cursor-pointer"
        title="Mark as watched"
      >
        <CheckCircle size={30} />
      </button>

    </div>
  );
}
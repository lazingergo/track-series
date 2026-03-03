import { useEffect, useState } from 'react';
import api from '../api/client';

export default function Home() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/collection/up-next')
      .then((response) => {
        setData(response.data);
      })
      .catch((err) => {
        console.error(err);
        setError('Failed to fetch data. Are you logged in?');
      });
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">My Collection</h1>
      
      {/* error */}
      {error && (
        <div className="bg-red-500/20 border border-red-500 text-red-400 p-4 rounded-lg mb-4">
          {error}
        </div>
      )}

      {/* */}
      {data && (
        <pre className="bg-tvcard p-4 rounded-lg overflow-auto text-sm text-green-400">
          {JSON.stringify(data, null, 2)}
        </pre>
      )}
    </div>
  );
}
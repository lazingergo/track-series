import { useEffect, useMemo, useState } from 'react';
import api from '../api/client';

const MONTH_LABELS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export default function Stats() {
  const currentYear = new Date().getFullYear();
  const [year, setYear] = useState(currentYear);
  const [availableYears, setAvailableYears] = useState([]);
  const [monthlyCounts, setMonthlyCounts] = useState(Array(12).fill(0));
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchMonthlyStats = async (targetYear) => {
    try {
      setLoading(true);
      const response = await api.get('/stats/monthly', {
        params: { year: targetYear },
      });

      const data = response.data || {};
      const normalizedCounts = Array.isArray(data.monthlyCounts) && data.monthlyCounts.length === 12
        ? data.monthlyCounts
        : Array(12).fill(0);

      setYear(data.year || targetYear);
      setMonthlyCounts(normalizedCounts);
      setAvailableYears(Array.isArray(data.availableYears) ? data.availableYears : []);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load monthly stats.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMonthlyStats(year);
  }, []);

  const maxCount = useMemo(() => Math.max(...monthlyCounts, 1), [monthlyCounts]);

  const handlePreviousYear = () => {
    const nextYear = year - 1;
    setYear(nextYear);
    fetchMonthlyStats(nextYear);
  };

  const handleNextYear = () => {
    const nextYear = year + 1;
    setYear(nextYear);
    fetchMonthlyStats(nextYear);
  };

  const handleYearChange = (event) => {
    const nextYear = Number(event.target.value);
    setYear(nextYear);
    fetchMonthlyStats(nextYear);
  };

  const yearOptions = availableYears.length > 0
    ? availableYears
    : [year];

  return (
    <div className="max-w-6xl mx-auto flex flex-col gap-6">
      <div className="bg-tvcard p-6 rounded-2xl border border-gray-800 shadow-lg flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Monthly Watching Stats</h1>
          <p className="text-sm text-gray-400 mt-1">12-month breakdown with yearly switch</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={handlePreviousYear}
            className="px-3 py-2 rounded-lg bg-gray-700 text-white hover:bg-gray-600 transition"
          >
            Previous
          </button>

          <select
            value={year}
            onChange={handleYearChange}
            className="bg-[#1e1e1e] border border-gray-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-tvprimary"
          >
            {yearOptions.map((item) => (
              <option key={item} value={item}>{item}</option>
            ))}
          </select>

          <button
            type="button"
            onClick={handleNextYear}
            className="px-3 py-2 rounded-lg bg-gray-700 text-white hover:bg-gray-600 transition"
          >
            Next
          </button>
        </div>
      </div>

      {error && <div className="text-red-400">{error}</div>}

      <div className="bg-tvcard p-6 rounded-2xl border border-gray-800 shadow-lg">
        {loading ? (
          <div className="text-center text-gray-500 animate-pulse py-12">Loading monthly stats...</div>
        ) : (
          <div className="w-full overflow-x-auto">
            <div className="min-w-[720px]">
              <div className="h-72 flex items-end gap-3">
                {monthlyCounts.map((count, index) => {
                  const barHeight = `${(count / maxCount) * 100}%`;
                  return (
                    <div key={MONTH_LABELS[index]} className="flex-1 flex flex-col items-center gap-2">
                      <span className="text-xs text-gray-300">{count}</span>
                      <div className="w-full h-56 bg-gray-900 rounded-md flex items-end p-1">
                        <div
                          className="w-full bg-tvprimary rounded-sm transition-all duration-300"
                          style={{ height: barHeight }}
                        />
                      </div>
                      <span className="text-xs text-gray-400">{MONTH_LABELS[index]}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

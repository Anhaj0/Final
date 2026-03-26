import React, { useEffect, useState } from 'react';
import axios from 'axios';

const ComplaintsAdmin = ({ apiBase }) => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadComplaints = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await axios.get(`${apiBase}/api/admin/complaints`);
        setComplaints(res.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load complaints');
      } finally {
        setLoading(false);
      }
    };

    loadComplaints();
  }, [apiBase]);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-slate-800 dark:text-white">Complaints</h2>
      </div>

      {error && (
        <div className="rounded-xl px-4 py-3 text-sm border bg-rose-50 dark:bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-200 dark:border-rose-500/30">
          {error}
        </div>
      )}

      <div className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl overflow-hidden">
        {loading ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">Loading complaints...</div>
        ) : (
          <table className="w-full text-sm text-left">
            <thead className="bg-slate-50 dark:bg-slate-800/50 text-xs uppercase tracking-wider text-slate-500 dark:text-slate-400">
              <tr>
                <th className="p-3">ID</th>
                <th className="p-3">Passenger Name</th>
                <th className="p-3">Passenger</th>
                <th className="p-3">Bus</th>
                <th className="p-3">Subject</th>
                <th className="p-3">Description</th>
                <th className="p-3">Status</th>
                <th className="p-3">Created At</th>
              </tr>
            </thead>
            <tbody>
              {complaints.length === 0 ? (
                <tr>
                  <td className="p-4 text-slate-400" colSpan={8}>No complaints found.</td>
                </tr>
              ) : complaints.map((c) => (
                <tr key={c.id} className="border-t border-slate-200 dark:border-slate-700">
                  <td className="p-3 font-semibold">{c.id}</td>
                  <td className="p-3">{c.passengerName || (c.passengerUserId ? `User #${c.passengerUserId}` : '-')}</td>
                  <td className="p-3">{c.passengerUserId || '-'}</td>
                  <td className="p-3">{c.busInfo || c.busCode || '-'}</td>
                  <td className="p-3">{c.subject || '-'}</td>
                  <td className="p-3 max-w-md truncate" title={c.description || ''}>{c.description || '-'}</td>
                  <td className="p-3">{c.status || 'OPEN'}</td>
                  <td className="p-3">{c.createdAt || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default ComplaintsAdmin;

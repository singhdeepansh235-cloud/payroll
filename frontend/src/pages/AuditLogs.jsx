import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiSearch, FiShield, FiChevronLeft, FiChevronRight, FiCalendar, FiFilter } from 'react-icons/fi';

const AuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [module, setModule] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const modules = ['EMPLOYEE', 'DEPARTMENT', 'LEAVE', 'PAYROLL', 'ATTENDANCE', 'REPORT', 'AUTH', 'SETTINGS'];

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 12, sort: 'timestamp', direction: 'DESC' };
      if (search) params.search = search;
      if (module) params.module = module;
      if (startDate) params.startDate = startDate;
      if (endDate) params.endDate = endDate;

      const response = await api.get('/audit-logs', { params });
      if (response.data.success) {
        const data = response.data.data;
        if (Array.isArray(data)) {
          setLogs(data);
          setTotalPages(1);
          setTotalElements(data.length);
        } else {
          setLogs(data.content || []);
          setTotalPages(data.totalPages || 1);
          setTotalElements(data.totalElements || 0);
        }
      }
    } catch (err) {
      toast.error('Failed to load audit logs.');
    } finally {
      setLoading(false);
    }
  }, [page, search, module, startDate, endDate]);

  useEffect(() => { fetchLogs(); }, [fetchLogs]);

  const clearFilters = () => {
    setSearch('');
    setModule('');
    setStartDate('');
    setEndDate('');
    setPage(0);
  };

  const moduleBadgeColor = (mod) => {
    const map = {
      EMPLOYEE: 'bg-sky-500/10 text-sky-400 border-sky-500/20',
      DEPARTMENT: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      LEAVE: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      PAYROLL: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20',
      ATTENDANCE: 'bg-teal-500/10 text-teal-400 border-teal-500/20',
      REPORT: 'bg-violet-500/10 text-violet-400 border-violet-500/20',
      AUTH: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
      SETTINGS: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
    };
    return map[mod] || 'bg-slate-500/10 text-slate-400 border-slate-500/20';
  };

  const hasActiveFilters = search || module || startDate || endDate;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">Audit Logs</h2>
          <p className="text-slate-400 text-sm mt-1">System activity trail — track all administrative actions.</p>
        </div>
        {hasActiveFilters && (
          <button onClick={clearFilters} className="flex items-center space-x-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl text-sm text-slate-300 transition-colors">
            <FiFilter className="text-xs" /><span>Clear Filters</span>
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div className="flex items-center bg-[#1e293b]/60 border border-slate-700/50 px-3 py-2.5 rounded-xl">
          <FiSearch className="text-slate-400 mr-2" />
          <input type="text" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} placeholder="Search actions..." className="bg-transparent border-none outline-none w-full text-slate-200 placeholder-slate-500 text-sm" />
        </div>
        <select value={module} onChange={(e) => { setModule(e.target.value); setPage(0); }} className="px-3 py-2.5 bg-[#1e293b]/60 border border-slate-700/50 rounded-xl text-slate-200 outline-none text-sm">
          <option value="">All Modules</option>
          {modules.map(m => <option key={m} value={m}>{m}</option>)}
        </select>
        <div className="flex items-center bg-[#1e293b]/60 border border-slate-700/50 px-3 py-2.5 rounded-xl">
          <FiCalendar className="text-slate-400 mr-2" />
          <input type="date" value={startDate} onChange={(e) => { setStartDate(e.target.value); setPage(0); }} className="bg-transparent border-none outline-none w-full text-slate-200 text-sm" placeholder="Start Date" />
        </div>
        <div className="flex items-center bg-[#1e293b]/60 border border-slate-700/50 px-3 py-2.5 rounded-xl">
          <FiCalendar className="text-slate-400 mr-2" />
          <input type="date" value={endDate} onChange={(e) => { setEndDate(e.target.value); setPage(0); }} className="bg-transparent border-none outline-none w-full text-slate-200 text-sm" placeholder="End Date" />
        </div>
      </div>

      {/* Table */}
      <div className="bg-[#1e293b]/40 border border-slate-700/50 rounded-2xl overflow-hidden backdrop-blur-md">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-700/50 text-slate-400 text-xs font-semibold uppercase tracking-wider bg-[#0f172a]/30">
                <th className="px-5 py-4">Timestamp</th>
                <th className="px-5 py-4">User</th>
                <th className="px-5 py-4">Action</th>
                <th className="px-5 py-4">Module</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/30 text-sm text-slate-300">
              {loading ? (
                <tr><td colSpan="4" className="text-center py-12 text-slate-500">
                  <svg className="animate-spin h-6 w-6 mx-auto text-sky-400 mb-2" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                  Loading audit logs...
                </td></tr>
              ) : logs.length === 0 ? (
                <tr><td colSpan="4" className="text-center py-12 text-slate-500">No audit logs found matching filters.</td></tr>
              ) : (
                logs.map((log, idx) => (
                  <tr key={log.id || idx} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-5 py-4 text-xs text-slate-400 font-mono whitespace-nowrap">
                      {log.timestamp ? new Date(log.timestamp).toLocaleString() : 'N/A'}
                    </td>
                    <td className="px-5 py-4 font-medium text-slate-200">{log.username}</td>
                    <td className="px-5 py-4 text-slate-300 max-w-[300px] truncate">{log.action}</td>
                    <td className="px-5 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-medium border ${moduleBadgeColor(log.module)}`}>{log.module}</span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-5 py-4 bg-[#0f172a]/20 border-t border-slate-700/50 flex items-center justify-between">
            <span className="text-xs text-slate-400">Page {page + 1} of {totalPages} ({totalElements} total)</span>
            <div className="flex items-center space-x-2">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 disabled:opacity-30 transition-colors"><FiChevronLeft /></button>
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                const start = Math.max(0, Math.min(page - 2, totalPages - 5));
                const p = start + i;
                return (
                  <button key={p} onClick={() => setPage(p)} className={`w-8 h-8 rounded-lg text-xs font-medium transition-colors ${p === page ? 'bg-sky-500 text-white' : 'bg-slate-800 hover:bg-slate-700 text-slate-300'}`}>{p + 1}</button>
                );
              })}
              <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 disabled:opacity-30 transition-colors"><FiChevronRight /></button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AuditLogs;

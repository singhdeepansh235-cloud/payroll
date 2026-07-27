import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Pagination from '../components/Pagination';
import { FiSearch, FiCalendar, FiFilter } from 'react-icons/fi';

const modules = ['EMPLOYEE','DEPARTMENT','LEAVE','PAYROLL','ATTENDANCE','REPORT','AUTH','SETTINGS'];

const moduleBadge = mod => {
  const m = { EMPLOYEE: 'badge-blue', DEPARTMENT: 'badge-blue', LEAVE: 'badge-amber', PAYROLL: 'badge-green', AUTH: 'badge-red', ATTENDANCE: 'badge-slate', REPORT: 'badge-slate', SETTINGS: 'badge-slate' };
  return m[mod] || 'badge-slate';
};

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

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 15, sort: 'timestamp', direction: 'DESC' };
      if (search) params.search = search;
      if (module) params.module = module;
      if (startDate) params.startDate = startDate;
      if (endDate) params.endDate = endDate;

      const r = await api.get('/audit-logs', { params });
      if (r.data.success) {
        const d = r.data.data;
        if (Array.isArray(d)) { setLogs(d); setTotalPages(1); setTotalElements(d.length); }
        else { setLogs(d.content || []); setTotalPages(d.totalPages || 1); setTotalElements(d.totalElements || 0); }
      }
    } catch { toast.error('Failed to load audit logs.'); }
    finally { setLoading(false); }
  }, [page, search, module, startDate, endDate]);

  useEffect(() => { fetchLogs(); }, [fetchLogs]);

  const clearFilters = () => { setSearch(''); setModule(''); setStartDate(''); setEndDate(''); setPage(0); };
  const hasFilters = search || module || startDate || endDate;

  return (
    <div className="space-y-6 fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="page-title">Audit Logs</h2>
          <p className="page-subtitle">A full activity trail of all administrative actions.</p>
        </div>
        {hasFilters && (
          <button onClick={clearFilters} className="btn-secondary space-x-1.5">
            <FiFilter className="text-xs" /><span>Clear Filters</span>
          </button>
        )}
      </div>

      {/* Filters Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div className="relative">
          <FiSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
          <input type="text" value={search} onChange={e => { setSearch(e.target.value); setPage(0); }} placeholder="Search actions, users..." className="form-input pl-9" />
        </div>
        <select value={module} onChange={e => { setModule(e.target.value); setPage(0); }} className="form-input">
          <option value="">All Modules</option>
          {modules.map(m => <option key={m} value={m}>{m}</option>)}
        </select>
        <div className="relative">
          <FiCalendar className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
          <input type="date" value={startDate} onChange={e => { setStartDate(e.target.value); setPage(0); }} className="form-input pl-9" />
        </div>
        <div className="relative">
          <FiCalendar className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
          <input type="date" value={endDate} onChange={e => { setEndDate(e.target.value); setPage(0); }} className="form-input pl-9" />
        </div>
      </div>

      {/* Table */}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead className="border-b border-slate-100">
              <tr>
                {['Timestamp', 'User', 'Action', 'Module'].map(h => <th key={h} className="table-header">{h}</th>)}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {loading ? (
                <tr><td colSpan={4} className="py-12 text-center text-slate-400 text-sm">Loading audit logs...</td></tr>
              ) : logs.length === 0 ? (
                <tr><td colSpan={4} className="py-12 text-center text-slate-400 text-sm">No logs found matching your filters.</td></tr>
              ) : logs.map((log, i) => (
                <tr key={log.id || i} className="hover:bg-slate-50/60 transition-colors">
                  <td className="table-cell text-xs text-slate-500 font-mono whitespace-nowrap">
                    {log.timestamp ? new Date(log.timestamp).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—'}
                  </td>
                  <td className="table-cell font-medium text-slate-700">{log.username}</td>
                  <td className="table-cell text-slate-600 max-w-[300px] truncate">{log.action}</td>
                  <td className="table-cell"><span className={moduleBadge(log.module)}>{log.module}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
      </div>
    </div>
  );
};

export default AuditLogs;

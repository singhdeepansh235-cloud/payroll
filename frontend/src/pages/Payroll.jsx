import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiChevronLeft, FiChevronRight, FiPlay, FiDownload, FiSearch, FiX } from 'react-icons/fi';

const Modal = ({ open, onClose, title, children }) => {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
      <div className="bg-[#151f32] border border-slate-700/50 rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-700/50">
          <h3 className="text-lg font-bold text-slate-100">{title}</h3>
          <button onClick={onClose} className="p-1.5 hover:bg-slate-700/50 rounded-lg transition-colors text-slate-400 hover:text-slate-200"><FiX /></button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
};

const Payroll = () => {
  const [payrollRecords, setPayrollRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Generate payroll modal
  const [showGenerate, setShowGenerate] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [genForm, setGenForm] = useState({ employeeId: '', month: '', year: '' });
  const [generating, setGenerating] = useState(false);

  const fetchPayroll = useCallback(async () => {
    setLoading(true);
    try {
      const response = await api.get('/payroll/paginated', {
        params: { search, page, size: 8, sort: 'payrollId', direction: 'DESC' },
      });
      if (response.data.success) {
        setPayrollRecords(response.data.data.content || []);
        setTotalPages(response.data.data.totalPages || 1);
        setTotalElements(response.data.data.totalElements || 0);
      }
    } catch (err) {
      toast.error('Failed to load payroll records.');
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  const fetchEmployees = async () => {
    try {
      const res = await api.get('/employees');
      if (res.data.success) setEmployees(res.data.data);
    } catch (err) { /* silent */ }
  };

  useEffect(() => { fetchPayroll(); }, [fetchPayroll]);
  useEffect(() => { fetchEmployees(); }, []);

  const handleGenerate = async (e) => {
    e.preventDefault();
    setGenerating(true);
    try {
      await api.post('/payroll/generate', null, {
        params: { employeeId: genForm.employeeId, month: genForm.month, year: genForm.year },
      });
      toast.success('Payroll generated successfully!');
      setShowGenerate(false);
      fetchPayroll();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to generate payroll.');
    } finally {
      setGenerating(false);
    }
  };

  const handleDownloadPayslip = (payrollId) => {
    toast.success('Downloading payslip...');
    window.open(`http://localhost:8080/api/payroll/${payrollId}/payslip`, '_blank');
  };

  const currentYear = new Date().getFullYear();
  const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">Payroll</h2>
          <p className="text-slate-400 text-sm mt-1">{totalElements} payroll records generated.</p>
        </div>
        <button onClick={() => setShowGenerate(true)} className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] text-sm">
          <FiPlay /><span>Generate Payroll</span>
        </button>
      </div>

      {/* Search Filter */}
      <div className="flex items-center bg-[#1e293b]/60 border border-slate-700/50 px-4 py-3 rounded-2xl">
        <FiSearch className="text-slate-400 mr-3 text-lg" />
        <input type="text" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} placeholder="Search by employee name, month..." className="bg-transparent border-none outline-none w-full text-slate-200 placeholder-slate-500 text-sm" />
      </div>

      {/* Table Card */}
      <div className="bg-[#1e293b]/40 border border-slate-700/50 rounded-2xl overflow-hidden backdrop-blur-md">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-700/50 text-slate-400 text-xs font-semibold uppercase tracking-wider bg-[#0f172a]/30">
                <th className="px-5 py-4">Employee</th>
                <th className="px-5 py-4">Month</th>
                <th className="px-5 py-4">Gross Salary</th>
                <th className="px-5 py-4">Deductions</th>
                <th className="px-5 py-4">Net Salary</th>
                <th className="px-5 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/30 text-sm text-slate-300">
              {loading ? (
                <tr><td colSpan="6" className="text-center py-12 text-slate-500">
                  <svg className="animate-spin h-6 w-6 mx-auto text-sky-400 mb-2" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                  Loading payroll records...
                </td></tr>
              ) : payrollRecords.length === 0 ? (
                <tr><td colSpan="6" className="text-center py-12 text-slate-500">No payroll records found.</td></tr>
              ) : (
                payrollRecords.map((record) => (
                  <tr key={record.payrollId} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-5 py-4 font-semibold text-slate-200">{record.employeeName}</td>
                    <td className="px-5 py-4 text-slate-400">{record.payrollMonth}</td>
                    <td className="px-5 py-4 font-medium text-emerald-400">₹{(record.grossSalary || 0).toLocaleString()}</td>
                    <td className="px-5 py-4 font-medium text-rose-400">₹{(record.deductions || 0).toLocaleString()}</td>
                    <td className="px-5 py-4 font-bold text-slate-200">₹{(record.netSalary || 0).toLocaleString()}</td>
                    <td className="px-5 py-4 text-right">
                      <button onClick={() => handleDownloadPayslip(record.payrollId)} className="inline-flex items-center space-x-1 px-3 py-1.5 bg-sky-500/10 hover:bg-sky-500/20 border border-sky-500/20 text-sky-400 rounded-lg text-xs font-semibold transition-all">
                        <FiDownload /><span>Payslip</span>
                      </button>
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

      {/* Generate Payroll Modal */}
      <Modal open={showGenerate} onClose={() => setShowGenerate(false)} title="Generate Payroll">
        <form onSubmit={handleGenerate} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Employee</label>
            <select value={genForm.employeeId} onChange={(e) => setGenForm({ ...genForm, employeeId: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
              <option value="">Select Employee</option>
              {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Month</label>
              <select value={genForm.month} onChange={(e) => setGenForm({ ...genForm, month: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
                <option value="">Select Month</option>
                {months.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Year</label>
              <select value={genForm.year} onChange={(e) => setGenForm({ ...genForm, year: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
                <option value="">Select Year</option>
                {[currentYear - 1, currentYear, currentYear + 1].map(y => <option key={y} value={y}>{y}</option>)}
              </select>
            </div>
          </div>
          <div className="flex justify-end space-x-3 pt-2">
            <button type="button" onClick={() => setShowGenerate(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
            <button type="submit" disabled={generating} className="px-5 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98] disabled:opacity-50">
              {generating ? 'Generating...' : 'Generate Payroll'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Payroll;

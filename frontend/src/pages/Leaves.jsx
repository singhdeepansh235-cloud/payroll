import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiCheck, FiX, FiPlus, FiCalendar, FiChevronLeft, FiChevronRight } from 'react-icons/fi';

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

const Leaves = () => {
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('PENDING');
  const [showApply, setShowApply] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState({
    employeeId: '', leaveType: 'SICK', startDate: '', endDate: '', reason: '',
  });

  const fetchLeaves = async () => {
    setLoading(true);
    try {
      let endpoint = '/leaves';
      if (tab === 'PENDING') endpoint = '/leaves/pending';
      else if (tab === 'ALL') endpoint = '/leaves';
      const response = await api.get(endpoint);
      if (response.data.success) setLeaves(response.data.data);
    } catch (err) {
      toast.error('Failed to load leave requests.');
    } finally {
      setLoading(false);
    }
  };

  const fetchEmployees = async () => {
    try {
      const res = await api.get('/employees');
      if (res.data.success) setEmployees(res.data.data);
    } catch (err) { /* silent */ }
  };

  useEffect(() => { fetchLeaves(); }, [tab]);
  useEffect(() => { fetchEmployees(); }, []);

  const handleUpdate = async (id, status) => {
    try {
      await api.patch(`/leaves/${id}/status`, { status, adminRemarks: 'Processed via admin dashboard.' });
      toast.success(`Leave request ${status.toLowerCase()} successfully!`);
      fetchLeaves();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update leave status.');
    }
  };

  const handleApply = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/leaves', form);
      toast.success('Leave request submitted successfully!');
      setShowApply(false);
      setForm({ employeeId: '', leaveType: 'SICK', startDate: '', endDate: '', reason: '' });
      fetchLeaves();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to apply leave.');
    } finally {
      setSubmitting(false);
    }
  };

  const statusBadge = (status) => {
    const map = {
      PENDING: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      APPROVED: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      REJECTED: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
    };
    return map[status] || map.PENDING;
  };

  const tabs = [
    { key: 'PENDING', label: 'Pending' },
    { key: 'ALL', label: 'All Requests' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">Leave Management</h2>
          <p className="text-slate-400 text-sm mt-1">Review and manage employee leave requests.</p>
        </div>
        <button onClick={() => setShowApply(true)} className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] text-sm">
          <FiPlus /><span>Apply Leave</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex space-x-1 bg-[#1e293b]/40 p-1 rounded-xl w-fit border border-slate-700/30">
        {tabs.map((t) => (
          <button key={t.key} onClick={() => setTab(t.key)} className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${tab === t.key ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/20' : 'text-slate-400 hover:text-slate-200'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="bg-[#1e293b]/40 border border-slate-700/50 rounded-2xl overflow-hidden backdrop-blur-md">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-700/50 text-slate-400 text-xs font-semibold uppercase tracking-wider bg-[#0f172a]/30">
                <th className="px-5 py-4">Employee</th>
                <th className="px-5 py-4">Type</th>
                <th className="px-5 py-4">Duration</th>
                <th className="px-5 py-4">Days</th>
                <th className="px-5 py-4">Reason</th>
                <th className="px-5 py-4">Status</th>
                {tab === 'PENDING' && <th className="px-5 py-4 text-right">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/30 text-sm text-slate-300">
              {loading ? (
                <tr><td colSpan="7" className="text-center py-12 text-slate-500">Loading...</td></tr>
              ) : leaves.length === 0 ? (
                <tr><td colSpan="7" className="text-center py-12 text-slate-500">No leave requests found.</td></tr>
              ) : (
                leaves.map((leave) => (
                  <tr key={leave.leaveId} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-5 py-4 font-semibold text-slate-200">{leave.employeeName}</td>
                    <td className="px-5 py-4">
                      <span className="px-2 py-0.5 bg-sky-500/10 text-sky-400 border border-sky-500/20 rounded-full text-xs font-medium">{leave.leaveType}</span>
                    </td>
                    <td className="px-5 py-4 text-xs text-slate-400">{leave.startDate} → {leave.endDate}</td>
                    <td className="px-5 py-4 font-medium">{leave.totalDays}</td>
                    <td className="px-5 py-4 text-slate-400 max-w-[200px] truncate">{leave.reason || '-'}</td>
                    <td className="px-5 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-medium border ${statusBadge(leave.status)}`}>{leave.status}</span>
                    </td>
                    {tab === 'PENDING' && (
                      <td className="px-5 py-4 text-right">
                        <div className="flex items-center justify-end space-x-2">
                          <button onClick={() => handleUpdate(leave.leaveId, 'APPROVED')} className="flex items-center space-x-1 px-3 py-1.5 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg text-xs font-medium transition-colors">
                            <FiCheck className="text-xs" /><span>Approve</span>
                          </button>
                          <button onClick={() => handleUpdate(leave.leaveId, 'REJECTED')} className="flex items-center space-x-1 px-3 py-1.5 bg-rose-500 hover:bg-rose-600 text-white rounded-lg text-xs font-medium transition-colors">
                            <FiX className="text-xs" /><span>Reject</span>
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Apply Leave Modal */}
      <Modal open={showApply} onClose={() => setShowApply(false)} title="Apply for Leave">
        <form onSubmit={handleApply} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Employee</label>
            <select value={form.employeeId} onChange={(e) => setForm({ ...form, employeeId: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
              <option value="">Select Employee</option>
              {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Leave Type</label>
            <select value={form.leaveType} onChange={(e) => setForm({ ...form, leaveType: e.target.value })} className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
              <option value="SICK">Sick Leave</option>
              <option value="CASUAL">Casual Leave</option>
              <option value="EARNED">Earned Leave</option>
              <option value="MATERNITY">Maternity Leave</option>
              <option value="UNPAID">Unpaid Leave</option>
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Start Date</label>
              <input type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">End Date</label>
              <input type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm" />
            </div>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Reason</label>
            <textarea value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 placeholder-slate-500 outline-none transition-all text-sm h-20 resize-none" placeholder="Reason for leave..." />
          </div>
          <div className="flex justify-end space-x-3 pt-2">
            <button type="button" onClick={() => setShowApply(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
            <button type="submit" disabled={submitting} className="px-5 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98] disabled:opacity-50">
              {submitting ? 'Submitting...' : 'Submit Leave'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Leaves;

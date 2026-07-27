import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Modal from '../components/Modal';
import { FiCheck, FiX, FiPlus } from 'react-icons/fi';

const statusMap = { PENDING: 'badge-amber', APPROVED: 'badge-green', REJECTED: 'badge-red' };

const Leaves = () => {
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('PENDING');
  const [showApply, setShowApply] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({ employeeId: '', leaveType: 'SICK', startDate: '', endDate: '', reason: '' });

  const fetchLeaves = async () => {
    setLoading(true);
    try {
      const ep = tab === 'PENDING' ? '/leaves/pending' : '/leaves';
      const r = await api.get(ep);
      if (r.data.success) setLeaves(r.data.data);
    } catch { toast.error('Failed to load leaves.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchLeaves(); }, [tab]);
  useEffect(() => { api.get('/employees').then(r => { if (r.data.success) setEmployees(r.data.data); }).catch(() => {}); }, []);

  const handleApproval = async (id, status) => {
    try {
      await api.patch(`/leaves/${id}/status`, { status, adminRemarks: 'Processed via admin dashboard.' });
      toast.success(`Leave ${status.toLowerCase()} successfully.`);
      fetchLeaves();
    } catch (err) { toast.error(err.response?.data?.message || 'Action failed.'); }
  };

  const handleApply = async e => {
    e.preventDefault(); setSubmitting(true);
    try {
      await api.post('/leaves', form);
      toast.success('Leave request submitted.');
      setShowApply(false);
      setForm({ employeeId: '', leaveType: 'SICK', startDate: '', endDate: '', reason: '' });
      fetchLeaves();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to apply leave.'); }
    finally { setSubmitting(false); }
  };

  const tabs = [{ key: 'PENDING', label: 'Pending Approvals' }, { key: 'ALL', label: 'All Requests' }];

  return (
    <div className="space-y-6 fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="page-title">Leave Management</h2>
          <p className="page-subtitle">Review and manage employee leave applications.</p>
        </div>
        <button onClick={() => setShowApply(true)} className="btn-primary space-x-1.5">
          <FiPlus className="text-sm" /><span>Apply Leave</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex space-x-1 bg-white border border-slate-200 rounded-lg p-1 w-fit shadow-sm">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${tab === t.key ? 'bg-[#2d4a8a] text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead className="border-b border-slate-100">
              <tr>
                {['Employee', 'Type', 'Duration', 'Days', 'Reason', 'Status', ...(tab === 'PENDING' ? ['Actions'] : [])].map(h => (
                  <th key={h} className="table-header">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {loading ? (
                <tr><td colSpan={7} className="py-12 text-center text-slate-400 text-sm">Loading...</td></tr>
              ) : leaves.length === 0 ? (
                <tr><td colSpan={7} className="py-12 text-center text-slate-400 text-sm">No leave requests found.</td></tr>
              ) : leaves.map(l => (
                <tr key={l.leaveId} className="hover:bg-slate-50/60 transition-colors">
                  <td className="table-cell font-medium text-slate-800">{l.employeeName}</td>
                  <td className="table-cell"><span className="badge-blue">{l.leaveType}</span></td>
                  <td className="table-cell text-slate-500 text-xs whitespace-nowrap">{l.startDate} → {l.endDate}</td>
                  <td className="table-cell font-medium text-slate-700">{l.totalDays}</td>
                  <td className="table-cell text-slate-500 max-w-[180px] truncate text-xs">{l.reason || '—'}</td>
                  <td className="table-cell"><span className={statusMap[l.status] || 'badge-slate'}>{l.status}</span></td>
                  {tab === 'PENDING' && (
                    <td className="table-cell">
                      <div className="flex items-center space-x-1.5">
                        <button onClick={() => handleApproval(l.leaveId, 'APPROVED')}
                          className="flex items-center space-x-1 px-2.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-medium transition-colors">
                          <FiCheck className="text-xs" /><span>Approve</span>
                        </button>
                        <button onClick={() => handleApproval(l.leaveId, 'REJECTED')}
                          className="flex items-center space-x-1 px-2.5 py-1.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-medium transition-colors">
                          <FiX className="text-xs" /><span>Reject</span>
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Apply Modal */}
      <Modal open={showApply} onClose={() => setShowApply(false)} title="Apply for Leave" size="md">
        <form onSubmit={handleApply} className="space-y-4">
          <div>
            <label className="form-label">Employee</label>
            <select value={form.employeeId} onChange={e => setForm({ ...form, employeeId: e.target.value })} required className="form-input">
              <option value="">Select employee</option>
              {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
            </select>
          </div>
          <div>
            <label className="form-label">Leave Type</label>
            <select value={form.leaveType} onChange={e => setForm({ ...form, leaveType: e.target.value })} className="form-input">
              {['SICK', 'CASUAL', 'EARNED', 'MATERNITY', 'UNPAID'].map(t => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div><label className="form-label">Start Date</label><input type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })} required className="form-input" /></div>
            <div><label className="form-label">End Date</label><input type="date" value={form.endDate} onChange={e => setForm({ ...form, endDate: e.target.value })} required className="form-input" /></div>
          </div>
          <div>
            <label className="form-label">Reason</label>
            <textarea value={form.reason} onChange={e => setForm({ ...form, reason: e.target.value })} className="form-input h-20 resize-none" placeholder="Reason for leave..." />
          </div>
          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setShowApply(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={submitting} className="btn-primary">{submitting ? 'Submitting...' : 'Submit Leave'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Leaves;

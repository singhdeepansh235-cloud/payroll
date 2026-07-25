import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiCalendar, FiSearch, FiPlus, FiX, FiChevronLeft, FiChevronRight } from 'react-icons/fi';

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

const Attendance = () => {
  const [attendance, setAttendance] = useState([]);
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(true);
  const [showMarkForm, setShowMarkForm] = useState(false);
  const [employees, setEmployees] = useState([]);

  // Mark attendance form
  const [markForm, setMarkForm] = useState({
    employeeId: '',
    date: new Date().toISOString().split('T')[0],
    checkIn: '09:00',
    checkOut: '18:00',
    attendanceStatus: 'PRESENT',
  });
  const [submitting, setSubmitting] = useState(false);

  const fetchAttendance = async () => {
    setLoading(true);
    try {
      const response = await api.get('/attendance/date', { params: { date } });
      if (response.data.success) setAttendance(response.data.data);
    } catch (err) {
      toast.error('Failed to load attendance records.');
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

  useEffect(() => { fetchAttendance(); }, [date]);
  useEffect(() => { fetchEmployees(); }, []);

  const handleMarkAttendance = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/attendance', markForm);
      toast.success('Attendance marked successfully!');
      setShowMarkForm(false);
      if (markForm.date === date) fetchAttendance();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to mark attendance.');
    } finally {
      setSubmitting(false);
    }
  };

  const statusBadge = (status) => {
    const map = {
      PRESENT: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      ABSENT: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
      HALF_DAY: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      ON_LEAVE: 'bg-sky-500/10 text-sky-400 border-sky-500/20',
    };
    return map[status] || map.PRESENT;
  };

  const changeDate = (offset) => {
    const d = new Date(date);
    d.setDate(d.getDate() + offset);
    setDate(d.toISOString().split('T')[0]);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">Attendance</h2>
          <p className="text-slate-400 text-sm mt-1">Daily attendance records for all employees.</p>
        </div>
        <button onClick={() => { setMarkForm({ ...markForm, date }); setShowMarkForm(true); }} className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] text-sm">
          <FiPlus /><span>Mark Attendance</span>
        </button>
      </div>

      {/* Date Selector */}
      <div className="flex items-center space-x-3">
        <button onClick={() => changeDate(-1)} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-colors"><FiChevronLeft /></button>
        <div className="flex items-center space-x-2 bg-[#1e293b] border border-slate-700 px-4 py-2.5 rounded-xl">
          <FiCalendar className="text-sky-400" />
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} className="bg-transparent border-none outline-none text-slate-200 text-sm cursor-pointer" />
        </div>
        <button onClick={() => changeDate(1)} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-colors"><FiChevronRight /></button>
        <span className="text-xs text-slate-400 ml-2">{new Date(date).toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
      </div>

      {/* Table */}
      <div className="bg-[#1e293b]/40 border border-slate-700/50 rounded-2xl overflow-hidden backdrop-blur-md">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-700/50 text-slate-400 text-xs font-semibold uppercase tracking-wider bg-[#0f172a]/30">
                <th className="px-6 py-4">Employee</th>
                <th className="px-6 py-4">Check In</th>
                <th className="px-6 py-4">Check Out</th>
                <th className="px-6 py-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/30 text-sm text-slate-300">
              {loading ? (
                <tr><td colSpan="4" className="text-center py-12 text-slate-500">
                  <svg className="animate-spin h-6 w-6 mx-auto text-sky-400 mb-2" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                  Loading records...
                </td></tr>
              ) : attendance.length === 0 ? (
                <tr><td colSpan="4" className="text-center py-12 text-slate-500">No attendance records for this date.</td></tr>
              ) : (
                attendance.map((att) => (
                  <tr key={att.attendanceId} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-6 py-4 font-semibold text-slate-200">{att.employeeName}</td>
                    <td className="px-6 py-4 text-slate-400 font-mono text-xs">{att.checkIn || '--:--'}</td>
                    <td className="px-6 py-4 text-slate-400 font-mono text-xs">{att.checkOut || '--:--'}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-medium border ${statusBadge(att.attendanceStatus)}`}>
                        {att.attendanceStatus}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Summary bar */}
        {!loading && attendance.length > 0 && (
          <div className="px-6 py-3 bg-[#0f172a]/20 border-t border-slate-700/50 flex items-center space-x-6 text-xs text-slate-400">
            <span>Total: <strong className="text-slate-200">{attendance.length}</strong></span>
            <span>Present: <strong className="text-emerald-400">{attendance.filter(a => a.attendanceStatus === 'PRESENT').length}</strong></span>
            <span>Absent: <strong className="text-rose-400">{attendance.filter(a => a.attendanceStatus === 'ABSENT').length}</strong></span>
          </div>
        )}
      </div>

      {/* Mark Attendance Modal */}
      <Modal open={showMarkForm} onClose={() => setShowMarkForm(false)} title="Mark Attendance">
        <form onSubmit={handleMarkAttendance} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Employee</label>
            <select value={markForm.employeeId} onChange={(e) => setMarkForm({ ...markForm, employeeId: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
              <option value="">Select Employee</option>
              {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Date</label>
            <input type="date" value={markForm.date} onChange={(e) => setMarkForm({ ...markForm, date: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Check In</label>
              <input type="time" value={markForm.checkIn} onChange={(e) => setMarkForm({ ...markForm, checkIn: e.target.value })} className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Check Out</label>
              <input type="time" value={markForm.checkOut} onChange={(e) => setMarkForm({ ...markForm, checkOut: e.target.value })} className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm" />
            </div>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Status</label>
            <select value={markForm.attendanceStatus} onChange={(e) => setMarkForm({ ...markForm, attendanceStatus: e.target.value })} className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
              <option value="PRESENT">Present</option>
              <option value="ABSENT">Absent</option>
              <option value="HALF_DAY">Half Day</option>
              <option value="ON_LEAVE">On Leave</option>
            </select>
          </div>
          <div className="flex justify-end space-x-3 pt-2">
            <button type="button" onClick={() => setShowMarkForm(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
            <button type="submit" disabled={submitting} className="px-5 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98] disabled:opacity-50">
              {submitting ? 'Saving...' : 'Mark Attendance'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Attendance;

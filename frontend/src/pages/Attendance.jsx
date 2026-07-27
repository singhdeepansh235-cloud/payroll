import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Modal from '../components/Modal';
import { FiCalendar, FiPlus, FiChevronLeft, FiChevronRight } from 'react-icons/fi';

const statusMap = {
  PRESENT:  'badge-green',
  ABSENT:   'badge-red',
  HALF_DAY: 'badge-amber',
  ON_LEAVE: 'badge-blue',
};

const Attendance = () => {
  const [attendance, setAttendance] = useState([]);
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(true);
  const [showMark, setShowMark] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [markForm, setMarkForm] = useState({ employeeId: '', date: new Date().toISOString().split('T')[0], checkIn: '09:00', checkOut: '18:00', attendanceStatus: 'PRESENT' });
  const [submitting, setSubmitting] = useState(false);

  const fetchAttendance = async () => {
    setLoading(true);
    try { const r = await api.get('/attendance/date', { params: { date } }); if (r.data.success) setAttendance(r.data.data); }
    catch { toast.error('Failed to load attendance.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchAttendance(); }, [date]);
  useEffect(() => { api.get('/employees').then(r => { if (r.data.success) setEmployees(r.data.data); }).catch(() => {}); }, []);

  const changeDate = d => { const nd = new Date(date); nd.setDate(nd.getDate() + d); setDate(nd.toISOString().split('T')[0]); };

  const handleMark = async e => {
    e.preventDefault(); setSubmitting(true);
    try { await api.post('/attendance', markForm); toast.success('Attendance recorded.'); setShowMark(false); if (markForm.date === date) fetchAttendance(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed.'); }
    finally { setSubmitting(false); }
  };

  const present  = attendance.filter(a => a.attendanceStatus === 'PRESENT').length;
  const absent   = attendance.filter(a => a.attendanceStatus === 'ABSENT').length;

  return (
    <div className="space-y-6 fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="page-title">Attendance</h2>
          <p className="page-subtitle">Daily attendance logs for all employees.</p>
        </div>
        <button onClick={() => { setMarkForm({ ...markForm, date }); setShowMark(true); }} className="btn-primary space-x-1.5">
          <FiPlus className="text-sm" /><span>Mark Attendance</span>
        </button>
      </div>

      {/* Date Navigator */}
      <div className="flex items-center space-x-3">
        <button onClick={() => changeDate(-1)} className="btn-secondary !px-2.5 !py-2.5"><FiChevronLeft className="text-sm" /></button>
        <div className="flex items-center space-x-2 bg-white border border-slate-200 rounded-lg px-4 py-2.5 shadow-sm">
          <FiCalendar className="text-slate-400 text-sm" />
          <input type="date" value={date} onChange={e => setDate(e.target.value)} className="text-sm font-medium text-slate-700 outline-none bg-transparent cursor-pointer" />
        </div>
        <button onClick={() => changeDate(1)} className="btn-secondary !px-2.5 !py-2.5"><FiChevronRight className="text-sm" /></button>
        <span className="text-sm text-slate-500">{new Date(date + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}</span>
      </div>

      {/* Summary */}
      {!loading && attendance.length > 0 && (
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2 px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm">
            <span className="font-semibold text-slate-700">{attendance.length}</span><span className="text-slate-400">Total</span>
          </div>
          <div className="flex items-center space-x-2 px-4 py-2 bg-emerald-50 border border-emerald-200 rounded-lg text-sm">
            <span className="font-semibold text-emerald-700">{present}</span><span className="text-emerald-600">Present</span>
          </div>
          <div className="flex items-center space-x-2 px-4 py-2 bg-red-50 border border-red-200 rounded-lg text-sm">
            <span className="font-semibold text-red-700">{absent}</span><span className="text-red-600">Absent</span>
          </div>
        </div>
      )}

      {/* Table */}
      <div className="card overflow-hidden">
        <table className="w-full border-collapse">
          <thead className="border-b border-slate-100">
            <tr>
              {['Employee', 'Check In', 'Check Out', 'Status'].map(h => <th key={h} className="table-header">{h}</th>)}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-50">
            {loading ? (
              <tr><td colSpan={4} className="py-12 text-center text-slate-400 text-sm">Loading...</td></tr>
            ) : attendance.length === 0 ? (
              <tr><td colSpan={4} className="py-12 text-center text-slate-400 text-sm">No records for this date.</td></tr>
            ) : attendance.map(att => (
              <tr key={att.attendanceId} className="hover:bg-slate-50/60 transition-colors">
                <td className="table-cell font-medium text-slate-800">{att.employeeName}</td>
                <td className="table-cell text-slate-500 font-mono text-xs">{att.checkIn || '—'}</td>
                <td className="table-cell text-slate-500 font-mono text-xs">{att.checkOut || '—'}</td>
                <td className="table-cell"><span className={statusMap[att.attendanceStatus] || 'badge-slate'}>{att.attendanceStatus}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal open={showMark} onClose={() => setShowMark(false)} title="Mark Attendance" size="md">
        <form onSubmit={handleMark} className="space-y-4">
          <div>
            <label className="form-label">Employee</label>
            <select value={markForm.employeeId} onChange={e => setMarkForm({ ...markForm, employeeId: e.target.value })} required className="form-input">
              <option value="">Select employee</option>
              {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
            </select>
          </div>
          <div>
            <label className="form-label">Date</label>
            <input type="date" value={markForm.date} onChange={e => setMarkForm({ ...markForm, date: e.target.value })} required className="form-input" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div><label className="form-label">Check In</label><input type="time" value={markForm.checkIn} onChange={e => setMarkForm({ ...markForm, checkIn: e.target.value })} className="form-input" /></div>
            <div><label className="form-label">Check Out</label><input type="time" value={markForm.checkOut} onChange={e => setMarkForm({ ...markForm, checkOut: e.target.value })} className="form-input" /></div>
          </div>
          <div>
            <label className="form-label">Status</label>
            <select value={markForm.attendanceStatus} onChange={e => setMarkForm({ ...markForm, attendanceStatus: e.target.value })} className="form-input">
              {['PRESENT', 'ABSENT', 'HALF_DAY', 'ON_LEAVE'].map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
            </select>
          </div>
          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setShowMark(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={submitting} className="btn-primary">{submitting ? 'Saving...' : 'Record Attendance'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Attendance;

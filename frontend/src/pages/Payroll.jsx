import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import { FiSearch, FiPlay, FiDownload } from 'react-icons/fi';

const Payroll = () => {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [showGen, setShowGen] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [genForm, setGenForm] = useState({ employeeId: '', month: '', year: '' });
  const [generating, setGenerating] = useState(false);

  const fetchPayroll = useCallback(async () => {
    setLoading(true);
    try {
      const r = await api.get('/payroll/paginated', { params: { search, page, size: 10, sort: 'payrollId', direction: 'DESC' } });
      if (r.data.success) {
        setRecords(r.data.data.content || []);
        setTotalPages(r.data.data.totalPages || 1);
        setTotalElements(r.data.data.totalElements || 0);
      }
    } catch { toast.error('Failed to load payroll.'); }
    finally { setLoading(false); }
  }, [page, search]);

  useEffect(() => { fetchPayroll(); }, [fetchPayroll]);
  useEffect(() => { api.get('/employees').then(r => { if (r.data.success) setEmployees(r.data.data); }).catch(() => {}); }, []);

  const handleGenerate = async e => {
    e.preventDefault(); setGenerating(true);
    try {
      await api.post('/payroll/generate', null, { params: { employeeId: genForm.employeeId, month: genForm.month, year: genForm.year } });
      toast.success('Payroll generated successfully.');
      setShowGen(false); fetchPayroll();
    } catch (err) { toast.error(err.response?.data?.message || 'Generation failed.'); }
    finally { setGenerating(false); }
  };

  const months = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  const year = new Date().getFullYear();

  return (
    <div className="space-y-6 fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="page-title">Payroll</h2>
          <p className="page-subtitle">{totalElements} payroll records generated.</p>
        </div>
        <button onClick={() => setShowGen(true)} className="btn-primary space-x-1.5">
          <FiPlay className="text-sm" /><span>Generate Payroll</span>
        </button>
      </div>

      <div className="relative">
        <FiSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
        <input type="text" value={search} onChange={e => { setSearch(e.target.value); setPage(0); }} placeholder="Search by employee name, month..." className="form-input pl-9 w-full max-w-sm" />
      </div>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead className="border-b border-slate-100">
              <tr>
                {['Employee', 'Month', 'Gross Salary', 'Deductions', 'Net Salary', ''].map(h => <th key={h} className="table-header">{h}</th>)}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {loading ? (
                <tr><td colSpan={6} className="py-12 text-center text-slate-400 text-sm">Loading...</td></tr>
              ) : records.length === 0 ? (
                <tr><td colSpan={6} className="py-12 text-center text-slate-400 text-sm">No payroll records found.</td></tr>
              ) : records.map(rec => (
                <tr key={rec.payrollId} className="hover:bg-slate-50/60 transition-colors">
                  <td className="table-cell font-medium text-slate-800">{rec.employeeName}</td>
                  <td className="table-cell text-slate-500">{rec.payrollMonth}</td>
                  <td className="table-cell font-medium text-emerald-700">₹{(rec.grossSalary || 0).toLocaleString()}</td>
                  <td className="table-cell text-red-600">₹{(rec.deductions || 0).toLocaleString()}</td>
                  <td className="table-cell font-bold text-slate-800">₹{(rec.netSalary || 0).toLocaleString()}</td>
                  <td className="table-cell text-right">
                    <button onClick={() => { toast.success('Downloading payslip...'); window.open(`http://localhost:8080/api/payroll/${rec.payrollId}/payslip`, '_blank'); }}
                      className="inline-flex items-center space-x-1.5 px-3 py-1.5 border border-slate-300 bg-white hover:bg-slate-50 text-slate-600 rounded-lg text-xs font-medium transition-colors shadow-sm">
                      <FiDownload className="text-xs" /><span>Payslip</span>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
      </div>

      <Modal open={showGen} onClose={() => setShowGen(false)} title="Generate Payroll" size="sm">
        <form onSubmit={handleGenerate} className="space-y-4">
          <div>
            <label className="form-label">Employee</label>
            <select value={genForm.employeeId} onChange={e => setGenForm({ ...genForm, employeeId: e.target.value })} required className="form-input">
              <option value="">Select employee</option>
              {employees.map(e => <option key={e.employeeId} value={e.employeeId}>{e.firstName} {e.lastName}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="form-label">Month</label>
              <select value={genForm.month} onChange={e => setGenForm({ ...genForm, month: e.target.value })} required className="form-input">
                <option value="">Month</option>
                {months.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
              </select>
            </div>
            <div>
              <label className="form-label">Year</label>
              <select value={genForm.year} onChange={e => setGenForm({ ...genForm, year: e.target.value })} required className="form-input">
                <option value="">Year</option>
                {[year - 1, year, year + 1].map(y => <option key={y} value={y}>{y}</option>)}
              </select>
            </div>
          </div>
          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setShowGen(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={generating} className="btn-primary">{generating ? 'Generating...' : 'Generate'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Payroll;

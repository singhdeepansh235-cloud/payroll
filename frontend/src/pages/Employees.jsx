import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import { FiSearch, FiUserPlus, FiDownload, FiEdit2, FiTrash2, FiEye } from 'react-icons/fi';

const INIT = {
  firstName: '', lastName: '', gender: 'MALE', dateOfBirth: '',
  email: '', phone: '', address: '', departmentId: '',
  designationId: '', joiningDate: '', salary: '', status: 'ACTIVE',
};

const Spinner = () => (
  <svg className="animate-spin h-5 w-5 mx-auto text-slate-400" viewBox="0 0 24 24">
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
  </svg>
);

const Employees = () => {
  const [employees, setEmployees] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [showView, setShowView] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [viewEmployee, setViewEmployee] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [form, setForm] = useState(INIT);
  const [submitting, setSubmitting] = useState(false);
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);

  const fetchEmployees = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get('/employees/paginated', { params: { search, page, size: 10, sort: 'employeeId', direction: 'ASC' } });
      if (res.data.success) {
        setEmployees(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
        setTotalElements(res.data.data.totalElements || 0);
      }
    } catch { toast.error('Failed to load employees.'); }
    finally { setLoading(false); }
  }, [page, search]);

  useEffect(() => { fetchEmployees(); }, [fetchEmployees]);
  useEffect(() => {
    Promise.all([api.get('/departments'), api.get('/designations')]).then(([d, g]) => {
      if (d.data.success) setDepartments(d.data.data);
      if (g.data.success) setDesignations(g.data.data);
    }).catch(() => {});
  }, []);

  const openCreate = () => { setEditingId(null); setForm(INIT); setShowForm(true); };
  const openEdit = (emp) => {
    setEditingId(emp.employeeId);
    setForm({ firstName: emp.firstName || '', lastName: emp.lastName || '', gender: emp.gender || 'MALE', dateOfBirth: emp.dateOfBirth || '', email: emp.email || '', phone: emp.phone || '', address: emp.address || '', departmentId: emp.departmentId || '', designationId: emp.designationId || '', joiningDate: emp.joiningDate || '', salary: emp.salary || '', status: emp.status || 'ACTIVE' });
    setShowForm(true);
  };
  const openView = async (id) => {
    try { const r = await api.get(`/employees/${id}`); if (r.data.success) { setViewEmployee(r.data.data); setShowView(true); } }
    catch { toast.error('Could not fetch employee.'); }
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setSubmitting(true);
    try {
      const p = { ...form, salary: parseFloat(form.salary), departmentId: parseInt(form.departmentId), designationId: parseInt(form.designationId) };
      if (editingId) { await api.put(`/employees/${editingId}`, p); toast.success('Employee updated.'); }
      else { await api.post('/employees', p); toast.success('Employee created.'); }
      setShowForm(false); fetchEmployees();
    } catch (err) { toast.error(err.response?.data?.message || 'Operation failed.'); }
    finally { setSubmitting(false); }
  };

  const handleDelete = async () => {
    try { await api.delete(`/employees/${deleteTarget.employeeId}`); toast.success('Employee removed.'); setShowDelete(false); fetchEmployees(); }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed.'); }
  };

  const FL = (name) => <><label className="form-label">{name}</label></>;

  return (
    <div className="space-y-6 fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="page-title">Employees</h2>
          <p className="page-subtitle">{totalElements} records in the system.</p>
        </div>
        <div className="flex items-center space-x-2">
          <button onClick={() => window.open('http://localhost:8080/api/reports/employees/pdf', '_blank')} className="btn-secondary space-x-1.5">
            <FiDownload className="text-sm" /><span>Export PDF</span>
          </button>
          <button onClick={openCreate} className="btn-primary space-x-1.5">
            <FiUserPlus className="text-sm" /><span>Add Employee</span>
          </button>
        </div>
      </div>

      {/* Search */}
      <div className="relative">
        <FiSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
        <input
          type="text" value={search}
          onChange={e => { setSearch(e.target.value); setPage(0); }}
          placeholder="Search by name, email, phone..."
          className="form-input pl-9 w-full max-w-sm"
        />
      </div>

      {/* Table */}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead className="border-b border-slate-100">
              <tr>
                {['Name', 'Email', 'Phone', 'Department', 'Salary', 'Status', ''].map(h => (
                  <th key={h} className="table-header">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {loading ? (
                <tr><td colSpan={7} className="py-14 text-center"><Spinner /><p className="text-slate-400 text-sm mt-2">Loading...</p></td></tr>
              ) : employees.length === 0 ? (
                <tr><td colSpan={7} className="py-14 text-center text-slate-400 text-sm">No employees found.</td></tr>
              ) : employees.map(emp => (
                <tr key={emp.employeeId} className="hover:bg-slate-50/60 transition-colors">
                  <td className="table-cell font-medium text-slate-800">{emp.firstName} {emp.lastName}</td>
                  <td className="table-cell text-slate-500">{emp.email}</td>
                  <td className="table-cell text-slate-500">{emp.phone}</td>
                  <td className="table-cell text-slate-600">{emp.departmentName || '—'}</td>
                  <td className="table-cell font-medium">₹{(emp.salary || 0).toLocaleString()}</td>
                  <td className="table-cell">
                    <span className={emp.status === 'ACTIVE' ? 'badge-green' : 'badge-red'}>{emp.status}</span>
                  </td>
                  <td className="table-cell text-right">
                    <div className="flex items-center justify-end space-x-1">
                      <button onClick={() => openView(emp.employeeId)} className="p-1.5 text-slate-400 hover:text-[#2d4a8a] hover:bg-blue-50 rounded-lg transition-colors"><FiEye className="text-sm" /></button>
                      <button onClick={() => openEdit(emp)} className="p-1.5 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"><FiEdit2 className="text-sm" /></button>
                      <button onClick={() => { setDeleteTarget(emp); setShowDelete(true); }} className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"><FiTrash2 className="text-sm" /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
      </div>

      {/* Create/Edit Modal */}
      <Modal open={showForm} onClose={() => setShowForm(false)} title={editingId ? 'Edit Employee' : 'Add New Employee'} size="xl">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[['First Name', 'firstName', 'text', 'John'], ['Last Name', 'lastName', 'text', 'Doe'], ['Email', 'email', 'email', 'john@example.com'], ['Phone', 'phone', 'text', '+919876543210'], ['Date of Birth', 'dateOfBirth', 'date', ''], ['Joining Date', 'joiningDate', 'date', ''], ['Monthly Salary (₹)', 'salary', 'number', '50000']].map(([label, name, type, ph]) => (
              <div key={name}>
                <label className="form-label">{label}</label>
                <input type={type} name={name} value={form[name]} onChange={e => setForm({ ...form, [name]: e.target.value })} placeholder={ph} required={['firstName','lastName','email','phone','dateOfBirth','joiningDate','salary'].includes(name)} className="form-input" />
              </div>
            ))}
            {[['Gender','gender',['MALE','Female','OTHER']], ['Status','status',['ACTIVE','INACTIVE']]].map(([label, name, opts]) => (
              <div key={name}>
                <label className="form-label">{label}</label>
                <select name={name} value={form[name]} onChange={e => setForm({...form,[name]:e.target.value})} className="form-input">
                  {opts.map(o => <option key={o} value={o}>{o}</option>)}
                </select>
              </div>
            ))}
            <div>
              <label className="form-label">Department</label>
              <select name="departmentId" value={form.departmentId} onChange={e => setForm({...form, departmentId: e.target.value})} required className="form-input">
                <option value="">Select department</option>
                {departments.map(d => <option key={d.departmentId} value={d.departmentId}>{d.departmentName}</option>)}
              </select>
            </div>
            <div>
              <label className="form-label">Designation</label>
              <select name="designationId" value={form.designationId} onChange={e => setForm({...form, designationId: e.target.value})} required className="form-input">
                <option value="">Select designation</option>
                {designations.map(d => <option key={d.designationId} value={d.designationId}>{d.designationName}</option>)}
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="form-label">Address</label>
              <input type="text" name="address" value={form.address} onChange={e => setForm({...form, address: e.target.value})} placeholder="Full address" className="form-input" />
            </div>
          </div>
          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={submitting} className="btn-primary">{submitting ? 'Saving...' : editingId ? 'Update Employee' : 'Create Employee'}</button>
          </div>
        </form>
      </Modal>

      {/* View Modal */}
      <Modal open={showView} onClose={() => setShowView(false)} title="Employee Details" size="md">
        {viewEmployee && (
          <dl className="space-y-3">
            {[['Full Name', `${viewEmployee.firstName} ${viewEmployee.lastName}`], ['Gender', viewEmployee.gender], ['Date of Birth', viewEmployee.dateOfBirth], ['Email', viewEmployee.email], ['Phone', viewEmployee.phone], ['Address', viewEmployee.address || '—'], ['Department', viewEmployee.departmentName || '—'], ['Designation', viewEmployee.designationName || '—'], ['Joining Date', viewEmployee.joiningDate], ['Salary', `₹${(viewEmployee.salary || 0).toLocaleString()}`], ['Status', viewEmployee.status]].map(([k, v]) => (
              <div key={k} className="flex justify-between text-sm py-2 border-b border-slate-100 last:border-0">
                <dt className="text-slate-400 font-medium">{k}</dt>
                <dd className="text-slate-700 font-medium">{v}</dd>
              </div>
            ))}
          </dl>
        )}
      </Modal>

      {/* Delete Modal */}
      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Remove Employee" size="sm">
        <p className="text-sm text-slate-600 mb-5">Are you sure you want to delete <strong className="text-slate-800">{deleteTarget?.firstName} {deleteTarget?.lastName}</strong>? This cannot be undone.</p>
        <div className="flex justify-end space-x-2">
          <button onClick={() => setShowDelete(false)} className="btn-secondary">Cancel</button>
          <button onClick={handleDelete} className="btn-danger">Delete Employee</button>
        </div>
      </Modal>
    </div>
  );
};

export default Employees;

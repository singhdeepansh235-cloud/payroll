import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import {
  FiSearch, FiUserPlus, FiChevronLeft, FiChevronRight, FiDownload,
  FiEdit2, FiTrash2, FiX, FiEye
} from 'react-icons/fi';

// ─── Reusable Modal Shell ───────────────────────────────────────────
const Modal = ({ open, onClose, title, children, wide }) => {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
      <div
        className={`bg-[#151f32] border border-slate-700/50 rounded-2xl shadow-2xl w-full ${wide ? 'max-w-2xl' : 'max-w-lg'} max-h-[90vh] overflow-y-auto`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-700/50">
          <h3 className="text-lg font-bold text-slate-100">{title}</h3>
          <button onClick={onClose} className="p-1.5 hover:bg-slate-700/50 rounded-lg transition-colors text-slate-400 hover:text-slate-200">
            <FiX />
          </button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
};

// ─── Input Component ────────────────────────────────────────────────
const FormInput = ({ label, ...props }) => (
  <div>
    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">{label}</label>
    <input
      {...props}
      className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 placeholder-slate-500 outline-none transition-all text-sm"
    />
  </div>
);

const FormSelect = ({ label, children, ...props }) => (
  <div>
    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">{label}</label>
    <select
      {...props}
      className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm"
    >
      {children}
    </select>
  </div>
);

const initialForm = {
  firstName: '', lastName: '', gender: 'MALE', dateOfBirth: '',
  email: '', phone: '', address: '', departmentId: '', designationId: '',
  joiningDate: '', salary: '', status: 'ACTIVE',
};

const Employees = () => {
  const [employees, setEmployees] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  // Modal state
  const [showForm, setShowForm] = useState(false);
  const [showView, setShowView] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [viewEmployee, setViewEmployee] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [form, setForm] = useState(initialForm);
  const [submitting, setSubmitting] = useState(false);

  // Lookup data
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);

  const fetchEmployees = useCallback(async () => {
    setLoading(true);
    try {
      const response = await api.get('/employees/paginated', {
        params: { search, page, size: 8, sort: 'employeeId', direction: 'ASC' },
      });
      if (response.data.success) {
        setEmployees(response.data.data.content || []);
        setTotalPages(response.data.data.totalPages || 1);
        setTotalElements(response.data.data.totalElements || 0);
      }
    } catch (err) {
      toast.error('Failed to load employees.');
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  const fetchLookups = async () => {
    try {
      const [deptRes, desgRes] = await Promise.all([
        api.get('/departments'),
        api.get('/designations'),
      ]);
      if (deptRes.data.success) setDepartments(deptRes.data.data);
      if (desgRes.data.success) setDesignations(desgRes.data.data);
    } catch (err) {
      // silent
    }
  };

  useEffect(() => { fetchEmployees(); }, [fetchEmployees]);
  useEffect(() => { fetchLookups(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const openCreate = () => {
    setEditingId(null);
    setForm(initialForm);
    setShowForm(true);
  };

  const openEdit = (emp) => {
    setEditingId(emp.employeeId);
    setForm({
      firstName: emp.firstName || '',
      lastName: emp.lastName || '',
      gender: emp.gender || 'MALE',
      dateOfBirth: emp.dateOfBirth || '',
      email: emp.email || '',
      phone: emp.phone || '',
      address: emp.address || '',
      departmentId: emp.departmentId || '',
      designationId: emp.designationId || '',
      joiningDate: emp.joiningDate || '',
      salary: emp.salary || '',
      status: emp.status || 'ACTIVE',
    });
    setShowForm(true);
  };

  const openView = async (id) => {
    try {
      const res = await api.get(`/employees/${id}`);
      if (res.data.success) {
        setViewEmployee(res.data.data);
        setShowView(true);
      }
    } catch (err) {
      toast.error('Failed to fetch employee details.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = { ...form, salary: parseFloat(form.salary), departmentId: parseInt(form.departmentId), designationId: parseInt(form.designationId) };
      if (editingId) {
        await api.put(`/employees/${editingId}`, payload);
        toast.success('Employee updated successfully!');
      } else {
        await api.post('/employees', payload);
        toast.success('Employee created successfully!');
      }
      setShowForm(false);
      fetchEmployees();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const confirmDelete = (emp) => {
    setDeleteTarget(emp);
    setShowDelete(true);
  };

  const handleDelete = async () => {
    try {
      await api.delete(`/employees/${deleteTarget.employeeId}`);
      toast.success('Employee deleted successfully!');
      setShowDelete(false);
      setDeleteTarget(null);
      fetchEmployees();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">Employees</h2>
          <p className="text-slate-400 text-sm mt-1">
            {totalElements} total employees registered in the system.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => window.open('http://localhost:8080/api/reports/employees/pdf', '_blank')}
            className="flex items-center space-x-2 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 border border-slate-700 hover:border-slate-600 rounded-xl transition-all text-sm font-medium text-slate-300"
          >
            <FiDownload className="text-sm" />
            <span>PDF Report</span>
          </button>
          <button
            onClick={openCreate}
            className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 hover:from-sky-600 hover:to-indigo-600 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] text-sm"
          >
            <FiUserPlus className="text-sm" />
            <span>Add Employee</span>
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div className="flex items-center bg-[#1e293b]/60 border border-slate-700/50 px-4 py-3 rounded-2xl">
        <FiSearch className="text-slate-400 mr-3 text-lg" />
        <input
          type="text"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          placeholder="Search by name, email, phone, department..."
          className="bg-transparent border-none outline-none w-full text-slate-200 placeholder-slate-500 text-sm"
        />
      </div>

      {/* Table Card */}
      <div className="bg-[#1e293b]/40 border border-slate-700/50 rounded-2xl overflow-hidden backdrop-blur-md">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-700/50 text-slate-400 text-xs font-semibold uppercase tracking-wider bg-[#0f172a]/30">
                <th className="px-5 py-4">Name</th>
                <th className="px-5 py-4">Email</th>
                <th className="px-5 py-4">Phone</th>
                <th className="px-5 py-4">Department</th>
                <th className="px-5 py-4">Salary</th>
                <th className="px-5 py-4">Status</th>
                <th className="px-5 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/30 text-sm text-slate-300">
              {loading ? (
                <tr><td colSpan="7" className="text-center py-12 text-slate-500">
                  <svg className="animate-spin h-6 w-6 mx-auto text-sky-400 mb-2" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                  Loading employees...
                </td></tr>
              ) : employees.length === 0 ? (
                <tr><td colSpan="7" className="text-center py-12 text-slate-500">No employees found.</td></tr>
              ) : (
                employees.map((emp) => (
                  <tr key={emp.employeeId} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-5 py-4 font-semibold text-slate-200">{emp.firstName} {emp.lastName}</td>
                    <td className="px-5 py-4 text-slate-400">{emp.email}</td>
                    <td className="px-5 py-4 text-slate-400">{emp.phone}</td>
                    <td className="px-5 py-4">{emp.departmentName || 'N/A'}</td>
                    <td className="px-5 py-4 font-medium">₹{(emp.salary || 0).toLocaleString()}</td>
                    <td className="px-5 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${
                        emp.status === 'ACTIVE'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}>
                        {emp.status}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-right">
                      <div className="flex items-center justify-end space-x-1">
                        <button onClick={() => openView(emp.employeeId)} title="View" className="p-2 hover:bg-sky-500/10 text-slate-400 hover:text-sky-400 rounded-lg transition-colors"><FiEye className="text-sm" /></button>
                        <button onClick={() => openEdit(emp)} title="Edit" className="p-2 hover:bg-amber-500/10 text-slate-400 hover:text-amber-400 rounded-lg transition-colors"><FiEdit2 className="text-sm" /></button>
                        <button onClick={() => confirmDelete(emp)} title="Delete" className="p-2 hover:bg-rose-500/10 text-slate-400 hover:text-rose-400 rounded-lg transition-colors"><FiTrash2 className="text-sm" /></button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="px-5 py-4 bg-[#0f172a]/20 border-t border-slate-700/50 flex items-center justify-between">
            <span className="text-xs text-slate-400">
              Page {page + 1} of {totalPages} ({totalElements} total)
            </span>
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

      {/* ─── Create / Edit Modal ────────────────────────────────────── */}
      <Modal open={showForm} onClose={() => setShowForm(false)} title={editingId ? 'Edit Employee' : 'Add New Employee'} wide>
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <FormInput label="First Name" name="firstName" value={form.firstName} onChange={handleChange} required placeholder="John" />
            <FormInput label="Last Name" name="lastName" value={form.lastName} onChange={handleChange} required placeholder="Doe" />
            <FormSelect label="Gender" name="gender" value={form.gender} onChange={handleChange}>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </FormSelect>
            <FormInput label="Date of Birth" name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={handleChange} required />
            <FormInput label="Email" name="email" type="email" value={form.email} onChange={handleChange} required placeholder="john@company.com" />
            <FormInput label="Phone" name="phone" value={form.phone} onChange={handleChange} required placeholder="+919876543210" />
            <FormSelect label="Department" name="departmentId" value={form.departmentId} onChange={handleChange} required>
              <option value="">Select Department</option>
              {departments.map(d => <option key={d.departmentId} value={d.departmentId}>{d.departmentName}</option>)}
            </FormSelect>
            <FormSelect label="Designation" name="designationId" value={form.designationId} onChange={handleChange} required>
              <option value="">Select Designation</option>
              {designations.map(d => <option key={d.designationId} value={d.designationId}>{d.designationName}</option>)}
            </FormSelect>
            <FormInput label="Joining Date" name="joiningDate" type="date" value={form.joiningDate} onChange={handleChange} required />
            <FormInput label="Monthly Salary (₹)" name="salary" type="number" step="0.01" value={form.salary} onChange={handleChange} required placeholder="75000" />
            <FormSelect label="Status" name="status" value={form.status} onChange={handleChange}>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </FormSelect>
          </div>
          <div className="md:col-span-2">
            <FormInput label="Address" name="address" value={form.address} onChange={handleChange} placeholder="123 Main St, City" />
          </div>
          <div className="flex justify-end space-x-3 pt-2">
            <button type="button" onClick={() => setShowForm(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
            <button type="submit" disabled={submitting} className="px-5 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98] disabled:opacity-50">
              {submitting ? 'Saving...' : editingId ? 'Update Employee' : 'Create Employee'}
            </button>
          </div>
        </form>
      </Modal>

      {/* ─── View Details Modal ─────────────────────────────────────── */}
      <Modal open={showView} onClose={() => setShowView(false)} title="Employee Details">
        {viewEmployee && (
          <div className="space-y-4 text-sm">
            {[
              ['Name', `${viewEmployee.firstName} ${viewEmployee.lastName}`],
              ['Gender', viewEmployee.gender],
              ['Date of Birth', viewEmployee.dateOfBirth],
              ['Email', viewEmployee.email],
              ['Phone', viewEmployee.phone],
              ['Address', viewEmployee.address || 'N/A'],
              ['Department', viewEmployee.departmentName || 'N/A'],
              ['Designation', viewEmployee.designationName || 'N/A'],
              ['Joining Date', viewEmployee.joiningDate],
              ['Salary', `₹${(viewEmployee.salary || 0).toLocaleString()}`],
              ['Status', viewEmployee.status],
            ].map(([k, v], i) => (
              <div key={i} className="flex justify-between py-2 border-b border-slate-700/30">
                <span className="text-slate-400 font-medium">{k}</span>
                <span className="text-slate-200 text-right">{v}</span>
              </div>
            ))}
          </div>
        )}
      </Modal>

      {/* ─── Delete Confirm Modal ───────────────────────────────────── */}
      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Confirm Deletion">
        <p className="text-slate-300 text-sm mb-6">
          Are you sure you want to delete <strong className="text-rose-400">{deleteTarget?.firstName} {deleteTarget?.lastName}</strong>? This action cannot be undone.
        </p>
        <div className="flex justify-end space-x-3">
          <button onClick={() => setShowDelete(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
          <button onClick={handleDelete} className="px-5 py-2.5 bg-rose-500 hover:bg-rose-600 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98]">Delete Employee</button>
        </div>
      </Modal>
    </div>
  );
};

export default Employees;

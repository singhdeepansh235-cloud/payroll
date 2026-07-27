import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Modal from '../components/Modal';
import { FiPlus, FiBriefcase, FiEdit2, FiTrash2 } from 'react-icons/fi';

const Departments = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetch = async () => {
    try { const r = await api.get('/departments'); if (r.data.success) setDepartments(r.data.data); }
    catch { toast.error('Failed to load departments.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetch(); }, []);

  const openCreate = () => { setEditingId(null); setName(''); setDescription(''); setShowForm(true); };
  const openEdit = d => { setEditingId(d.departmentId); setName(d.departmentName); setDescription(d.description || ''); setShowForm(true); };

  const handleSubmit = async e => {
    e.preventDefault(); setSubmitting(true);
    try {
      const p = { departmentName: name, description };
      if (editingId) { await api.put(`/departments/${editingId}`, p); toast.success('Department updated.'); }
      else { await api.post('/departments', p); toast.success('Department created.'); }
      setShowForm(false); fetch();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed.'); }
    finally { setSubmitting(false); }
  };

  const handleDelete = async () => {
    try { await api.delete(`/departments/${deleteTarget.departmentId}`); toast.success('Department deleted.'); setShowDelete(false); fetch(); }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed.'); }
  };

  return (
    <div className="space-y-6 fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="page-title">Departments</h2>
          <p className="page-subtitle">{departments.length} departments in the organization.</p>
        </div>
        <button onClick={openCreate} className="btn-primary space-x-1.5">
          <FiPlus className="text-sm" /><span>Add Department</span>
        </button>
      </div>

      <div className="card overflow-hidden">
        <table className="w-full border-collapse">
          <thead className="border-b border-slate-100">
            <tr>
              <th className="table-header">Department</th>
              <th className="table-header">Description</th>
              <th className="table-header text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-50">
            {loading ? (
              <tr><td colSpan={3} className="py-12 text-center text-slate-400 text-sm">Loading...</td></tr>
            ) : departments.length === 0 ? (
              <tr><td colSpan={3} className="py-12 text-center text-slate-400 text-sm">No departments found.</td></tr>
            ) : departments.map(dept => (
              <tr key={dept.departmentId} className="hover:bg-slate-50/60 transition-colors">
                <td className="table-cell">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center">
                      <FiBriefcase className="text-[#2d4a8a] text-sm" />
                    </div>
                    <span className="font-medium text-slate-800">{dept.departmentName}</span>
                  </div>
                </td>
                <td className="table-cell text-slate-500 max-w-xs truncate">{dept.description || '—'}</td>
                <td className="table-cell text-right">
                  <div className="flex items-center justify-end space-x-1">
                    <button onClick={() => openEdit(dept)} className="p-1.5 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"><FiEdit2 className="text-sm" /></button>
                    <button onClick={() => { setDeleteTarget(dept); setShowDelete(true); }} className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"><FiTrash2 className="text-sm" /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal open={showForm} onClose={() => setShowForm(false)} title={editingId ? 'Edit Department' : 'Add Department'} size="md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="form-label">Department Name</label>
            <input type="text" value={name} onChange={e => setName(e.target.value)} required placeholder="e.g. Engineering" className="form-input" />
          </div>
          <div>
            <label className="form-label">Description</label>
            <textarea value={description} onChange={e => setDescription(e.target.value)} placeholder="Brief description..." className="form-input h-24 resize-none" />
          </div>
          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={submitting} className="btn-primary">{submitting ? 'Saving...' : editingId ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Delete Department" size="sm">
        <p className="text-sm text-slate-600 mb-5">Delete <strong className="text-slate-800">{deleteTarget?.departmentName}</strong>? This cannot be undone.</p>
        <div className="flex justify-end space-x-2">
          <button onClick={() => setShowDelete(false)} className="btn-secondary">Cancel</button>
          <button onClick={handleDelete} className="btn-danger">Delete</button>
        </div>
      </Modal>
    </div>
  );
};

export default Departments;

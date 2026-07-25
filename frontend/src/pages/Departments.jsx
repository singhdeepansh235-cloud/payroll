import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiPlus, FiBriefcase, FiEdit2, FiTrash2, FiX } from 'react-icons/fi';

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

  const fetchDepartments = async () => {
    try {
      const response = await api.get('/departments');
      if (response.data.success) setDepartments(response.data.data);
    } catch (err) {
      toast.error('Failed to load departments.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchDepartments(); }, []);

  const openCreate = () => { setEditingId(null); setName(''); setDescription(''); setShowForm(true); };
  const openEdit = (dept) => { setEditingId(dept.departmentId); setName(dept.departmentName); setDescription(dept.description || ''); setShowForm(true); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = { departmentName: name, description };
      if (editingId) {
        await api.put(`/departments/${editingId}`, payload);
        toast.success('Department updated successfully!');
      } else {
        await api.post('/departments', payload);
        toast.success('Department created successfully!');
      }
      setShowForm(false);
      fetchDepartments();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const confirmDelete = (dept) => { setDeleteTarget(dept); setShowDelete(true); };

  const handleDelete = async () => {
    try {
      await api.delete(`/departments/${deleteTarget.departmentId}`);
      toast.success('Department deleted successfully!');
      setShowDelete(false);
      fetchDepartments();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete department.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">Departments</h2>
          <p className="text-slate-400 text-sm mt-1">{departments.length} departments registered in the system.</p>
        </div>
        <button onClick={openCreate} className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] text-sm">
          <FiPlus /><span>Add Department</span>
        </button>
      </div>

      {/* Table */}
      <div className="bg-[#1e293b]/40 border border-slate-700/50 rounded-2xl overflow-hidden backdrop-blur-md">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-700/50 text-slate-400 text-xs font-semibold uppercase tracking-wider bg-[#0f172a]/30">
              <th className="px-6 py-4">Department Name</th>
              <th className="px-6 py-4">Description</th>
              <th className="px-6 py-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-700/30 text-sm text-slate-300">
            {loading ? (
              <tr><td colSpan="3" className="text-center py-10 text-slate-500">Loading departments...</td></tr>
            ) : departments.length === 0 ? (
              <tr><td colSpan="3" className="text-center py-10 text-slate-500">No departments found.</td></tr>
            ) : (
              departments.map((dept) => (
                <tr key={dept.departmentId} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center space-x-3">
                      <div className="p-2 bg-sky-500/10 text-sky-400 rounded-lg"><FiBriefcase /></div>
                      <span className="font-semibold text-slate-200">{dept.departmentName}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-slate-400 max-w-xs truncate">{dept.description || 'No description'}</td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex items-center justify-end space-x-1">
                      <button onClick={() => openEdit(dept)} title="Edit" className="p-2 hover:bg-amber-500/10 text-slate-400 hover:text-amber-400 rounded-lg transition-colors"><FiEdit2 className="text-sm" /></button>
                      <button onClick={() => confirmDelete(dept)} title="Delete" className="p-2 hover:bg-rose-500/10 text-slate-400 hover:text-rose-400 rounded-lg transition-colors"><FiTrash2 className="text-sm" /></button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Create / Edit Modal */}
      <Modal open={showForm} onClose={() => setShowForm(false)} title={editingId ? 'Edit Department' : 'Add New Department'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Department Name</label>
            <input type="text" value={name} onChange={(e) => setName(e.target.value)} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 placeholder-slate-500 outline-none transition-all text-sm" placeholder="e.g. Engineering" />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Description</label>
            <textarea value={description} onChange={(e) => setDescription(e.target.value)} className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 placeholder-slate-500 outline-none transition-all text-sm h-24 resize-none" placeholder="Brief description..." />
          </div>
          <div className="flex justify-end space-x-3 pt-2">
            <button type="button" onClick={() => setShowForm(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
            <button type="submit" disabled={submitting} className="px-5 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98] disabled:opacity-50">
              {submitting ? 'Saving...' : editingId ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirm Modal */}
      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Confirm Deletion">
        <p className="text-slate-300 text-sm mb-6">Are you sure you want to delete <strong className="text-rose-400">{deleteTarget?.departmentName}</strong>?</p>
        <div className="flex justify-end space-x-3">
          <button onClick={() => setShowDelete(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
          <button onClick={handleDelete} className="px-5 py-2.5 bg-rose-500 hover:bg-rose-600 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98]">Delete</button>
        </div>
      </Modal>
    </div>
  );
};

export default Departments;

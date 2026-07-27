import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Modal from '../components/Modal';
import { FiPlus, FiAward, FiEdit2, FiTrash2 } from 'react-icons/fi';

const Designations = () => {
  const [designations, setDesignations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetch = async () => {
    try { const r = await api.get('/designations'); if (r.data.success) setDesignations(r.data.data); }
    catch { toast.error('Failed to load designations.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetch(); }, []);

  const openCreate = () => { setEditingId(null); setName(''); setShowForm(true); };
  const openEdit = d => { setEditingId(d.designationId); setName(d.designationName); setShowForm(true); };

  const handleSubmit = async e => {
    e.preventDefault(); setSubmitting(true);
    try {
      const p = { designationName: name };
      if (editingId) { await api.put(`/designations/${editingId}`, p); toast.success('Designation updated.'); }
      else { await api.post('/designations', p); toast.success('Designation created.'); }
      setShowForm(false); fetch();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed.'); }
    finally { setSubmitting(false); }
  };

  const handleDelete = async () => {
    try { await api.delete(`/designations/${deleteTarget.designationId}`); toast.success('Designation deleted.'); setShowDelete(false); fetch(); }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed.'); }
  };

  return (
    <div className="space-y-6 fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="page-title">Designations</h2>
          <p className="page-subtitle">{designations.length} job titles registered.</p>
        </div>
        <button onClick={openCreate} className="btn-primary space-x-1.5">
          <FiPlus className="text-sm" /><span>Add Designation</span>
        </button>
      </div>

      <div className="card overflow-hidden">
        <table className="w-full border-collapse">
          <thead className="border-b border-slate-100">
            <tr>
              <th className="table-header">#</th>
              <th className="table-header">Designation Title</th>
              <th className="table-header text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-50">
            {loading ? (
              <tr><td colSpan={3} className="py-12 text-center text-slate-400 text-sm">Loading...</td></tr>
            ) : designations.length === 0 ? (
              <tr><td colSpan={3} className="py-12 text-center text-slate-400 text-sm">No designations found.</td></tr>
            ) : designations.map((d, idx) => (
              <tr key={d.designationId} className="hover:bg-slate-50/60 transition-colors">
                <td className="table-cell text-slate-400 font-mono text-xs w-14">{idx + 1}</td>
                <td className="table-cell">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-slate-100 rounded-lg flex items-center justify-center">
                      <FiAward className="text-slate-500 text-sm" />
                    </div>
                    <span className="font-medium text-slate-800">{d.designationName}</span>
                  </div>
                </td>
                <td className="table-cell text-right">
                  <div className="flex items-center justify-end space-x-1">
                    <button onClick={() => openEdit(d)} className="p-1.5 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"><FiEdit2 className="text-sm" /></button>
                    <button onClick={() => { setDeleteTarget(d); setShowDelete(true); }} className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"><FiTrash2 className="text-sm" /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal open={showForm} onClose={() => setShowForm(false)} title={editingId ? 'Edit Designation' : 'Add Designation'} size="sm">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="form-label">Designation Title</label>
            <input type="text" value={name} onChange={e => setName(e.target.value)} required placeholder="e.g. Senior Developer" className="form-input" />
          </div>
          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={submitting} className="btn-primary">{submitting ? 'Saving...' : editingId ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Delete Designation" size="sm">
        <p className="text-sm text-slate-600 mb-5">Delete <strong className="text-slate-800">{deleteTarget?.designationName}</strong>? This cannot be undone.</p>
        <div className="flex justify-end space-x-2">
          <button onClick={() => setShowDelete(false)} className="btn-secondary">Cancel</button>
          <button onClick={handleDelete} className="btn-danger">Delete</button>
        </div>
      </Modal>
    </div>
  );
};

export default Designations;

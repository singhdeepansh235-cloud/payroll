import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiUpload, FiDownload, FiTrash2, FiFile, FiSearch, FiX, FiChevronLeft, FiChevronRight } from 'react-icons/fi';

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

const Documents = () => {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [employeeId, setEmployeeId] = useState('');
  const [employees, setEmployees] = useState([]);

  // Upload modal
  const [showUpload, setShowUpload] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [uploadForm, setUploadForm] = useState({ employeeId: '', documentType: 'AADHAAR', file: null });
  const [uploading, setUploading] = useState(false);

  const fetchEmployees = async () => {
    try {
      const res = await api.get('/employees');
      if (res.data.success) setEmployees(res.data.data);
    } catch (err) { /* silent */ }
  };

  const fetchDocuments = async (empId) => {
    if (!empId) return;
    setLoading(true);
    try {
      const res = await api.get(`/documents/employee/${empId}`);
      if (res.data.success) setDocuments(res.data.data);
    } catch (err) {
      toast.error('Failed to load documents.');
      setDocuments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchEmployees(); }, []);
  useEffect(() => { if (employeeId) fetchDocuments(employeeId); }, [employeeId]);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!uploadForm.file) { toast.error('Please select a file.'); return; }

    const maxSize = 5 * 1024 * 1024; // 5MB
    if (uploadForm.file.size > maxSize) { toast.error('File size must be under 5MB.'); return; }

    const allowed = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (!allowed.includes(uploadForm.file.type)) { toast.error('Only PDF, JPG, JPEG, PNG files are accepted.'); return; }

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('employeeId', uploadForm.employeeId);
      formData.append('documentType', uploadForm.documentType);
      formData.append('file', uploadForm.file);

      await api.post('/documents/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      toast.success('Document uploaded successfully!');
      setShowUpload(false);
      setUploadForm({ employeeId: '', documentType: 'AADHAAR', file: null });
      if (employeeId === uploadForm.employeeId) fetchDocuments(employeeId);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed.');
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = (docId) => {
    toast.success('Downloading document...');
    window.open(`http://localhost:8080/api/documents/download/${docId}`, '_blank');
  };

  const confirmDelete = (doc) => { setDeleteTarget(doc); setShowDelete(true); };

  const handleDelete = async () => {
    try {
      await api.delete(`/documents/${deleteTarget.documentId}`);
      toast.success('Document deleted successfully!');
      setShowDelete(false);
      fetchDocuments(employeeId);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete document.');
    }
  };

  const docTypeColor = (type) => {
    const map = {
      AADHAAR: 'bg-sky-500/10 text-sky-400 border-sky-500/20',
      PAN: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      RESUME: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      OFFER_LETTER: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20',
      OTHER: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
    };
    return map[type] || map.OTHER;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">Document Vault</h2>
          <p className="text-slate-400 text-sm mt-1">Upload, download, and manage employee documents securely.</p>
        </div>
        <button onClick={() => setShowUpload(true)} className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] text-sm">
          <FiUpload /><span>Upload Document</span>
        </button>
      </div>

      {/* Employee Selector */}
      <div className="flex items-center space-x-4">
        <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Select Employee:</label>
        <select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)} className="px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm min-w-[200px]">
          <option value="">Choose employee...</option>
          {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
        </select>
      </div>

      {/* Documents Table */}
      {employeeId && (
        <div className="bg-[#1e293b]/40 border border-slate-700/50 rounded-2xl overflow-hidden backdrop-blur-md">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-700/50 text-slate-400 text-xs font-semibold uppercase tracking-wider bg-[#0f172a]/30">
                <th className="px-6 py-4">File Name</th>
                <th className="px-6 py-4">Document Type</th>
                <th className="px-6 py-4">Upload Date</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/30 text-sm text-slate-300">
              {loading ? (
                <tr><td colSpan="4" className="text-center py-12 text-slate-500">
                  <svg className="animate-spin h-6 w-6 mx-auto text-sky-400 mb-2" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                  Loading documents...
                </td></tr>
              ) : documents.length === 0 ? (
                <tr><td colSpan="4" className="text-center py-12 text-slate-500">No documents found for this employee.</td></tr>
              ) : (
                documents.map((doc) => (
                  <tr key={doc.documentId} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-3">
                        <div className="p-2 bg-slate-700/30 text-slate-400 rounded-lg"><FiFile /></div>
                        <span className="font-medium text-slate-200 truncate max-w-[200px]">{doc.fileName}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-medium border ${docTypeColor(doc.documentType)}`}>{doc.documentType}</span>
                    </td>
                    <td className="px-6 py-4 text-slate-400 text-xs">{doc.uploadDate ? new Date(doc.uploadDate).toLocaleDateString() : 'N/A'}</td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end space-x-1">
                        <button onClick={() => handleDownload(doc.documentId)} title="Download" className="p-2 hover:bg-sky-500/10 text-slate-400 hover:text-sky-400 rounded-lg transition-colors"><FiDownload className="text-sm" /></button>
                        <button onClick={() => confirmDelete(doc)} title="Delete" className="p-2 hover:bg-rose-500/10 text-slate-400 hover:text-rose-400 rounded-lg transition-colors"><FiTrash2 className="text-sm" /></button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {!employeeId && (
        <div className="flex items-center justify-center py-20 text-slate-500 text-sm">
          Select an employee above to view their documents.
        </div>
      )}

      {/* Upload Modal */}
      <Modal open={showUpload} onClose={() => setShowUpload(false)} title="Upload Document">
        <form onSubmit={handleUpload} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Employee</label>
            <select value={uploadForm.employeeId} onChange={(e) => setUploadForm({ ...uploadForm, employeeId: e.target.value })} required className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
              <option value="">Select Employee</option>
              {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Document Type</label>
            <select value={uploadForm.documentType} onChange={(e) => setUploadForm({ ...uploadForm, documentType: e.target.value })} className="w-full px-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none transition-all text-sm">
              <option value="AADHAAR">Aadhaar</option>
              <option value="PAN">PAN</option>
              <option value="RESUME">Resume</option>
              <option value="OFFER_LETTER">Offer Letter</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">File (PDF, JPG, JPEG, PNG — max 5MB)</label>
            <input
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={(e) => setUploadForm({ ...uploadForm, file: e.target.files[0] })}
              required
              className="w-full text-sm text-slate-400 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-sm file:font-semibold file:bg-sky-500/10 file:text-sky-400 hover:file:bg-sky-500/20 cursor-pointer"
            />
          </div>
          <div className="flex justify-end space-x-3 pt-2">
            <button type="button" onClick={() => setShowUpload(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
            <button type="submit" disabled={uploading} className="px-5 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98] disabled:opacity-50">
              {uploading ? 'Uploading...' : 'Upload'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirm */}
      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Confirm Deletion">
        <p className="text-slate-300 text-sm mb-6">Are you sure you want to delete <strong className="text-rose-400">{deleteTarget?.fileName}</strong>?</p>
        <div className="flex justify-end space-x-3">
          <button onClick={() => setShowDelete(false)} className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors">Cancel</button>
          <button onClick={handleDelete} className="px-5 py-2.5 bg-rose-500 hover:bg-rose-600 text-white font-semibold rounded-xl text-sm transition-all active:scale-[0.98]">Delete</button>
        </div>
      </Modal>
    </div>
  );
};

export default Documents;

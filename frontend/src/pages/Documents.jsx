import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import Modal from '../components/Modal';
import { FiUpload, FiDownload, FiTrash2, FiFile } from 'react-icons/fi';

const typeColors = { AADHAAR: 'badge-blue', PAN: 'badge-amber', RESUME: 'badge-green', OFFER_LETTER: 'badge-slate', OTHER: 'badge-slate' };

const Documents = () => {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [employeeId, setEmployeeId] = useState('');
  const [employees, setEmployees] = useState([]);
  const [showUpload, setShowUpload] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [uploadForm, setUploadForm] = useState({ employeeId: '', documentType: 'AADHAAR', file: null });
  const [uploading, setUploading] = useState(false);

  useEffect(() => { api.get('/employees').then(r => { if (r.data.success) setEmployees(r.data.data); }).catch(() => {}); }, []);

  const fetchDocs = async id => {
    if (!id) return;
    setLoading(true);
    try { const r = await api.get(`/documents/employee/${id}`); if (r.data.success) setDocuments(r.data.data); }
    catch { toast.error('Failed to load documents.'); setDocuments([]); }
    finally { setLoading(false); }
  };

  const handleEmployeeChange = id => { setEmployeeId(id); fetchDocs(id); };

  const handleUpload = async e => {
    e.preventDefault();
    if (!uploadForm.file) { toast.error('Select a file.'); return; }
    if (uploadForm.file.size > 5 * 1024 * 1024) { toast.error('File must be under 5MB.'); return; }
    if (!['application/pdf','image/jpeg','image/jpg','image/png'].includes(uploadForm.file.type)) { toast.error('Only PDF, JPG, PNG accepted.'); return; }

    setUploading(true);
    try {
      const fd = new FormData();
      fd.append('employeeId', uploadForm.employeeId);
      fd.append('documentType', uploadForm.documentType);
      fd.append('file', uploadForm.file);
      await api.post('/documents/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
      toast.success('Document uploaded.');
      setShowUpload(false);
      setUploadForm({ employeeId: '', documentType: 'AADHAAR', file: null });
      if (employeeId === uploadForm.employeeId) fetchDocs(employeeId);
    } catch (err) { toast.error(err.response?.data?.message || 'Upload failed.'); }
    finally { setUploading(false); }
  };

  const handleDelete = async () => {
    try { await api.delete(`/documents/${deleteTarget.documentId}`); toast.success('Document deleted.'); setShowDelete(false); fetchDocs(employeeId); }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed.'); }
  };

  return (
    <div className="space-y-6 fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="page-title">Document Vault</h2>
          <p className="page-subtitle">Upload and manage employee identity documents securely.</p>
        </div>
        <button onClick={() => setShowUpload(true)} className="btn-primary space-x-1.5">
          <FiUpload className="text-sm" /><span>Upload Document</span>
        </button>
      </div>

      {/* Employee Selector */}
      <div className="flex items-center space-x-3">
        <label className="text-sm font-medium text-slate-600">Select Employee:</label>
        <select value={employeeId} onChange={e => handleEmployeeChange(e.target.value)} className="form-input w-auto min-w-[220px]">
          <option value="">Choose employee...</option>
          {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
        </select>
      </div>

      {employeeId ? (
        <div className="card overflow-hidden">
          <table className="w-full border-collapse">
            <thead className="border-b border-slate-100">
              <tr>
                {['File Name', 'Document Type', 'Upload Date', ''].map(h => <th key={h} className="table-header">{h}</th>)}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {loading ? (
                <tr><td colSpan={4} className="py-12 text-center text-slate-400 text-sm">Loading...</td></tr>
              ) : documents.length === 0 ? (
                <tr><td colSpan={4} className="py-12 text-center text-slate-400 text-sm">No documents uploaded for this employee.</td></tr>
              ) : documents.map(doc => (
                <tr key={doc.documentId} className="hover:bg-slate-50/60 transition-colors">
                  <td className="table-cell">
                    <div className="flex items-center space-x-3">
                      <div className="w-8 h-8 bg-slate-100 rounded-lg flex items-center justify-center flex-shrink-0">
                        <FiFile className="text-slate-500 text-sm" />
                      </div>
                      <span className="font-medium text-slate-800 truncate max-w-[180px]">{doc.fileName}</span>
                    </div>
                  </td>
                  <td className="table-cell"><span className={typeColors[doc.documentType] || 'badge-slate'}>{doc.documentType}</span></td>
                  <td className="table-cell text-slate-500 text-xs">{doc.uploadDate ? new Date(doc.uploadDate).toLocaleDateString('en-IN') : '—'}</td>
                  <td className="table-cell text-right">
                    <div className="flex items-center justify-end space-x-1">
                      <button onClick={() => { toast.success('Downloading...'); window.open(`http://localhost:8080/api/documents/download/${doc.documentId}`, '_blank'); }}
                        className="p-1.5 text-slate-400 hover:text-[#2d4a8a] hover:bg-blue-50 rounded-lg transition-colors"><FiDownload className="text-sm" /></button>
                      <button onClick={() => { setDeleteTarget(doc); setShowDelete(true); }}
                        className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"><FiTrash2 className="text-sm" /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="card py-20 text-center text-slate-400 text-sm">
          Select an employee above to view their uploaded documents.
        </div>
      )}

      {/* Upload Modal */}
      <Modal open={showUpload} onClose={() => setShowUpload(false)} title="Upload Document" size="md">
        <form onSubmit={handleUpload} className="space-y-4">
          <div>
            <label className="form-label">Employee</label>
            <select value={uploadForm.employeeId} onChange={e => setUploadForm({ ...uploadForm, employeeId: e.target.value })} required className="form-input">
              <option value="">Select employee</option>
              {employees.map(emp => <option key={emp.employeeId} value={emp.employeeId}>{emp.firstName} {emp.lastName}</option>)}
            </select>
          </div>
          <div>
            <label className="form-label">Document Type</label>
            <select value={uploadForm.documentType} onChange={e => setUploadForm({ ...uploadForm, documentType: e.target.value })} className="form-input">
              {['AADHAAR','PAN','RESUME','OFFER_LETTER','OTHER'].map(t => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
            </select>
          </div>
          <div>
            <label className="form-label">File <span className="text-slate-400 normal-case font-normal">(PDF, JPG, PNG — max 5MB)</span></label>
            <input type="file" accept=".pdf,.jpg,.jpeg,.png"
              onChange={e => setUploadForm({ ...uploadForm, file: e.target.files[0] })}
              required
              className="block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border file:border-slate-300 file:text-xs file:font-medium file:bg-white file:text-slate-600 hover:file:bg-slate-50 file:transition-colors cursor-pointer" />
          </div>
          <div className="flex justify-end space-x-2 pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setShowUpload(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={uploading} className="btn-primary">{uploading ? 'Uploading...' : 'Upload'}</button>
          </div>
        </form>
      </Modal>

      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Delete Document" size="sm">
        <p className="text-sm text-slate-600 mb-5">Delete <strong className="text-slate-800">{deleteTarget?.fileName}</strong>? This cannot be undone.</p>
        <div className="flex justify-end space-x-2">
          <button onClick={() => setShowDelete(false)} className="btn-secondary">Cancel</button>
          <button onClick={handleDelete} className="btn-danger">Delete</button>
        </div>
      </Modal>
    </div>
  );
};

export default Documents;

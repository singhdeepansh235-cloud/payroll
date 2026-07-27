import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiSave, FiMapPin, FiMail, FiPhone, FiGlobe, FiSettings } from 'react-icons/fi';

const Settings = () => {
  const [settings, setSettings] = useState({ companyName: '', address: '', email: '', phone: '', website: '', financialYear: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isNew, setIsNew] = useState(false);

  useEffect(() => {
    api.get('/settings').then(r => {
      if (r.data.success && r.data.data) { setSettings(r.data.data); setIsNew(false); }
      else { setIsNew(true); }
    }).catch(() => setIsNew(true)).finally(() => setLoading(false));
  }, []);

  const handleSave = async e => {
    e.preventDefault(); setSaving(true);
    try {
      if (isNew) { await api.post('/settings', settings); toast.success('Settings created.'); setIsNew(false); }
      else { await api.put('/settings', settings); toast.success('Settings updated.'); }
    } catch (err) { toast.error(err.response?.data?.message || 'Save failed.'); }
    finally { setSaving(false); }
  };

  const c = e => setSettings({ ...settings, [e.target.name]: e.target.value });

  if (loading) return <div className="py-20 text-center text-slate-400 text-sm">Loading settings...</div>;

  return (
    <div className="space-y-6 fade-in max-w-3xl">
      <div>
        <h2 className="page-title">Company Settings</h2>
        <p className="page-subtitle">Configure company identity. These details appear on payslips and all generated reports.</p>
      </div>

      {/* Preview */}
      <div className="card px-6 py-5">
        <p className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">Live Preview</p>
        <div className="flex items-start space-x-4">
          <div className="w-12 h-12 bg-[#1e2d4a] rounded-xl flex items-center justify-center text-white font-bold text-lg flex-shrink-0">
            {settings.companyName?.charAt(0) || 'C'}
          </div>
          <div>
            <h3 className="font-bold text-slate-800">{settings.companyName || 'Company Name'}</h3>
            <p className="text-xs text-slate-400 mt-0.5">{settings.address || 'Address not set'}</p>
            <div className="flex flex-wrap gap-3 mt-2 text-xs text-slate-500">
              {settings.email && <span className="flex items-center space-x-1"><FiMail className="text-slate-400" /><span>{settings.email}</span></span>}
              {settings.phone && <span className="flex items-center space-x-1"><FiPhone className="text-slate-400" /><span>{settings.phone}</span></span>}
              {settings.website && <span className="flex items-center space-x-1"><FiGlobe className="text-slate-400" /><span>{settings.website}</span></span>}
            </div>
          </div>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSave} className="card p-8 space-y-5">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div>
            <label className="form-label">Company Name *</label>
            <input type="text" name="companyName" value={settings.companyName} onChange={c} required placeholder="SRMCEM Pvt. Ltd." className="form-input" />
          </div>
          <div>
            <label className="form-label">Financial Year *</label>
            <input type="text" name="financialYear" value={settings.financialYear} onChange={c} required placeholder="2026-2027" className="form-input" />
          </div>
          <div>
            <label className="form-label">Contact Email</label>
            <div className="relative">
              <FiMail className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
              <input type="email" name="email" value={settings.email} onChange={c} placeholder="hr@company.com" className="form-input pl-9" />
            </div>
          </div>
          <div>
            <label className="form-label">Contact Phone</label>
            <div className="relative">
              <FiPhone className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
              <input type="text" name="phone" value={settings.phone} onChange={c} placeholder="+91-1234567890" className="form-input pl-9" />
            </div>
          </div>
          <div className="md:col-span-2">
            <label className="form-label">Website</label>
            <div className="relative">
              <FiGlobe className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
              <input type="url" name="website" value={settings.website} onChange={c} placeholder="https://www.company.com" className="form-input pl-9" />
            </div>
          </div>
          <div className="md:col-span-2">
            <label className="form-label">Office Address</label>
            <div className="relative">
              <FiMapPin className="absolute left-3.5 top-3 text-slate-400 text-sm" />
              <textarea name="address" value={settings.address} onChange={c} placeholder="Full office address..." className="form-input pl-9 h-20 resize-none" />
            </div>
          </div>
        </div>
        <div className="flex justify-end pt-2 border-t border-slate-100">
          <button type="submit" disabled={saving} className="btn-primary space-x-1.5">
            <FiSave className="text-sm" /><span>{saving ? 'Saving...' : isNew ? 'Create Settings' : 'Save Changes'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default Settings;

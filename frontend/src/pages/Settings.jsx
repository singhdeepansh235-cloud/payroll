import React, { useEffect, useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiSettings, FiSave, FiGlobe, FiMail, FiPhone, FiMapPin } from 'react-icons/fi';

const Settings = () => {
  const [settings, setSettings] = useState({
    companyName: '', address: '', email: '', phone: '', website: '', financialYear: '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isNew, setIsNew] = useState(false);

  const fetchSettings = async () => {
    try {
      const response = await api.get('/settings');
      if (response.data.success && response.data.data) {
        setSettings(response.data.data);
        setIsNew(false);
      } else {
        setIsNew(true);
      }
    } catch (err) {
      setIsNew(true);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchSettings(); }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (isNew) {
        await api.post('/settings', settings);
        toast.success('Company settings created successfully!');
      } else {
        await api.put('/settings', settings);
        toast.success('Company settings updated successfully!');
      }
      fetchSettings();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save settings.');
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (e) => setSettings({ ...settings, [e.target.name]: e.target.value });

  const fields = [
    { name: 'companyName', label: 'Company Name', icon: <FiSettings />, type: 'text', required: true, placeholder: 'SRMCEM Pvt. Ltd.' },
    { name: 'financialYear', label: 'Financial Year', icon: <FiSettings />, type: 'text', required: true, placeholder: '2026-2027' },
    { name: 'email', label: 'Contact Email', icon: <FiMail />, type: 'email', required: false, placeholder: 'hr@company.com' },
    { name: 'phone', label: 'Contact Phone', icon: <FiPhone />, type: 'text', required: false, placeholder: '+91-1234567890' },
    { name: 'website', label: 'Company Website', icon: <FiGlobe />, type: 'url', required: false, placeholder: 'https://www.company.com', span: 2 },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <svg className="animate-spin h-8 w-8 text-sky-400" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-2xl font-bold text-slate-100">Company Settings</h2>
        <p className="text-slate-400 text-sm mt-1">Configure company identity — these details appear on payslips and reports.</p>
      </div>

      {/* Preview Card */}
      <div className="p-6 bg-gradient-to-br from-sky-500/5 to-indigo-500/5 border border-slate-700/30 rounded-2xl">
        <div className="flex items-center space-x-3 mb-3">
          <div className="p-2 bg-sky-500/20 text-sky-400 rounded-lg"><FiSettings /></div>
          <div>
            <h3 className="font-bold text-slate-200">{settings.companyName || 'Company Name'}</h3>
            <p className="text-xs text-slate-400">{settings.website || 'No website configured'}</p>
          </div>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
          <div className="flex items-center space-x-2 text-slate-400">
            <FiMail className="text-sky-400 flex-shrink-0" />
            <span className="truncate">{settings.email || 'Not set'}</span>
          </div>
          <div className="flex items-center space-x-2 text-slate-400">
            <FiPhone className="text-sky-400 flex-shrink-0" />
            <span>{settings.phone || 'Not set'}</span>
          </div>
          <div className="flex items-center space-x-2 text-slate-400">
            <FiSettings className="text-sky-400 flex-shrink-0" />
            <span>FY {settings.financialYear || 'Not set'}</span>
          </div>
          <div className="flex items-center space-x-2 text-slate-400">
            <FiMapPin className="text-sky-400 flex-shrink-0" />
            <span className="truncate">{settings.address || 'Not set'}</span>
          </div>
        </div>
      </div>

      {/* Edit Form */}
      <form onSubmit={handleSave} className="bg-[#1e293b]/40 border border-slate-700/50 p-8 rounded-2xl space-y-6 backdrop-blur-md">
        <h3 className="text-lg font-semibold text-slate-200">{isNew ? 'Create Settings' : 'Update Settings'}</h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {fields.map((field) => (
            <div key={field.name} className={field.span === 2 ? 'md:col-span-2' : ''}>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">{field.label}</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">{field.icon}</span>
                <input
                  type={field.type}
                  name={field.name}
                  value={settings[field.name] || ''}
                  onChange={handleChange}
                  required={field.required}
                  placeholder={field.placeholder}
                  className="w-full pl-10 pr-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 placeholder-slate-500 outline-none transition-all text-sm"
                />
              </div>
            </div>
          ))}
          <div className="md:col-span-2">
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Office Address</label>
            <div className="relative">
              <span className="absolute top-3 left-3.5 text-slate-500"><FiMapPin /></span>
              <textarea
                name="address"
                value={settings.address || ''}
                onChange={handleChange}
                placeholder="Full office address..."
                className="w-full pl-10 pr-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 placeholder-slate-500 outline-none transition-all text-sm h-20 resize-none"
              />
            </div>
          </div>
        </div>

        <div className="flex justify-end">
          <button type="submit" disabled={saving} className="flex items-center space-x-2 px-6 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] disabled:opacity-50 text-sm">
            <FiSave />
            <span>{saving ? 'Saving...' : isNew ? 'Create Settings' : 'Save Changes'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default Settings;

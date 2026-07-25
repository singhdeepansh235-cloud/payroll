import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiUser, FiLock, FiMail, FiShield, FiEye, FiEyeOff } from 'react-icons/fi';

const Profile = () => {
  const { user } = useAuth();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (newPassword.length < 6) {
      toast.error('New password must be at least 6 characters.');
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error('New passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/auth/change-password', {
        currentPassword, newPassword, confirmPassword,
      });
      if (response.data.success) {
        toast.success('Password changed successfully!');
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to change password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-2xl font-bold text-slate-100">Admin Profile</h2>
        <p className="text-slate-400 text-sm mt-1">Manage your account details and security credentials.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* User Card */}
        <div className="bg-gradient-to-br from-[#1e293b]/60 to-[#0f172a]/60 border border-slate-700/50 p-6 rounded-2xl flex flex-col items-center justify-center text-center backdrop-blur-md">
          <div className="relative mb-4">
            <div className="p-5 bg-gradient-to-br from-sky-500/20 to-indigo-500/20 text-sky-400 rounded-full ring-2 ring-sky-500/20">
              <FiUser className="text-4xl" />
            </div>
            <div className="absolute -bottom-1 -right-1 p-1.5 bg-emerald-500 rounded-full ring-2 ring-[#151f32]">
              <div className="w-2 h-2 rounded-full bg-white"></div>
            </div>
          </div>
          <h3 className="text-lg font-bold text-slate-200">{user?.name || 'Admin User'}</h3>
          <span className="inline-flex items-center space-x-1 mt-1 px-2.5 py-0.5 bg-sky-500/10 text-sky-400 border border-sky-500/20 rounded-full text-xs font-semibold">
            <FiShield className="text-[10px]" /><span>ADMINISTRATOR</span>
          </span>

          <div className="w-full border-t border-slate-700/50 my-5"></div>

          <div className="text-left w-full space-y-3">
            <div className="flex items-center space-x-3 p-3 bg-[#0d1523]/50 rounded-xl">
              <FiUser className="text-sky-400 flex-shrink-0" />
              <div>
                <p className="text-[10px] font-semibold text-slate-500 uppercase tracking-wider">Username</p>
                <p className="text-sm text-slate-200 font-mono">{user?.username || 'admin'}</p>
              </div>
            </div>
            <div className="flex items-center space-x-3 p-3 bg-[#0d1523]/50 rounded-xl">
              <FiMail className="text-sky-400 flex-shrink-0" />
              <div>
                <p className="text-[10px] font-semibold text-slate-500 uppercase tracking-wider">Email</p>
                <p className="text-sm text-slate-200">{user?.email || 'admin@srmcem.ac.in'}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Change Password Form */}
        <div className="md:col-span-2 bg-[#1e293b]/40 border border-slate-700/50 p-8 rounded-2xl backdrop-blur-md">
          <div className="flex items-center space-x-3 mb-6">
            <div className="p-2 bg-sky-500/20 text-sky-400 rounded-lg"><FiLock /></div>
            <div>
              <h3 className="text-lg font-semibold text-slate-200">Change Password</h3>
              <p className="text-xs text-slate-400">Update your admin account credentials.</p>
            </div>
          </div>

          <form onSubmit={handleChangePassword} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Current Password</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500"><FiLock /></span>
                <input
                  type={showCurrent ? 'text' : 'password'}
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  required
                  className="w-full pl-10 pr-10 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none text-sm"
                />
                <button type="button" onClick={() => setShowCurrent(!showCurrent)} className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-500 hover:text-slate-300">
                  {showCurrent ? <FiEyeOff className="text-sm" /> : <FiEye className="text-sm" />}
                </button>
              </div>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">New Password <span className="text-slate-500">(min 6 characters)</span></label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500"><FiLock /></span>
                <input
                  type={showNew ? 'text' : 'password'}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  className="w-full pl-10 pr-10 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none text-sm"
                />
                <button type="button" onClick={() => setShowNew(!showNew)} className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-500 hover:text-slate-300">
                  {showNew ? <FiEyeOff className="text-sm" /> : <FiEye className="text-sm" />}
                </button>
              </div>
              {/* Password strength indicator */}
              {newPassword && (
                <div className="mt-2 flex items-center space-x-2">
                  <div className="flex-1 h-1 rounded-full bg-slate-700 overflow-hidden">
                    <div className={`h-full rounded-full transition-all ${
                      newPassword.length >= 10 ? 'w-full bg-emerald-500' :
                      newPassword.length >= 6 ? 'w-2/3 bg-amber-500' :
                      'w-1/3 bg-rose-500'
                    }`}></div>
                  </div>
                  <span className={`text-[10px] font-semibold ${
                    newPassword.length >= 10 ? 'text-emerald-400' :
                    newPassword.length >= 6 ? 'text-amber-400' :
                    'text-rose-400'
                  }`}>
                    {newPassword.length >= 10 ? 'Strong' : newPassword.length >= 6 ? 'Medium' : 'Weak'}
                  </span>
                </div>
              )}
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Confirm New Password</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500"><FiLock /></span>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  className="w-full pl-10 pr-4 py-2.5 bg-[#0d1523] border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 rounded-xl text-slate-200 outline-none text-sm"
                />
              </div>
              {confirmPassword && newPassword !== confirmPassword && (
                <p className="text-rose-400 text-xs mt-1">Passwords do not match.</p>
              )}
            </div>
            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="flex items-center space-x-2 px-6 py-2.5 bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-semibold rounded-xl transition-all shadow-lg shadow-sky-500/20 active:scale-[0.98] disabled:opacity-50 text-sm"
              >
                <FiLock />
                <span>{loading ? 'Updating...' : 'Update Password'}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Profile;

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
    <div className="space-y-6 max-w-4xl fade-in">
      <div>
        <h2 className="page-title">My Profile</h2>
        <p className="page-subtitle">Manage system administrator credentials and security settings.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* User Profile Overview */}
        <div className="card p-6 flex flex-col items-center justify-center text-center">
          <div className="w-16 h-16 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-[#2d4a8a] text-2xl font-bold mb-3 shadow-sm">
            {user?.username ? user.username.charAt(0).toUpperCase() : 'A'}
          </div>
          <h3 className="text-base font-bold text-slate-800">{user?.name || 'System Administrator'}</h3>
          <span className="inline-flex items-center space-x-1 mt-1.5 px-2.5 py-0.5 bg-blue-50 text-[#2d4a8a] border border-blue-200 rounded-full text-xs font-semibold">
            <FiShield className="text-[10px]" />
            <span>ROLE_ADMIN</span>
          </span>

          <div className="w-full border-t border-slate-100 my-5" />

          <div className="text-left w-full space-y-3">
            <div className="flex items-center space-x-3 p-3 bg-slate-50 border border-slate-100 rounded-lg">
              <FiUser className="text-slate-400 text-sm flex-shrink-0" />
              <div>
                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Username</p>
                <p className="text-xs text-slate-700 font-mono font-semibold">{user?.username || 'admin'}</p>
              </div>
            </div>
            <div className="flex items-center space-x-3 p-3 bg-slate-50 border border-slate-100 rounded-lg">
              <FiMail className="text-slate-400 text-sm flex-shrink-0" />
              <div>
                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Email Address</p>
                <p className="text-xs text-slate-700 font-mono font-semibold">{user?.email || 'admin@srmcem.ac.in'}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Change Password Form */}
        <div className="md:col-span-2 card p-6">
          <div className="flex items-center space-x-2.5 mb-5 pb-3 border-b border-slate-100">
            <div className="p-2 bg-slate-100 text-slate-500 border border-slate-200 rounded-lg">
              <FiLock className="text-base" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-slate-800">Update Account Password</h3>
              <p className="text-xs text-slate-400">Security rule: Requires active credentials to update.</p>
            </div>
          </div>

          <form onSubmit={handleChangePassword} className="space-y-4">
            <div>
              <label className="form-label">Current Password</label>
              <div className="relative">
                <FiLock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
                <input
                  type={showCurrent ? 'text' : 'password'}
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  required
                  placeholder="••••••••"
                  className="form-input pl-9"
                />
                <button
                  type="button"
                  onClick={() => setShowCurrent(!showCurrent)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showCurrent ? <FiEyeOff className="text-sm" /> : <FiEye className="text-sm" />}
                </button>
              </div>
            </div>

            <div>
              <label className="form-label">New Password</label>
              <div className="relative">
                <FiLock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
                <input
                  type={showNew ? 'text' : 'password'}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  placeholder="At least 6 characters"
                  className="form-input pl-9"
                />
                <button
                  type="button"
                  onClick={() => setShowNew(!showNew)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showNew ? <FiEyeOff className="text-sm" /> : <FiEye className="text-sm" />}
                </button>
              </div>
            </div>

            <div>
              <label className="form-label">Confirm New Password</label>
              <div className="relative">
                <FiLock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  placeholder="Confirm new password"
                  className="form-input pl-9"
                />
              </div>
              {confirmPassword && newPassword !== confirmPassword && (
                <p className="text-red-600 text-xs mt-1.5 font-medium">Passwords do not match.</p>
              )}
            </div>

            <div className="pt-3 border-t border-slate-100 flex justify-end">
              <button
                type="submit"
                disabled={loading}
                className="btn-primary"
              >
                <FiLock className="text-xs" />
                <span>{loading ? 'Updating Credentials...' : 'Save New Password'}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Profile;

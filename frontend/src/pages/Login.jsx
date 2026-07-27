import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FiLock, FiUser, FiShield } from 'react-icons/fi';
import toast from 'react-hot-toast';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const result = await login(username, password);
    setLoading(false);
    if (result.success) {
      toast.success('Signed in successfully.');
      navigate('/');
    } else {
      toast.error(result.message || 'Invalid credentials');
    }
  };

  return (
    <div className="min-h-screen w-screen flex bg-slate-100">
      {/* Left Brand Panel */}
      <div className="hidden lg:flex w-1/2 bg-[#1e2d4a] flex-col justify-between p-12">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-lg bg-white/15 flex items-center justify-center text-white font-bold text-base">
            S
          </div>
          <div>
            <p className="text-white font-bold text-base">SRMCEM Payroll</p>
            <p className="text-white/40 text-xs">Employee Management Portal</p>
          </div>
        </div>

        <div className="space-y-6">
          <div>
            <h2 className="text-3xl font-bold text-white leading-snug">
              Streamlined Payroll<br />Management System
            </h2>
            <p className="mt-3 text-white/55 text-sm leading-relaxed">
              A comprehensive solution for managing employees, attendance, leave approvals, payroll generation, and company documents — all in one place.
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {[
              { label: 'Employee Management', icon: '👥' },
              { label: 'Payroll Processing', icon: '💰' },
              { label: 'Attendance Tracking', icon: '📅' },
              { label: 'Leave Approvals', icon: '✅' },
            ].map((f, i) => (
              <div key={i} className="flex items-center space-x-2 bg-white/8 rounded-lg px-3 py-2.5">
                <span className="text-base">{f.icon}</span>
                <span className="text-white/75 text-xs font-medium">{f.label}</span>
              </div>
            ))}
          </div>
        </div>

        <p className="text-white/25 text-xs">
          © {new Date().getFullYear()} SRMCEM. For internal use only.
        </p>
      </div>

      {/* Right Login Form Panel */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-sm">
          {/* Logo (mobile only) */}
          <div className="flex items-center space-x-3 mb-8 lg:hidden">
            <div className="w-9 h-9 rounded-lg bg-[#1e2d4a] flex items-center justify-center text-white font-bold">S</div>
            <div>
              <p className="font-bold text-slate-800">SRMCEM Payroll</p>
              <p className="text-slate-400 text-xs">Employee Management Portal</p>
            </div>
          </div>

          <div className="bg-white border border-slate-200 rounded-2xl shadow-sm p-8">
            <div className="mb-7">
              <div className="w-11 h-11 rounded-xl bg-slate-100 flex items-center justify-center mb-4">
                <FiShield className="text-[#1e2d4a] text-xl" />
              </div>
              <h2 className="text-xl font-bold text-slate-800">Sign In</h2>
              <p className="text-sm text-slate-500 mt-1">Enter your credentials to access the portal.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="form-label">Username</label>
                <div className="relative">
                  <FiUser className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
                  <input
                    type="text"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    required
                    placeholder="admin"
                    className="form-input pl-9"
                  />
                </div>
              </div>

              <div>
                <label className="form-label">Password</label>
                <div className="relative">
                  <FiLock className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" />
                  <input
                    type="password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                    placeholder="••••••••"
                    className="form-input pl-9"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn-primary w-full justify-center py-3"
              >
                {loading ? (
                  <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                ) : null}
                <span>{loading ? 'Signing in...' : 'Sign In'}</span>
              </button>
            </form>
          </div>

          <p className="text-center text-xs text-slate-400 mt-5">
            SRMCEM Employee Payroll Management System
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;

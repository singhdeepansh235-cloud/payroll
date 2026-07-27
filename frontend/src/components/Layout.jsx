import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  FiGrid, FiUsers, FiBriefcase, FiCalendar, FiFileText,
  FiDollarSign, FiFolder, FiPieChart, FiSettings,
  FiShield, FiUser, FiLogOut, FiAward
} from 'react-icons/fi';

const navGroups = [
  {
    group: 'Overview',
    items: [
      { name: 'Dashboard', path: '/', icon: FiGrid },
    ],
  },
  {
    group: 'Workforce & HR',
    items: [
      { name: 'Employees', path: '/employees', icon: FiUsers },
      { name: 'Departments', path: '/departments', icon: FiBriefcase },
      { name: 'Designations', path: '/designations', icon: FiAward },
      { name: 'Attendance', path: '/attendance', icon: FiCalendar },
      { name: 'Leaves', path: '/leaves', icon: FiFileText },
    ],
  },
  {
    group: 'Finance & Admin',
    items: [
      { name: 'Payroll', path: '/payroll', icon: FiDollarSign },
      { name: 'Documents', path: '/documents', icon: FiFolder },
      { name: 'Reports', path: '/reports', icon: FiPieChart },
    ],
  },
  {
    group: 'System',
    items: [
      { name: 'Company Settings', path: '/settings', icon: FiSettings },
      { name: 'Audit Logs', path: '/audit-logs', icon: FiShield },
      { name: 'My Profile', path: '/profile', icon: FiUser },
    ],
  },
];

const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const currentPage = navGroups.flatMap(g => g.items).find(i => i.path === location.pathname);

  return (
    <div className="flex h-screen bg-slate-100 overflow-hidden" style={{ fontFamily: "'Inter', sans-serif" }}>

      {/* ── Sidebar ─────────────────────────────────────────────────── */}
      <aside className="w-60 bg-[#1e2d4a] flex flex-col flex-shrink-0 overflow-hidden">

        {/* Brand */}
        <div className="h-16 flex items-center px-5 border-b border-white/10 flex-shrink-0">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-lg bg-white/15 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
              S
            </div>
            <div>
              <p className="text-white font-bold text-sm leading-tight">SRMCEM</p>
              <p className="text-white/50 text-[10px] tracking-wide uppercase">Payroll Portal</p>
            </div>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto sidebar-scroll py-4 px-3 space-y-5">
          {navGroups.map((group, idx) => (
            <div key={idx}>
              <p className="px-2 mb-1.5 text-[9px] font-bold uppercase tracking-widest text-white/35">
                {group.group}
              </p>
              <div className="space-y-0.5">
                {group.items.map(item => {
                  const Icon = item.icon;
                  const active = location.pathname === item.path;
                  return (
                    <Link
                      key={item.name}
                      to={item.path}
                      className={`flex items-center space-x-2.5 px-3 py-2.5 rounded-lg text-[13px] font-medium transition-all duration-150 ${
                        active
                          ? 'bg-white/15 text-white shadow-sm'
                          : 'text-white/55 hover:bg-white/8 hover:text-white/90'
                      }`}
                    >
                      <Icon className={`text-[15px] flex-shrink-0 ${active ? 'text-white' : 'text-white/50'}`} />
                      <span>{item.name}</span>
                    </Link>
                  );
                })}
              </div>
            </div>
          ))}
        </nav>

        {/* Footer */}
        <div className="px-3 py-3 border-t border-white/10 flex-shrink-0">
          <div className="flex items-center justify-between px-2 py-2 rounded-lg hover:bg-white/8 transition-colors group">
            <div className="flex items-center space-x-2.5 min-w-0">
              <div className="w-7 h-7 rounded-full bg-white/20 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
                {user?.username?.charAt(0).toUpperCase() || 'A'}
              </div>
              <div className="min-w-0">
                <p className="text-xs font-semibold text-white/85 truncate">{user?.username || 'admin'}</p>
                <p className="text-[10px] text-white/40">System Admin</p>
              </div>
            </div>
            <button
              onClick={handleLogout}
              title="Sign Out"
              className="text-white/40 hover:text-red-400 transition-colors ml-2"
            >
              <FiLogOut className="text-sm" />
            </button>
          </div>
        </div>
      </aside>

      {/* ── Main Content ─────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">

        {/* Top Header */}
        <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 flex-shrink-0 shadow-sm">
          <div>
            <h1 className="text-[15px] font-semibold text-slate-800">
              {currentPage?.name || 'Dashboard'}
            </h1>
            <p className="text-[11px] text-slate-400">
              {new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-1.5 px-3 py-1.5 bg-emerald-50 border border-emerald-200 rounded-full text-xs text-emerald-700 font-medium">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              <span>System Online</span>
            </div>
          </div>
        </header>

        {/* Page Body */}
        <main className="flex-1 overflow-y-auto p-6 lg:p-8 fade-in">
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;

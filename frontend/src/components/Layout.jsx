import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  FiUsers,
  FiBriefcase,
  FiCalendar,
  FiFileText,
  FiLogOut,
  FiSliders,
  FiActivity,
  FiFolder,
  FiSettings,
  FiShield
} from 'react-icons/fi';

const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', path: '/', icon: <FiSliders /> },
    { name: 'Employees', path: '/employees', icon: <FiUsers /> },
    { name: 'Departments', path: '/departments', icon: <FiBriefcase /> },
    { name: 'Designations', path: '/designations', icon: <FiBriefcase /> },
    { name: 'Attendance', path: '/attendance', icon: <FiCalendar /> },
    { name: 'Leaves', path: '/leaves', icon: <FiFileText /> },
    { name: 'Payroll', path: '/payroll', icon: <FiActivity /> },
    { name: 'Documents', path: '/documents', icon: <FiFolder /> },
    { name: 'Reports Engine', path: '/reports', icon: <FiFileText /> },
    { name: 'Company Settings', path: '/settings', icon: <FiSettings /> },
    { name: 'Audit Logs', path: '/audit-logs', icon: <FiShield /> },
    { name: 'Admin Profile', path: '/profile', icon: <FiUsers /> },
  ];

  return (
    <div className="flex h-screen bg-[#0f172a] text-slate-100 overflow-hidden">
      {/* Sidebar */}
      <aside className="w-64 bg-[#1e293b] flex flex-col justify-between border-r border-slate-700">
        <div>
          {/* Logo / Title */}
          <div className="h-16 flex items-center px-6 bg-[#0f172a] border-b border-slate-700">
            <span className="text-xl font-bold tracking-wider bg-gradient-to-r from-sky-400 to-indigo-400 bg-clip-text text-transparent">
              SRMCEM Payroll
            </span>
          </div>

          {/* Nav Links */}
          <nav className="p-4 space-y-1">
            {navItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.name}
                  to={item.path}
                  className={`flex items-center space-x-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/20'
                      : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                  }`}
                >
                  <span className="text-lg">{item.icon}</span>
                  <span>{item.name}</span>
                </Link>
              );
            })}
          </nav>
        </div>

        {/* Footer / User Profile */}
        <div className="p-4 border-t border-slate-700 bg-[#0f172a] flex items-center justify-between">
          <div className="flex flex-col">
            <span className="text-xs text-slate-400">Logged in as</span>
            <span className="text-sm font-semibold text-slate-200">{user?.username || 'Admin'}</span>
          </div>
          <button
            onClick={handleLogout}
            className="p-2 rounded-lg text-slate-400 hover:text-red-400 hover:bg-slate-800 transition-colors"
            title="Log Out"
          >
            <FiLogOut className="text-lg" />
          </button>
        </div>
      </aside>

      {/* Main Content Pane */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Navbar */}
        <header className="h-16 bg-[#1e293b] flex items-center justify-between px-8 border-b border-slate-700">
          <h1 className="text-lg font-semibold text-slate-200">
            {navItems.find((item) => item.path === location.pathname)?.name || 'Payroll Portal'}
          </h1>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-slate-400">{new Date().toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
          </div>
        </header>

        {/* Dashboard Pages */}
        <main className="flex-1 overflow-y-auto p-8 bg-[#0f172a]">
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;

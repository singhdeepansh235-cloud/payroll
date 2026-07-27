import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiUsers, FiBriefcase, FiCalendar, FiDollarSign, FiArrowRight, FiClock } from 'react-icons/fi';

const StatCard = ({ title, value, icon: Icon, color, link }) => (
  <Link
    to={link}
    className="bg-white border border-slate-200 rounded-xl p-5 flex items-center justify-between hover:shadow-md hover:border-slate-300 transition-all duration-200 group"
  >
    <div>
      <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{title}</p>
      <p className="text-2xl font-bold text-slate-800 mt-1.5">{value}</p>
    </div>
    <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${color} group-hover:scale-105 transition-transform`}>
      <Icon className="text-xl" />
    </div>
  </Link>
);

const Dashboard = () => {
  const [stats, setStats] = useState({ totalEmployees: 0, totalDepartments: 0, activeLeaves: 0, totalPayrollGenerated: 0 });
  const [recentLogs, setRecentLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [sRes, lRes] = await Promise.allSettled([
          api.get('/dashboard/summary'),
          api.get('/audit-logs', { params: { page: 0, size: 6, sort: 'timestamp', direction: 'DESC' } }),
        ]);
        if (sRes.status === 'fulfilled' && sRes.value.data.success) setStats(sRes.value.data.data);
        if (lRes.status === 'fulfilled' && lRes.value.data.success) {
          const d = lRes.value.data.data;
          setRecentLogs(Array.isArray(d) ? d.slice(0, 6) : (d.content || []).slice(0, 6));
        }
      } catch { toast.error('Failed to load dashboard.'); }
      finally { setLoading(false); }
    };
    fetch();
  }, []);

  const cards = [
    { title: 'Total Employees', value: stats.totalEmployees, icon: FiUsers, color: 'bg-blue-50 text-[#2d4a8a]', link: '/employees' },
    { title: 'Departments', value: stats.totalDepartments, icon: FiBriefcase, color: 'bg-emerald-50 text-emerald-700', link: '/departments' },
    { title: 'Pending Leaves', value: stats.activeLeaves, icon: FiCalendar, color: 'bg-amber-50 text-amber-700', link: '/leaves' },
    { title: 'Monthly Payroll', value: `₹${(stats.totalPayrollGenerated || 0).toLocaleString()}`, icon: FiDollarSign, color: 'bg-slate-100 text-slate-700', link: '/payroll' },
  ];

  const quickActions = [
    { name: 'Manage Employees', path: '/employees', desc: 'Add or update employee records' },
    { name: 'Run Payroll Cycle', path: '/payroll', desc: 'Generate monthly salary processing' },
    { name: 'Review Leaves', path: '/leaves', desc: 'Approve or reject pending requests' },
    { name: 'Download Reports', path: '/reports', desc: 'Export payroll and HR reports' },
  ];

  const moduleBadgeMap = {
    EMPLOYEE: 'badge-blue', DEPARTMENT: 'badge-blue', LEAVE: 'badge-amber',
    PAYROLL: 'badge-green', ATTENDANCE: 'badge-slate', REPORT: 'badge-slate',
    AUTH: 'badge-red', SETTINGS: 'badge-slate',
  };

  return (
    <div className="space-y-7 fade-in">
      <div>
        <h2 className="page-title">Overview</h2>
        <p className="page-subtitle">Key metrics and activity for SRMCEM Payroll.</p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {cards.map((c, i) => (
          <StatCard key={i} {...c} value={loading ? '...' : c.value} />
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Quick Actions */}
        <div className="card">
          <div className="card-header">
            <h3 className="text-sm font-semibold text-slate-700">Quick Actions</h3>
          </div>
          <div className="divide-y divide-slate-100">
            {quickActions.map((a, i) => (
              <Link key={i} to={a.path} className="flex items-center justify-between px-6 py-4 hover:bg-slate-50 transition-colors group">
                <div>
                  <p className="text-sm font-medium text-slate-700 group-hover:text-[#2d4a8a]">{a.name}</p>
                  <p className="text-xs text-slate-400 mt-0.5">{a.desc}</p>
                </div>
                <FiArrowRight className="text-slate-300 group-hover:text-[#2d4a8a] text-sm transition-colors" />
              </Link>
            ))}
          </div>
        </div>

        {/* Recent Activity */}
        <div className="card lg:col-span-2">
          <div className="card-header">
            <h3 className="text-sm font-semibold text-slate-700 flex items-center space-x-2">
              <FiClock className="text-slate-400" />
              <span>Recent Activity</span>
            </h3>
          </div>
          {recentLogs.length === 0 ? (
            <p className="text-slate-400 text-sm px-6 py-8 text-center">No recent activity recorded.</p>
          ) : (
            <div className="divide-y divide-slate-100">
              {recentLogs.map((log, i) => (
                <div key={i} className="flex items-center justify-between px-6 py-3.5 hover:bg-slate-50 transition-colors">
                  <div className="flex items-center space-x-3 min-w-0">
                    <span className={moduleBadgeMap[log.module] || 'badge-slate'}>{log.module}</span>
                    <p className="text-sm text-slate-600 truncate">{log.action}</p>
                  </div>
                  <span className="text-xs text-slate-400 flex-shrink-0 ml-4">
                    {log.timestamp ? new Date(log.timestamp).toLocaleString('en-IN', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : ''}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

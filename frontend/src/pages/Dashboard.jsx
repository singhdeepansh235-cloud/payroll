import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import toast from 'react-hot-toast';
import { FiUsers, FiBriefcase, FiCalendar, FiDollarSign, FiArrowRight, FiActivity, FiTrendingUp, FiClock } from 'react-icons/fi';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalEmployees: 0,
    totalDepartments: 0,
    activeLeaves: 0,
    totalPayrollGenerated: 0,
  });
  const [recentLogs, setRecentLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const [statsRes, logsRes] = await Promise.allSettled([
          api.get('/dashboard/summary'),
          api.get('/audit-logs', { params: { page: 0, size: 5, sort: 'timestamp', direction: 'DESC' } }),
        ]);

        if (statsRes.status === 'fulfilled' && statsRes.value.data.success) {
          setStats(statsRes.value.data.data);
        }
        if (logsRes.status === 'fulfilled' && logsRes.value.data.success) {
          const logsData = logsRes.value.data.data;
          setRecentLogs(Array.isArray(logsData) ? logsData.slice(0, 5) : (logsData.content || []).slice(0, 5));
        }
      } catch (err) {
        toast.error('Failed to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  const cards = [
    {
      title: 'Total Employees',
      value: stats.totalEmployees,
      icon: <FiUsers className="text-2xl" />,
      color: 'from-sky-500 to-cyan-400',
      bg: 'bg-sky-500/10',
      iconBg: 'bg-sky-500/20 text-sky-400',
      link: '/employees',
    },
    {
      title: 'Departments',
      value: stats.totalDepartments,
      icon: <FiBriefcase className="text-2xl" />,
      color: 'from-emerald-500 to-teal-400',
      bg: 'bg-emerald-500/10',
      iconBg: 'bg-emerald-500/20 text-emerald-400',
      link: '/departments',
    },
    {
      title: 'Pending Leaves',
      value: stats.activeLeaves,
      icon: <FiCalendar className="text-2xl" />,
      color: 'from-amber-500 to-orange-400',
      bg: 'bg-amber-500/10',
      iconBg: 'bg-amber-500/20 text-amber-400',
      link: '/leaves',
    },
    {
      title: 'Monthly Payroll',
      value: `₹${(stats.totalPayrollGenerated || 0).toLocaleString()}`,
      icon: <FiDollarSign className="text-2xl" />,
      color: 'from-indigo-500 to-violet-400',
      bg: 'bg-indigo-500/10',
      iconBg: 'bg-indigo-500/20 text-indigo-400',
      link: '/payroll',
    },
  ];

  const quickActions = [
    { name: 'Add Employee', path: '/employees', icon: <FiUsers />, desc: 'Register new staff member' },
    { name: 'Run Payroll', path: '/payroll', icon: <FiDollarSign />, desc: 'Generate monthly salary' },
    { name: 'Review Leaves', path: '/leaves', icon: <FiCalendar />, desc: 'Approve or reject requests' },
    { name: 'View Reports', path: '/reports', icon: <FiTrendingUp />, desc: 'Download formatted reports' },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-100">Dashboard Overview</h2>
        <p className="text-slate-400 text-sm mt-1">Key metrics and statistics for SRMCEM Payroll management.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {cards.map((card, idx) => (
          <Link
            to={card.link}
            key={idx}
            className={`group p-6 ${card.bg} border border-slate-700/30 rounded-2xl flex items-center justify-between transition-all duration-300 hover:scale-[1.03] hover:shadow-lg hover:shadow-slate-900/50`}
          >
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">{card.title}</p>
              <p className="text-3xl font-bold text-slate-100 mt-2">{loading ? '...' : card.value}</p>
            </div>
            <div className={`p-3 ${card.iconBg} rounded-xl transition-transform group-hover:scale-110`}>
              {card.icon}
            </div>
          </Link>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Quick Actions */}
        <div className="lg:col-span-1 space-y-4">
          <h3 className="text-lg font-semibold text-slate-200 flex items-center space-x-2">
            <FiActivity className="text-sky-400" />
            <span>Quick Actions</span>
          </h3>
          <div className="space-y-3">
            {quickActions.map((action, idx) => (
              <Link
                to={action.path}
                key={idx}
                className="group flex items-center justify-between p-4 bg-[#1e293b]/40 hover:bg-[#1e293b]/70 border border-slate-700/40 hover:border-sky-500/30 rounded-xl transition-all"
              >
                <div className="flex items-center space-x-3">
                  <div className="p-2 bg-sky-500/10 text-sky-400 rounded-lg text-sm">{action.icon}</div>
                  <div>
                    <p className="font-medium text-sm text-slate-200">{action.name}</p>
                    <p className="text-xs text-slate-500">{action.desc}</p>
                  </div>
                </div>
                <FiArrowRight className="text-slate-600 group-hover:text-sky-400 transition-colors" />
              </Link>
            ))}
          </div>
        </div>

        {/* Recent Activity */}
        <div className="lg:col-span-2 space-y-4">
          <h3 className="text-lg font-semibold text-slate-200 flex items-center space-x-2">
            <FiClock className="text-amber-400" />
            <span>Recent Activity</span>
          </h3>
          <div className="bg-[#1e293b]/40 border border-slate-700/40 rounded-2xl overflow-hidden">
            {recentLogs.length === 0 ? (
              <p className="text-slate-500 text-sm p-6 text-center">No recent activity to display.</p>
            ) : (
              <div className="divide-y divide-slate-700/30">
                {recentLogs.map((log, idx) => (
                  <div key={idx} className="flex items-center justify-between px-5 py-4 hover:bg-slate-800/20 transition-colors">
                    <div className="flex items-center space-x-3 min-w-0">
                      <div className="w-2 h-2 rounded-full bg-sky-400 flex-shrink-0"></div>
                      <div className="min-w-0">
                        <p className="text-sm text-slate-200 font-medium truncate">{log.action}</p>
                        <p className="text-xs text-slate-500">{log.module} • {log.username}</p>
                      </div>
                    </div>
                    <span className="text-xs text-slate-500 flex-shrink-0 ml-4">
                      {log.timestamp ? new Date(log.timestamp).toLocaleDateString() : ''}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

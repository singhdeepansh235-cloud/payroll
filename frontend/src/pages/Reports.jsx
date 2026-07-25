import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { FiDownload, FiFileText, FiUsers, FiCalendar, FiBriefcase, FiDollarSign } from 'react-icons/fi';

const Reports = () => {
  const [generating, setGenerating] = useState(null);

  const reports = [
    { name: 'Employee Report', path: 'employees', desc: 'Complete employee directory with contact and salary details.', icon: <FiUsers className="text-xl" />, color: 'sky' },
    { name: 'Attendance Report', path: 'attendance', desc: 'Daily and monthly attendance records across all departments.', icon: <FiCalendar className="text-xl" />, color: 'emerald' },
    { name: 'Leave Report', path: 'leaves', desc: 'Leave applications, approvals, rejections and balance summary.', icon: <FiFileText className="text-xl" />, color: 'amber' },
    { name: 'Payroll Report', path: 'payroll', desc: 'Salary breakdowns including gross, deductions, and net payroll.', icon: <FiDollarSign className="text-xl" />, color: 'indigo' },
    { name: 'Department Report', path: 'departments', desc: 'Department-wise employee distribution and statistics.', icon: <FiBriefcase className="text-xl" />, color: 'violet' },
  ];

  const colorMap = {
    sky: { bg: 'bg-sky-500/10', border: 'border-sky-500/20', text: 'text-sky-400', iconBg: 'bg-sky-500/20' },
    emerald: { bg: 'bg-emerald-500/10', border: 'border-emerald-500/20', text: 'text-emerald-400', iconBg: 'bg-emerald-500/20' },
    amber: { bg: 'bg-amber-500/10', border: 'border-amber-500/20', text: 'text-amber-400', iconBg: 'bg-amber-500/20' },
    indigo: { bg: 'bg-indigo-500/10', border: 'border-indigo-500/20', text: 'text-indigo-400', iconBg: 'bg-indigo-500/20' },
    violet: { bg: 'bg-violet-500/10', border: 'border-violet-500/20', text: 'text-violet-400', iconBg: 'bg-violet-500/20' },
  };

  const handleDownload = (path, format) => {
    const key = `${path}-${format}`;
    setGenerating(key);
    toast.success(`Generating ${format.toUpperCase()} report...`);
    window.open(`http://localhost:8080/api/reports/${path}/${format}`, '_blank');
    setTimeout(() => setGenerating(null), 2000);
  };

  const formats = [
    { key: 'pdf', label: 'PDF', color: 'bg-rose-500/10 hover:bg-rose-500/20 border-rose-500/20 text-rose-400' },
    { key: 'excel', label: 'Excel', color: 'bg-emerald-500/10 hover:bg-emerald-500/20 border-emerald-500/20 text-emerald-400' },
    { key: 'csv', label: 'CSV', color: 'bg-sky-500/10 hover:bg-sky-500/20 border-sky-500/20 text-sky-400' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-100">Reports Engine</h2>
        <p className="text-slate-400 text-sm mt-1">Export company records and operational statistics in multiple formats.</p>
      </div>

      {/* Stats banner */}
      <div className="grid grid-cols-3 gap-4">
        {formats.map((f) => (
          <div key={f.key} className={`p-4 ${f.color} border rounded-xl text-center`}>
            <p className="text-xs font-semibold uppercase tracking-wider opacity-70">{f.label} Format</p>
            <p className="text-lg font-bold mt-1">.{f.key}</p>
          </div>
        ))}
      </div>

      {/* Report Cards */}
      <div className="space-y-4">
        {reports.map((report) => {
          const c = colorMap[report.color];
          return (
            <div key={report.path} className={`p-6 ${c.bg} border ${c.border} rounded-2xl flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 transition-all hover:scale-[1.005]`}>
              <div className="flex items-center space-x-4">
                <div className={`p-3 ${c.iconBg} ${c.text} rounded-xl`}>
                  {report.icon}
                </div>
                <div>
                  <h4 className="font-semibold text-slate-200">{report.name}</h4>
                  <p className="text-xs text-slate-400 mt-0.5 max-w-sm">{report.desc}</p>
                </div>
              </div>

              <div className="flex items-center space-x-2 flex-shrink-0">
                {formats.map((f) => (
                  <button
                    key={f.key}
                    onClick={() => handleDownload(report.path, f.key)}
                    disabled={generating === `${report.path}-${f.key}`}
                    className={`flex items-center space-x-1.5 px-3.5 py-2 ${f.color} border rounded-xl text-xs font-semibold transition-all active:scale-95 disabled:opacity-50`}
                  >
                    <FiDownload className="text-xs" />
                    <span>{f.label}</span>
                  </button>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Reports;

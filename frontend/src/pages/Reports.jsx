import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { FiDownload, FiUsers, FiCalendar, FiFileText, FiDollarSign, FiBriefcase } from 'react-icons/fi';

const reports = [
  { name: 'Employee Report', path: 'employees', desc: 'Complete staff directory with contact and compensation details.', icon: FiUsers },
  { name: 'Attendance Report', path: 'attendance', desc: 'Daily attendance logs across all departments.', icon: FiCalendar },
  { name: 'Leave Report', path: 'leaves', desc: 'Leave applications, approval history, and balance summaries.', icon: FiFileText },
  { name: 'Payroll Report', path: 'payroll', desc: 'Salary breakdown with gross pay, deductions, and net amounts.', icon: FiDollarSign },
  { name: 'Department Report', path: 'departments', desc: 'Department-wise employee distribution and statistics.', icon: FiBriefcase },
];

const formats = [
  { key: 'pdf', label: 'PDF', cls: 'text-red-600 border-red-200 hover:bg-red-50' },
  { key: 'excel', label: 'Excel', cls: 'text-emerald-700 border-emerald-200 hover:bg-emerald-50' },
  { key: 'csv', label: 'CSV', cls: 'text-slate-600 border-slate-300 hover:bg-slate-50' },
];

const Reports = () => {
  const [downloading, setDownloading] = useState(null);

  const handleDownload = (path, format) => {
    setDownloading(`${path}-${format}`);
    toast.success(`Generating ${format.toUpperCase()} report...`);
    window.open(`http://localhost:8080/api/reports/${path}/${format}`, '_blank');
    setTimeout(() => setDownloading(null), 2000);
  };

  return (
    <div className="space-y-6 fade-in">
      <div>
        <h2 className="page-title">Reports Center</h2>
        <p className="page-subtitle">Export structured reports for all modules in PDF, Excel, or CSV format.</p>
      </div>

      <div className="space-y-4">
        {reports.map((report) => {
          const Icon = report.icon;
          return (
            <div key={report.path} className="card px-6 py-5 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 hover:shadow-md transition-shadow">
              <div className="flex items-center space-x-4">
                <div className="w-10 h-10 bg-slate-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <Icon className="text-slate-600 text-base" />
                </div>
                <div>
                  <h4 className="font-semibold text-slate-800 text-sm">{report.name}</h4>
                  <p className="text-xs text-slate-400 mt-0.5">{report.desc}</p>
                </div>
              </div>
              <div className="flex items-center space-x-2 flex-shrink-0">
                {formats.map(f => (
                  <button
                    key={f.key}
                    disabled={downloading === `${report.path}-${f.key}`}
                    onClick={() => handleDownload(report.path, f.key)}
                    className={`inline-flex items-center space-x-1.5 px-3 py-2 rounded-lg border bg-white text-xs font-semibold transition-colors disabled:opacity-50 ${f.cls}`}
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

import React from 'react';

export const FormInput = ({ label, required, error, icon: Icon, ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
        {label} {required && <span className="text-rose-400">*</span>}
      </label>
    )}
    <div className="relative">
      {Icon && (
        <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400 pointer-events-none">
          <Icon className="text-base" />
        </span>
      )}
      <input
        {...props}
        className={`w-full ${Icon ? 'pl-9' : 'px-3.5'} pr-3.5 py-2 bg-[#0f172a] border ${
          error ? 'border-rose-500/80 focus:ring-rose-500' : 'border-slate-700/80 focus:border-blue-500 focus:ring-blue-500'
        } rounded-lg text-slate-200 placeholder-slate-500 text-sm outline-none transition-all focus:ring-1`}
      />
    </div>
    {error && <p className="text-xs text-rose-400 font-medium">{error}</p>}
  </div>
);

export const FormSelect = ({ label, required, error, children, ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
        {label} {required && <span className="text-rose-400">*</span>}
      </label>
    )}
    <select
      {...props}
      className={`w-full px-3.5 py-2 bg-[#0f172a] border ${
        error ? 'border-rose-500/80 focus:ring-rose-500' : 'border-slate-700/80 focus:border-blue-500 focus:ring-blue-500'
      } rounded-lg text-slate-200 text-sm outline-none transition-all focus:ring-1 cursor-pointer`}
    >
      {children}
    </select>
    {error && <p className="text-xs text-rose-400 font-medium">{error}</p>}
  </div>
);

export const FormTextarea = ({ label, required, error, ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
        {label} {required && <span className="text-rose-400">*</span>}
      </label>
    )}
    <textarea
      {...props}
      className={`w-full px-3.5 py-2 bg-[#0f172a] border ${
        error ? 'border-rose-500/80 focus:ring-rose-500' : 'border-slate-700/80 focus:border-blue-500 focus:ring-blue-500'
      } rounded-lg text-slate-200 placeholder-slate-500 text-sm outline-none transition-all focus:ring-1 resize-none`}
    />
    {error && <p className="text-xs text-rose-400 font-medium">{error}</p>}
  </div>
);

export const SearchInput = ({ value, onChange, placeholder = 'Search records...' }) => (
  <div className="relative w-full max-w-md">
    <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-400 pointer-events-none">
      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
    </span>
    <input
      type="text"
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      className="w-full pl-10 pr-4 py-2 bg-[#1e293b] border border-slate-700/80 rounded-lg text-slate-200 placeholder-slate-400 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all"
    />
  </div>
);

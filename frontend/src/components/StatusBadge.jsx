import React from 'react';

const StatusBadge = ({ status }) => {
  if (!status) return null;

  const normalized = String(status).toUpperCase();

  const styles = {
    // General / Employee status
    ACTIVE: 'bg-emerald-950/40 text-emerald-400 border-emerald-800/60',
    INACTIVE: 'bg-rose-950/40 text-rose-400 border-rose-800/60',
    
    // Attendance status
    PRESENT: 'bg-emerald-950/40 text-emerald-400 border-emerald-800/60',
    ABSENT: 'bg-rose-950/40 text-rose-400 border-rose-800/60',
    HALF_DAY: 'bg-amber-950/40 text-amber-400 border-amber-800/60',
    ON_LEAVE: 'bg-blue-950/40 text-blue-400 border-blue-800/60',
    HOLIDAY: 'bg-indigo-950/40 text-indigo-400 border-indigo-800/60',

    // Leave status
    APPROVED: 'bg-emerald-950/40 text-emerald-400 border-emerald-800/60',
    PENDING: 'bg-amber-950/40 text-amber-400 border-amber-800/60',
    REJECTED: 'bg-rose-950/40 text-rose-400 border-rose-800/60',

    // Document types
    CONTRACT: 'bg-blue-950/40 text-blue-400 border-blue-800/60',
    ID_PROOF: 'bg-indigo-950/40 text-indigo-400 border-indigo-800/60',
    TAX_FORM: 'bg-amber-950/40 text-amber-400 border-amber-800/60',
    CERTIFICATE: 'bg-emerald-950/40 text-emerald-400 border-emerald-800/60',
    OTHER: 'bg-slate-800 text-slate-300 border-slate-700',
  };

  const badgeClass = styles[normalized] || 'bg-slate-800 text-slate-300 border-slate-700';

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold tracking-wide border ${badgeClass}`}>
      <span className="w-1.5 h-1.5 rounded-full bg-current mr-1.5 opacity-80" />
      {normalized.replace('_', ' ')}
    </span>
  );
};

export default StatusBadge;

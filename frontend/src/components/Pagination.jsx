import React from 'react';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';

/**
 * Shared pagination component.
 */
const Pagination = ({ page, totalPages, totalElements, onPageChange }) => {
  if (totalPages <= 1) return null;

  const start = Math.max(0, Math.min(page - 2, totalPages - 5));
  const pages = Array.from({ length: Math.min(totalPages, 5) }, (_, i) => start + i);

  return (
    <div className="px-5 py-3.5 border-t border-slate-100 bg-slate-50 flex items-center justify-between rounded-b-xl">
      <span className="text-xs text-slate-500">
        Page <strong className="text-slate-700">{page + 1}</strong> of {totalPages}
        {totalElements != null && <> · {totalElements} total</>}
      </span>
      <div className="flex items-center space-x-1">
        <button
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
          className="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-40 transition-colors"
        >
          <FiChevronLeft className="text-sm" />
        </button>
        {pages.map(p => (
          <button
            key={p}
            onClick={() => onPageChange(p)}
            className={`w-8 h-8 rounded-lg border text-xs font-semibold transition-colors ${
              p === page
                ? 'bg-[#2d4a8a] border-[#2d4a8a] text-white'
                : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
            }`}
          >
            {p + 1}
          </button>
        ))}
        <button
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
          className="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-500 hover:bg-slate-50 disabled:opacity-40 transition-colors"
        >
          <FiChevronRight className="text-sm" />
        </button>
      </div>
    </div>
  );
};

export default Pagination;

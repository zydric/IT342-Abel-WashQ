import { Link, useLocation } from 'react-router-dom';

export default function Sidebar({ user, onLogout }) {
  const location = useLocation();
  const isAdmin = user.role === 'ADMIN';

  const isActive = (path) => location.pathname === path;

  const navLinkClass = (path) => `flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors font-medium ${
    isActive(path) 
      ? 'bg-primary text-white' 
      : 'text-neutral-300 hover:text-white hover:bg-white/5'
  }`;

  return (
    <aside className="w-[240px] bg-[#0F172A] hidden md:flex flex-col shrink-0 h-screen shadow-xl sticky top-0 overflow-hidden">
      <div className="p-6 pb-2">
        <h1 className="text-white text-2xl font-bold tracking-tight">WashQ</h1>
      </div>
      
      <div className="px-6 py-4 text-[10px] font-bold text-neutral-500 tracking-widest uppercase">
        Main
      </div>

      <nav className="flex-1 px-4 space-y-1">
        <Link to={isAdmin ? "/admin/dashboard" : "/staff/dashboard"} className={navLinkClass(isAdmin ? "/admin/dashboard" : "/staff/dashboard")}>
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
          </svg>
          Dashboard
        </Link>
        {/* 
        <button 
          disabled 
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-neutral-300 hover:text-white hover:bg-white/5 font-medium transition-colors cursor-not-allowed"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          Bookings
        </button> 
        */}
        {isAdmin && (
          <Link to="/admin/services" className={navLinkClass("/admin/services")}>
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            </svg>
            Services
          </Link>
        )}
      </nav>

      <div className="p-4 bg-white/5 border-t border-white/10 mt-auto">
        <div className="flex items-center gap-3 mb-4 px-2">
          <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white text-xs font-bold shrink-0">
            {user.firstName?.[0]}{user.lastName?.[0]}
          </div>
          <div className="overflow-hidden">
            <p className="text-sm font-medium text-white truncate">{user.firstName} {user.lastName}</p>
            <span className="inline-block px-1.5 py-0.5 rounded text-[9px] font-bold bg-white/20 text-white mt-0.5 uppercase tracking-wide">
              {user.role}
            </span>
          </div>
        </div>
        <button 
          onClick={onLogout}
          className="w-full flex items-center gap-2 px-3 py-2 text-sm text-neutral-400 hover:text-white hover:bg-white/10 rounded transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          Sign Out
        </button>
      </div>
    </aside>
  );
}

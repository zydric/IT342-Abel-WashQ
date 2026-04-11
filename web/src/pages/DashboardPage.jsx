import { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';

// ─── Shared Navbar ────────────────────────────────────────────────────────────
function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const initials = [user.firstName?.[0], user.lastName?.[0]]
    .filter(Boolean)
    .join('')
    .toUpperCase() || 'U';

  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const navLinks = [
    { label: 'Dashboard', to: '/dashboard' },
    { label: 'Services', to: '/services' },
    { label: 'My Bookings', to: '/bookings' },
  ];

  return (
    <nav className="h-16 bg-primary-dark sticky top-0 z-navbar shadow-elevated">
      <div className="max-w-content mx-auto h-full flex items-center justify-between px-6">
        {/* Logo */}
        <Link
          to="/dashboard"
          className="text-white font-bold text-[20px] tracking-tight select-none hover:opacity-90 transition-opacity"
        >
          WashQ
        </Link>

        {/* Nav links */}
        <div className="hidden md:flex items-center gap-8">
          {navLinks.map(({ label, to }) => {
            const active = location.pathname === to;
            return (
              <Link
                key={to}
                to={to}
                className={`text-body font-semibold transition-colors duration-150 relative group ${
                  active ? 'text-white' : 'text-white/70 hover:text-white'
                }`}
              >
                {label}
                <span
                  className={`absolute -bottom-0.5 left-0 h-0.5 bg-white transition-all duration-200 ${
                    active ? 'w-full' : 'w-0 group-hover:w-full bg-white/60'
                  }`}
                />
              </Link>
            );
          })}
        </div>

        {/* Avatar dropdown */}
        <div className="relative">
          <button
            id="avatar-menu-btn"
            onClick={() => setDropdownOpen((o) => !o)}
            className="w-9 h-9 rounded-full bg-primary flex items-center justify-center text-white text-caption font-bold hover:ring-2 hover:ring-white/40 transition-all"
            aria-expanded={dropdownOpen}
            aria-haspopup="true"
          >
            {initials}
          </button>
          {dropdownOpen && (
            <>
              <div className="fixed inset-0 z-0" onClick={() => setDropdownOpen(false)} />
              <div className="absolute right-0 top-11 w-48 bg-white rounded-card shadow-elevated z-10 py-1 overflow-hidden">
                <Link
                  to="/profile"
                  onClick={() => setDropdownOpen(false)}
                  className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors"
                >
                  My Profile
                </Link>
                <Link
                  to="/bookings"
                  onClick={() => setDropdownOpen(false)}
                  className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors"
                >
                  My Bookings
                </Link>
                <hr className="border-neutral-100 my-1" />
                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2.5 text-body text-error hover:bg-neutral-50 transition-colors font-medium"
                >
                  Sign Out
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

// ─── Quick-stat card ──────────────────────────────────────────────────────────
function StatCard({ icon, label, value, sub }) {
  return (
    <div className="bg-white rounded-card shadow-card p-6 flex items-start gap-4">
      <div className="w-12 h-12 rounded-2xl bg-primary-light flex items-center justify-center shrink-0">
        {icon}
      </div>
      <div>
        <p className="text-caption text-neutral-400 mb-0.5">{label}</p>
        <p className="text-h3 text-neutral-900">{value}</p>
        {sub && <p className="text-caption text-neutral-400 mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function DashboardPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const now = new Date();
  const greeting = (() => {
    const h = now.getHours();
    if (h < 12) return 'Good morning';
    if (h < 18) return 'Good afternoon';
    return 'Good evening';
  })();

  const dateStr = now.toLocaleDateString('en-PH', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />

      <main className="max-w-content mx-auto px-6 py-10 space-y-8">

        {/* ── Welcome Banner ─────────────────────────────────────────────── */}
        <section className="bg-primary-light rounded-card p-8 flex items-center justify-between gap-4 flex-wrap">
          <div>
            <h1 className="text-h2 text-neutral-900">
              {greeting}{user.firstName ? `, ${user.firstName}` : ''}!
            </h1>
            <p className="text-body text-neutral-400 mt-1">{dateStr}</p>
          </div>
          <Link
            to="/services"
            className="text-body font-semibold text-primary hover:text-primary-dark transition-colors whitespace-nowrap"
          >
            Book a new slot →
          </Link>
        </section>

        {/* ── Info Row: Quick Book CTA ───────────────────────────────────── */}
        <section className="grid gap-6 md:grid-cols-2">
          {/* Quick Book CTA */}
          <div
            className="rounded-card p-8 flex flex-col justify-between gap-6 shadow-elevated"
            style={{ background: 'linear-gradient(135deg, #1D4ED8 0%, #1E3A8A 100%)' }}
          >
            <div>
              <h2 className="text-h2 text-white leading-snug">
                Ready to book your laundry?
              </h2>
              <p className="text-body text-white/80 mt-2">
                Choose a service and reserve your slot in under 2 minutes.
              </p>
            </div>
            <div className="flex justify-end">
              <button
                id="dashboard-book-now-btn"
                onClick={() => navigate('/services')}
                className="transition-btn bg-white text-primary font-semibold text-body uppercase tracking-wide px-6 py-2.5 rounded-btn hover:bg-primary-light active:scale-[0.97]"
              >
                Book Now
              </button>
            </div>
          </div>

          {/* Quick stats placeholder */}
          <div className="grid grid-cols-1 gap-4">
            <StatCard
              icon={
                <svg className="w-6 h-6 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              }
              label="Active Bookings"
              value="—"
              sub="Coming soon"
            />
            <StatCard
              icon={
                <svg className="w-6 h-6 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              }
              label="Last Booking"
              value="—"
              sub="No bookings yet"
            />
          </div>
        </section>

        {/* ── Services shortcut ──────────────────────────────────────────── */}
        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-h2 text-neutral-900">Our Services</h2>
            <Link
              to="/services"
              className="text-body font-semibold text-primary hover:text-primary-dark transition-colors"
            >
              View all →
            </Link>
          </div>
          <div className="bg-white rounded-card shadow-card p-8 flex flex-col items-center justify-center text-center gap-3">
            <svg className="w-12 h-12 text-neutral-400" fill="none" viewBox="0 0 48 48" stroke="currentColor" strokeWidth="1.5">
              <circle cx="24" cy="24" r="20" />
              <path strokeLinecap="round" d="M16 24c0-4.4 3.6-8 8-8s8 3.6 8 8" />
              <circle cx="24" cy="30" r="3" />
            </svg>
            <p className="text-body text-neutral-400">
              Browse our laundry services and book a slot.
            </p>
            <button
              onClick={() => navigate('/services')}
              className="transition-btn mt-1 bg-primary hover:bg-primary-dark text-white text-body font-semibold uppercase tracking-wide px-6 py-2.5 rounded-btn active:scale-[0.97]"
            >
              View Services
            </button>
          </div>
        </section>

      </main>
    </div>
  );
}

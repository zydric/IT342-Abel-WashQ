import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getServices } from '../api/serviceApi';

// ─── Service icon map ────────────────────────────────────────────────────────
// Maps service names (case-insensitive, partial match) → SVG icon paths
const SERVICE_ICONS = {
  'wash only': (
    <svg viewBox="0 0 40 40" fill="none" className="w-10 h-10 text-primary">
      <circle cx="20" cy="20" r="17" stroke="currentColor" strokeWidth="2.2" />
      <path d="M12 20c0-4.4 3.6-8 8-8s8 3.6 8 8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M20 28a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" strokeWidth="2" />
      <circle cx="20" cy="20" r="1.5" fill="currentColor" />
    </svg>
  ),
  'wash & dry': (
    <svg viewBox="0 0 40 40" fill="none" className="w-10 h-10 text-primary">
      <rect x="6" y="8" width="28" height="24" rx="4" stroke="currentColor" strokeWidth="2.2" />
      <circle cx="20" cy="20" r="7" stroke="currentColor" strokeWidth="2" />
      <circle cx="20" cy="20" r="3" stroke="currentColor" strokeWidth="1.5" />
      <path d="M10 12h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  ),
  'wash, dry & fold': (
    <svg viewBox="0 0 40 40" fill="none" className="w-10 h-10 text-primary">
      <rect x="6" y="8" width="28" height="24" rx="4" stroke="currentColor" strokeWidth="2.2" />
      <circle cx="20" cy="20" r="7" stroke="currentColor" strokeWidth="2" />
      <circle cx="20" cy="20" r="3" stroke="currentColor" strokeWidth="1.5" />
      <path d="M10 12h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M14 32l12-4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  ),
  'dry clean': (
    <svg viewBox="0 0 40 40" fill="none" className="w-10 h-10 text-primary">
      <path d="M20 6 L34 14 L34 26 C34 30 20 36 20 36 C20 36 6 30 6 26 L6 14 Z" stroke="currentColor" strokeWidth="2.2" strokeLinejoin="round" />
      <path d="M14 20l4 4 8-8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  ),
  'iron only': (
    <svg viewBox="0 0 40 40" fill="none" className="w-10 h-10 text-primary">
      <path d="M8 22 Q8 18 16 18 H32 L34 28 H8 Z" stroke="currentColor" strokeWidth="2.2" strokeLinejoin="round" />
      <path d="M14 18 V14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <circle cx="14" cy="12" r="2" fill="currentColor" />
      <path d="M15 25h2M20 25h2M25 25h2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  ),
  default: (
    <svg viewBox="0 0 40 40" fill="none" className="w-10 h-10 text-primary">
      <circle cx="20" cy="20" r="17" stroke="currentColor" strokeWidth="2.2" />
      <path d="M13 20.5c1-4 3.5-6 7-6s6 2 7 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <circle cx="20" cy="27" r="3" stroke="currentColor" strokeWidth="2" />
    </svg>
  ),
};

function getIcon(name = '') {
  const lower = name.toLowerCase();
  const match = Object.keys(SERVICE_ICONS).find(
    (key) => key !== 'default' && lower.includes(key)
  );
  return SERVICE_ICONS[match] || SERVICE_ICONS['default'];
}

// ─── Skeleton card ────────────────────────────────────────────────────────────
function SkeletonCard() {
  return (
    <div className="bg-white rounded-card shadow-card p-6 flex flex-col gap-4 animate-pulse">
      <div className="w-10 h-10 rounded-full bg-neutral-100" />
      <div className="h-5 w-2/3 rounded bg-neutral-100" />
      <div className="space-y-2">
        <div className="h-3 w-full rounded bg-neutral-100" />
        <div className="h-3 w-5/6 rounded bg-neutral-100" />
      </div>
      <div className="h-6 w-1/3 rounded bg-neutral-100 mt-auto" />
      <div className="h-10 w-full rounded-btn bg-neutral-100" />
    </div>
  );
}

// ─── Toast ────────────────────────────────────────────────────────────────────
function Toast({ message, onClose }) {
  useEffect(() => {
    const t = setTimeout(onClose, 4000);
    return () => clearTimeout(t);
  }, [onClose]);

  return (
    <div
      role="alert"
      className="fixed bottom-6 right-6 z-toast flex items-center gap-3 bg-error text-white text-body font-medium px-5 py-3 rounded-btn shadow-elevated"
      style={{ animation: 'slideUp 0.25s ease' }}
    >
      <svg className="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
        <circle cx="12" cy="12" r="10" />
        <path d="M12 8v4M12 16h.01" strokeLinecap="round" />
      </svg>
      {message}
      <button
        onClick={onClose}
        aria-label="Dismiss"
        className="ml-2 opacity-80 hover:opacity-100 transition-opacity"
      >
        ✕
      </button>
    </div>
  );
}

// ─── Empty State ──────────────────────────────────────────────────────────────
function EmptyState() {
  return (
    <div className="col-span-full flex flex-col items-center justify-center py-24 gap-4 text-center">
      <svg className="w-16 h-16 text-neutral-400" fill="none" viewBox="0 0 64 64" stroke="currentColor" strokeWidth="1.5">
        <circle cx="32" cy="32" r="28" />
        <path d="M22 32c0-5.5 4.5-10 10-10s10 4.5 10 10" strokeLinecap="round" />
        <path d="M26 44h12" strokeLinecap="round" />
      </svg>
      <h3 className="text-h3 text-neutral-700">No services available</h3>
      <p className="text-body text-neutral-400 max-w-xs">
        There are no active services at the moment. Please check back later.
      </p>
    </div>
  );
}

// ─── Service Card ─────────────────────────────────────────────────────────────
function ServiceCard({ service, onBook }) {
  return (
    <article className="bg-white rounded-card shadow-card p-6 flex flex-col gap-4 hover:shadow-elevated transition-shadow duration-200 group">
      {/* Icon */}
      <div className="w-14 h-14 rounded-2xl bg-primary-light flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform duration-200">
        {getIcon(service.name)}
      </div>

      {/* Name */}
      <h3 className="text-h3 text-neutral-900 leading-snug">{service.name}</h3>

      {/* Description */}
      <p className="text-body text-neutral-400 line-clamp-2 flex-1">
        {service.description || 'Professional laundry care at your convenience.'}
      </p>

      {/* Price + Duration */}
      <div className="flex items-end justify-between mt-auto">
        <span className="text-[18px] font-semibold text-primary leading-none">
          ₱{Number(service.pricePerKg ?? service.price_per_kg).toFixed(2)}&nbsp;
          <span className="text-caption text-neutral-400 font-normal">/ kg</span>
        </span>
        <span className="text-caption text-neutral-400">
          ~{service.estimatedDurationHours ?? service.estimated_duration_hours} hrs
        </span>
      </div>

      {/* CTA */}
      <button
        id={`book-service-${service.id}`}
        onClick={() => onBook(service.id)}
        className="transition-btn w-full bg-primary hover:bg-primary-dark active:scale-[0.97] text-white text-body font-semibold uppercase tracking-wide py-2.5 px-5 rounded-btn mt-1"
      >
        Book Now
      </button>
    </article>
  );
}

// ─── Navbar ───────────────────────────────────────────────────────────────────
function Navbar() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const initials = [user.firstName?.[0], user.lastName?.[0]].filter(Boolean).join('').toUpperCase() || 'U';

  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <nav className="h-16 bg-primary-dark sticky top-0 z-navbar shadow-elevated">
      <div className="max-w-content mx-auto h-full flex items-center justify-between px-6">
        {/* Logo */}
        <Link to="/dashboard" className="text-white font-bold text-[20px] tracking-tight select-none hover:opacity-90 transition-opacity">
          WashQ
        </Link>

        {/* Nav links */}
        <div className="hidden md:flex items-center gap-8">
          {[
            { label: 'Dashboard', to: '/dashboard' },
            { label: 'Services', to: '/services' },
            { label: 'My Bookings', to: '/bookings' },
          ].map(({ label, to }) => (
            <Link
              key={to}
              to={to}
              className="text-white/70 hover:text-white text-body font-semibold transition-colors duration-150 relative group"
            >
              {label}
              <span className="absolute -bottom-0.5 left-0 w-0 group-hover:w-full h-0.5 bg-white/60 transition-all duration-200" />
            </Link>
          ))}
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
                <Link to="/profile" onClick={() => setDropdownOpen(false)}
                  className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors">
                  My Profile
                </Link>
                <Link to="/bookings" onClick={() => setDropdownOpen(false)}
                  className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors">
                  My Bookings
                </Link>
                <hr className="border-neutral-100 my-1" />
                <button onClick={handleLogout}
                  className="w-full text-left px-4 py-2.5 text-body text-error hover:bg-neutral-50 transition-colors font-medium">
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

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function ServiceCatalogPage() {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);

    getServices()
      .then((res) => {
        if (cancelled) return;
        const raw = res.data;
        // Support both { success, data: [...] } and direct array
        const list = Array.isArray(raw) ? raw : (raw?.data ?? []);
        setServices(list.filter((s) => s.isActive ?? s.is_active ?? true));
      })
      .catch((err) => {
        if (cancelled) return;
        const msg =
          err?.response?.data?.error?.message ||
          err?.message ||
          'Failed to load services. Please try again.';
        setToast(msg);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => { cancelled = true; };
  }, []);

  const handleBook = (serviceId) => {
    navigate(`/book/step1?serviceId=${serviceId}`);
  };

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />

      <main className="max-w-content mx-auto px-6 py-10">
        {/* Page header */}
        <header className="mb-8">
          <h1 className="text-h1 text-neutral-900">Our Services</h1>
          <p className="text-body text-neutral-400 mt-1">
            Choose a laundry service that fits your needs and book a slot in minutes.
          </p>
        </header>

        {/* Service grid */}
        <section
          id="service-catalog-grid"
          className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3"
          aria-label="Available laundry services"
        >
          {loading ? (
            // Skeleton placeholders
            Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)
          ) : services.length === 0 ? (
            <EmptyState />
          ) : (
            services.map((service) => (
              <ServiceCard
                key={service.id}
                service={service}
                onBook={handleBook}
              />
            ))
          )}
        </section>
      </main>

      {/* Error toast */}
      {toast && (
        <Toast message={toast} onClose={() => setToast(null)} />
      )}

      {/* Toast slide-up animation */}
      <style>{`
        @keyframes slideUp {
          from { opacity: 0; transform: translateY(16px); }
          to   { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
}

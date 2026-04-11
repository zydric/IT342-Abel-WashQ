import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams, Link, useLocation } from 'react-router-dom';
import { getServices } from '../api/serviceApi';

// ─── Progress Stepper ─────────────────────────────────────────────────────────
const STEPS = [
  { number: 1, label: 'Select Service' },
  { number: 2, label: 'Select Time Slot' },
  { number: 3, label: 'Confirm & Pay' },
];

function ProgressStepper({ current }) {
  return (
    <div className="flex items-center justify-center w-full mb-10">
      {STEPS.map((step, idx) => {
        const isActive = step.number === current;
        const isCompleted = step.number < current;
        const isLast = idx === STEPS.length - 1;

        return (
          <div key={step.number} className="flex items-center">
            {/* Circle + label */}
            <div className="flex flex-col items-center gap-1.5">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-caption font-bold transition-colors ${
                  isActive
                    ? 'bg-primary text-white'
                    : isCompleted
                    ? 'bg-primary text-white'
                    : 'bg-white border-2 border-neutral-400 text-neutral-400'
                }`}
              >
                {isCompleted ? (
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                ) : (
                  step.number
                )}
              </div>
              <span
                className={`text-caption whitespace-nowrap ${
                  isActive ? 'text-primary font-semibold' : 'text-neutral-400'
                }`}
              >
                {step.label}
              </span>
            </div>

            {/* Connector line */}
            {!isLast && (
              <div
                className={`h-0.5 w-16 sm:w-24 mx-2 mb-5 transition-colors ${
                  isCompleted ? 'bg-primary' : 'bg-neutral-200'
                }`}
              />
            )}
          </div>
        );
      })}
    </div>
  );
}

// ─── Navbar ───────────────────────────────────────────────────────────────────
function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const initials = [user.firstName?.[0], user.lastName?.[0]]
    .filter(Boolean).join('').toUpperCase() || 'U';
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
        <Link to="/dashboard" className="text-white font-bold text-[20px] tracking-tight hover:opacity-90 transition-opacity select-none">
          WashQ
        </Link>
        <div className="hidden md:flex items-center gap-8">
          {navLinks.map(({ label, to }) => {
            const active = location.pathname === to;
            return (
              <Link key={to} to={to}
                className={`text-body font-semibold transition-colors duration-150 relative group ${active ? 'text-white' : 'text-white/70 hover:text-white'}`}>
                {label}
                <span className={`absolute -bottom-0.5 left-0 h-0.5 bg-white transition-all duration-200 ${active ? 'w-full' : 'w-0 group-hover:w-full bg-white/60'}`} />
              </Link>
            );
          })}
        </div>
        <div className="relative">
          <button id="avatar-menu-btn" onClick={() => setDropdownOpen(o => !o)}
            className="w-9 h-9 rounded-full bg-primary flex items-center justify-center text-white text-caption font-bold hover:ring-2 hover:ring-white/40 transition-all"
            aria-expanded={dropdownOpen} aria-haspopup="true">
            {initials}
          </button>
          {dropdownOpen && (
            <>
              <div className="fixed inset-0 z-0" onClick={() => setDropdownOpen(false)} />
              <div className="absolute right-0 top-11 w-48 bg-white rounded-card shadow-elevated z-10 py-1 overflow-hidden">
                <Link to="/profile" onClick={() => setDropdownOpen(false)} className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors">My Profile</Link>
                <Link to="/bookings" onClick={() => setDropdownOpen(false)} className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors">My Bookings</Link>
                <hr className="border-neutral-100 my-1" />
                <button onClick={handleLogout} className="w-full text-left px-4 py-2.5 text-body text-error hover:bg-neutral-50 transition-colors font-medium">Sign Out</button>
              </div>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

// ─── Service icons ────────────────────────────────────────────────────────────
const SERVICE_ICONS = {
  'wash only': (
    <svg viewBox="0 0 40 40" fill="none" className="w-9 h-9 text-primary">
      <circle cx="20" cy="20" r="17" stroke="currentColor" strokeWidth="2.2" />
      <path d="M12 20c0-4.4 3.6-8 8-8s8 3.6 8 8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M20 28a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" strokeWidth="2" />
      <circle cx="20" cy="20" r="1.5" fill="currentColor" />
    </svg>
  ),
  'wash & dry': (
    <svg viewBox="0 0 40 40" fill="none" className="w-9 h-9 text-primary">
      <rect x="6" y="8" width="28" height="24" rx="4" stroke="currentColor" strokeWidth="2.2" />
      <circle cx="20" cy="20" r="7" stroke="currentColor" strokeWidth="2" />
      <circle cx="20" cy="20" r="3" stroke="currentColor" strokeWidth="1.5" />
      <path d="M10 12h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  ),
  'wash, dry & fold': (
    <svg viewBox="0 0 40 40" fill="none" className="w-9 h-9 text-primary">
      <rect x="6" y="8" width="28" height="24" rx="4" stroke="currentColor" strokeWidth="2.2" />
      <circle cx="20" cy="20" r="7" stroke="currentColor" strokeWidth="2" />
      <circle cx="20" cy="20" r="3" stroke="currentColor" strokeWidth="1.5" />
      <path d="M10 12h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M14 32l12-4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  ),
  'dry clean': (
    <svg viewBox="0 0 40 40" fill="none" className="w-9 h-9 text-primary">
      <path d="M20 6 L34 14 L34 26 C34 30 20 36 20 36 C20 36 6 30 6 26 L6 14 Z" stroke="currentColor" strokeWidth="2.2" strokeLinejoin="round" />
      <path d="M14 20l4 4 8-8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  ),
  'iron only': (
    <svg viewBox="0 0 40 40" fill="none" className="w-9 h-9 text-primary">
      <path d="M8 22 Q8 18 16 18 H32 L34 28 H8 Z" stroke="currentColor" strokeWidth="2.2" strokeLinejoin="round" />
      <path d="M14 18 V14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <circle cx="14" cy="12" r="2" fill="currentColor" />
      <path d="M15 25h2M20 25h2M25 25h2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  ),
  default: (
    <svg viewBox="0 0 40 40" fill="none" className="w-9 h-9 text-primary">
      <circle cx="20" cy="20" r="17" stroke="currentColor" strokeWidth="2.2" />
      <path d="M13 20.5c1-4 3.5-6 7-6s6 2 7 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <circle cx="20" cy="27" r="3" stroke="currentColor" strokeWidth="2" />
    </svg>
  ),
};

function getIcon(name = '') {
  const lower = name.toLowerCase();
  const match = Object.keys(SERVICE_ICONS).find(
    key => key !== 'default' && lower.includes(key)
  );
  return SERVICE_ICONS[match] || SERVICE_ICONS['default'];
}

// ─── Skeleton card ────────────────────────────────────────────────────────────
function SkeletonCard() {
  return (
    <div className="bg-white rounded-card border border-neutral-200 p-5 flex flex-col gap-3 animate-pulse">
      <div className="w-10 h-10 rounded-full bg-neutral-100" />
      <div className="h-4 w-2/3 rounded bg-neutral-100" />
      <div className="space-y-2">
        <div className="h-3 w-full rounded bg-neutral-100" />
        <div className="h-3 w-5/6 rounded bg-neutral-100" />
      </div>
      <div className="h-5 w-1/3 rounded bg-neutral-100 mt-auto" />
    </div>
  );
}

// ─── Selectable Service Card ──────────────────────────────────────────────────
function ServiceCard({ service, selected, onSelect }) {
  return (
    <button
      type="button"
      id={`select-service-${service.id}`}
      onClick={() => onSelect(service.id)}
      className={`relative text-left w-full rounded-card p-5 flex flex-col gap-3 transition-all duration-150 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary ${
        selected
          ? 'border-[2.5px] border-primary bg-[#F0F7FF] shadow-card'
          : 'border-[1.5px] border-[#CBD5E1] bg-white hover:border-primary/40 hover:shadow-card'
      }`}
      aria-pressed={selected}
    >
      {/* Checkmark — shown when selected */}
      {selected && (
        <span className="absolute top-3 right-3 w-5 h-5 rounded-full bg-primary flex items-center justify-center">
          <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3">
            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
          </svg>
        </span>
      )}

      {/* Icon */}
      <div className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 ${selected ? 'bg-primary-light' : 'bg-neutral-100'}`}>
        {getIcon(service.name)}
      </div>

      {/* Name */}
      <h3 className="text-h3 text-neutral-900 leading-snug pr-6">{service.name}</h3>

      {/* Description */}
      <p className="text-body text-neutral-400 line-clamp-2 flex-1">
        {service.description || 'Professional laundry care at your convenience.'}
      </p>

      {/* Price + Duration */}
      <div className="flex items-end justify-between mt-auto pt-1">
        <span className="text-[17px] font-semibold text-primary leading-none">
          ₱{Number(service.pricePerKg ?? service.price_per_kg).toFixed(2)}
          <span className="text-caption text-neutral-400 font-normal"> / kg</span>
        </span>
        <span className="text-caption text-neutral-400">
          ~{service.estimatedDurationHours ?? service.estimated_duration_hours} hrs
        </span>
      </div>
    </button>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function BookingStep1Page() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedId = searchParams.get('serviceId')
    ? Number(searchParams.get('serviceId'))
    : null;

  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedId, setSelectedId] = useState(preselectedId);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getServices()
      .then(res => {
        if (cancelled) return;
        const raw = res.data;
        const list = Array.isArray(raw) ? raw : (raw?.data ?? []);
        setServices(list.filter(s => s.isActive ?? s.is_active ?? true));
      })
      .catch(err => {
        if (cancelled) return;
        setError(err?.response?.data?.error?.message || err?.message || 'Failed to load services.');
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const handleContinue = () => {
    if (!selectedId) return;
    navigate(`/book/step2?serviceId=${selectedId}`);
  };

  const handleBack = () => {
    navigate('/services');
  };

  return (
    <div className="min-h-screen bg-neutral-50 flex flex-col">
      <Navbar />

      {/* ── Main content ─────────────────────────────────────── */}
      <main className="flex-1 w-full max-w-[860px] mx-auto px-6 py-10 pb-32">

        {/* Progress stepper */}
        <ProgressStepper current={1} />

        {/* Page heading */}
        <header className="mb-8">
          <h1 className="text-h1 text-neutral-900">Choose a Service</h1>
          <p className="text-body text-neutral-400 mt-1">
            Select the laundry service that fits your needs.
          </p>
        </header>

        {/* Error state */}
        {error && !loading && (
          <div className="flex flex-col items-center justify-center py-20 gap-3 text-center">
            <svg className="w-12 h-12 text-neutral-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p className="text-body text-neutral-400">{error}</p>
          </div>
        )}

        {/* Service grid */}
        {!error && (
          <section
            id="booking-step1-grid"
            className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3"
            aria-label="Select a laundry service"
          >
            {loading ? (
              Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)
            ) : services.length === 0 ? (
              <div className="col-span-full text-center py-20">
                <p className="text-body text-neutral-400">
                  No services are currently available. Please check back later.
                </p>
              </div>
            ) : (
              services.map(service => (
                <ServiceCard
                  key={service.id}
                  service={service}
                  selected={selectedId === service.id}
                  onSelect={setSelectedId}
                />
              ))
            )}
          </section>
        )}
      </main>

      {/* ── Sticky footer bar ────────────────────────────────── */}
      {!loading && !error && (
        <footer className="fixed bottom-0 left-0 right-0 z-navbar bg-white border-t border-neutral-100 shadow-elevated">
          <div className="max-w-[860px] mx-auto px-6 py-4 flex items-center justify-between">
            <button
              id="booking-step1-back"
              onClick={handleBack}
              className="transition-btn border-[1.5px] border-primary text-primary text-body font-semibold uppercase tracking-wide px-6 py-2.5 rounded-btn hover:bg-primary-light active:scale-[0.97]"
            >
              Back
            </button>
            <button
              id="booking-step1-continue"
              onClick={handleContinue}
              disabled={!selectedId}
              className={`transition-btn bg-primary text-white text-body font-semibold uppercase tracking-wide px-8 py-2.5 rounded-btn active:scale-[0.97] ${
                selectedId
                  ? 'hover:bg-primary-dark'
                  : 'opacity-40 cursor-not-allowed'
              }`}
            >
              Continue
            </button>
          </div>
        </footer>
      )}
    </div>
  );
}

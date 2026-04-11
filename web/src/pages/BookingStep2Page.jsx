import { useEffect, useState, useMemo } from 'react';
import { useNavigate, useSearchParams, Link, useLocation } from 'react-router-dom';
import { getSlots } from '../api/slotApi';
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

// ─── Format Time Helper ───────────────────────────────────────────────────────
function formatTime(timeStr) {
  if (!timeStr) return '';
  const [hours, minutes] = timeStr.split(':');
  const d = new Date();
  d.setHours(hours, minutes);
  return d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function BookingStep2Page() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const location = useLocation();
  const serviceId = searchParams.get('serviceId')
    ? Number(searchParams.get('serviceId'))
    : location.state?.serviceId
    ? Number(location.state.serviceId)
    : null;

  const [service, setService] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    const offset = today.getTimezoneOffset() * 60000;
    return new Date(today.getTime() - offset).toISOString().split('T')[0];
  });
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [weight, setWeight] = useState(0);

  // Generate next 7 days
  const upcomingDates = useMemo(() => {
    const dates = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    for (let i = 0; i < 7; i++) {
        const d = new Date(today);
        d.setDate(today.getDate() + i);
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        const dateStr = `${yyyy}-${mm}-${dd}`;
        dates.push({
            dateStr,
            dayName: d.toLocaleDateString('en-US', { weekday: 'short' }),
            dateNumber: d.getDate(),
            fullDateLabel: d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })
        });
    }
    return dates;
  }, []);

  useEffect(() => {
    if (!serviceId) {
      navigate('/services');
      return;
    }

    let cancelled = false;
    getServices()
      .then(res => {
        if (cancelled) return;
        const raw = res.data;
        const list = Array.isArray(raw) ? raw : (raw?.data ?? []);
        const found = list.find(s => s.id === serviceId);
        if (found) {
            setService(found);
        } else {
            navigate('/services');
        }
      })
      .catch(err => console.error('Failed to load service:', err));

    return () => { cancelled = true; };
  }, [serviceId, navigate]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    setSelectedSlot(null); // Reset slot on date change

    getSlots(selectedDate)
      .then(res => {
        if (cancelled) return;
        const raw = res.data;
        const list = Array.isArray(raw) ? raw : (raw?.data ?? []);
        setSlots(list);
      })
      .catch(err => {
        if (cancelled) return;
        setError('Could not load time slots. Please refresh.');
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [selectedDate]);

  const handleContinue = () => {
    if (!selectedSlot) return;
    navigate(`/book/step3?serviceId=${serviceId}&slotId=${selectedSlot.id}`, {
      state: { serviceId, slotId: selectedSlot.id, weight }
    });
  };

  const handleBack = () => {
    navigate(`/book/step1?serviceId=${serviceId}`);
  };

  const selectedDateInfo = upcomingDates.find(d => d.dateStr === selectedDate);
  const price = Number(service?.pricePerKg ?? service?.price_per_kg ?? 0);
  const estimatedTotal = price * weight;

  return (
    <div className="min-h-screen bg-neutral-50 flex flex-col">
      <Navbar />

      <main className="flex-1 w-full max-w-[1024px] mx-auto px-4 md:px-6 py-10 pb-32">
        <ProgressStepper current={2} />

        <div className="flex flex-col lg:flex-row gap-8">
          {/* ── Left Column: Slots ─────────────────────────────────────── */}
          <div className="w-full lg:w-[65%]">
            
            {/* Date Picker Strip */}
            <div className="flex gap-2 overflow-x-auto pb-4 mb-4 hide-scrollbar">
                {upcomingDates.map((item) => {
                    const isSelected = item.dateStr === selectedDate;
                    return (
                        <button
                            key={item.dateStr}
                            onClick={() => setSelectedDate(item.dateStr)}
                            className={`flex flex-col items-center justify-center min-w-[56px] h-[72px] rounded-lg border shrink-0 transition-colors outline-none focus-visible:ring-2 focus-visible:ring-primary ${
                                isSelected 
                                ? 'bg-primary text-white border-primary' 
                                : 'bg-white text-neutral-700 border-neutral-300 hover:border-primary/50'
                            }`}
                        >
                            <span className={`text-[12px] font-semibold ${isSelected ? 'text-white/80' : 'text-neutral-500'}`}>{item.dayName}</span>
                            <span className="text-h3 leading-none mt-1">{item.dateNumber}</span>
                        </button>
                    )
                })}
            </div>

            <h3 className="text-h3 text-neutral-900 mb-4">Available Slots for {selectedDateInfo?.fullDateLabel}</h3>

            {/* Slots Grid */}
            {error ? (
                <div className="bg-red-50 border border-red-200 text-red-700 p-6 rounded-card text-center">
                    <p className="mb-4">{error}</p>
                    <button 
                        onClick={() => setSelectedDate(selectedDate)} 
                        className="bg-white border border-red-200 text-red-600 px-4 py-2 rounded-btn font-medium hover:bg-red-50 transition-colors"
                    >
                        Retry
                    </button>
                </div>
            ) : loading ? (
                <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                    {Array.from({ length: 9 }).map((_, i) => (
                        <div key={i} className="h-24 bg-white rounded-card border border-neutral-200 p-4 animate-pulse">
                            <div className="h-5 w-2/3 bg-neutral-100 rounded mb-3"></div>
                            <div className="h-4 w-1/3 bg-neutral-100 rounded"></div>
                        </div>
                    ))}
                </div>
            ) : slots.length === 0 ? (
                <div className="bg-white border border-neutral-200 p-10 rounded-card text-center">
                    <p className="text-body text-neutral-500">No slots available for this date. Try another day.</p>
                </div>
            ) : (
                <div className="grid gap-3 sm:grid-cols-2 md:grid-cols-3">
                    {slots.map(slot => {
                        const isFull = slot.currentBookingCount >= slot.maxCapacity;
                        const capacityRatio = slot.currentBookingCount / slot.maxCapacity;
                        const isNearlyFull = capacityRatio >= 0.8 && !isFull;
                        const isSelected = selectedSlot?.id === slot.id;
                        
                        return (
                            <button
                                key={slot.id}
                                disabled={isFull}
                                onClick={() => setSelectedSlot(slot)}
                                className={`relative flex flex-col justify-center text-left p-4 rounded-card border transition-all duration-150 outline-none focus-visible:ring-2 focus-visible:ring-primary ${
                                    isFull 
                                    ? 'bg-neutral-100 border-neutral-200 cursor-not-allowed text-neutral-400'
                                    : isSelected
                                    ? 'bg-[#F0F7FF] border-[2.5px] border-primary shadow-card'
                                    : isNearlyFull
                                    ? 'bg-white border-warning hover:shadow-card'
                                    : 'bg-white border-neutral-300 hover:border-primary/40 hover:shadow-card'
                                }`}
                            >
                                {isSelected && (
                                    <span className="absolute top-2 right-2 w-4 h-4 rounded-full bg-primary flex items-center justify-center">
                                        <svg className="w-2.5 h-2.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3">
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                                        </svg>
                                    </span>
                                )}
                                
                                <span className={`text-[15px] font-bold mb-1 ${isFull ? 'text-neutral-400' : 'text-neutral-900'}`}>
                                    {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
                                </span>
                                
                                <div className="flex items-center gap-2">
                                    {isNearlyFull && !isSelected && (
                                        <span className="bg-warning-light text-warning-dark text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wide inline-block">
                                            Filling up
                                        </span>
                                    )}
                                    <span className={`text-[13px] ${isFull ? 'text-neutral-400' : 'text-neutral-500'}`}>
                                        {isFull ? 'Fully Booked' : `${slot.maxCapacity - slot.currentBookingCount} of ${slot.maxCapacity} left`}
                                    </span>
                                </div>
                            </button>
                        );
                    })}
                </div>
            )}
          </div>

          {/* ── Right Column: Summary Sidebar ──────────────────────────── */}
          <div className="w-full lg:w-[35%] relative">
             <div className="sticky top-24 bg-white border border-neutral-200 rounded-card p-6 shadow-sm">
                 <h3 className="text-h3 text-neutral-900 mb-5">Booking Summary</h3>
                 
                 <div className="mb-4 pb-4 border-b border-neutral-100 flex items-start gap-3">
                     <div className="w-8 h-8 rounded shrink-0 bg-primary-light flex items-center justify-center text-primary mt-0.5">
                        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                        </svg>
                     </div>
                     <div>
                         <p className="text-body font-semibold text-neutral-900 leading-tight">
                             {service?.name || 'Loading...'}
                         </p>
                         <p className="text-caption text-neutral-500 mt-0.5">
                             {service ? `₱${price.toFixed(2)} / kg` : ''}
                         </p>
                     </div>
                 </div>

                 <div className="mb-6 pb-4 border-b border-neutral-100 flex items-start gap-3">
                     <div className="w-8 h-8 rounded shrink-0 bg-neutral-100 flex items-center justify-center text-neutral-500 mt-0.5">
                        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                     </div>
                     <div>
                         <p className="text-body font-medium text-neutral-900 leading-tight">
                            {selectedSlot ? selectedDateInfo?.fullDateLabel : '—'}
                         </p>
                         <p className="text-caption text-neutral-500 mt-0.5">
                            {selectedSlot ? `${formatTime(selectedSlot.startTime)} - ${formatTime(selectedSlot.endTime)}` : 'Select a time slot'}
                         </p>
                     </div>
                 </div>

                 <div className="mb-6 pb-4 border-b border-neutral-100">
                    <p className="text-[13px] font-semibold text-neutral-700 mb-3">Estimated Weight</p>
                    <div className="flex items-center gap-3">
                        <div className="flex items-center justify-between w-32 border border-neutral-300 rounded-lg p-1">
                            <button 
                                onClick={() => setWeight(Math.max(0, weight - 1))}
                                className="w-8 h-8 flex items-center justify-center rounded-md hover:bg-neutral-100 active:bg-neutral-200 text-neutral-500 transition-colors"
                            >
                                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M20 12H4" /></svg>
                            </button>
                            <span className="font-semibold w-8 text-center">{weight}</span>
                            <button 
                                onClick={() => setWeight(weight + 1)}
                                className="w-8 h-8 flex items-center justify-center rounded-md hover:bg-neutral-100 active:bg-neutral-200 text-neutral-500 transition-colors"
                            >
                                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" /></svg>
                            </button>
                        </div>
                        <span className="text-neutral-500 font-medium">kg</span>
                    </div>
                 </div>

                 <div className="flex items-center justify-between mb-2">
                    <span className="text-body font-medium text-neutral-500">Estimated Total</span>
                    <span className="text-[22px] font-bold text-primary">₱{estimatedTotal.toFixed(2)}</span>
                 </div>
                 
                 <p className="text-[12px] italic text-neutral-400">
                    Final amount based on actual weight at pickup.
                 </p>

             </div>
          </div>
        </div>
      </main>

      {/* ── Sticky footer bar ────────────────────────────────── */}
      <footer className="fixed bottom-0 left-0 right-0 z-navbar bg-white border-t border-neutral-100 shadow-elevated">
        <div className="max-w-[1024px] mx-auto px-4 md:px-6 py-4 flex items-center justify-between">
          <button
            id="booking-step2-back"
            onClick={handleBack}
            className="transition-btn border-[1.5px] border-primary text-primary text-body font-semibold uppercase tracking-wide px-6 py-2.5 rounded-btn hover:bg-primary-light active:scale-[0.97]"
          >
            Back
          </button>
          <button
            id="booking-step2-continue"
            onClick={handleContinue}
            disabled={!selectedSlot}
            className={`transition-btn bg-primary text-white text-body font-semibold uppercase tracking-wide px-8 py-2.5 rounded-btn active:scale-[0.97] ${
              selectedSlot
                ? 'hover:bg-primary-dark'
                : 'opacity-40 cursor-not-allowed'
            }`}
          >
            Continue
          </button>
        </div>
      </footer>
    </div>
  );
}

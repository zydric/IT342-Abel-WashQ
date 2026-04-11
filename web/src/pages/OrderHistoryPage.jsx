import { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { getBookings, cancelBooking } from '../api/bookingApi';
import StatusTimeline from '../components/StatusTimeline';

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

  // We highlight "My Bookings" for either /bookings or /orders
  const navLinks = [
    { label: 'Dashboard', to: '/dashboard' },
    { label: 'Services', to: '/services' },
    { label: 'My Bookings', to: '/orders' },
  ];

  return (
    <nav className="h-16 bg-primary-dark sticky top-0 z-navbar shadow-elevated">
      <div className="max-w-content mx-auto h-full flex items-center justify-between px-6">
        <Link to="/dashboard" className="text-white font-bold text-[20px] tracking-tight hover:opacity-90 transition-opacity select-none">
          WashQ
        </Link>
        <div className="hidden md:flex items-center gap-8">
          {navLinks.map(({ label, to }) => {
            const active = location.pathname.startsWith('/order') || location.pathname.startsWith('/booking') 
                            ? to === '/orders' 
                            : location.pathname === to;
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
                <Link to="/orders" onClick={() => setDropdownOpen(false)} className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors">My Bookings</Link>
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

// ─── Formatting Helpers ────────────────────────────────────────────────────────
function formatDateTime(dateStr) {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('en-US', { 
    year: 'numeric', month: 'short', day: 'numeric',
    hour: 'numeric', minute: '2-digit' 
  });
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('en-US', { 
    weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' 
  });
}

const STATUS_COLORS = {
  PENDING: 'bg-neutral-100 text-neutral-700 border-neutral-200',
  RECEIVED: 'bg-blue-50 text-blue-700 border-blue-200',
  IN_PROGRESS: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  READY_FOR_PICKUP: 'bg-warning-light text-warning-dark border-warning/50',
  COMPLETED: 'bg-success-light text-success-dark border-success/30',
  CANCELLED: 'bg-red-50 text-red-700 border-red-200',
};

// ─── Booking Card Component ────────────────────────────────────────────────────
function BookingCard({ booking, onRequestCancel }) {
  const [expanded, setExpanded] = useState(false);
  const s = booking.service || {};
  const t = booking.timeSlot || {};

  return (
    <div className="bg-white rounded-card shadow-card border border-neutral-200 overflow-hidden mb-4 transition-all">
        <div className="p-5 sm:p-6 flex flex-col md:flex-row gap-5 md:items-center justify-between">
            
            {/* Left: Identifier */}
            <div className="flex gap-4 items-start md:flex-1 min-w-[200px]">
                <div className="w-12 h-12 rounded-lg bg-primary-light flex items-center justify-center text-primary shrink-0 mt-0.5">
                    <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                </div>
                <div>
                    <h3 className="text-h3 text-neutral-900 leading-tight">{s.name || 'Laundry Service'}</h3>
                    <p className="text-[12px] font-mono text-neutral-400 mt-1 uppercase">ID: {booking.id}</p>
                    <p className="text-caption text-neutral-400 md:hidden mt-2">
                         {formatDate(t.slotDate)} · {t.startTime}
                    </p>
                </div>
            </div>

            {/* Center: Details (Hidden on mobile via flex layout adjustment, shown inline) */}
            <div className="hidden md:flex flex-col gap-1 md:flex-1 min-w-[150px]">
                <p className="text-body font-medium text-neutral-900">{formatDate(t.slotDate)}</p>
                <p className="text-caption text-neutral-500">{t.startTime} - {t.endTime} · {booking.estimatedWeightKg} kg</p>
            </div>

            {/* Right: Price & Status */}
            <div className="flex items-center justify-between md:justify-end gap-6 md:flex-1 shrink-0">
                <div className="text-left md:text-right">
                   <p className="text-h3 text-primary">₱{booking.totalAmount?.toFixed(2)}</p>
                </div>
                <div className="flex flex-col gap-2 items-end">
                    <span className={`px-3 py-1 rounded-full text-[11px] font-bold uppercase tracking-wide border ${STATUS_COLORS[booking.status] || STATUS_COLORS.PENDING}`}>
                        {booking.status.replace(/_/g, ' ')}
                    </span>
                    <button 
                        onClick={() => setExpanded(!expanded)}
                        className="text-caption font-semibold text-neutral-500 hover:text-primary transition-colors flex items-center gap-1"
                    >
                        {expanded ? 'Hide Details' : 'View Details'}
                        <svg className={`w-3.5 h-3.5 transition-transform ${expanded ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5"><path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" /></svg>
                    </button>
                </div>
            </div>
        </div>

        {/* Expanded View */}
        {expanded && (
            <div className="border-t border-neutral-100 bg-neutral-50/50 p-5 sm:p-6 text-sm animate-in fade-in slide-in-from-top-2 duration-200">
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
                    <div>
                        <p className="text-caption text-neutral-400 font-semibold uppercase mb-1">Time Slot</p>
                        <p className="text-neutral-900 font-medium">{formatDate(t.slotDate)}</p>
                        <p className="text-neutral-600">{t.startTime} to {t.endTime}</p>
                    </div>
                    <div>
                        <p className="text-caption text-neutral-400 font-semibold uppercase mb-1">Order Details</p>
                        <p className="text-neutral-900 font-medium">Weight: {booking.estimatedWeightKg} kg</p>
                        <p className="text-neutral-600">Rate: ₱{s.pricePerKg}/kg</p>
                    </div>
                    <div className="sm:col-span-2">
                        <p className="text-caption text-neutral-400 font-semibold uppercase mb-1">Special Instructions</p>
                        <p className="text-neutral-900 italic">
                            {booking.specialInstructions ? `"${booking.specialInstructions}"` : 'None provided.'}
                        </p>
                    </div>
                </div>

                <div className="mt-8 mb-4">
                    <StatusTimeline currentStatus={booking.status} />
                </div>

                <div className="mt-6 flex items-center justify-between border-t border-neutral-200 pt-4">
                    <p className="text-caption text-neutral-400">Booked on {formatDateTime(booking.createdAt)}</p>
                    
                    {booking.status === 'PENDING' && (
                        <button 
                            onClick={() => onRequestCancel(booking)}
                            className="px-4 py-1.5 rounded bg-white border border-red-200 text-red-600 text-caption font-bold uppercase transition-colors hover:bg-red-50 focus:ring-2 focus:ring-red-200"
                        >
                            Cancel Booking
                        </button>
                    )}
                </div>
            </div>
        )}
    </div>
  )
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function OrderHistoryPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Filters
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [sortOrder, setSortOrder] = useState('NEWEST');

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;

  // Modals & Toasts
  const [bookingToCancel, setBookingToCancel] = useState(null);
  const [cancelling, setCancelling] = useState(false);
  const [toastMsg, setToastMsg] = useState(null);

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = () => {
    setLoading(true);
    getBookings()
      .then(res => {
         const data = Array.isArray(res.data) ? res.data : (res.data?.data || []);
         setBookings(data);
         setError(null);
      })
      .catch(err => {
         console.error(err);
         setError('Failed to load your booking history.');
      })
      .finally(() => setLoading(false));
  };

  const confirmCancelBooking = async () => {
    if (!bookingToCancel) return;
    setCancelling(true);
    try {
        await cancelBooking(bookingToCancel.id);
        setBookingToCancel(null);
        setToastMsg("Booking cancelled successfully.");
        fetchBookings();
        setTimeout(() => setToastMsg(null), 3500);
    } catch (err) {
        alert(err.response?.data?.error?.message || "Failed to cancel booking");
    } finally {
        setCancelling(false);
    }
  };

  // Find active booking for tracker (first non-cancelled, non-completed)
  const activeBooking = useMemo(() => {
    return bookings.find(b => b.status !== 'COMPLETED' && b.status !== 'CANCELLED');
  }, [bookings]);

  // Apply filters
  const filteredBookings = useMemo(() => {
      let filtered = [...bookings];
      
      if (statusFilter !== 'ALL') {
          filtered = filtered.filter(b => b.status === statusFilter);
      }

      filtered.sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return sortOrder === 'NEWEST' ? dateB - dateA : dateA - dateB;
      });

      return filtered;
  }, [bookings, statusFilter, sortOrder]);

  // Pagination logic
  const totalPages = Math.ceil(filteredBookings.length / ITEMS_PER_PAGE);
  const paginatedBookings = filteredBookings.slice(
      (currentPage - 1) * ITEMS_PER_PAGE, 
      currentPage * ITEMS_PER_PAGE
  );

  // Reset page on filter change
  useEffect(() => { setCurrentPage(1); }, [statusFilter, sortOrder]);

  const FILTER_OPTIONS = [
      { id: 'ALL', label: 'All' },
      { id: 'PENDING', label: 'Pending' },
      { id: 'RECEIVED', label: 'Received' },
      { id: 'IN_PROGRESS', label: 'In Progress' },
      { id: 'READY_FOR_PICKUP', label: 'Ready for Pickup' },
      { id: 'COMPLETED', label: 'Completed' },
      { id: 'CANCELLED', label: 'Cancelled' }
  ];

  return (
    <div className="min-h-screen bg-neutral-50 flex flex-col">
      <Navbar />

      <main className="flex-1 w-full max-w-[1280px] mx-auto px-4 md:px-6 py-8 pb-32">
        <header className="mb-8">
            <h1 className="text-h1 text-neutral-900">Order History</h1>
            <p className="text-body text-neutral-500 mt-1">Track your active laundry and view past bookings.</p>
        </header>

        {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl mb-8">
                {error} <button onClick={fetchBookings} className="font-bold underline ml-2">Retry</button>
            </div>
        )}

        {/* ── Active Tracker Card ── */}
        {!loading && activeBooking && (
            <div className="bg-blue-50 rounded-[16px] shadow-sm border border-blue-100 p-6 mb-10 overflow-hidden relative">
                <div className="flex items-center justify-between mb-4 relative z-10">
                    <h3 className="text-h3 text-neutral-900 flex items-center gap-2">
                        <svg className="w-5 h-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5"><path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                        Active Booking Tracker
                    </h3>
                    <span className="text-caption font-mono text-neutral-500 uppercase">ID: {activeBooking.id}</span>
                </div>
                
                <div className="relative z-10 mb-2">
                    <p className="text-body font-semibold text-neutral-900">{activeBooking.service?.name}</p>
                    <p className="text-sm text-neutral-500">
                        {formatDate(activeBooking.timeSlot?.slotDate)} · {activeBooking.timeSlot?.startTime} - {activeBooking.timeSlot?.endTime}
                    </p>
                </div>

                <div className="relative z-10 mt-6 sm:mb-8 pb-4">
                    <StatusTimeline currentStatus={activeBooking.status} />
                </div>
            </div>
        )}

        {/* ── Filter Bar ── */}
        <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 mb-6">
            <div className="flex flex-wrap items-center gap-2">
                {FILTER_OPTIONS.map(opt => (
                    <button
                        key={opt.id}
                        onClick={() => setStatusFilter(opt.id)}
                        className={`px-4 py-1.5 rounded-full text-[13px] font-bold transition-colors ${
                            statusFilter === opt.id
                            ? 'bg-primary text-white shadow-sm'
                            : 'bg-white border border-neutral-300 text-neutral-600 hover:bg-neutral-50'
                        }`}
                    >
                        {opt.label}
                    </button>
                ))}
            </div>
            
            <div className="shrink-0 flex items-center gap-2">
                <span className="text-caption font-semibold text-neutral-500">Sort:</span>
                <select 
                    value={sortOrder} 
                    onChange={(e) => setSortOrder(e.target.value)}
                    className="bg-white border border-neutral-300 text-body font-medium text-neutral-700 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary/50"
                >
                    <option value="NEWEST">Newest first</option>
                    <option value="OLDEST">Oldest first</option>
                </select>
            </div>
        </div>

        {/* ── Order List ── */}
        {loading ? (
            <div className="flex flex-col gap-4">
                {[1, 2, 3].map(i => (
                    <div key={i} className="bg-white rounded-card border border-neutral-200 p-6 h-28 animate-pulse flex items-center justify-between">
                        <div className="bg-neutral-100 rounded h-12 w-12 shrink-0"></div>
                        <div className="flex-1 ml-4 space-y-3">
                            <div className="h-4 bg-neutral-100 rounded w-1/4"></div>
                            <div className="h-3 bg-neutral-100 rounded w-1/3"></div>
                        </div>
                        <div className="h-8 bg-neutral-100 rounded w-24"></div>
                    </div>
                ))}
            </div>
        ) : filteredBookings.length === 0 ? (
            <div className="bg-white border border-neutral-200 rounded-card p-12 text-center flex flex-col items-center">
                <div className="w-20 h-20 bg-neutral-100 rounded-full flex items-center justify-center text-neutral-400 mb-4">
                    <svg className="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5"><path strokeLinecap="round" strokeLinejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
                </div>
                <h3 className="text-h3 text-neutral-900 mb-2">No bookings found</h3>
                {statusFilter !== 'ALL' ? (
                    <p className="text-body text-neutral-500 mb-5">No bookings match this filter.</p>
                ) : (
                    <>
                        <p className="text-body text-neutral-500 mb-6">You haven't made any laundry bookings yet.</p>
                        <Link to="/services" className="bg-primary text-white font-bold uppercase tracking-wide text-caption px-6 py-2.5 rounded-btn hover:bg-primary-dark transition-colors">
                            Book Now
                        </Link>
                    </>
                )}
            </div>
        ) : (
            <div className="flex flex-col">
                {paginatedBookings.map(booking => (
                    <BookingCard key={booking.id} booking={booking} onRequestCancel={setBookingToCancel} />
                ))}
            </div>
        )}

        {/* Pagination Controls */}
        {!loading && totalPages > 1 && (
            <div className="mt-8 flex items-center justify-center gap-2">
                <button 
                    disabled={currentPage === 1}
                    onClick={() => setCurrentPage(p => p - 1)}
                    className="w-10 h-10 rounded-lg border border-neutral-300 flex items-center justify-center text-neutral-500 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                    &larr;
                </button>
                
                {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                    <button
                        key={page}
                        onClick={() => setCurrentPage(page)}
                        className={`w-10 h-10 rounded-lg text-[13px] font-bold transition-colors ${
                            currentPage === page
                            ? 'bg-primary text-white border border-primary'
                            : 'bg-white border border-neutral-300 text-neutral-600 hover:bg-neutral-50'
                        }`}
                    >
                        {page}
                    </button>
                ))}

                <button 
                    disabled={currentPage === totalPages}
                    onClick={() => setCurrentPage(p => p + 1)}
                    className="w-10 h-10 rounded-lg border border-neutral-300 flex items-center justify-center text-neutral-500 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                    &rarr;
                </button>
            </div>
        )}

      </main>

      {/* Cancellation Modal */}
      {bookingToCancel && (
        <div className="fixed inset-0 bg-neutral-900/40 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
            <div className="bg-white rounded-card shadow-elevated w-full max-w-sm p-6 text-center animate-in fade-in zoom-in duration-200">
                <div className="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4 text-red-600 border-4 border-white shadow-sm">
                    <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                       <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                </div>
                <h3 className="text-h2 text-neutral-900 mb-2">Cancel Booking?</h3>
                <p className="text-body text-neutral-500 mb-6 leading-relaxed">
                    Are you sure you want to cancel your <strong>{bookingToCancel.service?.name}</strong> booking? This action cannot be undone.
                </p>
                <div className="flex gap-3">
                    <button
                        onClick={() => setBookingToCancel(null)}
                        disabled={cancelling}
                        className="flex-1 bg-white border border-neutral-300 text-neutral-700 text-body font-semibold uppercase tracking-wide py-2.5 rounded-btn hover:bg-neutral-50 transition-colors disabled:opacity-50"
                    >
                        Keep It
                    </button>
                    <button
                        onClick={confirmCancelBooking}
                        disabled={cancelling}
                        className="flex-1 bg-red-600 text-white text-body font-semibold uppercase tracking-wide py-2.5 rounded-btn hover:bg-red-700 transition-colors disabled:opacity-80 flex items-center justify-center gap-2"
                    >
                        {cancelling ? (
                            <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                        ) : 'Yes, Cancel'}
                    </button>
                </div>
            </div>
        </div>
      )}

      {/* Toast Notification */}
      {toastMsg && (
          <div className="fixed bottom-6 right-6 lg:bottom-10 lg:right-10 z-[200] bg-white border border-green-200 text-neutral-800 px-5 py-3 rounded-card shadow-elevated flex items-center gap-3 animate-in fade-in slide-in-from-bottom-5">
              <div className="w-6 h-6 rounded-full bg-green-100 text-green-600 flex items-center justify-center shrink-0">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3"><path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" /></svg>
              </div>
              <p className="text-[14px] font-semibold tracking-wide pr-2">{toastMsg}</p>
          </div>
      )}
    </div>
  );
}

import { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import StatusTimeline from '../components/StatusTimeline';
import { getBookings, updateBookingStatus, cancelBooking } from '../api/bookingApi';
import BookingStatusDropdown from '../components/BookingStatusDropdown';

// ─── Formatting Helpers ────────────────────────────────────────────────────────
function formatDateTime(dateStr) {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('en-US', { 
    year: 'numeric', month: 'short', day: 'numeric',
    hour: 'numeric', minute: '2-digit' 
  });
}

const STATUS_COLORS = {
  PENDING: 'bg-neutral-100 text-neutral-700',
  RECEIVED: 'bg-blue-50 text-blue-700',
  IN_PROGRESS: 'bg-orange-50 text-orange-700', 
  READY_FOR_PICKUP: 'bg-green-50 text-green-700',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-50 text-red-700',
};

const STATUS_DISPLAY_NAMES = {
  PENDING: 'Pending',
  RECEIVED: 'Received',
  IN_PROGRESS: 'In Progress',
  READY_FOR_PICKUP: 'Ready for Pickup',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled'
};

// ─── Table Row Component ───────────────────────────────────────────────────────
function BookingTableRow({ booking, onUpdateStatus, onCancelBooking, isAdmin }) {
    const [expanded, setExpanded] = useState(false);
    const [optimisticStatus, setOptimisticStatus] = useState(booking.status);

    // Sync if parent updates
    useEffect(() => { setOptimisticStatus(booking.status); }, [booking.status]);

    const handleUpdate = async (id, newStatus) => {
        const original = optimisticStatus;
        setOptimisticStatus(newStatus); // Optimistic UI
        try {
            await onUpdateStatus(id, newStatus);
        } catch(err) {
            setOptimisticStatus(original); // Revert
            throw err;
        }
    };

    const s = booking.service || {};
    const t = booking.timeSlot || {};

    const renderDate = (d) => new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });

    return (
        <>
            <tr 
                className="bg-white border-b border-neutral-100 hover:bg-neutral-50/50 transition-colors cursor-pointer"
                onClick={() => setExpanded(!expanded)}
            >
                <td className="px-6 py-4 whitespace-nowrap text-sm font-mono text-neutral-500 uppercase">
                    {booking.id}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-neutral-900">
                    {booking.user?.firstName} {booking.user?.lastName}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">
                    {s.name}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">
                    {t.slotDate ? renderDate(t.slotDate) : ''}, {t.startTime}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">
                    {booking.estimatedWeightKg} kg
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-neutral-900">
                    ₱{booking.totalAmount?.toFixed(2)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${STATUS_COLORS[optimisticStatus]}`}>
                        {STATUS_DISPLAY_NAMES[optimisticStatus]}
                    </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium" onClick={e => e.stopPropagation()}>
                    <BookingStatusDropdown 
                        booking={{...booking, status: optimisticStatus}} 
                        isAdmin={isAdmin}
                        onUpdateStatus={handleUpdate}
                        onViewDetails={() => setExpanded(true)}
                        onCancelBooking={() => onCancelBooking(booking)}
                    />
                </td>
            </tr>
            {expanded && (
                <tr className="bg-neutral-50/50">
                    <td colSpan={8} className="px-6 py-4 border-b border-neutral-200">
                        <div className="text-sm grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 animate-in fade-in duration-200 mb-6 mt-2">
                            <div>
                                <p className="font-semibold text-neutral-400 uppercase text-xs tracking-wider mb-2">Customer Info</p>
                                <p className="text-neutral-900 font-medium">{booking.user?.firstName} {booking.user?.lastName}</p>
                                <p className="text-neutral-600">{booking.user?.email}</p>
                                <p className="text-neutral-600">{booking.user?.contactNumber || 'No contact number'}</p>
                            </div>
                            <div className="lg:col-span-1">
                                <p className="font-semibold text-neutral-400 uppercase text-xs tracking-wider mb-2">Order Details</p>
                                <p className="text-neutral-900">Created: {formatDateTime(booking.createdAt)}</p>
                                <p className="text-neutral-600">Total: ₱{booking.totalAmount?.toFixed(2)} ({booking.estimatedWeightKg} kg)</p>
                            </div>
                            <div>
                                <p className="font-semibold text-neutral-400 uppercase text-xs tracking-wider mb-2">Special Instructions</p>
                                <p className="italic text-neutral-700">
                                    {booking.specialInstructions ? `"${booking.specialInstructions}"` : 'None provided.'}
                                </p>
                            </div>
                        </div>
                        <div className="pb-4">
                            <StatusTimeline currentStatus={optimisticStatus} />
                        </div>
                    </td>
                </tr>
            )}
        </>
    );
}

// ─── Main Staff Dashboard ─────────────────────────────────────────────────────
export default function StaffDashboardPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;
  
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role === 'ADMIN';

  useEffect(() => {
     if (user.role !== 'STAFF' && user.role !== 'ADMIN') {
         navigate('/dashboard'); // Kick customers out
         return;
     }
     fetchBookings();
  }, []);

  const fetchBookings = () => {
    setLoading(true);
    getBookings()
      .then(res => {
         const data = Array.isArray(res.data) ? res.data : (res.data?.data || []);
         setBookings(data);
      })
      .catch(err => {
         console.error(err);
         showToast("Failed to fetch bookings.", "error");
      })
      .finally(() => setLoading(false));
  };

  const showToast = (message, type = "success") => {
      setToast({ message, type });
      setTimeout(() => setToast(null), 2500);
  };

  const handleUpdateStatus = async (id, newStatus) => {
      try {
          await updateBookingStatus(id, newStatus);
          showToast(`Status updated to ${STATUS_DISPLAY_NAMES[newStatus]}`);
          // Background full refresh
          getBookings().then(res => {
              const data = Array.isArray(res.data) ? res.data : (res.data?.data || []);
              setBookings(data);
          });
      } catch (err) {
          showToast(err.response?.data?.error?.message || "Failed to update status", "error");
          throw err;
      }
  };

  const handleCancelBooking = async (booking) => {
      if (!window.confirm(`Force cancel booking ${booking.id}?`)) return;
      try {
          await cancelBooking(booking.id);
          showToast(`Booking ${booking.id} cancelled.`, 'error');
          fetchBookings();
      } catch (err) {
          showToast(err.response?.data?.error?.message || "Failed to cancel", "error");
      }
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  // Derived Stats
  const stats = useMemo(() => {
      const today = new Date().toISOString().split('T')[0];
      return {
          todayCount: bookings.filter(b => b.createdAt?.startsWith(today)).length,
          inProgress: bookings.filter(b => b.status === 'IN_PROGRESS').length,
          ready: bookings.filter(b => b.status === 'READY_FOR_PICKUP').length,
          pending: bookings.filter(b => b.status === 'PENDING').length
      };
  }, [bookings]);

  // Filtering
  const filteredBookings = useMemo(() => {
      let f = bookings;
      if (statusFilter !== 'ALL') {
          f = f.filter(b => b.status === statusFilter);
      }
      if (search) {
          const s = search.toLowerCase();
          f = f.filter(b => 
              b.id.toString().includes(s) || 
              (b.user?.firstName + ' ' + b.user?.lastName).toLowerCase().includes(s)
          );
      }
      return f;
  }, [bookings, statusFilter, search]);

  const totalPages = Math.ceil(filteredBookings.length / ITEMS_PER_PAGE);
  const paginated = filteredBookings.slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE);

  return (
    <div className="min-h-screen flex bg-[#F8FAFC]">
      
      {/* ── Sidebar ── */}
      <aside className="w-[240px] bg-[#0F172A] hidden md:flex flex-col shrink-0 min-h-screen shadow-xl sticky top-0">
          <div className="p-6 pb-2">
              <h1 className="text-white text-2xl font-bold tracking-tight">WashQ</h1>
          </div>
          
          <div className="px-6 py-4 text-[10px] font-bold text-neutral-500 tracking-widest uppercase">
              Main
          </div>

          <nav className="flex-1 px-4 space-y-1">
              <Link to="/staff/dashboard" className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-primary text-white font-medium transition-colors">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" /></svg>
                  Dashboard
              </Link>
              <button disabled className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-neutral-300 hover:text-white hover:bg-white/5 font-medium transition-colors cursor-not-allowed">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                  Bookings
              </button>
              {isAdmin && (
                  <button disabled className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-neutral-300 hover:text-white hover:bg-white/5 font-medium transition-colors cursor-not-allowed">
                      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /></svg>
                      Services
                  </button>
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
                  onClick={handleLogout}
                  className="w-full flex items-center gap-2 px-3 py-2 text-sm text-neutral-400 hover:text-white hover:bg-white/10 rounded transition-colors"
              >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
                  Sign Out
              </button>
          </div>
      </aside>

      {/* ── Main Dashboard Area ── */}
      <main className="flex-1 w-full px-4 py-8 md:px-8 overflow-y-auto">
          
          <header className="mb-8 flex items-center justify-between">
              <div>
                 <h2 className="text-2xl font-bold text-neutral-900 leading-tight">Dashboard</h2>
              </div>
          </header>

          {/* ── Stats Row ── */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {[
                  { label: 'Total Bookings Today', value: stats.todayCount, icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />, color: 'text-primary' },
                  { label: 'In Progress', value: stats.inProgress, icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />, color: 'text-orange-500' },
                  { label: 'Ready for Pickup', value: stats.ready, icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />, color: 'text-green-600' },
                  { label: 'Pending Payment', value: stats.pending, icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />, color: 'text-info' },
              ].map((s, i) => (
                  <div key={i} className="bg-white rounded-card shadow-card p-5 border border-neutral-100 flex items-start gap-4">
                      <div className={`mt-1 w-8 h-8 flex items-center justify-center ${s.color}`}>
                          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">{s.icon}</svg>
                      </div>
                      <div>
                          <p className="text-3xl font-bold text-neutral-900 mb-1">{loading ? '...' : s.value}</p>
                          <p className="text-xs font-semibold uppercase tracking-wide text-neutral-400">{s.label}</p>
                      </div>
                  </div>
              ))}
          </div>

          {/* ── Filters Bar ── */}
          <div className="bg-white rounded-xl shadow-sm border border-neutral-200 p-4 mb-6 flex flex-col md:flex-row gap-4 items-center justify-between">
              <div className="relative w-full md:w-96 text-neutral-500 focus-within:text-primary">
                  <svg className="w-5 h-5 absolute top-1/2 -translate-y-1/2 left-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                  <input 
                      type="text" 
                      placeholder="Search bookings..." 
                      className="w-full bg-neutral-50 border border-neutral-200 text-sm rounded-lg pl-10 pr-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                      value={search}
                      onChange={e => {setSearch(e.target.value); setCurrentPage(1);}}
                  />
              </div>

              <div className="flex w-full md:w-auto items-center gap-3">
                  <select 
                      className="bg-white border border-neutral-200 text-sm font-medium text-neutral-700 rounded-lg px-3 py-2 cursor-pointer outline-none focus:ring-2 focus:ring-primary/20"
                      value={statusFilter}
                      onChange={e => {setStatusFilter(e.target.value); setCurrentPage(1);}}
                  >
                      <option value="ALL">All Statuses</option>
                      {Object.entries(STATUS_DISPLAY_NAMES).map(([k, v]) => (
                          <option key={k} value={k}>{v}</option>
                      ))}
                  </select>
                  
                  <button className="hidden sm:flex border border-neutral-300 text-neutral-700 hover:bg-neutral-50 font-bold uppercase tracking-wide text-[11px] px-4 py-2 rounded-lg transition-colors">
                      Export CSV
                  </button>
              </div>
          </div>

          {/* ── Table ── */}
          <div className="bg-white rounded-[16px] shadow-sm border border-neutral-200 overflow-hidden">
              <div className="overflow-x-auto min-h-[400px]">
                  <table className="min-w-full divide-y divide-neutral-200">
                      <thead className="bg-[#F8FAFC]">
                          <tr>
                              {['Booking ID', 'Customer', 'Service', 'Slot', 'Weight', 'Amount', 'Status', 'Actions'].map((c, i) => (
                                  <th key={c} scope="col" className={`px-6 py-4 text-left text-[11px] font-bold text-neutral-400 uppercase tracking-wider ${i === 7 ? 'text-right' : ''}`}>
                                      {c}
                                  </th>
                              ))}
                          </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-neutral-200">
                          {loading ? (
                              Array.from({ length: 5 }).map((_, i) => (
                                  <tr key={i} className="animate-pulse">
                                      <td className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-16"></div></td>
                                      <td className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-24"></div></td>
                                      <td className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-20"></div></td>
                                      <td className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-24"></div></td>
                                      <td className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-8"></div></td>
                                      <td className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-12"></div></td>
                                      <td className="px-6 py-5"><div className="h-6 bg-neutral-100 rounded-full w-20"></div></td>
                                      <td className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-8 ml-auto"></div></td>
                                  </tr>
                              ))
                          ) : paginated.length === 0 ? (
                              <tr>
                                  <td colSpan={8} className="px-6 py-16 text-center text-neutral-500">
                                      No bookings found for the selected filters.
                                  </td>
                              </tr>
                          ) : (
                              paginated.map((booking) => (
                                  <BookingTableRow 
                                     key={booking.id} 
                                     booking={booking} 
                                     isAdmin={isAdmin}
                                     onUpdateStatus={handleUpdateStatus} 
                                     onCancelBooking={handleCancelBooking}
                                  />
                              ))
                          )}
                      </tbody>
                  </table>
              </div>

              {/* Table Footer / Pagination */}
              {!loading && totalPages > 1 && (
                  <div className="bg-white px-6 py-4 border-t border-neutral-200 flex items-center justify-between">
                      <p className="hidden sm:block text-sm text-neutral-500">
                          Showing <strong>{(currentPage-1)*ITEMS_PER_PAGE + 1}</strong> to <strong>{Math.min(currentPage*ITEMS_PER_PAGE, filteredBookings.length)}</strong> of <strong>{filteredBookings.length}</strong> results
                      </p>
                      <div className="flex items-center gap-2">
                          <button 
                              disabled={currentPage === 1}
                              onClick={() => setCurrentPage(p => p - 1)}
                              className="px-3 py-1.5 border border-neutral-300 rounded text-sm font-medium text-neutral-600 disabled:opacity-50 hover:bg-neutral-50 transition-colors"
                          >
                              Previous
                          </button>
                          <button 
                              disabled={currentPage === totalPages}
                              onClick={() => setCurrentPage(p => p + 1)}
                              className="px-3 py-1.5 border border-neutral-300 rounded text-sm font-medium text-neutral-600 disabled:opacity-50 hover:bg-neutral-50 transition-colors"
                          >
                              Next
                          </button>
                      </div>
                  </div>
              )}
          </div>
      </main>

      {/* ── Micro-Toast ── */}
      {toast && (
          <div className="fixed bottom-6 right-6 z-50 animate-in fade-in slide-in-from-bottom-5">
              <div className={`rounded-lg px-4 py-2.5 shadow-elevated flex items-center gap-2 text-sm font-medium ${
                  toast.type === 'error' ? 'bg-red-600 text-white' : 'bg-[#0F172A] text-white'
              }`}>
                  {toast.type === 'error' ? (
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5"><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                  ) : (
                      <svg className="w-4 h-4 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3"><path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" /></svg>
                  )}
                  {toast.message}
              </div>
          </div>
      )}
    </div>
  );
}

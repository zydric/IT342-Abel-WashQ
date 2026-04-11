import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import StatusTimeline from '../components/StatusTimeline';
import { getBookings, updateBookingStatus, cancelBooking } from '../api/bookingApi';
import BookingStatusDropdown from '../components/BookingStatusDropdown';
import Sidebar from '../components/Sidebar';

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

    useEffect(() => { setOptimisticStatus(booking.status); }, [booking.status]);

    const handleUpdate = async (id, newStatus) => {
        const original = optimisticStatus;
        setOptimisticStatus(newStatus);
        try {
            await onUpdateStatus(id, newStatus);
        } catch(err) {
            setOptimisticStatus(original);
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
         navigate('/dashboard');
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
          fetchBookings();
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

  const stats = useMemo(() => {
    const today = new Date().toISOString().split('T')[0];
    return {
        todayCount: bookings.filter(b => b.createdAt?.startsWith(today)).length,
        inProgress: bookings.filter(b => b.status === 'IN_PROGRESS').length,
        ready: bookings.filter(b => b.status === 'READY_FOR_PICKUP').length,
        pending: bookings.filter(b => b.status === 'PENDING').length
    };
  }, [bookings]);

  const filteredBookings = useMemo(() => {
      let f = bookings;
      if (statusFilter !== 'ALL') f = f.filter(b => b.status === statusFilter);
      if (search) {
          const s = search.toLowerCase();
          f = f.filter(b => b.id.toString().includes(s) || (b.user?.firstName + ' ' + b.user?.lastName).toLowerCase().includes(s));
      }
      return f;
  }, [bookings, statusFilter, search]);

  const totalPages = Math.ceil(filteredBookings.length / ITEMS_PER_PAGE);
  const paginated = filteredBookings.slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE);

  return (
    <div className="h-screen flex bg-[#F8FAFC] overflow-hidden">
      <Sidebar user={user} onLogout={handleLogout} />
      <main className="flex-1 w-full px-4 py-8 md:px-8 overflow-y-auto overflow-x-hidden">
          <header className="mb-8 flex items-center justify-between">
              <h2 className="text-2xl font-bold text-neutral-900 leading-tight">Dashboard</h2>
          </header>

          {/* Stats Summary Panel */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {[
                  { label: 'Total Today', value: stats.todayCount, color: 'text-primary' },
                  { label: 'In Progress', value: stats.inProgress, color: 'text-orange-500' },
                  { label: 'Ready for Pickup', value: stats.ready, color: 'text-green-600' },
                  { label: 'Pending Payment', value: stats.pending, color: 'text-info' },
              ].map((s, i) => (
                  <div key={i} className="bg-white rounded-card shadow-card p-5 border border-neutral-100 flex items-start gap-4">
                      <div>
                          <p className="text-2xl font-bold text-neutral-900 mb-0.5">{loading ? '...' : s.value}</p>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-neutral-400">{s.label}</p>
                      </div>
                  </div>
              ))}
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-neutral-200 p-4 mb-6 flex flex-col md:flex-row gap-4 items-center justify-between">
              <div className="relative w-full md:w-96 text-neutral-500 focus-within:text-primary">
                  <svg className="w-5 h-5 absolute top-1/2 -translate-y-1/2 left-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                  <input 
                      type="text" 
                      placeholder="Search by ID or customer name..." 
                      className="w-full bg-neutral-50 border border-neutral-200 text-sm rounded-lg pl-10 pr-4 py-2 focus:outline-none"
                      value={search}
                      onChange={e => {setSearch(e.target.value); setCurrentPage(1);}}
                  />
              </div>
              <div className="flex w-full md:w-auto items-center gap-3">
                  <select 
                      className="bg-white border border-neutral-200 text-sm font-medium text-neutral-700 rounded-lg px-3 py-2 outline-none"
                      value={statusFilter}
                      onChange={e => {setStatusFilter(e.target.value); setCurrentPage(1);}}
                  >
                      <option value="ALL">All Statuses</option>
                      {Object.entries(STATUS_DISPLAY_NAMES).map(([k, v]) => (
                          <option key={k} value={k}>{v}</option>
                      ))}
                  </select>
              </div>
          </div>

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
                          {loading ? Array.from({ length: 5 }).map((_, i) => (
                                  <tr key={i} className="animate-pulse">
                                      <td colSpan={8} className="px-6 py-5"><div className="h-4 bg-neutral-100 rounded w-full"></div></td>
                                  </tr>
                              )) : paginated.length === 0 ? (
                              <tr>
                                  <td colSpan={8} className="px-6 py-16 text-center text-neutral-500">No bookings found.</td>
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
              {!loading && totalPages > 1 && (
                  <div className="bg-white px-6 py-4 border-t border-neutral-200 flex items-center justify-between">
                      <p className="text-sm text-neutral-500">Showing {filteredBookings.length} results</p>
                      <div className="flex items-center gap-2">
                          <button disabled={currentPage === 1} onClick={() => setCurrentPage(p => p - 1)} className="px-3 py-1.5 border rounded text-sm disabled:opacity-50">Prev</button>
                          <button disabled={currentPage === totalPages} onClick={() => setCurrentPage(p => p + 1)} className="px-3 py-1.5 border rounded text-sm disabled:opacity-50">Next</button>
                      </div>
                  </div>
              )}
          </div>
      </main>
      {toast && (
          <div className="fixed bottom-6 right-6 z-50 animate-in fade-in slide-in-from-bottom-5">
              <div className={`rounded-lg px-4 py-2.5 shadow-elevated flex items-center gap-2 text-sm font-medium ${toast.type === 'error' ? 'bg-red-600 text-white' : 'bg-[#0F172A] text-white'}`}>
                  {toast.message}
              </div>
          </div>
      )}
    </div>
  );
}

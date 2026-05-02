import { useState, useRef, useEffect } from 'react';

const NEXT_STATUS_MAP = {
    'PENDING': [{ id: 'RECEIVED', label: 'Mark as Received' }],
    'RECEIVED': [{ id: 'IN_PROGRESS', label: 'Start Processing' }],
    'IN_PROGRESS': [{ id: 'READY_FOR_PICKUP', label: 'Ready for Pickup' }],
    'READY_FOR_PICKUP': [{ id: 'COMPLETED', label: 'Complete Order' }],
    'COMPLETED': [],
    'CANCELLED': []
};

export default function BookingStatusDropdown({ 
    booking, 
    onUpdateStatus, 
    onViewDetails, 
    onCancelBooking,
    isAdmin 
}) {
    const [open, setOpen] = useState(false);
    const [inlineOpen, setInlineOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const menuRef = useRef();

    useEffect(() => {
        function handleClickOutside(e) {
            if (menuRef.current && !menuRef.current.contains(e.target)) {
                setOpen(false);
                setInlineOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleActionClick = async (action) => {
        setLoading(true);
        try {
            await onUpdateStatus(booking.id, action);
            setOpen(false);
            setInlineOpen(false);
        } finally {
            setLoading(false);
        }
    };

    const nextStatuses = NEXT_STATUS_MAP[booking.status] || [];
    const canUpdate = nextStatuses.length > 0;
    const canCancel = isAdmin && booking.status === 'PENDING';

    return (
        <div className="relative inline-block text-left" ref={menuRef}>
            <button 
                onClick={() => setOpen(!open)}
                className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-neutral-100 text-neutral-500 transition-colors"
                disabled={loading}
            >
                {loading ? (
                    <svg className="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                ) : (
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" /></svg>
                )}
            </button>

            {open && (
                <div className="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-elevated bg-white ring-1 ring-black ring-opacity-5 z-20">
                    <div className="py-1" role="menu" aria-orientation="vertical">
                        {canUpdate && !inlineOpen && (
                            <button
                                onClick={() => setInlineOpen(true)}
                                className="block w-full text-left flex items-center justify-between px-4 py-2 text-sm text-neutral-700 hover:bg-neutral-50 hover:text-primary transition-colors"
                            >
                                <span>Update Status</span>
                                <svg className="w-4 h-4 text-neutral-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" /></svg>
                            </button>
                        )}

                        {inlineOpen && nextStatuses.map(ns => (
                            <button
                                key={ns.id}
                                onClick={() => handleActionClick(ns.id)}
                                className="block w-full text-left px-4 py-3 text-sm font-semibold text-primary bg-primary/10 hover:bg-primary/20 transition-colors border-l-4 border-primary"
                            >
                                {ns.label} &rarr;
                            </button>
                        ))}

                        <button
                            onClick={() => { setOpen(false); onViewDetails(); }}
                            className="block w-full text-left px-4 py-2 text-sm text-neutral-700 hover:bg-neutral-50 transition-colors"
                        >
                            View Details
                        </button>

                        {canCancel && (
                            <button
                                onClick={() => { setOpen(false); onCancelBooking(); }}
                                className="block w-full text-left px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 transition-colors border-t border-neutral-100"
                            >
                                Cancel Booking
                            </button>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

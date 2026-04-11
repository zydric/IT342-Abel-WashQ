import { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { createBooking } from '../api/bookingApi';
import Navbar from '../components/Navbar';

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

// ─── Format Time Helper ───────────────────────────────────────────────────────
function formatTime(timeStr) {
  if (!timeStr) return '';
  const [hours, minutes] = timeStr.split(':');
  const d = new Date();
  d.setHours(hours, minutes);
  return d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
}

function formatDateLabel(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function BookingStep3Page() {
  const navigate = useNavigate();
  const location = useLocation();

  const { service, slot, weight } = location.state || {};

  const [specialInstructions, setSpecialInstructions] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  useEffect(() => {
    if (!service || !slot || weight === undefined) {
      // If accessed directly without state, bounce back
      navigate('/services');
    }
  }, [service, slot, weight, navigate]);

  if (!service || !slot) return null; // Prevent rendering during redirect

  const price = Number(service.pricePerKg ?? service.price_per_kg ?? 0);
  const estimatedTotal = price * weight;

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);

    const payload = {
        serviceId: service.id,
        timeSlotId: slot.id,
        estimatedWeightKg: weight,
        specialInstructions: specialInstructions.trim()
    };

    try {
        const response = await createBooking(payload);
        if (response.data?.success) {
            setShowSuccessModal(true);
        }
    } catch (err) {
        setLoading(false);
        const errDetails = err.response?.data?.error;
        if (errDetails?.code === 'SLOT-001') {
            setError("This slot is no longer available, please choose another.");
        } else if (errDetails?.code === 'SLOT-002') {
            setError("You already have a booking in this time slot.");
        } else {
            setError(errDetails?.message || "An error occurred while creating your booking.");
        }
    }
  };

  const handleBack = () => {
    navigate(`/book/step2?serviceId=${service.id}`, {
      state: { serviceId: service.id }
    });
  };

  return (
    <div className="min-h-screen bg-neutral-50 flex flex-col">
      
      <Navbar />

      <main className="flex-1 w-full max-w-[720px] mx-auto px-6 py-10 pb-32">
        <ProgressStepper current={3} />

        <header className="mb-8">
          <h1 className="text-h1 text-neutral-900">Review Your Booking</h1>
          <p className="text-body text-neutral-400 mt-1">
            Please confirm your booking details before proceeding to payment.
          </p>
        </header>

        {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-card mb-6 flex flex-col sm:flex-row items-center justify-between gap-4">
                <p className="text-body font-medium">{error}</p>
                <button 
                  onClick={handleBack} 
                  className="bg-white border border-red-200 text-red-600 px-4 py-1.5 rounded-btn font-medium hover:bg-red-50 transition-colors whitespace-nowrap"
                >
                  Go Back
                </button>
            </div>
        )}

        <div className="bg-white rounded-card border border-neutral-200 p-8 shadow-sm">
            {/* Service Row */}
            <div className="flex items-start gap-4 mb-6 pb-6 border-b border-neutral-100">
                <div className="w-10 h-10 rounded shrink-0 bg-primary-light flex items-center justify-center text-primary mt-1">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                </div>
                <div className="flex-1">
                    <p className="text-caption text-neutral-400 uppercase tracking-wide font-semibold mb-1">Service</p>
                    <div className="flex items-center justify-between">
                        <p className="text-h3 text-neutral-900">{service.name}</p>
                        <p className="text-body font-semibold text-primary">₱{price.toFixed(2)}/kg</p>
                    </div>
                </div>
            </div>

            {/* Time Slot Row */}
            <div className="flex items-start gap-4 mb-6 pb-6 border-b border-neutral-100">
                <div className="w-10 h-10 rounded shrink-0 bg-neutral-100 flex items-center justify-center text-neutral-500 mt-1">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                </div>
                <div className="flex-1">
                    <p className="text-caption text-neutral-400 uppercase tracking-wide font-semibold mb-1">Time Slot</p>
                    <p className="text-body font-medium text-neutral-900">
                        {formatDateLabel(slot.slotDate)} · {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
                    </p>
                </div>
            </div>

            {/* Estimated Weight Row */}
            <div className="flex items-start gap-4 mb-8 pb-6 border-b border-neutral-100">
                <div className="w-10 h-10 rounded shrink-0 bg-neutral-100 flex items-center justify-center text-neutral-500 mt-1">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M3 6l3 1m0 0l-3 9a5.002 5.002 0 006.001 0M6 7l3 9M6 7l6-2m6 2l3-1m-3 1l-3 9a5.002 5.002 0 006.001 0M18 7l3 9m-3-9l-6-2m0-2v2m0 16V5m0 16H9m3 0h3" />
                    </svg>
                </div>
                <div className="flex-1">
                    <p className="text-caption text-neutral-400 uppercase tracking-wide font-semibold mb-1">Estimated Weight</p>
                    <p className="text-body font-medium text-neutral-900">{weight} kg</p>
                </div>
            </div>

            {/* Estimated Total */}
            <div className="flex items-center justify-between mb-2">
                <p className="text-body font-bold text-neutral-400 uppercase tracking-wide">Estimated Total</p>
                <p className="text-h1 text-primary">₱{estimatedTotal.toFixed(2)}</p>
            </div>
            <p className="text-caption text-neutral-400 text-right">Final charge is based on actual weight measured at the shop.</p>
        </div>

        {/* Special Instructions Field */}
        <div className="mt-8">
            <div className="flex items-center justify-between mb-2">
                <label htmlFor="specialInstructions" className="text-body font-semibold text-neutral-900">
                    Special Instructions <span className="text-neutral-400 font-normal">(Optional)</span>
                </label>
            </div>
            <textarea 
                id="specialInstructions"
                value={specialInstructions}
                onChange={(e) => {
                    if (e.target.value.length <= 300) setSpecialInstructions(e.target.value);
                }}
                rows={4}
                placeholder="e.g., separate whites from colors, handle with care..."
                className="w-full border border-neutral-300 rounded-lg p-4 text-body text-neutral-900 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow"
            />
            <p className="text-caption text-neutral-400 text-right mt-1">
                {specialInstructions.length} / 300
            </p>
        </div>

        {/* Payment Provider Badge */}
        <div className="mt-10 flex flex-col items-center justify-center gap-2">
            <div className="flex items-center gap-1.5 text-neutral-400 text-caption font-medium">
                <svg className="w-3.5 h-3.5 text-[#16A34A]" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" /></svg>
                Secured by PayMongo
            </div>
        </div>
      </main>

      {/* Sticky Footer */}
      <footer className="fixed bottom-0 left-0 right-0 z-navbar bg-white border-t border-neutral-100 shadow-elevated">
        <div className="max-w-[720px] mx-auto px-6 py-4 flex items-center justify-between">
          <button
            onClick={handleBack}
            disabled={loading}
            className={`transition-btn border-[1.5px] border-primary text-primary text-body font-semibold uppercase tracking-wide px-6 py-2.5 rounded-btn focus:outline-none focus-visible:ring-2 focus-visible:ring-primary ${
                loading ? 'opacity-50 cursor-not-allowed' : 'hover:bg-primary-light active:scale-[0.97]'
            }`}
          >
            Back
          </button>
          <button
            onClick={handleSubmit}
            disabled={loading}
            className={`transition-btn bg-primary text-white text-body font-semibold uppercase tracking-wide px-8 py-2.5 rounded-btn flex items-center gap-2 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 ${
                loading ? 'opacity-80 cursor-not-allowed' : 'hover:bg-primary-dark active:scale-[0.97]'
            }`}
          >
            {loading ? (
                <>
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Processing...
                </>
            ) : (
                'Proceed to Payment'
            )}
          </button>
        </div>
      </footer>

      {/* Overlay Submission */}
      {loading && !showSuccessModal && (
        <div className="fixed inset-0 bg-white/70 backdrop-blur-sm z-[100] flex flex-col items-center justify-center">
            <svg className="animate-spin h-10 w-10 text-primary mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <p className="text-h3 text-neutral-900">Locking your slot...</p>
        </div>
      )}

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="fixed inset-0 bg-neutral-900/40 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
            <div className="bg-white rounded-card shadow-elevated w-full max-w-sm p-6 text-center animate-in fade-in zoom-in duration-200">
                <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-4 text-green-600 border-4 border-white shadow-sm">
                    <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                </div>
                <h3 className="text-h2 text-neutral-900 mb-2">Booking Confirmed!</h3>
                <p className="text-body text-neutral-500 mb-6">
                    Your laundry session has been booked successfully.
                </p>
                <button
                    onClick={() => navigate('/bookings')}
                    className="w-full bg-primary text-white text-body font-semibold uppercase tracking-wide py-3 rounded-btn hover:bg-primary-dark active:scale-[0.98] transition-all"
                >
                    View My Bookings
                </button>
            </div>
        </div>
      )}
    </div>
  );
}

import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../api/auth';

/* ───── Inline SVG icons ───── */
const EyeIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-neutral-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
  </svg>
);
const EyeOffIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-neutral-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.97 9.97 0 012.31-3.814M6.938 6.938A9.966 9.966 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.969 9.969 0 01-4.043 5.062M6.938 6.938L3 3m3.938 3.938l3.124 3.124M21 21l-3.938-3.938m0 0l-3.124-3.124" />
  </svg>
);
const XCircleIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-error flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);
const CheckCircleIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-success flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);
const Spinner = () => (
  <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
  </svg>
);

const WaveDecoration = () => (
  <svg className="absolute bottom-0 left-0 w-full" viewBox="0 0 1440 320" preserveAspectRatio="none">
    <path fill="rgba(255,255,255,0.08)" d="M0,224L48,213.3C96,203,192,181,288,186.7C384,192,480,224,576,218.7C672,213,768,171,864,165.3C960,160,1056,192,1152,197.3C1248,203,1344,181,1392,170.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z" />
    <path fill="rgba(255,255,255,0.05)" d="M0,288L48,272C96,256,192,224,288,224C384,224,480,256,576,261.3C672,267,768,245,864,234.7C960,224,1056,224,1152,218.7C1248,213,1344,203,1392,197.3L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z" />
  </svg>
);

const INITIAL = { firstName: '', lastName: '', email: '', password: '', confirmPassword: '', address: '', contactNumber: '' };

export default function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState(INITIAL);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  const set = (field) => (e) => setForm((p) => ({ ...p, [field]: e.target.value }));

  /* ── validation ── */
  const validate = () => {
    const errs = {};
    if (!form.firstName.trim()) errs.firstName = 'First name is required';
    if (!form.lastName.trim()) errs.lastName = 'Last name is required';
    if (!form.email.trim()) errs.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Enter a valid email address';
    if (!form.password) errs.password = 'Password is required';
    else if (form.password.length < 8) errs.password = 'Password must be at least 8 characters';
    if (!form.confirmPassword) errs.confirmPassword = 'Please confirm your password';
    else if (form.password !== form.confirmPassword) errs.confirmPassword = 'Passwords do not match';
    if (!form.address.trim()) errs.address = 'Address is required';
    if (!form.contactNumber.trim()) errs.contactNumber = 'Contact number is required';
    return errs;
  };

  const handleBlur = (field) => {
    const errs = validate();
    setFieldErrors((prev) => ({ ...prev, [field]: errs[field] || '' }));
  };

  /* ── submit ── */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');
    const errs = validate();
    if (Object.keys(errs).length) {
      setFieldErrors(errs);
      return;
    }
    setFieldErrors({});
    setLoading(true);
    try {
      await registerUser({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
        address: form.address,
        contactNumber: form.contactNumber,
      });
      setSuccessMsg('Account created! Redirecting to login…');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      const msg =
        err.response?.data?.error?.message || 'Registration failed. Please try again.';
      setErrorMsg(msg);
    } finally {
      setLoading(false);
    }
  };

  /* ─── helpers ─── */
  const inputCls = (field) =>
    `w-full px-4 py-2.5 rounded-btn border text-body transition-input outline-none ${
      fieldErrors[field]
        ? 'border-error focus:border-error focus:ring-2 focus:ring-red-100'
        : 'border-[#CBD5E1] focus:border-primary focus:ring-3 focus:ring-primary-light'
    }`;

  return (
    <div className="flex min-h-screen bg-neutral-50">
      {/* ──── Left brand panel ──── */}
      <div className="hidden md:flex md:w-1/2 relative bg-primary-dark flex-col items-center justify-center text-white overflow-hidden">
        <div className="z-10 text-center px-8">
          <h1 className="text-[42px] font-bold tracking-tight mb-4">WashQ</h1>
          <p className="text-[22px] leading-relaxed opacity-90">
            Book your laundry slot.<br />Anytime.
          </p>
        </div>
        <WaveDecoration />
      </div>

      {/* ──── Right form panel ──── */}
      <div className="w-full md:w-1/2 flex items-center justify-center bg-white px-6 py-12 md:px-16 overflow-y-auto">
        <div className="w-full max-w-[440px]">
          {/* Brand mark */}
          <p className="text-primary font-bold text-[24px] mb-6">WashQ</p>

          <h1 className="text-h1 text-neutral-900 mb-1">Create account</h1>
          <p className="text-body text-neutral-400 mb-8">Sign up to get started</p>

          {/* Error banner */}
          {errorMsg && (
            <div className="flex items-center gap-2 bg-[#FEE2E2] border-l-4 border-error rounded-btn px-4 py-3 mb-6 animate-[slideDown_200ms_ease-out]">
              <XCircleIcon />
              <span className="text-body text-error">{errorMsg}</span>
            </div>
          )}

          {/* Success banner */}
          {successMsg && (
            <div className="flex items-center gap-2 bg-[#DCFCE7] border-l-4 border-success rounded-btn px-4 py-3 mb-6 animate-[slideDown_200ms_ease-out]">
              <CheckCircleIcon />
              <span className="text-body text-success">{successMsg}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            {/* Name row */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-body font-medium text-neutral-700 mb-1.5">First name</label>
                <input
                  type="text"
                  placeholder="John"
                  value={form.firstName}
                  onChange={set('firstName')}
                  onBlur={() => handleBlur('firstName')}
                  disabled={loading}
                  className={inputCls('firstName')}
                />
                {fieldErrors.firstName && <p className="text-caption text-error mt-1">{fieldErrors.firstName}</p>}
              </div>
              <div>
                <label className="block text-body font-medium text-neutral-700 mb-1.5">Last name</label>
                <input
                  type="text"
                  placeholder="Doe"
                  value={form.lastName}
                  onChange={set('lastName')}
                  onBlur={() => handleBlur('lastName')}
                  disabled={loading}
                  className={inputCls('lastName')}
                />
                {fieldErrors.lastName && <p className="text-caption text-error mt-1">{fieldErrors.lastName}</p>}
              </div>
            </div>

            {/* Email */}
            <div>
              <label className="block text-body font-medium text-neutral-700 mb-1.5">Email</label>
              <input
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={set('email')}
                onBlur={() => handleBlur('email')}
                disabled={loading}
                className={inputCls('email')}
              />
              {fieldErrors.email && <p className="text-caption text-error mt-1">{fieldErrors.email}</p>}
            </div>

            {/* Password */}
            <div>
              <label className="block text-body font-medium text-neutral-700 mb-1.5">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Min. 8 characters"
                  value={form.password}
                  onChange={set('password')}
                  onBlur={() => handleBlur('password')}
                  disabled={loading}
                  className={`${inputCls('password')} pr-10`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 bg-transparent border-none p-0 cursor-pointer transition-opacity duration-150"
                  tabIndex={-1}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              {fieldErrors.password && <p className="text-caption text-error mt-1">{fieldErrors.password}</p>}
            </div>

            {/* Confirm password */}
            <div>
              <label className="block text-body font-medium text-neutral-700 mb-1.5">Confirm password</label>
              <div className="relative">
                <input
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Re-enter password"
                  value={form.confirmPassword}
                  onChange={set('confirmPassword')}
                  onBlur={() => handleBlur('confirmPassword')}
                  disabled={loading}
                  className={`${inputCls('confirmPassword')} pr-10`}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirm(!showConfirm)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 bg-transparent border-none p-0 cursor-pointer transition-opacity duration-150"
                  tabIndex={-1}
                  aria-label={showConfirm ? 'Hide password' : 'Show password'}
                >
                  {showConfirm ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              {fieldErrors.confirmPassword && <p className="text-caption text-error mt-1">{fieldErrors.confirmPassword}</p>}
            </div>

            {/* Address */}
            <div>
              <label className="block text-body font-medium text-neutral-700 mb-1.5">Address</label>
              <input
                type="text"
                placeholder="123 Main St, City"
                value={form.address}
                onChange={set('address')}
                onBlur={() => handleBlur('address')}
                disabled={loading}
                className={inputCls('address')}
              />
              {fieldErrors.address && <p className="text-caption text-error mt-1">{fieldErrors.address}</p>}
            </div>

            {/* Contact number */}
            <div>
              <label className="block text-body font-medium text-neutral-700 mb-1.5">Contact number</label>
              <input
                type="tel"
                placeholder="+63 9XX XXX XXXX"
                value={form.contactNumber}
                onChange={set('contactNumber')}
                onBlur={() => handleBlur('contactNumber')}
                disabled={loading}
                className={inputCls('contactNumber')}
              />
              {fieldErrors.contactNumber && <p className="text-caption text-error mt-1">{fieldErrors.contactNumber}</p>}
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 bg-primary hover:bg-primary-dark text-white text-body font-semibold uppercase tracking-wider rounded-btn py-2.5 transition-btn disabled:opacity-60 disabled:cursor-not-allowed mt-2"
            >
              {loading ? <Spinner /> : 'Create Account'}
            </button>
          </form>

          {/* Login link */}
          <p className="text-center text-body text-neutral-400 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-primary font-semibold hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

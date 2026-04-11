import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { loginUser } from '../api/auth';

/* ───── tiny SVG icons (inlined to avoid extra deps) ───── */
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
const GoogleIcon = () => (
  <svg className="h-5 w-5" viewBox="0 0 24 24">
    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4" />
    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18A11.96 11.96 0 001 12c0 1.94.46 3.77 1.18 5.42l3.66-2.84z" fill="#FBBC05" />
    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
  </svg>
);
const Spinner = () => (
  <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
  </svg>
);

/* ───── Wave decoration for the left panel bottom ───── */
const WaveDecoration = () => (
  <svg className="absolute bottom-0 left-0 w-full" viewBox="0 0 1440 320" preserveAspectRatio="none">
    <path fill="rgba(255,255,255,0.08)" d="M0,224L48,213.3C96,203,192,181,288,186.7C384,192,480,224,576,218.7C672,213,768,171,864,165.3C960,160,1056,192,1152,197.3C1248,203,1344,181,1392,170.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z" />
    <path fill="rgba(255,255,255,0.05)" d="M0,288L48,272C96,256,192,224,288,224C384,224,480,256,576,261.3C672,267,768,245,864,234.7C960,224,1056,224,1152,218.7C1248,213,1344,203,1392,197.3L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z" />
  </svg>
);

export default function LoginPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  /* ── client-side validation ── */
  const validate = () => {
    const errs = {};
    if (!email.trim()) errs.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(email)) errs.email = 'Enter a valid email address';
    if (!password) errs.password = 'Password is required';
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
    const errs = validate();
    if (Object.keys(errs).length) {
      setFieldErrors(errs);
      return;
    }
    setFieldErrors({});
    setLoading(true);
    try {
      const { data } = await loginUser(email, password);
      const user = data.data.user;
      localStorage.setItem('accessToken', data.data.accessToken);
      localStorage.setItem('user', JSON.stringify(user));
      
      if (user.role === 'STAFF' || user.role === 'ADMIN') {
        navigate('/staff/dashboard');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      const apiError = err.response?.data?.error;
      const code = apiError?.code || 'AUTH-001';
      const detail = apiError?.message || 'Invalid email or password.';
      setErrorMsg(`[${code}] ${detail}`);
    } finally {
      setLoading(false);
    }
  };

  /* ─── input class helper ─── */
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
      <div className="w-full md:w-1/2 flex items-center justify-center bg-white px-6 py-12 md:px-16">
        <div className="w-full max-w-[400px]">
          {/* Brand mark */}
          <p className="text-primary font-bold text-[24px] mb-6">WashQ</p>

          {/* Heading */}
          <h1 className="text-h1 text-neutral-900 mb-1">Welcome back</h1>
          <p className="text-body text-neutral-400 mb-8">Sign in to your account</p>

          {/* Error banner */}
          {errorMsg && (
            <div className="flex items-center gap-2 bg-[#FEE2E2] border-l-4 border-error rounded-btn px-4 py-3 mb-6 animate-[slideDown_200ms_ease-out]">
              <XCircleIcon />
              <span className="text-body text-error">{errorMsg}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate className="space-y-5">
            {/* Email */}
            <div>
              <label className="block text-body font-medium text-neutral-700 mb-1.5">Email</label>
              <input
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onBlur={() => handleBlur('email')}
                disabled={loading}
                className={inputCls('email')}
              />
              {fieldErrors.email && (
                <p className="text-caption text-error mt-1">{fieldErrors.email}</p>
              )}
            </div>

            {/* Password */}
            <div>
              <label className="block text-body font-medium text-neutral-700 mb-1.5">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
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
              {fieldErrors.password && (
                <p className="text-caption text-error mt-1">{fieldErrors.password}</p>
              )}
              <div className="text-right mt-1.5">
                <Link to="/forgot-password" className="text-caption text-primary hover:underline">
                  Forgot password?
                </Link>
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 bg-primary hover:bg-primary-dark text-white text-body font-semibold uppercase tracking-wider rounded-btn py-2.5 transition-btn disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? <Spinner /> : 'Sign In'}
            </button>
          </form>

          {/* Divider */}
          <div className="flex items-center gap-3 my-6">
            <div className="flex-1 h-px bg-neutral-100" />
            <span className="text-caption text-neutral-400">or continue with</span>
            <div className="flex-1 h-px bg-neutral-100" />
          </div>

          {/* Google OAuth */}
          <button
            type="button"
            className="w-full flex items-center justify-center gap-2 border border-[#CBD5E1] rounded-btn py-2.5 text-body font-medium text-neutral-700 bg-white hover:bg-neutral-50 transition-btn"
          >
            <GoogleIcon />
            Continue with Google
          </button>

          {/* Register link */}
          <p className="text-center text-body text-neutral-400 mt-6">
            Don&apos;t have an account?{' '}
            <Link to="/register" className="text-primary font-semibold hover:underline">
              Register
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

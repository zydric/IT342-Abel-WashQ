import { useNavigate } from 'react-router-dom';

export default function DashboardPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Top navbar */}
      <nav className="h-16 bg-primary-dark flex items-center justify-between px-6 sticky top-0 z-navbar">
        <span className="text-white font-bold text-[20px] tracking-tight">WashQ</span>
        <button
          onClick={handleLogout}
          className="text-white/80 hover:text-white text-body font-medium transition-colors duration-150"
        >
          Sign out
        </button>
      </nav>

      {/* Placeholder content */}
      <main className="max-w-content mx-auto px-6 py-12">
        <div className="bg-white rounded-card shadow-card p-8">
          <h1 className="text-h1 text-neutral-900 mb-2">
            Welcome{user.firstName ? `, ${user.firstName}` : ''}!
          </h1>
          <p className="text-body text-neutral-400 mb-6">
            Your dashboard is under construction. More features are coming soon.
          </p>
          <div className="grid gap-4 sm:grid-cols-3">
            {['My Reservations', 'Laundry Status', 'Account Settings'].map((label) => (
              <div
                key={label}
                className="rounded-card border border-neutral-100 bg-neutral-50 p-6 text-center"
              >
                <p className="text-h3 text-neutral-700">{label}</p>
                <p className="text-caption text-neutral-400 mt-1">Coming soon</p>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}

import { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('user') || '{}'));
  const [dropdownOpen, setDropdownOpen] = useState(false);

  // Sync user from local storage in case it gets updated (e.g., from ProfilePage)
  useEffect(() => {
    const handleStorageChange = () => {
      setUser(JSON.parse(localStorage.getItem('user') || '{}'));
    };
    window.addEventListener('storage', handleStorageChange);
    
    // Also set up a custom event for same-window updates
    window.addEventListener('userUpdated', handleStorageChange);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('userUpdated', handleStorageChange);
    };
  }, []);

  const initials = [user.firstName?.[0], user.lastName?.[0]]
    .filter(Boolean)
    .join('')
    .toUpperCase() || 'U';

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const navLinks = [
    { label: 'Dashboard', to: '/dashboard' },
    { label: 'Services', to: '/services' },
    { label: 'My Bookings', to: '/orders' },
  ];

  return (
    <nav className="h-16 bg-primary-dark sticky top-0 z-navbar shadow-elevated">
      <div className="max-w-content mx-auto h-full flex items-center justify-between px-6">
        {/* Logo */}
        <Link
          to="/dashboard"
          className="text-white font-bold text-[20px] tracking-tight select-none hover:opacity-90 transition-opacity"
        >
          WashQ
        </Link>

        {/* Nav links */}
        <div className="hidden md:flex items-center gap-8">
          {navLinks.map(({ label, to }) => {
            const active = location.pathname === to;
            return (
              <Link
                key={to}
                to={to}
                className={`text-body font-semibold transition-colors duration-150 relative group ${
                  active ? 'text-white' : 'text-white/70 hover:text-white'
                }`}
              >
                {label}
                <span
                  className={`absolute -bottom-0.5 left-0 h-0.5 bg-white transition-all duration-200 ${
                    active ? 'w-full' : 'w-0 group-hover:w-full bg-white/60'
                  }`}
                />
              </Link>
            );
          })}
        </div>

        {/* Avatar dropdown */}
        <div className="relative">
          <button
            id="avatar-menu-btn"
            onClick={() => setDropdownOpen((o) => !o)}
            className="w-9 h-9 rounded-full bg-primary flex items-center justify-center text-white text-caption font-bold hover:ring-2 hover:ring-white/40 transition-all overflow-hidden"
            aria-expanded={dropdownOpen}
            aria-haspopup="true"
          >
            {user.profilePictureUrl ? (
               <img src={user.profilePictureUrl} alt="Avatar" className="w-full h-full object-cover bg-white" />
            ) : (
               initials
            )}
          </button>
          {dropdownOpen && (
            <>
              <div className="fixed inset-0 z-0" onClick={() => setDropdownOpen(false)} />
              <div className="absolute right-0 top-11 w-48 bg-white rounded-card shadow-elevated z-10 py-1 overflow-hidden">
                <Link
                  to="/profile"
                  onClick={() => setDropdownOpen(false)}
                  className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors"
                >
                  My Profile
                </Link>
                <Link
                  to="/orders"
                  onClick={() => setDropdownOpen(false)}
                  className="block px-4 py-2.5 text-body text-neutral-700 hover:bg-neutral-50 transition-colors"
                >
                  My Bookings
                </Link>
                <hr className="border-neutral-100 my-1" />
                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2.5 text-body text-error hover:bg-neutral-50 transition-colors font-medium"
                >
                  Sign Out
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

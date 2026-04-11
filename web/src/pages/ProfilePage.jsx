import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AvatarUpload from '../components/AvatarUpload';
import Navbar from '../components/Navbar';
import { updateProfile, getMe } from '../api/userApi';

export default function ProfilePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  
  // Form fields
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    address: '',
    contactNumber: ''
  });

  const [saving, setSaving] = useState(false);
  const [isChanged, setIsChanged] = useState(false);
  
  // UI states
  const [toastMsg, setToastMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    // Load auth user from local storage initially
    const storedUser = JSON.parse(localStorage.getItem('user') || 'null');
    if (!storedUser) {
      navigate('/login');
      return;
    }
    setUser(storedUser);
    setFormData({
      firstName: storedUser.firstName || '',
      lastName: storedUser.lastName || '',
      address: storedUser.address || '',
      contactNumber: storedUser.contactNumber || ''
    });

    // Fetch fresh details from backend
    getMe().then(res => {
      if (res.data && res.data.success) {
        const freshUser = res.data.data;
        setUser(freshUser);
        setFormData({
          firstName: freshUser.firstName || '',
          lastName: freshUser.lastName || '',
          address: freshUser.address || '',
          contactNumber: freshUser.contactNumber || ''
        });
        localStorage.setItem('user', JSON.stringify(freshUser));
        window.dispatchEvent(new Event('userUpdated'));
      }
    }).catch(err => console.error("Error fetching fresh user details", err));
  }, [navigate]);

  // Check if form changed
  useEffect(() => {
    if (!user) return;
    const changed = 
      formData.firstName !== (user.firstName || '') ||
      formData.lastName !== (user.lastName || '') ||
      formData.address !== (user.address || '') ||
      formData.contactNumber !== (user.contactNumber || '');
    setIsChanged(changed);
  }, [formData, user]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleAvatarUpdated = (newUrl) => {
    // Update local user state and localStorage immediately
    const updatedUser = { ...user, profilePictureUrl: newUrl };
    setUser(updatedUser);
    localStorage.setItem('user', JSON.stringify(updatedUser));
    window.dispatchEvent(new Event('userUpdated'));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setErrorMsg('');
    setToastMsg('');

    try {
      const res = await updateProfile(user.id, formData);
      if (res.data && res.data.success) {
        // Update user storage
        const updatedUser = { ...user, ...res.data.data };
        setUser(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
        window.dispatchEvent(new Event('userUpdated'));
        setIsChanged(false);

        // Show success toast
        setToastMsg('Profile updated successfully.');
        setTimeout(() => setToastMsg(''), 3000);
      } else {
        setErrorMsg('Failed to update profile.');
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.error?.message || 'Server error occurred.');
    } finally {
      setSaving(false);
    }
  };

  if (!user) return null;

  return (
    <div className="min-h-screen bg-neutral-50 pb-12">
      <Navbar />

      <main className="max-w-[960px] mx-auto px-6 pt-10">
        
        <div className="mb-8">
          <h1 className="text-h1 text-slate-900 tracking-tight">My Profile</h1>
          <div className="text-caption text-slate-500 mt-1 flex items-center gap-1.5">
            <Link to="/dashboard" className="hover:text-primary transition-colors">Home</Link>
            <span>&gt;</span>
            <span className="text-slate-700 font-medium">Profile</span>
          </div>
        </div>

        {/* Setup two-panel layout */}
        <div className="bg-white rounded-[16px] shadow-elevated flex flex-col md:flex-row overflow-hidden">
          
          {/* Avatar Panel - 30% */}
          <div className="w-full md:w-[30%] min-h-[300px] border-b md:border-b-0 md:border-r border-slate-200">
            <AvatarUpload user={user} onAvatarUpdated={handleAvatarUpdated} />
          </div>

          {/* Form Panel - 70% */}
          <div className="w-full md:w-[70%] p-8">

            {/* Error Banner */}
            {errorMsg && (
              <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-btn border border-red-100 text-body">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="flex flex-col gap-8">
              
              {/* Personal Information */}
              <section>
                <h3 className="text-h3 text-slate-900 mb-6">Personal Information</h3>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
                  <div className="flex flex-col gap-1.5">
                    <label className="text-caption font-semibold text-slate-700">First Name</label>
                    <input 
                      type="text" 
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleChange}
                      disabled={saving}
                      className="transition-all w-full bg-white border border-slate-300 rounded-[8px] px-3.5 py-2.5 text-body text-slate-900 outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary disabled:bg-slate-50 disabled:text-slate-400"
                      required
                    />
                  </div>
                  <div className="flex flex-col gap-1.5">
                    <label className="text-caption font-semibold text-slate-700">Last Name</label>
                    <input 
                      type="text" 
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleChange}
                      disabled={saving}
                      className="transition-all w-full bg-white border border-slate-300 rounded-[8px] px-3.5 py-2.5 text-body text-slate-900 outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary disabled:bg-slate-50 disabled:text-slate-400"
                      required
                    />
                  </div>
                </div>

                <div className="flex flex-col gap-1.5 mb-5">
                  <label className="text-caption font-semibold text-slate-700">Email</label>
                  <div className="relative group">
                    <input 
                      type="email" 
                      value={user.email}
                      disabled
                      className="w-full bg-slate-50 border border-slate-200 rounded-[8px] pl-3.5 pr-10 py-2.5 text-body text-slate-500 outline-none cursor-not-allowed"
                    />
                    <div className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400" title="Email cannot be changed">
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                      </svg>
                    </div>
                  </div>
                </div>

                <div className="flex flex-col gap-1.5 mb-5">
                  <label className="text-caption font-semibold text-slate-700">Address</label>
                  <textarea 
                    name="address"
                    rows="3"
                    value={formData.address}
                    onChange={handleChange}
                    disabled={saving}
                    className="transition-all w-full bg-white border border-slate-300 rounded-[8px] px-3.5 py-2.5 text-body text-slate-900 outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary resize-none disabled:bg-slate-50 disabled:text-slate-400"
                  />
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-caption font-semibold text-slate-700">Contact Number</label>
                  <input 
                    type="text" 
                    name="contactNumber"
                    value={formData.contactNumber}
                    onChange={handleChange}
                    disabled={saving}
                    className="transition-all w-full bg-white border border-slate-300 rounded-[8px] px-3.5 py-2.5 text-body text-slate-900 outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary disabled:bg-slate-50 disabled:text-slate-400"
                  />
                </div>
              </section>

              <hr className="border-slate-200" />

              {/* Account Details */}
              <section>
                <h3 className="text-h3 text-slate-900 mb-6">Account Details</h3>
                
                <div className="flex flex-col gap-4">
                  <div className="flex items-center gap-3">
                    <span className="bg-primary-light text-primary px-3 py-1 rounded-full text-caption font-bold tracking-wide uppercase">
                      {user.role}
                    </span>
                    {user.oauthProvider === 'GOOGLE' && (
                      <span className="text-caption text-slate-500 font-medium flex items-center gap-1.5">
                        <svg className="w-4 h-4" viewBox="0 0 24 24">
                          <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                          <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                          <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                          <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                        </svg>
                        Linked with Google
                      </span>
                    )}
                  </div>
                  
                  <div>
                    <button 
                      type="button" 
                      onClick={() => alert("Change password modal not yet implemented")}
                      className="text-primary font-semibold hover:underline text-body bg-transparent"
                    >
                      Change Password
                    </button>
                  </div>
                </div>
              </section>

              {/* Form Action */}
              <div className="flex justify-end pt-4 mt-2">
                <button
                  type="submit"
                  disabled={!isChanged || saving}
                  className="transition-btn bg-primary text-white font-semibold text-body uppercase tracking-wide px-8 py-3 rounded-btn hover:bg-primary-dark active:scale-[0.97] disabled:opacity-50 disabled:pointer-events-none flex items-center gap-2"
                >
                  {saving ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      Saving...
                    </>
                  ) : (
                    'SAVE CHANGES'
                  )}
                </button>
              </div>

            </form>
          </div>
        </div>
      </main>

      {/* Toast Notification */}
      {toastMsg && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#DCFCE7] text-[#166534] px-6 py-3 rounded-card shadow-card flex items-center gap-3 animate-in slide-in-from-bottom-5">
           <svg className="w-5 h-5 text-[#16A34A]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
           </svg>
           <span className="font-medium text-body">{toastMsg}</span>
        </div>
      )}
    </div>
  );
}

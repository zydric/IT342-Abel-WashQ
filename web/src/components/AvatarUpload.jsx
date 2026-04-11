import React, { useRef, useState } from 'react';
import { uploadAvatar } from '../api/userApi';

export default function AvatarUpload({ user, onAvatarUpdated }) {
  const fileInputRef = useRef(null);
  const [uploading, setUploading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const initials = [user?.firstName?.[0], user?.lastName?.[0]]
    .filter(Boolean)
    .join('')
    .toUpperCase() || 'U';

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setErrorMsg('');

    // Validate type
    if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
      setErrorMsg('File must be JPEG or PNG under 2 MB.');
      return;
    }

    // Validate size (2MB)
    if (file.size > 2 * 1024 * 1024) {
      setErrorMsg('File must be JPEG or PNG under 2 MB.');
      return;
    }

    setUploading(true);
    try {
      const response = await uploadAvatar(user.id, file);
      if (response.data && response.data.success) {
        onAvatarUpdated(response.data.data.profilePictureUrl);
      } else {
        // Axios wraps API response if it failed properly, otherwise catch block handles it
        setErrorMsg('Failed to upload profile picture.');
      }
    } catch (err) {
      setErrorMsg(
        err.response?.data?.error?.message || 'File must be JPEG or PNG under 2 MB.'
      );
    } finally {
      setUploading(false);
      // Reset input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const hasPhoto = !!user?.profilePictureUrl;

  return (
    <div className="flex flex-col items-center justify-center p-6 bg-slate-50 rounded-l-[16px] h-full relative">
      <div className="relative mb-6">
        {uploading && (
          <div className="absolute inset-0 z-10 flex items-center justify-center bg-white/70 rounded-full">
             <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
          </div>
        )}
        
        {hasPhoto ? (
          <img
            src={user.profilePictureUrl}
            alt="Profile Avatar"
            className="w-[120px] h-[120px] rounded-full object-cover shadow-sm bg-white"
          />
        ) : (
          <div className="w-[120px] h-[120px] rounded-full bg-[#1D4ED8] text-white flex flex-col items-center justify-center shadow-sm">
             <span className="text-[48px] font-medium leading-none">{initials}</span>
          </div>
        )}
        
        {/* Upload progress indicator ring could go here. We'll simulate with an SVG overlay or border */}
        {uploading && (
          <svg className="absolute top-0 left-0 w-full h-full transform -rotate-90 pointer-events-none" viewBox="0 0 120 120">
             <circle cx="60" cy="60" r="58" fill="none" stroke="#E2E8F0" strokeWidth="4" />
             <circle cx="60" cy="60" r="58" fill="none" stroke="#1D4ED8" strokeWidth="4" 
                     strokeDasharray="364" strokeDashoffset="91" className="transition-all duration-300" />
          </svg>
        )}
      </div>

      <input
        type="file"
        accept="image/jpeg, image/png"
        ref={fileInputRef}
        onChange={handleFileChange}
        className="hidden"
      />

      <button
        type="button"
        disabled={uploading}
        onClick={() => fileInputRef.current?.click()}
        className="transition-btn bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 font-semibold text-caption uppercase tracking-wide px-4 py-2 rounded-[6px] active:scale-[0.97] mb-2 disabled:opacity-50"
      >
        {uploading ? 'Uploading...' : 'CHANGE PHOTO'}
      </button>

      <p className="text-caption text-slate-400 mb-2 font-medium">JPEG or PNG · Max 2 MB</p>

      {errorMsg && (
        <p className="text-caption text-red-600 mt-2 text-center max-w-[200px] leading-tight">
          {errorMsg}
        </p>
      )}

      {hasPhoto && !uploading && (
        <button
          type="button"
          className="text-caption text-[#DC2626] hover:underline mt-2"
          onClick={() => {
            // Note: Remove photo wasn't strictly requested to have a backend endpoint in the prompt,
            // but the UI design has the button. Emulating removal by clearing the URL locally.
            // A real removal could hit a DELETE avatar endpoint.
            onAvatarUpdated(null); 
          }}
        >
          Remove photo
        </button>
      )}
    </div>
  );
}

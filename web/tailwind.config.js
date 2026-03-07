/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      colors: {
        primary: {
          DEFAULT: '#1D4ED8',
          light: '#DBEAFE',
          dark: '#1E3A8A',
        },
        accent: '#0EA5E9',
        success: '#16A34A',
        warning: '#D97706',
        error: '#DC2626',
        neutral: {
          50: '#F8FAFC',
          100: '#F1F5F9',
          400: '#94A3B8',
          700: '#334155',
          900: '#0F172A',
        },
      },
      fontSize: {
        h1: ['32px', { lineHeight: '1.2', fontWeight: '700' }],
        h2: ['24px', { lineHeight: '1.3', fontWeight: '600' }],
        h3: ['18px', { lineHeight: '1.4', fontWeight: '600' }],
        body: ['14px', { lineHeight: '1.5', fontWeight: '400' }],
        caption: ['12px', { lineHeight: '1.5', fontWeight: '400' }],
      },
      borderRadius: {
        card: '16px',
        btn: '8px',
        badge: '9999px',
      },
      boxShadow: {
        card: '0 1px 3px rgba(0,0,0,0.1)',
        elevated: '0 4px 12px rgba(0,0,0,0.1)',
      },
      maxWidth: {
        content: '1280px',
      },
      zIndex: {
        sidebar: '40',
        navbar: '50',
        modal: '60',
        toast: '70',
      },
    },
  },
  plugins: [],
}
import React, { useState, useEffect } from 'react';
import { getCurrentWeather } from '../api/weatherApi';

export default function WeatherWidget() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [displayTemp, setDisplayTemp] = useState(0);

  useEffect(() => {
    let isMounted = true;
    const fetchWeather = async () => {
      try {
        const response = await getCurrentWeather();
        if (isMounted) {
          if (response.data && response.data.success) {
            setData(response.data.data);
          } else {
            setError(true);
          }
        }
      } catch (err) {
        if (isMounted) setError(true);
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchWeather();
    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (data?.temperature) {
      let current = 0;
      const target = Math.round(data.temperature);
      const duration = 300;
      const intervalMs = 20;
      const steps = Math.max(1, duration / intervalMs);
      const increment = target / steps;
      
      const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
          setDisplayTemp(target);
          clearInterval(timer);
        } else {
          setDisplayTemp(Math.round(current));
        }
      }, intervalMs);
      
      return () => clearInterval(timer);
    }
  }, [data]);

  if (error) {
    return null; // Fail silently
  }

  if (loading) {
    return (
      <div className="bg-white rounded-card shadow-card p-6 flex flex-col gap-4 animate-pulse">
        <div className="h-6 bg-neutral-200 rounded-full w-24"></div>
        <div className="flex items-center gap-4">
          <div className="h-10 bg-neutral-200 rounded-lg w-16"></div>
          <div className="h-8 bg-neutral-200 rounded-lg w-24"></div>
        </div>
        <div className="h-4 bg-neutral-200 rounded w-32 mt-2"></div>
      </div>
    );
  }

  // Determine advisory message based on weather condition or description
  const conditions = ((data?.condition || '') + ' ' + (data?.description || '')).toLowerCase();
  const isGoodWeather = conditions.includes('clear') || conditions.includes('sun');
  const advisory = isGoodWeather 
    ? "Great day to drop off your laundry!" 
    : "Consider scheduling in advance.";

  return (
    <div className="bg-white rounded-card shadow-card p-6 flex flex-col justify-between">
      {/* Top row: City pill & Icon */}
      <div className="flex justify-between items-start mb-4">
        <div className="bg-sky-50 text-sky-500 px-3 py-1 rounded-full text-[12px] font-semibold tracking-wide self-start">
          {data.city || 'Cebu City, PH'}
        </div>
        {data.iconUrl && (
          <img src={data.iconUrl} alt={data.condition} className="w-12 h-12 -mt-2 -mr-2 object-cover" />
        )}
      </div>

      <div className="flex flex-col gap-1">
        {/* Main Temp & Condition */}
        <div className="flex items-end gap-3">
          <span className="text-[36px] font-bold leading-none text-slate-900">
            {displayTemp}&deg;C
          </span>
          <span className="text-h3 text-slate-700 pb-1 capitalize">
            {data.description || data.condition}
          </span>
        </div>

        {/* Humidity Row */}
        <div className="flex items-center gap-1.5 mt-2">
          <svg className="w-3.5 h-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z" />
          </svg>
          <span className="text-caption text-slate-400">{data.humidity}% Humidity</span>
        </div>

        {/* Advisory */}
        <p className="text-[12px] text-slate-400 italic mt-3 pt-3 border-t border-slate-100">
          {advisory}
        </p>
      </div>
    </div>
  );
}

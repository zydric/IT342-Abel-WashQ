import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import ServiceFormModal from '../components/ServiceFormModal';
import { getServices, createService, updateService, deactivateService } from '../api/serviceApi';

const WasherIcon = () => (
  <svg className="w-8 h-8 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

const PlusIcon = () => (
  <svg className="w-5 h-5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M12 4v16m8-8H4" />
  </svg>
);

export default function AdminServicesPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedService, setSelectedService] = useState(null);
  const [search, setSearch] = useState('');

  useEffect(() => {
    if (user.role !== 'ADMIN') {
      navigate('/staff/dashboard');
      return;
    }
    fetchServices();
  }, []);

  const fetchServices = async () => {
    setLoading(true);
    try {
      const res = await getServices();
      const data = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      setServices(data);
    } catch (err) {
      showToast('Failed to load services', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const handleCreateOrUpdate = async (formData) => {
    try {
      if (selectedService) {
        await updateService(selectedService.id, formData);
        showToast('Service updated successfully');
      } else {
        await createService(formData);
        showToast('Service created successfully');
      }
      fetchServices();
    } catch (err) {
      throw err; // Let modal handle error display
    }
  };

  const handleToggleActive = async (service) => {
    const action = service.isActive ? 'deactivate' : 'activate';
    if (service.isActive && !window.confirm(`Are you sure you want to deactivate "${service.name}"? It will no longer be visible to customers.`)) {
      return;
    }

    try {
      await updateService(service.id, { ...service, isActive: !service.isActive });
      showToast(`Service ${action}d successfully`);
      fetchServices();
    } catch (err) {
      showToast(`Failed to ${action} service`, 'error');
    }
  };

  const filteredServices = useMemo(() => {
    return services.filter(s => 
      s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.description?.toLowerCase().includes(search.toLowerCase())
    );
  }, [services, search]);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="h-screen flex bg-[#F8FAFC] overflow-hidden">
      <Sidebar user={user} onLogout={handleLogout} />

      <main className="flex-1 w-full px-4 py-8 md:px-8 overflow-y-auto overflow-x-hidden">
        
        <header className="mb-8 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-neutral-900 tracking-tight">Service Management</h1>
            <p className="text-neutral-500 mt-1">Configure and manage your laundry service catalog</p>
          </div>
          <button 
            onClick={() => { setSelectedService(null); setIsModalOpen(true); }}
            className="flex items-center justify-center gap-2 bg-primary hover:bg-primary-dark text-white font-bold uppercase tracking-widest text-xs px-6 py-3.5 rounded-xl transition-all shadow-btn shadow-primary/20"
          >
            <PlusIcon />
            Add Service
          </button>
        </header>

        {/* Filters */}
        <div className="mb-8 max-w-md relative">
          <svg className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input 
            type="text" 
            placeholder="Search services..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-12 pr-4 py-3 bg-white border border-neutral-200 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none shadow-sm transition-all"
          />
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map(i => (
              <div key={i} className="bg-white rounded-[24px] p-8 shadow-card border border-neutral-100 animate-pulse">
                <div className="w-12 h-12 bg-neutral-100 rounded-xl mb-6" />
                <div className="h-6 bg-neutral-100 rounded w-3/4 mb-4" />
                <div className="h-4 bg-neutral-100 rounded w-full mb-2" />
                <div className="h-4 bg-neutral-100 rounded w-2/3 mb-8" />
                <div className="h-10 bg-neutral-50 rounded-lg" />
              </div>
            ))}
          </div>
        ) : filteredServices.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-24 bg-white rounded-[32px] border border-dashed border-neutral-300">
            <div className="w-20 h-20 bg-neutral-50 rounded-full flex items-center justify-center mb-6">
              <svg className="w-10 h-10 text-neutral-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-neutral-900 mb-2">No services found</h3>
            <p className="text-neutral-500 mb-8 max-w-xs text-center">Get started by creating your first laundry service for your customers.</p>
            <button 
              onClick={() => setIsModalOpen(true)}
              className="bg-primary text-white font-bold uppercase tracking-widest text-xs px-8 py-3 rounded-xl hover:bg-primary-dark transition-all"
            >
              Add Your First Service
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {filteredServices.map((service) => (
              <div 
                key={service.id} 
                className={`group bg-white rounded-[24px] p-8 shadow-card border border-neutral-100 relative transition-all duration-300 hover:shadow-elevated hover:-translate-y-1 ${
                  !service.isActive ? 'opacity-70 grayscale-[0.5]' : ''
                }`}
              >
                {/* Status Dot */}
                <div className={`absolute top-6 right-6 w-2.5 h-2.5 rounded-full ${service.isActive ? 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.6)]' : 'bg-neutral-300'}`} />
                
                <div className="w-14 h-14 bg-primary/5 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                  <WasherIcon />
                </div>
                
                <h3 className={`text-xl font-bold text-neutral-900 mb-2 ${!service.isActive ? 'line-through text-neutral-400' : ''}`}>
                  {service.name}
                </h3>
                
                <p className="text-neutral-500 text-sm leading-relaxed mb-6 line-clamp-2 min-h-[40px]">
                  {service.description || 'No description provided.'}
                </p>
                
                <div className="flex flex-col gap-1 mb-8">
                  <span className="text-2xl font-bold text-primary tracking-tight">
                    ₱{service.pricePerKg?.toFixed(2)} <span className="text-xs font-semibold text-neutral-400 uppercase">/ kg</span>
                  </span>
                  <span className="text-sm font-medium text-neutral-400 flex items-center gap-1.5 uppercase tracking-wide">
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    ~{service.estimatedDurationHours} hours turnaround
                  </span>
                </div>

                <div className="pt-6 border-t border-neutral-50 flex items-center gap-3">
                  <button 
                    onClick={() => { setSelectedService(service); setIsModalOpen(true); }}
                    className="flex-1 px-4 py-2.5 border border-neutral-200 rounded-xl text-xs font-bold uppercase tracking-widest text-neutral-600 hover:bg-neutral-50 transition-colors"
                  >
                    Edit
                  </button>
                  <button 
                    onClick={() => handleToggleActive(service)}
                    className={`flex-1 px-4 py-2.5 rounded-xl text-xs font-bold uppercase tracking-widest transition-all ${
                      service.isActive 
                        ? 'bg-red-50 text-red-600 hover:bg-red-100' 
                        : 'bg-green-50 text-green-600 hover:bg-green-100'
                    }`}
                  >
                    {service.isActive ? 'Deactivate' : 'Activate'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      <ServiceFormModal 
        isOpen={isModalOpen}
        onClose={() => { setIsModalOpen(false); setSelectedService(null); }}
        onSave={handleCreateOrUpdate}
        service={selectedService}
      />

      {/* Toast Notification */}
      {toast && (
        <div className="fixed bottom-8 right-8 z-[100] animate-in fade-in slide-in-from-bottom-5">
          <div className={`rounded-2xl px-6 py-4 shadow-elevated flex items-center gap-3 text-sm font-bold tracking-tight text-white ${
            toast.type === 'error' ? 'bg-red-600' : 'bg-neutral-900 border border-white/10'
          }`}>
            {toast.type === 'error' ? (
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5"><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
            ) : (
              <svg className="w-5 h-5 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3"><path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" /></svg>
            )}
            {toast.message}
          </div>
        </div>
      )}
    </div>
  );
}

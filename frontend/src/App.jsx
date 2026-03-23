import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  LayoutDashboard, Users, AlertTriangle, Archive, Gift,
  Settings, HelpCircle, Bell, Search, TrendingUp, TrendingDown,
  Bus, MapIcon, Sun, Moon, LogOut, QrCode, Plus, Eye, UserPlus, ShieldCheck
} from 'lucide-react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import RoutesAdmin from './pages/RoutesAdmin';

const API_BASE = 'http://localhost:8080';

axios.interceptors.request.use(config => {
  const saved = localStorage.getItem('ts_user');
  if (saved) {
    const user = JSON.parse(saved);
    if (user.token) {
      config.headers.Authorization = `Bearer ${user.token}`;
    }
  }
  return config;
});

axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('ts_user');
      window.location.reload();
    }
    return Promise.reject(err);
  }
);

// ─── Auth Context ────────────────────────────────────────────
const useAuth = () => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('ts_user');
    return saved ? JSON.parse(saved) : null;
  });

  const login = async (email, password) => {
    const res = await axios.post(`${API_BASE}/api/auth/login`, { email, password });
    const userData = res.data;
    setUser(userData);
    localStorage.setItem('ts_user', JSON.stringify(userData));
    return userData;
  };

  const registerPassenger = async (fullName, email, phoneNumber, password) => {
    const res = await axios.post(`${API_BASE}/api/auth/register/passenger`, {
      fullName, email, phoneNumber, password
    });
    const userData = res.data;
    setUser(userData);
    localStorage.setItem('ts_user', JSON.stringify(userData));
    return userData;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('ts_user');
  };

  return { user, login, registerPassenger, logout };
};

// ─── Login Page ──────────────────────────────────────────────
const LoginPage = ({ onLogin, onSwitchToRegister }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await onLogin(email, password);
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-gray-950 flex items-center justify-center p-4 transition-colors duration-300">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-600/30 mx-auto mb-4">
            <Bus className="text-white" size={32} />
          </div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Transit<span className="font-light text-slate-500 dark:text-slate-400">Shield</span></h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-2">Sign in to your account</p>
        </div>

        <div className="bg-white dark:bg-slate-800/60 backdrop-blur-xl border border-slate-200 dark:border-slate-700/50 rounded-2xl p-8 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">Email</label>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)} required
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 transition-all"
                placeholder="admin@transitshield.com" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">Password</label>
              <input type="password" value={password} onChange={e => setPassword(e.target.value)} required
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 transition-all"
                placeholder="••••••••" />
            </div>

            {error && <div className="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-500/30 text-rose-700 dark:text-rose-400 text-sm rounded-xl px-4 py-3">{error}</div>}

            <button type="submit" disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl shadow-lg shadow-blue-600/30 transition-all disabled:opacity-50">
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <button onClick={onSwitchToRegister} className="text-sm text-blue-500 hover:text-blue-600 font-medium">
              New passenger? <span className="underline">Create an account</span>
            </button>
          </div>

          <div className="mt-6 pt-6 border-t border-slate-200 dark:border-slate-700/50">
            <p className="text-xs text-slate-400 dark:text-slate-500 text-center mb-3">Demo Accounts</p>
            <div className="grid grid-cols-1 gap-2 text-xs">
              <div className="bg-slate-50 dark:bg-slate-900/50 rounded-lg px-3 py-2 flex justify-between items-center">
                <span className="text-slate-600 dark:text-slate-400"><ShieldCheck size={12} className="inline mr-1" />Admin</span>
                <span className="text-slate-500 dark:text-slate-500 font-mono">admin@transitshield.com / admin123</span>
              </div>
              <div className="bg-slate-50 dark:bg-slate-900/50 rounded-lg px-3 py-2 flex justify-between items-center">
                <span className="text-slate-600 dark:text-slate-400"><Users size={12} className="inline mr-1" />Passenger</span>
                <span className="text-slate-500 dark:text-slate-500 font-mono">passenger@demo.com / password</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// ─── Register Page ───────────────────────────────────────────
const RegisterPage = ({ onRegister, onSwitchToLogin }) => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await onRegister(fullName, email, phone, password);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-gray-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-600/30 mx-auto mb-4">
            <Bus className="text-white" size={32} />
          </div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Passenger <span className="font-light text-slate-500">Registration</span></h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-2">Create your TransitShield account</p>
          <p className="text-xs text-amber-600 dark:text-amber-400 mt-1">Note: Driver accounts are created by admin only</p>
        </div>

        <div className="bg-white dark:bg-slate-800/60 backdrop-blur-xl border border-slate-200 dark:border-slate-700/50 rounded-2xl p-8 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div><label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Full Name</label>
              <input type="text" value={fullName} onChange={e => setFullName(e.target.value)} required className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" /></div>
            <div><label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Email</label>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)} required className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" /></div>
            <div><label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Phone</label>
              <input type="text" value={phone} onChange={e => setPhone(e.target.value)} className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" /></div>
            <div><label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Password</label>
              <input type="password" value={password} onChange={e => setPassword(e.target.value)} required className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" /></div>
            {error && <div className="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-500/30 text-rose-700 dark:text-rose-400 text-sm rounded-xl px-4 py-3">{error}</div>}
            <button type="submit" disabled={loading} className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl shadow-lg shadow-blue-600/30 transition-all disabled:opacity-50">
              {loading ? 'Creating...' : 'Create Passenger Account'}
            </button>
          </form>
          <div className="mt-6 text-center">
            <button onClick={onSwitchToLogin} className="text-sm text-blue-500 hover:text-blue-600 font-medium">Already have an account? <span className="underline">Sign In</span></button>
          </div>
        </div>
      </div>
    </div>
  );
};

// ─── Components ──────────────────────────────────────────────
const SidebarItem = ({ icon: Icon, label, active, onClick }) => (
  <div onClick={onClick} className={`flex items-center space-x-3 px-4 py-3 rounded-full cursor-pointer transition-all duration-200 ${active ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/30' : 'text-slate-500 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-400 dark:hover:text-white dark:hover:bg-slate-800/50'}`}>
    <Icon size={20} className={active ? 'text-white' : 'text-slate-500 dark:text-slate-400'} />
    <span className="font-medium text-sm">{label}</span>
  </div>
);

const StatCard = ({ title, value, trend, isPositive, icon: Icon, colorClass }) => (
  <div className="bg-white border-slate-200 shadow-sm dark:bg-slate-800/40 dark:backdrop-blur-xl border dark:border-slate-700/50 rounded-2xl p-6 flex flex-col justify-between transition-transform hover:-translate-y-1 hover:shadow-xl duration-300">
    <div className="flex justify-between items-start">
      <div>
        <h3 className="text-slate-500 dark:text-slate-400 text-sm font-medium mb-1">{title}</h3>
        <p className="text-3xl font-bold text-slate-800 dark:text-white tracking-tight">{value}</p>
      </div>
      <div className={`p-3 rounded-full ${colorClass} bg-opacity-10 dark:bg-opacity-20`}>
        <Icon size={22} className={colorClass.replace('bg-', 'text-').replace('text-opacity-20', '').replace('text-opacity-10', '')} />
      </div>
    </div>
    <div className="mt-4 flex items-center space-x-2">
      <span className={`flex items-center text-xs font-semibold px-2 py-1 rounded-full ${isPositive ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400'}`}>
        {isPositive ? <TrendingUp size={12} className="mr-1" /> : <TrendingDown size={12} className="mr-1" />}
        {trend}
      </span>
      <span className="text-slate-400 dark:text-slate-500 text-xs">vs last week</span>
    </div>
  </div>
);

const StatusDot = ({ status }) => {
  const colors = {
    'Moving': 'bg-blue-500 shadow-blue-500/50',
    'Stopped': 'bg-emerald-500 shadow-emerald-500/50',
    'Delayed': 'bg-amber-500 shadow-amber-500/50',
    'Offline': 'bg-slate-400 dark:bg-slate-500 shadow-slate-400/50 dark:shadow-slate-500/50'
  };
  return <div className={`w-3 h-3 rounded-full shadow-lg ${colors[status] || colors.Offline}`}></div>;
};

const getMapIcon = (status) => {
  const colors = {
    'Moving': 'bg-blue-500 shadow-blue-500/50',
    'Stopped': 'bg-emerald-500 shadow-emerald-500/50',
    'Delayed': 'bg-amber-500 shadow-amber-500/50',
    'Offline': 'bg-slate-400 shadow-slate-400/50'
  };
  const colorClass = colors[status] || colors.Offline;
  return L.divIcon({
    className: 'custom-bus-marker',
    html: `<div class="w-4 h-4 rounded-full border-2 border-white shadow-lg ${colorClass} animate-pulse"></div>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8]
  });
};

const MapBoundsUpdater = ({ markers }) => {
  const map = useMap();
  useEffect(() => {
    if (markers && markers.length > 0) {
      const bounds = L.latLngBounds(markers.map(m => [m.lat, m.lng]));
      map.flyToBounds(bounds, { padding: [50, 50], maxZoom: 15, duration: 1.5 });
    }
  }, [markers, map]);
  return null;
};

// ─── Drivers Module ──────────────────────────────────────────
const DriversModule = () => {
  const [drivers, setDrivers] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ fullName: '', email: '', phoneNumber: '', password: '' });
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState('');

  const fetchDrivers = async () => {
    try {
      const res = await axios.get(`${API_BASE}/api/users?role=DRIVER`);
      setDrivers(res.data);
    } catch { setDrivers([]); }
    setLoading(false);
  };
  useEffect(() => { fetchDrivers(); }, []);

  const createDriver = async (e) => {
    e.preventDefault();
    setMsg('');
    try {
      await axios.post(`${API_BASE}/api/admin/drivers`, form);
      setMsg('Driver created successfully');
      setForm({ fullName: '', email: '', phoneNumber: '', password: '' });
      setShowForm(false);
      fetchDrivers();
    } catch (err) {
      setMsg(err.response?.data?.message || 'Failed to create driver');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-800 dark:text-white">Driver Management</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">Admin-only: create and manage driver accounts</p>
        </div>
        <button onClick={() => setShowForm(!showForm)} className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors shadow-lg shadow-blue-600/20">
          <UserPlus size={16} /><span>{showForm ? 'Cancel' : 'Add Driver'}</span>
        </button>
      </div>

      {msg && <div className={`rounded-xl px-4 py-3 text-sm border ${msg.includes('success') ? 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-500/30' : 'bg-rose-50 dark:bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-200 dark:border-rose-500/30'}`}>{msg}</div>}

      {showForm && (
        <div className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-6">
          <h3 className="font-semibold text-slate-800 dark:text-white mb-4">Create Driver Account</h3>
          <form onSubmit={createDriver} className="grid grid-cols-2 gap-4">
            <input value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} placeholder="Full Name" required className="px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" />
            <input value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="Email" type="email" required className="px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" />
            <input value={form.phoneNumber} onChange={e => setForm({ ...form, phoneNumber: e.target.value })} placeholder="Phone Number" className="px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" />
            <input value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} placeholder="Password" type="password" required className="px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all" />
            <button type="submit" className="col-span-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl transition-colors">Create Driver</button>
          </form>
        </div>
      )}

      <div className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl overflow-hidden">
        {loading ? <div className="p-8 text-center text-slate-400">Loading...</div> :
        drivers.length === 0 ? <div className="p-8 text-center text-slate-400 dark:text-slate-500">No drivers registered. Use "Add Driver" to create one.</div> : (
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/50 text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4">Name</th><th className="px-6 py-4">Email</th><th className="px-6 py-4">Phone</th><th className="px-6 py-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
              {drivers.map(d => (
                <tr key={d.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-4 font-medium text-slate-800 dark:text-white">{d.fullName}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{d.email}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{d.phoneNumber || '-'}</td>
                  <td className="px-6 py-4"><span className={`px-2 py-1 rounded-full text-xs font-semibold ${d.isActive ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-rose-100 text-rose-700'}`}>{d.isActive ? 'Active' : 'Inactive'}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

// ─── Bus Management with QR ──────────────────────────────────
const BusManagement = () => {
  const emptyBus = { busCode: '', registrationNumber: '', busDisplayName: '', capacity: 50, operatorName: '', status: 'ACTIVE' };
  const [buses, setBuses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [qrMap, setQrMap] = useState({});
  const [genMsg, setGenMsg] = useState('');
  const [form, setForm] = useState(emptyBus);
  const [editingId, setEditingId] = useState(null);

  const fetchBuses = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API_BASE}/api/buses`);
      setBuses(res.data);
      const qrs = {};
      for (const bus of res.data) {
        try {
          const qrRes = await axios.get(`${API_BASE}/api/buses/${bus.id}/active-qr`);
          if (qrRes.data?.qrToken) qrs[bus.id] = qrRes.data;
        } catch { }
      }
      setQrMap(qrs);
    } catch { setBuses([]); }
    setLoading(false);
  };

  useEffect(() => { fetchBuses(); }, []);

  const generateQr = async (busId) => {
    setGenMsg('');
    try {
      const res = await axios.post(`${API_BASE}/api/admin/buses/${busId}/generate-qr`);
      setQrMap(prev => ({ ...prev, [busId]: res.data }));
      setGenMsg(`QR generated for bus ${busId}`);
    } catch (err) { setGenMsg(err.response?.data?.message || 'Failed to generate QR'); }
  };

  const saveBus = async (e) => {
    e.preventDefault();
    try {
      if (editingId) await axios.put(`${API_BASE}/api/buses/${editingId}`, form);
      else await axios.post(`${API_BASE}/api/buses`, form);
      setForm(emptyBus); setEditingId(null); fetchBuses();
    } catch (err) { setGenMsg(err.response?.data?.message || 'Failed to save bus'); }
  };

  const editBus = (bus) => {
    setEditingId(bus.id);
    setForm({
      busCode: bus.busCode || '',
      registrationNumber: bus.registrationNumber || '',
      busDisplayName: bus.busDisplayName || '',
      capacity: bus.capacity || 50,
      operatorName: bus.operatorName || '',
      status: bus.status || 'ACTIVE'
    });
  };

  const deleteBus = async (id) => {
    if (!window.confirm('Delete this bus?')) return;
    try { await axios.delete(`${API_BASE}/api/buses/${id}`); fetchBuses(); }
    catch (err) { setGenMsg(err.response?.data?.message || 'Failed to delete bus'); }
  };

  return (<div className="space-y-6">
    <div>
      <h2 className="text-xl font-bold text-slate-800 dark:text-white flex items-center"><Bus className="mr-2 text-blue-500" size={22} />Bus Fleet & QR Management</h2>
      <p className="text-sm text-slate-500 dark:text-slate-400">Complete bus CRUD + admin QR generation</p>
    </div>

    <form onSubmit={saveBus} className="grid grid-cols-1 md:grid-cols-6 gap-3 bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 p-4 rounded-xl">
      <input value={form.busCode} onChange={e => setForm({ ...form, busCode: e.target.value })} placeholder="Bus Code" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required />
      <input value={form.registrationNumber} onChange={e => setForm({ ...form, registrationNumber: e.target.value })} placeholder="Registration" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required />
      <input value={form.busDisplayName} onChange={e => setForm({ ...form, busDisplayName: e.target.value })} placeholder="Display Name" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required />
      <input type="number" value={form.capacity} onChange={e => setForm({ ...form, capacity: Number(e.target.value) })} placeholder="Capacity" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required />
      <input value={form.operatorName} onChange={e => setForm({ ...form, operatorName: e.target.value })} placeholder="Operator" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required />
      <button className="bg-blue-600 text-white rounded-lg px-4 py-2">{editingId ? 'Update Bus' : 'Create Bus'}</button>
    </form>

    {genMsg && <div className="bg-blue-50 dark:bg-blue-500/10 border border-blue-200 dark:border-blue-500/30 text-blue-700 dark:text-blue-400 text-sm rounded-xl px-4 py-3">{genMsg}</div>}

    {loading ? <div className="text-center text-slate-400 py-8">Loading...</div> : (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {buses.map(bus => (
          <div key={bus.id} className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-6">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h3 className="font-bold text-slate-800 dark:text-white">{bus.busDisplayName}</h3>
                <p className="text-sm text-slate-500 dark:text-slate-400">{bus.busCode} · {bus.registrationNumber}</p>
              </div>
              <span className="px-2 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-700 dark:bg-slate-700">{bus.status}</span>
            </div>
            <div className="text-sm text-slate-600 dark:text-slate-400 space-y-1 mb-4">
              <p>Capacity: <span className="font-medium">{bus.capacity}</span></p>
              <p>Operator: <span className="font-medium">{bus.operatorName}</span></p>
            </div>
            <div className="flex gap-2 mb-3">
              <button onClick={() => editBus(bus)} className="flex-1 bg-slate-200 dark:bg-slate-700 rounded-lg px-3 py-2 text-xs">Edit</button>
              <button onClick={() => deleteBus(bus.id)} className="flex-1 bg-rose-600 text-white rounded-lg px-3 py-2 text-xs">Delete</button>
            </div>
            {qrMap[bus.id] ? (
              <div className="bg-slate-50 dark:bg-slate-900/50 rounded-xl p-3">
                <p className="text-xs font-mono truncate">{qrMap[bus.id].qrToken}</p>
                <button onClick={() => generateQr(bus.id)} className="w-full mt-2 bg-amber-500 text-white text-xs font-semibold py-2 rounded-lg">Regenerate QR</button>
              </div>
            ) : (
              <button onClick={() => generateQr(bus.id)} className="w-full bg-blue-600 text-white text-xs font-semibold px-4 py-2 rounded-lg"><Plus size={12} className="inline mr-1" />Generate QR</button>
            )}
          </div>
        ))}
      </div>
    )}
  </div>);
};

const AssignmentManagement = () => {
  const [assignments, setAssignments] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [buses, setBuses] = useState([]);
  const [variants, setVariants] = useState([]);
  const [msg, setMsg] = useState('');
  const [form, setForm] = useState({ busId: '', driverProfileId: '', routeVariantId: '' });

  const fetchAll = async () => {
    try {
      const [a, d, b, v] = await Promise.all([
        axios.get(`${API_BASE}/api/admin/assignments`),
        axios.get(`${API_BASE}/api/admin/driver-profiles`),
        axios.get(`${API_BASE}/api/buses`),
        axios.get(`${API_BASE}/api/route-variants`)
      ]);
      setAssignments(a.data); setDrivers(d.data); setBuses(b.data); setVariants(v.data);
    } catch { setMsg('Failed to load assignment data'); }
  };
  useEffect(() => { fetchAll(); }, []);

  const createAssignment = async (e) => {
    e.preventDefault();
    setMsg('');
    try {
      await axios.post(`${API_BASE}/api/admin/assignments`, {
        busId: Number(form.busId),
        driverProfileId: Number(form.driverProfileId),
        routeVariantId: Number(form.routeVariantId)
      });
      setMsg('Driver-bus schedule assignment created');
      setForm({ busId: '', driverProfileId: '', routeVariantId: '' });
      fetchAll();
    } catch (err) { setMsg(err.response?.data?.message || 'Failed to create assignment'); }
  };

  return (<div className="space-y-6">
    <div>
      <h2 className="text-xl font-bold text-slate-800 dark:text-white">Driver-Bus Scheduling Assignments</h2>
      <p className="text-sm text-slate-500 dark:text-slate-400">Assign drivers + buses to route variants via backend assignment endpoint.</p>
    </div>

    <form onSubmit={createAssignment} className="grid grid-cols-1 md:grid-cols-4 gap-3 bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 p-4 rounded-xl">
      <select value={form.driverProfileId} onChange={e => setForm({ ...form, driverProfileId: e.target.value })} className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required>
        <option value="">Select Driver</option>{drivers.map(d => <option key={d.id} value={d.id}>{d.fullName || d.driverCode}</option>)}
      </select>
      <select value={form.busId} onChange={e => setForm({ ...form, busId: e.target.value })} className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required>
        <option value="">Select Bus</option>{buses.map(b => <option key={b.id} value={b.id}>{b.busCode} - {b.busDisplayName}</option>)}
      </select>
      <select value={form.routeVariantId} onChange={e => setForm({ ...form, routeVariantId: e.target.value })} className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" required>
        <option value="">Select Route Variant</option>{variants.map(v => <option key={v.id} value={v.id}>{v.variantCode} ({v.originName} → {v.destinationName})</option>)}
      </select>
      <button className="bg-blue-600 text-white rounded-lg px-4 py-2">Assign</button>
    </form>

    {msg && <div className="text-sm text-blue-600 dark:text-blue-400">{msg}</div>}
    <div className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-xl overflow-auto">
      <table className="w-full text-sm">
        <thead><tr className="text-left bg-slate-100 dark:bg-slate-900"><th className="p-3">Bus</th><th className="p-3">Driver</th><th className="p-3">Route</th><th className="p-3">Status</th></tr></thead>
        <tbody>{assignments.map(a => <tr key={a.id} className="border-t border-slate-200 dark:border-slate-700"><td className="p-3">{a.busCode} - {a.registrationNumber}</td><td className="p-3">{a.driverName || a.driverCode}</td><td className="p-3">{a.routeNumber} {a.originName} → {a.destinationName}</td><td className="p-3">{a.assignmentStatus}</td></tr>)}</tbody>
      </table>
    </div>
  </div>);
};

const LostFoundManagement = () => {
  const [reports, setReports] = useState([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [msg, setMsg] = useState('');

  const loadReports = async () => {
    try {
      const query = statusFilter !== 'ALL' ? `?status=${statusFilter}` : '';
      const res = await axios.get(`${API_BASE}/api/admin/lost-items${query}`);
      setReports(res.data || []);
    } catch {
      setReports([]);
      setMsg('Failed to load lost item reports');
    }
  };

  useEffect(() => { loadReports(); }, [statusFilter]);

  const updateStatus = async (id, status) => {
    const notes = window.prompt('Admin notes (optional):', '') || '';
    const resolutionNotes = status === 'CLOSED' ? (window.prompt('Resolution notes (optional):', '') || '') : '';
    try {
      await axios.put(`${API_BASE}/api/admin/lost-items/${id}/status`, { status, adminNotes: notes, resolutionNotes });
      setMsg(`Report #${id} updated to ${status}`);
      loadReports();
    } catch (err) {
      setMsg(err.response?.data?.message || 'Failed to update status');
    }
  };

  const filtered = reports.filter(r => {
    const q = search.toLowerCase();
    return !q || [r.itemTitle, r.description, r.routeInfo, r.busInfo, r.reporterName, r.status].some(v => (v || '').toLowerCase().includes(q));
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-slate-800 dark:text-white">Lost & Found Management</h2>
        <p className="text-sm text-slate-500 dark:text-slate-400">Admin-only: review, track, and resolve lost item reports.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search by item, route, bus, reporter..." className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" />
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900">
          {['ALL', 'REPORTED', 'UNDER_REVIEW', 'FOUND', 'CLAIMED', 'CLOSED'].map(s => <option key={s} value={s}>{s}</option>)}
        </select>
        <button onClick={loadReports} className="bg-blue-600 text-white rounded-lg px-4 py-2">Refresh</button>
      </div>

      {msg && <div className="text-sm text-blue-600 dark:text-blue-400">{msg}</div>}

      <div className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-xl overflow-auto">
        <table className="w-full text-sm">
          <thead><tr className="text-left bg-slate-100 dark:bg-slate-900"><th className="p-3">Item</th><th className="p-3">Reporter</th><th className="p-3">Route/Bus</th><th className="p-3">Status</th><th className="p-3">Notes</th><th className="p-3">Actions</th></tr></thead>
          <tbody>
            {filtered.map(r => (
              <tr key={r.id} className="border-t border-slate-200 dark:border-slate-700 align-top">
                <td className="p-3"><div className="font-semibold">{r.itemTitle}</div><div className="text-xs text-slate-500">{r.description}</div></td>
                <td className="p-3">{r.reporterName}<div className="text-xs text-slate-500">{r.reporterRole}</div></td>
                <td className="p-3">{r.routeInfo || '-'}<div className="text-xs text-slate-500">{r.busInfo || '-'}</div></td>
                <td className="p-3"><span className="px-2 py-1 rounded text-xs bg-slate-100 dark:bg-slate-700">{r.status}</span></td>
                <td className="p-3 text-xs">{r.adminNotes || '-'}{r.resolutionNotes ? <div className="mt-1">Resolved: {r.resolutionNotes}</div> : null}</td>
                <td className="p-3">
                  <div className="flex flex-wrap gap-1">
                    {['UNDER_REVIEW', 'FOUND', 'CLAIMED', 'CLOSED'].map(st => (
                      <button key={st} onClick={() => updateStatus(r.id, st)} className="px-2 py-1 text-xs rounded bg-blue-600 text-white">{st}</button>
                    ))}
                  </div>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && <tr><td className="p-4 text-slate-400" colSpan={6}>No lost item reports found.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// ─── Placeholder Module ──────────────────────────────────────
const PlaceholderModule = ({ tabName }) => {
  const iconMap = {
    'Complaints': AlertTriangle,
    'Lost & Found': Archive,
    'Rewards': Gift,
    'Settings': Settings,
    'Support': HelpCircle
  };
  const Icon = iconMap[tabName] || LayoutDashboard;
  return (
    <div className="h-full flex items-center justify-center p-8">
      <div className="bg-white dark:bg-slate-800/40 dark:backdrop-blur-xl border border-slate-200 dark:border-slate-700/50 rounded-2xl p-12 flex flex-col items-center justify-center max-w-lg w-full text-center shadow-sm">
        <div className="w-16 h-16 bg-blue-50 dark:bg-blue-500/10 rounded-full flex items-center justify-center mb-6">
          <Icon className="text-blue-500" size={32} />
        </div>
        <h2 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">{tabName}</h2>
        <p className="text-slate-500 dark:text-slate-400 text-sm">This module is planned for a future release and is not part of the current submission scope.</p>
      </div>
    </div>
  );
};

// ─── Passenger Dashboard ────────────────────────────────────
const PassengerView = ({ user, logout }) => (
  <div className="min-h-screen bg-slate-50 dark:bg-gray-950 flex items-center justify-center p-4">
    <div className="w-full max-w-lg bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-8 shadow-xl text-center">
      <div className="w-16 h-16 bg-blue-500 rounded-full flex items-center justify-center mx-auto mb-4">
        <Users className="text-white" size={28} />
      </div>
      <h2 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">Welcome, {user.fullName}!</h2>
      <p className="text-slate-500 dark:text-slate-400 text-sm mb-6">You are logged in as a <span className="font-semibold text-blue-500">Passenger</span>.</p>
      <p className="text-slate-500 dark:text-slate-400 text-sm mb-8">The full passenger experience is available on the <strong>TransitShield Android App</strong>. Use the mobile app to scan QR codes, track live buses, manage trips, and earn rewards.</p>
      <button onClick={logout} className="bg-slate-200 dark:bg-slate-700 hover:bg-slate-300 dark:hover:bg-slate-600 text-slate-700 dark:text-slate-200 font-medium px-6 py-3 rounded-xl transition-colors">
        <LogOut size={16} className="inline mr-2" />Sign Out
      </button>
    </div>
  </div>
);

// ─── Main App ────────────────────────────────────────────────
export default function App() {
  const { user, login, logout } = useAuth();
  const [data, setData] = useState({ stats: { active_buses: 0, total_complaints: 0, lost_items: 0, demerit_warnings: 0 }, recent_violations: [], map_markers: [] });
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('Overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [isDarkMode, setIsDarkMode] = useState(() => {
    if (typeof window !== "undefined") {
      const saved = localStorage.getItem("theme");
      if (saved) return saved === "dark";
      return true;
    }
    return true;
  });

  useEffect(() => {
    if (isDarkMode) { document.documentElement.classList.add('dark'); localStorage.setItem("theme", "dark"); }
    else { document.documentElement.classList.remove('dark'); localStorage.setItem("theme", "light"); }
  }, [isDarkMode]);

  useEffect(() => {
    if (user && user.role === 'ADMIN') {
      const fetchData = async () => {
        try {
          const response = await axios.get(`${API_BASE}/api/dashboard`);
          setData(response.data);
        } catch (error) {
          console.error("Error fetching data:", error);
        } finally { setLoading(false); }
      };
      fetchData();
    }
  }, [user]);

  // Not logged in -> admin login only
  if (!user) return <LoginPage onLogin={login} />;

  if (user.role !== 'ADMIN') {
    return (
      <div className="min-h-screen bg-slate-50 dark:bg-gray-950 flex items-center justify-center p-4">
        <div className="w-full max-w-lg bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-8 shadow-xl text-center">
          <ShieldCheck className="text-blue-500 mx-auto mb-4" size={40} />
          <h2 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">Admin-Only Web Dashboard</h2>
          <p className="text-slate-500 dark:text-slate-400 text-sm mb-8">This web app is restricted to ADMIN accounts. Please continue with the Android app for passenger/driver flows.</p>
          <button onClick={logout} className="bg-slate-200 dark:bg-slate-700 hover:bg-slate-300 dark:hover:bg-slate-600 text-slate-700 dark:text-slate-200 font-medium px-6 py-3 rounded-xl transition-colors"><LogOut size={16} className="inline mr-2" />Sign Out</button>
        </div>
      </div>
    );
  }

  // ─── ADMIN DASHBOARD ──────────────────────────────────────
  const filteredMarkers = data.map_markers.filter(m =>
    (m.route || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getViolationColor = (type) => {
    switch (type) {
      case 'Speeding': return 'text-rose-700 bg-rose-50 border-rose-200 dark:text-rose-400 dark:bg-rose-400/10 dark:border-rose-400/20';
      case 'Late Arrival': return 'text-amber-700 bg-amber-50 border-amber-200 dark:text-amber-400 dark:bg-amber-400/10 dark:border-amber-400/20';
      case 'Off Route': return 'text-purple-700 bg-purple-50 border-purple-200 dark:text-purple-400 dark:bg-purple-400/10 dark:border-purple-400/20';
      default: return 'text-slate-600 bg-slate-100 border-slate-200 dark:text-slate-300 dark:bg-slate-400/10 dark:border-slate-400/20';
    }
  };

  const renderContent = () => {
    if (activeTab === 'Overview') {
      if (loading) return <div className="flex items-center justify-center h-full"><div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div></div>;
      return (
        <div className="space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <StatCard title="Active Buses" value={data.stats.active_buses} trend="+12%" isPositive={true} icon={Bus} colorClass="bg-blue-500 text-blue-500" />
            <StatCard title="Total Complaints" value={data.stats.total_complaints} trend="-4%" isPositive={true} icon={AlertTriangle} colorClass="bg-rose-500 text-rose-500" />
            <StatCard title="Lost Items" value={data.stats.lost_items} trend="+2%" isPositive={false} icon={Archive} colorClass="bg-amber-500 text-amber-500" />
            <StatCard title="Demerit Warnings" value={data.stats.demerit_warnings} trend="-1%" isPositive={true} icon={Users} colorClass="bg-purple-500 text-purple-500" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 pb-8">
            <div className="lg:col-span-2 bg-white dark:bg-slate-800/40 dark:backdrop-blur-xl border border-slate-200 dark:border-slate-700/50 rounded-2xl p-6 flex flex-col shadow-sm relative overflow-hidden">
              <div className="flex items-center justify-between mb-6 relative z-10 w-full">
                <div>
                  <h2 className="text-xl font-bold text-slate-800 dark:text-white flex items-center"><MapIcon className="mr-2 text-blue-500" size={20} />Live Transit Map</h2>
                  <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Real-time bus tracking (Colombo Region)</p>
                </div>
                {searchQuery && <span className="text-sm bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-300 px-3 py-1.5 rounded-full border border-blue-200 dark:border-blue-500/30 flex items-center"><Search size={14} className="mr-1" />Filtering: {searchQuery}</span>}
              </div>
              <div className="relative flex-1 min-h-[450px] bg-slate-100 dark:bg-[#111822] rounded-xl overflow-hidden border border-slate-200 dark:border-slate-700/50 z-0 shadow-inner">
                <MapContainer center={[6.9271, 79.8612]} zoom={12} scrollWheelZoom={true} className="h-full w-full z-0">
                  <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" className={isDarkMode ? 'map-tiles-dark' : 'map-tiles-light'} />
                  <MapBoundsUpdater markers={filteredMarkers} />
                  {filteredMarkers.map(marker => (
                    <Marker key={marker.id} position={[marker.lat, marker.lng]} icon={getMapIcon(marker.status)}>
                      <Popup className={isDarkMode ? 'dark-popup' : ''}>
                        <div className="font-sans text-slate-800 dark:text-slate-200">
                          <div className="font-bold text-base border-b pb-1 mb-1.5 flex items-center justify-between">
                            <span>Bus #{marker.id}</span>
                            <span className={`text-[10px] uppercase font-black px-1.5 rounded-sm ${marker.status === 'Moving' ? 'bg-blue-100 text-blue-700' : marker.status === 'Stopped' ? 'bg-emerald-100 text-emerald-700' : marker.status === 'Delayed' ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-700'}`}>{marker.status}</span>
                          </div>
                          <div className="text-sm">Route: <span className="font-semibold">{marker.route || 'N/A'}</span></div>
                        </div>
                      </Popup>
                    </Marker>
                  ))}
                </MapContainer>
                <div className="absolute bottom-4 left-4 bg-white/90 dark:bg-gray-900/90 backdrop-blur-md border border-slate-200 dark:border-slate-700/80 p-4 rounded-xl shadow-xl flex flex-col space-y-3 pointer-events-none" style={{ zIndex: 1000 }}>
                  <h4 className="text-xs font-bold text-slate-500 dark:text-slate-300 uppercase tracking-wider mb-1">Status Legend</h4>
                  {['Moving', 'Stopped', 'Delayed', 'Offline'].map(s => <div key={s} className="flex items-center space-x-2 text-sm"><StatusDot status={s} /><span className="text-slate-700 dark:text-slate-300">{s}</span></div>)}
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-slate-800/40 dark:backdrop-blur-xl border border-slate-200 dark:border-slate-700/50 rounded-2xl p-6 shadow-sm flex flex-col">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-bold text-slate-800 dark:text-white flex items-center"><AlertTriangle className="mr-2 text-rose-500" size={20} />Recent Violations</h2>
                <span className="text-xs font-semibold bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400 px-2.5 py-1 rounded-full border border-blue-200 dark:border-blue-500/20">Live</span>
              </div>
              <div className="flex-1 w-full overflow-hidden">
                {data.recent_violations.length === 0 ? (
                  <div className="h-full min-h-[200px] flex flex-col items-center justify-center text-slate-400 dark:text-slate-500 space-y-3">
                    <AlertTriangle size={32} className="opacity-50" />
                    <p>No recent violations</p>
                  </div>
                ) : (
                  <table className="w-full table-fixed text-left text-sm whitespace-nowrap">
                    <thead className="text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wider bg-slate-50 dark:bg-slate-800/50">
                      <tr><th className="px-4 py-3 font-semibold rounded-l-lg w-[40%]">Driver</th><th className="px-4 py-3 font-semibold w-[30%]">Route</th><th className="px-4 py-3 font-semibold rounded-r-lg w-[30%]">Violation</th></tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
                      {data.recent_violations.map(v => (
                        <tr key={v.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors">
                          <td className="px-4 py-4 overflow-hidden text-ellipsis">
                            <div className="flex items-center space-x-3">
                              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-slate-100 dark:bg-gradient-to-br dark:from-slate-600 dark:to-slate-800 flex items-center justify-center font-bold text-xs">{v.driver?.name?.substring(0, 2).toUpperCase()}</div>
                              <div className="overflow-hidden"><p className="font-medium text-slate-800 dark:text-white truncate">{v.driver?.name}</p></div>
                            </div>
                          </td>
                          <td className="px-4 py-4"><span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-slate-100 text-slate-600 border border-slate-200 dark:bg-slate-700 dark:text-slate-300 dark:border-slate-600 truncate max-w-full">{v.route}</span></td>
                          <td className="px-4 py-4"><span className={`inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold border truncate max-w-full ${getViolationColor(v.type)}`}>{v.type}</span></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          </div>
        </div>
      );
    }
    if (activeTab === 'Drivers') return <DriversModule />;
    if (activeTab === 'Buses & QR') return <BusManagement />;
    if (activeTab === 'Assignments') return <AssignmentManagement />;
    if (activeTab === 'Routes & Stops') return <RoutesAdmin apiBase={API_BASE} />;
    if (activeTab === 'Lost & Found') return <LostFoundManagement />;
    return <PlaceholderModule tabName={activeTab} />;
  };

  return (
    <div className="h-screen bg-slate-50 dark:bg-gray-950 text-slate-800 dark:text-slate-200 flex font-sans overflow-hidden selection:bg-blue-500/30 transition-colors duration-300">
      <aside className="w-64 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800/80 flex flex-col h-full flex-shrink-0 relative z-20 transition-colors duration-300">
        <div className="p-6 flex items-center space-x-3">
          <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-600/20">
            <Bus className="text-white" size={24} />
          </div>
          <span className="text-xl font-bold tracking-wide text-slate-900 dark:text-white">Transit<span className="font-light text-slate-500 dark:text-slate-400">Admin</span></span>
        </div>
        <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
          <SidebarItem icon={LayoutDashboard} label="Overview" active={activeTab === 'Overview'} onClick={() => setActiveTab('Overview')} />
          <SidebarItem icon={Users} label="Drivers" active={activeTab === 'Drivers'} onClick={() => setActiveTab('Drivers')} />
          <SidebarItem icon={Bus} label="Buses & QR" active={activeTab === 'Buses & QR'} onClick={() => setActiveTab('Buses & QR')} />
          <SidebarItem icon={UserPlus} label="Assignments" active={activeTab === 'Assignments'} onClick={() => setActiveTab('Assignments')} />
          <SidebarItem icon={MapIcon} label="Routes & Stops" active={activeTab === 'Routes & Stops'} onClick={() => setActiveTab('Routes & Stops')} />
          <SidebarItem icon={Archive} label="Lost & Found" active={activeTab === 'Lost & Found'} onClick={() => setActiveTab('Lost & Found')} />
        </nav>
        <div className="p-4 border-t border-slate-200 dark:border-slate-800/80 space-y-2">
          <div className="mt-6 pt-6 border-t border-slate-200 dark:border-slate-800/80">
            <div className="flex items-center space-x-3 px-2 mb-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-500 flex items-center justify-center text-white font-bold shadow-lg shadow-indigo-500/20">
                {user.fullName?.substring(0, 2).toUpperCase() || 'AD'}
              </div>
              <div className="flex flex-col">
                <span className="text-sm font-semibold text-slate-900 dark:text-white">{user.fullName}</span>
                <span className="text-xs text-slate-500 dark:text-slate-400">{user.role}</span>
              </div>
            </div>
            <button onClick={logout} className="w-full flex items-center justify-center space-x-2 text-sm text-slate-500 hover:text-rose-600 dark:text-slate-400 dark:hover:text-rose-400 py-2 rounded-xl hover:bg-rose-50 dark:hover:bg-rose-500/10 transition-colors">
              <LogOut size={16} /><span>Sign Out</span>
            </button>
          </div>
        </div>
      </aside>
      <main className="flex-1 h-full overflow-y-auto relative flex flex-col">
        <div className="pointer-events-none fixed inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-blue-100/50 dark:from-blue-900/20 via-slate-50 dark:via-slate-900/0 to-transparent z-0"></div>
        <header className="sticky top-0 z-30 bg-white/80 dark:bg-gray-950/80 backdrop-blur-md border-b border-slate-200 dark:border-slate-800/40 px-8 py-5 flex items-center justify-between flex-shrink-0 transition-colors duration-300">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">{activeTab === 'Overview' ? 'Dashboard' : activeTab}</h1>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">TransitShield Admin Panel</p>
          </div>
          <div className="flex items-center space-x-6">
            <div className="relative group">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 dark:text-slate-500 group-focus-within:text-blue-500 transition-colors" size={18} />
              <input type="text" placeholder="Search..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)}
                className="bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-700/50 text-sm rounded-full pl-10 pr-4 py-2 w-64 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 transition-all text-slate-800 dark:text-slate-200 placeholder-slate-400 dark:placeholder-slate-500" />
            </div>
            <button onClick={() => setIsDarkMode(!isDarkMode)} className="p-2 text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white transition-colors rounded-full hover:bg-slate-100 dark:hover:bg-slate-800">
              {isDarkMode ? <Sun size={20} /> : <Moon size={20} />}
            </button>
            <button className="relative p-2 text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white transition-colors rounded-full hover:bg-slate-100 dark:hover:bg-slate-800">
              <Bell size={20} /><span className="absolute top-1.5 right-1.5 w-2 h-2 bg-rose-500 rounded-full border-2 border-white dark:border-gray-950"></span>
            </button>
          </div>
        </header>
        <div className="flex-1 p-8 max-w-7xl w-full mx-auto relative z-10">
          {renderContent()}
        </div>
      </main>
    </div>
  );
}

import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { AlertTriangle, Archive, MapIcon, RefreshCw, Route, Save } from 'lucide-react';

const API_BASE = typeof window !== 'undefined'
  ? (localStorage.getItem('ts_api_base') || `${window.location.protocol}//${window.location.hostname}:8080`)
  : 'http://localhost:8080';

const getErrorMessage = (error, fallback) => (
  error?.response?.data?.message ||
  error?.response?.data?.error ||
  fallback
);

const cardClass = 'bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl';
const inputClass = 'w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all';
const selectClass = inputClass;

export const RoutesStopsModule = () => {
  const getAuthHeader = () => {
    const token = JSON.parse(localStorage.getItem('ts_user'))?.token;
    return { headers: { Authorization: `Bearer ${token}` } };
  };
  const [routes, setRoutes] = useState([]);
  const [variants, setVariants] = useState([]);
  const [stops, setStops] = useState([]);
  const [variantStops, setVariantStops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [routeForm, setRouteForm] = useState({
    routeNumber: '',
    displayName: '',
    routeCategory: 'CITY',
    isActive: true
  });
  const [stopForm, setStopForm] = useState({
    stopCode: '',
    stopName: '',
    latitude: '',
    longitude: '',
    isActive: true
  });
  const [variantForm, setVariantForm] = useState({
    routeId: '',
    variantCode: '',
    originName: '',
    destinationName: '',
    directionLabel: 'OUTBOUND',
    serviceType: 'NORMAL',
    notes: '',
    isActive: true
  });
  const [selectedVariantId, setSelectedVariantId] = useState('');
  const [selectedStopIds, setSelectedStopIds] = useState([]);
  const [editingRouteId, setEditingRouteId] = useState(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const config = getAuthHeader();
      const [routesRes, variantsRes, stopsRes, variantStopsRes] = await Promise.all([
        axios.get(`${API_BASE}/api/routes`, config),
        axios.get(`${API_BASE}/api/route-variants`, config),
        axios.get(`${API_BASE}/api/stops`, config),
        axios.get(`${API_BASE}/api/route-variant-stops`, config)
      ]);
      setRoutes(routesRes.data || []);
      setVariants(variantsRes.data || []);
      setStops(stopsRes.data || []);
      setVariantStops(variantStopsRes.data || []);
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to load routes and stops'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const variantsByRoute = useMemo(() => {
    const map = new Map();
    variants.forEach((variant) => {
      const list = map.get(variant.routeId) || [];
      list.push(variant);
      map.set(variant.routeId, list);
    });
    return map;
  }, [variants]);

  const stopLookup = useMemo(() => {
    const map = new Map();
    stops.forEach((stop) => map.set(stop.id, stop));
    return map;
  }, [stops]);

  const selectedVariantStops = useMemo(() => (
    variantStops
      .filter((item) => String(item.routeVariantId) === String(selectedVariantId))
      .sort((a, b) => (a.stopOrder || 0) - (b.stopOrder || 0))
  ), [selectedVariantId, variantStops]);

  const createRoute = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      const payload = {
        routeNumber: routeForm.routeNumber.trim(),
        displayName: routeForm.displayName.trim(),
        routeCategory: routeForm.routeCategory.trim(),
        isActive: routeForm.isActive
      };
      if (editingRouteId) {
        await axios.put(`${API_BASE}/api/routes/${editingRouteId}`, payload, getAuthHeader());
        setMessage('Route updated successfully');
      } else {
        await axios.post(`${API_BASE}/api/routes`, payload, getAuthHeader());
        setMessage('Route created successfully');
      }
      setRouteForm({ routeNumber: '', displayName: '', routeCategory: 'CITY', isActive: true });
      setEditingRouteId(null);
      await loadData();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to save route'));
    }
  };

  const createStop = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      await axios.post(`${API_BASE}/api/stops`, {
        stopCode: stopForm.stopCode.trim(),
        stopName: stopForm.stopName.trim(),
        latitude: Number(stopForm.latitude),
        longitude: Number(stopForm.longitude),
        isActive: stopForm.isActive
      }, getAuthHeader());
      setStopForm({
        stopCode: '',
        stopName: '',
        latitude: '',
        longitude: '',
        isActive: true
      });
      setMessage('Stop created successfully');
      await loadData();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to create stop'));
    }
  };

  const createVariant = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      const payload = {
        routeId: Number(variantForm.routeId),
        variantCode: variantForm.variantCode.trim() || null,
        originName: variantForm.originName.trim(),
        destinationName: variantForm.destinationName.trim(),
        directionLabel: variantForm.directionLabel.trim(),
        serviceType: variantForm.serviceType,
        notes: variantForm.notes.trim(),
        isActive: variantForm.isActive
      };
      const response = await axios.post(`${API_BASE}/api/route-variants`, payload, getAuthHeader());
      setSelectedVariantId(String(response.data.id));
      setMessage('Route variant created successfully');
      setVariantForm({
        routeId: '',
        variantCode: '',
        originName: '',
        destinationName: '',
        directionLabel: 'OUTBOUND',
        serviceType: 'NORMAL',
        notes: '',
        isActive: true
      });
      await loadData();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to create route variant'));
    }
  };

  const deleteRoute = async (id) => {
    const confirmed = window.confirm('Are you sure you want to delete this route?');
    if (!confirmed) return;

    try {
      await axios.delete(`${API_BASE}/api/routes/${id}`, getAuthHeader());
      setMessage('Route deleted successfully.');
      loadData();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to delete route. Check if it has active variants.');
    }
  };

  const editRoute = (route) => {
    setEditingRouteId(route.id);
    setRouteForm({
      routeNumber: route.routeNumber || '',
      displayName: route.displayName || '',
      routeCategory: route.routeCategory || 'CITY',
      isActive: !!route.isActive
    });
    setMessage('');
  };

  const saveOrderedStops = async () => {
    if (!selectedVariantId || selectedStopIds.length === 0) {
      setMessage('Select a route variant and at least one stop');
      return;
    }
    setMessage('');
    try {
      const config = getAuthHeader();
      const existing = selectedVariantStops;
      await Promise.all(existing.map((item) => axios.delete(`${API_BASE}/api/route-variant-stops/${item.id}`, config)));
      await Promise.all(selectedStopIds.map((stopId, index) => (
        axios.post(`${API_BASE}/api/route-variant-stops`, {
          routeVariantId: Number(selectedVariantId),
          stopId,
          stopOrder: index + 1,
          distanceFromStartKm: index,
          isMajorStop: index === 0 || index === selectedStopIds.length - 1
        }, config)
      )));
      setMessage('Ordered stops saved successfully');
      await loadData();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to save ordered stops'));
    }
  };

  const toggleStop = (stopId) => {
    setSelectedStopIds((current) => (
      current.includes(stopId)
        ? current.filter((value) => value !== stopId)
        : [...current, stopId]
    ));
  };

  useEffect(() => {
    if (!selectedVariantId) {
      setSelectedStopIds([]);
      return;
    }
    setSelectedStopIds(selectedVariantStops.map((item) => item.stopId));
  }, [selectedVariantId, selectedVariantStops]);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-slate-800 dark:text-white flex items-center"><Route className="mr-2 text-blue-500" size={22} />Routes & Stops Admin</h2>
      </div>

      {message && (
        <div className={`rounded-xl px-4 py-3 text-sm border ${message.toLowerCase().includes('success') ? 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-500/30' : 'bg-blue-50 dark:bg-blue-500/10 text-blue-700 dark:text-blue-400 border-blue-200 dark:border-blue-500/30'}`}>
          {message}
        </div>
      )}

      <div className={`${cardClass} p-6`}>
        <h3 className="font-semibold text-slate-800 dark:text-white mb-4">{editingRouteId ? 'Edit Route' : 'Create Route'}</h3>
        <form onSubmit={createRoute} className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <input className={inputClass} placeholder="Route Number" value={routeForm.routeNumber} onChange={(e) => setRouteForm({ ...routeForm, routeNumber: e.target.value })} required />
          <input className={inputClass} placeholder="Display Name" value={routeForm.displayName} onChange={(e) => setRouteForm({ ...routeForm, displayName: e.target.value })} required />
          <input className={inputClass} placeholder="Route Category" value={routeForm.routeCategory} onChange={(e) => setRouteForm({ ...routeForm, routeCategory: e.target.value.toUpperCase() })} required />
          <select className={selectClass} value={routeForm.isActive ? 'ACTIVE' : 'INACTIVE'} onChange={(e) => setRouteForm({ ...routeForm, isActive: e.target.value === 'ACTIVE' })}>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
          <div className="md:col-span-4 flex gap-3">
            <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl transition-colors">{editingRouteId ? 'Update Route' : 'Create Route'}</button>
            {editingRouteId && (
              <button
                type="button"
                onClick={() => {
                  setEditingRouteId(null);
                  setRouteForm({ routeNumber: '', displayName: '', routeCategory: 'CITY', isActive: true });
                }}
                className="px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300"
              >
                Cancel
              </button>
            )}
          </div>
        </form>
      </div>

      <div className={`${cardClass} p-6`}>
        <h3 className="font-semibold text-slate-800 dark:text-white mb-4">Create Stop</h3>
        <form onSubmit={createStop} className="grid grid-cols-1 md:grid-cols-5 gap-4">
          <input className={inputClass} placeholder="Stop Code" value={stopForm.stopCode} onChange={(e) => setStopForm({ ...stopForm, stopCode: e.target.value.toUpperCase() })} required />
          <input className={inputClass} placeholder="Stop Name" value={stopForm.stopName} onChange={(e) => setStopForm({ ...stopForm, stopName: e.target.value })} required />
          <input className={inputClass} placeholder="Latitude" type="number" step="any" value={stopForm.latitude} onChange={(e) => setStopForm({ ...stopForm, latitude: e.target.value })} required />
          <input className={inputClass} placeholder="Longitude" type="number" step="any" value={stopForm.longitude} onChange={(e) => setStopForm({ ...stopForm, longitude: e.target.value })} required />
          <button type="submit" className="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-3 rounded-xl transition-colors">Create Stop</button>
        </form>
      </div>

      <div className={`${cardClass} p-6`}>
        <h3 className="font-semibold text-slate-800 dark:text-white mb-4">Create Route Variant</h3>
        <form onSubmit={createVariant} className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <select className={selectClass} value={variantForm.routeId} onChange={(e) => setVariantForm({ ...variantForm, routeId: e.target.value })} required>
            <option value="">Select Route</option>
            {routes.map((route) => (
              <option key={route.id} value={route.id}>{route.routeNumber} - {route.displayName}</option>
            ))}
          </select>
          <input className={inputClass} placeholder="Variant Code" value={variantForm.variantCode} onChange={(e) => setVariantForm({ ...variantForm, variantCode: e.target.value.toUpperCase() })} />
          <input className={inputClass} placeholder="Origin Name" value={variantForm.originName} onChange={(e) => setVariantForm({ ...variantForm, originName: e.target.value })} required />
          <input className={inputClass} placeholder="Destination Name" value={variantForm.destinationName} onChange={(e) => setVariantForm({ ...variantForm, destinationName: e.target.value })} required />
          <input className={inputClass} placeholder="Direction Label" value={variantForm.directionLabel} onChange={(e) => setVariantForm({ ...variantForm, directionLabel: e.target.value.toUpperCase() })} required />
          <select className={selectClass} value={variantForm.serviceType} onChange={(e) => setVariantForm({ ...variantForm, serviceType: e.target.value })}>
            <option value="NORMAL">Normal</option>
            <option value="EXPRESS">Express</option>
            <option value="LUXURY">Luxury</option>
            <option value="AC">AC</option>
          </select>
          <input className={inputClass} placeholder="Notes" value={variantForm.notes} onChange={(e) => setVariantForm({ ...variantForm, notes: e.target.value })} />
          <select className={selectClass} value={variantForm.isActive ? 'ACTIVE' : 'INACTIVE'} onChange={(e) => setVariantForm({ ...variantForm, isActive: e.target.value === 'ACTIVE' })}>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
          <button type="submit" className="md:col-span-4 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 rounded-xl transition-colors">Create Variant</button>
        </form>
      </div>

      <div className={`${cardClass} p-6`}>
        <div className="flex items-center justify-between gap-4 mb-4">
          <div>
            <h3 className="font-semibold text-slate-800 dark:text-white">Add Ordered Stops to Variant</h3>
          </div>
          <button type="button" onClick={saveOrderedStops} className="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold px-4 py-2.5 rounded-xl transition-colors">
            <Save size={16} />Save Ordered Stops
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
          <select className={selectClass} value={selectedVariantId} onChange={(e) => setSelectedVariantId(e.target.value)}>
            <option value="">Select Route Variant</option>
            {variants.map((variant) => {
              const route = routes.find((item) => item.id === variant.routeId);
              return (
                <option key={variant.id} value={variant.id}>
                  {route?.routeNumber || 'Route'} - {variant.originName} to {variant.destinationName}
                </option>
              );
            })}
          </select>
          <div className="md:col-span-2 flex items-center rounded-xl border border-slate-200 dark:border-slate-700 px-4 text-sm text-slate-500 dark:text-slate-400">
            Selected order: {selectedStopIds.map((stopId) => stopLookup.get(stopId)?.stopName || stopId).join(' -> ') || 'None'}
          </div>
        </div>

        <div className="flex flex-wrap gap-3">
          {stops.map((stop) => {
            const active = selectedStopIds.includes(stop.id);
            const order = selectedStopIds.indexOf(stop.id) + 1;
            return (
              <button
                key={stop.id}
                type="button"
                onClick={() => toggleStop(stop.id)}
                className={`px-4 py-2 rounded-xl text-sm border transition-colors ${active ? 'bg-blue-600 text-white border-blue-600' : 'bg-slate-50 dark:bg-slate-900 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-slate-700 hover:border-blue-400'}`}
              >
                {active ? `${order}. ` : ''}{stop.stopName}
              </button>
            );
          })}
        </div>
      </div>

      <div className={`${cardClass} overflow-hidden`}>
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 dark:bg-slate-800/50 text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wider">
            <tr>
              <th className="px-6 py-4">Stop</th>
              <th className="px-6 py-4">Code</th>
              <th className="px-6 py-4">Latitude</th>
              <th className="px-6 py-4">Longitude</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
            {stops.length === 0 ? (
              <tr><td className="px-6 py-6 text-slate-400" colSpan="4">No stops found.</td></tr>
            ) : (
              stops.map((stop) => (
                <tr key={stop.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-4 font-medium text-slate-800 dark:text-white">{stop.stopName}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{stop.stopCode || '-'}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{stop.latitude ?? '-'}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{stop.longitude ?? '-'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className={`${cardClass} overflow-hidden`}>
        {loading ? <div className="p-8 text-center text-slate-400">Loading routes...</div> : (
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/50 text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4">Route Number</th>
                <th className="px-6 py-4">Display Name</th>
                <th className="px-6 py-4">Category</th>
                <th className="px-6 py-4">Active</th>
                <th className="px-6 py-4">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
              {routes.length === 0 ? (
                <tr><td className="px-6 py-6 text-slate-400" colSpan="5">No routes found.</td></tr>
              ) : (
                routes.map((route) => (
                  <tr key={route.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors">
                    <td className="px-6 py-4 font-medium text-slate-800 dark:text-white">{route.routeNumber}</td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{route.displayName}</td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{route.routeCategory || '-'}</td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{route.isActive ? 'Active' : 'Inactive'}</td>
                    <td className="px-6 py-4">
                      <div className="flex gap-3">
                        <button
                          type="button"
                          onClick={() => editRoute(route)}
                          className="text-blue-600 hover:text-blue-800 font-medium transition-colors"
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => deleteRoute(route.id)}
                          className="text-rose-600 hover:text-rose-800 font-medium transition-colors"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export const AssignmentsModule = () => {
  const getAuthHeader = () => {
    const token = JSON.parse(localStorage.getItem('ts_user'))?.token;
    return { headers: { Authorization: `Bearer ${token}` } };
  };
  const emptyForm = {
    busId: '',
    driverProfileId: '',
    routeVariantId: '',
    assignmentStatus: 'ACTIVE'
  };
  const [assignments, setAssignments] = useState([]);
  const [buses, setBuses] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [variants, setVariants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);

  const loadData = async () => {
    setLoading(true);
    try {
      const config = getAuthHeader();
      const [assignmentsRes, busesRes, driversRes, variantsRes] = await Promise.all([
        axios.get(`${API_BASE}/api/admin/assignments`, config),
        axios.get(`${API_BASE}/api/buses`, config),
        axios.get(`${API_BASE}/api/admin/driver-profiles`, config),
        axios.get(`${API_BASE}/api/route-variants`, config)
      ]);
      setAssignments(assignmentsRes.data || []);
      setBuses(busesRes.data || []);
      setDrivers(driversRes.data || []);
      setVariants(variantsRes.data || []);
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to load assignments'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const saveAssignment = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      const config = getAuthHeader();
      const payload = {
        busId: Number(form.busId),
        driverProfileId: Number(form.driverProfileId),
        routeVariantId: Number(form.routeVariantId),
        assignmentStatus: form.assignmentStatus
      };
      if (editingId) {
        console.log("Sending payload:", payload);
        await axios.put(`${API_BASE}/api/admin/assignments/${editingId}`, payload, config);
        setMessage('Assignment updated successfully');
      } else {
        await axios.post(`${API_BASE}/api/admin/assignments`, payload, config);
        setMessage('Assignment created successfully');
      }
      setEditingId(null);
      setForm(emptyForm);
      await loadData();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to save assignment'));
    }
  };

  const editAssignment = (assignment) => {
    setEditingId(assignment.id);
    setForm({
      busId: String(assignment.busId || ''),
      driverProfileId: String(assignment.driverProfileId || ''),
      routeVariantId: String(assignment.routeVariantId || ''),
      assignmentStatus: assignment.assignmentStatus || 'ACTIVE'
    });
    setMessage('');
  };

  const deleteAssignment = async (assignmentId) => {
    if (!window.confirm('Delete this assignment?')) return;
    setMessage('');
    try {
      await axios.delete(`${API_BASE}/api/admin/assignments/${assignmentId}`, getAuthHeader());
      if (editingId === assignmentId) {
        setEditingId(null);
        setForm(emptyForm);
      }
      setMessage('Assignment deleted successfully');
      await loadData();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to delete assignment'));
    }
  };

  const busLookup = new Map(buses.map((bus) => [bus.id, `${bus.busCode} - ${bus.busDisplayName}`]));
  const driverLookup = new Map(drivers.map((driver) => [driver.id, driver.fullName || driver.driverCode || `Driver ${driver.id}`]));
  const variantLookup = new Map(variants.map((variant) => [variant.id, `${variant.variantCode || 'Variant'} - ${variant.originName || 'Origin'} to ${variant.destinationName || 'Destination'}`]));

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-slate-800 dark:text-white">Assignments</h2>
      </div>

      {message && (
        <div className={`rounded-xl px-4 py-3 text-sm border ${message.toLowerCase().includes('success') ? 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-500/30' : 'bg-blue-50 dark:bg-blue-500/10 text-blue-700 dark:text-blue-400 border-blue-200 dark:border-blue-500/30'}`}>
          {message}
        </div>
      )}

      <div className={`${cardClass} p-6`}>
        <form onSubmit={saveAssignment} className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <select className={selectClass} value={form.busId} onChange={(e) => setForm({ ...form, busId: e.target.value })} required>
            <option value="">Select Bus</option>
            {buses.map((bus) => (
              <option key={bus.id} value={bus.id}>{bus.busCode} - {bus.busDisplayName}</option>
            ))}
          </select>
          <select className={selectClass} value={form.driverProfileId} onChange={(e) => setForm({ ...form, driverProfileId: e.target.value })} required>
            <option value="">Select Driver</option>
            {drivers.map((driver) => (
              <option key={driver.id} value={driver.id}>{driver.fullName || driver.driverCode || `Driver ${driver.id}`}</option>
            ))}
          </select>
          <select className={selectClass} value={form.routeVariantId} onChange={(e) => setForm({ ...form, routeVariantId: e.target.value })} required>
            <option value="">Select Route Variant</option>
            {variants.map((variant) => (
              <option key={variant.id} value={variant.id}>{variant.variantCode || 'Variant'} - {variant.originName || 'Origin'} to {variant.destinationName || 'Destination'}</option>
            ))}
          </select>
          <div className="flex gap-3">
            <select className={selectClass} value={form.assignmentStatus} onChange={(e) => setForm({ ...form, assignmentStatus: e.target.value })}>
              <option value="ACTIVE">Active</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-5 py-3 rounded-xl transition-colors whitespace-nowrap">
              {editingId ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>

      <div className={`${cardClass} overflow-hidden`}>
        {loading ? <div className="p-8 text-center text-slate-400">Loading assignments...</div> : (
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/50 text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4">Bus</th>
                <th className="px-6 py-4">Driver</th>
                <th className="px-6 py-4">Route Variant</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
              {assignments.length === 0 ? (
                <tr><td className="px-6 py-6 text-slate-400" colSpan="5">No assignments found.</td></tr>
              ) : (
                assignments.map((assignment) => (
                  <tr key={assignment.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors">
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{busLookup.get(assignment.busId) || assignment.busId}</td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{driverLookup.get(assignment.driverProfileId) || assignment.driverProfileId}</td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{variantLookup.get(assignment.routeVariantId) || assignment.routeVariantId}</td>
                    <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{assignment.assignmentStatus}</td>
                    <td className="px-6 py-4">
                      <div className="flex gap-3">
                        <button type="button" onClick={() => editAssignment(assignment)} className="text-blue-600 hover:text-blue-800 font-medium transition-colors">Edit</button>
                        <button type="button" onClick={() => deleteAssignment(assignment.id)} className="text-rose-600 hover:text-rose-800 font-medium transition-colors">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export const LostFoundModule = () => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [search, setSearch] = useState('');
  const [message, setMessage] = useState('');

  const loadReports = async () => {
    setLoading(true);
    try {
      const token = JSON.parse(localStorage.getItem('ts_user'))?.token;
      const query = statusFilter !== 'ALL' ? `?status=${statusFilter}` : '';
      const res = await axios.get(`${API_BASE}/api/admin/lost-items${query}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setReports(res.data || []);
    } catch {
      setReports([]);
      setMessage('Failed to load lost item reports');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReports();
  }, []);

  const updateStatus = async (reportId, status) => {
    setMessage('');
    try {
      await axios.patch(`${API_BASE}/api/admin/lost-items/${reportId}/status`, { status }, {
        headers: { Authorization: `Bearer ${JSON.parse(localStorage.getItem('ts_user'))?.token}` }
      });
      await loadReports();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to update lost item status'));
    }
  };

  const filteredReports = reports.filter((report) => {
    const matchesStatus = statusFilter === 'ALL' || report.status === statusFilter;
    const haystack = [
      report.itemName,
      report.reporterName,
      report.routeOrBus,
      report.notes
    ].join(' ').toLowerCase();
    return matchesStatus && haystack.includes(search.toLowerCase());
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-slate-800 dark:text-white flex items-center"><Archive className="mr-2 text-blue-500" size={22} />Lost & Found Management</h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <input className={inputClass} placeholder="Search by item, route, bus, reporter..." value={search} onChange={(e) => setSearch(e.target.value)} />
        <select className={selectClass} value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="ALL">All</option>
          <option value="OPEN">Open</option>
          <option value="MATCHED">Matched</option>
          <option value="RESOLVED">Resolved</option>
        </select>
        <button type="button" onClick={loadReports} className="inline-flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl px-4 py-3 transition-colors">
          <RefreshCw size={16} />Refresh
        </button>
      </div>

      {message && (
        <div className="rounded-xl px-4 py-3 text-sm border bg-blue-50 dark:bg-blue-500/10 text-blue-700 dark:text-blue-400 border-blue-200 dark:border-blue-500/30">
          {message}
        </div>
      )}

      <div className={`${cardClass} overflow-hidden`}>
        {loading ? <div className="p-8 text-center text-slate-400">Loading lost item reports...</div> : (
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/50 text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4">Item</th>
                <th className="px-6 py-4">Reporter</th>
                <th className="px-6 py-4">Route/Bus</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Notes</th>
                <th className="px-6 py-4">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
              {filteredReports.length === 0 && (
                <tr><td className="px-6 py-6 text-slate-400" colSpan="6">No lost item reports found.</td></tr>
              )}
              {filteredReports.map((report) => (
                <tr key={report.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-4 font-medium text-slate-800 dark:text-white">{report.itemName}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{report.reporterName || '-'}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{report.routeOrBus || '-'}</td>
                  <td className="px-6 py-4">
                    <span className="px-2 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-200">
                      {report.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{report.notes || '-'}</td>
                  <td className="px-6 py-4">
                    <div className="flex flex-wrap gap-2">
                      <button type="button" onClick={() => updateStatus(report.id, 'OPEN')} className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-200">Open</button>
                      <button type="button" onClick={() => updateStatus(report.id, 'MATCHED')} className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-amber-500 text-white">Matched</button>
                      <button type="button" onClick={() => updateStatus(report.id, 'RESOLVED')} className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 text-white">Resolved</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export const ComplaintsModule = () => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  const loadComplaints = async () => {
    setLoading(true);
    try {
      const token = JSON.parse(localStorage.getItem('ts_user'))?.token;
      const res = await axios.get(`${API_BASE}/api/admin/complaints`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setComplaints(res.data || []);
    } catch (err) {
      setMessage('Failed to load complaints. You may not have permission.');
      setComplaints([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadComplaints();
  }, []);

  const updateStatus = async (complaintId, status) => {
    setMessage('');
    try {
      await axios.patch(`${API_BASE}/api/admin/complaints/${complaintId}/status`, { status }, {
        headers: { Authorization: `Bearer ${JSON.parse(localStorage.getItem('ts_user'))?.token}` }
      });
      await loadComplaints();
    } catch (error) {
      setMessage(getErrorMessage(error, 'Failed to update complaint status'));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-800 dark:text-white flex items-center"><AlertTriangle className="mr-2 text-blue-500" size={22} />Complaints</h2>
        </div>
        <button type="button" onClick={loadComplaints} className="inline-flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl px-4 py-3 transition-colors">
          <RefreshCw size={16} />Refresh
        </button>
      </div>

      {message && (
        <div className="rounded-xl px-4 py-3 text-sm border bg-blue-50 dark:bg-blue-500/10 text-blue-700 dark:text-blue-400 border-blue-200 dark:border-blue-500/30">
          {message}
        </div>
      )}

      <div className={`${cardClass} overflow-hidden`}>
        {loading ? <div className="p-8 text-center text-slate-400">Loading complaints...</div> : (
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/50 text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4">ID</th>
                <th className="px-6 py-4">Passenger Name</th>
                <th className="px-6 py-4">Passenger</th>
                <th className="px-6 py-4">Bus</th>
                <th className="px-6 py-4">Subject</th>
                <th className="px-6 py-4">Description</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Created At</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
              {complaints.length === 0 && (
                <tr><td className="px-6 py-6 text-slate-400" colSpan="8">No complaints found.</td></tr>
              )}
              {complaints.map((complaint) => (
                <tr key={complaint.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors align-top">
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{complaint.id}</td>
                  <td className="px-6 py-4 font-medium text-slate-800 dark:text-white">{complaint.passengerName || '-'}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{complaint.passengerContact || '-'}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{complaint.busReference || '-'}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{complaint.subject}</td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400 max-w-xs">{complaint.description}</td>
                  <td className="px-6 py-4">
                    <div className="flex flex-col gap-2">
                      <span className="px-2 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-200 w-fit">
                        {complaint.status}
                      </span>
                      <div className="flex flex-wrap gap-2">
                        <button type="button" onClick={() => updateStatus(complaint.id, 'OPEN')} className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-200">Open</button>
                        <button type="button" onClick={() => updateStatus(complaint.id, 'IN_REVIEW')} className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-amber-500 text-white">In Review</button>
                        <button type="button" onClick={() => updateStatus(complaint.id, 'RESOLVED')} className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 text-white">Resolved</button>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{complaint.createdAt ? new Date(complaint.createdAt).toLocaleString() : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export const getAdminModuleIcon = (tabName) => {
  const iconMap = {
    'Routes & Stops': MapIcon,
    'Lost & Found': Archive,
    'Complaints': AlertTriangle
  };
  return iconMap[tabName];
};

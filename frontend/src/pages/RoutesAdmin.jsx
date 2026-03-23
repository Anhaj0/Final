import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';

export default function RoutesAdmin({ apiBase }) {
  const [routes, setRoutes] = useState([]);
  const [stops, setStops] = useState([]);
  const [routeVariants, setRouteVariants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  const [routeForm, setRouteForm] = useState({
    routeNumber: '',
    displayName: '',
    routeCategory: 'CITY',
    isActive: true
  });

  const [mappingForm, setMappingForm] = useState({
    routeId: '',
    variantCode: '',
    originName: '',
    destinationName: '',
    directionLabel: 'OUTBOUND',
    selectedStops: []
  });

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [routesRes, stopsRes, variantsRes] = await Promise.all([
        axios.get(`${apiBase}/api/routes`),
        axios.get(`${apiBase}/api/stops`),
        axios.get(`${apiBase}/api/route-variants`)
      ]);
      setRoutes(routesRes.data || []);
      setStops(stopsRes.data || []);
      setRouteVariants(variantsRes.data || []);
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to load routes/stops');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
  }, []);

  const routeOptions = useMemo(() => routes.map(r => ({
    id: r.id,
    label: `${r.routeNumber || '-'} — ${r.displayName || 'Unnamed'}`
  })), [routes]);

  const createRoute = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      await axios.post(`${apiBase}/api/routes`, routeForm);
      setRouteForm({ routeNumber: '', displayName: '', routeCategory: 'CITY', isActive: true });
      setMessage('Route created successfully.');
      fetchAll();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to create route');
    }
  };

  const toggleStop = (stopId) => {
    setMappingForm(prev => {
      const exists = prev.selectedStops.includes(stopId);
      return {
        ...prev,
        selectedStops: exists
          ? prev.selectedStops.filter(id => id !== stopId)
          : [...prev.selectedStops, stopId]
      };
    });
  };

  const addOrderedStopsToRoute = async (e) => {
    e.preventDefault();
    setMessage('');

    if (!mappingForm.routeId || mappingForm.selectedStops.length === 0) {
      setMessage('Select a route and at least one stop.');
      return;
    }

    try {
      const variantPayload = {
        routeId: Number(mappingForm.routeId),
        variantCode: mappingForm.variantCode || `V-${Date.now().toString().slice(-4)}`,
        originName: mappingForm.originName || 'Origin',
        destinationName: mappingForm.destinationName || 'Destination',
        directionLabel: mappingForm.directionLabel,
        serviceType: 'NORMAL',
        isActive: true
      };

      const variantRes = await axios.post(`${apiBase}/api/route-variants`, variantPayload);
      const variantId = variantRes.data?.id;

      for (let i = 0; i < mappingForm.selectedStops.length; i++) {
        const stopId = mappingForm.selectedStops[i];
        await axios.post(`${apiBase}/api/route-variant-stops`, {
          routeVariantId: variantId,
          stopId,
          stopOrder: i + 1,
          distanceFromStartKm: i === 0 ? 0 : i * 1.5,
          isMajorStop: i % 2 === 0
        });
      }

      setMessage('Route variant and ordered stops saved.');
      setMappingForm({
        routeId: '',
        variantCode: '',
        originName: '',
        destinationName: '',
        directionLabel: 'OUTBOUND',
        selectedStops: []
      });
      fetchAll();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to add ordered stops');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-slate-800 dark:text-white">Routes & Stops Admin</h2>
        <p className="text-sm text-slate-500 dark:text-slate-400">Create routes and attach ordered stops.</p>
      </div>

      {message && (
        <div className="rounded-xl px-4 py-3 text-sm border bg-blue-50 dark:bg-blue-500/10 text-blue-700 dark:text-blue-300 border-blue-200 dark:border-blue-500/30">
          {message}
        </div>
      )}

      <form onSubmit={createRoute} className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-4 grid grid-cols-1 md:grid-cols-5 gap-3">
        <input
          value={routeForm.routeNumber}
          onChange={e => setRouteForm({ ...routeForm, routeNumber: e.target.value })}
          placeholder="Route Number (e.g., 138)"
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900"
          required
        />
        <input
          value={routeForm.displayName}
          onChange={e => setRouteForm({ ...routeForm, displayName: e.target.value })}
          placeholder="Display Name"
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900"
          required
        />
        <input
          value={routeForm.routeCategory}
          onChange={e => setRouteForm({ ...routeForm, routeCategory: e.target.value })}
          placeholder="Category"
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900"
        />
        <select
          value={routeForm.isActive ? 'true' : 'false'}
          onChange={e => setRouteForm({ ...routeForm, isActive: e.target.value === 'true' })}
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900"
        >
          <option value="true">Active</option>
          <option value="false">Inactive</option>
        </select>
        <button className="bg-blue-600 text-white rounded-lg px-4 py-2">Create Route</button>
      </form>

      <form onSubmit={addOrderedStopsToRoute} className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-4 space-y-4">
        <h3 className="font-semibold text-slate-800 dark:text-white">Add Ordered Stops to Route</h3>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <select
            value={mappingForm.routeId}
            onChange={e => setMappingForm({ ...mappingForm, routeId: e.target.value })}
            className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900"
            required
          >
            <option value="">Select Route</option>
            {routeOptions.map(route => (
              <option key={route.id} value={route.id}>{route.label}</option>
            ))}
          </select>
          <input value={mappingForm.variantCode} onChange={e => setMappingForm({ ...mappingForm, variantCode: e.target.value })} placeholder="Variant Code (optional)" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" />
          <input value={mappingForm.originName} onChange={e => setMappingForm({ ...mappingForm, originName: e.target.value })} placeholder="Origin Name" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" />
          <input value={mappingForm.destinationName} onChange={e => setMappingForm({ ...mappingForm, destinationName: e.target.value })} placeholder="Destination Name" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900" />
        </div>

        <div>
          <p className="text-sm text-slate-600 dark:text-slate-300 mb-2">Choose stops in sequence (click to add/remove).</p>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 max-h-64 overflow-y-auto border border-slate-200 dark:border-slate-700 rounded-xl p-3">
            {stops.map(stop => {
              const selected = mappingForm.selectedStops.includes(stop.id);
              return (
                <button
                  type="button"
                  key={stop.id}
                  onClick={() => toggleStop(stop.id)}
                  className={`text-left px-3 py-2 rounded-lg border transition ${selected ? 'bg-blue-600 text-white border-blue-600' : 'bg-slate-100 dark:bg-slate-900 border-slate-200 dark:border-slate-700'}`}
                >
                  <div className="font-medium">{stop.stopName || stop.stopCode || `Stop ${stop.id}`}</div>
                  <div className="text-xs opacity-80">#{stop.id}</div>
                </button>
              );
            })}
          </div>
        </div>

        <button className="bg-emerald-600 text-white rounded-lg px-4 py-2">Save Ordered Stops</button>
      </form>

      <div className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl overflow-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left bg-slate-100 dark:bg-slate-900">
              <th className="p-3">Route Number</th>
              <th className="p-3">Display Name</th>
              <th className="p-3">Category</th>
              <th className="p-3">Active</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td className="p-4 text-slate-400" colSpan={4}>Loading...</td></tr>
            ) : routes.length === 0 ? (
              <tr><td className="p-4 text-slate-400" colSpan={4}>No routes found</td></tr>
            ) : routes.map(route => (
              <tr key={route.id} className="border-t border-slate-200 dark:border-slate-700">
                <td className="p-3 font-semibold">{route.routeNumber}</td>
                <td className="p-3">{route.displayName}</td>
                <td className="p-3">{route.routeCategory}</td>
                <td className="p-3">{route.isActive ? 'Yes' : 'No'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="text-xs text-slate-500 dark:text-slate-400">
        Existing variants: {routeVariants.length}
      </div>
    </div>
  );
}

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

  // State for manual stop addition
  const [selectedStopToAdd, setSelectedStopToAdd] = useState('');

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

  // --- Manual Stop Management Functions ---
  const handleAddStop = () => {
    if (!selectedStopToAdd) return;
    const stopId = Number(selectedStopToAdd);
    
    // Optional: Prevent adding the exact same stop back-to-back
    if (mappingForm.selectedStops[mappingForm.selectedStops.length - 1] === stopId) {
      setMessage("You just added this stop. Pick the next one.");
      return;
    }

    setMappingForm(prev => ({
      ...prev,
      selectedStops: [...prev.selectedStops, stopId]
    }));
    setSelectedStopToAdd(''); // Reset dropdown
    setMessage('');
  };

  const handleRemoveStop = (indexToRemove) => {
    setMappingForm(prev => ({
      ...prev,
      selectedStops: prev.selectedStops.filter((_, index) => index !== indexToRemove)
    }));
  };

  const addOrderedStopsToRoute = async (e) => {
    e.preventDefault();
    setMessage('');

    if (!mappingForm.routeId || mappingForm.selectedStops.length === 0) {
      setMessage('Select a route and add at least one stop.');
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

      setMessage('Route variant and ordered stops saved successfully.');
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
      </div>

      {message && (
        <div className="rounded-xl px-4 py-3 text-sm border bg-blue-50 dark:bg-blue-500/10 text-blue-700 dark:text-blue-300 border-blue-200 dark:border-blue-500/30">
          {message}
        </div>
      )}

      {/* 1. Create Route Form */}
      <form onSubmit={createRoute} className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-4 grid grid-cols-1 md:grid-cols-5 gap-3">
        <input
          value={routeForm.routeNumber}
          onChange={e => setRouteForm({ ...routeForm, routeNumber: e.target.value })}
          placeholder="Route Number (e.g., 138)"
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
          required
        />
        <input
          value={routeForm.displayName}
          onChange={e => setRouteForm({ ...routeForm, displayName: e.target.value })}
          placeholder="Display Name"
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
          required
        />
        <input
          value={routeForm.routeCategory}
          onChange={e => setRouteForm({ ...routeForm, routeCategory: e.target.value })}
          placeholder="Category"
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
        />
        <select
          value={routeForm.isActive ? 'true' : 'false'}
          onChange={e => setRouteForm({ ...routeForm, isActive: e.target.value === 'true' })}
          className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
        >
          <option value="true">Active</option>
          <option value="false">Inactive</option>
        </select>
        <button className="bg-blue-600 hover:bg-blue-700 text-white rounded-lg px-4 py-2 font-semibold transition-colors">Create Route</button>
      </form>

      {/* 2. Map Stops to Route Form */}
      <form onSubmit={addOrderedStopsToRoute} className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl p-6 space-y-6">
        <h3 className="font-semibold text-slate-800 dark:text-white">Build Route Sequence</h3>
        
        {/* Route Details */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <select
            value={mappingForm.routeId}
            onChange={e => setMappingForm({ ...mappingForm, routeId: e.target.value })}
            className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
            required
          >
            <option value="">-- Select Parent Route --</option>
            {routeOptions.map(route => (
              <option key={route.id} value={route.id}>{route.label}</option>
            ))}
          </select>
          <input value={mappingForm.variantCode} onChange={e => setMappingForm({ ...mappingForm, variantCode: e.target.value })} placeholder="Variant Code (e.g. 138-OUT)" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/50" />
          <input value={mappingForm.originName} onChange={e => setMappingForm({ ...mappingForm, originName: e.target.value })} placeholder="Origin Name" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/50" />
          <input value={mappingForm.destinationName} onChange={e => setMappingForm({ ...mappingForm, destinationName: e.target.value })} placeholder="Destination Name" className="px-3 py-2 rounded-lg bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/50" />
        </div>

        {/* Manual Stop Builder Area */}
        <div className="bg-slate-50 dark:bg-slate-800/20 border border-slate-200 dark:border-slate-700 rounded-xl p-5">
          <p className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">1. Add Stops (Type to search)</p>
          
          <div className="flex gap-3 mb-6">
            <select
              value={selectedStopToAdd}
              onChange={(e) => setSelectedStopToAdd(e.target.value)}
              className="flex-1 px-4 py-2.5 rounded-lg bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
            >
              <option value="">-- Type or Select a Stop --</option>
              {stops.map(s => (
                <option key={s.id} value={s.id}>{s.stopName} ({s.stopCode})</option>
              ))}
            </select>
            <button
              type="button"
              onClick={handleAddStop}
              className="bg-slate-800 dark:bg-slate-700 hover:bg-slate-900 dark:hover:bg-slate-600 text-white font-semibold px-6 py-2.5 rounded-lg transition-colors"
            >
              + Add
            </button>
          </div>

          <p className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">2. Current Sequence</p>
          
          {mappingForm.selectedStops.length === 0 ? (
            <div className="text-center p-6 bg-white dark:bg-slate-900 rounded-lg border border-dashed border-slate-300 dark:border-slate-700 text-slate-400">
              No stops added yet. Select a stop above to begin building the route.
            </div>
          ) : (
            <div className="space-y-2 max-h-80 overflow-y-auto pr-2">
              {mappingForm.selectedStops.map((stopId, index) => {
                const stopObj = stops.find(s => s.id === stopId);
                return (
                  <div key={`${stopId}-${index}`} className="flex justify-between items-center bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 p-3 rounded-lg shadow-sm">
                    <div className="flex items-center gap-4">
                      <span className="bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400 font-black px-3 py-1 rounded-md text-sm">
                        #{index + 1}
                      </span>
                      <div>
                        <p className="font-bold text-slate-800 dark:text-slate-200">{stopObj ? stopObj.stopName : 'Unknown Stop'}</p>
                        <p className="text-xs text-slate-500">{stopObj ? stopObj.stopCode : `ID: ${stopId}`}</p>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemoveStop(index)}
                      className="text-rose-500 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-500/10 px-3 py-1.5 rounded-md text-sm font-semibold transition-colors"
                    >
                      Remove
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl px-4 py-3 transition-colors shadow-lg shadow-emerald-600/20">
          Save Final Sequence
        </button>
      </form>

      {/* 3. Existing Routes Table */}
      <div className="bg-white dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/50 rounded-2xl overflow-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left bg-slate-50 dark:bg-slate-900 text-slate-500 dark:text-slate-400 uppercase tracking-wider text-xs">
              <th className="p-4">Route Number</th>
              <th className="p-4">Display Name</th>
              <th className="p-4">Category</th>
              <th className="p-4">Active</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
            {loading ? (
              <tr><td className="p-6 text-center text-slate-400" colSpan={4}>Loading routes...</td></tr>
            ) : routes.length === 0 ? (
              <tr><td className="p-6 text-center text-slate-400" colSpan={4}>No routes found</td></tr>
            ) : routes.map(route => (
              <tr key={route.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/30">
                <td className="p-4 font-bold text-slate-800 dark:text-slate-200">{route.routeNumber}</td>
                <td className="p-4 text-slate-600 dark:text-slate-300">{route.displayName}</td>
                <td className="p-4 text-slate-600 dark:text-slate-300">{route.routeCategory}</td>
                <td className="p-4">
                  <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${route.isActive ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400'}`}>
                    {route.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="text-xs font-medium text-slate-500 dark:text-slate-400 pl-2">
        Total variants in database: {routeVariants.length}
      </div>
    </div>
  );
}

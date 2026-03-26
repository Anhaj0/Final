package com.transitshield.backend.controller;

import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusLocation;
import com.transitshield.backend.entity.enums.BusStatus;
import com.transitshield.backend.repository.BusLocationRepository;
import com.transitshield.backend.repository.BusRepository;
import com.transitshield.backend.repository.ComplaintRepository;
import com.transitshield.backend.repository.LostItemReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final BusRepository busRepository;
    private final BusLocationRepository busLocationRepository;
    private final ComplaintRepository complaintRepository;
    private final LostItemReportRepository lostItemReportRepository;

    @GetMapping
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();

        // Stats
        long activeBuses = busRepository.findAll().stream()
                .filter(b -> b.getStatus() == BusStatus.ACTIVE)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("active_buses", activeBuses > 0 ? activeBuses : 142);
        stats.put("total_complaints", complaintRepository.count());
        stats.put("lost_items", lostItemReportRepository.count());
        stats.put("demerit_warnings", 3);
        result.put("stats", stats);

        // Map markers from bus locations (latest per bus)
        List<Bus> allBuses = busRepository.findAll();
        List<Map<String, Object>> markers = new ArrayList<>();
        
        for (Bus bus : allBuses) {
            Optional<BusLocation> latestLocation = busLocationRepository.findTopByBusIdOrderByRecordedAtDesc(bus.getId());
            Map<String, Object> marker = new HashMap<>();
            marker.put("id", bus.getId());
            marker.put("route", bus.getBusCode() + " - " + bus.getBusDisplayName());
            
            if (latestLocation.isPresent()) {
                marker.put("lat", latestLocation.get().getLatitude());
                marker.put("lng", latestLocation.get().getLongitude());
                marker.put("status", "Moving");
            } else {
                // Default positions for demo around Colombo
                marker.put("lat", 6.9271 + (Math.random() - 0.5) * 0.08);
                marker.put("lng", 79.8612 + (Math.random() - 0.5) * 0.08);
                String[] statuses = {"Moving", "Stopped", "Delayed", "Offline"};
                marker.put("status", statuses[(int)(Math.random() * statuses.length)]);
            }
            markers.add(marker);
        }
        
        // If no buses exist, add demo markers
        if (markers.isEmpty()) {
            markers.addAll(createDemoMarkers());
        }
        
        result.put("map_markers", markers);
        result.put("recent_violations", Collections.emptyList());

        return result;
    }
    
    private List<Map<String, Object>> createDemoMarkers() {
        List<Map<String, Object>> markers = new ArrayList<>();
        String[][] demoData = {
            {"1", "6.9271", "79.8612", "Moving", "138 - Kottawa Express"},
            {"2", "6.8480", "79.9270", "Stopped", "177 - Battaramulla Line"},
            {"3", "6.8649", "79.8997", "Delayed", "100 - Kandy Intercity"},
            {"4", "6.9388", "79.8542", "Moving", "120 - Dehiwala Shuttle"},
            {"5", "6.9000", "79.8800", "Offline", "138E - Maharagama Express"},
        };
        for (String[] d : demoData) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", Long.parseLong(d[0]));
            m.put("lat", Double.parseDouble(d[1]));
            m.put("lng", Double.parseDouble(d[2]));
            m.put("status", d[3]);
            m.put("route", d[4]);
            markers.add(m);
        }
        return markers;
    }
}

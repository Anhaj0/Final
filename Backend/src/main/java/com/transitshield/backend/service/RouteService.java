package com.transitshield.backend.service;

import com.transitshield.backend.dto.RouteDto;
import com.transitshield.backend.entity.Route;
import com.transitshield.backend.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;

    public List<RouteDto> findAll() {
        return routeRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public RouteDto findById(Long id) {
        return routeRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    public RouteDto create(RouteDto dto) {
        Route route = mapToEntity(dto);
        route = routeRepository.save(route);
        return mapToDto(route);
    }

    public RouteDto update(Long id, RouteDto dto) {
        return routeRepository.findById(id).map(route -> {
            route.setRouteNumber(dto.getRouteNumber());
            route.setDisplayName(dto.getDisplayName());
            route.setRouteCategory(dto.getRouteCategory());
            if (dto.getIsActive() != null) route.setIsActive(dto.getIsActive());
            return mapToDto(routeRepository.save(route));
        }).orElse(null);
    }

    public void delete(Long id) {
        routeRepository.deleteById(id);
    }

    private RouteDto mapToDto(Route route) {
        RouteDto dto = new RouteDto();
        dto.setId(route.getId());
        dto.setRouteNumber(route.getRouteNumber());
        dto.setDisplayName(route.getDisplayName());
        dto.setRouteCategory(route.getRouteCategory());
        dto.setIsActive(route.getIsActive());
        return dto;
    }

    private Route mapToEntity(RouteDto dto) {
        Route route = new Route();
        route.setId(dto.getId());
        route.setRouteNumber(dto.getRouteNumber());
        route.setDisplayName(dto.getDisplayName());
        route.setRouteCategory(dto.getRouteCategory());
        if(dto.getIsActive() != null) route.setIsActive(dto.getIsActive());
        return route;
    }
}

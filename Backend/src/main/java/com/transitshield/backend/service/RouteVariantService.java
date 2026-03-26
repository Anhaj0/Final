package com.transitshield.backend.service;

import com.transitshield.backend.dto.RouteVariantDto;
import com.transitshield.backend.entity.Route;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.repository.RouteRepository;
import com.transitshield.backend.repository.RouteVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteVariantService {
    private final RouteVariantRepository routeVariantRepository;
    private final RouteRepository routeRepository;

    public List<RouteVariantDto> findAll() {
        return routeVariantRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public RouteVariantDto findById(Long id) {
        return routeVariantRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    public RouteVariantDto create(RouteVariantDto dto) {
        RouteVariant rv = mapToEntity(dto);
        rv = routeVariantRepository.save(rv);
        return mapToDto(rv);
    }

    public RouteVariantDto update(Long id, RouteVariantDto dto) {
        return routeVariantRepository.findById(id).map(rv -> {
            rv.setVariantCode(dto.getVariantCode());
            rv.setOriginName(dto.getOriginName());
            rv.setDestinationName(dto.getDestinationName());
            rv.setDirectionLabel(dto.getDirectionLabel());
            rv.setServiceType(dto.getServiceType());
            rv.setNotes(dto.getNotes());
            if(dto.getIsActive() != null) rv.setIsActive(dto.getIsActive());
            
            if (dto.getRouteId() != null) {
                Route route = routeRepository.findById(dto.getRouteId()).orElse(null);
                rv.setRoute(route);
            }
            return mapToDto(routeVariantRepository.save(rv));
        }).orElse(null);
    }

    public void delete(Long id) {
        routeVariantRepository.deleteById(id);
    }

    private RouteVariantDto mapToDto(RouteVariant rv) {
        RouteVariantDto dto = new RouteVariantDto();
        dto.setId(rv.getId());
        dto.setVariantCode(rv.getVariantCode());
        dto.setOriginName(rv.getOriginName());
        dto.setDestinationName(rv.getDestinationName());
        dto.setDirectionLabel(rv.getDirectionLabel());
        dto.setServiceType(rv.getServiceType());
        dto.setNotes(rv.getNotes());
        dto.setIsActive(rv.getIsActive());
        if (rv.getRoute() != null) {
            dto.setRouteId(rv.getRoute().getId());
        }
        return dto;
    }

    private RouteVariant mapToEntity(RouteVariantDto dto) {
        RouteVariant rv = new RouteVariant();
        rv.setId(dto.getId());
        rv.setVariantCode(dto.getVariantCode());
        rv.setOriginName(dto.getOriginName());
        rv.setDestinationName(dto.getDestinationName());
        rv.setDirectionLabel(dto.getDirectionLabel());
        rv.setServiceType(dto.getServiceType());
        rv.setNotes(dto.getNotes());
        if(dto.getIsActive() != null) rv.setIsActive(dto.getIsActive());
        if (dto.getRouteId() != null) {
            Route route = routeRepository.findById(dto.getRouteId()).orElse(null);
            rv.setRoute(route);
        }
        return rv;
    }
}

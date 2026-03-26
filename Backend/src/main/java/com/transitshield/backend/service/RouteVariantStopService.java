package com.transitshield.backend.service;

import com.transitshield.backend.dto.RouteVariantStopDto;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.RouteVariantStop;
import com.transitshield.backend.entity.Stop;
import com.transitshield.backend.repository.RouteVariantRepository;
import com.transitshield.backend.repository.RouteVariantStopRepository;
import com.transitshield.backend.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteVariantStopService {
    private final RouteVariantStopRepository routeVariantStopRepository;
    private final RouteVariantRepository routeVariantRepository;
    private final StopRepository stopRepository;

    public List<RouteVariantStopDto> findAll() {
        return routeVariantStopRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public RouteVariantStopDto findById(Long id) {
        return routeVariantStopRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    public RouteVariantStopDto create(RouteVariantStopDto dto) {
        RouteVariantStop entity = mapToEntity(dto);
        entity = routeVariantStopRepository.save(entity);
        return mapToDto(entity);
    }

    public RouteVariantStopDto update(Long id, RouteVariantStopDto dto) {
        return routeVariantStopRepository.findById(id).map(entity -> {
            entity.setStopOrder(dto.getStopOrder());
            entity.setDistanceFromStartKm(dto.getDistanceFromStartKm());
            entity.setIsMajorStop(dto.getIsMajorStop());
            if (dto.getRouteVariantId() != null) {
                RouteVariant rv = routeVariantRepository.findById(dto.getRouteVariantId()).orElse(null);
                entity.setRouteVariant(rv);
            }
            if (dto.getStopId() != null) {
                Stop stop = stopRepository.findById(dto.getStopId()).orElse(null);
                entity.setStop(stop);
            }
            return mapToDto(routeVariantStopRepository.save(entity));
        }).orElse(null);
    }

    public void delete(Long id) {
        routeVariantStopRepository.deleteById(id);
    }

    private RouteVariantStopDto mapToDto(RouteVariantStop entity) {
        RouteVariantStopDto dto = new RouteVariantStopDto();
        dto.setId(entity.getId());
        dto.setStopOrder(entity.getStopOrder());
        dto.setDistanceFromStartKm(entity.getDistanceFromStartKm());
        dto.setIsMajorStop(entity.getIsMajorStop());
        if (entity.getRouteVariant() != null) {
            dto.setRouteVariantId(entity.getRouteVariant().getId());
        }
        if (entity.getStop() != null) {
            dto.setStopId(entity.getStop().getId());
            dto.setStopName(entity.getStop().getStopName());
            dto.setStopLatitude(entity.getStop().getLatitude());
            dto.setStopLongitude(entity.getStop().getLongitude());
        }
        return dto;
    }

    private RouteVariantStop mapToEntity(RouteVariantStopDto dto) {
        RouteVariantStop entity = new RouteVariantStop();
        entity.setId(dto.getId());
        entity.setStopOrder(dto.getStopOrder());
        entity.setDistanceFromStartKm(dto.getDistanceFromStartKm());
        entity.setIsMajorStop(dto.getIsMajorStop() != null ? dto.getIsMajorStop() : false);
        if (dto.getRouteVariantId() != null) {
            RouteVariant rv = routeVariantRepository.findById(dto.getRouteVariantId()).orElse(null);
            entity.setRouteVariant(rv);
        }
        if (dto.getStopId() != null) {
            Stop stop = stopRepository.findById(dto.getStopId()).orElse(null);
            entity.setStop(stop);
        }
        return entity;
    }
}

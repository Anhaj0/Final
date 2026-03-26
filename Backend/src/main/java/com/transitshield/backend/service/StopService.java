package com.transitshield.backend.service;

import com.transitshield.backend.dto.StopDto;
import com.transitshield.backend.entity.Stop;
import com.transitshield.backend.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StopService {
    private final StopRepository stopRepository;

    public List<StopDto> findAll() {
        return stopRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public StopDto findById(Long id) {
        return stopRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    public StopDto create(StopDto dto) {
        Stop stop = mapToEntity(dto);
        stop = stopRepository.save(stop);
        return mapToDto(stop);
    }

    public StopDto update(Long id, StopDto dto) {
        return stopRepository.findById(id).map(stop -> {
            stop.setStopCode(dto.getStopCode());
            stop.setStopName(dto.getStopName());
            stop.setLatitude(dto.getLatitude());
            stop.setLongitude(dto.getLongitude());
            if(dto.getIsActive() != null) stop.setIsActive(dto.getIsActive());
            return mapToDto(stopRepository.save(stop));
        }).orElse(null);
    }

    public void delete(Long id) {
        stopRepository.deleteById(id);
    }

    private StopDto mapToDto(Stop stop) {
        StopDto dto = new StopDto();
        dto.setId(stop.getId());
        dto.setStopCode(stop.getStopCode());
        dto.setStopName(stop.getStopName());
        dto.setLatitude(stop.getLatitude());
        dto.setLongitude(stop.getLongitude());
        dto.setIsActive(stop.getIsActive());
        return dto;
    }

    private Stop mapToEntity(StopDto dto) {
        Stop stop = new Stop();
        stop.setId(dto.getId());
        stop.setStopCode(dto.getStopCode());
        stop.setStopName(dto.getStopName());
        stop.setLatitude(dto.getLatitude());
        stop.setLongitude(dto.getLongitude());
        if(dto.getIsActive() != null) stop.setIsActive(dto.getIsActive());
        return stop;
    }
}

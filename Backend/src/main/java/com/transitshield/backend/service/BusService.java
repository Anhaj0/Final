package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusDto;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {
    private final BusRepository busRepository;

    public List<BusDto> findAll() {
        return busRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public BusDto findById(Long id) {
        return busRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    public BusDto create(BusDto dto) {
        Bus bus = mapToEntity(dto);
        bus = busRepository.save(bus);
        return mapToDto(bus);
    }

    public BusDto update(Long id, BusDto dto) {
        return busRepository.findById(id).map(bus -> {
            bus.setBusCode(dto.getBusCode());
            bus.setRegistrationNumber(dto.getRegistrationNumber());
            bus.setBusDisplayName(dto.getBusDisplayName());
            bus.setCapacity(dto.getCapacity());
            bus.setOperatorName(dto.getOperatorName());
            bus.setStatus(dto.getStatus());
            return mapToDto(busRepository.save(bus));
        }).orElse(null);
    }

    public void delete(Long id) {
        busRepository.deleteById(id);
    }

    private BusDto mapToDto(Bus bus) {
        BusDto dto = new BusDto();
        dto.setId(bus.getId());
        dto.setBusCode(bus.getBusCode());
        dto.setRegistrationNumber(bus.getRegistrationNumber());
        dto.setBusDisplayName(bus.getBusDisplayName());
        dto.setCapacity(bus.getCapacity());
        dto.setOperatorName(bus.getOperatorName());
        dto.setStatus(bus.getStatus());
        return dto;
    }

    private Bus mapToEntity(BusDto dto) {
        Bus bus = new Bus();
        bus.setId(dto.getId());
        bus.setBusCode(dto.getBusCode());
        bus.setRegistrationNumber(dto.getRegistrationNumber());
        bus.setBusDisplayName(dto.getBusDisplayName());
        bus.setCapacity(dto.getCapacity());
        bus.setOperatorName(dto.getOperatorName());
        bus.setStatus(dto.getStatus());
        return bus;
    }
}

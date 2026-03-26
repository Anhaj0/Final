package com.transitshield.backend.repository;

import com.transitshield.backend.entity.BusQrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BusQrCodeRepository extends JpaRepository<BusQrCode, Long> {
    Optional<BusQrCode> findByQrTokenAndIsActiveTrue(String qrToken);
    Optional<BusQrCode> findByBusIdAndIsActiveTrue(Long busId);
    List<BusQrCode> findAllByBusIdAndIsActiveTrue(Long busId);
}

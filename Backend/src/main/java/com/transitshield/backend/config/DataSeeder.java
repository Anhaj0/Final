package com.transitshield.backend.config;

import com.transitshield.backend.entity.*;
import com.transitshield.backend.entity.enums.*;
import com.transitshield.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RouteRepository routeRepository;
    private final RouteVariantRepository routeVariantRepository;
    private final StopRepository stopRepository;
    private final RouteVariantStopRepository routeVariantStopRepository;
    private final FareRuleRepository fareRuleRepository;
    private final BusRepository busRepository;
    private final BusQrCodeRepository busQrCodeRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final ComplaintRepository complaintRepository;
    private final LostItemReportRepository lostItemReportRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Empty DB detected. Seeding demo data...");

            // ─── 1. ADMIN USER ───────────────────────────────────────
            User adminUser = new User();
            adminUser.setFullName("System Admin");
            adminUser.setEmail("admin@transitshield.com");
            adminUser.setPhoneNumber("0710000000");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setToken(java.util.UUID.randomUUID().toString());
            adminUser.setRole(UserRole.ADMIN);
            adminUser.setIsActive(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(adminUser);

            // ─── 2. PASSENGER USER ───────────────────────────────────
            User passengerUser = new User();
            passengerUser.setFullName("Ashan Perera");
            passengerUser.setEmail("passenger@demo.com");
            passengerUser.setPhoneNumber("0710000001");
            passengerUser.setPassword(passwordEncoder.encode("password"));
            passengerUser.setToken(java.util.UUID.randomUUID().toString());
            passengerUser.setRole(UserRole.PASSENGER);
            passengerUser.setIsActive(true);
            passengerUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(passengerUser);

            PassengerProfile passengerProfile = new PassengerProfile();
            passengerProfile.setUser(passengerUser);
            passengerProfile.setPublicUserId("PAX0001");
            passengerProfile.setWalletBalance(1000.0);
            passengerProfile.setTotalPoints(384.0);
            passengerProfileRepository.save(passengerProfile);

            User passengerUser2 = new User();
            passengerUser2.setFullName("Nuwan Sandeep");
            passengerUser2.setEmail("passenger2@demo.com");
            passengerUser2.setPhoneNumber("0710000003");
            passengerUser2.setPassword(passwordEncoder.encode("password"));
            passengerUser2.setToken(java.util.UUID.randomUUID().toString());
            passengerUser2.setRole(UserRole.PASSENGER);
            passengerUser2.setIsActive(true);
            passengerUser2.setCreatedAt(LocalDateTime.now());
            userRepository.save(passengerUser2);

            PassengerProfile passengerProfile2 = new PassengerProfile();
            passengerProfile2.setUser(passengerUser2);
            passengerProfile2.setPublicUserId("PAX0002");
            passengerProfile2.setWalletBalance(500.0);
            passengerProfile2.setTotalPoints(120.5);
            passengerProfileRepository.save(passengerProfile2);

            // ─── 3. DRIVER USER (admin-created) ─────────────────────
            User driverUser = new User();
            driverUser.setFullName("Kamal Jayasinghe");
            driverUser.setEmail("driver@demo.com");
            driverUser.setPhoneNumber("0710000002");
            driverUser.setPassword(passwordEncoder.encode("password"));
            driverUser.setToken(java.util.UUID.randomUUID().toString());
            driverUser.setRole(UserRole.DRIVER);
            driverUser.setIsActive(true);
            driverUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(driverUser);

            DriverProfile driverProfile = new DriverProfile();
            driverProfile.setUser(driverUser);
            driverProfile.setDriverCode("DRV-001");
            driverProfile.setLicenseNumber("B1234567");
            driverProfile.setDepot("Colombo");
            driverProfile.setDemeritPoints(2);
            driverProfile.setStatus(DriverStatus.AVAILABLE);
            driverProfileRepository.save(driverProfile);

            // ─── 4. ROUTE & VARIANT ─────────────────────────────────
            Route route = new Route();
            route.setRouteNumber("138");
            route.setDisplayName("Kottawa - Pettah");
            route.setRouteCategory("Main");
            route.setIsActive(true);
            routeRepository.save(route);

            RouteVariant variant = new RouteVariant();
            variant.setRoute(route);
            variant.setVariantCode("138-INBOUND");
            variant.setOriginName("Kottawa");
            variant.setDestinationName("Pettah");
            variant.setDirectionLabel("Inbound");
            variant.setServiceType(ServiceType.NORMAL);
            variant.setIsActive(true);
            routeVariantRepository.save(variant);

            // ─── 5. STOPS ───────────────────────────────────────────
            Stop stop1 = new Stop();
            stop1.setStopCode("STP-001");
            stop1.setStopName("Kottawa Bus Stand");
            stop1.setLatitude(6.8402);
            stop1.setLongitude(79.9654);
            stop1.setIsActive(true);
            stopRepository.save(stop1);

            Stop stop2 = new Stop();
            stop2.setStopCode("STP-002");
            stop2.setStopName("Maharagama");
            stop2.setLatitude(6.8480);
            stop2.setLongitude(79.9270);
            stop2.setIsActive(true);
            stopRepository.save(stop2);

            Stop stop3 = new Stop();
            stop3.setStopCode("STP-003");
            stop3.setStopName("Nugegoda");
            stop3.setLatitude(6.8649);
            stop3.setLongitude(79.8997);
            stop3.setIsActive(true);
            stopRepository.save(stop3);

            Stop stop4 = new Stop();
            stop4.setStopCode("STP-004");
            stop4.setStopName("Pettah");
            stop4.setLatitude(6.9388);
            stop4.setLongitude(79.8542);
            stop4.setIsActive(true);
            stopRepository.save(stop4);

            // ─── 6. ROUTE VARIANT STOPS ─────────────────────────────
            RouteVariantStop rvs1 = new RouteVariantStop();
            rvs1.setRouteVariant(variant);
            rvs1.setStop(stop1);
            rvs1.setStopOrder(1);
            rvs1.setDistanceFromStartKm(0.0);
            rvs1.setIsMajorStop(true);
            routeVariantStopRepository.save(rvs1);

            RouteVariantStop rvs2 = new RouteVariantStop();
            rvs2.setRouteVariant(variant);
            rvs2.setStop(stop2);
            rvs2.setStopOrder(2);
            rvs2.setDistanceFromStartKm(5.2);
            rvs2.setIsMajorStop(true);
            routeVariantStopRepository.save(rvs2);

            RouteVariantStop rvs3 = new RouteVariantStop();
            rvs3.setRouteVariant(variant);
            rvs3.setStop(stop3);
            rvs3.setStopOrder(3);
            rvs3.setDistanceFromStartKm(9.5);
            rvs3.setIsMajorStop(true);
            routeVariantStopRepository.save(rvs3);

            RouteVariantStop rvs4 = new RouteVariantStop();
            rvs4.setRouteVariant(variant);
            rvs4.setStop(stop4);
            rvs4.setStopOrder(4);
            rvs4.setDistanceFromStartKm(18.0);
            rvs4.setIsMajorStop(true);
            routeVariantStopRepository.save(rvs4);

            // ─── 7. FARE RULES ──────────────────────────────────────
            FareRule fareRule1 = new FareRule();
            fareRule1.setRouteVariant(variant);
            fareRule1.setBoardingStop(stop1);
            fareRule1.setDestinationStop(stop2);
            fareRule1.setFareLkr(50.0);
            fareRuleRepository.save(fareRule1);

            FareRule fareRule2 = new FareRule();
            fareRule2.setRouteVariant(variant);
            fareRule2.setBoardingStop(stop1);
            fareRule2.setDestinationStop(stop3);
            fareRule2.setFareLkr(80.0);
            fareRuleRepository.save(fareRule2);

            FareRule fareRule3 = new FareRule();
            fareRule3.setRouteVariant(variant);
            fareRule3.setBoardingStop(stop1);
            fareRule3.setDestinationStop(stop4);
            fareRule3.setFareLkr(120.0);
            fareRuleRepository.save(fareRule3);

            // ─── 8. BUS & QR ────────────────────────────────────────
            Bus bus1 = new Bus();
            bus1.setBusCode("BUS-100");
            bus1.setRegistrationNumber("NC-4455");
            bus1.setBusDisplayName("Lanka Leyland Express");
            bus1.setCapacity(54);
            bus1.setOperatorName("SLTB");
            bus1.setStatus(BusStatus.ACTIVE);
            busRepository.save(bus1);

            Bus bus2 = new Bus();
            bus2.setBusCode("BUS-101");
            bus2.setRegistrationNumber("NB-1892");
            bus2.setBusDisplayName("Colombo City Liner");
            bus2.setCapacity(42);
            bus2.setOperatorName("Private");
            bus2.setStatus(BusStatus.ACTIVE);
            busRepository.save(bus2);

            Bus bus3 = new Bus();
            bus3.setBusCode("BUS-102");
            bus3.setRegistrationNumber("ND-5541");
            bus3.setBusDisplayName("Southern Express");
            bus3.setCapacity(50);
            bus3.setOperatorName("SLTB");
            bus3.setStatus(BusStatus.ACTIVE);
            busRepository.save(bus3);

            // QR code for bus1 (admin-generated demo)
            BusQrCode qrCode = new BusQrCode();
            qrCode.setBus(bus1);
            qrCode.setQrToken("DEMO-QR-TOKEN-BUS100");
            qrCode.setQrLabel("QR-BUS-100");
            qrCode.setIsActive(true);
            busQrCodeRepository.save(qrCode);

            // ─── 9. ACTIVE ASSIGNMENT ───────────────────────────────
            BusAssignment assignment = new BusAssignment();
            assignment.setBus(bus1);
            assignment.setDriverProfile(driverProfile);
            assignment.setRouteVariant(variant);
            assignment.setAssignmentStatus(AssignmentStatus.ACTIVE);
            assignment.setStartedAt(LocalDateTime.now());
            busAssignmentRepository.save(assignment);

            Complaint complaint = new Complaint();
            complaint.setPassengerName(passengerUser.getFullName());
            complaint.setPassengerContact(passengerUser.getEmail());
            complaint.setBusReference("BUS-100");
            complaint.setSubject("Overcrowding");
            complaint.setDescription("The bus was too crowded during the evening trip.");
            complaint.setStatus("OPEN");
            complaint.setCreatedAt(LocalDateTime.now().minusHours(5));
            complaintRepository.save(complaint);

            LostItemReport lostItem = new LostItemReport();
            lostItem.setItemName("Black Backpack");
            lostItem.setReporterName(passengerUser2.getFullName());
            lostItem.setReporterContact(passengerUser2.getEmail());
            lostItem.setRouteOrBus("Route 138 / BUS-100");
            lostItem.setNotes("Lost near Maharagama around 6 PM.");
            lostItem.setStatus("OPEN");
            lostItem.setCreatedAt(LocalDateTime.now().minusHours(2));
            lostItemReportRepository.save(lostItem);

            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("  TransitShield Demo Data Seeded Successfully");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("  Admin:     admin@transitshield.com / admin123");
            System.out.println("  Passenger: passenger@demo.com / password");
            System.out.println("  Driver:    driver@demo.com / password");
            System.out.println("═══════════════════════════════════════════════════");
        }
    }
}

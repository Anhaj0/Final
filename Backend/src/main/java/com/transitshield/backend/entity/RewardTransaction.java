package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.RewardTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_profile_id", nullable = false)
    private PassengerProfile passengerProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardTransactionType type;

    @Column(nullable = false)
    private Double points;

    private String description;

    // Optional relation for transferring points to/from another user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_user_id")
    private PassengerProfile relatedUser;

    // Optional relation if it's earned from a trip
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_trip_id")
    private PassengerTrip relatedTrip;

    private LocalDateTime createdAt = LocalDateTime.now();
}

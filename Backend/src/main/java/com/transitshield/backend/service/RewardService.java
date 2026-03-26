package com.transitshield.backend.service;

import com.transitshield.backend.dto.RewardTransactionDto;
import com.transitshield.backend.dto.PassengerBalanceDto;
import com.transitshield.backend.dto.TransferRequest;
import com.transitshield.backend.entity.PassengerProfile;
import com.transitshield.backend.entity.PassengerTrip;
import com.transitshield.backend.entity.RewardTransaction;
import com.transitshield.backend.entity.enums.RewardTransactionType;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.PassengerProfileRepository;
import com.transitshield.backend.repository.RewardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final PassengerProfileRepository profileRepository;
    private final RewardTransactionRepository transactionRepository;

    @Transactional
    public void earnPointsFromTrip(PassengerProfile profile, PassengerTrip trip, Double pointsEarned) {
        if (pointsEarned <= 0.0) return;

        profile.setTotalPoints(readPoints(profile) + pointsEarned);
        profileRepository.save(profile);

        RewardTransaction transaction = new RewardTransaction();
        transaction.setPassengerProfile(profile);
        transaction.setType(RewardTransactionType.EARNED);
        transaction.setPoints(pointsEarned);
        transaction.setDescription("Points earned from completed trip");
        transaction.setRelatedTrip(trip);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    @Transactional
    public RewardTransactionDto transferPoints(Long senderId, TransferRequest request) {
        if (request.getAmount() <= 0.0) {
            throw new BadRequestException("Transfer amount must be greater than zero");
        }

        PassengerProfile sender = profileRepository.findByUserId(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender profile not found"));

        if (sender.getPublicUserId() != null && sender.getPublicUserId().equalsIgnoreCase(request.getRecipientPublicId())) {
            throw new BadRequestException("You cannot transfer points to your own account");
        }

        if (readPoints(sender) < request.getAmount()) {
            throw new BadRequestException("Insufficient balance");
        }
        
        if (readPoints(sender) - request.getAmount() < 20.0) {
            throw new BadRequestException("You must retain at least 20 points after transfer");
        }

        PassengerProfile recipient = profileRepository.findByPublicUserId(request.getRecipientPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        // Deduct from sender
        sender.setTotalPoints(readPoints(sender) - request.getAmount());
        profileRepository.save(sender);

        RewardTransaction outTransaction = new RewardTransaction();
        outTransaction.setPassengerProfile(sender);
        outTransaction.setType(RewardTransactionType.TRANSFER_OUT);
        outTransaction.setPoints(request.getAmount());
        outTransaction.setDescription("Transferred to " + recipient.getUser().getFullName() + " (" + recipient.getPublicUserId() + ")");
        outTransaction.setRelatedUser(recipient);
        outTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(outTransaction);

        // Add to recipient
        recipient.setTotalPoints(readPoints(recipient) + request.getAmount());
        profileRepository.save(recipient);

        RewardTransaction inTransaction = new RewardTransaction();
        inTransaction.setPassengerProfile(recipient);
        inTransaction.setType(RewardTransactionType.TRANSFER_IN);
        inTransaction.setPoints(request.getAmount());
        inTransaction.setDescription("Received from " + sender.getUser().getFullName() + " (" + sender.getPublicUserId() + ")");
        inTransaction.setRelatedUser(sender);
        inTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(inTransaction);

        return mapToDto(outTransaction);
    }

    public List<RewardTransactionDto> getHistory(Long userId) {
        PassengerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        return transactionRepository.findByPassengerProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public String getUserPublicId(Long userId) {
        PassengerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return profile.getPublicUserId();
    }
    
    public Double getBalance(Long userId) {
        PassengerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return readPoints(profile);
    }

    public PassengerBalanceDto getBalanceSnapshot(Long userId) {
        PassengerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        PassengerBalanceDto dto = new PassengerBalanceDto();
        dto.setFullName(profile.getUser() != null ? profile.getUser().getFullName() : null);
        dto.setPublicUserId(profile.getPublicUserId());
        dto.setWalletBalance(readWallet(profile));
        dto.setTotalPoints(readPoints(profile));
        return dto;
    }
    
    // --- Admin endpoints ---
    public List<PassengerProfile> getAllBalances() {
        return profileRepository.findAll();
    }
    
    public List<RewardTransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private RewardTransactionDto mapToDto(RewardTransaction t) {
        RewardTransactionDto dto = new RewardTransactionDto();
        dto.setId(t.getId());
        dto.setType(t.getType().name());
        dto.setPoints(t.getPoints());
        dto.setDescription(t.getDescription());
        dto.setCreatedAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        return dto;
    }

    private Double readPoints(PassengerProfile profile) {
        return profile.getTotalPoints() != null ? profile.getTotalPoints() : 0.0;
    }

    private Double readWallet(PassengerProfile profile) {
        return profile.getWalletBalance() != null ? profile.getWalletBalance() : 0.0;
    }
}

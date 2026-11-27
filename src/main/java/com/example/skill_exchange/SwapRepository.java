package com.example.skill_exchange;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwapRepository extends JpaRepository<SwapRequest, Long> {

    List<SwapRequest> findByRequesterIdOrReceiverIdOrderByCreatedAtDesc(Long requesterId, Long receiverId);

    boolean existsByRequesterIdAndReceiverIdAndSkillOfferedAndSkillRequestedAndStatus(
            Long requesterId, Long receiverId, String skillOffered, String skillRequested, SwapStatus status);

    void deleteByRequesterIdOrReceiverId(Long requesterId, Long receiverId);
}

package com.example.skill_exchange;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "swap_requests")
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requesterId;
    private Long receiverId;
    private String skillOffered;
    private String skillRequested;

    @Enumerated(EnumType.STRING)
    private SwapStatus status = SwapStatus.PENDING;

    private Instant createdAt = Instant.now();

    public SwapRequest() {
    }

    public SwapRequest(Long requesterId, Long receiverId, String skillOffered, String skillRequested) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.skillOffered = skillOffered;
        this.skillRequested = skillRequested;
    }

    public Long getId() { return id; }
    public Long getRequesterId() { return requesterId; }
    public Long getReceiverId() { return receiverId; }
    public String getSkillOffered() { return skillOffered; }
    public String getSkillRequested() { return skillRequested; }
    public SwapStatus getStatus() { return status; }
    public void setStatus(SwapStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}

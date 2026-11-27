package com.example.skill_exchange;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SwapService {

    public record SwapView(Long id, Long requesterId, String requesterName,
                           Long receiverId, String receiverName,
                           String skillOffered, String skillRequested,
                           SwapStatus status, Instant createdAt) {
    }

    private final SwapRepository swaps;
    private final StudentRepository students;

    public SwapService(SwapRepository swaps, StudentRepository students) {
        this.swaps = swaps;
        this.students = students;
    }

    @Transactional
    public SwapView create(Long requesterId, Long receiverId, String offered, String requested) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("You can't send a swap request to yourself.");
        }
        Student requester = find(requesterId);
        Student receiver = find(receiverId);
        String give = StudentService.normalizeSkill(offered);
        String get = StudentService.normalizeSkill(requested);

        if (!requester.getSkillsToTeach().contains(give)) {
            throw new IllegalArgumentException("You can only offer a skill you teach.");
        }
        if (!receiver.getSkillsToTeach().contains(get)) {
            throw new IllegalArgumentException(receiver.getName() + " doesn't teach " + get + ".");
        }
        if (swaps.existsByRequesterIdAndReceiverIdAndSkillOfferedAndSkillRequestedAndStatus(
                requesterId, receiverId, give, get, SwapStatus.PENDING)) {
            throw new IllegalArgumentException("You already have a pending request for this swap.");
        }
        return view(swaps.save(new SwapRequest(requesterId, receiverId, give, get)));
    }

    @Transactional(readOnly = true)
    public List<SwapView> forStudent(Long studentId) {
        find(studentId);
        return swaps.findByRequesterIdOrReceiverIdOrderByCreatedAtDesc(studentId, studentId)
                .stream().map(this::view).toList();
    }

    /**
     * PENDING -> ACCEPTED or REJECTED (only by the receiver); ACCEPTED -> COMPLETED (either person).
     */
    @Transactional
    public SwapView updateStatus(Long swapId, Long actorId, SwapStatus next) {
        SwapRequest swap = swaps.findById(swapId)
                .orElseThrow(() -> new NotFoundException("Swap request not found."));
        boolean isReceiver = swap.getReceiverId().equals(actorId);
        if (!isReceiver && !swap.getRequesterId().equals(actorId)) {
            throw new ForbiddenException("You're not part of this swap.");
        }
        SwapStatus current = swap.getStatus();
        boolean allowed =
                (current == SwapStatus.PENDING && (next == SwapStatus.ACCEPTED || next == SwapStatus.REJECTED))
                        || (current == SwapStatus.ACCEPTED && next == SwapStatus.COMPLETED);
        if (!allowed) {
            throw new IllegalArgumentException("A " + current + " request can't become " + next + ".");
        }
        if (current == SwapStatus.PENDING && !isReceiver) {
            throw new ForbiddenException("Only the person who received the request can respond to it.");
        }
        swap.setStatus(next);
        return view(swaps.save(swap));
    }

    private Student find(Long id) {
        return students.findById(id).orElseThrow(() -> new NotFoundException("Student ID " + id + " not found."));
    }

    private SwapView view(SwapRequest s) {
        String from = students.findById(s.getRequesterId()).map(Student::getName).orElse("Deleted user");
        String to = students.findById(s.getReceiverId()).map(Student::getName).orElse("Deleted user");
        return new SwapView(s.getId(), s.getRequesterId(), from, s.getReceiverId(), to,
                s.getSkillOffered(), s.getSkillRequested(), s.getStatus(), s.getCreatedAt());
    }
}

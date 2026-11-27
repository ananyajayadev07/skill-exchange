package com.example.skill_exchange;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SwapController {

    private final SwapService service;

    public SwapController(SwapService service) {
        this.service = service;
    }

    record CreateSwapRequest(@NotNull Long requesterId, @NotNull Long receiverId,
                             @NotBlank String skillOffered, @NotBlank String skillRequested) {}

    record StatusRequest(@NotNull SwapStatus status) {}

    @PostMapping("/swaps")
    @ResponseStatus(HttpStatus.CREATED)
    public SwapService.SwapView create(@Valid @RequestBody CreateSwapRequest r) {
        return service.create(r.requesterId(), r.receiverId(), r.skillOffered(), r.skillRequested());
    }

    @GetMapping("/students/{id}/swaps")
    public List<SwapService.SwapView> forStudent(@PathVariable Long id) {
        return service.forStudent(id);
    }

    @PatchMapping("/swaps/{id}")
    public SwapService.SwapView updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest r) {
        return service.updateStatus(id, r.status());
    }
}

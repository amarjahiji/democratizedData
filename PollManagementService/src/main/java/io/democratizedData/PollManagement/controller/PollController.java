package io.democratizedData.PollManagement.controller;

import io.democratizedData.PollManagement.model.dto.CreatePollRequest;
import io.democratizedData.PollManagement.model.dto.PollResponse;
import io.democratizedData.PollManagement.serviceImpl.PollServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollServiceImpl pollService;

    public PollController(PollServiceImpl pollService) {
        this.pollService = pollService;
    }

    @PostMapping
    public ResponseEntity<?> createPoll(
            @RequestBody CreatePollRequest request,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only administrators can create polls"));
        }

        PollResponse createdPoll = pollService.createPoll(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPoll);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PollResponse>> getActivePolls(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Username") String username,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-City") String userCity,
            @RequestHeader("X-User-Gender") String userGender) {

        List<PollResponse> activePolls = pollService.getActivePolls();
        return ResponseEntity.ok(activePolls);
    }
}
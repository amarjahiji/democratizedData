package io.democratizedData.PollManagement.controller;

import io.democratizedData.PollManagement.model.dto.CreatePollRequest;
import io.democratizedData.PollManagement.model.dto.PollResponse;
import io.democratizedData.PollManagement.service.PollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @PostMapping
    public ResponseEntity<PollResponse> createPoll(@RequestBody CreatePollRequest request) {
        PollResponse createdPoll = pollService.createPoll(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPoll);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PollResponse>> getActivePolls() {
        List<PollResponse> activePolls = pollService.getActivePolls();
        return ResponseEntity.ok(activePolls);
    }
}

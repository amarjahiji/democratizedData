package io.democratizedData.VoteService.controller;

import io.democratizedData.VoteService.model.dto.PollSaveDto;
import io.democratizedData.VoteService.service.PollValkeyService;
import jakarta.websocket.server.PathParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/valkey/poll")
public class PollValkeyController {

    private final PollValkeyService pollValkeyService;

    public PollValkeyController(PollValkeyService pollValkeyService) {
        this.pollValkeyService = pollValkeyService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> savePoll(@RequestBody PollSaveDto pollSaveDto) {
        pollValkeyService.savePollVote(pollSaveDto.getPollId(), pollSaveDto.getOption());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{poll_id}/get")
    public ResponseEntity<?> getPollVote(@PathVariable("poll_id") String pollId) {
        return ResponseEntity.ok(pollValkeyService.getPollResults(pollId));
    }

}
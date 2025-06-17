package io.democratizedData.VoteService.controller;

import io.democratizedData.VoteService.model.dto.PollSaveDto;
import io.democratizedData.VoteService.model.entity.PollVoteEntity;
import io.democratizedData.VoteService.service.PollDatabaseService;
import io.democratizedData.VoteService.service.PollValkeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/poll")
public class PollValkeyController {

    private final PollValkeyService pollValkeyService;
    private final PollDatabaseService pollDatabaseService;

    public PollValkeyController(PollValkeyService pollValkeyService, PollDatabaseService pollDatabaseService) {
        this.pollValkeyService = pollValkeyService;
        this.pollDatabaseService = pollDatabaseService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> savePoll(@RequestBody PollSaveDto pollSaveDto) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> valkeyFuture = executor.submit(() -> {
                pollValkeyService.savePollVote(pollSaveDto.getPollId(), pollSaveDto.getOption());
            });
            Future<?> dbFuture = executor.submit(() -> {
                pollDatabaseService.savePollVoteToDatabase(pollSaveDto);
            });
            valkeyFuture.get();
            dbFuture.get();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving poll vote");
        } finally {
            executor.shutdown();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{poll_id}/get")
    public ResponseEntity<?> getPollVote(@PathVariable("poll_id") String pollId) {
        return ResponseEntity.ok(pollValkeyService.getPollResults(pollId));
    }

    @GetMapping("/votes")
    public ResponseEntity<List<PollVoteEntity>> getAllVotes() {
        List<PollVoteEntity> votes = pollDatabaseService.getAllVotes();
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/{poll_id}/votes")
    public ResponseEntity<List<PollVoteEntity>> getVotesByPollId(@PathVariable("poll_id") String pollId) {
        List<PollVoteEntity> votes = pollDatabaseService.getVotesByPollId(pollId);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/{poll_id}/votes/{option}")
    public ResponseEntity<List<PollVoteEntity>> getVotesByPollIdAndOption(
            @PathVariable("poll_id") String pollId,
            @PathVariable("option") String option) {
        List<PollVoteEntity> votes = pollDatabaseService.getVotesByPollIdAndOption(pollId, option);
        return ResponseEntity.ok(votes);
    }

}
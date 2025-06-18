package io.democratizedData.VoteService.controller;

import io.democratizedData.VoteService.model.dto.PollSaveDto;
import io.democratizedData.VoteService.model.entity.PollVote;
import io.democratizedData.VoteService.service.PollDatabaseService;
import io.democratizedData.VoteService.service.PollValkeyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/poll")
public class PollValkeyController {

    private final PollValkeyService pollValkeyService;
    private final PollDatabaseService pollDatabaseService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public PollValkeyController(PollValkeyService pollValkeyService, PollDatabaseService pollDatabaseService) {
        this.pollValkeyService = pollValkeyService;
        this.pollDatabaseService = pollDatabaseService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> savePoll(@RequestBody PollSaveDto pollSaveDto) {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> valkeyFuture = executor.submit(() -> {
                pollValkeyService.savePollVote(pollSaveDto.getPollId(), pollSaveDto.getOption());
            });
            Future<?> dbFuture = executor.submit(() -> {
                pollDatabaseService.savePollVoteToDatabase(pollSaveDto);
            });
            valkeyFuture.get();
            dbFuture.get();
//            broadcastPollUpdate(pollSaveDto.getPollId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving poll vote");
        } finally {
            executor.shutdown();
        }
        return ResponseEntity.ok().build();
    }

    //need frontend for this, streams updates from server to client (vote count updates)
    @GetMapping(value = "/{poll_id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPollResults(@PathVariable("poll_id") String pollId) {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        try {
            emitter.send(pollValkeyService.getPollResults(pollId));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    public void broadcastPollUpdate(String pollId) {
        Map<String, Integer> results = pollValkeyService.getPollResults(pollId);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(results);
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    @GetMapping("/{poll_id}/get")
    public ResponseEntity<?> getPollVote(@PathVariable("poll_id") String pollId) {
        return ResponseEntity.ok(pollValkeyService.getPollResults(pollId));
    }

    @GetMapping("/votes")
    public ResponseEntity<List<PollVote>> getAllVotes() {
        List<PollVote> votes = pollDatabaseService.getAllVotes();
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/{poll_id}/votes")
    public ResponseEntity<List<PollVote>> getVotesByPollId(@PathVariable("poll_id") String pollId) {
        List<PollVote> votes = pollDatabaseService.getVotesByPollId(pollId);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/{poll_id}/votes/{option}")
    public ResponseEntity<List<PollVote>> getVotesByPollIdAndOption(
            @PathVariable("poll_id") String pollId,
            @PathVariable("option") String option) {
        List<PollVote> votes = pollDatabaseService.getVotesByPollIdAndOption(pollId, option);
        return ResponseEntity.ok(votes);
    }

}
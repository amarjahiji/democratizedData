package io.democratizedData.VoteService.controller;

import io.democratizedData.VoteService.service.PollAnalyticsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics/poll")
public class PollAnalyticsController {
    private final PollAnalyticsService analyticsService;

    public PollAnalyticsController(PollAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{pollId}/votes-over-time")
    public ResponseEntity<?> getVotesOverTime(@PathVariable String pollId) {
        return ResponseEntity.ok(analyticsService.getVotesOverTime(pollId));
    }

    @GetMapping("/{pollId}/percentage-distribution")
    public ResponseEntity<?> getPercentageDistribution(@PathVariable String pollId) {
        return ResponseEntity.ok(analyticsService.getPercentageDistribution(pollId));
    }

    @GetMapping("/{pollId}/demographics")
    public ResponseEntity<?> getDemographics(@PathVariable String pollId) {
        return ResponseEntity.ok(analyticsService.getDemographicsBreakdown(pollId));
    }

    @GetMapping(value = "/{pollId}/votes-over-time/export", produces = "text/csv")
    public ResponseEntity<String> exportVotesOverTimeCsv(@PathVariable String pollId) {
        String csvData = analyticsService.exportVotesOverTimeCsv(pollId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=votes_over_time_" + pollId + ".csv")
                .body(csvData);
    }
}

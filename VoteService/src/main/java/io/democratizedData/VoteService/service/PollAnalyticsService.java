package io.democratizedData.VoteService.service;

import io.democratizedData.VoteService.model.dto.AgeGroupCount;
import io.democratizedData.VoteService.model.dto.GenderCount;
import io.democratizedData.VoteService.model.dto.OptionVotes;
import io.democratizedData.VoteService.model.dto.VotesOverTimeEntry;
import io.democratizedData.VoteService.repository.PollVoteRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PollAnalyticsService {
    private final PollVoteRepository pollVoteRepository;

    public PollAnalyticsService(PollVoteRepository pollVoteRepository) {
        this.pollVoteRepository = pollVoteRepository;
    }

    // Votes per option over time (daily)
    public Map<String, List<VotesOverTimeEntry>> getVotesOverTime(String pollId) {
        List<VotesOverTimeEntry> rows = pollVoteRepository.findVotesOverTime(pollId);

        Map<String, List<VotesOverTimeEntry>> result = new HashMap<>();
        for (VotesOverTimeEntry entry : rows) {
            result.computeIfAbsent(entry.getOption(), k -> new ArrayList<>()).add(entry);
        }
        return result;
    }


    // Percentage distribution per option
    public Map<String, Double> getPercentageDistribution(String pollId) {
        int totalVotes = pollVoteRepository.countTotalVotes(pollId);
        if (totalVotes == 0) {
            return Collections.emptyMap();
        }

        List<OptionVotes> optionVotes = pollVoteRepository.findOptionVotes(pollId);

        Map<String, Double> result = new HashMap<>();
        for (OptionVotes ov : optionVotes) {
            double percentage = ov.getVotes() * 100.0 / totalVotes;
            result.put(ov.getOption(), percentage);
        }
        return result;
    }

    // Demographics breakdown (gender and age groups)
    public Map<String, Map<String, Integer>> getDemographicsBreakdown(String pollId) {
        Map<String, Integer> genderCounts = new HashMap<>();
        for (GenderCount gc : pollVoteRepository.findGenderCounts(pollId)) {
            genderCounts.put(gc.getGender(), (int) gc.getVotes());
        }

        Map<String, Integer> ageGroupCounts = new HashMap<>();
        for (AgeGroupCount agc : pollVoteRepository.findAgeGroupCounts(pollId)) {
            ageGroupCounts.put(agc.getAgeGroup(), (int) agc.getVotes());
        }

        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("gender", genderCounts);
        result.put("ageGroup", ageGroupCounts);
        return result;
    }


    public String exportVotesOverTimeCsv(String pollId) {
        Map<String, List<VotesOverTimeEntry>> votesOverTime = getVotesOverTime(pollId);

        StringBuilder csvBuilder = new StringBuilder("Option,Date,Votes\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Map.Entry<String, List<VotesOverTimeEntry>> entry : votesOverTime.entrySet()) {
            String option = entry.getKey();
            for (VotesOverTimeEntry voteEntry : entry.getValue()) {
                String dateStr = sdf.format(voteEntry.getDate());
                csvBuilder.append(option).append(",")
                        .append(dateStr).append(",")
                        .append(voteEntry.getVotes()).append("\n");
            }
        }

        return csvBuilder.toString();
    }

}

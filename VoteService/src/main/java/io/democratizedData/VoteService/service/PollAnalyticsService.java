package io.democratizedData.VoteService.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PollAnalyticsService {
    private final JdbcTemplate jdbcTemplate;

    public PollAnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Votes per option over time (daily)
    public Map<String, List<VotesOverTimeEntry>> getVotesOverTime(String pollId) {
        String sql = "SELECT option, DATE(timestamp) as vote_date, COUNT(*) as votes " +
                "FROM poll_votes WHERE poll_id = ? GROUP BY option, vote_date ORDER BY vote_date";

        List<VotesOverTimeEntry> rows = jdbcTemplate.query(sql, new Object[]{pollId}, (rs, rowNum) -> {
            return new VotesOverTimeEntry(
                    rs.getString("option"),
                    rs.getDate("vote_date"),
                    rs.getInt("votes")
            );
        });

        // Group by option
        Map<String, List<VotesOverTimeEntry>> result = new HashMap<>();
        for (VotesOverTimeEntry entry : rows) {
            result.computeIfAbsent(entry.option, k -> new ArrayList<>()).add(entry);
        }
        return result;
    }

    // Percentage distribution per option
    public Map<String, Double> getPercentageDistribution(String pollId) {
        String sql = "SELECT option, COUNT(*) as votes FROM poll_votes WHERE poll_id = ? GROUP BY option";
        String totalSql = "SELECT COUNT(*) FROM poll_votes WHERE poll_id = ?";

        int totalVotes = jdbcTemplate.queryForObject(totalSql, Integer.class, pollId);
        if (totalVotes == 0) {
            return Collections.emptyMap();
        }

        List<OptionVotes> optionVotes = jdbcTemplate.query(sql, new Object[]{pollId}, (rs, rowNum) -> {
            return new OptionVotes(rs.getString("option"), rs.getInt("votes"));
        });

        Map<String, Double> result = new HashMap<>();
        for (OptionVotes ov : optionVotes) {
            double percentage = ov.votes * 100.0 / totalVotes;
            result.put(ov.option, percentage);
        }
        return result;
    }

    // Demographics breakdown (gender and age groups)
    public Map<String, Map<String, Integer>> getDemographicsBreakdown(String pollId) {
        Map<String, Integer> genderCounts = getGenderCounts(pollId);
        Map<String, Integer> ageGroupCounts = getAgeGroupCounts(pollId);

        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("gender", genderCounts);
        result.put("ageGroup", ageGroupCounts);
        return result;
    }

    private Map<String, Integer> getGenderCounts(String pollId) {
        String sql = "SELECT gender, COUNT(*) as votes FROM poll_votes WHERE poll_id = ? GROUP BY gender";
        List<GenderCount> rows = jdbcTemplate.query(sql, new Object[]{pollId}, (rs, rowNum) -> {
            return new GenderCount(rs.getString("gender"), rs.getInt("votes"));
        });
        Map<String, Integer> result = new HashMap<>();
        for (GenderCount gc : rows) {
            result.put(gc.gender, gc.votes);
        }
        return result;
    }

    private Map<String, Integer> getAgeGroupCounts(String pollId) {
        String sql = "SELECT " +
                "CASE " +
                " WHEN age < 20 THEN '<20' " +
                " WHEN age BETWEEN 20 AND 29 THEN '20-29' " +
                " WHEN age BETWEEN 30 AND 39 THEN '30-39' " +
                " ELSE '40+' END as age_group, " +
                "COUNT(*) as votes " +
                "FROM poll_votes WHERE poll_id = ? GROUP BY age_group";

        List<AgeGroupCount> rows = jdbcTemplate.query(sql, new Object[]{pollId}, (rs, rowNum) -> {
            return new AgeGroupCount(rs.getString("age_group"), rs.getInt("votes"));
        });

        Map<String, Integer> result = new HashMap<>();
        for (AgeGroupCount agc : rows) {
            result.put(agc.ageGroup, agc.votes);
        }
        return result;
    }

    public String exportVotesOverTimeCsv(String pollId) {
        // Get votes over time grouped by option
        Map<String, List<VotesOverTimeEntry>> votesOverTime = getVotesOverTime(pollId);

        // CSV header
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Option,Date,Votes\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Flatten map to CSV rows
        for (Map.Entry<String, List<VotesOverTimeEntry>> entry : votesOverTime.entrySet()) {
            String option = entry.getKey();
            for (VotesOverTimeEntry voteEntry : entry.getValue()) {
                String dateStr = sdf.format(voteEntry.date);
                csvBuilder.append(option).append(",").append(dateStr).append(",").append(voteEntry.votes).append("\n");
            }
        }

        return csvBuilder.toString();
    }

    // Helper classes for query results
    public static class VotesOverTimeEntry {
        public final String option;
        public final Date date;
        public final int votes;

        public VotesOverTimeEntry(String option, Date date, int votes) {
            this.option = option;
            this.date = date;
            this.votes = votes;
        }
    }

    public static class OptionVotes {
        public final String option;
        public final int votes;

        public OptionVotes(String option, int votes) {
            this.option = option;
            this.votes = votes;
        }
    }

    public static class GenderCount {
        public final String gender;
        public final int votes;

        public GenderCount(String gender, int votes) {
            this.gender = gender;
            this.votes = votes;
        }
    }

    public static class AgeGroupCount {
        public final String ageGroup;
        public final int votes;

        public AgeGroupCount(String ageGroup, int votes) {
            this.ageGroup = ageGroup;
            this.votes = votes;
        }
    }
}

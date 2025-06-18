package io.democratizedData.VoteService.service;

import io.democratizedData.VoteService.model.entity.PollVote;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional()
public class PollAnalyticsService {

    @PersistenceContext
    private EntityManager entityManager;

    // Votes per option over time (daily)
    public Map<String, List<VotesOverTimeEntry>> getVotesOverTime(String pollId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<PollVote> root = query.from(PollVote.class);

        query.select(cb.array(
                        root.get("option"),
                        cb.function("DATE", Date.class, root.get("voteDate")),
                        cb.count(root)
                ))
                .where(cb.equal(root.get("pollId"), pollId))
                .groupBy(root.get("option"), cb.function("DATE", Date.class, root.get("voteDate")))
                .orderBy(cb.asc(cb.function("DATE", Date.class, root.get("voteDate"))));

        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.mapping(
                                row -> new VotesOverTimeEntry((String) row[0], (Date) row[1], ((Long) row[2]).intValue()),
                                Collectors.toList()
                        )
                ));
    }

    // Percentage distribution per option
    public Map<String, Double> getPercentageDistribution(String pollId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<PollVote> root = query.from(PollVote.class);

        // Total votes
        CriteriaQuery<Long> totalQuery = cb.createQuery(Long.class);
        Root<PollVote> totalRoot = totalQuery.from(PollVote.class);
        totalQuery.select(cb.count(totalRoot))
                .where(cb.equal(totalRoot.get("pollId"), pollId));

        Long totalVotes = entityManager.createQuery(totalQuery).getSingleResult();
        if (totalVotes == 0) {
            return Collections.emptyMap();
        }

        // Votes per option
        query.select(cb.array(root.get("option"), cb.count(root)))
                .where(cb.equal(root.get("pollId"), pollId))
                .groupBy(root.get("option"));

        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Long) row[1] * 100.0 / totalVotes)
                ));
    }

    // Demographics breakdown (gender and age groups)
    public Map<String, Map<String, Integer>> getDemographicsBreakdown(String pollId) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("gender", getGenderCounts(pollId));
        result.put("ageGroup", getAgeGroupCounts(pollId));
        return result;
    }

    private Map<String, Integer> getGenderCounts(String pollId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<PollVote> root = query.from(PollVote.class);

        query.select(cb.array(root.get("gender"), cb.count(root)))
                .where(cb.equal(root.get("pollId"), pollId))
                .groupBy(root.get("gender"));

        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    private Map<String, Integer> getAgeGroupCounts(String pollId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<PollVote> root = query.from(PollVote.class);

        // Create the case expression once and reuse it
        Expression<Object> ageGroupExpression = cb.selectCase()
                .when(cb.lessThan(root.get("age"), 20), "<20")
                .when(cb.between(root.get("age"), 20, 29), "20-29")
                .when(cb.between(root.get("age"), 30, 39), "30-39")
                .otherwise("40+");

        query.select(cb.array(ageGroupExpression, cb.count(root)))
                .where(cb.equal(root.get("pollId"), pollId))
                .groupBy(ageGroupExpression); // Use the same expression

        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    public String exportVotesOverTimeCsv(String pollId) {
        Map<String, List<VotesOverTimeEntry>> votesOverTime = getVotesOverTime(pollId);
        StringBuilder csvBuilder = new StringBuilder("Option,Date,Votes\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        votesOverTime.forEach((option, entries) ->
                entries.forEach(entry ->
                        csvBuilder.append(option)
                                .append(",")
                                .append(sdf.format(entry.date))
                                .append(",")
                                .append(entry.votes)
                                .append("\n")
                )
        );

        return csvBuilder.toString();
    }

    // Helper class for query results
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
}
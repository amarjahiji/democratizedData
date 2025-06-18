package io.democratizedData.VoteService.repository;

import io.democratizedData.VoteService.model.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    @Query("SELECT pv.option, DATE(pv.voteDate) as voteDate, COUNT(pv) as votes " +
            "FROM PollVote pv WHERE pv.pollId = :pollId " +
            "GROUP BY pv.option, DATE(pv.voteDate) " +
            "ORDER BY DATE(pv.voteDate)")
    List<VotesOverTimeProjection> findVotesOverTime(@Param("pollId") String pollId);

    @Query("SELECT pv.option, COUNT(pv) as votes " +
            "FROM PollVote pv WHERE pv.pollId = :pollId " +
            "GROUP BY pv.option")
    List<OptionVotesProjection> findVotesByOption(@Param("pollId") String pollId);

    @Query("SELECT COUNT(pv) FROM PollVote pv WHERE pv.pollId = :pollId")
    long countTotalVotes(@Param("pollId") String pollId);

    @Query("SELECT pv.gender, COUNT(pv) as votes " +
            "FROM PollVote pv WHERE pv.pollId = :pollId AND pv.gender IS NOT NULL " +
            "GROUP BY pv.gender")
    List<GenderCountProjection> findGenderCounts(@Param("pollId") String pollId);

    @Query("SELECT " +
            "CASE " +
            "  WHEN pv.age < 20 THEN '<20' " +
            "  WHEN pv.age BETWEEN 20 AND 29 THEN '20-29' " +
            "  WHEN pv.age BETWEEN 30 AND 39 THEN '30-39' " +
            "  ELSE '40+' " +
            "END as ageGroup, " +
            "COUNT(pv) as votes " +
            "FROM PollVote pv WHERE pv.pollId = :pollId AND pv.age IS NOT NULL " +
            "GROUP BY " +
            "CASE " +
            "  WHEN pv.age < 20 THEN '<20' " +
            "  WHEN pv.age BETWEEN 20 AND 29 THEN '20-29' " +
            "  WHEN pv.age BETWEEN 30 AND 39 THEN '30-39' " +
            "  ELSE '40+' " +
            "END")
    List<AgeGroupCountProjection> findAgeGroupCounts(@Param("pollId") String pollId);

    // Projection Interfaces
    interface VotesOverTimeProjection {
        String getOption();
        LocalDate getVoteDate();
        Long getVotes();
    }

    interface OptionVotesProjection {
        String getOption();
        Long getVotes();
    }

    interface GenderCountProjection {
        String getGender();
        Long getVotes();
    }

    interface AgeGroupCountProjection {
        String getAgeGroup();
        Long getVotes();
    }

    List<PollVote> findByPollId(String pollId);

    List<PollVote> findByPollIdAndOption(String pollId, String option);
}
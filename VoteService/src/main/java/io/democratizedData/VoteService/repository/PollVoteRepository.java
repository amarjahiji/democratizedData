package io.democratizedData.VoteService.repository;

import io.democratizedData.VoteService.model.dto.AgeGroupCount;
import io.democratizedData.VoteService.model.dto.GenderCount;
import io.democratizedData.VoteService.model.dto.OptionVotes;
import io.democratizedData.VoteService.model.dto.VotesOverTimeEntry;
import io.democratizedData.VoteService.model.entity.PollVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVoteEntity, Long> {
    List<PollVoteEntity> findByPollId(String pollId);

    List<PollVoteEntity> findByPollIdAndOption(String pollId, String option);


    @Query("SELECT new io.democratizedData.VoteService.model.dto.VotesOverTimeEntry(" +
            "p.option, p.voteDate, COUNT(p)) " +
            "FROM PollVoteEntity p WHERE p.pollId = :pollId " +
            "GROUP BY p.option, p.voteDate ORDER BY p.voteDate")
    List<VotesOverTimeEntry> findVotesOverTime(@Param("pollId") String pollId);

    @Query("SELECT new io.democratizedData.VoteService.model.dto.OptionVotes(" +
            "p.option, COUNT(p)) FROM PollVoteEntity p " +
            "WHERE p.pollId = :pollId GROUP BY p.option")
    List<OptionVotes> findOptionVotes(@Param("pollId") String pollId);

    @Query("SELECT COUNT(p) FROM PollVoteEntity p WHERE p.pollId = :pollId")
    int countTotalVotes(@Param("pollId") String pollId);

    @Query("SELECT new io.democratizedData.VoteService.model.dto.GenderCount(p.gender, COUNT(p)) " +
            "FROM PollVoteEntity p WHERE p.pollId = :pollId GROUP BY p.gender")
    List<GenderCount> findGenderCounts(@Param("pollId") String pollId);

    @Query("SELECT new io.democratizedData.VoteService.model.dto.AgeGroupCount(" +
            "CASE WHEN p.age < 20 THEN '<20' " +
            "     WHEN p.age BETWEEN 20 AND 29 THEN '20-29' " +
            "     WHEN p.age BETWEEN 30 AND 39 THEN '30-39' " +
            "     ELSE '40+' END, COUNT(p)) " +
            "FROM PollVoteEntity p WHERE p.pollId = :pollId " +
            "GROUP BY CASE " +
            "WHEN p.age < 20 THEN '<20' " +
            "WHEN p.age BETWEEN 20 AND 29 THEN '20-29' " +
            "WHEN p.age BETWEEN 30 AND 39 THEN '30-39' " +
            "ELSE '40+' END")
    List<AgeGroupCount> findAgeGroupCounts(@Param("pollId") String pollId);
}

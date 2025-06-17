package io.democratizedData.VoteService.repository;

import io.democratizedData.VoteService.model.entity.PollVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollVoteRepository extends JpaRepository<PollVoteEntity, Long> {
    List<PollVoteEntity> findByPollId(String pollId);

    List<PollVoteEntity> findByPollIdAndOption(String pollId, String option);
}

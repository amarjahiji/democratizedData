package io.democratizedData.VoteService.service;

import io.democratizedData.VoteService.model.dto.PollSaveDto;
import io.democratizedData.VoteService.model.entity.PollVote;
import io.democratizedData.VoteService.repository.PollVoteRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PollDatabaseService {
    private final PollVoteRepository pollVoteRepository;

    public PollDatabaseService(PollVoteRepository pollVoteRepository) {
        this.pollVoteRepository = pollVoteRepository;
    }

    public void savePollVoteToDatabase(PollSaveDto dto) {
        PollVote entity = PollVote.builder()
                .pollId(dto.getPollId())
                .option(dto.getOption())
                .gender(dto.getGender())
                .voteDate(new Date())
                .age(dto.getAge())
                .build();

        pollVoteRepository.save(entity);
    }

    public List<PollVote> getAllVotes() {
        return pollVoteRepository.findAll();
    }

    public List<PollVote> getVotesByPollId(String pollId) {
        return pollVoteRepository.findByPollId(pollId);
    }

    public List<PollVote> getVotesByPollIdAndOption(String pollId, String option) {
        return pollVoteRepository.findByPollIdAndOption(pollId, option);
    }
}

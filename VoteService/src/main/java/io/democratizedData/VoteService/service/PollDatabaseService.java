package io.democratizedData.VoteService.service;

import io.democratizedData.VoteService.model.dto.PollSaveDto;
import io.democratizedData.VoteService.model.entity.PollVoteEntity;
import io.democratizedData.VoteService.repository.PollVoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PollDatabaseService {
    private final PollVoteRepository pollVoteRepository;

    public PollDatabaseService(PollVoteRepository pollVoteRepository) {
        this.pollVoteRepository = pollVoteRepository;
    }

    public void savePollVoteToDatabase(PollSaveDto dto) {
        PollVoteEntity entity = PollVoteEntity.builder()
                .pollId(dto.getPollId())
                .option(dto.getOption())
                .gender(dto.getGender())
                .age(dto.getAge())
                .build();

        pollVoteRepository.save(entity);
    }

    public List<PollVoteEntity> getAllVotes() {
        return pollVoteRepository.findAll();
    }

    public List<PollVoteEntity> getVotesByPollId(String pollId) {
        return pollVoteRepository.findByPollId(pollId);
    }

    public List<PollVoteEntity> getVotesByPollIdAndOption(String pollId, String option) {
        return pollVoteRepository.findByPollIdAndOption(pollId, option);
    }
}

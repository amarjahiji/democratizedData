package io.democratizedData.PollManagement.serviceImpl;

import io.democratizedData.PollManagement.mapper.PollMapper;
import io.democratizedData.PollManagement.model.dto.CreatePollRequest;
import io.democratizedData.PollManagement.model.dto.PollResponse;
import io.democratizedData.PollManagement.model.entity.Poll;
import io.democratizedData.PollManagement.repository.PollRepository;
import io.democratizedData.PollManagement.service.PollService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PollServiceImpl implements PollService {

    private final PollRepository pollRepository;
    private final PollMapper pollMapper;

    public PollServiceImpl(PollRepository pollRepository, PollMapper pollMapper) {
        this.pollRepository = pollRepository;
        this.pollMapper = pollMapper;
    }

    @Override
    @Transactional
    public PollResponse createPoll(CreatePollRequest request) {
        Poll poll = pollMapper.mapToEntity(request);
        pollRepository.save(poll);
        return pollMapper.mapToDto(poll);
    }

    @Override
    public List<PollResponse> getActivePolls() {
        List<Poll> activePolls = pollRepository.findByActiveTrue();
        return pollMapper.mapToDtoList(activePolls);
    }
}

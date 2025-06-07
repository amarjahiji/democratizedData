package io.democratizedData.PollManagement.mapper;

import io.democratizedData.PollManagement.model.dto.CreatePollRequest;
import io.democratizedData.PollManagement.model.dto.PollOptionResponse;
import io.democratizedData.PollManagement.model.dto.PollResponse;
import io.democratizedData.PollManagement.model.entity.Poll;
import io.democratizedData.PollManagement.model.entity.PollOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PollMapper {

    public Poll mapToEntity(CreatePollRequest request) {
        Poll poll = new Poll();
        poll.setId(UUID.randomUUID().toString());
        poll.setTitle(request.getTitle());
        poll.setQuestion(request.getQuestion());
        poll.setCreatedBy(request.getCreatedBy());
        poll.setStartTime(request.getStartTime());
        poll.setEndTime(request.getEndTime());
        poll.setActive(true);

        List<PollOption> options = request.getOptions().stream()
                .map(optionText -> {
                    PollOption opt = new PollOption();
                    opt.setId(UUID.randomUUID().toString());
                    opt.setOptionText(optionText);
                    opt.setPoll(poll);
                    return opt;
                })
                .collect(Collectors.toList());

        poll.setOptions(options);
        return poll;
    }

    public PollResponse mapToDto(Poll poll) {
        return PollResponse.builder()
                .id(poll.getId())
                .title(poll.getTitle())
                .question(poll.getQuestion())
                .startTime(poll.getStartTime())
                .endTime(poll.getEndTime())
                .active(poll.isActive())
                .options(mapOptionListToDto(poll.getOptions()))
                .build();
    }

    public List<PollResponse> mapToDtoList(List<Poll> polls) {
        return polls.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PollOptionResponse mapToDto(PollOption option) {
        return PollOptionResponse.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .build();
    }

    private List<PollOptionResponse> mapOptionListToDto(List<PollOption> options) {
        return options.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
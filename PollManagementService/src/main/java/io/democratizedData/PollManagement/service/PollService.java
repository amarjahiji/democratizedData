package io.democratizedData.PollManagement.service;

import io.democratizedData.PollManagement.model.dto.CreatePollRequest;
import io.democratizedData.PollManagement.model.dto.PollResponse;

import java.util.List;

public interface PollService {
    PollResponse createPoll(CreatePollRequest request);
    List<PollResponse> getActivePolls();

}
